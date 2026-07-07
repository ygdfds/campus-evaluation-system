package com.campus.evaluation.common.log.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志持久化服务
 * <p>
 * 将操作日志异步写入 log_operation_log 表。
 * 写入失败仅打印 warn 日志，不影响主业务流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 记录操作日志
     *
     * @param module     所属模块（school / file / auth 等）
     * @param action     操作类型（CREATE / UPDATE / DELETE 等）
     * @param targetType 业务对象类型（可为空）
     * @param targetId   业务对象ID（可为空）
     * @param content    操作描述
     * @param result     success / fail
     * @param errorMsg   异常信息（成功时传 null）
     */
    public void save(String module, String action, String targetType, Long targetId,
                     String content, String result, String errorMsg) {
        try {
            Long tenantId = safeGetTenantId();
            Long schoolId = safeGetSchoolId();
            Long userId = safeGetUserId();
            String ip = getClientIp();

            String fullContent = content;
            if (errorMsg != null && !errorMsg.isEmpty()) {
                fullContent = content + " | 异常: " + truncate(errorMsg, 500);
            }

            jdbcTemplate.update(
                    "INSERT INTO log_operation_log (tenant_id, school_id, user_id, module, action, " +
                            "target_type, target_id, target_name, content, result, ip, device, created_at, deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                    tenantId != null ? tenantId : 0,
                    schoolId,
                    userId,
                    module,
                    action,
                    targetType,
                    targetId,
                    null,  // target_name — 预留，后续可扩展
                    truncate(fullContent, 3000),
                    result,
                    ip,
                    null,  // device — 预留
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            log.warn("[操作日志] 写入数据库失败: module={}, action={}, error={}", module, action, e.getMessage());
        }
    }

    // ──────────── Sa-Token 安全取值（不抛异常） ────────────

    private Long safeGetTenantId() {
        return safeGetLoginUserField("tenantId");
    }

    private Long safeGetSchoolId() {
        return safeGetLoginUserField("schoolId");
    }

    /**
     * 通过反射从 Sa-Token Session 中的 LoginUser 对象提取字段值。
     * 避免 campus-common-log 直接依赖 campus-common-security。
     */
    private Long safeGetLoginUserField(String fieldName) {
        try {
            if (!StpUtil.isLogin()) return null;
            Object loginUser = StpUtil.getSession().get("loginUser");
            if (loginUser == null) return null;
            String getter = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method method = loginUser.getClass().getMethod(getter);
            Object value = method.invoke(loginUser);
            return value != null ? Long.valueOf(value.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long safeGetUserId() {
        try {
            if (!StpUtil.isLogin()) return null;
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    // ──────────── HTTP 请求信息 ────────────

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // X-Forwarded-For 可能包含多个 IP，取第一个
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }
}
