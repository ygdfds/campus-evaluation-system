package com.campus.evaluation.imports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.auth.domain.dto.StaffUserCreateDTO;
import com.campus.evaluation.auth.domain.dto.StudentUserCreateDTO;
import com.campus.evaluation.auth.service.SchoolStaffUserService;
import com.campus.evaluation.auth.service.SchoolStudentUserService;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.imports.domain.entity.ImportBatch;
import com.campus.evaluation.imports.domain.entity.ImportRecordError;
import com.campus.evaluation.imports.domain.vo.ImportBatchVO;
import com.campus.evaluation.imports.domain.vo.ImportRecordErrorVO;
import com.campus.evaluation.imports.mapper.ImportBatchMapper;
import com.campus.evaluation.imports.mapper.ImportRecordErrorMapper;
import com.campus.evaluation.imports.service.SchoolImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SchoolImportServiceImpl implements SchoolImportService {
    private final ImportBatchMapper batchMapper;
    private final ImportRecordErrorMapper errorMapper;
    private final SchoolStudentUserService studentUserService;
    private final SchoolStaffUserService staffUserService;

    @Override
    public PageResult<ImportBatchVO> list(String importType, String status, String keyword, int pageNum, int pageSize) {
        Long tenantId = requireTenantId();
        Page<ImportBatch> page = batchMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ImportBatch>()
                        .eq(ImportBatch::getTenantId, tenantId)
                        .eq(importType != null && !importType.isBlank(), ImportBatch::getImportType, importType)
                        .eq(status != null && !status.isBlank(), ImportBatch::getStatus, status)
                        .like(keyword != null && !keyword.isBlank(), ImportBatch::getFileName, keyword)
                        .orderByDesc(ImportBatch::getId));
        return new PageResult<>(page.getTotal(), page.getRecords().stream().map(this::toVO).toList(), pageNum, pageSize);
    }

    @Override
    public List<ImportRecordErrorVO> errors(Long batchId) {
        ImportBatch batch = getBatch(batchId);
        return errorMapper.selectList(new LambdaQueryWrapper<ImportRecordError>()
                        .eq(ImportRecordError::getBatchId, batch.getId())
                        .orderByAsc(ImportRecordError::getRowNumber)
                        .orderByAsc(ImportRecordError::getId))
                .stream().map(this::toErrorVO).toList();
    }

    @Override
    @Transactional
    public ImportBatchVO upload(String importType, MultipartFile file) {
        if (!Set.of("student", "staff").contains(importType)) {
            throw new BusinessException(400, "Only student and staff import are enabled in phase A");
        }
        if (file == null || file.isEmpty()) throw new BusinessException(400, "Import file is required");
        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("import.csv");
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new BusinessException(400, "Only CSV templates are supported in phase A");
        }

        ImportBatch batch = new ImportBatch();
        batch.setTenantId(requireTenantId());
        batch.setSchoolId(SecurityUtils.getSchoolId());
        batch.setImportType(importType);
        batch.setFileName(fileName);
        batch.setTotalCount(0);
        batch.setSuccessCount(0);
        batch.setErrorCount(0);
        batch.setStatus("processing");
        batch.setUploaderId(SecurityUtils.getUserId());
        batch.setUploaderName(SecurityUtils.getUsername());
        batch.setStartedAt(LocalDateTime.now());
        batchMapper.insert(batch);

        int total = 0;
        int success = 0;
        int errors = 0;
        try {
            List<String> lines = new String(file.getBytes(), StandardCharsets.UTF_8)
                    .replace("\uFEFF", "")
                    .lines()
                    .filter(line -> !line.isBlank())
                    .toList();
            if (lines.size() < 2) throw new BusinessException(400, "CSV must include header and at least one data row");
            String[] headers = split(lines.get(0));
            for (int i = 1; i < lines.size(); i++) {
                total++;
                Map<String, String> row = toRow(headers, split(lines.get(i)));
                try {
                    if ("student".equals(importType)) importStudent(row);
                    else importStaff(row);
                    success++;
                } catch (Exception ex) {
                    errors++;
                    addError(batch.getId(), i + 1, firstBlankField(row), ex.getMessage(), lines.get(i));
                }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            addError(batch.getId(), 1, "file", ex.getMessage(), fileName);
            errors++;
        }

        batch.setTotalCount(total);
        batch.setSuccessCount(success);
        batch.setErrorCount(errors);
        batch.setStatus(errors == 0 ? "success" : (success == 0 ? "failed" : "partial_failed"));
        batch.setFinishedAt(LocalDateTime.now());
        batchMapper.updateById(batch);
        return toVO(batch);
    }

    @Override
    public byte[] template(String importType) {
        String content = switch (importType) {
            case "student" -> "username,realName,studentNo,classId,phone,email\nstudent001,张三,2026001,1,13800000000,student001@example.com\n";
            case "staff" -> "username,realName,staffNo,teachingOrgId,serviceOrgId,phone,email\nstaff001,李四,T2026001,1,,13900000000,staff001@example.com\n";
            default -> throw new BusinessException(400, "Template is only available for student or staff");
        };
        return content.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void cancel(Long batchId) {
        ImportBatch batch = getBatch(batchId);
        if (!"processing".equals(batch.getStatus()) && !"pending".equals(batch.getStatus())) {
            throw new BusinessException(409, "Only pending or processing batch can be cancelled");
        }
        batch.setStatus("cancelled");
        batch.setFinishedAt(LocalDateTime.now());
        batchMapper.updateById(batch);
    }

    private void importStudent(Map<String, String> row) {
        require(row, "username", "realName", "classId");
        StudentUserCreateDTO dto = new StudentUserCreateDTO();
        dto.setUsername(row.get("username"));
        dto.setRealName(row.get("realName"));
        dto.setStudentNo(row.get("studentNo"));
        dto.setClassId(Long.valueOf(row.get("classId")));
        dto.setPhone(blankToNull(row.get("phone")));
        dto.setEmail(blankToNull(row.get("email")));
        studentUserService.create(dto);
    }

    private void importStaff(Map<String, String> row) {
        require(row, "username", "realName");
        StaffUserCreateDTO dto = new StaffUserCreateDTO();
        dto.setUsername(row.get("username"));
        dto.setRealName(row.get("realName"));
        dto.setStaffNo(row.get("staffNo"));
        dto.setTeachingOrgId(parseLong(row.get("teachingOrgId")));
        dto.setServiceOrgId(parseLong(row.get("serviceOrgId")));
        dto.setPhone(blankToNull(row.get("phone")));
        dto.setEmail(blankToNull(row.get("email")));
        dto.setRoleCodes(List.of("staff"));
        staffUserService.create(dto);
    }

    private void require(Map<String, String> row, String... fields) {
        for (String field : fields) {
            if (row.get(field) == null || row.get(field).isBlank()) {
                throw new BusinessException(400, "Required field is blank: " + field);
            }
        }
    }

    private ImportBatch getBatch(Long batchId) {
        ImportBatch batch = batchMapper.selectOne(new LambdaQueryWrapper<ImportBatch>()
                .eq(ImportBatch::getId, batchId)
                .eq(ImportBatch::getTenantId, requireTenantId()));
        if (batch == null) throw new BusinessException(404, "Import batch does not exist");
        return batch;
    }

    private void addError(Long batchId, int row, String field, String reason, String value) {
        ImportRecordError error = new ImportRecordError();
        error.setBatchId(batchId);
        error.setRowNumber(row);
        error.setFieldName(field);
        error.setErrorReason(reason == null ? "Import failed" : reason);
        error.setOriginalValue(value);
        error.setCreatedAt(LocalDateTime.now());
        errorMapper.insert(error);
    }

    private String[] split(String line) {
        return line.split(",", -1);
    }

    private Map<String, String> toRow(String[] headers, String[] values) {
        Map<String, String> row = new HashMap<>();
        for (int i = 0; i < headers.length; i++) row.put(headers[i].trim(), i < values.length ? values[i].trim() : "");
        return row;
    }

    private String firstBlankField(Map<String, String> row) {
        return row.entrySet().stream().filter(e -> e.getValue() == null || e.getValue().isBlank()).map(Map.Entry::getKey).findFirst().orElse("row");
    }

    private Long parseLong(String value) {
        return value == null || value.isBlank() ? null : Long.valueOf(value);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "Tenant context is required");
        return tenantId;
    }

    private ImportBatchVO toVO(ImportBatch b) {
        return ImportBatchVO.builder().id(b.getId()).importType(b.getImportType()).fileName(b.getFileName())
                .totalCount(b.getTotalCount()).successCount(b.getSuccessCount()).errorCount(b.getErrorCount())
                .status(b.getStatus()).uploaderId(b.getUploaderId()).uploaderName(b.getUploaderName())
                .startedAt(b.getStartedAt()).finishedAt(b.getFinishedAt()).createdAt(b.getCreatedAt()).build();
    }

    private ImportRecordErrorVO toErrorVO(ImportRecordError e) {
        return ImportRecordErrorVO.builder().id(e.getId()).batchId(e.getBatchId()).rowNumber(e.getRowNumber())
                .fieldName(e.getFieldName()).errorReason(e.getErrorReason()).originalValue(e.getOriginalValue())
                .createdAt(e.getCreatedAt()).build();
    }
}
