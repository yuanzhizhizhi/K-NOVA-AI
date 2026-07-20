package com.knova.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * LangChain4j 模型装配配置。
 * 分别创建文档向量化模型、RAG 回答模型和短超时意图分类模型。
 */
@Configuration
public class AiConfig {
    /** 文档切片和用户问题共用此模型，确保两者生成的向量处于同一语义空间。 */
    @Bean
    EmbeddingModel embeddingModel(
            @Value("${app.ai.embedding.api-key}") String apiKey,
            @Value("${app.ai.embedding.base-url}") String baseUrl,
            @Value("${app.ai.embedding.model}") String modelName) {
        // 1. 读取 OpenAI 兼容的 Embedding 接口配置。
        // 2. 创建用于文档切片和问题向量化的共享模型客户端。
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /** RAG 主回答模型；标记为 Primary，供未指定 Bean 名称的知识问答服务默认注入。 */
    @Bean
    @Primary
    ChatModel chatModel(
            @Value("${app.ai.chat.api-key}") String apiKey,
            @Value("${app.ai.chat.base-url}") String baseUrl,
            @Value("${app.ai.chat.model}") String modelName) {
        // 1. 读取主聊天模型配置。
        // 2. 使用较低温度保证企业知识回答稳定，并设置远程调用超时。
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.2D)
                .timeout(Duration.ofSeconds(90))
                .build();
    }

    /** 意图分类使用独立短超时模型，避免分类服务阻塞整个问答请求。 */
    @Bean("intentChatModel")
    ChatModel intentChatModel(
            @Value("${app.ai.chat.api-key}") String apiKey,
            @Value("${app.ai.chat.base-url}") String baseUrl,
            @Value("${app.ai.chat.intent-model:${app.ai.chat.model}}") String modelName,
            @Value("${app.chat-routing.intent-timeout-seconds:5}") long timeoutSeconds) {
        // 1. 读取可独立覆盖的意图模型及短超时时间。
        // 2. 使用零温度减少同一消息产生不同分类的概率。
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.0D)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

}
