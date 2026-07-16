package com.knova.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knova.domain.KnowledgeBase;
import com.knova.domain.KnowledgeDocument;
import com.knova.repository.KnowledgeBaseMapper;
import com.knova.repository.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/** 知识库聚合业务：管理知识库元数据及其文档生命周期。 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {
    private final KnowledgeBaseMapper baseMapper;
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeService knowledgeService;
    private final ConversationService conversationService;

    public List<KnowledgeBaseSummary> list() {
        return baseMapper.selectList(Wrappers.<KnowledgeBase>lambdaQuery().orderByDesc(KnowledgeBase::getCreatedAt))
                .stream().map(base -> new KnowledgeBaseSummary(base, documentMapper.selectCount(
                        Wrappers.<KnowledgeDocument>lambdaQuery().eq(KnowledgeDocument::getKnowledgeBaseId, base.getId())))).toList();
    }

    public KnowledgeBase create(String name, String description, String color) {
        if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "知识库名称不能为空");
        KnowledgeBase base = new KnowledgeBase();
        base.setName(name.trim());
        base.setDescription(description);
        if (color != null && !color.isBlank()) base.setColor(color);
        baseMapper.insert(base);
        return base;
    }

    public List<KnowledgeDocument> listDocuments(Long baseId) {
        requireBase(baseId);
        return documentMapper.selectList(Wrappers.<KnowledgeDocument>lambdaQuery()
                .eq(KnowledgeDocument::getKnowledgeBaseId, baseId).orderByDesc(KnowledgeDocument::getCreatedAt));
    }

    /** 删除知识库前逐个清理 Milvus 向量，避免留下孤儿数据。 */
    @Transactional
    public void delete(Long baseId) {
        listDocuments(baseId).forEach(knowledgeService::delete);
        conversationService.deleteByKnowledgeBase(baseId);
        baseMapper.deleteById(baseId);
    }

    public KnowledgeDocument requireDocument(Long documentId) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文档不存在");
        return document;
    }

    public void requireBase(Long baseId) {
        if (baseMapper.selectById(baseId) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "知识库不存在");
    }

    public record KnowledgeBaseSummary(KnowledgeBase base, long documentCount) {}
}
