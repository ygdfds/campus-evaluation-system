package com.campus.evaluation.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.auth.domain.entity.AuthPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限 Mapper
 */
@Mapper
public interface AuthPermissionMapper extends BaseMapper<AuthPermission> {

    /**
     * 查询用户的所有权限（通过 user_role → role_permission → permission）
     */
    @Select("""
            SELECT DISTINCT p.* FROM auth_permission p
            INNER JOIN auth_role_permission rp ON rp.permission_id = p.id AND rp.deleted = 0
            INNER JOIN auth_user_role ur ON ur.role_id = rp.role_id AND ur.deleted = 0
            WHERE ur.user_id = #{userId} AND p.deleted = 0
            """)
    List<AuthPermission> selectPermissionsByUserId(@Param("userId") Long userId);
}
