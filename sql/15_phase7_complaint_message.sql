USE campus_evaluation_system;

-- Phase 7 complaint, work order, and notification tables.
-- Existing databases should also run 17_phase7_ab_compatibility.sql.

CREATE TABLE IF NOT EXISTS cmp_complaint (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  school_id BIGINT NULL,
  submitter_id BIGINT NOT NULL,
  complaint_type VARCHAR(32) NOT NULL,
  target_type VARCHAR(32) NOT NULL,
  target_id VARCHAR(64) NULL,
  course_id BIGINT NULL,
  teaching_org_id BIGINT NULL,
  service_item_id BIGINT NULL,
  service_org_id BIGINT NULL,
  title VARCHAR(180) NOT NULL,
  content VARCHAR(3000) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'pending',
  priority VARCHAR(32) NOT NULL DEFAULT 'normal',
  anonymous_to_handler TINYINT(1) NOT NULL DEFAULT 0,
  attachment_file_ids JSON NULL,
  cancelled_at DATETIME NULL,
  cancel_reason VARCHAR(300) NULL,
  resolved_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_cmp_complaint_submitter (tenant_id, submitter_id, deleted),
  KEY idx_cmp_complaint_status (tenant_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Complaint and suggestion';

CREATE TABLE IF NOT EXISTS cmp_complaint_process_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  school_id BIGINT NULL,
  complaint_id BIGINT NOT NULL,
  handler_id BIGINT NULL,
  from_status VARCHAR(32) NULL,
  to_status VARCHAR(32) NULL,
  content VARCHAR(2000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_cmp_record_complaint (tenant_id, complaint_id, created_at),
  KEY idx_cmp_record_handler (tenant_id, handler_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Complaint process record';

CREATE TABLE IF NOT EXISTS cmp_feedback_work_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  school_id BIGINT NULL,
  submitter_id BIGINT NOT NULL,
  source VARCHAR(32) NOT NULL DEFAULT 'complaint',
  source_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'pending',
  priority VARCHAR(32) NOT NULL DEFAULT 'normal',
  handler_org_id BIGINT NULL,
  assignee_id BIGINT NULL,
  completed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_cmp_work_order_source (tenant_id, source, source_id, deleted),
  KEY idx_cmp_work_order_status (tenant_id, status, deleted),
  KEY idx_cmp_work_order_assignee (tenant_id, assignee_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Feedback work order';

CREATE TABLE IF NOT EXISTS msg_notification (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  school_id BIGINT NULL,
  sender_user_id BIGINT NULL,
  receiver_user_id BIGINT NULL,
  target_roles VARCHAR(1000) NULL,
  type VARCHAR(64) NOT NULL DEFAULT 'system',
  biz_type VARCHAR(64) NULL,
  title VARCHAR(180) NOT NULL,
  content TEXT NULL,
  priority VARCHAR(32) NOT NULL DEFAULT 'normal',
  read_status VARCHAR(32) NOT NULL DEFAULT 'unread',
  link VARCHAR(300) NULL,
  biz_id BIGINT NULL,
  read_at DATETIME NULL,
  tag VARCHAR(64) NULL,
  cover_file_id BIGINT NULL,
  publish_time DATETIME NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'published',
  notice_type VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_msg_notification_receiver (tenant_id, receiver_user_id, read_status, deleted),
  KEY idx_msg_notification_biz (tenant_id, biz_type, biz_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Notification';
