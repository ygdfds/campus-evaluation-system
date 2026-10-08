USE campus_evaluation_system;

-- Message, announcement, help center, and notification preference domain.

CREATE TABLE IF NOT EXISTS msg_notification (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  sender_user_id BIGINT NULL COMMENT '发送人ID',
  receiver_user_id BIGINT NULL COMMENT '接收人ID，为空时按target_roles发送',
  target_roles JSON NULL COMMENT '目标角色数组',
  type VARCHAR(64) NOT NULL COMMENT '通知类型',
  title VARCHAR(180) NOT NULL COMMENT '标题',
  content VARCHAR(3000) NOT NULL COMMENT '内容',
  read_status VARCHAR(32) NOT NULL DEFAULT 'unread' COMMENT 'unread/read',
  read_at DATETIME NULL COMMENT '已读时间',
  link VARCHAR(255) NULL COMMENT '跳转链接',
  biz_type VARCHAR(64) NULL COMMENT '业务类型',
  biz_id BIGINT NULL COMMENT '业务ID',
  tag VARCHAR(64) NULL COMMENT '标签',
  cover_file_id BIGINT NULL COMMENT '封面文件ID',
  publish_time DATETIME NULL COMMENT '发布时间',
  status VARCHAR(32) NOT NULL DEFAULT 'published' COMMENT '状态',
  notice_type VARCHAR(64) NULL COMMENT '通知细类',
  priority VARCHAR(32) NOT NULL DEFAULT 'normal' COMMENT '优先级',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_msg_notification_receiver (tenant_id, receiver_user_id, read_status, deleted),
  KEY idx_msg_notification_school (tenant_id, school_id, status, deleted),
  KEY idx_msg_notification_biz (tenant_id, biz_type, biz_id, deleted),
  KEY idx_msg_notification_publish (tenant_id, publish_time, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知';

CREATE TABLE IF NOT EXISTS msg_announcement (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  title VARCHAR(180) NOT NULL COMMENT '公告标题',
  summary VARCHAR(500) NULL COMMENT '摘要',
  content TEXT NOT NULL COMMENT '正文',
  tag VARCHAR(64) NOT NULL COMMENT '公告分类',
  cover_file_id BIGINT NULL COMMENT '封面文件ID',
  target_roles JSON NULL COMMENT '目标角色数组',
  status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT 'draft/published/offline',
  publish_time DATETIME NULL COMMENT '发布时间',
  publisher_id BIGINT NULL COMMENT '发布人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_msg_announcement_status (tenant_id, school_id, status, deleted),
  KEY idx_msg_announcement_tag (tenant_id, tag, status, deleted),
  KEY idx_msg_announcement_publish (tenant_id, publish_time, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园公告';

CREATE TABLE IF NOT EXISTS msg_help_ticket (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '帮助工单ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submitter_id BIGINT NOT NULL COMMENT '提交人ID',
  ticket_no VARCHAR(64) NOT NULL COMMENT '工单编号',
  title VARCHAR(180) NOT NULL COMMENT '标题',
  content VARCHAR(3000) NOT NULL COMMENT '问题描述',
  category VARCHAR(64) NOT NULL COMMENT '分类',
  priority VARCHAR(32) NOT NULL DEFAULT 'normal' COMMENT '优先级',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/replied/closed',
  attachment_file_ids JSON NULL COMMENT '附件ID数组',
  reply_content VARCHAR(3000) NULL COMMENT '回复内容',
  replied_by BIGINT NULL COMMENT '回复人ID',
  replied_at DATETIME NULL COMMENT '回复时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_msg_help_ticket_no (tenant_id, ticket_no),
  KEY idx_msg_help_ticket_status (tenant_id, school_id, status, deleted),
  KEY idx_msg_help_ticket_submitter (tenant_id, submitter_id, deleted),
  KEY idx_msg_help_ticket_category (tenant_id, category, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帮助工单';

CREATE TABLE IF NOT EXISTS msg_help_faq (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  question VARCHAR(255) NOT NULL COMMENT '问题',
  answer TEXT NOT NULL COMMENT '答案',
  category VARCHAR(64) NOT NULL COMMENT '分类',
  target_roles JSON NULL COMMENT '适用角色数组',
  keywords VARCHAR(500) NULL COMMENT '关键词',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT 'enabled/disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_msg_help_faq_category (tenant_id, school_id, category, enabled, deleted),
  KEY idx_msg_help_faq_sort (tenant_id, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帮助FAQ';

CREATE TABLE IF NOT EXISTS msg_help_guide (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '操作指引ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  title VARCHAR(180) NOT NULL COMMENT '标题',
  module VARCHAR(64) NOT NULL COMMENT '所属模块',
  summary VARCHAR(500) NULL COMMENT '摘要',
  steps JSON NULL COMMENT '步骤数组',
  target_roles JSON NULL COMMENT '适用角色数组',
  related_link VARCHAR(255) NULL COMMENT '关联链接',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_msg_help_guide_module (tenant_id, school_id, module, enabled, deleted),
  KEY idx_msg_help_guide_sort (tenant_id, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帮助操作指引';

CREATE TABLE IF NOT EXISTS msg_staff_notification_preference (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '职工通知偏好ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  feedback_notice TINYINT(1) NOT NULL DEFAULT 1 COMMENT '反馈通知',
  evaluation_notice TINYINT(1) NOT NULL DEFAULT 1 COMMENT '评价通知',
  appeal_notice TINYINT(1) NOT NULL DEFAULT 1 COMMENT '申诉通知',
  report_warning_notice TINYINT(1) NOT NULL DEFAULT 1 COMMENT '报表预警通知',
  system_notice TINYINT(1) NOT NULL DEFAULT 1 COMMENT '系统通知',
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_msg_staff_pref_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职工通知偏好';
