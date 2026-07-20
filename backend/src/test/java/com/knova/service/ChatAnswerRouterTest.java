package com.knova.service;

import com.knova.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatAnswerRouterTest {

    @Test
    void routesRequestToMatchingStrategy() {
        ChatAnswerStrategy strategy = mock(ChatAnswerStrategy.class);
        ChatAnswerContext context = new ChatAnswerContext("alice", "你好");
        when(strategy.intent()).thenReturn(ChatIntent.SMALL_TALK);
        ChatAnswerResult answer = ChatAnswerResult.withoutSources("你好");
        when(strategy.answer(context)).thenReturn(answer);
        ChatAnswerRouter router = new ChatAnswerRouter(List.of(strategy));

        assertEquals(answer, router.route(ChatIntent.SMALL_TALK, context));
    }

    @Test
    void rejectsMissingStrategyWithBusinessException() {
        ChatAnswerRouter router = new ChatAnswerRouter(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> router.route(ChatIntent.HELP, new ChatAnswerContext("alice", "帮助")));
        assertEquals("CHAT_STRATEGY_NOT_FOUND", exception.getCode());
    }

    @Test
    void rejectsDuplicateIntentAtStartup() {
        ChatAnswerStrategy first = mock(ChatAnswerStrategy.class);
        ChatAnswerStrategy second = mock(ChatAnswerStrategy.class);
        when(first.intent()).thenReturn(ChatIntent.HELP);
        when(second.intent()).thenReturn(ChatIntent.HELP);

        assertThrows(IllegalStateException.class, () -> new ChatAnswerRouter(List.of(first, second)));
    }
}
