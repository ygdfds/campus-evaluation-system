package com.campus.evaluation.complaint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.complaint.domain.entity.Complaint;
import com.campus.evaluation.complaint.domain.entity.ComplaintProcessRecord;
import com.campus.evaluation.complaint.domain.entity.FeedbackWorkOrder;
import com.campus.evaluation.complaint.mapper.ComplaintMapper;
import com.campus.evaluation.complaint.mapper.ComplaintProcessRecordMapper;
import com.campus.evaluation.complaint.mapper.FeedbackWorkOrderMapper;
import com.campus.evaluation.complaint.service.ComplaintService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final ComplaintProcessRecordMapper processRecordMapper;
    private final FeedbackWorkOrderMapper workOrderMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<Map<String, Object>> listComplaints(Map<String, Object> params) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getTenantId, tenantId)
                .orderByDesc(Complaint::getCreatedAt);

        if (isStudent()) {
            wrapper.eq(Complaint::getSubmitterId, SecurityUtils.getUserId());
        } else {
            Long submitterId = asLong(params.get("submitter_id"));
            if (submitterId != null) {
                wrapper.eq(Complaint::getSubmitterId, submitterId);
            }
        }
        eqIfPresent(wrapper, Complaint::getStatus, asString(params.get("status")));
        eqIfPresent(wrapper, Complaint::getComplaintType, asString(params.get("complaint_type")));
        eqIfPresent(wrapper, Complaint::getTargetType, asString(params.get("target_type")));

        String keyword = asString(params.get("keyword"));
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Complaint::getTitle, keyword)
                    .or().like(Complaint::getContent, keyword));
        }
        return complaintMapper.selectList(wrapper).stream().map(this::toComplaintMap).toList();
    }

    @Override
    public Map<String, Object> getComplaint(Long id) {
        return toComplaintMap(findComplaint(id));
    }

    @Override
    @Transactional
    public Map<String, Object> createComplaint(Map<String, Object> payload) {
        Long tenantId = requireTenantId();
        Complaint entity = new Complaint();
        entity.setTenantId(tenantId);
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setSubmitterId(SecurityUtils.getUserId());
        entity.setComplaintType(required(payload, "complaint_type"));
        entity.setTargetType(required(payload, "target_type"));
        entity.setTargetId(asString(payload.get("target_id")));
        entity.setCourseId(asLong(payload.get("course_id")));
        entity.setTeachingOrgId(asLong(payload.get("teaching_org_id")));
        entity.setServiceItemId(asLong(payload.get("service_item_id")));
        entity.setServiceOrgId(asLong(payload.get("service_org_id")));
        entity.setTitle(required(payload, "title"));
        entity.setContent(required(payload, "content"));
        entity.setStatus(defaultValue(payload, "status", "pending"));
        entity.setPriority(defaultValue(payload, "priority", "normal"));
        entity.setAnonymousToHandler(asBoolean(payload.get("anonymous_to_handler"), false));
        entity.setAttachmentFileIds(writeAttachmentIds(payload.get("attachment_file_ids")));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(0);
        complaintMapper.insert(entity);
        return toComplaintMap(entity);
    }

    @Override
    @Transactional
    public Map<String, Object> updateComplaint(Long id, Map<String, Object> payload) {
        Complaint entity = findComplaint(id);
        if (isStudent() && !SecurityUtils.getUserId().equals(entity.getSubmitterId())) {
            throw new BusinessException(403, "只能修改自己的投诉建议");
        }
        if (payload.containsKey("status")) {
            entity.setStatus(asString(payload.get("status")));
        }
        if (payload.containsKey("priority")) {
            entity.setPriority(asString(payload.get("priority")));
        }
        if (payload.containsKey("cancelled_at")) {
            entity.setCancelledAt(asDateTime(payload.get("cancelled_at")));
        }
        if (payload.containsKey("cancel_reason")) {
            entity.setCancelReason(asString(payload.get("cancel_reason")));
        }
        if (payload.containsKey("resolved_at")) {
            entity.setResolvedAt(asDateTime(payload.get("resolved_at")));
        }
        if (payload.containsKey("attachment_file_ids")) {
            entity.setAttachmentFileIds(writeAttachmentIds(payload.get("attachment_file_ids")));
        }
        entity.setUpdatedAt(LocalDateTime.now());
        complaintMapper.updateById(entity);
        return toComplaintMap(findComplaint(id));
    }

    @Override
    public List<Map<String, Object>> listProcessRecords(Map<String, Object> params) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<ComplaintProcessRecord> wrapper = new LambdaQueryWrapper<ComplaintProcessRecord>()
                .eq(ComplaintProcessRecord::getTenantId, tenantId)
                .eq(asLong(params.get("complaint_id")) != null,
                        ComplaintProcessRecord::getComplaintId, asLong(params.get("complaint_id")))
                .orderByAsc(ComplaintProcessRecord::getCreatedAt);
        return processRecordMapper.selectList(wrapper).stream().map(this::toProcessMap).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> createProcessRecord(Map<String, Object> payload) {
        Long tenantId = requireTenantId();
        Long complaintId = asLong(payload.get("complaint_id"));
        if (complaintId == null) {
            throw new BusinessException(400, "complaint_id不能为空");
        }
        findComplaint(complaintId);
        ComplaintProcessRecord entity = new ComplaintProcessRecord();
        entity.setTenantId(tenantId);
        entity.setComplaintId(complaintId);
        entity.setHandlerId(asLong(payload.get("handler_id")));
        entity.setFromStatus(asString(payload.get("from_status")));
        entity.setToStatus(required(payload, "to_status"));
        entity.setContent(asString(payload.get("content")));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDeleted(0);
        processRecordMapper.insert(entity);
        return toProcessMap(entity);
    }

    @Override
    public List<Map<String, Object>> listWorkOrders(Map<String, Object> params) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<FeedbackWorkOrder> wrapper = new LambdaQueryWrapper<FeedbackWorkOrder>()
                .eq(FeedbackWorkOrder::getTenantId, tenantId)
                .orderByDesc(FeedbackWorkOrder::getCreatedAt);
        if (isStudent()) {
            wrapper.eq(FeedbackWorkOrder::getSubmitterId, SecurityUtils.getUserId());
        } else {
            Long submitterId = asLong(params.get("submitter_id"));
            if (submitterId != null) {
                wrapper.eq(FeedbackWorkOrder::getSubmitterId, submitterId);
            }
        }
        eqIfPresent(wrapper, FeedbackWorkOrder::getSource, asString(params.get("source")));
        Long sourceId = asLong(params.get("source_id"));
        if (sourceId != null) {
            wrapper.eq(FeedbackWorkOrder::getSourceId, sourceId);
        }
        eqIfPresent(wrapper, FeedbackWorkOrder::getStatus, asString(params.get("status")));
        return workOrderMapper.selectList(wrapper).stream().map(this::toWorkOrderMap).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> createWorkOrder(Map<String, Object> payload) {
        Long tenantId = requireTenantId();
        FeedbackWorkOrder entity = new FeedbackWorkOrder();
        entity.setTenantId(tenantId);
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setSubmitterId(asLong(payload.get("submitter_id")));
        entity.setSource(required(payload, "source"));
        entity.setSourceId(asLong(payload.get("source_id")));
        entity.setStatus(defaultValue(payload, "status", "pending"));
        entity.setPriority(defaultValue(payload, "priority", "normal"));
        entity.setHandlerOrgId(asLong(payload.get("handler_org_id")));
        entity.setAssigneeId(asLong(payload.get("assignee_id")));
        entity.setCompletedAt(asDateTime(payload.get("completed_at")));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(0);
        workOrderMapper.insert(entity);
        return toWorkOrderMap(entity);
    }

    @Override
    @Transactional
    public Map<String, Object> updateWorkOrder(Long id, Map<String, Object> payload) {
        FeedbackWorkOrder entity = findWorkOrder(id);
        if (payload.containsKey("status")) {
            entity.setStatus(asString(payload.get("status")));
        }
        if (payload.containsKey("priority")) {
            entity.setPriority(asString(payload.get("priority")));
        }
        if (payload.containsKey("handler_org_id")) {
            entity.setHandlerOrgId(asLong(payload.get("handler_org_id")));
        }
        if (payload.containsKey("assignee_id")) {
            entity.setAssigneeId(asLong(payload.get("assignee_id")));
        }
        if (payload.containsKey("completed_at")) {
            entity.setCompletedAt(asDateTime(payload.get("completed_at")));
        }
        entity.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(entity);
        return toWorkOrderMap(findWorkOrder(id));
    }

    private Complaint findComplaint(Long id) {
        if (id == null) {
            throw new BusinessException(400, "投诉记录ID不能为空");
        }
        Complaint entity = complaintMapper.selectOne(new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getId, id)
                .eq(Complaint::getTenantId, requireTenantId()));
        if (entity == null) {
            throw new BusinessException(404, "投诉记录不存在");
        }
        if (isStudent() && !SecurityUtils.getUserId().equals(entity.getSubmitterId())) {
            throw new BusinessException(403, "无权访问该投诉记录");
        }
        return entity;
    }

    private FeedbackWorkOrder findWorkOrder(Long id) {
        FeedbackWorkOrder entity = workOrderMapper.selectOne(new LambdaQueryWrapper<FeedbackWorkOrder>()
                .eq(FeedbackWorkOrder::getId, id)
                .eq(FeedbackWorkOrder::getTenantId, requireTenantId()));
        if (entity == null) {
            throw new BusinessException(404, "反馈工单不存在");
        }
        if (isStudent() && !SecurityUtils.getUserId().equals(entity.getSubmitterId())) {
            throw new BusinessException(403, "无权修改该反馈工单");
        }
        return entity;
    }

    private Map<String, Object> toComplaintMap(Complaint entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("submitter_id", entity.getSubmitterId());
        map.put("complaint_type", entity.getComplaintType());
        map.put("target_type", entity.getTargetType());
        map.put("target_id", parseTargetId(entity.getTargetId()));
        map.put("course_id", entity.getCourseId());
        map.put("teaching_org_id", entity.getTeachingOrgId());
        map.put("service_item_id", entity.getServiceItemId());
        map.put("service_org_id", entity.getServiceOrgId());
        map.put("title", entity.getTitle());
        map.put("content", entity.getContent());
        map.put("status", entity.getStatus());
        map.put("priority", entity.getPriority());
        map.put("anonymous_to_handler", entity.getAnonymousToHandler());
        map.put("attachment_file_ids", readAttachmentIds(entity.getAttachmentFileIds()));
        map.put("cancelled_at", entity.getCancelledAt());
        map.put("cancel_reason", entity.getCancelReason());
        map.put("resolved_at", entity.getResolvedAt());
        map.put("created_at", entity.getCreatedAt());
        map.put("updated_at", entity.getUpdatedAt());
        map.put("deleted", entity.getDeleted());
        return map;
    }

    private Map<String, Object> toProcessMap(ComplaintProcessRecord entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("complaint_id", entity.getComplaintId());
        map.put("handler_id", entity.getHandlerId());
        map.put("from_status", entity.getFromStatus());
        map.put("to_status", entity.getToStatus());
        map.put("content", entity.getContent());
        map.put("created_at", entity.getCreatedAt());
        map.put("deleted", entity.getDeleted());
        return map;
    }

    private Map<String, Object> toWorkOrderMap(FeedbackWorkOrder entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("submitter_id", entity.getSubmitterId());
        map.put("source", entity.getSource());
        map.put("source_id", entity.getSourceId());
        map.put("status", entity.getStatus());
        map.put("priority", entity.getPriority());
        map.put("handler_org_id", entity.getHandlerOrgId());
        map.put("assignee_id", entity.getAssigneeId());
        map.put("completed_at", entity.getCompletedAt());
        map.put("created_at", entity.getCreatedAt());
        map.put("updated_at", entity.getUpdatedAt());
        map.put("deleted", entity.getDeleted());
        return map;
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(403, "无法获取租户信息");
        }
        return tenantId;
    }

    private boolean isStudent() {
        return SecurityUtils.hasRole("student");
    }

    private static <T> void eqIfPresent(LambdaQueryWrapper<T> wrapper,
                                        com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> column,
                                        String value) {
        wrapper.eq(value != null && !value.isBlank(), column, value);
    }

    private String writeAttachmentIds(Object value) {
        if (value == null) {
            return "[]";
        }
        try {
            if (value instanceof String text) {
                objectMapper.readTree(text);
                return text;
            }
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(400, "附件ID格式不正确");
        }
    }

    private List<Long> readAttachmentIds(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception ignored) {
            List<Long> ids = new ArrayList<>();
            for (String item : value.split(",")) {
                Long id = asLong(item.trim());
                if (id != null) {
                    ids.add(id);
                }
            }
            return ids;
        }
    }

    private Object parseTargetId(String value) {
        Long id = asLong(value);
        return id != null ? id : value;
    }

    private static String required(Map<String, Object> payload, String key) {
        String value = asString(payload.get(key));
        if (value == null || value.isBlank()) {
            throw new BusinessException(400, key + "不能为空");
        }
        return value.trim();
    }

    private static String defaultValue(Map<String, Object> payload, String key, String fallback) {
        String value = asString(payload.get(key));
        return value == null || value.isBlank() ? fallback : value;
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

    private static Boolean asBoolean(Object value, boolean fallback) {
        return value == null ? fallback : (value instanceof Boolean b ? b : Boolean.valueOf(String.valueOf(value)));
    }

    private static LocalDateTime asDateTime(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).replace("Z", ""));
        } catch (Exception ignored) {
            return null;
        }
    }
}
