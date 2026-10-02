USE campus_evaluation_system;

-- Complaint and feedback work order domain.

CREATE TABLE IF NOT EXISTS cmp_complaint (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '投诉建议ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submitter_id BIGINT NOT NULL COMMENT '提交人ID',
  complaint_type VARCHAR(32) NOT NULL COMMENT 'complaint/suggestion/inquiry/praise',
  target_type VARCHAR(32) NOT NULL COMMENT 'teaching/logistics/other',
  target_id VARCHAR(64) NULL COMMENT '目标ID，兼容其他类型字符串目标',
  service_item_id BIGINT NULL COMMENT '服务项目ID',
  service_org_id BIGINT NULL COMMENT '服务组织ID',
  course_id BIGINT NULL COMMENT '课程ID',
  teaching_org_id BIGINT NULL COMMENT '教学组织ID',
  title VARCHAR(180) NOT NULL COMMENT '标题',
  content VARCHAR(3000) NOT NULL COMMENT '内容',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/resolved/rejected/cancelled',
  priority VARCHAR(32) NOT NULL DEFAULT 'normal' COMMENT '优先级',
  anonymous_to_handler TINYINT(1) NOT NULL DEFAULT 1 COMMENT '对处理人匿名',
  attachment_file_ids JSON NULL COMMENT '附件ID数组',
  resolved_at DATETIME NULL COMMENT '办结时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_cmp_complaint_status (tenant_id, school_id, status, deleted),
  KEY idx_cmp_complaint_submitter (tenant_id, submitter_id, deleted),
  KEY idx_cmp_complaint_target (tenant_id, target_type, service_item_id, course_id, deleted),
  KEY idx_cmp_complaint_created (tenant_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉建议';

CREATE TABLE IF NOT EXISTS cmp_feedback_work_order (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈工单ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  source VARCHAR(32) NOT NULL DEFAULT 'complaint' COMMENT '来源',
  source_id BIGINT NOT NULL COMMENT '来源业务ID',
  submitter_id BIGINT NOT NULL COMMENT '提交人ID',
  assignee_id BIGINT NULL COMMENT '处理人ID',
  handler_org_id BIGINT NULL COMMENT '处理组织ID',
  service_item_id BIGINT NULL COMMENT '服务项目ID',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/resolved/rejected/closed',
  priority VARCHAR(32) NOT NULL DEFAULT 'normal',
  completed_at DATETIME NULL COMMENT '完成时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_cmp_work_order_status (tenant_id, school_id, status, deleted),
  KEY idx_cmp_work_order_assignee (tenant_id, assignee_id, status, deleted),
  KEY idx_cmp_work_order_source (tenant_id, source, source_id, deleted),
  KEY idx_cmp_work_order_org (tenant_id, handler_org_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈工单';

CREATE TABLE IF NOT EXISTS cmp_complaint_process_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '投诉处理记录ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  complaint_id BIGINT NOT NULL COMMENT '投诉建议ID',
  handler_id BIGINT NULL COMMENT '处理人ID',
  from_status VARCHAR(32) NULL COMMENT '原状态',
  to_status VARCHAR(32) NULL COMMENT '新状态',
  content VARCHAR(2000) NULL COMMENT '处理内容',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_cmp_record_complaint (tenant_id, complaint_id, created_at),
  KEY idx_cmp_record_handler (tenant_id, handler_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉处理记录';
