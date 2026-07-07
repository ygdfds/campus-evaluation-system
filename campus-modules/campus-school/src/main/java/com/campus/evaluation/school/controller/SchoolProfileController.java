package com.campus.evaluation.school.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.school.domain.dto.SchoolProfileUpdateDTO;
import com.campus.evaluation.school.domain.vo.SchoolProfileVO;
import com.campus.evaluation.school.service.SchoolProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学校资料管理")
@RestController
@RequestMapping("/school/profile")
@RequiredArgsConstructor
public class SchoolProfileController {

    private final SchoolProfileService schoolProfileService;

    @Operation(summary = "获取当前学校资料")
    @GetMapping("/current")
    public R<SchoolProfileVO> getCurrent() {
        return R.ok(schoolProfileService.getCurrentProfile());
    }

    @Operation(summary = "更新当前学校资料")
    @OperationLog(module = "school", value = "更新学校资料", type = "UPDATE")
    @PutMapping("/current")
    public R<SchoolProfileVO> updateCurrent(@Valid @RequestBody SchoolProfileUpdateDTO dto) {
        return R.ok(schoolProfileService.updateCurrentProfile(dto));
    }
}
