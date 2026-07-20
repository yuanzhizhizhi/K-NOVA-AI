package com.knova.service;

import com.knova.domain.ChatConversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.knova.common.ChatConstants.ASSISTANT_ROLE;
import static com.knova.common.ChatConstants.USER_ROLE;

/** 统一编排会话创建、意图路由、知识检索及消息持久化。 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    private final ChatRequestValidator requestValidator;
    private final ChatConversationManager conversationManager;
    private final ChatIntentService intentService;
    private final ChatAnswerRouter answerRouter;

    /**
     * 执行一次完整对话：校验参数、确认会话归属、保存用户消息、路由回答并保存 AI 消息。
     *
     * @param username 当前登录用户名，用于隔离会话和知识库权限
     * @param conversationId 已有会话 ID；为空时自动创建全局知识问答会话
     * @param question 用户本次输入
     * @return 会话 ID、回答内容及本次命中的意图类型
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatResult chat(String username, Long conversationId, String question) {
        // 1. 校验当前用户和问题内容，阻止空问题、纯标点和超长输入。
        requestValidator.validate(username, question);
        log.info("开始处理对话，用户名={}，会话ID={}，问题长度={}",
                username, conversationId, question.length());

        // 2. 创建新会话或校验已有会话归属，并保存用户消息。
        ChatConversation conversation = conversationManager.prepare(conversationId, username, question);
        conversationManager.append(conversation.getId(), USER_ROLE, question);
        // 3. 先使用本地规则识别意图，不确定时由 LLM 分类兜底。
        ChatIntent intent = intentService.classify(question);
        if (log.isDebugEnabled()) {
            log.debug("对话意图识别完成，会话ID={}，意图={}", conversation.getId(), intent);
        }

        // 4. 根据意图匹配闲聊、帮助或知识问答策略并生成回答。
        ChatAnswerResult answerResult = answerRouter.route(intent, new ChatAnswerContext(username, question));
        // 5. 保存 AI 回答并返回回答类型及引用来源。
        conversationManager.append(conversation.getId(), ASSISTANT_ROLE, answerResult.answer());
        log.info("对话处理完成，会话ID={}，意图={}，回答长度={}",
                conversation.getId(), intent, answerResult.answer().length());
        return new ChatResult(conversation.getId(), answerResult.answer(), intent, answerResult.sources());
    }

    /** 聊天业务层返回值，控制器会将其转换为对外接口结构。 */
    public record ChatResult(Long conversationId, String answer, ChatIntent intent, List<String> sources) {}
}
