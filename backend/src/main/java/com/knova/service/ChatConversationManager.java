package com.knova.service;

import com.knova.domain.ChatConversation;
import com.knova.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.knova.common.ChatConstants.GLOBAL_KNOWLEDGE_BASE_ID;

/** 负责全局问答会话的创建、归属检查及消息持久化。 */
@Component
@RequiredArgsConstructor
public class ChatConversationManager {
    private final ConversationService conversationService;

    public ChatConversation prepare(Long conversationId, String username, String firstQuestion) {
        // 1. 未指定会话时创建全局问答会话，否则校验已有会话属于当前用户。
        ChatConversation conversation = conversationId == null
                ? conversationService.create(GLOBAL_KNOWLEDGE_BASE_ID, username, firstQuestion)
                : conversationService.requireOwned(conversationId, username);
        // 2. 拒绝旧版单知识库会话进入当前全局知识问答流程。
        if (!isGlobalConversation(conversation)) {
            throw BusinessException.badRequest("CHAT_CONVERSATION_TYPE_INVALID", "该会话不是全局知识问答会话");
        }
        return conversation;
    }

    public void append(Long conversationId, String role, String content) {
        // 1. 保存指定角色的消息。
        // 2. 由 ConversationService 同步更新会话标题和最后活跃时间。
        conversationService.addMessage(conversationId, role, content);
    }

    private boolean isGlobalConversation(ChatConversation conversation) {
        // 1. 使用语义化常量判断会话是否默认查询全部可访问知识库。
        return conversation.getKnowledgeBaseId() != null
                && Long.valueOf(GLOBAL_KNOWLEDGE_BASE_ID).equals(conversation.getKnowledgeBaseId());
    }
}
