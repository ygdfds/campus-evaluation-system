USE campus_evaluation_system;

-- Operation and audit logs are append-only from business perspective.

CREATE TABLE IF NOT EXISTS log_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  user_id BIGINT NULL COMMENT '操作者ID',
  module VARCHAR(64) NOT NULL COMMENT '模块',
  action VARCHAR(64) NOT NULL COMMENT '操作类型',
  target_type VARCHAR(64) NULL COMMENT '业务对象类型',
  target_id BIGINT NULL COMMENT '业务对象ID',
  target_name VARCHAR(255) NULL COMMENT '业务对象名称',
  content VARCHAR(3000) NULL COMMENT '操作内容',
  result VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT 'success/fail',
  ip VARCHAR(64) NULL COMMENT 'IP',
  device VARCHAR(255) NULL COMMENT '设备',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '保留字段，业务不删除',
  PRIMARY KEY (id),
  KEY idx_log_operation_tenant (tenant_id, school_id, created_at),
  KEY idx_log_operation_user (tenant_id, user_id, created_at),
  KEY idx_log_operation_module (tenant_id, module, action, created_at),
  KEY idx_log_operation_target (tenant_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志';

CREATE TABLE IF NOT EXISTS log_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  user_id BIGINT NULL COMMENT '操作者ID',
  module VARCHAR(64) NOT NULL COMMENT '模块',
  action_type VARCHAR(64) NOT NULL COMMENT '审计动作',
  target_type VARCHAR(64) NULL COMMENT '对象类型',
  target_id BIGINT NULL COMMENT '对象ID',
  target_name VARCHAR(255) NULL COMMENT '对象名称',
  content VARCHAR(3000) NULL COMMENT '审计内容',
  result VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT '结果',
  ip VARCHAR(64) NULL COMMENT 'IP',
  device VARCHAR(255) NULL COMMENT '设备',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '保留字段，业务不删除',
  PRIMARY KEY (id),
  KEY idx_log_audit_tenant (tenant_id, school_id, created_at),
  KEY idx_log_audit_user (tenant_id, user_id, created_at),
  KEY idx_log_audit_module (tenant_id, module, action_type, created_at),
  KEY idx_log_audit_target (tenant_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志';
