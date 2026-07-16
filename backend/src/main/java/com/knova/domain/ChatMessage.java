package com.knova.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.Instant;

/** 对话中的用户问题或 AI 回答。 */
@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO) private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private Instant createdAt = Instant.now();
}
