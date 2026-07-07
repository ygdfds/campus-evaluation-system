package com.campus.evaluation.school.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.school.domain.dto.ClassGroupDTO;
import com.campus.evaluation.school.domain.vo.ClassGroupVO;
import com.campus.evaluation.school.domain.vo.OptionVO;

import java.util.List;

public interface ClassGroupService {
    PageResult<ClassGroupVO> list(String keyword, Long teachingOrgId, String grade, String status, int pageNum, int pageSize);
    ClassGroupVO create(ClassGroupDTO dto);
    ClassGroupVO update(Long id, ClassGroupDTO dto);
    void delete(Long id);
    List<OptionVO> options(Long teachingOrgId);
}
