package com.campus.evaluation.review.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/review/appeals")
    public R<List<Map<String, Object>>> listAppeals(@RequestParam Map<String, Object> params) {
        return R.ok(reviewService.listAppeals(params));
    }

    @GetMapping("/review/appeals/{id}")
    public R<Map<String, Object>> getAppeal(@PathVariable Long id) {
        return R.ok(reviewService.getAppeal(id));
    }

    @PostMapping("/review/appeals")
    public R<Map<String, Object>> createAppeal(@RequestBody Map<String, Object> payload) {
        return R.ok(reviewService.createAppeal(payload));
    }

    @OperationLog(module = "review", value = "更新评价申诉", type = "UPDATE")
    @PatchMapping("/review/appeals/{id}")
    public R<Map<String, Object>> updateAppeal(@PathVariable Long id,
                                               @RequestBody Map<String, Object> payload) {
        return R.ok(reviewService.updateAppeal(id, payload));
    }

    @GetMapping("/review/appealProcessRecords")
    public R<List<Map<String, Object>>> listAppealProcessRecords(@RequestParam Map<String, Object> params) {
        return R.ok(reviewService.listAppealProcessRecords(params));
    }

    @OperationLog(module = "review", value = "新增申诉处理记录", type = "CREATE")
    @PostMapping("/review/appealProcessRecords")
    public R<Map<String, Object>> createAppealProcessRecord(@RequestBody Map<String, Object> payload) {
        return R.ok(reviewService.createAppealProcessRecord(payload));
    }

    @GetMapping("/review/traceAuthorizations")
    public R<List<Map<String, Object>>> listTraceAuthorizations(@RequestParam Map<String, Object> params) {
        return R.ok(reviewService.listTraceAuthorizations(params));
    }

    @GetMapping("/review/traceAuthorizations/{id}")
    public R<Map<String, Object>> getTraceAuthorization(@PathVariable Long id) {
        return R.ok(reviewService.getTraceAuthorization(id));
    }

    @OperationLog(module = "review", value = "申请评价追溯授权", type = "CREATE")
    @PostMapping("/review/traceAuthorizations")
    public R<Map<String, Object>> createTraceAuthorization(@RequestBody Map<String, Object> payload) {
        return R.ok(reviewService.createTraceAuthorization(payload));
    }

    @OperationLog(module = "review", value = "审批评价追溯授权", type = "UPDATE")
    @PatchMapping("/review/traceAuthorizations/{id}")
    public R<Map<String, Object>> updateTraceAuthorization(@PathVariable Long id,
                                                            @RequestBody Map<String, Object> payload) {
        return R.ok(reviewService.updateTraceAuthorization(id, payload));
    }
}
