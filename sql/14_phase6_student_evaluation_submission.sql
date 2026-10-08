USE campus_evaluation_system;

CREATE TABLE IF NOT EXISTS eval_submission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  form_id BIGINT NOT NULL COMMENT '评价表单ID',
  window_id BIGINT NOT NULL COMMENT '评价窗口ID',
  evaluator_user_id BIGINT NOT NULL COMMENT '评价人用户ID',
  target_type VARCHAR(32) NOT NULL COMMENT '评价对象类型',
  target_id BIGINT NOT NULL COMMENT '评价对象ID',
  overall_score DECIMAL(6,2) NULL COMMENT '综合平均分',
  anonymous TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否匿名',
  submitted_at DATETIME NULL COMMENT '提交时间',
  modifiable_until DATETIME NULL COMMENT '可修改截止时间',
  locked_at DATETIME NULL COMMENT '锁定时间',
  status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT 'draft/submitted/locked',
  review_status VARCHAR(32) NULL COMMENT 'pending/approved/rejected',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_eval_submission_tenant_user (tenant_id, evaluator_user_id, deleted),
  KEY idx_eval_submission_form_user (tenant_id, form_id, evaluator_user_id, deleted),
  KEY idx_eval_submission_form (tenant_id, form_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价提交记录';

CREATE TABLE IF NOT EXISTS eval_answer (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submission_id BIGINT NOT NULL COMMENT '提交记录ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  answer_value TEXT NULL COMMENT '答案值',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_eval_answer_submission (tenant_id, submission_id, deleted),
  KEY idx_eval_answer_question (tenant_id, question_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价文本/选择答案';

CREATE TABLE IF NOT EXISTS eval_score (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submission_id BIGINT NOT NULL COMMENT '提交记录ID',
  question_id BIGINT NOT NULL COMMENT '评分题目ID',
  score DECIMAL(6,2) NOT NULL COMMENT '评分值',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_eval_score_submission (tenant_id, submission_id, deleted),
  KEY idx_eval_score_question (tenant_id, question_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价评分答案';

CREATE TABLE IF NOT EXISTS eval_attachment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  submission_id BIGINT NOT NULL COMMENT '提交记录ID',
  question_id BIGINT NULL COMMENT '题目ID',
  file_id BIGINT NOT NULL COMMENT '文件资源ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_eval_attachment_submission (tenant_id, submission_id, deleted),
  KEY idx_eval_attachment_file (tenant_id, file_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价附件';
