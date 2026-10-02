USE campus_evaluation_system;

-- File resource domain. Object storage implementation can later move to a file service.

CREATE TABLE IF NOT EXISTS file_resource (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  tenant_id BIGINT NULL COMMENT '租户ID，平台文件可为空',
  school_id BIGINT NULL COMMENT '学校ID',
  object_key VARCHAR(500) NOT NULL COMMENT '对象存储Key',
  file_name VARCHAR(255) NOT NULL COMMENT '文件名',
  mime_type VARCHAR(120) NULL COMMENT 'MIME类型',
  url VARCHAR(1000) NULL COMMENT '访问URL',
  uploader_id BIGINT NULL COMMENT '上传人ID',
  size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小',
  biz_type VARCHAR(64) NULL COMMENT '业务类型',
  biz_id BIGINT NULL COMMENT '业务ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_file_resource_tenant (tenant_id, deleted),
  KEY idx_file_resource_uploader (tenant_id, uploader_id, deleted),
  KEY idx_file_resource_biz (tenant_id, biz_type, biz_id, deleted),
  KEY idx_file_resource_object (object_key(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件资源';
