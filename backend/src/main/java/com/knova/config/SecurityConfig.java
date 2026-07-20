package com.knova.config;

import com.knova.domain.User;
import com.knova.service.UserService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    BCryptPasswordEncoder encoder() {
        // 1. 创建 BCrypt 密码编码器，供登录校验和密码摘要生成共用。
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtFilter jwtFilter) throws Exception {
        // 1. 关闭无状态 REST 接口不需要的 CSRF，并启用跨域配置。
        // 2. 禁用服务器 Session，声明公开接口和需要认证的接口。
        // 3. 在用户名密码过滤器前增加 JWT 身份解析过滤器。
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> corsConfiguration()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**", "/api/profile/avatar/**", "/actuator/health")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private CorsConfiguration corsConfiguration() {
        // 1. 创建跨域配置。
        // 2. 允许前端开发地址、HTTP 方法和请求头；生产环境可进一步收紧来源。
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        return configuration;
    }
}

@Component
@RequiredArgsConstructor
class JwtFilter extends OncePerRequestFilter {
    private final UserService userService;
    @Value("${app.jwt-secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 读取 Authorization 请求头并提取 Bearer Token。
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authenticate(authorization.substring("Bearer ".length()));
        }
        // 2. 无论是否携带有效 Token，都继续交给后续安全过滤器决定是否允许访问。
        filterChain.doFilter(request, response);
    }

    /** JWT 无效时保持匿名身份，后续由 Spring Security 统一返回 401。 */
    private void authenticate(String token) {
        try {
            // 1. 校验 JWT 签名和有效期并读取用户名。
            var claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // 2. 再次检查数据库用户存在且启用，防止已禁用账号继续使用旧 Token。
            userService.findByUsername(claims.getSubject())
                    .filter(User::isEnabled)
                    .ifPresent(this::setAuthentication);
        } catch (JwtException ignored) {
            // 非法或过期令牌属于常见认证失败，不在过滤器重复打印日志。
        }
    }

    private void setAuthentication(User user) {
        // 1. 把系统角色转换为 Spring Security 权限。
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
        // 2. 创建认证对象并写入当前请求安全上下文。
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, List.of(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
