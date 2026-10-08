package com.campus.evaluation.evaluation.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StudentEvaluationSubmitDTO {

    private Long submissionId;

    @NotNull(message = "formId is required")
    private Long formId;

    private Long windowId;

    private String targetType;

    private Long targetId;

    private Integer modifiableHours;

    private String reviewStatus;

    @Valid
    private List<AnswerItem> answers;

    @Valid
    private List<ScoreItem> scores;

    @Valid
    private List<AttachmentItem> attachments;

    @Data
    public static class AnswerItem {
        @JsonProperty("question_id")
        private Long questionId;

        @JsonProperty("answer_value")
        private Object answerValue;
    }

    @Data
    public static class ScoreItem {
        @JsonProperty("question_id")
        private Long questionId;

        private BigDecimal score;
    }

    @Data
    public static class AttachmentItem {
        @JsonProperty("question_id")
        private Long questionId;

        @JsonProperty("file_id")
        private Long fileId;
    }
}
