package com.campus.evaluation.message.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.message.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/staff/notification-preferences")
@RequiredArgsConstructor
public class StaffNotificationPreferenceController {

    private final NotificationService notificationService;

    @GetMapping
    public R<Map<String, Object>> get() {
        return R.ok(notificationService.getStaffPreference());
    }

    @PutMapping
    public R<Map<String, Object>> update(@RequestBody Map<String, Object> payload) {
        return R.ok(notificationService.updateStaffPreference(payload));
    }
}
