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
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain chain(HttpSecurity h, JwtFilter f) throws Exception {
        return h.csrf(x -> x.disable()).cors(c -> c.configurationSource(r -> {
            var x = new CorsConfiguration();
            x.setAllowedOriginPatterns(List.of("*"));
            x.setAllowedMethods(List.of("*"));
            x.setAllowedHeaders(List.of("*"));
            return x;
        })).sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(x -> x.requestMatchers("/api/auth/**", "/api/profile/avatar/**", "/actuator/health").permitAll().anyRequest().authenticated()).addFilterBefore(f, UsernamePasswordAuthenticationFilter.class).build();
    }
}

@Component
@RequiredArgsConstructor
class JwtFilter extends OncePerRequestFilter {
    private final UserService userService;
    @Value("${app.jwt-secret}")
    String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) try {
            var c = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).build().parseSignedClaims(h.substring(7)).getPayload();
            // JWT 合法后仍读取一次当前用户，确保已删除或禁用的账号不能继续访问。
            userService.findByUsername(c.getSubject()).filter(User::isEnabled).ifPresent(u -> SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())))));
        } catch (JwtException ignored) {
        }
        chain.doFilter(req, res);
    }
}
