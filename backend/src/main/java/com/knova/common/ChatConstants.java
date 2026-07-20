package com.knova.common;

/** 对话领域共享常量，集中表达特殊标识和消息角色的业务语义。 */
public final class ChatConstants {
    /** 0 代表默认检索用户全部可访问知识库的全局对话。 */
    public static final long GLOBAL_KNOWLEDGE_BASE_ID = 0L;
    public static final String USER_ROLE = "user";
    public static final String ASSISTANT_ROLE = "assistant";
    private ChatConstants() {
        // 1. 禁止实例化纯常量类。
    }
}
