package com.campus.evaluation.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.message.domain.entity.HelpFaq;
import com.campus.evaluation.message.domain.entity.HelpGuide;
import com.campus.evaluation.message.domain.entity.HelpTicket;
import com.campus.evaluation.message.mapper.HelpFaqMapper;
import com.campus.evaluation.message.mapper.HelpGuideMapper;
import com.campus.evaluation.message.mapper.HelpTicketMapper;
import com.campus.evaluation.message.service.HelpCenterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HelpCenterServiceImpl implements HelpCenterService {

    private final HelpGuideMapper guideMapper;
    private final HelpFaqMapper faqMapper;
    private final HelpTicketMapper ticketMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<Map<String, Object>> listGuides(Map<String, Object> params) {
        boolean platformAdmin = isPlatformAdmin();
        Long tenantId = platformAdmin ? null : requireTenantId();
        Long schoolId = SecurityUtils.getSchoolId();
        LambdaQueryWrapper<HelpGuide> wrapper = new LambdaQueryWrapper<HelpGuide>()
                .eq(HelpGuide::getEnabled, true)
                .orderByAsc(HelpGuide::getSortOrder)
                .orderByDesc(HelpGuide::getCreatedAt);
        if (!platformAdmin) {
            wrapper.eq(HelpGuide::getTenantId, tenantId);
        }

        String module = asString(params.get("module"));
        if (module != null && !module.isBlank() && !"all".equals(module)) {
            wrapper.eq(HelpGuide::getModule, module);
        }

        List<HelpGuide> rows = guideMapper.selectList(wrapper).stream()
                .filter(row -> platformAdmin || belongsToSchool(row.getSchoolId(), schoolId))
                .filter(row -> belongsToCurrentRole(row.getTargetRoles()))
                .toList();
        return rows.stream().map(this::toMap).toList();
    }

    @Override
    public List<Map<String, Object>> listFaqs(Map<String, Object> params) {
        boolean platformAdmin = isPlatformAdmin();
        Long tenantId = platformAdmin ? null : requireTenantId();
        Long schoolId = SecurityUtils.getSchoolId();
        LambdaQueryWrapper<HelpFaq> wrapper = new LambdaQueryWrapper<HelpFaq>()
                .eq(HelpFaq::getEnabled, true)
                .orderByAsc(HelpFaq::getSortOrder)
                .orderByDesc(HelpFaq::getCreatedAt);
        if (!platformAdmin) {
            wrapper.eq(HelpFaq::getTenantId, tenantId);
        }

        String category = asString(params.get("category"));
        if (category != null && !category.isBlank() && !"all".equals(category)) {
            wrapper.eq(HelpFaq::getCategory, category);
        }

        List<HelpFaq> rows = faqMapper.selectList(wrapper).stream()
                .filter(row -> platformAdmin || belongsToSchool(row.getSchoolId(), schoolId))
                .filter(row -> belongsToCurrentRole(row.getTargetRoles()))
                .toList();
        return rows.stream().map(this::toMap).toList();
    }

    @Override
    public List<Map<String, Object>> listTickets(Map<String, Object> params) {
        boolean platformAdmin = isPlatformAdmin();
        LambdaQueryWrapper<HelpTicket> wrapper = new LambdaQueryWrapper<HelpTicket>()
                .orderByDesc(HelpTicket::getCreatedAt);
        if (!platformAdmin) {
            wrapper.eq(HelpTicket::getTenantId, requireTenantId())
                    .eq(HelpTicket::getSubmitterId, requireUserId());
        }

        Long id = asLong(params.get("id"));
        if (id != null) {
            wrapper.eq(HelpTicket::getId, id);
        }
        String status = asString(params.get("status"));
        if (status != null && !status.isBlank() && !"all".equals(status)) {
            wrapper.eq(HelpTicket::getStatus, status);
        }

        List<HelpTicket> rows = ticketMapper.selectList(wrapper);
        Integer limit = asInteger(params.get("_limit"));
        if (limit != null && limit > 0 && rows.size() > limit) {
            rows = rows.subList(0, limit);
        }
        return rows.stream().map(this::toMap).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> createTicket(Map<String, Object> payload) {
        HelpTicket ticket = new HelpTicket();
        ticket.setTenantId(requireTenantId());
        ticket.setSchoolId(SecurityUtils.getSchoolId());
        ticket.setSubmitterId(requireUserId());
        ticket.setTicketNo(defaultValue(payload, "ticket_no", generateTicketNo()));
        ticket.setTitle(required(payload, "title"));
        ticket.setContent(required(payload, "content"));
        ticket.setCategory(defaultValue(payload, "category", "other"));
        ticket.setPriority(defaultValue(payload, "priority", "normal"));
        ticket.setStatus("pending");
        ticket.setAttachmentFileIds(toJsonArray(payload.get("attachment_file_ids")));
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setDeleted(0);
        ticketMapper.insert(ticket);
        return toMap(ticket);
    }

    @Override
    @Transactional
    public Map<String, Object> updateTicket(Long id, Map<String, Object> payload) {
        boolean platformAdmin = isPlatformAdmin();
        HelpTicket ticket = platformAdmin ? findTicket(id) : findMyTicket(id);
        String status = asString(payload.get("status"));
        String replyContent = asString(payload.get("reply_content"));
        if (platformAdmin && replyContent != null && !replyContent.isBlank()) {
            ticket.setReplyContent(replyContent.trim());
            ticket.setRepliedBy(requireUserId());
            ticket.setRepliedAt(LocalDateTime.now());
            ticket.setStatus("replied");
        }
        if ("closed".equals(status)) {
            if (!platformAdmin && !"replied".equals(ticket.getStatus())) {
                throw new BusinessException(409, "只能关闭已回复的工单");
            }
            ticket.setStatus("closed");
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        return toMap(platformAdmin ? findTicket(id) : findMyTicket(id));
    }

    private HelpTicket findTicket(Long id) {
        if (id == null) {
            throw new BusinessException(400, "ticket id is required");
        }
        HelpTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "ticket not found");
        }
        return ticket;
    }

    private HelpTicket findMyTicket(Long id) {
        if (id == null) {
            throw new BusinessException(400, "工单ID不能为空");
        }
        HelpTicket ticket = ticketMapper.selectOne(new LambdaQueryWrapper<HelpTicket>()
                .eq(HelpTicket::getId, id)
                .eq(HelpTicket::getTenantId, requireTenantId())
                .eq(HelpTicket::getSubmitterId, requireUserId()));
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }
        return ticket;
    }

    private Map<String, Object> toMap(HelpGuide entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("title", entity.getTitle());
        map.put("module", entity.getModule());
        map.put("summary", entity.getSummary());
        map.put("steps", readJsonArray(entity.getSteps()));
        map.put("target_roles", readJsonArray(entity.getTargetRoles()));
        map.put("related_link", entity.getRelatedLink());
        map.put("sort_order", entity.getSortOrder());
        map.put("enabled", entity.getEnabled());
        map.put("created_at", entity.getCreatedAt());
        map.put("updated_at", entity.getUpdatedAt());
        map.put("deleted", entity.getDeleted());
        return map;
    }

    private Map<String, Object> toMap(HelpFaq entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("question", entity.getQuestion());
        map.put("answer", entity.getAnswer());
        map.put("category", entity.getCategory());
        map.put("target_roles", readJsonArray(entity.getTargetRoles()));
        map.put("keywords", entity.getKeywords());
        map.put("sort_order", entity.getSortOrder());
        map.put("enabled", entity.getEnabled());
        map.put("status", entity.getStatus());
        map.put("created_at", entity.getCreatedAt());
        map.put("updated_at", entity.getUpdatedAt());
        map.put("deleted", entity.getDeleted());
        return map;
    }

    private Map<String, Object> toMap(HelpTicket entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("submitter_id", entity.getSubmitterId());
        map.put("ticket_no", entity.getTicketNo());
        map.put("title", entity.getTitle());
        map.put("content", entity.getContent());
        map.put("category", entity.getCategory());
        map.put("priority", entity.getPriority());
        map.put("status", entity.getStatus());
        map.put("attachment_file_ids", readJsonArray(entity.getAttachmentFileIds()));
        map.put("reply_content", entity.getReplyContent());
        map.put("replied_by", entity.getRepliedBy());
        map.put("replied_at", entity.getRepliedAt());
        map.put("created_at", entity.getCreatedAt());
        map.put("updated_at", entity.getUpdatedAt());
        map.put("deleted", entity.getDeleted());
        return map;
    }

    private boolean belongsToSchool(Long rowSchoolId, Long currentSchoolId) {
        return rowSchoolId == null || currentSchoolId == null || rowSchoolId.equals(currentSchoolId);
    }

    private boolean belongsToCurrentRole(String targetRoles) {
        if (isPlatformAdmin()) {
            return true;
        }
        List<String> roles = readJsonArray(targetRoles);
        if (roles.isEmpty()) {
            return true;
        }
        List<String> currentRoles = SecurityUtils.getRoles();
        return roles.stream().anyMatch(currentRoles::contains);
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(403, "无法获取租户信息");
        }
        return tenantId;
    }

    private boolean isPlatformAdmin() {
        return SecurityUtils.getRoles().contains("system_admin");
    }

    private Long requireUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "登录已过期");
        }
        return userId;
    }

    private String generateTicketNo() {
        return "HT" + java.time.LocalDate.now().toString().replace("-", "") + System.currentTimeMillis() % 100000;
    }

    private String toJsonArray(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            if (text.isBlank()) {
                return null;
            }
            if (text.trim().startsWith("[")) {
                return text;
            }
            value = Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> readJsonArray(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception ignored) {
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
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
        return value == null || value.isBlank() ? fallback : value.trim();
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

    private static Integer asInteger(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return value instanceof Number number ? number.intValue() : Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
