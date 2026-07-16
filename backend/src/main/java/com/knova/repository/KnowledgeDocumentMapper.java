package com.knova.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knova.domain.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;

/** 知识文档数据访问层。 */
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {}
