package com.knova.service;

import java.util.List;

/** 各回答策略的统一返回结构。 */
public record ChatAnswerResult(String answer, List<String> sources) {
    public ChatAnswerResult {
        // 1. 把 null 来源统一转换为空集合。
        // 2. 创建不可变副本，防止返回结果在外部被修改。
        sources = sources == null ? List.of() : List.copyOf(sources);
    }

    static ChatAnswerResult withoutSources(String answer) {
        // 1. 为闲聊、帮助和降级提示创建不包含引用来源的结果。
        return new ChatAnswerResult(answer, List.of());
    }
}
