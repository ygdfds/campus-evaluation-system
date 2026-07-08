package com.campus.evaluation.evaluation.controller;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.evaluation.domain.dto.AuditDecisionDTO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationAuditVO;
import com.campus.evaluation.evaluation.service.EvaluationAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "审核中心", description = "评价表单发布审核管理")
@RestController
@RequestMapping("/evaluation/audits")
@RequiredArgsConstructor
public class EvaluationAuditController {

    private final EvaluationAuditService auditService;

    @Operation(summary = "审核列表")
    @GetMapping
    public R<PageResult<EvaluationAuditVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String formType,
            @RequestParam(required = false) Long submitterId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(auditService.list(keyword, status, formType, submitterId, pageNum, pageSize));
    }

    @Operation(summary = "审核详情")
    @GetMapping("/{id}")
    public R<EvaluationAuditService.AuditDetailResult> getDetail(@PathVariable Long id) {
        return R.ok(auditService.getDetail(id));
    }

    @Operation(summary = "审核通过")
    @OperationLog(module = "evaluation", value = "审核通过", type = "UPDATE")
    @PostMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        auditService.approve(id);
        return R.ok();
    }

    @Operation(summary = "审核驳回")
    @OperationLog(module = "evaluation", value = "审核驳回", type = "UPDATE")
    @PostMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id, @Valid @RequestBody AuditDecisionDTO dto) {
        auditService.reject(id, dto);
        return R.ok();
    }
}
