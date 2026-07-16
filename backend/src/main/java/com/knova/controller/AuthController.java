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

    @PostMapping("/login")
    @ApiLog(value = "用户登录", logResult = true)
    Session login(@Valid @RequestBody Login request) {
        var result = userService.login(request.username(), request.password());
        return new Session(result.token(), result.name(), result.role(), result.avatarUrl());
    }

    @Bean
    CommandLineRunner initializeAdmin() {
        return args -> userService.initializeAdmin();
    }

    public record Login(@NotBlank String username, @NotBlank String password) {
    }

    public record Session(String token, String name, String role, String avatarUrl) {
    }
}
