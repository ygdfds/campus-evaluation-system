package com.campus.evaluation.auth.controller;

import com.campus.evaluation.auth.domain.vo.RoleOptionVO;
import com.campus.evaluation.auth.service.RoleService;
import com.campus.evaluation.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "角色查询", description = "学校端可分配角色查询")
@RestController
@RequestMapping("/school/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "查询可分配角色选项")
    @GetMapping("/options")
    public R<List<RoleOptionVO>> options(
            @RequestParam(required = false) String userType) {
        return R.ok(roleService.getOptions(userType));
    }
}
