package com.campus.evaluation.school.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.school.domain.dto.OrgUnitDTO;
import com.campus.evaluation.school.domain.vo.OrgTreeVO;
import com.campus.evaluation.school.domain.vo.ServiceOrgVO;

import java.util.List;

public interface ServiceOrgService {
    PageResult<ServiceOrgVO> list(String keyword, String status, Long parentId, int pageNum, int pageSize);
    List<OrgTreeVO> tree();
    ServiceOrgVO create(OrgUnitDTO dto);
    ServiceOrgVO update(Long id, OrgUnitDTO dto);
    void delete(Long id);
}
