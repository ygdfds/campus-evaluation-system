package com.campus.evaluation.evaluation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.evaluation.domain.dto.EvaluationWindowSaveDTO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationWindowVO;
import com.campus.evaluation.evaluation.service.EvaluationWindowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评价窗口", description = "评价表单的窗口配置")
@RestController
@RequestMapping("/evaluation/forms/{formId}/window")
@SaCheckRole(value = {"school_admin", "form_publisher"}, mode = SaMode.OR)
@RequiredArgsConstructor
public class EvaluationWindowController {

    private final EvaluationWindowService windowService;

    @Operation(summary = "获取窗口配置")
    @GetMapping
    public R<EvaluationWindowVO> getWindow(@PathVariable Long formId) {
        return R.ok(windowService.getWindow(formId));
    }

    @Operation(summary = "保存窗口配置")
    @OperationLog(module = "evaluation", value = "保存窗口配置", type = "UPDATE")
    @PutMapping
    public R<Void> saveWindow(@PathVariable Long formId,
                              @Valid @RequestBody EvaluationWindowSaveDTO dto) {
        windowService.saveWindow(formId, dto);
        return R.ok();
    }

    @Operation(summary = "关闭评价窗口")
    @OperationLog(module = "evaluation", value = "关闭评价窗口", type = "UPDATE")
    @PostMapping("/close")
    public R<Void> closeWindow(@PathVariable Long formId) {
        windowService.closeWindow(formId);
        return R.ok();
    }
}
