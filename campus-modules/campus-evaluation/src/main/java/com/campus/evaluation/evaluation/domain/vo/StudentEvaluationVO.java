package com.campus.evaluation.evaluation.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class StudentEvaluationVO {

    private StudentEvaluationVO() {
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Task {
        private Long id;
        private String title;
        private String type;
        private String typeKey;
        private String dept;
        private String target;
        private String startDate;
        private String endDate;
        private String status;
        private String statusType;
        private String img;

        @JsonProperty("form_id")
        private Long formId;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubmitPage {
        private Form form;
        private Window window;
        private List<Question> questions;
        private Map<Long, List<Option>> options;
        private Submission submission;
        private List<Answer> answers;
        private List<Score> scores;
        private List<Attachment> attachments;
        private String mode;
        private Boolean readonly;
        private String readonlyReason;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Form {
        private Long id;
        private String title;
        private String description;
        private String type;
        private String status;

        @JsonProperty("publish_scope")
        private String publishScope;

        @JsonProperty("course_id")
        private Long courseId;

        @JsonProperty("service_item_id")
        private Long serviceItemId;

        @JsonProperty("cover_file_id")
        private Long coverFileId;

        @JsonProperty("anonymous")
        private Boolean anonymous;

        @JsonProperty("score_enabled")
        private Boolean scoreEnabled;

        @JsonProperty("_target_name")
        private String targetName;

        @JsonProperty("_dept_name")
        private String deptName;

        @JsonProperty("_type_label")
        private String typeLabel;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Window {
        private Long id;

        @JsonProperty("form_id")
        private Long formId;

        private String type;

        @JsonProperty("start_at")
        private LocalDateTime startAt;

        @JsonProperty("end_at")
        private LocalDateTime endAt;

        @JsonProperty("modifiable_hours")
        private Integer modifiableHours;

        private String status;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Question {
        private Long id;
        private String type;
        private String title;
        private Boolean required;

        @JsonProperty("max_score")
        private BigDecimal maxScore;

        @JsonProperty("min_length")
        private Integer minLength;

        @JsonProperty("sort_order")
        private Integer sortOrder;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Option {
        private Long id;
        private String label;

        @JsonProperty("option_text")
        private String optionText;

        @JsonProperty("sort_order")
        private Integer sortOrder;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Submission {
        private Long id;

        @JsonProperty("form_id")
        private Long formId;

        @JsonProperty("window_id")
        private Long windowId;

        @JsonProperty("target_type")
        private String targetType;

        @JsonProperty("target_id")
        private Long targetId;

        @JsonProperty("evaluator_user_id")
        private Long evaluatorUserId;

        @JsonProperty("submitted_at")
        private LocalDateTime submittedAt;

        @JsonProperty("modifiable_until")
        private LocalDateTime modifiableUntil;

        @JsonProperty("locked_at")
        private LocalDateTime lockedAt;

        private String status;

        @JsonProperty("review_status")
        private String reviewStatus;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Answer {
        private Long id;

        @JsonProperty("question_id")
        private Long questionId;

        @JsonProperty("answer_value")
        private Object answerValue;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Score {
        private Long id;

        @JsonProperty("question_id")
        private Long questionId;

        private BigDecimal score;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Attachment {
        private Long id;

        @JsonProperty("question_id")
        private Long questionId;

        @JsonProperty("file_id")
        private Long fileId;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class History {
        private Long id;

        @JsonProperty("form_id")
        private Long formId;

        @JsonProperty("form_title")
        private String formTitle;

        @JsonProperty("form_type")
        private String formType;

        @JsonProperty("display_status")
        private String displayStatus;

        @JsonProperty("can_edit")
        private Boolean canEdit;

        @JsonProperty("can_view")
        private Boolean canView;

        @JsonProperty("cover_img")
        private String coverImg;

        @JsonProperty("target_name")
        private String targetName;

        @JsonProperty("dept_name")
        private String deptName;

        @JsonProperty("submitted_at")
        private LocalDateTime submittedAt;

        @JsonProperty("modifiable_until")
        private LocalDateTime modifiableUntil;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;

        private String status;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SimpleOption {
        private Long id;
        private Long value;
        private String label;
        private String name;

        @JsonProperty("course_name")
        private String courseName;

        @JsonProperty("teaching_org_id")
        private Long teachingOrgId;

        @JsonProperty("service_org_id")
        private Long serviceOrgId;

        @JsonProperty("course_id")
        private Long courseId;
    }
}
