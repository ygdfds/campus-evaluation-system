package com.campus.evaluation.school.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.school.domain.dto.CourseEnrollmentDTO;
import com.campus.evaluation.school.domain.vo.CourseEnrollmentVO;

public interface CourseEnrollmentService {
    PageResult<CourseEnrollmentVO> list(Long courseId, Long studentId, Long classGroupId,
                                         String status, int pageNum, int pageSize);
    CourseEnrollmentVO create(CourseEnrollmentDTO dto);
    CourseEnrollmentVO update(Long id, CourseEnrollmentDTO dto);
    void delete(Long id);
}
