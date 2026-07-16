package com.knova.log;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;
import org.springframework.web.multipart.MultipartFile;
import java.lang.reflect.Method;
import java.util.*;

/** 统一记录全部 REST 接口的入参、出参、耗时和异常。 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {
    private static final int MAX_LOG_LENGTH = 12_000;
    private static final Set<String> SENSITIVE = Set.of(
            "password", "passwd", "pwd", "token", "authorization", "secret", "apikey", "api_key", "accessToken", "refreshToken");
    private final ObjectMapper objectMapper;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logApi(ProceedingJoinPoint point) throws Throwable {
        long started = System.currentTimeMillis();
        HttpServletRequest request = currentRequest();
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        ApiLog settings = resolveSettings(point, method);
        String operation = settings != null && !settings.value().isBlank() ? settings.value() : method.getName();
        String module = settings != null && !settings.module().isBlank() ? settings.module() : point.getTarget().getClass().getSimpleName();
        boolean logArgs = settings == null || settings.logArgs();
        boolean logResult = settings == null || settings.logResult();

        log.info("API_REQUEST module={} operation={} method={} path={} ip={} args={}",
                module, operation, request == null ? "-" : request.getMethod(),
                request == null ? "-" : request.getRequestURI(), clientIp(request),
                logArgs ? serializeArguments(point) : "<disabled>");
        try {
            Object result = point.proceed();
            log.info("API_RESPONSE module={} operation={} status=SUCCESS durationMs={} result={}",
                    module, operation, System.currentTimeMillis() - started,
                    logResult ? safeSerialize(result) : "<disabled>");
            return result;
        } catch (Throwable error) {
            log.error("API_RESPONSE module={} operation={} status=FAILED durationMs={} exception={} message={}",
                    module, operation, System.currentTimeMillis() - started,
                    error.getClass().getSimpleName(), error.getMessage(), error);
            throw error;
        }
    }

    private ApiLog resolveSettings(ProceedingJoinPoint point, Method method) {
        ApiLog annotation = AnnotatedElementUtils.findMergedAnnotation(method, ApiLog.class);
        return annotation != null ? annotation : AnnotatedElementUtils.findMergedAnnotation(point.getTarget().getClass(), ApiLog.class);
    }

    private String serializeArguments(ProceedingJoinPoint point) {
        String[] names = ((MethodSignature) point.getSignature()).getParameterNames();
        Map<String, Object> values = new LinkedHashMap<>();
        Object[] args = point.getArgs();
        for (int index = 0; index < args.length; index++) {
            Object value = args[index];
            if (value instanceof ServletRequest || value instanceof ServletResponse) continue;
            String name = names != null && index < names.length ? names[index] : "arg" + index;
            values.put(name, summarize(value));
        }
        return safeSerialize(values);
    }

    /** 文件只记录名称、大小和类型，绝不把文件正文写入日志。 */
    private Object summarize(Object value) {
        if (value instanceof MultipartFile file) {
            return Map.of("fileName", Objects.toString(file.getOriginalFilename(), ""),
                    "size", file.getSize(), "contentType", Objects.toString(file.getContentType(), ""));
        }
        return value;
    }

    private String safeSerialize(Object value) {
        try {
            JsonNode tree = objectMapper.valueToTree(value);
            mask(tree);
            return limit(objectMapper.writeValueAsString(tree));
        } catch (Exception exception) {
            return limit(String.valueOf(value));
        }
    }

    private void mask(JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.fields().forEachRemaining(entry -> {
                if (isSensitive(entry.getKey())) object.put(entry.getKey(), "******");
                else mask(entry.getValue());
            });
        } else if (node instanceof ArrayNode array) array.forEach(this::mask);
    }

    private boolean isSensitive(String key) {
        return SENSITIVE.stream().anyMatch(value -> value.equalsIgnoreCase(key));
    }

    private String limit(String value) {
        return value.length() <= MAX_LOG_LENGTH ? value : value.substring(0, MAX_LOG_LENGTH) + "...<truncated>";
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) return "-";
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
