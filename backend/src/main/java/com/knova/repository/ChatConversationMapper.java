package com.knova.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knova.domain.ChatConversation;
import org.apache.ibatis.annotations.Mapper;

/** 对话主记录数据访问接口，通用 CRUD 由 MyBatis-Plus BaseMapper 提供。 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {
}
