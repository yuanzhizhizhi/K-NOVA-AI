package com.knova.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knova.domain.ChatConversation;
import com.knova.domain.ChatMessage;
import com.knova.repository.ChatConversationMapper;
import com.knova.repository.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/**
 * 对话及消息持久化业务，按当前登录用户隔离数据。
 */
@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ChatConversationMapper conversations;
    private final ChatMessageMapper messages;

    public List<ChatConversation> list(Long knowledgeBaseId, String username) {
        return conversations.selectList(Wrappers.<ChatConversation>lambdaQuery()
                .eq(ChatConversation::getKnowledgeBaseId, knowledgeBaseId)
                .eq(ChatConversation::getUsername, username)
                .orderByDesc(ChatConversation::getUpdatedAt));
    }

    public List<ChatMessage> messages(Long conversationId, String username) {
        requireOwned(conversationId, username);
        return messages.selectList(Wrappers.<ChatMessage>lambdaQuery()
                .eq(ChatMessage::getConversationId, conversationId).orderByAsc(ChatMessage::getCreatedAt));
    }

    public ChatConversation create(Long knowledgeBaseId, String username, String firstQuestion) {
        ChatConversation value = new ChatConversation();
        value.setKnowledgeBaseId(knowledgeBaseId);
        value.setUsername(username);
        String title = firstQuestion == null ? "新对话" : firstQuestion.trim();
        value.setTitle(title.substring(0, Math.min(title.length(), 40)));
        conversations.insert(value);
        return value;
    }

    public void addMessage(Long conversationId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        messages.insert(message);
        ChatConversation conversation = conversations.selectById(conversationId);
        // 空白对话在第一次提问后自动使用问题摘要作为标题。
        if ("user".equals(role) && "新对话".equals(conversation.getTitle()) && content != null && !content.isBlank()) {
            String title = content.trim();
            conversation.setTitle(title.substring(0, Math.min(title.length(), 40)));
        }
        conversation.setUpdatedAt(Instant.now());
        conversations.updateById(conversation);
    }

    @Transactional
    public void delete(Long conversationId, String username) {
        requireOwned(conversationId, username);
        messages.delete(Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getConversationId, conversationId));
        conversations.deleteById(conversationId);
    }

    /**
     * 删除知识库时同步清理其全部用户的历史对话。
     */
    @Transactional
    public void deleteByKnowledgeBase(Long knowledgeBaseId) {
        List<ChatConversation> values = conversations.selectList(Wrappers.<ChatConversation>lambdaQuery()
                .eq(ChatConversation::getKnowledgeBaseId, knowledgeBaseId));
        values.forEach(value -> {
            messages.delete(Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getConversationId, value.getId()));
            conversations.deleteById(value.getId());
        });
    }

    public ChatConversation requireOwned(Long id, String username) {
        ChatConversation value = conversations.selectById(id);
        if (value == null || !value.getUsername().equals(username))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "对话不存在");
        return value;
    }
}
