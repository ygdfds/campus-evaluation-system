USE campus_evaluation_system;

-- Review, appeal, trace authorization, and risk control domain.

CREATE TABLE IF NOT EXISTS rv_evaluation_trace (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价追溯加密记录ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submission_id BIGINT NOT NULL COMMENT '评价提交ID',
  evaluator_user_id_encrypted VARCHAR(512) NOT NULL COMMENT '加密评价者ID',
  trace_hash VARCHAR(128) NOT NULL COMMENT '追溯哈希',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_rv_trace_submission (tenant_id, submission_id),
  KEY idx_rv_trace_hash (tenant_id, trace_hash, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价追溯加密记录';

CREATE TABLE IF NOT EXISTS rv_evaluation_review_task (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '风险复核任务ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submission_id BIGINT NOT NULL COMMENT '评价提交ID',
  form_id BIGINT NOT NULL COMMENT '表单ID',
  risk_type VARCHAR(64) NOT NULL COMMENT '风险类型',
  risk_level VARCHAR(32) NOT NULL COMMENT '风险等级',
  risk_description VARCHAR(1000) NOT NULL COMMENT '风险说明',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/confirmed/ignored',
  reviewer_id BIGINT NULL COMMENT '复核人ID',
  reviewed_at DATETIME NULL COMMENT '复核时间',
  review_comment VARCHAR(1000) NULL COMMENT '复核意见',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_rv_review_status (tenant_id, school_id, status, deleted),
  KEY idx_rv_review_submission (tenant_id, submission_id, deleted),
  KEY idx_rv_review_risk (tenant_id, risk_type, risk_level, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险评价复核任务';

CREATE TABLE IF NOT EXISTS rv_appeal_request (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '申诉ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  appeal_no VARCHAR(64) NOT NULL COMMENT '申诉编号',
  submission_id BIGINT NOT NULL COMMENT '评价提交ID',
  form_id BIGINT NOT NULL COMMENT '表单ID',
  target_type VARCHAR(32) NOT NULL COMMENT '对象类型',
  target_id BIGINT NOT NULL COMMENT '对象ID',
  appellant_user_id BIGINT NOT NULL COMMENT '申诉人ID',
  appeal_type VARCHAR(64) NOT NULL COMMENT '申诉类型',
  reason VARCHAR(2000) NOT NULL COMMENT '申诉原因',
  evidence_file_ids JSON NULL COMMENT '证据附件ID数组',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/waiting_trace_auth/resolved/rejected/closed',
  priority VARCHAR(32) NOT NULL DEFAULT 'normal' COMMENT '优先级',
  handler_id BIGINT NULL COMMENT '处理人ID',
  handle_result VARCHAR(64) NULL COMMENT '处理结果',
  handle_comment VARCHAR(1000) NULL COMMENT '处理说明',
  submitted_at DATETIME NOT NULL COMMENT '提交时间',
  accepted_at DATETIME NULL COMMENT '受理时间',
  resolved_at DATETIME NULL COMMENT '办结时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_rv_appeal_no (tenant_id, appeal_no),
  KEY idx_rv_appeal_status (tenant_id, school_id, status, deleted),
  KEY idx_rv_appeal_handler (tenant_id, handler_id, status, deleted),
  KEY idx_rv_appeal_appellant (tenant_id, appellant_user_id, deleted),
  KEY idx_rv_appeal_submission (tenant_id, submission_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价申诉';

CREATE TABLE IF NOT EXISTS rv_appeal_process_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '申诉处理记录ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  appeal_id BIGINT NOT NULL COMMENT '申诉ID',
  operator_id BIGINT NULL COMMENT '操作人ID',
  action VARCHAR(64) NOT NULL COMMENT '操作类型',
  from_status VARCHAR(32) NULL COMMENT '原状态',
  to_status VARCHAR(32) NULL COMMENT '新状态',
  content VARCHAR(2000) NULL COMMENT '处理内容',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_rv_appeal_record_appeal (tenant_id, appeal_id, created_at),
  KEY idx_rv_appeal_record_operator (tenant_id, operator_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申诉处理记录';

CREATE TABLE IF NOT EXISTS rv_trace_authorization (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '追溯授权ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submission_id BIGINT NOT NULL COMMENT '评价提交ID',
  appeal_id BIGINT NOT NULL COMMENT '申诉ID',
  applicant_id BIGINT NOT NULL COMMENT '申请人ID',
  approver_id BIGINT NULL COMMENT '审批人ID',
  reason VARCHAR(2000) NOT NULL COMMENT '申请原因',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  reject_reason VARCHAR(1000) NULL COMMENT '拒绝原因',
  requested_at DATETIME NOT NULL COMMENT '申请时间',
  approved_at DATETIME NULL COMMENT '通过时间',
  rejected_at DATETIME NULL COMMENT '拒绝时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_rv_trace_auth_status (tenant_id, school_id, status, deleted),
  KEY idx_rv_trace_auth_appeal (tenant_id, appeal_id, deleted),
  KEY idx_rv_trace_auth_applicant (tenant_id, applicant_id, requested_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='匿名评价追溯授权';
