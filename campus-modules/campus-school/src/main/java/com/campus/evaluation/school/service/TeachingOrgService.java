package com.campus.evaluation.school.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.school.domain.dto.OrgUnitDTO;
import com.campus.evaluation.school.domain.vo.OrgTreeVO;
import com.campus.evaluation.school.domain.vo.TeachingOrgVO;

import java.util.List;

public interface TeachingOrgService {

    PageResult<TeachingOrgVO> list(String keyword, String status, Long parentId, int pageNum, int pageSize);

    List<OrgTreeVO> tree();

    TeachingOrgVO create(OrgUnitDTO dto);

    TeachingOrgVO update(Long id, OrgUnitDTO dto);

    void delete(Long id);
}
