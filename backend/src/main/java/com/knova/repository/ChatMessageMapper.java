package com.knova.repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knova.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
@Mapper public interface ChatMessageMapper extends BaseMapper<ChatMessage> {}
