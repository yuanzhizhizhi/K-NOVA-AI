package com.knova.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 知识问答策略，仅依赖抽象检索接口，不感知 Milvus 或知识库列表细节。 */
@Component
@RequiredArgsConstructor
class KnowledgeAnswerStrategy implements ChatAnswerStrategy {
    private final KnowledgeRetriever knowledgeRetriever;

    @Override
    public ChatIntent intent() {
        // 1. 声明本策略处理需要企业资料支持的知识问题。
        return ChatIntent.KNOWLEDGE;
    }

    @Override
    public ChatAnswerResult answer(ChatAnswerContext context) {
        // 1. 使用当前用户身份查询全部可访问知识库。
        // 2. 执行 RAG 并返回回答正文及引用来源。
        return knowledgeRetriever.retrieveAndAnswer(context.username(), context.question());
    }
}
