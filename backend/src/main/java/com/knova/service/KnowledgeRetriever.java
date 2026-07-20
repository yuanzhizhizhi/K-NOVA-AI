package com.knova.service;

/** 面向回答策略的最小知识检索接口，使上层不依赖知识库列表和 RAG 实现细节。 */
public interface KnowledgeRetriever {
    ChatAnswerResult retrieveAndAnswer(String username, String question);
}
