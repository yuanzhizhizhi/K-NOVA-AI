package com.knova.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 为每个请求生成 traceId，方便串联接口日志和异常日志。
 */
@Component
public class RequestTraceFilter extends OncePerRequestFilter {
    public static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 1. 优先接收调用方传入的合法 traceId，否则生成新的随机标识。
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || !traceId.matches("[a-zA-Z0-9_-]{8,64}")) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        // 2. 写入 MDC 和响应头，串联服务端日志与客户端问题反馈。
        MDC.put(TRACE_ID, traceId);
        response.setHeader("X-Trace-Id", traceId);
        try {
            // 3. 执行后续过滤器及接口业务。
            chain.doFilter(request, response);
        } finally {
            // 4. 请求结束后清理线程上下文，避免线程复用导致 traceId 串号。
            MDC.remove(TRACE_ID);
        }
    }
}
