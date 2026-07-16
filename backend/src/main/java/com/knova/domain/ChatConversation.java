package com.knova.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.Instant;

/** 一次可继续追问的知识库对话。 */
@Data
@TableName("chat_conversation")
public class ChatConversation {
    @TableId(type = IdType.AUTO) private Long id;
    private Long knowledgeBaseId;
    private String username;
    private String title;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}
