package com.knova.service;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LlmChatIntentClassifierTest {
    private final ChatModel chatModel = mock(ChatModel.class);
    private final LlmChatIntentClassifier classifier = new LlmChatIntentClassifier(chatModel);

    @Test
    void parsesWhitelistedIntentIgnoringCaseAndSpaces() {
        assertEquals(ChatIntent.SMALL_TALK, classifier.parse(" small_talk \n"));
        assertEquals(ChatIntent.HELP, classifier.parse("HELP"));
    }

    @Test
    void rejectsExplanatoryOrUnknownOutput() {
        assertEquals(ChatIntent.KNOWLEDGE, classifier.parse("分类是 SMALL_TALK"));
        assertEquals(ChatIntent.KNOWLEDGE, classifier.parse("OTHER"));
        assertEquals(ChatIntent.KNOWLEDGE, classifier.parse(null));
    }

    @Test
    void fallsBackToKnowledgeWhenModelThrowsException() {
        when(chatModel.chat(contains("用户消息：测试"))).thenThrow(new RuntimeException("timeout"));

        assertEquals(ChatIntent.KNOWLEDGE, classifier.classify("测试"));
    }
}
