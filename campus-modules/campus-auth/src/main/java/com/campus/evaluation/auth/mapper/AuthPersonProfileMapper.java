package com.campus.evaluation.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.auth.domain.entity.AuthPersonProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人员档案 Mapper
 */
@Mapper
public interface AuthPersonProfileMapper extends BaseMapper<AuthPersonProfile> {
}
