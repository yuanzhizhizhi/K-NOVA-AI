package com.knova.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/** 防止配置文件注释、缩进或占位符调整破坏 Spring Boot YAML 解析。 */
class ApplicationYamlTest {
    private static final List<String> CONFIGURATION_FILES = List.of(
            "application.yml", "application-dev.yml", "application-prod.yml");

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    void loadsAllApplicationYamlFiles() throws IOException {
        for (String fileName : CONFIGURATION_FILES) {
            assertFalse(loader.load(fileName, new ClassPathResource(fileName)).isEmpty());
        }
    }
}
