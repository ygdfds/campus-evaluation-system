package com.campus.evaluation.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.auth.domain.entity.AuthRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface AuthRoleMapper extends BaseMapper<AuthRole> {

    /**
     * 查询用户的所有角色（通过 auth_user_role 关联）
     */
    @Select("""
            SELECT r.* FROM auth_role r
            INNER JOIN auth_user_role ur ON ur.role_id = r.id AND ur.deleted = 0
            WHERE ur.user_id = #{userId} AND r.deleted = 0
            """)
    List<AuthRole> selectRolesByUserId(@Param("userId") Long userId);
}
