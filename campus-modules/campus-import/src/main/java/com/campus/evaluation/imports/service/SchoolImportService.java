package com.campus.evaluation.imports.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.imports.domain.vo.ImportBatchVO;
import com.campus.evaluation.imports.domain.vo.ImportRecordErrorVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SchoolImportService {
    PageResult<ImportBatchVO> list(String importType, String status, String keyword, int pageNum, int pageSize);
    List<ImportRecordErrorVO> errors(Long batchId);
    ImportBatchVO upload(String importType, MultipartFile file);
    byte[] template(String importType);
    void cancel(Long batchId);
}
