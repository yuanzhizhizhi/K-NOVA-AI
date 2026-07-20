package com.knova.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knova.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/** 对话消息数据访问接口，负责用户消息和 AI 消息的持久化。 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
