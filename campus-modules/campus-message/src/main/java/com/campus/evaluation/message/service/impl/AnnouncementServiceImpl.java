package com.campus.evaluation.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.message.domain.entity.Announcement;
import com.campus.evaluation.message.mapper.AnnouncementMapper;
import com.campus.evaluation.message.service.AnnouncementService;
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
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<Map<String, Object>> list(Map<String, Object> params) {
        Long tenantId = requireTenantId();
        Long schoolId = SecurityUtils.getSchoolId();
        boolean management = SecurityUtils.hasRole("school_admin");
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getTenantId, tenantId)
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getCreatedAt);
        if (schoolId != null) {
            wrapper.and(w -> w.isNull(Announcement::getSchoolId).or().eq(Announcement::getSchoolId, schoolId));
        }
        String status = asString(params.get("status"));
        if (status != null && !status.isBlank() && !"all".equals(status)) {
            wrapper.eq(Announcement::getStatus, status);
        } else if (!management) {
            wrapper.eq(Announcement::getStatus, "published");
        }
        String tag = asString(params.get("tag"));
        if (tag != null && !tag.isBlank() && !"all".equals(tag)) {
            wrapper.eq(Announcement::getTag, tag);
        }
        String keyword = asString(params.get("keyword"));
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Announcement::getTitle, keyword)
                    .or().like(Announcement::getSummary, keyword)
                    .or().like(Announcement::getContent, keyword));
        }
        return announcementMapper.selectList(wrapper).stream()
                .filter(row -> management || visibleToCurrentRole(row))
                .map(this::toMap)
                .toList();
    }

    @Override
    public Map<String, Object> get(Long id) {
        Announcement entity = find(id);
        if (!SecurityUtils.hasRole("school_admin") && !visibleToCurrentRole(entity)) {
            throw new BusinessException(403, "无权访问该公告");
        }
        return toMap(entity);
    }

    @Override
    @Transactional
    public Map<String, Object> create(Map<String, Object> payload) {
        requireSchoolAdmin();
        Announcement entity = new Announcement();
        entity.setTenantId(requireTenantId());
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setTitle(required(payload, "title"));
        entity.setSummary(asString(payload.get("summary")));
        entity.setContent(required(payload, "content"));
        entity.setTag(defaultValue(payload, "tag", "系统公告"));
        entity.setCoverFileId(asLong(payload.get("cover_file_id")));
        entity.setTargetRoles(toJsonArray(payload.get("target_roles")));
        entity.setStatus(defaultValue(payload, "status", "draft"));
        entity.setPublishTime("published".equals(entity.getStatus()) ? LocalDateTime.now() : asDateTime(payload.get("publish_time")));
        entity.setPublisherId("published".equals(entity.getStatus()) ? SecurityUtils.getUserId() : null);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(0);
        announcementMapper.insert(entity);
        return toMap(entity);
    }

    @Override
    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> payload) {
        requireSchoolAdmin();
        Announcement entity = find(id);
        if (payload.containsKey("title")) entity.setTitle(required(payload, "title"));
        if (payload.containsKey("summary")) entity.setSummary(asString(payload.get("summary")));
        if (payload.containsKey("content")) entity.setContent(required(payload, "content"));
        if (payload.containsKey("tag")) entity.setTag(asString(payload.get("tag")));
        if (payload.containsKey("cover_file_id")) entity.setCoverFileId(asLong(payload.get("cover_file_id")));
        if (payload.containsKey("target_roles")) entity.setTargetRoles(toJsonArray(payload.get("target_roles")));
        if (payload.containsKey("status")) {
            String status = asString(payload.get("status"));
            entity.setStatus(status);
            if ("published".equals(status)) {
                entity.setPublishTime(LocalDateTime.now());
                entity.setPublisherId(SecurityUtils.getUserId());
            }
        }
        if (payload.containsKey("publish_time")) {
            entity.setPublishTime(asDateTime(payload.get("publish_time")));
        }
        if (payload.containsKey("deleted")) {
            entity.setDeleted(Boolean.parseBoolean(String.valueOf(payload.get("deleted"))) ? 1 : 0);
        }
        entity.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(entity);
        return toMap(find(id));
    }

    private Announcement find(Long id) {
        if (id == null) {
            throw new BusinessException(400, "公告ID不能为空");
        }
        Announcement entity = announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .eq(Announcement::getTenantId, requireTenantId()));
        if (entity == null) {
            throw new BusinessException(404, "公告不存在");
        }
        return entity;
    }

    private boolean visibleToCurrentRole(Announcement entity) {
        List<String> roles = readJsonArray(entity.getTargetRoles());
        return roles.isEmpty() || roles.stream().anyMatch(SecurityUtils.getRoles()::contains);
    }

    private Map<String, Object> toMap(Announcement entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("title", entity.getTitle());
        map.put("summary", entity.getSummary());
        map.put("content", entity.getContent());
        map.put("tag", entity.getTag());
        map.put("cover_file_id", entity.getCoverFileId());
        map.put("target_roles", readJsonArray(entity.getTargetRoles()));
        map.put("status", entity.getStatus());
        map.put("publish_time", entity.getPublishTime());
        map.put("publisher_id", entity.getPublisherId());
        map.put("created_at", entity.getCreatedAt());
        map.put("updated_at", entity.getUpdatedAt());
        map.put("deleted", entity.getDeleted());
        return map;
    }

    private void requireSchoolAdmin() {
        if (!SecurityUtils.hasRole("school_admin")) {
            throw new BusinessException(403, "仅学校管理员可以管理公告");
        }
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(403, "无法获取租户信息");
        }
        return tenantId;
    }

    private String toJsonArray(Object value) {
        if (value == null) return null;
        if (value instanceof String text && text.isBlank()) return null;
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
            throw new BusinessException(400, "公告可见角色格式不正确");
        }
    }

    private List<String> readJsonArray(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception ignored) {
            return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        }
    }

    private static String required(Map<String, Object> payload, String key) {
        String value = asString(payload.get(key));
        if (value == null || value.isBlank()) throw new BusinessException(400, key + "不能为空");
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
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static LocalDateTime asDateTime(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            return LocalDateTime.parse(String.valueOf(value).replace("Z", ""));
        } catch (Exception ignored) {
            return null;
        }
    }
}
