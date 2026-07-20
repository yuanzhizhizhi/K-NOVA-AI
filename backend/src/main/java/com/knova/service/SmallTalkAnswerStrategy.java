package com.knova.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 闲聊回答策略，不触发向量检索和模型调用。 */
@Component
@RequiredArgsConstructor
class SmallTalkAnswerStrategy implements ChatAnswerStrategy {
    private final SmallTalkService smallTalkService;

    @Override
    public ChatIntent intent() {
        // 1. 声明本策略只处理纯闲聊意图。
        return ChatIntent.SMALL_TALK;
    }

    @Override
    public ChatAnswerResult answer(ChatAnswerContext context) {
        // 1. 使用本地模板生成闲聊回答。
        // 2. 返回空来源列表，因为闲聊不检索知识库。
        return ChatAnswerResult.withoutSources(smallTalkService.answer(context.question()));
    }
}
