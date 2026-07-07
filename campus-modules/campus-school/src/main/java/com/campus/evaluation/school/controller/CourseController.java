package com.campus.evaluation.school.controller;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.school.domain.dto.CourseDTO;
import com.campus.evaluation.school.domain.dto.CourseTeachersDTO;
import com.campus.evaluation.school.domain.vo.CourseDetailVO;
import com.campus.evaluation.school.domain.vo.CourseVO;
import com.campus.evaluation.school.domain.vo.OptionVO;
import com.campus.evaluation.school.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "课程管理")
@RestController
@RequestMapping("/school/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "课程列表")
    @GetMapping
    public R<PageResult<CourseVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long teachingOrgId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(courseService.list(keyword, teachingOrgId, status, pageNum, pageSize));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/{id}")
    public R<CourseDetailVO> getDetail(@PathVariable Long id) {
        return R.ok(courseService.getDetail(id));
    }

    @Operation(summary = "新增课程")
    @OperationLog(module = "school", value = "新增课程", type = "CREATE")
    @PostMapping
    public R<CourseVO> create(@Valid @RequestBody CourseDTO dto) {
        return R.ok(courseService.create(dto));
    }

    @Operation(summary = "编辑课程")
    @OperationLog(module = "school", value = "编辑课程", type = "UPDATE")
    @PutMapping("/{id}")
    public R<CourseVO> update(@PathVariable Long id, @Valid @RequestBody CourseDTO dto) {
        return R.ok(courseService.update(id, dto));
    }

    @Operation(summary = "删除课程")
    @OperationLog(module = "school", value = "删除课程", type = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return R.ok();
    }

    @Operation(summary = "维护课程教师关系")
    @OperationLog(module = "school", value = "维护课程教师", type = "UPDATE")
    @PutMapping("/{id}/teachers")
    public R<Void> updateTeachers(@PathVariable Long id, @Valid @RequestBody CourseTeachersDTO dto) {
        courseService.updateTeachers(id, dto);
        return R.ok();
    }

    @Operation(summary = "课程下拉选项")
    @GetMapping("/options")
    public R<List<OptionVO>> options(@RequestParam(required = false) Long teachingOrgId) {
        return R.ok(courseService.options(teachingOrgId));
    }
}
