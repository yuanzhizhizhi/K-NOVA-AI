package com.knova.controller;

import com.knova.domain.User;
import com.knova.log.ApiLog;
import com.knova.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户管理接口，仅 ADMIN 可以访问。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@ApiLog(module = "用户管理")
public class UserController {
    private final UserService userService;

    /** 返回用户管理页面使用的全部账号，密码等敏感字段不会进入 DTO。 */
    @GetMapping
    @ApiLog("查询用户列表")
    List<UserDto> list() {
        // 1. 查询全部系统用户。
        // 2. 转换为不包含密码摘要的用户 DTO。
        return userService.list().stream().map(UserDto::from).toList();
    }

    /** 创建系统账号并执行用户名、密码长度和角色白名单校验。 */
    @PostMapping
    @ApiLog(value = "创建用户", logResult = true)
    UserDto create(@RequestBody CreateUser input) {
        // 1. 校验用户名、密码和系统角色。
        // 2. 创建用户并返回脱敏后的用户信息。
        return UserDto.from(userService.create(input.username(), input.password(), input.displayName(), input.role()));
    }

    /** 修改展示名称、系统角色或启用状态，未提供的字段保持原值。 */
    @PatchMapping("/{id}")
    @ApiLog("修改用户")
    UserDto update(@PathVariable Long id, @RequestBody UpdateUser input) {
        // 1. 查询目标用户并校验当前管理员不能禁用自己。
        // 2. 更新请求中提供的用户字段并转换为 DTO。
        return UserDto.from(userService.update(id, input.displayName(), input.role(), input.enabled(), username()));
    }

    /** 管理员重置指定用户密码，接口日志禁止记录密码入参和出参。 */
    @PutMapping("/{id}/password")
    @ApiLog(value = "重置用户密码", logResult = false)
    void resetPassword(@PathVariable Long id, @RequestBody ResetPassword input) {
        // 1. 校验新密码长度。
        // 2. 使用 BCrypt 生成摘要并更新目标用户密码。
        userService.resetPassword(id, input.password());
    }

    /** 删除用户；Service 会阻止自删除及仍拥有知识空间的账号被删除。 */
    @DeleteMapping("/{id}")
    @ApiLog("删除用户")
    void delete(@PathVariable Long id) {
        // 1. 校验不能删除当前账号且目标用户不再拥有知识空间。
        // 2. 清理成员关系后删除系统用户。
        userService.delete(id, username());
    }

    private String username() {
        // 1. 从 Spring Security 上下文读取当前管理员用户名。
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public record CreateUser(String username, String password, String displayName, String role) {
    }

    public record UpdateUser(String displayName, String role, Boolean enabled) {
    }

    public record ResetPassword(String password) {
    }

    public record UserDto(Long id, String username, String displayName, String role, boolean enabled,
                          String avatarUrl) {
        static UserDto from(User user) {
            // 1. 仅复制允许前端展示的字段，禁止返回密码摘要。
            return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole(),
                    user.isEnabled(), user.getAvatarUrl());
        }
    }
}
