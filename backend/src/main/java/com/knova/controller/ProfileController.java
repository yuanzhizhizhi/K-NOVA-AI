package com.knova.controller;

import com.knova.log.ApiLog;
import com.knova.exception.BusinessException;
import com.knova.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * 当前登录用户的头像管理接口。
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@ApiLog(module = "个人资料")
public class ProfileController {
    private static final long MAX_AVATAR_SIZE = 2L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final UserService userService;
    @Value("${app.avatar-dir:./data/avatars}")
    private String avatarDir;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiLog(value = "上传用户头像", logResult = true)
    Avatar upload(@RequestPart MultipartFile file) throws IOException {
        // 1. 校验文件非空、体积限制和允许的图片类型。
        if (file.isEmpty() || file.getSize() > MAX_AVATAR_SIZE
                || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw BusinessException.badRequest("AVATAR_FILE_INVALID",
                    "头像仅支持 JPG、PNG、WEBP，且不能超过 2MB");
        }
        // 2. 创建头像目录并根据 MIME 类型确定安全扩展名。
        Path directory = Paths.get(avatarDir).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        String suffix = switch (file.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        // 3. 使用随机文件名保存头像，避免原始文件名引发路径安全问题。
        String name = UUID.randomUUID().toString().replace("-", "") + suffix;
        Files.copy(file.getInputStream(), directory.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        // 4. 更新当前用户头像地址并返回前端访问 URL。
        String url = "/api/profile/avatar/" + name;
        userService.updateAvatar(username(), url);
        return new Avatar(url);
    }

    @GetMapping("/avatar/{name:.+}")
    @ApiLog(value = "读取用户头像", logArgs = false, logResult = false)
    ResponseEntity<Resource> avatar(@PathVariable String name) throws IOException {
        // 1. 规范化头像目录和目标文件路径，阻止目录穿越。
        Path directory = Paths.get(avatarDir).toAbsolutePath().normalize();
        Path file = directory.resolve(name).normalize();
        if (!file.startsWith(directory) || !Files.isRegularFile(file)) {
            throw BusinessException.notFound("AVATAR_NOT_FOUND", "头像不存在");
        }
        // 2. 检测文件 MIME 类型并设置七天客户端缓存。
        String type = Files.probeContentType(file);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(type == null ? "image/jpeg" : type))
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7))).body(new FileSystemResource(file));
    }

    @PutMapping("/password")
    @ApiLog(value = "修改当前用户密码", logArgs = false, logResult = false)
    void changePassword(@RequestBody PasswordInput input) {
        // 1. 读取当前登录用户。
        // 2. 校验旧密码、新密码长度及新旧密码差异后更新密码摘要。
        userService.changePassword(username(), input.currentPassword(), input.newPassword());
    }

    private String username() {
        // 1. 从 Spring Security 上下文读取已认证用户名。
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public record Avatar(String url) {
    }

    public record PasswordInput(String currentPassword, String newPassword) {
    }
}
