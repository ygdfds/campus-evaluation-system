package com.campus.evaluation.ai.domain;

import lombok.Data;

import java.util.List;

@Data
public class AiAskRequest {

    private String question;

    private List<String> conversation;
}
