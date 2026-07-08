package com.campus.evaluation.evaluation.service;

import com.campus.evaluation.evaluation.domain.dto.EvaluationQuestionSaveDTO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationQuestionVO;

import java.util.List;

public interface EvaluationQuestionService {

    List<EvaluationQuestionVO> getQuestions(Long formId);

    void saveQuestions(Long formId, EvaluationQuestionSaveDTO dto);
}
