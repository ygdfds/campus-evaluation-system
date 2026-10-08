package com.campus.evaluation.auth.controller;

import com.campus.evaluation.auth.domain.dto.ChangePasswordDTO;
import com.campus.evaluation.auth.domain.dto.LoginRequest;
import com.campus.evaluation.auth.domain.vo.CurrentUserVO;
import com.campus.evaluation.auth.domain.vo.LoginResponse;
import com.campus.evaluation.auth.domain.vo.PermissionVO;
import com.campus.evaluation.auth.service.AuthService;
import com.campus.evaluation.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Auth", description = "Authentication and authorization")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

    @Operation(summary = "Change password")
    @PostMapping("/change-password")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        authService.changePassword(request);
        return R.ok(null, "password changed");
    }
}
