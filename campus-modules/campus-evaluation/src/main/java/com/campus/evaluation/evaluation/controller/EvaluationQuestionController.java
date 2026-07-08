package com.campus.evaluation.evaluation.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.evaluation.domain.dto.EvaluationQuestionSaveDTO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationQuestionVO;
import com.campus.evaluation.evaluation.service.EvaluationQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "题目配置", description = "评价表单的题目管理")
@RestController
@RequestMapping("/evaluation/forms/{formId}/questions")
@RequiredArgsConstructor
public class EvaluationQuestionController {

    private final EvaluationQuestionService questionService;

    @Operation(summary = "获取题目配置")
    @GetMapping
    public R<List<EvaluationQuestionVO>> getQuestions(@PathVariable Long formId) {
        return R.ok(questionService.getQuestions(formId));
    }

    @Operation(summary = "保存题目配置")
    @OperationLog(module = "evaluation", value = "保存题目配置", type = "UPDATE")
    @PutMapping
    public R<Void> saveQuestions(@PathVariable Long formId,
                                 @Valid @RequestBody EvaluationQuestionSaveDTO dto) {
        questionService.saveQuestions(formId, dto);
        return R.ok();
    }
}
