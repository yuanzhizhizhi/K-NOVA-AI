package com.knova.controller;

import com.knova.domain.ChatConversation;
import com.knova.domain.ChatMessage;
import com.knova.domain.KnowledgeBase;
import com.knova.domain.KnowledgeDocument;
import com.knova.domain.KnowledgeSpaceMember;
import com.knova.log.ApiLog;
import com.knova.service.ChatService;
import com.knova.service.ConversationService;
import com.knova.service.KnowledgeBaseService;
import com.knova.service.KnowledgeService;
import com.knova.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.knova.common.ChatConstants.GLOBAL_KNOWLEDGE_BASE_ID;

/** AI 知识库 REST 接口；业务编排、权限和数据规则统一交给 Service 层。 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@ApiLog(module = "AI知识库")
public class KnowledgeController {
    private final KnowledgeBaseService baseService;
    private final KnowledgeService knowledgeService;
    private final ConversationService conversationService;
    private final KnowledgeSpaceService spaceService;
    private final ChatService chatService;

    @GetMapping("/knowledge-bases")
    @ApiLog("查询知识库列表")
    List<BaseDto> bases() {
        // 1. 查询当前用户有权限访问的知识库及聚合信息。
        // 2. 将业务对象转换为前端需要的知识库卡片数据。
        return baseService.list(username()).stream().map(item -> new BaseDto(
                item.base().getId(), item.base().getName(), item.base().getDescription(), item.base().getColor(),
                item.documentCount(), item.profile().getIcon(), item.profile().isFavorite(),
                item.profile().isArchived(), item.profile().getSortOrder(), item.role(),
                item.profile().getOwnerUsername())).toList();
    }

    @PostMapping("/knowledge-bases")
    @ApiLog("创建知识库")
    KnowledgeBase create(@RequestBody BaseInput input) {
        // 1. 读取当前登录用户作为知识库所有者。
        // 2. 创建知识库并初始化所有者权限及展示配置。
        return baseService.create(input.name(), input.description(), input.color(), username());
    }

    @PatchMapping("/knowledge-bases/{id}")
    @ApiLog("更新知识库配置")
    void update(@PathVariable Long id, @RequestBody SpaceUpdate input) {
        // 1. 校验当前用户具备知识库编辑权限。
        // 2. 按请求中提供的字段更新基础信息和个人展示配置。
        spaceService.update(id, username(), input.icon(), input.color(), input.sortOrder(), input.favorite(),
                input.archived(), input.name(), input.description());
    }

    @DeleteMapping("/knowledge-bases/{id}")
    @ApiLog("删除知识库")
    void deleteBase(@PathVariable Long id) {
        // 1. 校验当前用户是知识库所有者。
        // 2. 删除文档向量、历史会话、成员配置和知识库主记录。
        baseService.delete(id, username());
    }

    @GetMapping("/knowledge-bases/{id}/members")
    @ApiLog("查询知识库成员")
    List<KnowledgeSpaceMember> members(@PathVariable Long id) {
        // 1. 校验当前用户至少具备知识库只读权限。
        // 2. 返回知识库全部成员及其空间角色。
        return spaceService.listMembers(id, username());
    }

    @PostMapping("/knowledge-bases/{id}/members")
    @ApiLog("新增或修改知识库成员")
    KnowledgeSpaceMember saveMember(@PathVariable Long id, @RequestBody MemberInput input) {
        // 1. 校验当前用户是知识库所有者。
        // 2. 新增成员或更新已有成员角色。
        return spaceService.saveMember(id, username(), input.username(), input.role());
    }

    @DeleteMapping("/knowledge-bases/{id}/members/{memberId}")
    @ApiLog("移除知识库成员")
    void removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        // 1. 校验当前用户是知识库所有者。
        // 2. 校验目标成员存在且不是所有者后删除成员关系。
        spaceService.removeMember(id, username(), memberId);
    }

    @GetMapping("/knowledge-bases/{id}/documents")
    @ApiLog("查询知识库文档")
    List<KnowledgeDocument> documents(@PathVariable Long id) {
        // 1. 校验当前用户具有知识库访问权限。
        // 2. 按上传时间倒序返回文档处理记录。
        spaceService.require(id, username(), "OWNER", "EDITOR", "VIEWER");
        return baseService.listDocuments(id);
    }

    @PostMapping(value = "/knowledge-bases/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiLog("上传并向量化文档")
    KnowledgeDocument upload(@PathVariable Long id, @RequestPart MultipartFile file) {
        // 1. 校验当前用户具有知识库编辑权限。
        // 2. 解析文件、切片、向量化并保存文档处理结果。
        spaceService.require(id, username(), "OWNER", "EDITOR");
        return knowledgeService.ingest(id, file);
    }

    @DeleteMapping("/documents/{id}")
    @ApiLog("删除知识文档")
    void deleteDocument(@PathVariable Long id) {
        // 1. 查询文档并确认其所属知识库。
        KnowledgeDocument document = baseService.requireDocument(id);
        // 2. 校验当前用户具有知识库编辑权限。
        spaceService.require(document.getKnowledgeBaseId(), username(), "OWNER", "EDITOR");
        // 3. 删除文档向量及关系数据库记录。
        knowledgeService.delete(document);
    }

    @GetMapping("/conversations")
    @ApiLog("查询对话列表")
    List<ChatConversation> conversations() {
        // 1. 查询当前用户的全局知识问答会话。
        // 2. 按最后活跃时间倒序返回会话列表。
        return conversationService.list(GLOBAL_KNOWLEDGE_BASE_ID, username());
    }

    @PostMapping("/conversations")
    @ApiLog("创建空白对话")
    ChatConversation createConversation() {
        // 1. 创建当前用户的空白全局问答会话。
        // 2. 使用默认标题，首次提问后由业务层自动更新标题。
        return conversationService.create(GLOBAL_KNOWLEDGE_BASE_ID, username(), null);
    }

    @GetMapping("/conversations/{id}/messages")
    @ApiLog("查询对话消息")
    List<ChatMessage> messages(@PathVariable Long id) {
        // 1. 校验会话属于当前用户。
        // 2. 按创建时间正序返回完整消息记录。
        return conversationService.messages(id, username());
    }

    @DeleteMapping("/conversations/{id}")
    @ApiLog("删除对话")
    void deleteConversation(@PathVariable Long id) {
        // 1. 校验会话属于当前用户。
        // 2. 在事务内删除会话消息和会话主记录。
        conversationService.delete(id, username());
    }

    @PostMapping("/chat")
    @ApiLog("AI对话")
    Answer chat(@RequestBody Ask request) {
        // 1. 校验或创建当前用户的全局问答会话。
        // 2. 识别用户意图并匹配闲聊、帮助或知识问答策略。
        // 3. 保存用户消息和 AI 回答。
        ChatService.ChatResult result = chatService.chat(username(), request.conversationId(), request.question());
        // 4. 返回会话 ID、回答类型、回答正文和引用文件。
        return new Answer(result.conversationId(), result.answer(), result.intent().name(), result.sources());
    }

    private String username() {
        // 1. 从 Spring Security 上下文读取已通过 JWT 认证的用户名。
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public record BaseDto(Long id, String name, String description, String color, long documents,
                          String icon, boolean favorite, boolean archived, int sortOrder,
                          String role, String ownerUsername) {}
    public record BaseInput(String name, String description, String color) {}
    public record SpaceUpdate(String name, String description, String color, String icon,
                              Integer sortOrder, Boolean favorite, Boolean archived) {}
    public record MemberInput(String username, String role) {}
    public record Ask(Long conversationId, String question) {}
    public record Answer(Long conversationId, String answer, String answerType, List<String> sources) {}
}
