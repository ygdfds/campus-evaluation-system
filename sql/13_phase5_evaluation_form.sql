USE campus_evaluation_system;

CREATE TABLE IF NOT EXISTS sys_schema_migration (
  version VARCHAR(64) NOT NULL COMMENT 'Migration version',
  description VARCHAR(255) NOT NULL COMMENT 'Migration description',
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Schema migration ledger';

SET @schema_name = DATABASE();

SET @has_score_enabled = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'eval_form' AND COLUMN_NAME = 'score_enabled'
);
SET @ddl = IF(@has_score_enabled = 0,
  'ALTER TABLE eval_form ADD COLUMN score_enabled TINYINT(1) NOT NULL DEFAULT 0 AFTER anonymous',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_published_at = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'eval_form' AND COLUMN_NAME = 'published_at'
);
SET @ddl = IF(@has_published_at = 0,
  'ALTER TABLE eval_form ADD COLUMN published_at DATETIME NULL DEFAULT NULL AFTER status',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO sys_schema_migration (version, description)
VALUES ('13_phase5_evaluation_form', 'Add score and publish timestamp columns to eval_form')
ON DUPLICATE KEY UPDATE applied_at = applied_at;
