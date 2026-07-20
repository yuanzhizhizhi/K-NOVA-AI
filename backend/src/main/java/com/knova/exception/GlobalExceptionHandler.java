package com.knova.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/** 将领域和应用层业务异常统一转换为稳定的 HTTP 错误协议。 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException exception,
                                                             HttpServletRequest request) {
        // 1. 使用中文参数化日志记录错误码、请求路径和业务信息。
        log.warn("业务请求失败，错误码={}，请求路径={}，错误信息={}",
                exception.getCode(), request.getRequestURI(), exception.getMessage());
        // 2. 构造统一错误响应并使用异常指定的 HTTP 状态返回。
        ApiError body = new ApiError(Instant.now(), exception.getCode(), exception.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(exception.getStatus()).body(body);
    }

    public record ApiError(Instant timestamp, String code, String message, String path) {
    }
}
