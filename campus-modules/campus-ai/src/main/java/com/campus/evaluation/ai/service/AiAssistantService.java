package com.campus.evaluation.ai.service;

import com.campus.evaluation.ai.domain.AiAskRequest;

import java.util.Map;

public interface AiAssistantService {

    Map<String, Object> status();

    Map<String, Object> ask(AiAskRequest request);
}
