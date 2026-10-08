package com.campus.evaluation.stats.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.stats.service.StaffStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats/staff")
@RequiredArgsConstructor
public class StaffStatsController {

    private final StaffStatsService staffStatsService;

    @GetMapping("/reports/context")
    public R<Map<String, Object>> reportContext() {
        requireStaffAccess();
        return R.ok(staffStatsService.reportContext());
    }

    @GetMapping("/dashboard/todos")
    public R<Map<String, Object>> todoStats() {
        requireStaffAccess();
        return R.ok(staffStatsService.todoStats());
    }

    @GetMapping("/dashboard/active-windows")
    public R<List<Map<String, Object>>> activeWindows() {
        requireStaffAccess();
        return R.ok(staffStatsService.activeWindows());
    }

    @GetMapping("/dashboard/pending-feedback")
    public R<List<Map<String, Object>>> pendingFeedback() {
        requireStaffAccess();
        return R.ok(staffStatsService.pendingFeedback());
    }

    @GetMapping("/dashboard/evaluation-summary")
    public R<Map<String, Object>> evaluationSummary() {
        requireStaffAccess();
        return R.ok(staffStatsService.evaluationSummary());
    }

    private void requireStaffAccess() {
        if (SecurityUtils.hasRole("student")) {
            throw new BusinessException(403, "无权访问员工统计数据");
        }
    }
}
