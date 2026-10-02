USE campus_evaluation_system;

-- Platform and tenant domain. Platform-level tables are intentionally kept independent
-- from school business tables so they can be moved to a platform service later.

CREATE TABLE IF NOT EXISTS pf_tenant_plan (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
  plan_code VARCHAR(64) NOT NULL COMMENT '套餐编码',
  plan_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
  features_json JSON NULL COMMENT '套餐能力JSON',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pf_tenant_plan_code (plan_code),
  KEY idx_pf_tenant_plan_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台租户套餐';

CREATE TABLE IF NOT EXISTS pf_tenant (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  tenant_code VARCHAR(64) NOT NULL COMMENT '租户编码',
  plan_id BIGINT NULL COMMENT '套餐ID',
  school_name VARCHAR(160) NOT NULL COMMENT '学校名称',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '租户状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pf_tenant_code (tenant_code),
  KEY idx_pf_tenant_plan (plan_id),
  KEY idx_pf_tenant_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台租户';

CREATE TABLE IF NOT EXISTS pf_school_onboarding_application (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '入驻申请ID',
  plan_id BIGINT NULL COMMENT '申请套餐ID',
  tenant_id BIGINT NULL COMMENT '审核通过后生成的租户ID',
  school_full_name VARCHAR(180) NOT NULL COMMENT '学校全称',
  school_credit_code VARCHAR(64) NULL COMMENT '统一社会信用代码',
  contact_name VARCHAR(80) NOT NULL COMMENT '联系人',
  contact_phone VARCHAR(32) NOT NULL COMMENT '联系电话',
  contact_email VARCHAR(120) NULL COMMENT '联系邮箱',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected/resubmit_required',
  submit_reason VARCHAR(500) NULL COMMENT '申请说明',
  review_comment VARCHAR(1000) NULL COMMENT '审核意见',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_pf_onboarding_status (status, deleted),
  KEY idx_pf_onboarding_tenant (tenant_id),
  KEY idx_pf_onboarding_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学校入驻申请';

CREATE TABLE IF NOT EXISTS pf_onboarding_material (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '入驻材料ID',
  application_id BIGINT NULL COMMENT '申请ID',
  tenant_id BIGINT NULL COMMENT '租户ID',
  file_id BIGINT NOT NULL COMMENT '文件ID',
  material_type VARCHAR(64) NOT NULL COMMENT '材料类型',
  status VARCHAR(32) NOT NULL DEFAULT 'uploaded' COMMENT '材料状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_pf_material_application (application_id, deleted),
  KEY idx_pf_material_tenant (tenant_id, deleted),
  KEY idx_pf_material_file (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入驻材料';

CREATE TABLE IF NOT EXISTS pf_onboarding_audit_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '入驻审核记录ID',
  application_id BIGINT NOT NULL COMMENT '申请ID',
  auditor_id BIGINT NULL COMMENT '审核人ID',
  action VARCHAR(32) NOT NULL COMMENT '审核动作',
  from_status VARCHAR(32) NULL COMMENT '原状态',
  to_status VARCHAR(32) NOT NULL COMMENT '新状态',
  comment VARCHAR(1000) NULL COMMENT '审核说明',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_pf_onboarding_audit_app (application_id, created_at),
  KEY idx_pf_onboarding_audit_auditor (auditor_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入驻审核记录';
