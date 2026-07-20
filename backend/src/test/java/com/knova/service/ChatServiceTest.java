package com.knova.service;

import com.knova.domain.ChatConversation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.knova.common.ChatConstants.ASSISTANT_ROLE;
import static com.knova.common.ChatConstants.USER_ROLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock
    private ChatRequestValidator validator;
    @Mock
    private ChatConversationManager conversationManager;
    @Mock
    private ChatIntentService intentService;
    @Mock
    private ChatAnswerRouter answerRouter;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(validator, conversationManager, intentService, answerRouter);
    }

    @Test
    void orchestratesConversationWithoutKnowingAnswerImplementation() {
        ChatConversation conversation = new ChatConversation();
        conversation.setId(12L);
        String username = "alice";
        String question = "退款流程是什么？";
        String answer = "退款流程如下";

        when(conversationManager.prepare(null, username, question)).thenReturn(conversation);
        when(intentService.classify(question)).thenReturn(ChatIntent.KNOWLEDGE);
        ChatAnswerResult answerResult = new ChatAnswerResult(answer, List.of("退款流程.docx"));
        when(answerRouter.route(ChatIntent.KNOWLEDGE, new ChatAnswerContext(username, question)))
                .thenReturn(answerResult);

        ChatService.ChatResult result = chatService.chat(username, null, question);

        assertEquals(new ChatService.ChatResult(
                12L, answer, ChatIntent.KNOWLEDGE, List.of("退款流程.docx")), result);
        verify(validator).validate(username, question);
        verify(answerRouter).route(ChatIntent.KNOWLEDGE, new ChatAnswerContext(username, question));
        InOrder persistenceOrder = inOrder(conversationManager);
        persistenceOrder.verify(conversationManager).prepare(null, username, question);
        persistenceOrder.verify(conversationManager).append(12L, USER_ROLE, question);
        persistenceOrder.verify(conversationManager).append(12L, ASSISTANT_ROLE, answer);
    }
}
