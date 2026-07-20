package com.knova.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 可预期的业务异常，由全局异常处理器转换为统一接口错误响应。 */
@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        // 1. 保存可展示的业务错误信息。
        // 2. 保存稳定错误码和对应 HTTP 状态供全局异常处理器使用。
        super(message);
        this.code = code;
        this.status = status;
    }

    public static BusinessException badRequest(String code, String message) {
        // 1. 创建表示请求参数或业务规则不合法的 400 异常。
        return new BusinessException(code, message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException notFound(String code, String message) {
        // 1. 创建表示目标业务数据不存在的 404 异常。
        return new BusinessException(code, message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException unauthorized(String code, String message) {
        // 1. 创建表示登录认证失败的 401 异常。
        return new BusinessException(code, message, HttpStatus.UNAUTHORIZED);
    }

    public static BusinessException forbidden(String code, String message) {
        // 1. 创建表示身份有效但无操作权限的 403 异常。
        return new BusinessException(code, message, HttpStatus.FORBIDDEN);
    }

    public static BusinessException conflict(String code, String message) {
        // 1. 创建表示当前资源状态冲突的 409 异常。
        return new BusinessException(code, message, HttpStatus.CONFLICT);
    }
}
