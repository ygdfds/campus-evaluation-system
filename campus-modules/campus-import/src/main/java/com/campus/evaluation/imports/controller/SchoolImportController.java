package com.campus.evaluation.imports.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.imports.domain.vo.ImportBatchVO;
import com.campus.evaluation.imports.domain.vo.ImportRecordErrorVO;
import com.campus.evaluation.imports.service.SchoolImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/school/imports")
@SaCheckRole("school_admin")
@RequiredArgsConstructor
public class SchoolImportController {
    private final SchoolImportService importService;

    @GetMapping("/batches")
    public R<PageResult<ImportBatchVO>> list(@RequestParam(required = false) String importType,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(importService.list(importType, status, keyword, pageNum, pageSize));
    }

    @GetMapping("/batches/{batchId}/errors")
    public R<List<ImportRecordErrorVO>> errors(@PathVariable Long batchId) {
        return R.ok(importService.errors(batchId));
    }

    @PostMapping("/upload")
    @OperationLog(module = "import", value = "Upload school import file", type = "CREATE")
    public R<ImportBatchVO> upload(@RequestParam String importType, @RequestParam("file") MultipartFile file) {
        return R.ok(importService.upload(importType, file));
    }

    @DeleteMapping("/batches/{batchId}")
    public R<Void> cancel(@PathVariable Long batchId) {
        importService.cancel(batchId);
        return R.ok();
    }

    @GetMapping("/templates/{importType}")
    public ResponseEntity<byte[]> template(@PathVariable String importType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + importType + "-template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(importService.template(importType));
    }
}
