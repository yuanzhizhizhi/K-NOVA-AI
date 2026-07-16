package com.knova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KnovaApplication {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        SpringApplication.run(KnovaApplication.class, args);
        System.out.println("启动耗时：" + (System.currentTimeMillis() - start) + "ms");
        System.out.println("启动成功");
    }
}
