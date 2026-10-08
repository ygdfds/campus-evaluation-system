package com.campus.evaluation.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.review.domain.entity.AppealProcessRecord;
import com.campus.evaluation.review.domain.entity.AppealRequest;
import com.campus.evaluation.review.domain.entity.TraceAuthorization;
import com.campus.evaluation.review.mapper.AppealProcessRecordMapper;
import com.campus.evaluation.review.mapper.AppealRequestMapper;
import com.campus.evaluation.review.mapper.TraceAuthorizationMapper;
import com.campus.evaluation.review.service.ReviewService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final DateTimeFormatter APPEAL_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AppealRequestMapper appealMapper;
    private final AppealProcessRecordMapper processRecordMapper;
    private final TraceAuthorizationMapper traceMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<Map<String, Object>> listAppeals(Map<String, Object> params) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<AppealRequest> wrapper = new LambdaQueryWrapper<AppealRequest>()
                .eq(AppealRequest::getTenantId, tenantId)
                .orderByDesc(AppealRequest::getSubmittedAt);
        eqIfPresent(wrapper, AppealRequest::getStatus, asString(params.get("status")));
        eqIfPresent(wrapper, AppealRequest::getAppealType, asString(params.get("appeal_type")));
        eqIfPresent(wrapper, AppealRequest::getTargetType, asString(params.get("target_type")));
        Long appellantId = asLong(params.get("appellant_user_id"));
        if (appellantId != null) {
            wrapper.eq(AppealRequest::getAppellantUserId, appellantId);
        }
        Long handlerId = asLong(params.get("handler_id"));
        if (handlerId != null) {
            wrapper.eq(AppealRequest::getHandlerId, handlerId);
        }
        String keyword = asString(params.get("keyword"));
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(AppealRequest::getAppealNo, keyword)
                    .or().like(AppealRequest::getReason, keyword));
        }
        return appealMapper.selectList(wrapper).stream().map(this::toAppealMap).toList();
    }

    @Override
    public Map<String, Object> getAppeal(Long id) {
        return toAppealMap(findAppeal(id));
    }

    @Override
    @Transactional
    public Map<String, Object> createAppeal(Map<String, Object> payload) {
        requireStudentOrStaff();
        AppealRequest entity = new AppealRequest();
        entity.setTenantId(requireTenantId());
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setAppealNo(defaultValue(payload, "appeal_no", generateAppealNo()));
        entity.setSubmissionId(requiredLong(payload, "submission_id"));
        entity.setFormId(requiredLong(payload, "form_id"));
        entity.setTargetType(required(payload, "target_type"));
        entity.setTargetId(requiredLong(payload, "target_id"));
        entity.setAppellantUserId(SecurityUtils.getUserId());
        entity.setAppealType(required(payload, "appeal_type"));
        entity.setReason(required(payload, "reason"));
        entity.setEvidenceFileIds(toJsonArray(payload.get("evidence_file_ids")));
        entity.setStatus("pending");
        entity.setPriority(defaultValue(payload, "priority", "normal"));
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(0);
        appealMapper.insert(entity);
        return toAppealMap(entity);
    }

    @Override
    @Transactional
    public Map<String, Object> updateAppeal(Long id, Map<String, Object> payload) {
        requireStaff();
        AppealRequest entity = findAppeal(id);
        String status = asString(payload.get("status"));
        if (status != null && !status.isBlank()) {
            entity.setStatus(status);
            if ("processing".equals(status) && entity.getAcceptedAt() == null) {
                entity.setAcceptedAt(LocalDateTime.now());
            }
            if (List.of("resolved", "rejected", "closed").contains(status)) {
                entity.setResolvedAt(LocalDateTime.now());
            }
        }
        if (payload.containsKey("handler_id")) {
            entity.setHandlerId(asLong(payload.get("handler_id")));
        }
        if (payload.containsKey("handle_result")) {
            entity.setHandleResult(asString(payload.get("handle_result")));
        }
        if (payload.containsKey("handle_comment")) {
            entity.setHandleComment(asString(payload.get("handle_comment")));
        }
        if (payload.containsKey("priority")) {
            entity.setPriority(asString(payload.get("priority")));
        }
        entity.setUpdatedAt(LocalDateTime.now());
        appealMapper.updateById(entity);
        return toAppealMap(findAppeal(id));
    }

    @Override
    public List<Map<String, Object>> listAppealProcessRecords(Map<String, Object> params) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<AppealProcessRecord> wrapper = new LambdaQueryWrapper<AppealProcessRecord>()
                .eq(AppealProcessRecord::getTenantId, tenantId)
                .orderByAsc(AppealProcessRecord::getCreatedAt);
        Long appealId = asLong(params.get("appeal_id"));
        if (appealId != null) {
            wrapper.eq(AppealProcessRecord::getAppealId, appealId);
        }
        return processRecordMapper.selectList(wrapper).stream().map(this::toProcessRecordMap).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> createAppealProcessRecord(Map<String, Object> payload) {
        requireStaff();
        Long appealId = requiredLong(payload, "appeal_id");
        AppealRequest appeal = findAppeal(appealId);
        AppealProcessRecord entity = new AppealProcessRecord();
        entity.setTenantId(requireTenantId());
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setAppealId(appealId);
        entity.setOperatorId(SecurityUtils.getUserId());
        entity.setAction(required(payload, "action"));
        entity.setFromStatus(asString(payload.get("from_status")));
        entity.setToStatus(asString(payload.get("to_status")));
        entity.setContent(asString(payload.get("content")));
        entity.setCreatedAt(LocalDateTime.now());
        processRecordMapper.insert(entity);
        return toProcessRecordMap(entity);
    }

    @Override
    public List<Map<String, Object>> listTraceAuthorizations(Map<String, Object> params) {
        requireSchoolAdmin();
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<TraceAuthorization> wrapper = new LambdaQueryWrapper<TraceAuthorization>()
                .eq(TraceAuthorization::getTenantId, tenantId)
                .orderByDesc(TraceAuthorization::getRequestedAt);
        eqIfPresent(wrapper, TraceAuthorization::getStatus, asString(params.get("status")));
        Long appealId = asLong(params.get("appeal_id"));
        if (appealId != null) {
            wrapper.eq(TraceAuthorization::getAppealId, appealId);
        }
        Long applicantId = asLong(params.get("applicant_id"));
        if (applicantId != null) {
            wrapper.eq(TraceAuthorization::getApplicantId, applicantId);
        }
        return traceMapper.selectList(wrapper).stream().map(this::toTraceMap).toList();
    }

    @Override
    public Map<String, Object> getTraceAuthorization(Long id) {
        requireSchoolAdmin();
        return toTraceMap(findTrace(id));
    }

    @Override
    @Transactional
    public Map<String, Object> createTraceAuthorization(Map<String, Object> payload) {
        requireStaff();
        TraceAuthorization entity = new TraceAuthorization();
        entity.setTenantId(requireTenantId());
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setSubmissionId(requiredLong(payload, "submission_id"));
        entity.setAppealId(requiredLong(payload, "appeal_id"));
        entity.setApplicantId(SecurityUtils.getUserId());
        entity.setReason(required(payload, "reason"));
        entity.setStatus("pending");
        entity.setRequestedAt(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(0);
        traceMapper.insert(entity);
        return toTraceMap(entity);
    }

    @Override
    @Transactional
    public Map<String, Object> updateTraceAuthorization(Long id, Map<String, Object> payload) {
        requireSchoolAdmin();
        TraceAuthorization entity = findTrace(id);
        String status = asString(payload.get("status"));
        if (status == null || !List.of("approved", "rejected").contains(status)) {
            throw new BusinessException(400, "追溯授权只能审批通过或驳回");
        }
        if (!"pending".equals(entity.getStatus())) {
            throw new BusinessException(409, "该追溯授权已处理");
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(status);
        entity.setApproverId(SecurityUtils.getUserId());
        entity.setUpdatedAt(now);
        if ("approved".equals(status)) {
            entity.setApprovedAt(now);
        } else {
            entity.setRejectedAt(now);
            entity.setRejectReason(required(payload, "reject_reason"));
        }
        traceMapper.updateById(entity);
        return toTraceMap(findTrace(id));
    }

    private AppealRequest findAppeal(Long id) {
        if (id == null) {
            throw new BusinessException(400, "申诉ID不能为空");
        }
        AppealRequest entity = appealMapper.selectOne(new LambdaQueryWrapper<AppealRequest>()
                .eq(AppealRequest::getId, id)
                .eq(AppealRequest::getTenantId, requireTenantId()));
        if (entity == null) {
            throw new BusinessException(404, "申诉记录不存在");
        }
        if (SecurityUtils.hasRole("student")
                && !SecurityUtils.getUserId().equals(entity.getAppellantUserId())) {
            throw new BusinessException(403, "无权访问该申诉");
        }
        return entity;
    }

    private TraceAuthorization findTrace(Long id) {
        if (id == null) {
            throw new BusinessException(400, "追溯授权ID不能为空");
        }
        TraceAuthorization entity = traceMapper.selectOne(new LambdaQueryWrapper<TraceAuthorization>()
                .eq(TraceAuthorization::getId, id)
                .eq(TraceAuthorization::getTenantId, requireTenantId()));
        if (entity == null) {
            throw new BusinessException(404, "追溯授权记录不存在");
        }
        return entity;
    }

    private Map<String, Object> toAppealMap(AppealRequest entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("appeal_no", entity.getAppealNo());
        map.put("submission_id", entity.getSubmissionId());
        map.put("form_id", entity.getFormId());
        map.put("target_type", entity.getTargetType());
        map.put("target_id", entity.getTargetId());
        map.put("appellant_user_id", entity.getAppellantUserId());
        map.put("appeal_type", entity.getAppealType());
        map.put("reason", entity.getReason());
        map.put("evidence_file_ids", readJsonArray(entity.getEvidenceFileIds()));
        map.put("status", entity.getStatus());
        map.put("priority", entity.getPriority());
        map.put("handler_id", entity.getHandlerId());
        map.put("handle_result", entity.getHandleResult());
        map.put("handle_comment", entity.getHandleComment());
        map.put("submitted_at", entity.getSubmittedAt());
        map.put("accepted_at", entity.getAcceptedAt());
        map.put("resolved_at", entity.getResolvedAt());
        map.put("created_at", entity.getCreatedAt());
        map.put("updated_at", entity.getUpdatedAt());
        map.put("deleted", entity.getDeleted());
        return map;
    }

    private Map<String, Object> toProcessRecordMap(AppealProcessRecord entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("appeal_id", entity.getAppealId());
        map.put("operator_id", entity.getOperatorId());
        map.put("action", entity.getAction());
        map.put("from_status", entity.getFromStatus());
        map.put("to_status", entity.getToStatus());
        map.put("content", entity.getContent());
        map.put("created_at", entity.getCreatedAt());
        return map;
    }

    private Map<String, Object> toTraceMap(TraceAuthorization entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("submission_id", entity.getSubmissionId());
        map.put("appeal_id", entity.getAppealId());
        map.put("applicant_id", entity.getApplicantId());
        map.put("approver_id", entity.getApproverId());
        map.put("reason", entity.getReason());
        map.put("status", entity.getStatus());
        map.put("reject_reason", entity.getRejectReason());
        map.put("requested_at", entity.getRequestedAt());
        map.put("approved_at", entity.getApprovedAt());
        map.put("rejected_at", entity.getRejectedAt());
        map.put("created_at", entity.getCreatedAt());
        map.put("updated_at", entity.getUpdatedAt());
        map.put("deleted", entity.getDeleted());
        return map;
    }

    private void requireStudentOrStaff() {
        if (!SecurityUtils.isLogin()) {
            throw new BusinessException(401, "请先登录");
        }
    }

    private void requireStaff() {
        if (!SecurityUtils.getRoles().stream().anyMatch(role -> List.of(
                "school_admin", "teaching_admin", "service_admin", "course_owner",
                "feedback_handler", "form_publisher"
        ).contains(role))) {
            throw new BusinessException(403, "无权处理申诉");
        }
    }

    private void requireSchoolAdmin() {
        if (!SecurityUtils.hasRole("school_admin")) {
            throw new BusinessException(403, "仅学校管理员可以审批追溯授权");
        }
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(403, "无法获取租户信息");
        }
        return tenantId;
    }

    private static <T> void eqIfPresent(LambdaQueryWrapper<T> wrapper,
                                        com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> column,
                                        String value) {
        wrapper.eq(value != null && !value.isBlank() && !"all".equals(value), column, value);
    }

    private String generateAppealNo() {
        return "AP" + LocalDateTime.now().format(APPEAL_NO_DATE) + String.format("%06d",
                System.nanoTime() % 1_000_000);
    }

    private static String required(Map<String, Object> payload, String key) {
        String value = asString(payload.get(key));
        if (value == null || value.isBlank()) {
            throw new BusinessException(400, key + "不能为空");
        }
        return value.trim();
    }

    private static Long requiredLong(Map<String, Object> payload, String key) {
        Long value = asLong(payload.get(key));
        if (value == null) {
            throw new BusinessException(400, key + "不能为空");
        }
        return value;
    }

    private static String defaultValue(Map<String, Object> payload, String key, String fallback) {
        String value = asString(payload.get(key));
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String toJsonArray(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text && text.isBlank()) {
            return null;
        }
        try {
            if (value instanceof String text && text.trim().startsWith("[")) {
                objectMapper.readTree(text);
                return text;
            }
            if (value instanceof String text) {
                value = Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
            }
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(400, "附件ID格式不正确");
        }
    }

    private List<Object> readJsonArray(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception ignored) {
            return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isBlank()).map(s -> (Object) s).toList();
        }
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long asLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
