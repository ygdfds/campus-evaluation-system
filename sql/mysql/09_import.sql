USE campus_evaluation_system;

-- Excel/import task domain.

CREATE TABLE IF NOT EXISTS imp_import_batch (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '导入批次ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  import_type VARCHAR(64) NOT NULL COMMENT 'student/staff/course/class/enrollment/service_item',
  file_name VARCHAR(255) NOT NULL COMMENT '导入文件名',
  file_id BIGINT NULL COMMENT '导入文件ID',
  total_count INT NOT NULL DEFAULT 0 COMMENT '总数',
  success_count INT NOT NULL DEFAULT 0 COMMENT '成功数',
  error_count INT NOT NULL DEFAULT 0 COMMENT '错误数',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/success/partial_failed/failed/cancelled',
  uploader_id BIGINT NOT NULL COMMENT '上传人ID',
  uploader_name VARCHAR(80) NULL COMMENT '上传人姓名快照',
  started_at DATETIME NULL COMMENT '开始时间',
  finished_at DATETIME NULL COMMENT '完成时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_imp_batch_type_status (tenant_id, school_id, import_type, status, deleted),
  KEY idx_imp_batch_uploader (tenant_id, uploader_id, created_at),
  KEY idx_imp_batch_created (tenant_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导入批次';

CREATE TABLE IF NOT EXISTS imp_import_record_error (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '导入错误ID',
  batch_id BIGINT NOT NULL COMMENT '批次ID',
  `row_number` INT NOT NULL COMMENT '行号',
  field_name VARCHAR(120) NULL COMMENT '字段名',
  error_reason VARCHAR(1000) NOT NULL COMMENT '错误原因',
  original_value VARCHAR(1000) NULL COMMENT '原始值',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_imp_error_batch (batch_id, `row_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导入错误记录';
