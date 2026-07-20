package com.knova.service;

import com.knova.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HelpServiceTest {
    private final UserService userService = mock(UserService.class);
    private final HelpService helpService = new HelpService(userService);

    @Test
    void answersCurrentUserIdentityFromLoginAccount() {
        // 1. 准备当前登录用户资料。
        User user = new User();
        user.setUsername("alice");
        user.setDisplayName("爱丽丝");
        when(userService.current("alice")).thenReturn(user);

        // 2. 验证“我是谁”返回用户信息而不是 AI 身份介绍。
        String answer = helpService.answer("alice", "我是谁？");
        assertEquals("你当前登录的账号是“alice”，展示名称是“爱丽丝”。除此之外，我不会猜测你的真实身份。", answer);
    }

    @Test
    void answersAssistantIdentitySeparately() {
        // 1. 验证“你是谁”只介绍 AI 身份，不查询当前用户资料。
        String answer = helpService.answer("alice", "你是谁");
        assertTrue(answer.startsWith("我是 GGBOND AI 企业知识助手"));
    }

    @Test
    void answersKnowledgeBaseDeletionWithOperationGuide() {
        // 1. 请求知识库删除操作帮助。
        String answer = helpService.answer("alice", "删除自己的知识库");

        // 2. 验证回答提供管理页面路径和所有者权限说明。
        assertTrue(answer.contains("知识库管理"));
        assertTrue(answer.contains("只有知识库所有者可以删除"));
    }
}
