package com.knova.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 本地开发向量库：启动时从文件恢复，变更后由持久化服务写回文件。
 */
@Configuration
@Profile("dev")
public class DevVectorStoreConfig {
    /**
     * 创建本地内存向量库：已有持久化文件时恢复历史向量，否则创建空向量库。
     * 本配置只在 dev 环境生效，不依赖 Milvus。
     */
    @Bean
    InMemoryEmbeddingStore<TextSegment> embeddingStore(
            @Value("${app.vector.local-file}") String file) throws IOException {
        // 1. 规范化本地向量文件路径并创建父目录。
        Path path = Path.of(file).toAbsolutePath().normalize();
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        // 2. 文件存在且非空时恢复历史向量，否则创建空内存向量库。
        return Files.exists(path) && Files.size(path) > 0
                ? InMemoryEmbeddingStore.fromFile(path)
                : new InMemoryEmbeddingStore<>();
    }
}
