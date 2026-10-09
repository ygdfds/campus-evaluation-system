package com.campus.evaluation.auth.controller;

import com.campus.evaluation.auth.domain.dto.ChangePasswordDTO;
import com.campus.evaluation.auth.domain.dto.LoginRequest;
import com.campus.evaluation.auth.domain.vo.CurrentUserVO;
import com.campus.evaluation.auth.domain.vo.LoginResponse;
import com.campus.evaluation.auth.domain.vo.PermissionVO;
import com.campus.evaluation.auth.service.AuthService;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.core.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Tag(name = "Auth", description = "Authentication and authorization")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Operation(summary = "Login")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return R.ok(authService.login(request, httpRequest));
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok(null, "logout success");
    }

    @Operation(summary = "Current user")
    @GetMapping("/me")
    public R<CurrentUserVO> me() {
        return R.ok(authService.getCurrentUser());
    }

    @PutMapping("/me")
    public R<Map<String, Object>> updateMe(@RequestBody Map<String, Object> payload) {
        return R.ok(authService.updateCurrentUser(payload));
    }

    @GetMapping("/login-logs")
    public R<List<Map<String, Object>>> loginLogs() {
        return R.ok(authService.getLoginLogs());
    }

    @Operation(summary = "Current permissions")
    @GetMapping("/permissions")
    public R<PermissionVO> permissions() {
        return R.ok(authService.getPermissions());
    }

    @Operation(summary = "Authorized frontend route names")
    @GetMapping("/routes")
    public R<Map<String, Object>> routes() {
        CurrentUserVO currentUser = authService.getCurrentUser();
        Map<String, List<String>> routeMap = Map.of(
                "system_admin", List.of("AdminDashboard", "AdminTenantList", "AdminOnboardingAudit", "AdminRoleManagement"),
                "school_admin", List.of("SchoolDashboard", "SchoolInfo", "SchoolDeptList", "SchoolStaffList", "SchoolStudentList", "SchoolAdminList"),
                "staff", List.of("StaffDashboard", "StaffEvalForms", "StaffFeedback", "StaffReports"),
                "student", List.of("StudentDashboard", "StudentEvaluationTasks", "StudentEvalHistory", "StudentComplaint")
        );
        Map<String, Object> payload = new HashMap<>();
        payload.put("roleType", currentUser.getRoleType());
        payload.put("routes", routeMap.getOrDefault(currentUser.getRoleType(), Collections.emptyList()));
        return R.ok(payload);
    }

    @Operation(summary = "Captcha capability")
    @GetMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        return R.ok(Map.of(
                "enabled", false,
                "required", false,
                "reason", "captcha service is not enabled in phase A"
        ));
    }

    @Operation(summary = "Public active school list")
    @GetMapping("/schools")
    public R<List<Map<String, Object>>> schools() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT sp.id, sp.tenant_id AS tenantId, sp.name AS schoolName,
                       sp.name AS name, t.school_name AS tenantSchoolName
                FROM sch_school_profile sp
                JOIN pf_tenant t ON t.id = sp.tenant_id AND t.deleted = 0 AND t.status = 'active'
                WHERE sp.deleted = 0 AND sp.status = 'active'
                ORDER BY sp.id
                """);
        return R.ok(rows);
    }

    @Operation(summary = "Public student or staff identity verification")
    @PostMapping("/verify-identity")
    public R<Map<String, Object>> verifyIdentity(@RequestBody Map<String, Object> payload) {
        Long schoolId = asLong(payload.get("schoolId"));
        String no = asString(payload.get("studentNo"));
        String realName = asString(payload.get("realName"));
        if (schoolId == null || no == null || no.isBlank() || realName == null || realName.isBlank()) {
            throw new BusinessException(400, "学校、学号/工号和真实姓名不能为空");
        }
        Map<String, Object> school = first("SELECT id, tenant_id AS tenantId, name FROM sch_school_profile WHERE id = ? AND deleted = 0 AND status = 'active'", schoolId);
        if (school == null) {
            throw new BusinessException(404, "学校不存在或未启用");
        }
        Long tenantId = ((Number) school.get("tenantId")).longValue();
        Map<String, Object> profile = first("""
                SELECT id, tenant_id AS tenantId, user_id AS userId, real_name AS realName,
                       role_type AS role, no_student AS studentNo, no_work AS workNo,
                       department_name AS department, class_name AS className
                FROM auth_person_profile
                WHERE tenant_id = ? AND real_name = ? AND deleted = 0
                  AND ((role_type = 'student' AND no_student = ?) OR (role_type = 'staff' AND no_work = ?))
                LIMIT 1
                """, tenantId, realName.trim(), no.trim(), no.trim());
        if (profile == null) {
            throw new BusinessException(404, "未找到匹配的学生或教职工档案，请联系学校管理员");
        }
        profile.put("schoolId", schoolId);
        profile.put("schoolName", school.get("name"));
        profile.put("registered", profile.get("userId") != null);
        return R.ok(profile);
    }

    @Operation(summary = "Public user registration")
    @PostMapping("/register")
    public R<Map<String, Object>> register(@RequestBody Map<String, Object> payload) {
        Long schoolId = asLong(payload.get("schoolId"));
        String username = asString(payload.get("username"));
        String password = asString(payload.get("password"));
        String realName = asString(payload.get("realName"));
        String role = asString(payload.get("role"));
        String studentNo = asString(payload.get("studentNo"));
        String workNo = asString(payload.get("workNo"));
        String no = "student".equals(role) ? studentNo : workNo;
        if (schoolId == null || isBlank(username) || isBlank(password) || isBlank(realName) || isBlank(role) || isBlank(no)) {
            throw new BusinessException(400, "注册信息不完整");
        }
        Map<String, Object> school = first("SELECT id, tenant_id AS tenantId FROM sch_school_profile WHERE id = ? AND deleted = 0 AND status = 'active'", schoolId);
        if (school == null) {
            throw new BusinessException(404, "学校不存在或未启用");
        }
        Long tenantId = ((Number) school.get("tenantId")).longValue();
        Map<String, Object> profile = first("""
                SELECT id, user_id AS userId FROM auth_person_profile
                WHERE tenant_id = ? AND real_name = ? AND role_type = ? AND deleted = 0
                  AND ((role_type = 'student' AND no_student = ?) OR (role_type = 'staff' AND no_work = ?))
                LIMIT 1
                """, tenantId, realName.trim(), role.trim(), no.trim(), no.trim());
        if (profile == null) {
            throw new BusinessException(404, "身份未通过学校档案校验");
        }
        if (profile.get("userId") != null) {
            throw new BusinessException(409, "该身份已绑定账号，请直接登录或联系管理员重置密码");
        }
        if (first("SELECT id FROM auth_user_account WHERE username = ? AND deleted = 0", username.trim()) != null) {
            throw new BusinessException(409, "用户名已存在");
        }
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO auth_user_account
                    (tenant_id, username, password_hash, phone, email, status, must_change_password, created_at, updated_at, deleted)
                    VALUES (?, ?, ?, ?, ?, 'active', 0, ?, ?, 0)
                    """, Statement.RETURN_GENERATED_KEYS);
            LocalDateTime now = LocalDateTime.now();
            ps.setLong(1, tenantId);
            ps.setString(2, username.trim());
            ps.setString(3, passwordEncoder.encode(password));
            ps.setString(4, asString(payload.get("phone")));
            ps.setString(5, asString(payload.get("email")));
            ps.setObject(6, now);
            ps.setObject(7, now);
            return ps;
        }, kh);
        Long userId = Objects.requireNonNull(kh.getKey()).longValue();
        jdbc.update("UPDATE auth_person_profile SET user_id = ?, updated_at = NOW() WHERE id = ?", userId, profile.get("id"));
        Map<String, Object> roleRow = first("SELECT id FROM auth_role WHERE tenant_id = ? AND role_code = ? AND deleted = 0 LIMIT 1", tenantId, role);
        if (roleRow != null) {
            jdbc.update("INSERT INTO auth_user_role (tenant_id, user_id, role_id, created_at, updated_at, deleted) VALUES (?, ?, ?, NOW(), NOW(), 0)",
                    tenantId, userId, roleRow.get("id"));
        }
        return R.ok(Map.of("userId", userId, "username", username.trim()), "注册成功");
    }

    @Operation(summary = "Public forgot password request")
    @PostMapping("/forgot-password")
    public R<Map<String, Object>> forgotPassword(@RequestBody Map<String, Object> payload) {
        String username = asString(payload.get("username"));
        String realName = asString(payload.get("realName"));
        String identityNo = asString(payload.get("identityNo"));
        if (isBlank(username)) {
            throw new BusinessException(400, "Username is required");
        }
        Map<String, Object> account = first("""
                SELECT au.id, au.username, au.tenant_id AS tenantId, pp.real_name AS realName,
                       pp.role_type AS roleType, pp.no_student AS studentNo, pp.no_work AS workNo,
                       sp.name AS schoolName
                FROM auth_user_account au
                LEFT JOIN auth_person_profile pp ON pp.user_id = au.id AND pp.deleted = 0
                LEFT JOIN sch_school_profile sp ON sp.tenant_id = au.tenant_id AND sp.deleted = 0
                WHERE au.username = ? AND au.deleted = 0
                LIMIT 1
                """, username.trim());
        boolean matched = account != null
                && (isBlank(realName) || realName.equals(account.get("realName")))
                && (isBlank(identityNo)
                    || identityNo.equals(account.get("studentNo"))
                    || identityNo.equals(account.get("workNo")));
        String schoolName = matched && account.get("schoolName") != null
                ? String.valueOf(account.get("schoolName"))
                : "your school";
        return R.ok(Map.of(
                "accepted", true,
                "matched", matched,
                "schoolName", schoolName,
                "message", "Password reset request accepted. Please contact " + schoolName + " administrator to verify your identity and reset the password."
        ));
    }

    @Operation(summary = "Change password")
    @PostMapping("/change-password")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        authService.changePassword(request);
        return R.ok(null, "password changed");
    }

    private Map<String, Object> first(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Long asLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return value instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
