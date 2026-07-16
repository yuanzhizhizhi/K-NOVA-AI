package com.knova.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {
    @Bean
    EmbeddingModel embeddingModel(
            @Value("${app.ai.embedding.api-key}") String key,
            @Value("${app.ai.embedding.base-url}") String url,
            @Value("${app.ai.embedding.model}") String model) {
        return OpenAiEmbeddingModel.builder().apiKey(key).baseUrl(url).modelName(model).timeout(Duration.ofSeconds(60)).build();
    }

    @Bean
    ChatModel chatModel(
            @Value("${app.ai.chat.api-key}") String key,
            @Value("${app.ai.chat.base-url}") String url,
            @Value("${app.ai.chat.model}") String model) {
        return OpenAiChatModel.builder().apiKey(key).baseUrl(url).modelName(model).temperature(0.2).timeout(Duration.ofSeconds(90)).build();
    }

}
