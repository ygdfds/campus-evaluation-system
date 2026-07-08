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

    /**
     * 检查用户名是否已存在（全局唯一）
     */
    @Select("<script>SELECT COUNT(*) FROM auth_user_account WHERE username = #{username} AND deleted = 0" +
            "<if test='excludeId != null'> AND id != #{excludeId}</if></script>")
    int existsByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    /**
     * 检查手机号是否已存在（全局唯一）
     */
    @Select("<script>SELECT COUNT(*) FROM auth_user_account WHERE phone = #{phone} AND deleted = 0" +
            "<if test='excludeId != null'> AND id != #{excludeId}</if></script>")
    int existsByPhone(@Param("phone") String phone, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(*) FROM sch_teaching_org_unit WHERE id = #{id} AND tenant_id = #{tenantId} AND deleted = 0")
    int countTeachingOrg(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Select("SELECT COUNT(*) FROM sch_service_org_unit WHERE id = #{id} AND tenant_id = #{tenantId} AND deleted = 0")
    int countServiceOrg(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Select("SELECT COUNT(*) FROM sch_class_group WHERE id = #{id} AND tenant_id = #{tenantId} AND deleted = 0")
    int countClassGroup(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Select("SELECT name FROM sch_teaching_org_unit WHERE id = #{id} AND deleted = 0 LIMIT 1")
    String selectTeachingOrgName(@Param("id") Long id);

    @Select("SELECT name FROM sch_service_org_unit WHERE id = #{id} AND deleted = 0 LIMIT 1")
    String selectServiceOrgName(@Param("id") Long id);

    @Select("SELECT class_name FROM sch_class_group WHERE id = #{id} AND deleted = 0 LIMIT 1")
    String selectClassName(@Param("id") Long id);

    @Select("SELECT grade_name FROM sch_class_group WHERE id = #{id} AND deleted = 0 LIMIT 1")
    String selectClassGrade(@Param("id") Long id);

    @Select("SELECT teaching_org_id FROM sch_class_group WHERE id = #{id} AND deleted = 0 LIMIT 1")
    Long selectClassTeachingOrgId(@Param("id") Long id);
}
