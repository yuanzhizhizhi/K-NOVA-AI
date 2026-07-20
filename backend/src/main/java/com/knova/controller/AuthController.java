package com.knova.controller;

import com.knova.log.ApiLog;
import com.knova.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口层：只负责参数接收与响应转换。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@ApiLog(module = "用户认证")
public class AuthController {
    private final UserService userService;

    /** 校验用户名和密码，成功后返回 JWT、用户展示名称、角色及头像。 */
    @PostMapping("/login")
    @ApiLog(value = "用户登录", logResult = true)
    Session login(@Valid @RequestBody Login request) {
        // 1. 校验账号状态和 BCrypt 密码。
        var result = userService.login(request.username(), request.password());
        // 2. 返回 JWT、展示名称、系统角色和头像地址。
        return new Session(result.token(), result.name(), result.role(), result.avatarUrl());
    }

    /** 应用启动后检查用户表，为全新数据库初始化默认管理员账号。 */
    @Bean
    CommandLineRunner initializeAdmin() {
        // 1. Spring Boot 启动完成后检查用户表。
        // 2. 用户表为空时创建默认管理员，已有用户时不做修改。
        return args -> userService.initializeAdmin();
    }

    public record Login(@NotBlank String username, @NotBlank String password) {
    }

    public record Session(String token, String name, String role, String avatarUrl) {
    }
}
