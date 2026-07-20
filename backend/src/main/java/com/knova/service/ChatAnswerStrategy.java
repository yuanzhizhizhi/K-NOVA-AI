package com.knova.service;

/** 不同意图回答方式的统一扩展点；新增意图时增加实现即可。 */
public interface ChatAnswerStrategy {
    ChatIntent intent();

    ChatAnswerResult answer(ChatAnswerContext context);
}
