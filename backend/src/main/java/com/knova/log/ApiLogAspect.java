package com.knova.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 统一记录全部 REST 接口的入参、出参、耗时和异常。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {
    private static final int MAX_LOG_LENGTH = 12_000;
    private static final Set<String> SENSITIVE = Set.of(
            "password", "passwd", "pwd", "token", "authorization", "secret", "apikey", "api_key",
            "accessToken", "refreshToken");
    private final ObjectMapper objectMapper;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logApi(ProceedingJoinPoint point) throws Throwable {
        // 1. 读取请求、目标方法和个性化日志注解配置。
        long started = System.currentTimeMillis();
        HttpServletRequest request = currentRequest();
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        ApiLog settings = resolveSettings(point, method);
        String operation = settings != null && !settings.value().isBlank() ? settings.value() : method.getName();
        String module = settings != null && !settings.module().isBlank()
                ? settings.module() : point.getTarget().getClass().getSimpleName();
        boolean logArgs = settings == null || settings.logArgs();
        boolean logResult = settings == null || settings.logResult();

        // 2. 在接口执行前记录已脱敏的请求信息和入参。
        log.info("接口请求，模块={}，操作={}，请求方法={}，请求路径={}，客户端IP={}，入参={}",
                module, operation, request == null ? "-" : request.getMethod(),
                request == null ? "-" : request.getRequestURI(), clientIp(request),
                logArgs ? serializeArguments(point) : "<disabled>");
        try {
            // 3. 执行 Controller 方法并记录耗时及脱敏出参。
            Object result = point.proceed();
            log.info("接口响应，模块={}，操作={}，状态=成功，耗时毫秒={}，出参={}",
                    module, operation, System.currentTimeMillis() - started,
                    logResult ? safeSerialize(result) : "<disabled>");
            return result;
        } catch (Throwable error) {
            // 4. 接口异常时记录现场信息和完整堆栈，然后保持原异常向上抛出。
            log.error("接口响应，模块={}，操作={}，状态=失败，耗时毫秒={}，异常类型={}，异常信息={}",
                    module, operation, System.currentTimeMillis() - started,
                    error.getClass().getSimpleName(), error.getMessage(), error);
            throw error;
        }
    }

    private ApiLog resolveSettings(ProceedingJoinPoint point, Method method) {
        // 1. 优先读取方法级日志注解。
        ApiLog annotation = AnnotatedElementUtils.findMergedAnnotation(method, ApiLog.class);
        // 2. 方法未配置时回退读取 Controller 类级注解。
        return annotation != null ? annotation
                : AnnotatedElementUtils.findMergedAnnotation(point.getTarget().getClass(), ApiLog.class);
    }

    private String serializeArguments(ProceedingJoinPoint point) {
        // 1. 获取参数名称和值并排除 Servlet 基础设施对象。
        String[] names = ((MethodSignature) point.getSignature()).getParameterNames();
        Map<String, Object> values = new LinkedHashMap<>();
        Object[] args = point.getArgs();
        // 2. 对文件参数生成摘要，对普通参数保留待脱敏对象。
        for (int index = 0; index < args.length; index++) {
            Object value = args[index];
            if (value instanceof ServletRequest || value instanceof ServletResponse) {
                continue;
            }
            String name = names != null && index < names.length ? names[index] : "arg" + index;
            values.put(name, summarize(value));
        }
        // 3. 统一序列化、脱敏并限制日志长度。
        return safeSerialize(values);
    }

    /**
     * 文件只记录名称、大小和类型，绝不把文件正文写入日志。
     */
    private Object summarize(Object value) {
        // 1. 文件只记录名称、大小和类型，禁止把文件正文写入日志。
        if (value instanceof MultipartFile file) {
            return Map.of("fileName", Objects.toString(file.getOriginalFilename(), ""),
                    "size", file.getSize(), "contentType", Objects.toString(file.getContentType(), ""));
        }
        // 2. 其他对象交给统一脱敏序列化流程。
        return value;
    }

    private String safeSerialize(Object value) {
        try {
            // 1. 转换为 JSON 树后递归遮盖敏感字段。
            JsonNode tree = objectMapper.valueToTree(value);
            mask(tree);
            return limit(objectMapper.writeValueAsString(tree));
        } catch (Exception exception) {
            // 2. 序列化失败时使用字符串兜底，日志记录不能影响正常业务。
            return limit(String.valueOf(value));
        }
    }

    private void mask(JsonNode node) {
        // 1. 对象节点逐字段判断敏感名称并递归处理子节点。
        if (node instanceof ObjectNode object) {
            object.properties().forEach(entry -> {
                if (isSensitive(entry.getKey())) {
                    object.put(entry.getKey(), "******");
                } else {
                    mask(entry.getValue());
                }
            });
        // 2. 数组节点递归处理每个元素。
        } else if (node instanceof ArrayNode array) {
            array.forEach(this::mask);
        }
    }

    private boolean isSensitive(String key) {
        // 1. 忽略大小写匹配密码、Token、密钥等敏感字段名。
        return SENSITIVE.stream().anyMatch(value -> value.equalsIgnoreCase(key));
    }

    private String limit(String value) {
        // 1. 日志内容超过上限时截断并追加明确标记。
        return value.length() <= MAX_LOG_LENGTH ? value : value.substring(0, MAX_LOG_LENGTH) + "...<truncated>";
    }

    private HttpServletRequest currentRequest() {
        // 1. 从 Spring 请求上下文安全获取当前 HTTP 请求。
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }

    private String clientIp(HttpServletRequest request) {
        // 1. 没有 HTTP 请求上下文时返回占位符。
        if (request == null) {
            return "-";
        }
        // 2. 优先读取代理转发地址，否则使用直连远端地址。
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
