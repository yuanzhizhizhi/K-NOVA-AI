package com.knova.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knova.domain.KnowledgeBase;
import com.knova.domain.KnowledgeSpaceMember;
import com.knova.domain.KnowledgeSpaceProfile;
import com.knova.domain.User;
import com.knova.exception.BusinessException;
import com.knova.repository.KnowledgeBaseMapper;
import com.knova.repository.KnowledgeSpaceMemberMapper;
import com.knova.repository.KnowledgeSpaceProfileMapper;
import com.knova.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 负责知识空间的展示配置、成员关系及角色权限。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeSpaceService {
    private static final String OWNER_ROLE = "OWNER";
    private static final String EDITOR_ROLE = "EDITOR";
    private static final String VIEWER_ROLE = "VIEWER";
    private static final List<String> ASSIGNABLE_ROLES = List.of(EDITOR_ROLE, VIEWER_ROLE);

    private final KnowledgeSpaceProfileMapper profiles;
    private final KnowledgeSpaceMemberMapper members;
    private final KnowledgeBaseMapper bases;
    private final UserMapper users;

    /**
     * 为旧数据补齐空间配置；创建者自动成为 OWNER。
     */
    public KnowledgeSpaceProfile ensureProfile(Long baseId, String username) {
        // 1. 查询知识空间现有展示配置，存在时直接复用。
        KnowledgeSpaceProfile profile = profiles.selectOne(Wrappers.<KnowledgeSpaceProfile>lambdaQuery()
                .eq(KnowledgeSpaceProfile::getKnowledgeBaseId, baseId));
        if (profile != null) {
            return profile;
        }

        // 2. 旧数据缺少配置时创建默认配置并记录所有者。
        profile = new KnowledgeSpaceProfile();
        profile.setKnowledgeBaseId(baseId);
        profile.setOwnerUsername(username);
        profiles.insert(profile);

        // 3. 同步创建 OWNER 成员关系，保证权限模型完整。
        KnowledgeSpaceMember owner = new KnowledgeSpaceMember();
        owner.setKnowledgeBaseId(baseId);
        owner.setUsername(username);
        owner.setRole(OWNER_ROLE);
        members.insert(owner);
        return profile;
    }

    /** 查询用户在指定知识库内的 OWNER、EDITOR 或 VIEWER 角色。 */
    public String role(Long baseId, String username) {
        // 1. 按知识库和用户名查询成员关系。
        // 2. 存在时返回角色，不存在时返回 null 表示无访问权限。
        KnowledgeSpaceMember member = members.selectOne(Wrappers.<KnowledgeSpaceMember>lambdaQuery()
                .eq(KnowledgeSpaceMember::getKnowledgeBaseId, baseId)
                .eq(KnowledgeSpaceMember::getUsername, username));
        return member == null ? null : member.getRole();
    }

    /**
     * 校验当前用户是否拥有指定角色之一。
     */
    public void require(Long baseId, String username, String... allowed) {
        // 1. 查询当前用户在目标知识库中的角色。
        String currentRole = role(baseId, username);
        // 2. 命中任一允许角色时通过校验。
        for (String value : allowed) {
            if (value.equals(currentRole)) {
                return;
            }
        }
        // 3. 未命中时抛出禁止访问业务异常。
        throw BusinessException.forbidden("KNOWLEDGE_BASE_ACCESS_DENIED", "没有知识空间操作权限");
    }

    /** 更新知识库名称、描述及展示配置；只有所有者和编辑者可以调用。 */
    public KnowledgeSpaceProfile update(Long baseId, String username, String icon, String color,
                                        Integer sortOrder, Boolean favorite, Boolean archived,
                                        String name, String description) {
        // 1. 校验当前用户具有所有者或编辑者权限。
        require(baseId, username, OWNER_ROLE, EDITOR_ROLE);
        // 2. 查询并按非空字段更新知识库基础信息。
        KnowledgeBase base = bases.selectById(baseId);
        if (base == null) {
            throw BusinessException.notFound("KNOWLEDGE_BASE_NOT_FOUND", "知识空间不存在");
        }
        if (name != null && !name.isBlank()) {
            base.setName(name.trim());
        }
        if (description != null) {
            base.setDescription(description);
        }
        if (color != null) {
            base.setColor(color);
        }
        bases.updateById(base);

        // 3. 更新当前用户的图标、排序、收藏和归档配置。
        KnowledgeSpaceProfile profile = ensureProfile(baseId, username);
        if (icon != null && !icon.isBlank()) {
            profile.setIcon(icon);
        }
        if (sortOrder != null) {
            profile.setSortOrder(sortOrder);
        }
        if (favorite != null) {
            profile.setFavorite(favorite);
        }
        if (archived != null) {
            profile.setArchived(archived);
        }
        profile.setUpdatedAt(Instant.now());
        profiles.updateById(profile);
        return profile;
    }

    /** 查询知识库成员，查询者至少需要拥有该知识库的只读权限。 */
    public List<KnowledgeSpaceMember> listMembers(Long baseId, String username) {
        // 1. 校验当前用户至少具有只读权限。
        // 2. 按成员 ID 正序返回知识库成员。
        require(baseId, username, OWNER_ROLE, EDITOR_ROLE, VIEWER_ROLE);
        return members.selectList(Wrappers.<KnowledgeSpaceMember>lambdaQuery()
                .eq(KnowledgeSpaceMember::getKnowledgeBaseId, baseId).orderByAsc(KnowledgeSpaceMember::getId));
    }

    /**
     * 只有 OWNER 可以邀请成员或调整成员角色。
     */
    public KnowledgeSpaceMember saveMember(Long baseId, String current, String username, String role) {
        // 1. 校验操作者是知识库所有者并验证目标角色白名单。
        require(baseId, current, OWNER_ROLE);
        if (!ASSIGNABLE_ROLES.contains(role)) {
            throw BusinessException.badRequest("KNOWLEDGE_MEMBER_ROLE_INVALID",
                    "成员角色只能是 EDITOR 或 VIEWER");
        }
        // 2. 校验目标系统用户存在。
        if (users.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username)) == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }

        // 3. 查询现有成员关系，不存在时创建新关系。
        KnowledgeSpaceMember member = members.selectOne(Wrappers.<KnowledgeSpaceMember>lambdaQuery()
                .eq(KnowledgeSpaceMember::getKnowledgeBaseId, baseId)
                .eq(KnowledgeSpaceMember::getUsername, username));
        if (member == null) {
            member = new KnowledgeSpaceMember();
            member.setKnowledgeBaseId(baseId);
            member.setUsername(username);
        }
        // 4. 保存新成员或更新已有成员角色。
        member.setRole(role);
        if (member.getId() == null) {
            members.insert(member);
        } else {
            members.updateById(member);
        }
        return member;
    }

    /** 所有者移除成员；空间 OWNER 本身不能通过成员接口删除。 */
    public void removeMember(Long baseId, String current, Long memberId) {
        // 1. 校验操作者是知识库所有者。
        require(baseId, current, OWNER_ROLE);
        // 2. 校验目标成员属于当前知识库且不是所有者。
        KnowledgeSpaceMember member = members.selectById(memberId);
        if (member == null || !baseId.equals(member.getKnowledgeBaseId())) {
            throw BusinessException.notFound("KNOWLEDGE_MEMBER_NOT_FOUND", "成员不存在");
        }
        if (OWNER_ROLE.equals(member.getRole())) {
            throw BusinessException.badRequest("KNOWLEDGE_OWNER_REMOVE_FORBIDDEN", "不能删除空间所有者");
        }
        // 3. 删除成员关系。
        members.deleteById(memberId);
    }

    /** 删除知识库时清理其成员关系和展示配置。 */
    public void cleanup(Long baseId) {
        // 1. 删除知识库全部成员关系。
        members.delete(Wrappers.<KnowledgeSpaceMember>lambdaQuery()
                .eq(KnowledgeSpaceMember::getKnowledgeBaseId, baseId));
        // 2. 删除知识空间展示配置。
        profiles.delete(Wrappers.<KnowledgeSpaceProfile>lambdaQuery()
                .eq(KnowledgeSpaceProfile::getKnowledgeBaseId, baseId));
    }
}
