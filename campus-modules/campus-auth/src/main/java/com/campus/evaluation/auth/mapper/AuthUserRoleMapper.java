package com.campus.evaluation.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.auth.domain.entity.AuthUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关系 Mapper
 */
@Mapper
public interface AuthUserRoleMapper extends BaseMapper<AuthUserRole> {

    /**
     * 查询用户的所有角色ID（通过 auth_user_role）
     */
    @Select("SELECT role_id FROM auth_user_role WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 物理删除用户的所有角色绑定（绕过逻辑删除，避免唯一键冲突）
     */
    @Delete("DELETE FROM auth_user_role WHERE user_id = #{userId}")
    int physicalDeleteByUserId(@Param("userId") Long userId);
}
