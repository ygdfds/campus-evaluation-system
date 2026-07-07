package com.campus.evaluation.file.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.log.annotation.OperationLog;
import com.campus.evaluation.file.domain.vo.FileResourceVO;
import com.campus.evaluation.file.service.FileResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件资源管理")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileResourceController {

    private final FileResourceService fileResourceService;

    @Operation(summary = "上传文件")
    @OperationLog(module = "file", value = "上传文件", type = "CREATE")
    @PostMapping("/upload")
    public R<FileResourceVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", required = false) String bizType) {
        return R.ok(fileResourceService.upload(file, bizType));
    }

    @Operation(summary = "获取文件元数据")
    @GetMapping("/{id}")
    public R<FileResourceVO> getById(@PathVariable Long id) {
        return R.ok(fileResourceService.getById(id));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        try {
            FileResourceVO vo = fileResourceService.getById(id);
            byte[] data = fileResourceService.download(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + vo.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (BusinessException e) {
            return ResponseEntity.status(mapHttpStatus(e.getCode())).build();
        }
    }

    @Operation(summary = "预览文件")
    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id) {
        try {
            FileResourceVO vo = fileResourceService.getById(id);
            byte[] data = fileResourceService.preview(id);
            String mimeType = vo.getMimeType() != null ? vo.getMimeType() : "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(data);
        } catch (BusinessException e) {
            return ResponseEntity.status(mapHttpStatus(e.getCode())).build();
        }
    }

    /**
     * 将业务错误码映射为 HTTP 状态码
     */
    private HttpStatus mapHttpStatus(int code) {
        return switch (code) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    @Operation(summary = "删除文件")
    @OperationLog(module = "file", value = "删除文件", type = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        fileResourceService.delete(id);
        return R.ok();
    }
}
