package com.knova.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** 统一编排本地高置信规则与 LLM 意图分类兜底。 */
@Slf4j
@Service
public class ChatIntentService {
    private final LocalChatIntentClassifier localClassifier;
    private final LlmChatIntentClassifier modelClassifier;
    private final boolean routingEnabled;
    private final boolean modelFallbackEnabled;

    ChatIntentService(LocalChatIntentClassifier localClassifier,
                      LlmChatIntentClassifier modelClassifier,
                      @Value("${app.chat-routing.enabled:true}") boolean routingEnabled,
                      @Value("${app.chat-routing.model-fallback-enabled:true}") boolean modelFallbackEnabled) {
        // 1. 注入本地规则分类器和 LLM 分类器。
        // 2. 保存路由开关及模型兜底开关。
        this.localClassifier = localClassifier;
        this.modelClassifier = modelClassifier;
        this.routingEnabled = routingEnabled;
        this.modelFallbackEnabled = modelFallbackEnabled;
    }

    /** 本地规则优先；规则不确定时调用模型；模型关闭或异常时按知识问题处理。 */
    public ChatIntent classify(String question) {
        // 1. 路由关闭时直接进入系统核心的知识问答流程。
        if (!routingEnabled) {
            return ChatIntent.KNOWLEDGE;
        }
        // 2. 优先使用零成本本地高置信规则。
        // 3. 本地规则返回不确定时调用模型分类。
        return localClassifier.classify(question)
                .map(intent -> {
                    log.info("本地规则意图识别完成，意图={}", intent);
                    return intent;
                })
                .orElseGet(() -> classifyByModel(question));
    }

    private ChatIntent classifyByModel(String question) {
        // 1. 模型兜底关闭时按 KNOWLEDGE 安全降级。
        if (!modelFallbackEnabled) {
            log.info("意图模型兜底已关闭，降级意图={}", ChatIntent.KNOWLEDGE);
            return ChatIntent.KNOWLEDGE;
        }
        // 2. 调用独立短超时意图模型执行二次分类。
        return modelClassifier.classify(question);
    }
}
