package com.knova.controller;

import com.knova.domain.*;
import com.knova.log.ApiLog;
import com.knova.service.ConversationService;
import com.knova.service.KnowledgeBaseService;
import com.knova.service.KnowledgeService;
import com.knova.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * AI 知识库 REST 接口；权限判断和业务规则统一交给 Service 层。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@ApiLog(module = "AI知识库")
public class KnowledgeController {
    private final KnowledgeBaseService baseService;
    private final KnowledgeService knowledgeService;
    private final ConversationService conversationService;
    private final KnowledgeSpaceService spaceService;

    @GetMapping("/knowledge-bases")
    @ApiLog("查询知识空间列表")
    List<BaseDto> bases() {
        return baseService.list(username()).stream().map(item -> new BaseDto(
                item.base().getId(), item.base().getName(), item.base().getDescription(), item.base().getColor(),
                item.documentCount(), item.profile().getIcon(), item.profile().isFavorite(),
                item.profile().isArchived(), item.profile().getSortOrder(), item.role(),
                item.profile().getOwnerUsername())).toList();
    }

    @PostMapping("/knowledge-bases")
    @ApiLog("创建知识空间")
    KnowledgeBase create(@RequestBody BaseInput input) {
        return baseService.create(input.name(), input.description(), input.color(), username());
    }

    @PatchMapping("/knowledge-bases/{id}")
    @ApiLog("更新知识空间配置")
    void update(@PathVariable Long id, @RequestBody SpaceUpdate input) {
        spaceService.update(id, username(), input.icon(), input.color(), input.sortOrder(), input.favorite(),
                input.archived(), input.name(), input.description());
    }

    @DeleteMapping("/knowledge-bases/{id}")
    @ApiLog("删除知识空间")
    void deleteBase(@PathVariable Long id) {
        baseService.delete(id, username());
    }

    @GetMapping("/knowledge-bases/{id}/members")
    @ApiLog("查询空间成员")
    List<KnowledgeSpaceMember> members(@PathVariable Long id) {
        return spaceService.listMembers(id, username());
    }

    @PostMapping("/knowledge-bases/{id}/members")
    @ApiLog("新增或修改空间成员")
    KnowledgeSpaceMember saveMember(@PathVariable Long id, @RequestBody MemberInput input) {
        return spaceService.saveMember(id, username(), input.username(), input.role());
    }

    @DeleteMapping("/knowledge-bases/{id}/members/{memberId}")
    @ApiLog("移除空间成员")
    void removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        spaceService.removeMember(id, username(), memberId);
    }

    @GetMapping("/knowledge-bases/{id}/documents")
    @ApiLog("查询知识空间文档")
    List<KnowledgeDocument> documents(@PathVariable Long id) {
        spaceService.require(id, username(), "OWNER", "EDITOR", "VIEWER");
        return baseService.listDocuments(id);
    }

    @PostMapping(value = "/knowledge-bases/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiLog("上传并向量化文档")
    KnowledgeDocument upload(@PathVariable Long id, @RequestPart MultipartFile file) {
        spaceService.require(id, username(), "OWNER", "EDITOR");
        return knowledgeService.ingest(id, file);
    }

    @DeleteMapping("/documents/{id}")
    @ApiLog("删除知识文档")
    void deleteDocument(@PathVariable Long id) {
        KnowledgeDocument document = baseService.requireDocument(id);
        spaceService.require(document.getKnowledgeBaseId(), username(), "OWNER", "EDITOR");
        knowledgeService.delete(document);
    }

    @GetMapping("/conversations")
    @ApiLog("查询对话列表")
    List<ChatConversation> conversations() {
        return conversationService.list(0L, username());
    }

    @PostMapping("/conversations")
    @ApiLog("创建空白对话")
    ChatConversation createConversation() {
        return conversationService.create(0L, username(), null);
    }

    @GetMapping("/conversations/{id}/messages")
    @ApiLog("查询对话消息")
    List<ChatMessage> messages(@PathVariable Long id) {
        return conversationService.messages(id, username());
    }

    @DeleteMapping("/conversations/{id}")
    @ApiLog("删除对话")
    void deleteConversation(@PathVariable Long id) {
        conversationService.delete(id, username());
    }

    @PostMapping("/chat")
    @ApiLog("AI知识问答")
    Answer chat(@RequestBody Ask request) {
        List<Long> accessibleBaseIds = baseService.list(username()).stream()
                .filter(item -> !item.profile().isArchived())
                .map(item -> item.base().getId())
                .toList();
        ChatConversation conversation = request.conversationId() == null
                ? conversationService.create(0L, username(), request.question())
                : conversationService.requireOwned(request.conversationId(), username());
        if (!Long.valueOf(0L).equals(conversation.getKnowledgeBaseId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该会话不是全局知识问答会话");
        conversationService.addMessage(conversation.getId(), "user", request.question());
        String answer = knowledgeService.ask(accessibleBaseIds, request.question());
        conversationService.addMessage(conversation.getId(), "assistant", answer);
        return new Answer(conversation.getId(), answer);
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public record BaseDto(Long id, String name, String description, String color, long documents,
                          String icon, boolean favorite, boolean archived, int sortOrder,
                          String role, String ownerUsername) {
    }

    public record BaseInput(String name, String description, String color) {
    }

    public record SpaceUpdate(String name, String description, String color, String icon,
                              Integer sortOrder, Boolean favorite, Boolean archived) {
    }

    public record MemberInput(String username, String role) {
    }

    public record Ask(Long conversationId, String question) {
    }

    public record Answer(Long conversationId, String answer) {
    }
}
