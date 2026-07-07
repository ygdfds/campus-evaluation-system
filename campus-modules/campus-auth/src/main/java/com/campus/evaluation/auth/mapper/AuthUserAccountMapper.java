package com.campus.evaluation.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.auth.domain.entity.AuthUserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户账号 Mapper
 */
@Mapper
public interface AuthUserAccountMapper extends BaseMapper<AuthUserAccount> {

    /**
     * 查询租户状态是否正常（active 且未删除）
     * @return 匹配记录数
     */
    @Select("SELECT COUNT(*) FROM pf_tenant WHERE id = #{tenantId} AND status = 'active' AND deleted = 0")
    int countActiveTenant(@Param("tenantId") Long tenantId);

    /**
     * 根据租户ID查询学校ID
     */
    @Select("SELECT id FROM sch_school_profile WHERE tenant_id = #{tenantId} AND deleted = 0 LIMIT 1")
    Long selectSchoolIdByTenantId(@Param("tenantId") Long tenantId);
}
