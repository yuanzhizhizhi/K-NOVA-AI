package com.knova.controller;

import com.knova.domain.*;
import com.knova.log.ApiLog;
import com.knova.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/** 知识库 REST 接口层，业务规则统一由 Service 承担。 */
@RestController @RequestMapping("/api") @RequiredArgsConstructor
@ApiLog(module = "AI知识库")
public class KnowledgeController {
    private final KnowledgeBaseService baseService;
    private final KnowledgeService knowledgeService;
    private final ConversationService conversationService;

    @GetMapping("/knowledge-bases") @ApiLog("查询知识库列表")
    List<BaseDto> bases() {
        return baseService.list().stream().map(item -> {
            var base = item.base();
            return new BaseDto(base.getId(), base.getName(), base.getDescription(), base.getColor(), item.documentCount());
        }).toList();
    }

    @PostMapping("/knowledge-bases") @ApiLog("创建知识库")
    KnowledgeBase create(@RequestBody BaseInput input) {
        return baseService.create(input.name(), input.description(), input.color());
    }

    @DeleteMapping("/knowledge-bases/{id}") @ApiLog("删除知识库")
    void deleteBase(@PathVariable Long id) { baseService.delete(id); }

    @GetMapping("/knowledge-bases/{id}/documents") @ApiLog("查询知识库文档")
    List<KnowledgeDocument> documents(@PathVariable Long id) { return baseService.listDocuments(id); }

    @PostMapping(value = "/knowledge-bases/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiLog("上传并向量化文档")
    KnowledgeDocument upload(@PathVariable Long id, @RequestPart MultipartFile file) {
        baseService.requireBase(id); return knowledgeService.ingest(id, file);
    }

    @DeleteMapping("/documents/{id}") @ApiLog("删除知识文档")
    void deleteDocument(@PathVariable Long id) { knowledgeService.delete(baseService.requireDocument(id)); }

    @GetMapping("/knowledge-bases/{id}/conversations") @ApiLog("查询对话列表")
    List<ChatConversation> conversations(@PathVariable Long id) {
        baseService.requireBase(id); return conversationService.list(id, username());
    }

    @GetMapping("/conversations/{id}/messages") @ApiLog("查询对话消息")
    List<ChatMessage> messages(@PathVariable Long id) { return conversationService.messages(id, username()); }

    @DeleteMapping("/conversations/{id}") @ApiLog("删除对话")
    void deleteConversation(@PathVariable Long id) { conversationService.delete(id, username()); }

    @PostMapping("/knowledge-bases/{id}/chat") @ApiLog("知识库问答")
    Answer chat(@PathVariable Long id, @RequestBody Ask request) {
        baseService.requireBase(id);
        ChatConversation conversation = request.conversationId() == null
                ? conversationService.create(id, username(), request.question())
                : conversationService.requireOwned(request.conversationId(), username());
        conversationService.addMessage(conversation.getId(), "user", request.question());
        String answer = knowledgeService.ask(id, request.question());
        conversationService.addMessage(conversation.getId(), "assistant", answer);
        return new Answer(conversation.getId(), answer);
    }

    private String username() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    public record BaseDto(Long id, String name, String description, String color, long documents) {}
    public record BaseInput(String name, String description, String color) {}
    public record Ask(Long conversationId, String question) {}
    public record Answer(Long conversationId, String answer) {}
}
