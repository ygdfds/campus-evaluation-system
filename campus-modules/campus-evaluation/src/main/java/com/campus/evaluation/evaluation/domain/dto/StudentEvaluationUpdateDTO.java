package com.campus.evaluation.evaluation.domain.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class StudentEvaluationUpdateDTO {

    private String reviewStatus;

    @Valid
    private List<StudentEvaluationSubmitDTO.AnswerItem> answers;

    @Valid
    private List<StudentEvaluationSubmitDTO.ScoreItem> scores;

    @Valid
    private List<StudentEvaluationSubmitDTO.AttachmentItem> attachments;
}
