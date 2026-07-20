package com.knova.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * dev 环境每次新增或删除向量后，将内存索引写回本地文件。
 */
@Service
@Profile("dev")
@RequiredArgsConstructor
public class LocalVectorStorePersistence implements VectorStorePersistence {
    private final InMemoryEmbeddingStore<TextSegment> embeddingStore;
    @Value("${app.vector.local-file}")
    private String file;

    @Override
    public synchronized void persist() {
        // 1. 把配置路径转换为规范化绝对路径。
        // 2. 将完整内存向量索引序列化到本地文件。
        embeddingStore.serializeToFile(Path.of(file).toAbsolutePath().normalize());
    }
}
