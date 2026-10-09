package com.campus.evaluation.platform.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.platform.domain.dto.PlatformDTOs.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlatformManagementService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public List<Map<String, Object>> activePlans() {
        return jdbc.queryForList("""
                SELECT id, plan_code AS planCode, plan_name AS planName,
                       COALESCE(description, '') AS description, features_json AS features,
                       price, status, created_at AS createdAt
                FROM pf_tenant_plan
                WHERE deleted = 0 AND status = 'active'
                ORDER BY id
                """).stream().map(this::normalizePlan).toList();
    }

    public PageResult<Map<String, Object>> tenants(String keyword, String status, int pageNum, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE t.deleted = 0 ");
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            where.append(" AND t.status = ? ");
            args.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (t.school_name LIKE ? OR t.tenant_code LIKE ?) ");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        long total = count("SELECT COUNT(*) FROM pf_tenant t " + where, args);
        args.add(offset(pageNum, pageSize));
        args.add(pageSize);
        List<Map<String, Object>> records = jdbc.queryForList("""
                SELECT t.id, t.id AS tenantId, t.tenant_code AS tenantCode, t.school_name AS schoolName,
                       t.status, t.created_at AS createdAt, t.updated_at AS updatedAt,
                       p.id AS planId, p.plan_name AS planName,
                       sp.id AS schoolId, sp.address, sp.website,
                       au.id AS adminUserId, au.username AS adminUsername, au.phone AS adminPhone,
                       pp.real_name AS adminName
                FROM pf_tenant t
                LEFT JOIN pf_tenant_plan p ON p.id = t.plan_id AND p.deleted = 0
                LEFT JOIN sch_school_profile sp ON sp.tenant_id = t.id AND sp.deleted = 0
                LEFT JOIN auth_person_profile pp ON pp.tenant_id = t.id AND pp.role_type = 'school_admin' AND pp.deleted = 0
                LEFT JOIN auth_user_account au ON au.id = pp.user_id AND au.deleted = 0
                """ + where + " ORDER BY t.created_at DESC LIMIT ?, ?", args.toArray()).stream()
                .map(this::normalizeTenant).toList();
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public Map<String, Object> tenantDetail(Long id) {
        Map<String, Object> tenant = first("""
                SELECT t.id, t.id AS tenantId, t.tenant_code AS tenantCode, t.school_name AS schoolName,
                       t.status, t.frozen_reason AS frozenReason, t.created_at AS createdAt,
                       p.id AS planId, p.plan_name AS planName, p.features_json AS features,
                       sp.id AS schoolId, sp.name AS schoolProfileName, sp.address, sp.website, sp.intro
                FROM pf_tenant t
                LEFT JOIN pf_tenant_plan p ON p.id = t.plan_id AND p.deleted = 0
                LEFT JOIN sch_school_profile sp ON sp.tenant_id = t.id AND sp.deleted = 0
                WHERE t.id = ? AND t.deleted = 0
                """, id);
        if (tenant == null) throw new BusinessException(404, "租户不存在");
        tenant = normalizeTenant(tenant);
        List<Map<String, Object>> admins = jdbc.queryForList("""
                SELECT au.id, au.username, au.phone, au.email, au.status,
                       pp.real_name AS realName, au.created_at AS createdAt
                FROM auth_person_profile pp
                JOIN auth_user_account au ON au.id = pp.user_id AND au.deleted = 0
                WHERE pp.tenant_id = ? AND pp.role_type = 'school_admin' AND pp.deleted = 0
                ORDER BY au.id
                """, id);
        List<Map<String, Object>> logs = jdbc.queryForList("""
                SELECT id, from_status AS fromStatus, to_status AS toStatus, reason, operator_id AS operatorId,
                       created_at AS createdAt
                FROM pf_tenant_status_log
                WHERE tenant_id = ?
                ORDER BY created_at DESC
                LIMIT 20
                """, id);
        return mapOf(
                "tenant", tenant,
                "school", mapOf("id", tenant.get("schoolId"), "name", tenant.get("schoolProfileName"), "address", tenant.get("address"), "website", tenant.get("website"), "intro", tenant.get("intro")),
                "admins", admins,
                "auditLogs", logs,
                "quota", extractQuota((String) tenant.get("features"))
        );
    }

    @Transactional
    public Map<String, Object> createTenantAdmin(Long tenantId, AdminUserDTO dto) {
        if (dto == null || isBlank(dto.getUsername()) || isBlank(dto.getRealName())
                || isBlank(dto.getPassword())) {
            throw new BusinessException(400, "用户名、姓名和初始密码不能为空");
        }
        if (first("SELECT id FROM pf_tenant WHERE id = ? AND deleted = 0", tenantId) == null) {
            throw new BusinessException(404, "租户不存在");
        }
        if (first("SELECT id FROM auth_user_account WHERE username = ? AND deleted = 0", dto.getUsername()) != null) {
            throw new BusinessException(409, "用户名已存在");
        }
        Map<String, Object> school = first("SELECT id FROM sch_school_profile WHERE tenant_id = ? AND deleted = 0 LIMIT 1", tenantId);
        if (school == null) {
            throw new BusinessException(400, "租户尚未建立学校档案");
        }

        KeyHolder userKh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO auth_user_account (tenant_id, username, password_hash, phone, email, status, must_change_password)
                    VALUES (?, ?, ?, ?, ?, ?, 1)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tenantId);
            ps.setString(2, dto.getUsername().trim());
            ps.setString(3, passwordEncoder.encode(dto.getPassword()));
            ps.setString(4, dto.getPhone());
            ps.setString(5, dto.getEmail());
            ps.setString(6, dto.getStatus() == null ? "active" : dto.getStatus());
            return ps;
        }, userKh);
        Long userId = Objects.requireNonNull(userKh.getKey()).longValue();
        jdbc.update("INSERT INTO auth_person_profile (tenant_id, user_id, real_name, role_type, department_name) VALUES (?, ?, ?, 'school_admin', '校级管理')",
                tenantId, userId, dto.getRealName().trim());
        Long roleId = ensureSchoolAdminRole(tenantId);
        jdbc.update("INSERT INTO auth_user_role (tenant_id, user_id, role_id, scope_json, created_at, updated_at, deleted) VALUES (?, ?, ?, JSON_OBJECT('schoolIds', JSON_ARRAY(?)), NOW(), NOW(), 0)",
                tenantId, userId, roleId, school.get("id"));
        return first("SELECT au.id, au.username, au.phone, au.email, au.status, pp.real_name AS realName, au.created_at AS createdAt FROM auth_user_account au JOIN auth_person_profile pp ON pp.user_id = au.id AND pp.deleted = 0 WHERE au.id = ?",
                userId);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Transactional
    public void changeTenantStatus(Long id, ChangeStatusDTO dto) {
        String status = requireStatus(dto.getStatus(), Set.of("active", "disabled", "frozen"));
        Map<String, Object> tenant = first("SELECT status FROM pf_tenant WHERE id = ? AND deleted = 0", id);
        if (tenant == null) throw new BusinessException(404, "租户不存在");
        String from = Objects.toString(tenant.get("status"), null);
        if (from.equals(status)) return;
        jdbc.update("UPDATE pf_tenant SET status = ?, frozen_reason = ?, updated_at = NOW() WHERE id = ?",
                status, dto.getReason(), id);
        jdbc.update("INSERT INTO pf_tenant_status_log (tenant_id, operator_id, from_status, to_status, reason) VALUES (?, ?, ?, ?, ?)",
                id, currentUserId(), from, status, dto.getReason());
    }

    public void bindTenantPlan(Long id, BindPlanDTO dto) {
        Long planId = dto.getPlanId();
        if (planId == null && dto.getPlanName() != null) {
            Map<String, Object> plan = first("SELECT id FROM pf_tenant_plan WHERE plan_name = ? AND status = 'active' AND deleted = 0", dto.getPlanName());
            if (plan != null) planId = ((Number) plan.get("id")).longValue();
        }
        if (planId == null) throw new BusinessException(400, "套餐不能为空");
        if (first("SELECT id FROM pf_tenant_plan WHERE id = ? AND status = 'active' AND deleted = 0", planId) == null) {
            throw new BusinessException(400, "套餐不存在或已停用");
        }
        if (first("SELECT id FROM pf_tenant WHERE id = ? AND deleted = 0", id) == null) {
            throw new BusinessException(404, "租户不存在");
        }
        jdbc.update("UPDATE pf_tenant SET plan_id = ?, updated_at = NOW() WHERE id = ? AND deleted = 0", planId, id);
    }

    public PageResult<Map<String, Object>> onboardingApplications(String status, String keyword, int pageNum, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE a.deleted = 0 ");
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            where.append(" AND a.status = ? ");
            args.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (a.school_full_name LIKE ? OR a.contact_name LIKE ?) ");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        long total = count("SELECT COUNT(*) FROM pf_school_onboarding_application a " + where, args);
        args.add(offset(pageNum, pageSize));
        args.add(pageSize);
        List<Map<String, Object>> records = jdbc.queryForList("""
                SELECT a.id, a.plan_id AS planId, p.plan_name AS planName, a.tenant_id AS tenantId,
                       a.school_full_name AS schoolName, a.school_credit_code AS creditCode,
                       a.contact_name AS contactName, a.contact_phone AS contactPhone,
                       a.contact_email AS contactEmail, a.status, a.submit_reason AS submitReason,
                       a.review_comment AS reviewComment, a.created_at AS createdAt, a.updated_at AS updatedAt
                FROM pf_school_onboarding_application a
                LEFT JOIN pf_tenant_plan p ON p.id = a.plan_id
                """ + where + " ORDER BY a.created_at DESC LIMIT ?, ?", args.toArray());
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public List<Map<String, Object>> onboardingMaterials(Long id) {
        return jdbc.queryForList("""
                SELECT id, application_id AS applicationId, file_id AS fileId, material_type AS materialType,
                       status, created_at AS createdAt
                FROM pf_onboarding_material
                WHERE application_id = ? AND deleted = 0
                ORDER BY id
                """, id);
    }

    public Map<String, Object> submitOnboarding(OnboardingSubmitDTO dto) {
        String schoolName = firstNonBlank(dto.getSchoolFullName(), dto.getSchoolName());
        if (schoolName == null || dto.getContactName() == null || dto.getContactName().isBlank()
                || dto.getContactPhone() == null || dto.getContactPhone().isBlank()) {
            throw new BusinessException(400, "学校名称、联系人和联系电话不能为空");
        }
        if (dto.getPlanId() != null && first("SELECT id FROM pf_tenant_plan WHERE id = ? AND status = 'active' AND deleted = 0", dto.getPlanId()) == null) {
            throw new BusinessException(400, "选择的套餐不存在或已停用");
        }
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO pf_school_onboarding_application
                    (plan_id, school_full_name, school_credit_code, contact_name, contact_phone, contact_email, status, submit_reason)
                    VALUES (?, ?, ?, ?, ?, ?, 'pending', ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, dto.getPlanId());
            ps.setString(2, schoolName);
            ps.setString(3, dto.getSchoolCreditCode());
            ps.setString(4, dto.getContactName());
            ps.setString(5, dto.getContactPhone());
            ps.setString(6, dto.getContactEmail());
            ps.setString(7, firstNonBlank(dto.getSubmitReason(), dto.getReason()));
            return ps;
        }, kh);
        Long id = Objects.requireNonNull(kh.getKey()).longValue();
        return first("SELECT id, school_full_name AS schoolName, status, created_at AS createdAt FROM pf_school_onboarding_application WHERE id = ?", id);
    }

    @Transactional
    public Map<String, Object> auditOnboarding(Long id, AuditOnboardingDTO dto) {
        Map<String, Object> app = first("SELECT * FROM pf_school_onboarding_application WHERE id = ? AND deleted = 0 FOR UPDATE", id);
        if (app == null) throw new BusinessException(404, "入驻申请不存在");
        String from = Objects.toString(app.get("status"), "");
        if (!Set.of("pending", "resubmit_required").contains(from)) {
            throw new BusinessException(409, "当前状态不能审核");
        }
        String to = resolveAuditStatus(dto);
        Long tenantId = app.get("tenant_id") == null ? null : ((Number) app.get("tenant_id")).longValue();
        Map<String, Object> initialAdmin = null;
        if ("approved".equals(to)) {
            initialAdmin = approveOnboarding(app, dto.getPlanId());
            tenantId = ((Number) initialAdmin.get("tenantId")).longValue();
        }
        jdbc.update("UPDATE pf_school_onboarding_application SET status = ?, tenant_id = ?, review_comment = ?, updated_at = NOW() WHERE id = ?",
                to, tenantId, firstNonBlank(dto.getComment(), dto.getReason()), id);
        jdbc.update("""
                INSERT INTO pf_onboarding_audit_record
                (application_id, auditor_id, action, from_status, to_status, comment)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, currentUserId(), firstNonBlank(dto.getAction(), to), from, to, firstNonBlank(dto.getComment(), dto.getReason()));
        Map<String, Object> result = first("SELECT id, status, tenant_id AS tenantId, review_comment AS reviewComment FROM pf_school_onboarding_application WHERE id = ?", id);
        if (initialAdmin != null) {
            result.put("initialAdminUsername", initialAdmin.get("username"));
            result.put("temporaryPassword", initialAdmin.get("temporaryPassword"));
        }
        return result;
    }

    public PageResult<Map<String, Object>> plans(String keyword, String status, int pageNum, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE deleted = 0 ");
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            where.append(" AND status = ? ");
            args.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (plan_name LIKE ? OR plan_code LIKE ?) ");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        long total = count("SELECT COUNT(*) FROM pf_tenant_plan " + where, args);
        args.add(offset(pageNum, pageSize));
        args.add(pageSize);
        List<Map<String, Object>> records = jdbc.queryForList("""
                SELECT id, plan_code AS planCode, plan_name AS planName, COALESCE(description, '') AS description,
                       features_json AS features, price, status, created_at AS createdAt, updated_at AS updatedAt
                FROM pf_tenant_plan
                """ + where + " ORDER BY id LIMIT ?, ?", args.toArray()).stream().map(this::normalizePlan).toList();
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public Map<String, Object> createPlan(PlanDTO dto) {
        validatePlan(dto);
        String features = validateFeatures(dto);
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO pf_tenant_plan (plan_code, plan_name, description, features_json, price, status)
                    VALUES (?, ?, ?, CAST(? AS JSON), ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, dto.getPlanCode());
            ps.setString(2, dto.getPlanName());
            ps.setString(3, dto.getDescription());
            ps.setString(4, features);
            ps.setBigDecimal(5, dto.getPrice() == null ? BigDecimal.ZERO : dto.getPrice());
            ps.setString(6, dto.getStatus() == null ? "active" : dto.getStatus());
            return ps;
        }, kh);
        return planById(Objects.requireNonNull(kh.getKey()).longValue());
    }

    public Map<String, Object> updatePlan(Long id, PlanDTO dto) {
        validatePlan(dto);
        if (first("SELECT id FROM pf_tenant_plan WHERE id = ? AND deleted = 0", id) == null) {
            throw new BusinessException(404, "套餐不存在");
        }
        String features = validateFeatures(dto);
        jdbc.update("""
                UPDATE pf_tenant_plan SET plan_code = ?, plan_name = ?, description = ?,
                    features_json = CAST(? AS JSON), price = ?, status = ?, updated_at = NOW()
                WHERE id = ? AND deleted = 0
                """, dto.getPlanCode(), dto.getPlanName(), dto.getDescription(), features,
                dto.getPrice() == null ? BigDecimal.ZERO : dto.getPrice(),
                dto.getStatus() == null ? "active" : dto.getStatus(), id);
        return planById(id);
    }

    public void changePlanStatus(Long id, ChangeStatusDTO dto) {
        String status = requireStatus(dto.getStatus(), Set.of("active", "disabled"));
        if (jdbc.update("UPDATE pf_tenant_plan SET status = ?, updated_at = NOW() WHERE id = ? AND deleted = 0", status, id) == 0) {
            throw new BusinessException(404, "套餐不存在");
        }
    }

    public Map<String, Object> capability(Long id, String capability) {
        Map<String, Object> plan = planById(id);
        Map<String, Object> features = readJsonMap((String) plan.get("features"));
        Object value = features.get(capability);
        if (value == null) {
            return mapOf("capability", capability, "enabled", false, "status", "unavailable", "message", "未开放");
        }
        return mapOf("capability", capability, "enabled", true, "status", "available", "value", value);
    }

    public Map<String, Object> settings() {
        Map<String, Object> values = defaultSettings();
        jdbc.queryForList("SELECT setting_key AS settingKey, setting_value AS settingValue FROM pf_system_setting WHERE deleted = 0")
                .forEach(row -> values.put(String.valueOf(row.get("settingKey")), parseSettingValue(row.get("settingValue"))));
        return values;
    }

    @Transactional
    public Map<String, Object> saveSettings(Map<String, Object> payload) {
        if (payload == null) {
            payload = Map.of();
        }
        Map<String, Object> allowed = defaultSettings();
        for (String key : allowed.keySet()) {
            Object value = payload.containsKey(key) ? payload.get(key) : allowed.get(key);
            jdbc.update("""
                    INSERT INTO pf_system_setting (setting_key, setting_value, updated_by, updated_at, deleted)
                    VALUES (?, ?, ?, NOW(), 0)
                    ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), updated_by = VALUES(updated_by),
                                            updated_at = NOW(), deleted = 0
                    """, key, String.valueOf(value), currentUserId());
        }
        return settings();
    }

    public List<Map<String, Object>> permissions() {
        return jdbc.queryForList("""
                SELECT id, permission_code AS permissionCode, permission_name AS permissionName,
                       resource_action AS resourceAction, created_at AS createdAt
                FROM auth_permission
                WHERE deleted = 0 AND permission_code LIKE 'admin.%'
                ORDER BY permission_code
                """);
    }

    public List<Map<String, Object>> roles() {
        List<Map<String, Object>> roles = jdbc.queryForList("""
                SELECT id, tenant_id AS tenantId, role_code AS roleCode, role_name AS roleName,
                       scope_type AS scopeType, created_at AS createdAt
                FROM auth_role
                WHERE deleted = 0 AND tenant_id IS NULL AND scope_type = 'platform'
                ORDER BY id
                """);
        for (Map<String, Object> role : roles) {
            role.put("permissionKeys", jdbc.queryForList("""
                    SELECT p.permission_code FROM auth_permission p
                    JOIN auth_role_permission rp ON rp.permission_id = p.id AND rp.deleted = 0
                    WHERE rp.role_id = ? AND p.deleted = 0 AND p.permission_code LIKE 'admin.%'
                    ORDER BY p.permission_code
                    """, role.get("id")).stream().map(r -> r.get("permission_code")).toList());
        }
        return roles;
    }

    @Transactional
    public Map<String, Object> createRole(RoleDTO dto) {
        validateRole(dto);
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO auth_role (tenant_id, role_code, role_name, scope_type)
                    VALUES (NULL, ?, ?, 'platform')
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, dto.getRoleCode());
            ps.setString(2, dto.getRoleName());
            return ps;
        }, kh);
        Long id = Objects.requireNonNull(kh.getKey()).longValue();
        assignRolePermissions(id, dto);
        return first("SELECT id, role_code AS roleCode, role_name AS roleName, scope_type AS scopeType FROM auth_role WHERE id = ?", id);
    }

    @Transactional
    public Map<String, Object> updateRole(Long id, RoleDTO dto) {
        validateRole(dto);
        if (jdbc.update("UPDATE auth_role SET role_code = ?, role_name = ?, updated_at = NOW() WHERE id = ? AND tenant_id IS NULL AND scope_type = 'platform' AND deleted = 0",
                dto.getRoleCode(), dto.getRoleName(), id) == 0) {
            throw new BusinessException(404, "平台角色不存在");
        }
        assignRolePermissions(id, dto);
        return first("SELECT id, role_code AS roleCode, role_name AS roleName, scope_type AS scopeType FROM auth_role WHERE id = ?", id);
    }

    @Transactional
    public void deleteRole(Long id) {
        if (first("SELECT id FROM auth_role WHERE id = ? AND tenant_id IS NULL AND scope_type = 'platform' AND deleted = 0", id) == null) {
            throw new BusinessException(404, "平台角色不存在");
        }
        if ("system_admin".equals(first("SELECT role_code AS roleCode FROM auth_role WHERE id = ?", id).get("roleCode"))) {
            throw new BusinessException(400, "系统管理员内置角色不能删除");
        }
        if (count("SELECT COUNT(*) FROM auth_user_role WHERE role_id = ? AND deleted = 0", List.of(id)) > 0) {
            throw new BusinessException(409, "角色仍有账号使用，不能删除");
        }
        jdbc.update("UPDATE auth_role SET deleted = 1, updated_at = NOW() WHERE id = ?", id);
        jdbc.update("UPDATE auth_role_permission SET deleted = 1, updated_at = NOW() WHERE role_id = ? AND deleted = 0", id);
    }

    @Transactional
    public void assignRolePermissions(Long roleId, RoleDTO dto) {
        if (dto.getPermissionIds() == null && dto.getPermissionCodes() == null) {
            return;
        }
        List<Long> ids = dto.getPermissionIds();
        if ((ids == null || ids.isEmpty()) && dto.getPermissionCodes() != null && !dto.getPermissionCodes().isEmpty()) {
            ids = jdbc.queryForList("SELECT id FROM auth_permission WHERE permission_code IN (" + placeholders(dto.getPermissionCodes().size()) + ") AND permission_code LIKE 'admin.%' AND deleted = 0",
                    dto.getPermissionCodes().toArray()).stream().map(r -> ((Number) r.get("id")).longValue()).toList();
        }
        if (ids == null) ids = List.of();
        if (first("SELECT id FROM auth_role WHERE id = ? AND tenant_id IS NULL AND scope_type = 'platform' AND deleted = 0", roleId) == null) {
            throw new BusinessException(404, "平台角色不存在");
        }
        if (!ids.isEmpty()) {
            List<Object> permissionArgs = new ArrayList<>(ids);
            long validPermissionCount = count("SELECT COUNT(*) FROM auth_permission WHERE id IN (" + placeholders(ids.size()) + ") AND permission_code LIKE 'admin.%' AND deleted = 0", permissionArgs);
            if (validPermissionCount != ids.size()) {
                throw new BusinessException(400, "只能分配平台权限");
            }
        }
        jdbc.update("DELETE FROM auth_role_permission WHERE role_id = ?", roleId);
        for (Long pid : ids) {
            jdbc.update("INSERT INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted) VALUES (?, ?, NOW(), NOW(), 0)", roleId, pid);
        }
    }

    public PageResult<Map<String, Object>> adminUsers(String username, String realName, String status, int pageNum, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE pp.role_type = 'system_admin' AND pp.deleted = 0 AND au.deleted = 0 ");
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            where.append(" AND au.status = ? ");
            args.add(status);
        }
        if (username != null && !username.isBlank()) {
            where.append(" AND au.username LIKE ? ");
            args.add("%" + username + "%");
        }
        if (realName != null && !realName.isBlank()) {
            where.append(" AND pp.real_name LIKE ? ");
            args.add("%" + realName + "%");
        }
        long total = count("SELECT COUNT(*) FROM auth_person_profile pp JOIN auth_user_account au ON au.id = pp.user_id " + where, args);
        args.add(offset(pageNum, pageSize));
        args.add(pageSize);
        List<Map<String, Object>> records = jdbc.queryForList("""
                SELECT au.id, au.username, au.phone, au.email, au.status, au.last_login_at AS lastLoginAt,
                       au.created_at AS createdAt, pp.real_name AS realName,
                       (SELECT r.id FROM auth_user_role ur
                        JOIN auth_role r ON r.id = ur.role_id AND r.scope_type = 'platform' AND r.deleted = 0
                        WHERE ur.user_id = au.id AND ur.tenant_id IS NULL AND ur.deleted = 0
                        ORDER BY r.id LIMIT 1) AS roleId,
                       (SELECT r.role_name FROM auth_user_role ur
                        JOIN auth_role r ON r.id = ur.role_id AND r.scope_type = 'platform' AND r.deleted = 0
                        WHERE ur.user_id = au.id AND ur.tenant_id IS NULL AND ur.deleted = 0
                        ORDER BY r.id LIMIT 1) AS roleName
                FROM auth_person_profile pp
                JOIN auth_user_account au ON au.id = pp.user_id
                """ + where + " ORDER BY au.id LIMIT ?, ?", args.toArray());
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    @Transactional
    public Map<String, Object> createAdminUser(AdminUserDTO dto) {
        if (dto == null || dto.getUsername() == null || dto.getUsername().isBlank()
                || dto.getRealName() == null || dto.getRealName().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException(400, "用户名、姓名和初始密码不能为空");
        }
        String password = dto.getPassword();
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO auth_user_account (tenant_id, username, password_hash, phone, email, status, must_change_password)
                    VALUES (NULL, ?, ?, ?, ?, ?, 1)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, dto.getUsername());
            ps.setString(2, passwordEncoder.encode(password));
            ps.setString(3, dto.getPhone());
            ps.setString(4, dto.getEmail());
            ps.setString(5, dto.getStatus() == null ? "active" : dto.getStatus());
            return ps;
        }, kh);
        Long userId = Objects.requireNonNull(kh.getKey()).longValue();
        jdbc.update("INSERT INTO auth_person_profile (tenant_id, user_id, real_name, role_type) VALUES (NULL, ?, ?, 'system_admin')",
                userId, dto.getRealName());
        Long roleId = systemAdminRoleId();
        jdbc.update("INSERT INTO auth_user_role (tenant_id, user_id, role_id, created_at, updated_at, deleted) VALUES (NULL, ?, ?, NOW(), NOW(), 0)", userId, roleId);
        return first("SELECT id, username, status, created_at AS createdAt FROM auth_user_account WHERE id = ?", userId);
    }

    public Map<String, Object> updateAdminUser(Long id, AdminUserDTO dto) {
        jdbc.update("UPDATE auth_user_account SET phone = ?, email = ?, status = COALESCE(?, status), updated_at = NOW() WHERE id = ? AND tenant_id IS NULL AND deleted = 0",
                dto.getPhone(), dto.getEmail(), dto.getStatus(), id);
        jdbc.update("UPDATE auth_person_profile SET real_name = ?, updated_at = NOW() WHERE user_id = ? AND role_type = 'system_admin' AND deleted = 0",
                dto.getRealName(), id);
        return first("SELECT id, username, phone, email, status FROM auth_user_account WHERE id = ?", id);
    }

    public void changeAdminStatus(Long id, ChangeStatusDTO dto) {
        jdbc.update("UPDATE auth_user_account SET status = ?, updated_at = NOW() WHERE id = ? AND tenant_id IS NULL AND deleted = 0",
                requireStatus(dto.getStatus(), Set.of("active", "disabled", "locked")), id);
    }

    @Transactional
    public void assignAdminRoles(Long id, AdminUserDTO dto) {
        Map<String, Object> user = first("""
                SELECT au.id
                FROM auth_user_account au
                JOIN auth_person_profile pp ON pp.user_id = au.id AND pp.role_type = 'system_admin' AND pp.deleted = 0
                WHERE au.id = ? AND au.tenant_id IS NULL AND au.deleted = 0
                """, id);
        if (user == null) throw new BusinessException(404, "平台管理员不存在");
        List<Long> roleIds = dto.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            roleIds = List.of(systemAdminRoleId());
        }
        List<Object> roleArgs = new ArrayList<>(roleIds);
        long validCount = count("SELECT COUNT(*) FROM auth_role WHERE id IN (" + placeholders(roleIds.size()) + ") AND scope_type = 'platform' AND deleted = 0",
                roleArgs);
        if (validCount != roleIds.size()) {
            throw new BusinessException(400, "只能分配平台角色");
        }
        jdbc.update("DELETE FROM auth_user_role WHERE user_id = ? AND tenant_id IS NULL", id);
        for (Long roleId : roleIds) {
            jdbc.update("INSERT INTO auth_user_role (tenant_id, user_id, role_id, created_at, updated_at, deleted) VALUES (NULL, ?, ?, NOW(), NOW(), 0)",
                    id, roleId);
        }
    }

    public PageResult<Map<String, Object>> auditLogs(String operationType, String operatorName, String schoolName,
                                                     String startTime, String endTime, int pageNum, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE l.deleted = 0 ");
        List<Object> args = new ArrayList<>();
        if (operationType != null && !operationType.isBlank()) {
            where.append(" AND l.action = ? ");
            args.add(operationType);
        }
        if (operatorName != null && !operatorName.isBlank()) {
            where.append(" AND pp.real_name LIKE ? ");
            args.add("%" + operatorName + "%");
        }
        if (schoolName != null && !schoolName.isBlank()) {
            where.append(" AND t.school_name LIKE ? ");
            args.add("%" + schoolName + "%");
        }
        if (startTime != null && !startTime.isBlank()) {
            where.append(" AND l.created_at >= ? ");
            args.add(startTime + " 00:00:00");
        }
        if (endTime != null && !endTime.isBlank()) {
            where.append(" AND l.created_at < DATE_ADD(?, INTERVAL 1 DAY) ");
            args.add(endTime + " 00:00:00");
        }
        String joins = """
                FROM log_operation_log l
                LEFT JOIN auth_person_profile pp ON pp.user_id = l.user_id AND pp.deleted = 0
                LEFT JOIN pf_tenant t ON t.id = l.tenant_id AND t.deleted = 0
                """;
        long total = count("SELECT COUNT(*) " + joins + where, args);
        args.add(offset(pageNum, pageSize));
        args.add(pageSize);
        List<Map<String, Object>> records = jdbc.queryForList("""
                SELECT l.id, l.tenant_id AS tenantId, l.school_id AS schoolId, l.user_id AS userId,
                       pp.real_name AS operatorName, t.school_name AS schoolName,
                       l.module, l.action AS operationType, l.target_type AS targetType, l.target_id AS targetId,
                       l.target_name AS targetName, l.content, l.result, l.ip, l.device, l.created_at AS createdAt
                """ + joins + where + " ORDER BY l.created_at DESC LIMIT ?, ?", args.toArray());
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public Map<String, Object> health() {
        String db = "up";
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
        } catch (Exception e) {
            db = "down";
        }
        return mapOf(
                "database", mapOf("status", db),
                "redis", mapOf("status", "unopened", "message", "未开放"),
                "fileStorage", mapOf("status", "unopened", "message", "未开放"),
                "skyWalking", mapOf("status", "unopened", "message", "未开放"),
                "elasticsearch", mapOf("status", "unopened", "message", "未开放")
        );
    }

    public Map<String, Object> reportSummary() {
        long tenants = count("SELECT COUNT(*) FROM pf_tenant WHERE deleted = 0", List.of());
        long active = count("SELECT COUNT(*) FROM pf_tenant WHERE deleted = 0 AND status = 'active'", List.of());
        long users = count("SELECT COUNT(*) FROM auth_user_account WHERE deleted = 0", List.of());
        long pending = count("SELECT COUNT(*) FROM pf_school_onboarding_application WHERE deleted = 0 AND status = 'pending'", List.of());
        return mapOf("tenantCount", tenants, "activeSchoolCount", active, "userCount", users, "pendingOnboardingCount", pending);
    }

    private Map<String, Object> approveOnboarding(Map<String, Object> app, Long requestedPlanId) {
        String schoolName = Objects.toString(app.get("school_full_name"), null);
        Long planId = requestedPlanId != null ? requestedPlanId : app.get("plan_id") == null ? null : ((Number) app.get("plan_id")).longValue();
        if (planId == null || first("SELECT id FROM pf_tenant_plan WHERE id = ? AND status = 'active' AND deleted = 0", planId) == null) {
            throw new BusinessException(400, "请选择有效套餐");
        }
        KeyHolder tenantKh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO pf_tenant (tenant_code, plan_id, school_name, status)
                    VALUES (?, ?, ?, 'active')
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "tenant_" + UUID.randomUUID().toString().replace("-", ""));
            ps.setObject(2, planId);
            ps.setString(3, schoolName);
            return ps;
        }, tenantKh);
        Long tenantId = Objects.requireNonNull(tenantKh.getKey()).longValue();
        KeyHolder schoolKh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO sch_school_profile (tenant_id, name, status) VALUES (?, ?, 'active')", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tenantId);
            ps.setString(2, schoolName);
            return ps;
        }, schoolKh);
        String username = "school_admin_" + tenantId;
        String temporaryPassword = generateTemporaryPassword();
        KeyHolder userKh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO auth_user_account (tenant_id, username, password_hash, phone, email, status, must_change_password)
                    VALUES (?, ?, ?, ?, ?, 'active', 1)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tenantId);
            ps.setString(2, username);
            ps.setString(3, passwordEncoder.encode(temporaryPassword));
            ps.setString(4, Objects.toString(app.get("contact_phone"), null));
            ps.setString(5, Objects.toString(app.get("contact_email"), null));
            return ps;
        }, userKh);
        Long userId = Objects.requireNonNull(userKh.getKey()).longValue();
        jdbc.update("INSERT INTO auth_person_profile (tenant_id, user_id, real_name, role_type, department_name) VALUES (?, ?, ?, 'school_admin', '校级管理')",
                tenantId, userId, Objects.toString(app.get("contact_name"), "学校管理员"));
        Long roleId = ensureSchoolAdminRole(tenantId);
        jdbc.update("INSERT INTO auth_user_role (tenant_id, user_id, role_id, scope_json, created_at, updated_at, deleted) VALUES (?, ?, ?, JSON_OBJECT('schoolIds', JSON_ARRAY(?)), NOW(), NOW(), 0)",
                tenantId, userId, roleId, schoolKh.getKey());
        return mapOf("tenantId", tenantId, "username", username, "temporaryPassword", temporaryPassword);
    }

    private String generateTemporaryPassword() {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Long ensureSchoolAdminRole(Long tenantId) {
        Map<String, Object> existing = first("SELECT id FROM auth_role WHERE tenant_id = ? AND role_code = 'school_admin' AND deleted = 0", tenantId);
        if (existing != null) return ((Number) existing.get("id")).longValue();
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO auth_role (tenant_id, role_code, role_name, scope_type) VALUES (?, 'school_admin', '学校管理员', 'school')", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tenantId);
            return ps;
        }, kh);
        Long roleId = Objects.requireNonNull(kh.getKey()).longValue();
        jdbc.update("""
                INSERT INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
                SELECT ?, id, NOW(), NOW(), 0 FROM auth_permission WHERE permission_code LIKE 'school.%' AND deleted = 0
                """, roleId);
        return roleId;
    }

    private Long systemAdminRoleId() {
        Map<String, Object> existing = first("SELECT id FROM auth_role WHERE tenant_id IS NULL AND role_code = 'system_admin' AND deleted = 0");
        if (existing != null) return ((Number) existing.get("id")).longValue();
        jdbc.update("INSERT INTO auth_role (tenant_id, role_code, role_name, scope_type) VALUES (NULL, 'system_admin', '系统管理员', 'platform')");
        return ((Number) first("SELECT id FROM auth_role WHERE tenant_id IS NULL AND role_code = 'system_admin'").get("id")).longValue();
    }

    private Map<String, Object> planById(Long id) {
        Map<String, Object> plan = first("""
                SELECT id, plan_code AS planCode, plan_name AS planName, COALESCE(description, '') AS description,
                       features_json AS features, price, status, created_at AS createdAt, updated_at AS updatedAt
                FROM pf_tenant_plan WHERE id = ? AND deleted = 0
                """, id);
        if (plan == null) throw new BusinessException(404, "套餐不存在");
        return normalizePlan(plan);
    }

    private Map<String, Object> normalizePlan(Map<String, Object> row) {
        Map<String, Object> map = new LinkedHashMap<>(row);
        String features = Objects.toString(map.get("features"), "{}");
        Map<String, Object> json = readJsonMap(features);
        map.put("featuresJson", features);
        map.put("featureMap", json);
        map.put("maxUsers", json.getOrDefault("maxUsers", 0));
        map.put("maxForms", json.getOrDefault("maxForms", 0));
        map.put("storageQuota", json.getOrDefault("storageQuota", "未开放"));
        map.put("priceText", priceText(map.get("price")));
        return map;
    }

    private Map<String, Object> normalizeTenant(Map<String, Object> row) {
        Map<String, Object> map = new LinkedHashMap<>(row);
        Object id = map.get("tenantId");
        map.put("tenantId", id == null ? map.get("id") : String.valueOf(id));
        return map;
    }

    private Map<String, Object> extractQuota(String features) {
        Map<String, Object> json = readJsonMap(features);
        return mapOf(
                "maxUsers", json.getOrDefault("maxUsers", 0),
                "maxForms", json.getOrDefault("maxForms", 0),
                "storageQuota", json.getOrDefault("storageQuota", "未开放"),
                "concurrencyQuota", json.getOrDefault("concurrencyQuota", "未开放")
        );
    }

    private String validateFeatures(PlanDTO dto) {
        try {
            String json = dto.getFeaturesJson();
            if ((json == null || json.isBlank()) && dto.getFeatures() != null) {
                json = objectMapper.writeValueAsString(dto.getFeatures());
            }
            if (json == null || json.isBlank()) json = "{}";
            if (!objectMapper.readTree(json).isObject()) {
                throw new IllegalArgumentException("features must be a JSON object");
            }
            return json;
        } catch (Exception e) {
            throw new BusinessException(400, "套餐功能 JSON 不合法");
        }
    }

    private void validatePlan(PlanDTO dto) {
        if (dto == null || dto.getPlanCode() == null || dto.getPlanCode().isBlank()
                || dto.getPlanName() == null || dto.getPlanName().isBlank()) {
            throw new BusinessException(400, "套餐编码和套餐名称不能为空");
        }
        if (dto.getStatus() != null && !Set.of("active", "disabled").contains(dto.getStatus())) {
            throw new BusinessException(400, "套餐状态不合法");
        }
        if (dto.getPrice() != null && dto.getPrice().signum() < 0) {
            throw new BusinessException(400, "套餐价格不能为负数");
        }
    }

    private void validateRole(RoleDTO dto) {
        if (dto == null || dto.getRoleCode() == null || dto.getRoleCode().isBlank()
                || dto.getRoleName() == null || dto.getRoleName().isBlank()) {
            throw new BusinessException(400, "角色编码和角色名称不能为空");
        }
        if (!dto.getRoleCode().matches("[A-Za-z][A-Za-z0-9_.-]{1,63}")) {
            throw new BusinessException(400, "角色编码格式不合法");
        }
    }

    private Map<String, Object> readJsonMap(String json) {
        try {
            if (json == null || json.isBlank()) return new LinkedHashMap<>();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> defaultSettings() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("onboardingNotificationTemplate", "您的学校入驻申请已提交，请等待平台审核。");
        defaults.put("expirationWarningTemplate", "您的套餐即将到期，请及时联系平台管理员续费。");
        defaults.put("sensitiveWords", "");
        defaults.put("extremeLowScoreThreshold", 2);
        defaults.put("manualReviewEnabled", true);
        defaults.put("maxFileSize", 50);
        defaults.put("attachmentExpiryDays", 180);
        return defaults;
    }

    private Object parseSettingValue(Object raw) {
        String value = raw == null ? "" : String.valueOf(raw);
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    private String resolveAuditStatus(AuditOnboardingDTO dto) {
        String action = firstNonBlank(dto.getStatus(), dto.getAction());
        if (action == null) throw new BusinessException(400, "审核动作不能为空");
        return switch (action) {
            case "approve", "approved" -> "approved";
            case "reject", "rejected" -> "rejected";
            case "resubmit", "resubmit_required" -> "resubmit_required";
            default -> throw new BusinessException(400, "不支持的审核动作");
        };
    }

    private String requireStatus(String status, Set<String> allowed) {
        if (status == null || !allowed.contains(status)) throw new BusinessException(400, "状态不合法");
        return status;
    }

    private Map<String, Object> first(String sql, Object... args) {
        try {
            return jdbc.queryForMap(sql, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private long count(String sql, List<Object> args) {
        Long value = jdbc.queryForObject(sql, Long.class, args.toArray());
        return value == null ? 0L : value;
    }

    private int offset(int pageNum, int pageSize) {
        return Math.max(pageNum - 1, 0) * pageSize;
    }

    private Long currentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private String placeholders(int size) {
        return String.join(",", Collections.nCopies(size, "?"));
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return null;
    }

    private String priceText(Object price) {
        BigDecimal value = price instanceof BigDecimal bd ? bd : price == null ? BigDecimal.ZERO : new BigDecimal(price.toString());
        return value.compareTo(BigDecimal.ZERO) == 0 ? "免费" : "¥" + value.stripTrailingZeros().toPlainString() + "/年";
    }

    private Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }
}
