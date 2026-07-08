package com.campus.evaluation.auth.controller;

import com.campus.evaluation.auth.domain.dto.AssignRolesDTO;
import com.campus.evaluation.auth.domain.dto.ChangeUserStatusDTO;
import com.campus.evaluation.auth.domain.dto.ResetPasswordDTO;
import com.campus.evaluation.auth.domain.dto.StaffUserCreateDTO;
import com.campus.evaluation.auth.domain.dto.StaffUserUpdateDTO;
import com.campus.evaluation.auth.domain.vo.StaffUserVO;
import com.campus.evaluation.auth.domain.vo.UserDetailVO;
import com.campus.evaluation.auth.service.SchoolStaffUserService;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教职工管理", description = "学校端教职工账号管理")
@RestController
@RequestMapping("/school/staff-users")
@RequiredArgsConstructor
public class SchoolStaffUserController {

    private final SchoolStaffUserService staffUserService;

    @Operation(summary = "查询教职工列表")
    @GetMapping
    public R<PageResult<StaffUserVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teachingOrgId,
            @RequestParam(required = false) Long serviceOrgId,
            @RequestParam(required = false) String roleCode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(staffUserService.list(keyword, status, teachingOrgId, serviceOrgId, roleCode, pageNum, pageSize));
    }

    @Operation(summary = "查看教职工详情")
    @GetMapping("/{id}")
    public R<UserDetailVO> getById(@PathVariable Long id) {
        return R.ok(staffUserService.getById(id));
    }

    @Operation(summary = "新增教职工")
    @OperationLog(module = "auth", value = "新增教职工", type = "CREATE")
    @PostMapping
    public R<StaffUserVO> create(@Valid @RequestBody StaffUserCreateDTO dto) {
        return R.ok(staffUserService.create(dto));
    }

    @Operation(summary = "编辑教职工资料")
    @OperationLog(module = "auth", value = "编辑教职工", type = "UPDATE")
    @PutMapping("/{id}")
    public R<StaffUserVO> update(@PathVariable Long id, @Valid @RequestBody StaffUserUpdateDTO dto) {
        return R.ok(staffUserService.update(id, dto));
    }

    @Operation(summary = "调整职工角色")
    @OperationLog(module = "auth", value = "调整职工角色", type = "UPDATE")
    @PutMapping("/{id}/roles")
    public R<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRolesDTO dto) {
        staffUserService.assignRoles(id, dto);
        return R.ok();
    }

    @Operation(summary = "启用/停用教职工")
    @OperationLog(module = "auth", value = "教职工启停", type = "UPDATE")
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeUserStatusDTO dto) {
        staffUserService.changeStatus(id, dto);
        return R.ok();
    }

    @Operation(summary = "重置教职工密码")
    @OperationLog(module = "auth", value = "重置教职工密码", type = "UPDATE")
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) ResetPasswordDTO dto) {
        staffUserService.resetPassword(id, dto != null ? dto : new ResetPasswordDTO());
        return R.ok();
    }
}
