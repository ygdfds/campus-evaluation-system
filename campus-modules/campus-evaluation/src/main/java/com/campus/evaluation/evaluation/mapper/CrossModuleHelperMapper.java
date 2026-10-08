package com.campus.evaluation.evaluation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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

    @Select("SELECT COUNT(*) FROM sch_course_enrollment " +
            "WHERE tenant_id = #{tenantId} AND course_id = #{courseId} AND student_id = #{studentId} " +
            "AND deleted = 0 AND (status IS NULL OR status = 'active')")
    int countActiveCourseEnrollment(@Param("tenantId") Long tenantId,
                                    @Param("courseId") Long courseId,
                                    @Param("studentId") Long studentId);

    @Select("SELECT e.course_id AS course_id FROM sch_course_enrollment e " +
            "WHERE e.tenant_id = #{tenantId} AND e.student_id = #{studentId} " +
            "AND e.deleted = 0 AND (e.status IS NULL OR e.status = 'active')")
    List<Map<String, Object>> selectStudentCourseEnrollments(@Param("tenantId") Long tenantId,
                                                             @Param("studentId") Long studentId);

    @Select("SELECT c.id AS id, c.course_name AS course_name, c.teaching_org_id AS teaching_org_id, " +
            "o.name AS org_name FROM sch_course c " +
            "LEFT JOIN sch_teaching_org_unit o ON o.id = c.teaching_org_id AND o.deleted = 0 " +
            "WHERE c.id = #{id} AND c.deleted = 0 LIMIT 1")
    Map<String, Object> selectCourseInfoById(@Param("id") Long id);

    @Select("SELECT s.id AS id, s.name AS name, s.service_org_id AS service_org_id, " +
            "o.name AS org_name FROM sch_service_item s " +
            "LEFT JOIN sch_service_org_unit o ON o.id = s.service_org_id AND o.deleted = 0 " +
            "WHERE s.id = #{id} AND s.deleted = 0 LIMIT 1")
    Map<String, Object> selectServiceItemInfoById(@Param("id") Long id);

    @Select("SELECT url FROM file_resource WHERE id = #{id} AND deleted = 0 LIMIT 1")
    String selectFileUrlById(@Param("id") Long id);

    @Select("SELECT id, course_name, teaching_org_id FROM sch_course " +
            "WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY course_name")
    List<Map<String, Object>> selectCourseOptions(@Param("tenantId") Long tenantId);

    @Select("SELECT id, name, service_org_id FROM sch_service_item " +
            "WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY name")
    List<Map<String, Object>> selectServiceItemOptions(@Param("tenantId") Long tenantId);

    @Select("SELECT id, name FROM sch_teaching_org_unit " +
            "WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY name")
    List<Map<String, Object>> selectTeachingOrgOptions(@Param("tenantId") Long tenantId);
}
