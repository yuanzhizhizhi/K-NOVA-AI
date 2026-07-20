package com.knova.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knova.domain.KnowledgeSpaceMember;
import com.knova.domain.KnowledgeSpaceProfile;
import com.knova.domain.User;
import com.knova.exception.BusinessException;
import com.knova.repository.KnowledgeSpaceMemberMapper;
import com.knova.repository.KnowledgeSpaceProfileMapper;
import com.knova.repository.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 负责用户认证、账号生命周期和用户数据安全规则。
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final long JWT_VALIDITY_MILLISECONDS = 86_400_000L;
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    private static final List<String> SYSTEM_ROLES = List.of(ADMIN_ROLE, USER_ROLE);

    private final UserMapper userMapper;
    private final KnowledgeSpaceProfileMapper profileMapper;
    private final KnowledgeSpaceMemberMapper memberMapper;
    private final BCryptPasswordEncoder encoder;
    @Value("${app.jwt-secret}")
    private String secret;

    /** 校验账号状态与 BCrypt 密码，成功后签发 24 小时有效的 JWT。 */
    public LoginResult login(String username, String password) {
        // 1. 查询启用账号并使用 BCrypt 校验密码。
        User user = findByUsername(username)
                .filter(User::isEnabled)
                .filter(value -> encoder.matches(password, value.getPassword()))
                .orElseThrow(() -> BusinessException.unauthorized("LOGIN_FAILED",
                        "用户名或密码错误，或账号已禁用"));
        // 2. 签发包含用户名和有效期的 JWT。
        String token = Jwts.builder().subject(user.getUsername()).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_VALIDITY_MILLISECONDS))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
        // 3. 返回登录令牌及前端展示所需用户信息。
        return new LoginResult(token, user.getDisplayName(), user.getRole(), user.getAvatarUrl());
    }

    /** 按登录名查询用户，认证过滤器也通过此方法确认用户仍存在且可用。 */
    public Optional<User> findByUsername(String username) {
        // 1. 按唯一用户名查询用户并使用 Optional 表达可能不存在。
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username)));
    }

    /** 返回用户管理页面所需的全部系统用户。 */
    public List<User> list() {
        // 1. 按用户 ID 正序查询全部系统账号。
        return userMapper.selectList(Wrappers.<User>lambdaQuery().orderByAsc(User::getId));
    }

    /** 创建系统账号；负责用户名唯一性、密码长度和角色白名单校验。 */
    public User create(String username, String password, String displayName, String role) {
        // 1. 校验用户名、密码长度和用户名唯一性。
        if (username == null || username.isBlank()
                || password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw BusinessException.badRequest("USER_INPUT_INVALID", "用户名不能为空，密码至少需要 6 位");
        }
        if (findByUsername(username.trim()).isPresent()) {
            throw BusinessException.conflict("USERNAME_ALREADY_EXISTS", "用户名已存在");
        }
        // 2. 生成 BCrypt 密码摘要并规范化展示名称和系统角色。
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(encoder.encode(password));
        user.setDisplayName(displayName == null || displayName.isBlank() ? username.trim() : displayName.trim());
        user.setRole(normalizeRole(role));
        // 3. 保存用户并返回创建结果。
        userMapper.insert(user);
        return user;
    }

    /** 修改用户基本资料、系统角色和启用状态，禁止当前用户禁用自己。 */
    public User update(Long id, String displayName, String role, Boolean enabled, String currentUsername) {
        // 1. 查询目标用户并按非空字段更新资料和角色。
        User user = require(id);
        if (displayName != null && !displayName.isBlank()) {
            user.setDisplayName(displayName.trim());
        }
        if (role != null) {
            user.setRole(normalizeRole(role));
        }
        // 2. 修改启用状态时禁止当前用户禁用自己。
        if (enabled != null) {
            if (user.getUsername().equals(currentUsername) && !enabled) {
                throw BusinessException.badRequest("CURRENT_USER_DISABLE_FORBIDDEN", "不能禁用当前登录账号");
            }
            user.setEnabled(enabled);
        }
        // 3. 保存修改并返回用户。
        userMapper.updateById(user);
        return user;
    }

    /** 管理员重置指定用户密码，明文只在内存中参与 BCrypt 编码。 */
    public void resetPassword(Long id, String password) {
        // 1. 校验新密码长度并查询目标用户。
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw BusinessException.badRequest("PASSWORD_TOO_SHORT", "密码至少需要 6 位");
        }
        // 2. 生成 BCrypt 摘要并更新密码。
        User user = require(id);
        user.setPassword(encoder.encode(password));
        userMapper.updateById(user);
    }

    /** 获取当前登录用户，不存在时统一转换为 404 业务异常。 */
    public User current(String username) {
        // 1. 查询当前登录用户，不存在时抛出业务异常。
        return findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "用户不存在"));
    }

    /** 保存头像访问地址；图片文件本身由 ProfileController 写入文件目录。 */
    public void updateAvatar(String username, String avatarUrl) {
        // 1. 查询当前用户并更新头像访问地址。
        // 2. 保存用户资料变更。
        User user = current(username);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
    }

    /** 用户修改自己的密码，需要先校验当前密码，并禁止新旧密码相同。 */
    public void changePassword(String username, String currentPassword, String newPassword) {
        // 1. 查询当前用户并校验原密码。
        User user = current(username);
        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw BusinessException.badRequest("CURRENT_PASSWORD_INVALID", "当前密码不正确");
        }
        // 2. 校验新密码长度且不能与原密码相同。
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw BusinessException.badRequest("NEW_PASSWORD_TOO_SHORT", "新密码至少需要 6 位");
        }
        if (encoder.matches(newPassword, user.getPassword())) {
            throw BusinessException.badRequest("PASSWORD_UNCHANGED", "新密码不能与当前密码相同");
        }
        // 3. 生成新密码摘要并保存。
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /** 删除用户前检查自删除和空间所有权，并清理普通成员关系。 */
    public void delete(Long id, String currentUsername) {
        // 1. 查询目标用户并禁止删除当前账号。
        User user = require(id);
        if (user.getUsername().equals(currentUsername)) {
            throw BusinessException.badRequest("CURRENT_USER_DELETE_FORBIDDEN", "不能删除当前登录账号");
        }
        // 2. 检查目标用户是否仍拥有知识空间。
        long owned = profileMapper.selectCount(Wrappers.<KnowledgeSpaceProfile>lambdaQuery()
                .eq(KnowledgeSpaceProfile::getOwnerUsername, user.getUsername()));
        if (owned > 0) {
            throw BusinessException.conflict("USER_OWNS_KNOWLEDGE_BASE",
                    "该用户仍是知识空间所有者，请先处理所属空间");
        }
        // 3. 清理普通成员关系后删除用户主记录。
        memberMapper.delete(Wrappers.<KnowledgeSpaceMember>lambdaQuery()
                .eq(KnowledgeSpaceMember::getUsername, user.getUsername()));
        userMapper.deleteById(id);
    }

    /** 系统首次启动且用户表为空时创建默认管理员。 */
    public void initializeAdmin() {
        // 1. 用户表已有数据时跳过初始化。
        if (userMapper.selectCount(null) > 0) {
            return;
        }
        // 2. 创建并启用默认管理员账号。
        User user = create("admin", "admin123", "知识库管理员", ADMIN_ROLE);
        user.setEnabled(true);
        userMapper.updateById(user);
    }

    /** 查询必须存在的用户，减少各业务方法重复的空值判断。 */
    private User require(Long id) {
        // 1. 根据主键查询用户。
        // 2. 用户不存在时抛出稳定业务异常。
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }
        return user;
    }

    /** 将角色统一为大写并限制在 ADMIN、USER 范围内。 */
    private String normalizeRole(String role) {
        // 1. 空角色使用普通用户默认值，其他值统一转换为大写。
        String value = role == null ? USER_ROLE : role.toUpperCase();
        // 2. 校验角色只允许 ADMIN 或 USER。
        if (!SYSTEM_ROLES.contains(value)) {
            throw BusinessException.badRequest("SYSTEM_ROLE_INVALID", "系统角色只能是 ADMIN 或 USER");
        }
        return value;
    }

    public record LoginResult(String token, String name, String role, String avatarUrl) {
    }
}
