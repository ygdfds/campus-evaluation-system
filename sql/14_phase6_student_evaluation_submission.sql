USE campus_evaluation_system;

-- Phase 6 student evaluation submission tables.
-- Existing databases should also run 17_phase7_ab_compatibility.sql.

CREATE TABLE IF NOT EXISTS eval_submission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  school_id BIGINT NULL,
  form_id BIGINT NOT NULL,
  window_id BIGINT NOT NULL,
  evaluator_user_id BIGINT NULL,
  evaluator_hash VARCHAR(128) NOT NULL,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  overall_score DECIMAL(6,2) NULL,
  anonymous TINYINT(1) NOT NULL DEFAULT 1,
  submitted_at DATETIME NULL,
  modifiable_until DATETIME NULL,
  locked_at DATETIME NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  review_status VARCHAR(32) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_submission_once
    (tenant_id, window_id, target_type, target_id, evaluator_hash),
  KEY idx_eval_submission_tenant_user (tenant_id, evaluator_user_id, deleted),
  KEY idx_eval_submission_form (tenant_id, form_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Evaluation submission';

CREATE TABLE IF NOT EXISTS eval_answer (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  school_id BIGINT NULL,
  submission_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  answer_value TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_answer_question (submission_id, question_id),
  KEY idx_eval_answer_submission (tenant_id, submission_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Evaluation answer';

CREATE TABLE IF NOT EXISTS eval_score (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  school_id BIGINT NULL,
  submission_id BIGINT NOT NULL,
  question_id BIGINT NULL,
  indicator_id BIGINT NULL,
  score DECIMAL(6,2) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_score_submission (tenant_id, submission_id, deleted),
  KEY idx_eval_score_question (question_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Evaluation score';

CREATE TABLE IF NOT EXISTS eval_attachment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  school_id BIGINT NULL,
  submission_id BIGINT NOT NULL,
  question_id BIGINT NULL,
  answer_id BIGINT NULL,
  file_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_attachment_submission (tenant_id, submission_id, deleted),
  KEY idx_eval_attachment_file (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Evaluation attachment';
