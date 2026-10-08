package com.campus.evaluation.complaint.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.complaint.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping("/complaints")
    public R<List<Map<String, Object>>> listComplaints(@RequestParam Map<String, Object> params) {
        return R.ok(complaintService.listComplaints(params));
    }

    @GetMapping("/complaints/{id}")
    public R<Map<String, Object>> getComplaint(@PathVariable Long id) {
        return R.ok(complaintService.getComplaint(id));
    }

    @PostMapping("/complaints")
    public R<Map<String, Object>> createComplaint(@RequestBody Map<String, Object> payload) {
        return R.ok(complaintService.createComplaint(payload));
    }

    @PatchMapping("/complaints/{id}")
    public R<Map<String, Object>> updateComplaint(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> payload) {
        return R.ok(complaintService.updateComplaint(id, payload));
    }

    @GetMapping("/complaintProcessRecords")
    public R<List<Map<String, Object>>> listProcessRecords(@RequestParam Map<String, Object> params) {
        return R.ok(complaintService.listProcessRecords(params));
    }

    @PostMapping("/complaintProcessRecords")
    public R<Map<String, Object>> createProcessRecord(@RequestBody Map<String, Object> payload) {
        return R.ok(complaintService.createProcessRecord(payload));
    }

    @GetMapping("/feedbackWorkOrders")
    public R<List<Map<String, Object>>> listWorkOrders(@RequestParam Map<String, Object> params) {
        return R.ok(complaintService.listWorkOrders(params));
    }

    @PostMapping("/feedbackWorkOrders")
    public R<Map<String, Object>> createWorkOrder(@RequestBody Map<String, Object> payload) {
        return R.ok(complaintService.createWorkOrder(payload));
    }

    @PatchMapping("/feedbackWorkOrders/{id}")
    public R<Map<String, Object>> updateWorkOrder(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> payload) {
        return R.ok(complaintService.updateWorkOrder(id, payload));
    }
}
