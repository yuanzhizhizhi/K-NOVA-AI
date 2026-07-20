package com.knova.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knova.domain.KnowledgeBase;
import com.knova.domain.KnowledgeDocument;
import com.knova.domain.KnowledgeSpaceProfile;
import com.knova.exception.BusinessException;
import com.knova.repository.KnowledgeBaseMapper;
import com.knova.repository.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 查询当前用户可访问的知识库，并聚合文档数量、展示配置和空间角色；
     * 结果依次按收藏、归档状态和自定义排序值排列。
     */
    public List<KnowledgeBaseSummary> list(String username) {
        // 1. 查询全部知识库主记录。
        // 2. 聚合当前用户角色、个人展示配置和文档数量。
        // 3. 排除无权限空间，并按收藏、归档和自定义顺序排序。
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

    /** 创建知识库并为创建者初始化 OWNER 权限及默认展示配置。 */
    public KnowledgeBase create(String name, String description, String color, String username) {
        // 1. 校验知识库名称。
        if (name == null || name.isBlank()) {
            throw BusinessException.badRequest("KNOWLEDGE_BASE_NAME_REQUIRED", "知识空间名称不能为空");
        }
        // 2. 保存知识库主记录和可选主题颜色。
        KnowledgeBase base = new KnowledgeBase();
        base.setName(name.trim());
        base.setDescription(description);
        if (color != null && !color.isBlank()) {
            base.setColor(color);
        }
        baseMapper.insert(base);
        // 3. 为创建者初始化 OWNER 成员关系和默认展示配置。
        spaceService.ensureProfile(base.getId(), username);
        return base;
    }

    /** 查询指定知识库的文档处理记录，最新上传的文档优先。 */
    public List<KnowledgeDocument> listDocuments(Long id) {
        // 1. 校验知识库存在。
        // 2. 按上传时间倒序返回文档处理记录。
        requireBase(id);
        return documentMapper.selectList(Wrappers.<KnowledgeDocument>lambdaQuery()
                .eq(KnowledgeDocument::getKnowledgeBaseId, id)
                .orderByDesc(KnowledgeDocument::getCreatedAt));
    }

    /**
     * 删除知识库的完整清理流程：校验 OWNER -> 删除文档向量 ->
     * 清理历史对话 -> 清理成员配置 -> 删除知识库主记录。
     */
    @Transactional
    public void delete(Long id, String username) {
        // 1. 校验当前用户是知识库所有者。
        spaceService.require(id, username, "OWNER");
        // 2. 删除全部文档向量和文档记录。
        listDocuments(id).forEach(knowledgeService::delete);
        // 3. 清理历史会话、成员配置和知识库主记录。
        conversationService.deleteByKnowledgeBase(id);
        spaceService.cleanup(id);
        baseMapper.deleteById(id);
    }

    /** 获取必须存在的文档记录，供文档删除接口复用。 */
    public KnowledgeDocument requireDocument(Long id) {
        // 1. 根据主键查询文档。
        // 2. 文档不存在时抛出稳定业务异常。
        KnowledgeDocument document = documentMapper.selectById(id);
        if (document == null) {
            throw BusinessException.notFound("KNOWLEDGE_DOCUMENT_NOT_FOUND", "文档不存在");
        }
        return document;
    }

    /** 校验知识库主记录是否存在。 */
    public void requireBase(Long id) {
        // 1. 查询并确认知识库主记录存在。
        if (baseMapper.selectById(id) == null) {
            throw BusinessException.notFound("KNOWLEDGE_BASE_NOT_FOUND", "知识空间不存在");
        }
    }

    public record KnowledgeBaseSummary(KnowledgeBase base, long documentCount,
                                       KnowledgeSpaceProfile profile, String role) {
    }
}
