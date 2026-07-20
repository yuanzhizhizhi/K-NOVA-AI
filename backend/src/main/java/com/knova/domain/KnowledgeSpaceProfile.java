package com.knova.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 知识空间的展示、排序、收藏和归档配置。
 */
@Data
@TableName("knowledge_space_profile")
public class KnowledgeSpaceProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long knowledgeBaseId;
    private String ownerUsername;
    private String icon = "📚";
    private int sortOrder;
    private boolean favorite;
    private boolean archived;
    private Instant updatedAt = Instant.now();
}
