package com.campus.evaluation.auth.controller;

import com.campus.evaluation.auth.domain.dto.ChangeUserStatusDTO;
import com.campus.evaluation.auth.domain.dto.ResetPasswordDTO;
import com.campus.evaluation.auth.domain.dto.StudentUserCreateDTO;
import com.campus.evaluation.auth.domain.dto.StudentUserUpdateDTO;
import com.campus.evaluation.auth.domain.vo.StudentUserVO;
import com.campus.evaluation.auth.domain.vo.UserDetailVO;
import com.campus.evaluation.auth.service.SchoolStudentUserService;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生管理", description = "学校端学生账号管理")
@RestController
@RequestMapping("/school/student-users")
@RequiredArgsConstructor
public class SchoolStudentUserController {

    private final SchoolStudentUserService studentUserService;

    @Operation(summary = "查询学生列表")
    @GetMapping
    public R<PageResult<StudentUserVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String grade,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(studentUserService.list(keyword, status, classId, grade, pageNum, pageSize));
    }

    @Operation(summary = "查看学生详情")
    @GetMapping("/{id}")
    public R<UserDetailVO> getById(@PathVariable Long id) {
        return R.ok(studentUserService.getById(id));
    }

    @Operation(summary = "新增学生")
    @OperationLog(module = "auth", value = "新增学生", type = "CREATE")
    @PostMapping
    public R<StudentUserVO> create(@Valid @RequestBody StudentUserCreateDTO dto) {
        return R.ok(studentUserService.create(dto));
    }

    @Operation(summary = "编辑学生资料")
    @OperationLog(module = "auth", value = "编辑学生", type = "UPDATE")
    @PutMapping("/{id}")
    public R<StudentUserVO> update(@PathVariable Long id, @Valid @RequestBody StudentUserUpdateDTO dto) {
        return R.ok(studentUserService.update(id, dto));
    }

    @Operation(summary = "启用/停用学生")
    @OperationLog(module = "auth", value = "学生启停", type = "UPDATE")
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeUserStatusDTO dto) {
        studentUserService.changeStatus(id, dto);
        return R.ok();
    }

    @Operation(summary = "重置学生密码")
    @OperationLog(module = "auth", value = "重置学生密码", type = "UPDATE")
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) ResetPasswordDTO dto) {
        studentUserService.resetPassword(id, dto != null ? dto : new ResetPasswordDTO());
        return R.ok();
    }
}
