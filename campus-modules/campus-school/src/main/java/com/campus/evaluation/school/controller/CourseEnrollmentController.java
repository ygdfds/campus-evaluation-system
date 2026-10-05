package com.campus.evaluation.school.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.school.domain.dto.CourseEnrollmentDTO;
import com.campus.evaluation.school.domain.vo.CourseEnrollmentVO;
import com.campus.evaluation.school.service.CourseEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Course enrollment management")
@RestController
@RequestMapping("/school/enrollments")
@SaCheckRole("school_admin")
@RequiredArgsConstructor
public class CourseEnrollmentController {
    private final CourseEnrollmentService enrollmentService;

    @GetMapping
    public R<PageResult<CourseEnrollmentVO>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long classGroupId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(enrollmentService.list(courseId, studentId, classGroupId, status, pageNum, pageSize));
    }

    @PostMapping
    @OperationLog(module = "school", value = "Create enrollment", type = "CREATE")
    public R<CourseEnrollmentVO> create(@Valid @RequestBody CourseEnrollmentDTO dto) {
        return R.ok(enrollmentService.create(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "school", value = "Update enrollment", type = "UPDATE")
    public R<CourseEnrollmentVO> update(@PathVariable Long id, @Valid @RequestBody CourseEnrollmentDTO dto) {
        return R.ok(enrollmentService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete enrollment")
    public R<Void> delete(@PathVariable Long id) {
        enrollmentService.delete(id);
        return R.ok();
    }
}
