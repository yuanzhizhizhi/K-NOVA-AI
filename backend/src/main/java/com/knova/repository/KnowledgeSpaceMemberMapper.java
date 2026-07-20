package com.knova.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knova.domain.KnowledgeSpaceMember;
import org.apache.ibatis.annotations.Mapper;

/** 知识空间成员关系数据访问接口，用于查询 OWNER、EDITOR、VIEWER 权限。 */
@Mapper
public interface KnowledgeSpaceMemberMapper extends BaseMapper<KnowledgeSpaceMember> {
}
