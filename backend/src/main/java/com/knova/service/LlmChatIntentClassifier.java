package com.knova.service;

import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** 使用独立短超时模型对本地规则无法判断的问题做二次分类。 */
@Slf4j
@Component
class LlmChatIntentClassifier {
    private static final String INTENT_PROMPT = """
            你是消息意图分类器，不回答用户问题。

            可选分类：
            - SMALL_TALK：纯问候、致谢、告别、无业务内容的寒暄。
            - HELP：询问当前 AI 的身份、能力或使用方法。
            - KNOWLEDGE：需要从企业知识、文档、制度、流程或业务资料中寻找答案。

            要求：
            1. 只输出一个分类名称。
            2. 包含问候语但同时提出业务问题时，输出 KNOWLEDGE。
            3. 无法确定时输出 KNOWLEDGE。

            用户消息：%s
            """;

    private final ChatModel intentChatModel;

    LlmChatIntentClassifier(@Qualifier("intentChatModel") ChatModel intentChatModel) {
        // 1. 注入专门用于意图分类的短超时模型客户端。
        this.intentChatModel = intentChatModel;
    }

    /** 调用意图模型并统计耗时；模型超时、调用异常或非法输出均降级为 KNOWLEDGE。 */
    ChatIntent classify(String question) {
        // 1. 记录模型分类开始时间，用于统计额外路由延迟。
        long startTime = System.currentTimeMillis();
        try {
            // 2. 组装只允许输出分类名称的提示词并调用模型。
            String output = intentChatModel.chat(INTENT_PROMPT.formatted(question));
            // 3. 对模型输出进行严格白名单解析。
            ChatIntent intent = parse(output);
            log.info("意图模型分类完成，意图={}，耗时毫秒={}",
                    intent, System.currentTimeMillis() - startTime);
            return intent;
        } catch (Exception exception) {
            // 4. 超时或模型异常时记录中文降级日志并默认进入知识问答。
            log.warn("意图模型分类失败，降级意图={}，耗时毫秒={}，异常信息={}",
                    ChatIntent.KNOWLEDGE, System.currentTimeMillis() - startTime, exception.getMessage());
            return ChatIntent.KNOWLEDGE;
        }
    }

    /** 模型输出采用枚举白名单解析，解释性文本或空值均降级为知识问答。 */
    ChatIntent parse(String output) {
        // 1. 空输出直接按知识问答处理。
        if (output == null) {
            return ChatIntent.KNOWLEDGE;
        }
        try {
            // 2. 清理首尾空白并统一大写后，仅接受 ChatIntent 枚举值。
            return ChatIntent.valueOf(output.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            // 3. 解释性文本或未知分类均按知识问答安全降级。
            return ChatIntent.KNOWLEDGE;
        }
    }
}
