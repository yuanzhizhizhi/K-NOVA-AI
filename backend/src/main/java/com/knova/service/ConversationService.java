package com.knova.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knova.domain.ChatConversation;
import com.knova.domain.ChatMessage;
import com.knova.exception.BusinessException;
import com.knova.repository.ChatConversationMapper;
import com.knova.repository.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** 按用户隔离查询对话，并按最后更新时间倒序展示。 */
    public List<ChatConversation> list(Long knowledgeBaseId, String username) {
        // 1. 按知识库范围和用户名隔离会话数据。
        // 2. 按最后活跃时间倒序返回列表。
        return conversations.selectList(Wrappers.<ChatConversation>lambdaQuery()
                .eq(ChatConversation::getKnowledgeBaseId, knowledgeBaseId)
                .eq(ChatConversation::getUsername, username)
                .orderByDesc(ChatConversation::getUpdatedAt));
    }

    /** 校验会话归属后，按时间正序返回完整消息记录。 */
    public List<ChatMessage> messages(Long conversationId, String username) {
        // 1. 校验会话存在且属于当前用户。
        // 2. 按创建时间正序查询完整消息历史。
        requireOwned(conversationId, username);
        return messages.selectList(Wrappers.<ChatMessage>lambdaQuery()
                .eq(ChatMessage::getConversationId, conversationId).orderByAsc(ChatMessage::getCreatedAt));
    }

    /** 创建空白或首问会话；首问最多截取 40 个字符作为标题。 */
    public ChatConversation create(Long knowledgeBaseId, String username, String firstQuestion) {
        // 1. 创建会话实体并绑定知识库范围和当前用户。
        ChatConversation value = new ChatConversation();
        value.setKnowledgeBaseId(knowledgeBaseId);
        value.setUsername(username);
        // 2. 有首问时截取摘要作为标题，否则使用默认标题。
        String title = firstQuestion == null ? "新对话" : firstQuestion.trim();
        value.setTitle(title.substring(0, Math.min(title.length(), 40)));
        // 3. 保存会话并返回数据库生成的 ID。
        conversations.insert(value);
        return value;
    }

    /** 保存用户或 AI 消息，同时更新会话标题和最后活跃时间。 */
    public void addMessage(Long conversationId, String role, String content) {
        // 1. 创建并保存用户或 AI 消息。
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        messages.insert(message);
        // 2. 查询会话，在首次用户提问时更新默认标题。
        ChatConversation conversation = conversations.selectById(conversationId);
        // 空白对话在第一次提问后自动使用问题摘要作为标题。
        if ("user".equals(role) && "新对话".equals(conversation.getTitle())
                && content != null && !content.isBlank()) {
            String title = content.trim();
            conversation.setTitle(title.substring(0, Math.min(title.length(), 40)));
        }
        // 3. 刷新最后活跃时间并保存会话。
        conversation.setUpdatedAt(Instant.now());
        conversations.updateById(conversation);
    }

    /** 只允许会话所有者删除，并在同一事务内先删消息再删会话。 */
    @Transactional
    public void delete(Long conversationId, String username) {
        // 1. 校验当前用户拥有目标会话。
        requireOwned(conversationId, username);
        // 2. 先删除从表消息，再删除会话主记录。
        messages.delete(Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getConversationId, conversationId));
        conversations.deleteById(conversationId);
    }

    /**
     * 删除知识库时同步清理其全部用户的历史对话。
     */
    @Transactional
    public void deleteByKnowledgeBase(Long knowledgeBaseId) {
        // 1. 查询知识库关联的全部历史会话。
        List<ChatConversation> values = conversations.selectList(Wrappers.<ChatConversation>lambdaQuery()
                .eq(ChatConversation::getKnowledgeBaseId, knowledgeBaseId));
        // 2. 对每个会话先清理消息，再删除会话主记录。
        values.forEach(value -> {
            messages.delete(Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getConversationId, value.getId()));
            conversations.deleteById(value.getId());
        });
    }

    /** 校验会话存在且属于当前用户，避免越权读取和删除他人对话。 */
    public ChatConversation requireOwned(Long id, String username) {
        // 1. 根据主键查询会话。
        ChatConversation value = conversations.selectById(id);
        // 2. 会话不存在或不属于当前用户时统一返回不存在，避免泄露他人数据。
        if (value == null || !value.getUsername().equals(username))
            throw BusinessException.notFound("CONVERSATION_NOT_FOUND", "对话不存在");
        return value;
    }
}
