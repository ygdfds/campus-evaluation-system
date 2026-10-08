package com.campus.evaluation.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.school.domain.entity.SchoolProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SchoolProfileMapper extends BaseMapper<SchoolProfile> {
    @Select("""
            SELECT COUNT(*) FROM file_resource
            WHERE id = #{fileId}
              AND tenant_id = #{tenantId}
              AND school_id = #{schoolId}
              AND deleted = 0
              AND mime_type LIKE 'image/%'
            """)
    int countOwnedImage(@Param("fileId") Long fileId, @Param("tenantId") Long tenantId,
                        @Param("schoolId") Long schoolId);
}
