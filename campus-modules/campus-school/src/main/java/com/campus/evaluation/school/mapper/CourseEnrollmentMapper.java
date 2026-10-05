package com.campus.evaluation.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.school.domain.entity.CourseEnrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {
    @Select("""
            SELECT COUNT(*) FROM auth_person_profile p
            JOIN auth_user_account a ON a.id = p.user_id
            WHERE p.user_id = #{studentId}
              AND p.tenant_id = #{tenantId}
              AND p.role_type = 'student'
              AND p.deleted = 0
              AND a.tenant_id = #{tenantId}
              AND a.status = 'active'
              AND a.deleted = 0
            """)
    int countActiveStudent(@Param("studentId") Long studentId, @Param("tenantId") Long tenantId);
}
