package com.campus.evaluation.school.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.school.domain.dto.CourseDTO;
import com.campus.evaluation.school.domain.dto.CourseTeachersDTO;
import com.campus.evaluation.school.domain.vo.CourseDetailVO;
import com.campus.evaluation.school.domain.vo.CourseVO;
import com.campus.evaluation.school.domain.vo.OptionVO;

import java.util.List;

public interface CourseService {
    PageResult<CourseVO> list(String keyword, Long teachingOrgId, String status, int pageNum, int pageSize);
    CourseDetailVO getDetail(Long id);
    CourseVO create(CourseDTO dto);
    CourseVO update(Long id, CourseDTO dto);
    void delete(Long id);
    void updateTeachers(Long id, CourseTeachersDTO dto);
    List<OptionVO> options(Long teachingOrgId);
}
