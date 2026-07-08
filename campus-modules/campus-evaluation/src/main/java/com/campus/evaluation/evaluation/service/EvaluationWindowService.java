package com.campus.evaluation.evaluation.service;

import com.campus.evaluation.evaluation.domain.dto.EvaluationWindowSaveDTO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationWindowVO;

public interface EvaluationWindowService {

    EvaluationWindowVO getWindow(Long formId);

    void saveWindow(Long formId, EvaluationWindowSaveDTO dto);
}
