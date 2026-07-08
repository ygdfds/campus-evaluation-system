package com.campus.evaluation.auth.controller;

import com.campus.evaluation.auth.domain.dto.AdminUserCreateDTO;
import com.campus.evaluation.auth.domain.dto.AdminUserUpdateDTO;
import com.campus.evaluation.auth.domain.dto.ChangeUserStatusDTO;
import com.campus.evaluation.auth.domain.dto.ResetPasswordDTO;
import com.campus.evaluation.auth.domain.vo.AdminUserVO;
import com.campus.evaluation.auth.domain.vo.UserDetailVO;
import com.campus.evaluation.auth.service.SchoolAdminUserService;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员管理", description = "学校端管理员账号管理")
@RestController
@RequestMapping("/school/admin-users")
@RequiredArgsConstructor
public class SchoolAdminUserController {

    private final SchoolAdminUserService adminUserService;

    @Operation(summary = "查询管理员列表")
    @GetMapping
    public R<PageResult<AdminUserVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(adminUserService.list(keyword, status, pageNum, pageSize));
    }

    @Operation(summary = "查看管理员详情")
    @GetMapping("/{id}")
    public R<UserDetailVO> getById(@PathVariable Long id) {
        return R.ok(adminUserService.getById(id));
    }

    @Operation(summary = "新增子管理员")
    @OperationLog(module = "auth", value = "新增管理员", type = "CREATE")
    @PostMapping
    public R<AdminUserVO> create(@Valid @RequestBody AdminUserCreateDTO dto) {
        return R.ok(adminUserService.create(dto));
    }

    @Operation(summary = "编辑管理员资料")
    @OperationLog(module = "auth", value = "编辑管理员", type = "UPDATE")
    @PutMapping("/{id}")
    public R<AdminUserVO> update(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateDTO dto) {
        return R.ok(adminUserService.update(id, dto));
    }

    @Operation(summary = "启用/停用管理员")
    @OperationLog(module = "auth", value = "管理员启停", type = "UPDATE")
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeUserStatusDTO dto) {
        adminUserService.changeStatus(id, dto);
        return R.ok();
    }

    @Operation(summary = "重置管理员密码")
    @OperationLog(module = "auth", value = "重置管理员密码", type = "UPDATE")
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) ResetPasswordDTO dto) {
        adminUserService.resetPassword(id, dto != null ? dto : new ResetPasswordDTO());
        return R.ok();
    }
}
