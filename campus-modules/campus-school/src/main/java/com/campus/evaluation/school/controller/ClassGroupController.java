package com.campus.evaluation.school.controller;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.school.domain.dto.ClassGroupDTO;
import com.campus.evaluation.school.domain.vo.ClassGroupVO;
import com.campus.evaluation.school.domain.vo.OptionVO;
import com.campus.evaluation.school.service.ClassGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "班级管理")
@RestController
@RequestMapping("/school/classes")
@RequiredArgsConstructor
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    @Operation(summary = "班级列表")
    @GetMapping
    public R<PageResult<ClassGroupVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long teachingOrgId,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(classGroupService.list(keyword, teachingOrgId, grade, status, pageNum, pageSize));
    }

    @Operation(summary = "新增班级")
    @OperationLog(module = "school", value = "新增班级", type = "CREATE")
    @PostMapping
    public R<ClassGroupVO> create(@Valid @RequestBody ClassGroupDTO dto) {
        return R.ok(classGroupService.create(dto));
    }

    @Operation(summary = "编辑班级")
    @OperationLog(module = "school", value = "编辑班级", type = "UPDATE")
    @PutMapping("/{id}")
    public R<ClassGroupVO> update(@PathVariable Long id, @Valid @RequestBody ClassGroupDTO dto) {
        return R.ok(classGroupService.update(id, dto));
    }

    @Operation(summary = "删除班级")
    @OperationLog(module = "school", value = "删除班级", type = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        classGroupService.delete(id);
        return R.ok();
    }

    @Operation(summary = "班级下拉选项")
    @GetMapping("/options")
    public R<List<OptionVO>> options(@RequestParam(required = false) Long teachingOrgId) {
        return R.ok(classGroupService.options(teachingOrgId));
    }
}
