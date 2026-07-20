package com.knova;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** K-NOVA-AI 服务启动入口。 */
@Slf4j
@SpringBootApplication
public class KnovaApplication {
    public static void main(String[] args) {
        // 1. 记录应用启动开始时间。
        long startTime = System.currentTimeMillis();
        // 2. 创建 Spring Boot 容器并完成全部 Bean 初始化。
        SpringApplication.run(KnovaApplication.class, args);
        // 3. 使用中文参数化日志记录启动成功及总耗时。
        log.info("K-NOVA-AI 启动成功，耗时毫秒={}", System.currentTimeMillis() - startTime);
    }
}
