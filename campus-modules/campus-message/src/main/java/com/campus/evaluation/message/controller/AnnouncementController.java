package com.campus.evaluation.message.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.message.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/announcements")
    public R<List<Map<String, Object>>> list(@RequestParam Map<String, Object> params) {
        return R.ok(announcementService.list(params));
    }

    @GetMapping("/announcements/{id}")
    public R<Map<String, Object>> get(@PathVariable Long id) {
        return R.ok(announcementService.get(id));
    }

    @PostMapping("/announcements")
    public R<Map<String, Object>> create(@RequestBody Map<String, Object> payload) {
        return R.ok(announcementService.create(payload));
    }

    @PatchMapping("/announcements/{id}")
    public R<Map<String, Object>> update(@PathVariable Long id,
                                         @RequestBody Map<String, Object> payload) {
        return R.ok(announcementService.update(id, payload));
    }
}
