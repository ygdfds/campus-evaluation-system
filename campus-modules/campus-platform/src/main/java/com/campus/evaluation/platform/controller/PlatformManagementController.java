package com.campus.evaluation.platform.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.platform.domain.dto.PlatformDTOs.*;
import com.campus.evaluation.platform.service.PlatformManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PlatformManagementController {

    private final PlatformManagementService service;

    @GetMapping("/platform/public/plans")
    public R<List<Map<String, Object>>> publicPlans() {
        return R.ok(service.activePlans());
    }

    @PostMapping("/platform/public/onboarding/applications")
    public R<Map<String, Object>> submitOnboarding(@RequestBody OnboardingSubmitDTO dto) {
        return R.ok(service.submitOnboarding(dto));
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/tenants")
    public R<PageResult<Map<String, Object>>> tenants(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer pageNum,
                                                      @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(service.tenants(keyword, status, page != null ? page : (pageNum != null ? pageNum : 1), pageSize));
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/tenants/{id}")
    public R<Map<String, Object>> tenantDetail(@PathVariable Long id) {
        return R.ok(service.tenantDetail(id));
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/tenants/{id}/status")
    @OperationLog(module = "platform", value = "租户状态变更", type = "UPDATE")
    public R<Void> tenantStatus(@PathVariable Long id, @RequestBody ChangeStatusDTO dto) {
        service.changeTenantStatus(id, dto);
        return R.ok();
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/tenants/{id}/plan")
    @OperationLog(module = "platform", value = "租户套餐绑定", type = "UPDATE")
    public R<Void> tenantPlan(@PathVariable Long id, @RequestBody BindPlanDTO dto) {
        service.bindTenantPlan(id, dto);
        return R.ok();
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/onboarding/applications")
    public R<PageResult<Map<String, Object>>> onboarding(@RequestParam(required = false) String status,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer pageNum,
                                                         @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(service.onboardingApplications(status, keyword, page != null ? page : (pageNum != null ? pageNum : 1), pageSize));
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/onboarding/applications/{id}/materials")
    public R<List<Map<String, Object>>> onboardingMaterials(@PathVariable Long id) {
        return R.ok(service.onboardingMaterials(id));
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/onboarding/applications/{id}/audit")
    @OperationLog(module = "platform", value = "入驻审核", type = "UPDATE")
    public R<Map<String, Object>> auditOnboarding(@PathVariable Long id, @RequestBody AuditOnboardingDTO dto) {
        return R.ok(service.auditOnboarding(id, dto));
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/plans")
    public R<PageResult<Map<String, Object>>> plans(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer pageNum,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(service.plans(keyword, status, page != null ? page : (pageNum != null ? pageNum : 1), pageSize));
    }

    @SaCheckRole("system_admin")
    @PostMapping("/platform/plans")
    public R<Map<String, Object>> createPlan(@RequestBody PlanDTO dto) {
        return R.ok(service.createPlan(dto));
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/plans/{id}")
    public R<Map<String, Object>> updatePlan(@PathVariable Long id, @RequestBody PlanDTO dto) {
        return R.ok(service.updatePlan(id, dto));
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/plans/{id}/status")
    public R<Void> planStatus(@PathVariable Long id, @RequestBody ChangeStatusDTO dto) {
        service.changePlanStatus(id, dto);
        return R.ok();
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/plans/{id}/capabilities/{capability}")
    public R<Map<String, Object>> capability(@PathVariable Long id, @PathVariable String capability) {
        return R.ok(service.capability(id, capability));
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/permissions")
    public R<List<Map<String, Object>>> permissions() {
        return R.ok(service.permissions());
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/roles")
    public R<List<Map<String, Object>>> roles() {
        return R.ok(service.roles());
    }

    @SaCheckRole("system_admin")
    @PostMapping("/platform/roles")
    public R<Map<String, Object>> createRole(@RequestBody RoleDTO dto) {
        return R.ok(service.createRole(dto));
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/roles/{id}")
    public R<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody RoleDTO dto) {
        return R.ok(service.updateRole(id, dto));
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/roles/{id}/permissions")
    public R<Void> assignPermissions(@PathVariable Long id, @RequestBody RoleDTO dto) {
        service.assignRolePermissions(id, dto);
        return R.ok();
    }

    @SaCheckRole("system_admin")
    @DeleteMapping("/platform/roles/{id}")
    public R<Void> deleteRole(@PathVariable Long id) {
        service.deleteRole(id);
        return R.ok();
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/admin-users")
    public R<PageResult<Map<String, Object>>> adminUsers(@RequestParam(required = false) String username,
                                                         @RequestParam(required = false) String realName,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer pageNum,
                                                         @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(service.adminUsers(username, realName, status, page != null ? page : (pageNum != null ? pageNum : 1), pageSize));
    }

    @SaCheckRole("system_admin")
    @PostMapping("/platform/admin-users")
    public R<Map<String, Object>> createAdminUser(@RequestBody AdminUserDTO dto) {
        return R.ok(service.createAdminUser(dto));
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/admin-users/{id}")
    public R<Map<String, Object>> updateAdminUser(@PathVariable Long id, @RequestBody AdminUserDTO dto) {
        return R.ok(service.updateAdminUser(id, dto));
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/admin-users/{id}/status")
    public R<Void> adminStatus(@PathVariable Long id, @RequestBody ChangeStatusDTO dto) {
        service.changeAdminStatus(id, dto);
        return R.ok();
    }

    @SaCheckRole("system_admin")
    @PutMapping("/platform/admin-users/{id}/roles")
    public R<Void> adminRoles(@PathVariable Long id, @RequestBody AdminUserDTO dto) {
        service.assignAdminRoles(id, dto);
        return R.ok();
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/audit/logs")
    public R<PageResult<Map<String, Object>>> auditLogs(@RequestParam(required = false) String operationType,
                                                        @RequestParam(required = false) String operatorName,
                                                        @RequestParam(required = false) String schoolName,
                                                        @RequestParam(required = false) String startTime,
                                                        @RequestParam(required = false) String endTime,
                                                        @RequestParam(required = false) Integer page,
                                                        @RequestParam(required = false) Integer pageNum,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(service.auditLogs(operationType, operatorName, schoolName, startTime, endTime,
                page != null ? page : (pageNum != null ? pageNum : 1), pageSize));
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/monitoring/health")
    public R<Map<String, Object>> health() {
        return R.ok(service.health());
    }

    @SaCheckRole("system_admin")
    @GetMapping("/platform/reports/summary")
    public R<Map<String, Object>> reportSummary() {
        return R.ok(service.reportSummary());
    }
}
