package com.campus.evaluation.platform.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PlatformDTOs {
    private PlatformDTOs() {}

    @Data
    public static class ChangeStatusDTO {
        private String status;
        private String reason;
    }

    @Data
    public static class BindPlanDTO {
        private Long planId;
        private String planName;
    }

    @Data
    public static class AuditOnboardingDTO {
        private String action;
        private String status;
        private String comment;
        private String reason;
        private Long planId;
    }

    @Data
    public static class PlanDTO {
        private String planCode;
        private String planName;
        private String description;
        private Map<String, Object> features;
        private String featuresJson;
        private BigDecimal price;
        private String status;
    }

    @Data
    public static class RoleDTO {
        private String roleCode;
        private String roleName;
        private String scopeType;
        private List<Long> permissionIds;
        private List<String> permissionCodes;
    }

    @Data
    public static class AdminUserDTO {
        private String username;
        private String password;
        private String realName;
        private String phone;
        private String email;
        private String status;
        private List<Long> roleIds;
    }

    @Data
    public static class OnboardingSubmitDTO {
        private Long planId;
        private String schoolFullName;
        private String schoolName;
        private String schoolCreditCode;
        private String contactName;
        private String contactPhone;
        private String contactEmail;
        private String submitReason;
        private String reason;
    }
}
