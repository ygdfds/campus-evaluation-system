package com.campus.evaluation.school.controller;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.school.domain.dto.OrgUnitDTO;
import com.campus.evaluation.school.domain.vo.OrgTreeVO;
import com.campus.evaluation.school.domain.vo.ServiceOrgVO;
import com.campus.evaluation.school.service.ServiceOrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "服务组织管理")
@RestController
@RequestMapping("/school/service-orgs")
@RequiredArgsConstructor
public class ServiceOrgController {

    private final ServiceOrgService serviceOrgService;

    @Operation(summary = "服务组织列表")
    @GetMapping
    public R<PageResult<ServiceOrgVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(serviceOrgService.list(keyword, status, parentId, pageNum, pageSize));
    }

    @Operation(summary = "服务组织树结构")
    @GetMapping("/tree")
    public R<List<OrgTreeVO>> tree() {
        return R.ok(serviceOrgService.tree());
    }

    @Operation(summary = "新增服务组织")
    @OperationLog(module = "school", value = "新增服务组织", type = "CREATE")
    @PostMapping
    public R<ServiceOrgVO> create(@Valid @RequestBody OrgUnitDTO dto) {
        return R.ok(serviceOrgService.create(dto));
    }

    @Operation(summary = "编辑服务组织")
    @OperationLog(module = "school", value = "编辑服务组织", type = "UPDATE")
    @PutMapping("/{id}")
    public R<ServiceOrgVO> update(@PathVariable Long id, @Valid @RequestBody OrgUnitDTO dto) {
        return R.ok(serviceOrgService.update(id, dto));
    }

    @Operation(summary = "删除服务组织")
    @OperationLog(module = "school", value = "删除服务组织", type = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        serviceOrgService.delete(id);
        return R.ok();
    }
}
