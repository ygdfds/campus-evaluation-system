package com.campus.evaluation.school.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CourseDetailVO {
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private Long teachingOrgId;
    private String teachingOrgName;
    private String courseCode;
    private String courseName;
    private String term;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private List<TeacherInfo> teachers;
    private Integer enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class TeacherInfo {
        private Long teacherId;
        private String realName;
        private String roleStatus;
    }
}
