package com.knova.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 知识库元数据。文档向量本身存放在 Milvus。
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String color = "#6D5EF5";
    private Instant createdAt = Instant.now();
}
