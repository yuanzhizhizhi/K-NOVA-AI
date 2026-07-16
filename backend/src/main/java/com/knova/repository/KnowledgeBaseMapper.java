package com.knova.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knova.domain.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;

/** 知识库元数据访问层。 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {}
