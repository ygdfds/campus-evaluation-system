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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Evaluation Audit", description = "Evaluation form publish audit management")
@RestController
@RequestMapping("/evaluation/audits")
@RequiredArgsConstructor
public class EvaluationAuditController {

    private final EvaluationAuditService auditService;

    @Operation(summary = "Audit list")
    @GetMapping
    public R<PageResult<EvaluationAuditVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String formType,
            @RequestParam(required = false) Long submitterId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        int currentPage = page != null ? page : (pageNum != null ? pageNum : 1);
        return R.ok(auditService.list(keyword, status, formType, submitterId, currentPage, pageSize));
    }

    @Operation(summary = "Audit list")
    @GetMapping("/forms")
    public R<PageResult<EvaluationAuditVO>> formList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String formType,
            @RequestParam(required = false) Long submitterId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return list(keyword, status, formType, submitterId, page, pageNum, pageSize);
    }

    @Operation(summary = "Audit detail")
    @GetMapping("/{id}")
    public R<EvaluationAuditService.AuditDetailResult> getDetail(@PathVariable Long id) {
        return R.ok(auditService.getDetail(id));
    }

    @Operation(summary = "Approve audit")
    @OperationLog(module = "evaluation", value = "Audit approved", type = "UPDATE")
    @PostMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        auditService.approve(id);
        return R.ok();
    }

    @Operation(summary = "Reject audit")
    @OperationLog(module = "evaluation", value = "Audit rejected", type = "UPDATE")
    @PostMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id, @Valid @RequestBody AuditDecisionDTO dto) {
        auditService.reject(id, dto);
        return R.ok();
    }
}
