package com.knova.service;

/** 回答策略所需的最小上下文，避免策略依赖 Controller 或会话实体。 */
public record ChatAnswerContext(String username, String question) {
}
