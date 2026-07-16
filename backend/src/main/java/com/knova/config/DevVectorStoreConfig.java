package com.knova.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import java.io.IOException;
import java.nio.file.*;

/** 本地开发向量库：启动时从文件恢复，变更后由持久化服务写回文件。 */
@Configuration
@Profile("dev")
public class DevVectorStoreConfig {
    @Bean
    InMemoryEmbeddingStore<TextSegment> embeddingStore(@Value("${app.vector.local-file}") String file) throws IOException {
        Path path = Path.of(file).toAbsolutePath().normalize();
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        return Files.exists(path) && Files.size(path) > 0
                ? InMemoryEmbeddingStore.fromFile(path)
                : new InMemoryEmbeddingStore<>();
    }
}
