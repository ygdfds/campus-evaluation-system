package com.campus.evaluation.evaluation.controller;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.evaluation.domain.dto.EvaluationFormCreateDTO;
import com.campus.evaluation.evaluation.domain.dto.EvaluationFormUpdateDTO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationFormDetailVO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationFormListVO;
import com.campus.evaluation.evaluation.service.EvaluationFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评价表单管理", description = "评价表单的 CRUD、复制、提交审核")
@RestController
@RequestMapping("/evaluation/forms")
@RequiredArgsConstructor
public class EvaluationFormController {

    private final EvaluationFormService formService;

    @Operation(summary = "分页查询表单列表")
    @GetMapping
    public R<PageResult<EvaluationFormListVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String formType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(formService.list(keyword, status, formType, targetType, creatorId, pageNum, pageSize));
    }

    @Operation(summary = "查看表单详情")
    @GetMapping("/{id}")
    public R<EvaluationFormDetailVO> getDetail(@PathVariable Long id) {
        return R.ok(formService.getDetail(id));
    }

    @Operation(summary = "新建表单")
    @OperationLog(module = "evaluation", value = "新建评价表单", type = "CREATE")
    @PostMapping
    public R<EvaluationFormListVO> create(@Valid @RequestBody EvaluationFormCreateDTO dto) {
        return R.ok(formService.create(dto));
    }

    @Operation(summary = "编辑表单")
    @OperationLog(module = "evaluation", value = "编辑评价表单", type = "UPDATE")
    @PutMapping("/{id}")
    public R<EvaluationFormListVO> update(@PathVariable Long id, @Valid @RequestBody EvaluationFormUpdateDTO dto) {
        return R.ok(formService.update(id, dto));
    }

    @Operation(summary = "删除表单")
    @OperationLog(module = "evaluation", value = "删除评价表单", type = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        formService.delete(id);
        return R.ok();
    }

    @Operation(summary = "复制表单")
    @OperationLog(module = "evaluation", value = "复制评价表单", type = "CREATE")
    @PostMapping("/{id}/copy")
    public R<EvaluationFormListVO> copy(@PathVariable Long id) {
        return R.ok(formService.copy(id));
    }

    @Operation(summary = "提交审核")
    @OperationLog(module = "evaluation", value = "提交表单审核", type = "UPDATE")
    @PostMapping("/{id}/submit-audit")
    public R<Void> submitAudit(@PathVariable Long id) {
        formService.submitAudit(id);
        return R.ok();
    }
}
