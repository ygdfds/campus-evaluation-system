package com.campus.evaluation.school.controller;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.school.domain.dto.ServiceItemDTO;
import com.campus.evaluation.school.domain.vo.OptionVO;
import com.campus.evaluation.school.domain.vo.ServiceItemVO;
import com.campus.evaluation.school.service.ServiceItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "服务项目管理")
@RestController
@RequestMapping("/school/service-items")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    @Operation(summary = "服务项目列表")
    @GetMapping
    public R<PageResult<ServiceItemVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long serviceOrgId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(serviceItemService.list(keyword, serviceOrgId, status, pageNum, pageSize));
    }

    @Operation(summary = "服务项目详情")
    @GetMapping("/{id}")
    public R<ServiceItemVO> getDetail(@PathVariable Long id) {
        return R.ok(serviceItemService.getDetail(id));
    }

    @Operation(summary = "新增服务项目")
    @OperationLog(module = "school", value = "新增服务项目", type = "CREATE")
    @PostMapping
    public R<ServiceItemVO> create(@Valid @RequestBody ServiceItemDTO dto) {
        return R.ok(serviceItemService.create(dto));
    }

    @Operation(summary = "编辑服务项目")
    @OperationLog(module = "school", value = "编辑服务项目", type = "UPDATE")
    @PutMapping("/{id}")
    public R<ServiceItemVO> update(@PathVariable Long id, @Valid @RequestBody ServiceItemDTO dto) {
        return R.ok(serviceItemService.update(id, dto));
    }

    @Operation(summary = "删除服务项目")
    @OperationLog(module = "school", value = "删除服务项目", type = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        serviceItemService.delete(id);
        return R.ok();
    }

    @Operation(summary = "服务项目下拉选项")
    @GetMapping("/options")
    public R<List<OptionVO>> options(@RequestParam(required = false) Long serviceOrgId) {
        return R.ok(serviceItemService.options(serviceOrgId));
    }
}
