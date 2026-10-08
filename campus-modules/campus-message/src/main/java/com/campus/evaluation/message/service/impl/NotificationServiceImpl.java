package com.campus.evaluation.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.message.domain.entity.Notification;
import com.campus.evaluation.message.mapper.NotificationMapper;
import com.campus.evaluation.message.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public List<Map<String, Object>> list(Map<String, Object> params) {
        Long tenantId = requireTenantId();
        Long currentUserId = SecurityUtils.getUserId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getTenantId, tenantId)
                .orderByDesc(Notification::getCreatedAt);

        Long receiverUserId = asLong(params.get("receiver_user_id"));
        if (receiverUserId != null) {
            wrapper.eq(Notification::getReceiverUserId, receiverUserId);
        }
        eqIfPresent(wrapper, Notification::getType, asString(params.get("type")));
        eqIfPresent(wrapper, Notification::getBusinessType,
                firstNonBlank(asString(params.get("business_type")), asString(params.get("biz_type"))));
        eqIfPresent(wrapper, Notification::getReadStatus, asString(params.get("read_status")));
        eqIfPresent(wrapper, Notification::getPriority, asString(params.get("priority")));

        String keyword = asString(params.get("keyword"));
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Notification::getTitle, keyword)
                    .or().like(Notification::getContent, keyword));
        }

        List<Notification> rows = notificationMapper.selectList(wrapper);
        if (receiverUserId == null) {
            rows = rows.stream()
                    .filter(row -> belongsToCurrentUser(row, currentUserId))
                    .toList();
        }
        return rows.stream().map(this::toMap).toList();
    }

    @Override
    public Map<String, Object> get(Long id) {
        return toMap(findNotification(id));
    }

    @Override
    @Transactional
    public Map<String, Object> create(Map<String, Object> payload) {
        Notification entity = new Notification();
        entity.setTenantId(requireTenantId());
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setReceiverUserId(asLong(payload.get("receiver_user_id")));
        entity.setTargetRoles(asString(payload.get("target_roles")));
        entity.setType(defaultValue(payload, "type", "system"));
        entity.setBusinessType(firstNonBlank(asString(payload.get("business_type")), asString(payload.get("biz_type"))));
        entity.setTitle(required(payload, "title"));
        entity.setContent(asString(payload.get("content")));
        entity.setPriority(defaultValue(payload, "priority", "normal"));
        entity.setReadStatus(defaultValue(payload, "read_status", "unread"));
        entity.setLink(asString(payload.get("link")));
        entity.setBizId(asLong(payload.get("biz_id")));
        entity.setReadAt(asDateTime(payload.get("read_at")));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(0);
        notificationMapper.insert(entity);
        return toMap(entity);
    }

    @Override
    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> payload) {
        Notification entity = findNotification(id);
        if (payload.containsKey("read_status")) {
            entity.setReadStatus(asString(payload.get("read_status")));
            if ("read".equals(entity.getReadStatus()) && entity.getReadAt() == null) {
                entity.setReadAt(LocalDateTime.now());
            }
        }
        if (payload.containsKey("read_at")) {
            entity.setReadAt(asDateTime(payload.get("read_at")));
        }
        if (payload.containsKey("deleted")) {
            entity.setDeleted(Boolean.TRUE.equals(asBoolean(payload.get("deleted"))) ? 1 : 0);
        }
        entity.setUpdatedAt(LocalDateTime.now());
        notificationMapper.updateById(entity);
        return toMap(findNotification(id));
    }

    private Notification findNotification(Long id) {
        if (id == null) {
            throw new BusinessException(400, "通知ID不能为空");
        }
        Notification entity = notificationMapper.selectOne(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getTenantId, requireTenantId()));
        if (entity == null) {
            throw new BusinessException(404, "通知不存在");
        }
        if (!belongsToCurrentUser(entity, SecurityUtils.getUserId())) {
            throw new BusinessException(403, "无权访问该通知");
        }
        return entity;
    }

    private boolean belongsToCurrentUser(Notification entity, Long userId) {
        if (entity.getReceiverUserId() != null) {
            return entity.getReceiverUserId().equals(userId);
        }
        if (entity.getTargetRoles() == null || entity.getTargetRoles().isBlank()) {
            return true;
        }
        List<String> currentRoles = SecurityUtils.getRoles();
        return Arrays.stream(entity.getTargetRoles().split(","))
                .map(String::trim)
                .anyMatch(currentRoles::contains);
    }

    private Map<String, Object> toMap(Notification entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenant_id", entity.getTenantId());
        map.put("school_id", entity.getSchoolId());
        map.put("receiver_user_id", entity.getReceiverUserId());
        map.put("target_roles", entity.getTargetRoles());
        map.put("type", entity.getType());
        map.put("business_type", entity.getBusinessType());
        map.put("biz_type", entity.getBusinessType());
        map.put("title", entity.getTitle());
        map.put("content", entity.getContent());
        map.put("priority", entity.getPriority());
        map.put("read_status", entity.getReadStatus());
        map.put("link", entity.getLink());
        map.put("biz_id", entity.getBizId());
        map.put("read_at", entity.getReadAt());
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

    private static <T> void eqIfPresent(LambdaQueryWrapper<T> wrapper,
                                        com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> column,
                                        String value) {
        wrapper.eq(value != null && !value.isBlank() && !"all".equals(value), column, value);
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

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
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

    private static Boolean asBoolean(Object value) {
        return value == null ? null : (value instanceof Boolean b ? b : Boolean.valueOf(String.valueOf(value)));
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
