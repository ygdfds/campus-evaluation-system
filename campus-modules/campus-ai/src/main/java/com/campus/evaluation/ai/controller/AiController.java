package com.campus.evaluation.ai.controller;

import com.campus.evaluation.ai.domain.AiAskRequest;
import com.campus.evaluation.ai.service.AiAssistantService;
import com.campus.evaluation.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AiController {

    private final AiAssistantService aiAssistantService;

    @GetMapping("/ai/status")
    public R<Map<String, Object>> status() {
        return R.ok(aiAssistantService.status());
    }

    @PostMapping("/ai/ask")
    public R<Map<String, Object>> ask(@RequestBody AiAskRequest request) {
        return R.ok(aiAssistantService.ask(request));
    }
}
