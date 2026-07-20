package com.knova.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** 从当前用户可访问且未归档的全部知识库中执行 RAG 检索。 */
@Slf4j
@Service
@RequiredArgsConstructor
class AccessibleKnowledgeRetriever implements KnowledgeRetriever {
    private static final String EMPTY_KNOWLEDGE_MESSAGE =
            "目前没有可用的知识库，请先进入“知识库管理”创建知识库并上传文档。";

    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeService knowledgeService;

    @Override
    public ChatAnswerResult retrieveAndAnswer(String username, String question) {
        // 1. 查询当前用户有权限且未归档的全部知识库 ID。
        List<Long> baseIds = findAccessibleBaseIds(username);
        // 2. 没有可用知识库时返回明确引导，不调用 Embedding 和向量库。
        if (baseIds.isEmpty()) {
            log.info("跳过知识检索：用户没有可用知识库，用户名={}", username);
            return ChatAnswerResult.withoutSources(EMPTY_KNOWLEDGE_MESSAGE);
        }
        if (log.isDebugEnabled()) {
            log.debug("开始执行知识检索，用户名={}，知识库数量={}", username, baseIds.size());
        }
        // 3. 执行多知识库 RAG 检索并把内部结果转换为统一回答结构。
        KnowledgeService.RagResult result = knowledgeService.ask(baseIds, question);
        return new ChatAnswerResult(result.answer(), result.sources());
    }

    /** 单独保留查询方法，测试时可以直接验证知识库过滤规则。 */
    List<Long> findAccessibleBaseIds(String username) {
        // 1. 查询用户可访问的知识库聚合信息。
        // 2. 排除已归档空间并只保留知识库 ID。
        return knowledgeBaseService.list(username).stream()
                .filter(item -> !item.profile().isArchived())
                .map(item -> item.base().getId())
                .toList();
    }
}
