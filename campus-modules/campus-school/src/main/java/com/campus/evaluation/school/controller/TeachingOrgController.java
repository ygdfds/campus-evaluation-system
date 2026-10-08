package com.campus.evaluation.school.controller;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.school.domain.dto.OrgUnitDTO;
import com.campus.evaluation.school.domain.vo.OrgTreeVO;
import com.campus.evaluation.school.domain.vo.TeachingOrgVO;
import com.campus.evaluation.school.service.TeachingOrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教学组织管理")
@RestController
@RequestMapping("/school/teaching-orgs")
@SaCheckRole("school_admin")
@RequiredArgsConstructor
public class TeachingOrgController {

    private final TeachingOrgService teachingOrgService;

    @Operation(summary = "教学组织列表")
    @GetMapping
    public R<PageResult<TeachingOrgVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        int currentPage = page != null ? page : (pageNum != null ? pageNum : 1);
        return R.ok(teachingOrgService.list(keyword, status, parentId, currentPage, pageSize));
    }

    @Operation(summary = "教学组织树结构")
    @GetMapping("/tree")
    public R<List<OrgTreeVO>> tree() {
        return R.ok(teachingOrgService.tree());
    }

    @Operation(summary = "新增教学组织")
    @OperationLog(module = "school", value = "新增教学组织", type = "CREATE")
    @PostMapping
    public R<TeachingOrgVO> create(@Valid @RequestBody OrgUnitDTO dto) {
        return R.ok(teachingOrgService.create(dto));
    }

    @Operation(summary = "编辑教学组织")
    @OperationLog(module = "school", value = "编辑教学组织", type = "UPDATE")
    @PutMapping("/{id}")
    public R<TeachingOrgVO> update(@PathVariable Long id, @Valid @RequestBody OrgUnitDTO dto) {
        return R.ok(teachingOrgService.update(id, dto));
    }

    @Operation(summary = "删除教学组织")
    @OperationLog(module = "school", value = "删除教学组织", type = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        teachingOrgService.delete(id);
        return R.ok();
    }
}
