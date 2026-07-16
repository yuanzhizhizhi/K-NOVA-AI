package com.knova.log;

import java.lang.annotation.*;

/**
 * 个性化接口日志注解。
 * 未标注的 Controller 接口仍会记录通用日志；标注后可补充业务名称并控制入参、出参。
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiLog {
    /**
     * 业务操作名称，例如“创建知识库”。
     */
    String value() default "";

    /**
     * 所属业务模块，例如“知识库管理”。
     */
    String module() default "";

    /**
     * 是否输出请求参数。
     */
    boolean logArgs() default true;

    /**
     * 是否输出响应结果。
     */
    boolean logResult() default true;
}
