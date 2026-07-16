package com.knova.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knova.domain.KnowledgeBase;
import com.knova.domain.KnowledgeDocument;
import com.knova.domain.KnowledgeSpaceProfile;
import com.knova.repository.KnowledgeBaseMapper;
import com.knova.repository.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 聚合知识空间、文档数量及当前用户权限。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {
    private final KnowledgeBaseMapper baseMapper;
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeService knowledgeService;
    private final ConversationService conversationService;
    private final KnowledgeSpaceService spaceService;

    public List<KnowledgeBaseSummary> list(String username) {
        return baseMapper.selectList(Wrappers.<KnowledgeBase>lambdaQuery().orderByDesc(KnowledgeBase::getCreatedAt))
                .stream().map(base -> {
                    KnowledgeSpaceProfile profile = spaceService.ensureProfile(base.getId(), username);
                    String role = spaceService.role(base.getId(), username);
                    long count = documentMapper.selectCount(Wrappers.<KnowledgeDocument>lambdaQuery()
                            .eq(KnowledgeDocument::getKnowledgeBaseId, base.getId()));
                    return role == null ? null : new KnowledgeBaseSummary(base, count, profile, role);
                }).filter(Objects::nonNull)
                .sorted(Comparator.comparing((KnowledgeBaseSummary item) -> !item.profile().isFavorite())
                        .thenComparing(item -> item.profile().isArchived())
                        .thenComparingInt(item -> item.profile().getSortOrder()))
                .toList();
    }

    public KnowledgeBase create(String name, String description, String color, String username) {
        if (name == null || name.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "知识空间名称不能为空");
        KnowledgeBase base = new KnowledgeBase();
        base.setName(name.trim());
        base.setDescription(description);
        if (color != null && !color.isBlank()) base.setColor(color);
        baseMapper.insert(base);
        spaceService.ensureProfile(base.getId(), username);
        return base;
    }

    public List<KnowledgeDocument> listDocuments(Long id) {
        requireBase(id);
        return documentMapper.selectList(Wrappers.<KnowledgeDocument>lambdaQuery()
                .eq(KnowledgeDocument::getKnowledgeBaseId, id)
                .orderByDesc(KnowledgeDocument::getCreatedAt));
    }

    @Transactional
    public void delete(Long id, String username) {
        spaceService.require(id, username, "OWNER");
        listDocuments(id).forEach(knowledgeService::delete);
        conversationService.deleteByKnowledgeBase(id);
        spaceService.cleanup(id);
        baseMapper.deleteById(id);
    }

    public KnowledgeDocument requireDocument(Long id) {
        KnowledgeDocument document = documentMapper.selectById(id);
        if (document == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文档不存在");
        return document;
    }

    public void requireBase(Long id) {
        if (baseMapper.selectById(id) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "知识空间不存在");
    }

    public record KnowledgeBaseSummary(KnowledgeBase base, long documentCount,
                                       KnowledgeSpaceProfile profile, String role) {
    }
}
