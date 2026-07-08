package com.campus.evaluation.evaluation.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.evaluation.domain.dto.EvaluationFormCreateDTO;
import com.campus.evaluation.evaluation.domain.dto.EvaluationFormUpdateDTO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationFormDetailVO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationFormListVO;

public interface EvaluationFormService {

    PageResult<EvaluationFormListVO> list(String keyword, String status, String formType,
                                          String targetType, Long creatorId,
                                          int pageNum, int pageSize);

    EvaluationFormDetailVO getDetail(Long id);

    EvaluationFormListVO create(EvaluationFormCreateDTO dto);

    EvaluationFormListVO update(Long id, EvaluationFormUpdateDTO dto);

    void delete(Long id);

    EvaluationFormListVO copy(Long id);

    void submitAudit(Long id);
}
