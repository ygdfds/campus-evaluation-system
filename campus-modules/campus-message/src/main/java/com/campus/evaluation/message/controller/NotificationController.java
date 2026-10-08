package com.campus.evaluation.message.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.message.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public R<List<Map<String, Object>>> list(@RequestParam Map<String, Object> params) {
        return R.ok(notificationService.list(params));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> get(@PathVariable Long id) {
        return R.ok(notificationService.get(id));
    }

    @PostMapping
    public R<Map<String, Object>> create(@RequestBody Map<String, Object> payload) {
        return R.ok(notificationService.create(payload));
    }

    @PatchMapping("/{id}")
    public R<Map<String, Object>> update(@PathVariable Long id,
                                          @RequestBody Map<String, Object> payload) {
        return R.ok(notificationService.update(id, payload));
    }
}
