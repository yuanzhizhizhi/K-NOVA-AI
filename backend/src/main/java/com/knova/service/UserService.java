package com.knova.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knova.domain.User;
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
import java.util.Optional;

/** 用户认证与初始化业务，Controller 不直接访问用户表。 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder;
    @Value("${app.jwt-secret}") private String secret;

    /** 校验账号密码并签发 24 小时有效的 JWT。 */
    public LoginResult login(String username, String password) {
        User user = findByUsername(username)
                .filter(value -> encoder.matches(password, value.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
        String token = Jwts.builder().subject(user.getUsername()).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86_400_000L))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
        return new LoginResult(token, user.getDisplayName(), user.getRole());
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username)));
    }

    /** 首次启动时创建演示管理员；已有用户时不会覆盖。 */
    public void initializeAdmin() {
        if (userMapper.selectCount(null) > 0) return;
        User user = new User();
        user.setUsername("admin");
        user.setPassword(encoder.encode("admin123"));
        user.setDisplayName("知识库管理员");
        userMapper.insert(user);
    }

    public record LoginResult(String token, String name, String role) {}
}
