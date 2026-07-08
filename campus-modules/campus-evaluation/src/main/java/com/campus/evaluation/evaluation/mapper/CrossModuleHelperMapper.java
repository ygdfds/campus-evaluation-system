package com.campus.evaluation.evaluation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 跨模块辅助查询 Mapper（只读）
 */
@Mapper
public interface CrossModuleHelperMapper {

    @Select("SELECT real_name FROM auth_person_profile WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    String selectRealNameByUserId(@Param("userId") Long userId);

    @Select("SELECT course_name FROM sch_course WHERE id = #{id} AND deleted = 0 LIMIT 1")
    String selectCourseNameById(@Param("id") Long id);

    @Select("SELECT name FROM sch_service_item WHERE id = #{id} AND deleted = 0 LIMIT 1")
    String selectServiceItemNameById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sch_course WHERE id = #{id} AND tenant_id = #{tenantId} AND deleted = 0")
    int countCourseByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Select("SELECT COUNT(*) FROM sch_service_item WHERE id = #{id} AND tenant_id = #{tenantId} AND deleted = 0")
    int countServiceItemByIdAndTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
