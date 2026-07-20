package com.knova.service;

import com.knova.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatRequestValidatorTest {
    private static final int MAX_QUESTION_LENGTH = 4000;
    private final ChatRequestValidator validator = new ChatRequestValidator(MAX_QUESTION_LENGTH);

    @Test
    void rejectsBlankQuestionWithStableBusinessCode() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> validator.validate("alice", "  "));
        assertEquals("CHAT_QUESTION_REQUIRED", exception.getCode());
    }

    @Test
    void rejectsQuestionOverLimit() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> validator.validate("alice", "a".repeat(MAX_QUESTION_LENGTH + 1)));
        assertEquals("CHAT_QUESTION_TOO_LONG", exception.getCode());
    }

    @Test
    void rejectsPunctuationOnlyQuestion() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> validator.validate("alice", "！？。"));
        assertEquals("CHAT_QUESTION_INVALID", exception.getCode());
    }
}
