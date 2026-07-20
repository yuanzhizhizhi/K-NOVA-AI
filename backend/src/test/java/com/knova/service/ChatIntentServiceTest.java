package com.knova.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ChatIntentServiceTest {
    private final LocalChatIntentClassifier localClassifier = new LocalChatIntentClassifier();
    private final LlmChatIntentClassifier modelClassifier = mock(LlmChatIntentClassifier.class);
    private final ChatIntentService service = new ChatIntentService(
            localClassifier, modelClassifier, true, true);

    @Test
    void recognizesHighConfidenceSmallTalkWithoutCallingModel() {
        assertEquals(ChatIntent.SMALL_TALK, service.classify("你好"));
        assertEquals(ChatIntent.SMALL_TALK, service.classify("谢谢！"));
        assertEquals(ChatIntent.SMALL_TALK, service.classify("bye"));
        verifyNoInteractions(modelClassifier);
    }

    @Test
    void recognizesProductHelpWithoutCallingModel() {
        assertEquals(ChatIntent.HELP, service.classify("你能做什么？"));
        assertEquals(ChatIntent.HELP, service.classify("支持上传什么文件"));
        assertEquals(ChatIntent.HELP, service.classify("我是谁"));
        assertEquals(ChatIntent.HELP, service.classify("删除自己的知识库"));
        assertEquals(ChatIntent.HELP, service.classify("如何上传知识库文档"));
        verifyNoInteractions(modelClassifier);
    }

    @Test
    void protectsBusinessQuestionsContainingGreeting() {
        assertEquals(ChatIntent.KNOWLEDGE, service.classify("你好，请介绍退款流程"));
        assertEquals(ChatIntent.KNOWLEDGE, service.classify("谢谢，另外合同怎么审批"));
        assertEquals(ChatIntent.KNOWLEDGE, service.classify("怎么退款？"));
        verifyNoInteractions(modelClassifier);
    }

    @Test
    void delegatesUncertainMessageToIntentModel() {
        when(modelClassifier.classify("你今天怎么样")).thenReturn(ChatIntent.SMALL_TALK);

        assertEquals(ChatIntent.SMALL_TALK, service.classify("你今天怎么样"));
        verify(modelClassifier).classify("你今天怎么样");
    }

    @Test
    void defaultsToKnowledgeWhenModelFallbackIsDisabled() {
        ChatIntentService disabledService = new ChatIntentService(
                localClassifier, modelClassifier, true, false);

        assertEquals(ChatIntent.KNOWLEDGE, disabledService.classify("随便聊聊"));
        verifyNoInteractions(modelClassifier);
    }
}
