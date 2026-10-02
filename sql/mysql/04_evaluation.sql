USE campus_evaluation_system;

-- Evaluation domain. formQuestions and evaluationQuestions from mock are merged into eval_question.

CREATE TABLE IF NOT EXISTS eval_form (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价表单ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  type VARCHAR(32) NOT NULL COMMENT 'teaching/service/instant',
  title VARCHAR(180) NOT NULL COMMENT '表单标题',
  description VARCHAR(2000) NULL COMMENT '描述',
  cover_file_id BIGINT NULL COMMENT '封面文件ID',
  publisher_id BIGINT NOT NULL COMMENT '发布人ID',
  publish_scope VARCHAR(64) NOT NULL DEFAULT 'school' COMMENT '发布范围',
  anonymous TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否匿名',
  teaching_org_id BIGINT NULL COMMENT '教学组织ID',
  service_org_id BIGINT NULL COMMENT '服务组织ID',
  course_id BIGINT NULL COMMENT '课程ID',
  service_item_id BIGINT NULL COMMENT '服务项目ID',
  status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT 'draft/pending_review/published/rejected/closed',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_form_tenant_status (tenant_id, status, deleted),
  KEY idx_eval_form_school (school_id, status, deleted),
  KEY idx_eval_form_publisher (tenant_id, publisher_id, deleted),
  KEY idx_eval_form_target (tenant_id, type, course_id, service_item_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表单';

CREATE TABLE IF NOT EXISTS eval_question (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  form_id BIGINT NOT NULL COMMENT '表单ID',
  type VARCHAR(32) NOT NULL COMMENT 'rating/single/multiple/text/file',
  title VARCHAR(500) NOT NULL COMMENT '题目标题',
  required TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必答',
  max_score DECIMAL(5,2) NULL COMMENT '最高分',
  min_length INT NULL COMMENT '最小字数',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_question_form (tenant_id, form_id, deleted),
  KEY idx_eval_question_sort (form_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价题目';

CREATE TABLE IF NOT EXISTS eval_question_option (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目选项ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  option_text VARCHAR(255) NOT NULL COMMENT '选项文本',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_option_question (question_id, deleted),
  KEY idx_eval_option_sort (question_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价题目选项';

CREATE TABLE IF NOT EXISTS eval_form_publish_audit (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '表单发布审核ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  form_id BIGINT NOT NULL COMMENT '表单ID',
  action VARCHAR(32) NOT NULL DEFAULT 'publish' COMMENT '审核动作',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  requested_by BIGINT NOT NULL COMMENT '提交人ID',
  requested_at DATETIME NOT NULL COMMENT '提交时间',
  submitter_role VARCHAR(64) NULL COMMENT '提交人角色',
  submit_reason VARCHAR(1000) NULL COMMENT '提交说明',
  reviewed_by BIGINT NULL COMMENT '审核人ID',
  reviewed_at DATETIME NULL COMMENT '审核时间',
  review_comment VARCHAR(1000) NULL COMMENT '审核意见',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_form_audit_status (tenant_id, status, deleted),
  KEY idx_eval_form_audit_form (tenant_id, form_id, deleted),
  KEY idx_eval_form_audit_requested (tenant_id, requested_by, requested_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单发布审核';

CREATE TABLE IF NOT EXISTS eval_window (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价窗口ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  form_id BIGINT NOT NULL COMMENT '表单ID',
  type VARCHAR(32) NOT NULL COMMENT 'teaching/service/instant',
  start_at DATETIME NOT NULL COMMENT '开始时间',
  end_at DATETIME NOT NULL COMMENT '结束时间',
  modifiable_hours INT NOT NULL DEFAULT 24 COMMENT '可修改小时数',
  status VARCHAR(32) NOT NULL DEFAULT 'scheduled' COMMENT 'scheduled/open/closed',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_window_form (tenant_id, form_id, deleted),
  KEY idx_eval_window_status_time (tenant_id, status, start_at, end_at, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价窗口';

CREATE TABLE IF NOT EXISTS eval_submission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价提交ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  form_id BIGINT NOT NULL COMMENT '表单ID',
  window_id BIGINT NOT NULL COMMENT '窗口ID',
  evaluator_user_id BIGINT NULL COMMENT '评价者用户ID，仅授权追溯使用',
  evaluator_hash VARCHAR(128) NOT NULL COMMENT '评价者匿名哈希',
  target_type VARCHAR(32) NOT NULL COMMENT 'course/service_item/other',
  target_id BIGINT NOT NULL COMMENT '评价对象ID',
  overall_score DECIMAL(5,2) NULL COMMENT '总评分',
  anonymous TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否匿名',
  submitted_at DATETIME NULL COMMENT '提交时间',
  modifiable_until DATETIME NULL COMMENT '可修改截止时间',
  locked_at DATETIME NULL COMMENT '锁定时间',
  status VARCHAR(32) NOT NULL DEFAULT 'submitted' COMMENT 'draft/submitted/locked/deleted',
  review_status VARCHAR(32) NULL COMMENT 'normal/risk/pending_review/confirmed/ignored',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_submission_once (tenant_id, window_id, target_type, target_id, evaluator_hash),
  KEY idx_eval_submission_form (tenant_id, form_id, deleted),
  KEY idx_eval_submission_user (tenant_id, evaluator_user_id, deleted),
  KEY idx_eval_submission_target (tenant_id, target_type, target_id, deleted),
  KEY idx_eval_submission_status (tenant_id, status, review_status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价提交';

CREATE TABLE IF NOT EXISTS eval_answer (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价答案ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submission_id BIGINT NOT NULL COMMENT '提交ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  answer_value JSON NULL COMMENT '答案，文本/单选/多选统一JSON存储',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_answer_question (submission_id, question_id),
  KEY idx_eval_answer_submission (tenant_id, submission_id, deleted),
  KEY idx_eval_answer_question (question_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价答案';

CREATE TABLE IF NOT EXISTS eval_score (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评分ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submission_id BIGINT NOT NULL COMMENT '提交ID',
  question_id BIGINT NULL COMMENT '题目ID',
  indicator_id BIGINT NULL COMMENT '指标ID预留',
  score DECIMAL(5,2) NOT NULL COMMENT '得分',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_score_submission (tenant_id, submission_id, deleted),
  KEY idx_eval_score_question (question_id, deleted),
  KEY idx_eval_score_value (tenant_id, score, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价评分';

CREATE TABLE IF NOT EXISTS eval_attachment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价附件ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submission_id BIGINT NOT NULL COMMENT '提交ID',
  answer_id BIGINT NULL COMMENT '答案ID',
  file_id BIGINT NOT NULL COMMENT '文件ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_attachment_submission (tenant_id, submission_id, deleted),
  KEY idx_eval_attachment_file (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价附件';
