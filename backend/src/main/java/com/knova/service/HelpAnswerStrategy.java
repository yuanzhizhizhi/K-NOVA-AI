package com.knova.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 系统帮助回答策略。 */
@Component
@RequiredArgsConstructor
class HelpAnswerStrategy implements ChatAnswerStrategy {
    private final HelpService helpService;

    @Override
    public ChatIntent intent() {
        // 1. 声明本策略只处理系统帮助意图。
        return ChatIntent.HELP;
    }

    @Override
    public ChatAnswerResult answer(ChatAnswerContext context) {
        // 1. 根据当前用户名和具体问题返回用户身份、AI 身份或系统能力说明。
        // 2. 返回空来源列表，因为帮助回答不检索知识库。
        return ChatAnswerResult.withoutSources(helpService.answer(context.username(), context.question()));
    }
}
