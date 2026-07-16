package com.knova.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knova.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层，基础 CRUD 由 MyBatis-Plus 自动实现。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
