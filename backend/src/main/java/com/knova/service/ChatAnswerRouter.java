package com.knova.service;

import com.knova.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** 根据意图选择回答策略，取代 ChatService 中随意图增长的 switch 分支。 */
@Component
public class ChatAnswerRouter {
    private final Map<ChatIntent, ChatAnswerStrategy> strategies;

    public ChatAnswerRouter(List<ChatAnswerStrategy> strategyList) {
        // 1. 按意图构建策略映射，保证查询复杂度稳定。
        EnumMap<ChatIntent, ChatAnswerStrategy> mapping = new EnumMap<>(ChatIntent.class);
        // 2. 启动时检查同一意图不能存在多个实现，避免运行期随机选择策略。
        strategyList.forEach(strategy -> {
            ChatAnswerStrategy previous = mapping.put(strategy.intent(), strategy);
            if (previous != null) {
                throw new IllegalStateException("存在重复的回答策略: " + strategy.intent());
            }
        });
        // 3. 转换为不可变映射，防止运行过程中策略集合被修改。
        this.strategies = Map.copyOf(mapping);
    }

    public ChatAnswerResult route(ChatIntent intent, ChatAnswerContext context) {
        // 1. 根据识别出的意图获取对应回答策略。
        ChatAnswerStrategy strategy = strategies.get(intent);
        if (strategy == null) {
            throw new BusinessException("CHAT_STRATEGY_NOT_FOUND", "未找到对应的回答策略", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 2. 调用策略生成统一回答结果。
        return strategy.answer(context);
    }
}
