package com.knova.service;

import com.knova.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 仅负责校验聊天用例的输入约束，不包含会话或回答业务。 */
@Component
public class ChatRequestValidator {
    private final int maxQuestionLength;

    ChatRequestValidator(@Value("${app.chat-routing.max-question-length:4000}") int maxQuestionLength) {
        // 1. 保存配置的最大问题长度，供每次聊天请求复用。
        this.maxQuestionLength = maxQuestionLength;
    }

    public void validate(String username, String question) {
        // 1. 校验当前登录用户存在。
        if (username == null || username.isBlank()) {
            throw BusinessException.badRequest("CHAT_USER_REQUIRED", "当前用户不能为空");
        }
        // 2. 校验问题不是 null 或空白内容。
        if (question == null || question.isBlank()) {
            throw BusinessException.badRequest("CHAT_QUESTION_REQUIRED", "问题不能为空");
        }
        // 3. 规范化问题并拒绝只包含标点的无效输入。
        if (LocalChatIntentClassifier.normalize(question).isBlank()) {
            throw BusinessException.badRequest("CHAT_QUESTION_INVALID", "问题不能只包含空格或标点");
        }
        // 4. 校验问题长度，避免异常请求占用模型上下文和日志空间。
        if (question.length() > maxQuestionLength) {
            throw BusinessException.badRequest("CHAT_QUESTION_TOO_LONG",
                    "问题不能超过 " + maxQuestionLength + " 个字符");
        }
    }
}
