package com.knova.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 正式环境向量库：连接独立 Milvus 服务。
 */
@Configuration
@Profile("prod")
public class ProdVectorStoreConfig {
    @Bean
    EmbeddingStore<TextSegment> embeddingStore(
            @Value("${app.milvus.host}") String host,
            @Value("${app.milvus.port}") int port,
            @Value("${app.milvus.collection}") String collection,
            @Value("${app.ai.embedding.dimension}") int dimension) {
        return MilvusEmbeddingStore.builder().host(host).port(port)
                .collectionName(collection).dimension(dimension)
                .indexType(IndexType.AUTOINDEX).metricType(MetricType.COSINE)
                .autoFlushOnInsert(true).build();
    }
}
