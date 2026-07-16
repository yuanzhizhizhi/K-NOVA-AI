package com.knova.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.Instant;

/** 文档处理记录，用于追踪解析、切片和向量化状态。 */
@Data
@TableName("knowledge_document")
public class KnowledgeDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long knowledgeBaseId;
    private String fileName;
    private String contentType;
    private long size;
    private int segmentCount;
    private String status = "PROCESSING";
    private String errorMessage;
    private Instant createdAt = Instant.now();
}
