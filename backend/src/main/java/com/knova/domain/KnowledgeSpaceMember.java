package com.knova.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 知识空间成员。角色可为 OWNER、EDITOR 或 VIEWER。
 */
@Data
@TableName("knowledge_space_member")
public class KnowledgeSpaceMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long knowledgeBaseId;
    private String username;
    private String role;
    private Instant createdAt = Instant.now();
}
