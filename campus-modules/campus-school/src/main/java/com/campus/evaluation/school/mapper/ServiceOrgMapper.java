package com.campus.evaluation.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.school.domain.entity.ServiceOrgUnit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ServiceOrgMapper extends BaseMapper<ServiceOrgUnit> {

    @Select("""
            SELECT COUNT(*) FROM auth_person_profile
            WHERE tenant_id = #{tenantId} AND service_org_id = #{orgId} AND deleted = 0
            """)
    int countAssignedUsers(@Param("orgId") Long orgId, @Param("tenantId") Long tenantId);
}
