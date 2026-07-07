package com.campus.evaluation.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.auth.domain.entity.AuthLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper
 */
@Mapper
public interface AuthLoginLogMapper extends BaseMapper<AuthLoginLog> {
}
