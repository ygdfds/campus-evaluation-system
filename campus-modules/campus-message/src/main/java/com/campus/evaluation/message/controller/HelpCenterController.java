package com.campus.evaluation.message.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.message.service.HelpCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HelpCenterController {

    private final HelpCenterService helpCenterService;

    @GetMapping({"/helpGuides", "/platform/helpGuides"})
    public R<List<Map<String, Object>>> listGuides(@RequestParam Map<String, Object> params) {
        return R.ok(helpCenterService.listGuides(params));
    }

    @GetMapping({"/helpFaqs", "/platform/helpFaqs"})
    public R<List<Map<String, Object>>> listFaqs(@RequestParam Map<String, Object> params) {
        return R.ok(helpCenterService.listFaqs(params));
    }

    @GetMapping({"/helpTickets", "/platform/helpTickets"})
    public R<List<Map<String, Object>>> listTickets(@RequestParam Map<String, Object> params) {
        return R.ok(helpCenterService.listTickets(params));
    }

    @PostMapping("/helpTickets")
    public R<Map<String, Object>> createTicket(@RequestBody Map<String, Object> payload) {
        return R.ok(helpCenterService.createTicket(payload));
    }

    @PatchMapping({"/helpTickets/{id}", "/platform/helpTickets/{id}"})
    public R<Map<String, Object>> updateTicket(@PathVariable Long id,
                                               @RequestBody Map<String, Object> payload) {
        return R.ok(helpCenterService.updateTicket(id, payload));
    }
}
