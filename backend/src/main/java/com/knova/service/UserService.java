package com.knova.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knova.domain.KnowledgeSpaceMember;
import com.knova.domain.KnowledgeSpaceProfile;
import com.knova.domain.User;
import com.knova.repository.KnowledgeSpaceMemberMapper;
import com.knova.repository.KnowledgeSpaceProfileMapper;
import com.knova.repository.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    private final UserMapper userMapper;
    private final KnowledgeSpaceProfileMapper profileMapper;
    private final KnowledgeSpaceMemberMapper memberMapper;
    private final BCryptPasswordEncoder encoder;
    @Value("${app.jwt-secret}")
    private String secret;

    public LoginResult login(String username, String password) {
        User user = findByUsername(username)
                .filter(User::isEnabled)
                .filter(value -> encoder.matches(password, value.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误，或账号已禁用"));
        String token = Jwts.builder().subject(user.getUsername()).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86_400_000L))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
        return new LoginResult(token, user.getDisplayName(), user.getRole(), user.getAvatarUrl());
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username)));
    }

    public List<User> list() {
        return userMapper.selectList(Wrappers.<User>lambdaQuery().orderByAsc(User::getId));
    }

    public User create(String username, String password, String displayName, String role) {
        if (username == null || username.isBlank() || password == null || password.length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名不能为空，密码至少需要 6 位");
        if (findByUsername(username.trim()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(encoder.encode(password));
        user.setDisplayName(displayName == null || displayName.isBlank() ? username.trim() : displayName.trim());
        user.setRole(normalizeRole(role));
        userMapper.insert(user);
        return user;
    }

    public User update(Long id, String displayName, String role, Boolean enabled, String currentUsername) {
        User user = require(id);
        if (displayName != null && !displayName.isBlank()) user.setDisplayName(displayName.trim());
        if (role != null) user.setRole(normalizeRole(role));
        if (enabled != null) {
            if (user.getUsername().equals(currentUsername) && !enabled)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能禁用当前登录账号");
            user.setEnabled(enabled);
        }
        userMapper.updateById(user);
        return user;
    }

    public void resetPassword(Long id, String password) {
        if (password == null || password.length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码至少需要 6 位");
        User user = require(id);
        user.setPassword(encoder.encode(password));
        userMapper.updateById(user);
    }

    public User current(String username) {
        return findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    public void updateAvatar(String username, String avatarUrl) {
        User user = current(username);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = current(username);
        if (!encoder.matches(currentPassword, user.getPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前密码不正确");
        if (newPassword == null || newPassword.length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新密码至少需要 6 位");
        if (encoder.matches(newPassword, user.getPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新密码不能与当前密码相同");
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public void delete(Long id, String currentUsername) {
        User user = require(id);
        if (user.getUsername().equals(currentUsername))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能删除当前登录账号");
        long owned = profileMapper.selectCount(Wrappers.<KnowledgeSpaceProfile>lambdaQuery()
                .eq(KnowledgeSpaceProfile::getOwnerUsername, user.getUsername()));
        if (owned > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该用户仍是知识空间所有者，请先处理所属空间");
        memberMapper.delete(Wrappers.<KnowledgeSpaceMember>lambdaQuery().eq(KnowledgeSpaceMember::getUsername, user.getUsername()));
        userMapper.deleteById(id);
    }

    public void initializeAdmin() {
        if (userMapper.selectCount(null) > 0) return;
        User user = create("admin", "admin123", "知识库管理员", "ADMIN");
        user.setEnabled(true);
        userMapper.updateById(user);
    }

    private User require(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        return user;
    }

    private String normalizeRole(String role) {
        String value = role == null ? "USER" : role.toUpperCase();
        if (!List.of("ADMIN", "USER").contains(value))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "系统角色只能是 ADMIN 或 USER");
        return value;
    }

    public record LoginResult(String token, String name, String role, String avatarUrl) {
    }
}
