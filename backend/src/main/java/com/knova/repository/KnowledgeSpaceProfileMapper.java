package com.knova.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knova.domain.KnowledgeSpaceProfile;
import org.apache.ibatis.annotations.Mapper;

/** 知识空间展示配置数据访问接口，保存所有者、收藏、归档、图标和排序信息。 */
@Mapper
public interface KnowledgeSpaceProfileMapper extends BaseMapper<KnowledgeSpaceProfile> {
}
