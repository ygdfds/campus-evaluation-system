package com.campus.evaluation.school.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.school.domain.dto.ServiceItemDTO;
import com.campus.evaluation.school.domain.vo.OptionVO;
import com.campus.evaluation.school.domain.vo.ServiceItemVO;

import java.util.List;

public interface ServiceItemService {
    PageResult<ServiceItemVO> list(String keyword, Long serviceOrgId, String status, int pageNum, int pageSize);
    ServiceItemVO getDetail(Long id);
    ServiceItemVO create(ServiceItemDTO dto);
    ServiceItemVO update(Long id, ServiceItemDTO dto);
    void delete(Long id);
    List<OptionVO> options(Long serviceOrgId);
}
