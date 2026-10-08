USE campus_evaluation_system;

CREATE TABLE IF NOT EXISTS sys_schema_migration (
  version VARCHAR(64) NOT NULL COMMENT 'Migration version',
  description VARCHAR(255) NOT NULL COMMENT 'Migration description',
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Schema migration ledger';

SET @schema_name = DATABASE();

SET @has_teaching_org_id = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'auth_person_profile' AND COLUMN_NAME = 'teaching_org_id'
);
SET @ddl = IF(@has_teaching_org_id = 0,
  'ALTER TABLE auth_person_profile ADD COLUMN teaching_org_id BIGINT NULL COMMENT ''Teaching organization ID'' AFTER org_unit_id',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_service_org_id = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'auth_person_profile' AND COLUMN_NAME = 'service_org_id'
);
SET @ddl = IF(@has_service_org_id = 0,
  'ALTER TABLE auth_person_profile ADD COLUMN service_org_id BIGINT NULL COMMENT ''Service organization ID'' AFTER teaching_org_id',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_class_id = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'auth_person_profile' AND COLUMN_NAME = 'class_id'
);
SET @ddl = IF(@has_class_id = 0,
  'ALTER TABLE auth_person_profile ADD COLUMN class_id BIGINT NULL COMMENT ''Class ID'' AFTER service_org_id',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx_teaching = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'auth_person_profile' AND INDEX_NAME = 'idx_auth_profile_teaching_org'
);
SET @ddl = IF(@has_idx_teaching = 0,
  'ALTER TABLE auth_person_profile ADD KEY idx_auth_profile_teaching_org (tenant_id, teaching_org_id)',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx_service = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'auth_person_profile' AND INDEX_NAME = 'idx_auth_profile_service_org'
);
SET @ddl = IF(@has_idx_service = 0,
  'ALTER TABLE auth_person_profile ADD KEY idx_auth_profile_service_org (tenant_id, service_org_id)',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx_class = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'auth_person_profile' AND INDEX_NAME = 'idx_auth_profile_class'
);
SET @ddl = IF(@has_idx_class = 0,
  'ALTER TABLE auth_person_profile ADD KEY idx_auth_profile_class (tenant_id, class_id)',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO sys_schema_migration (version, description)
VALUES ('12_phase4_user_management', 'Add organization binding columns and indexes to auth_person_profile')
ON DUPLICATE KEY UPDATE applied_at = applied_at;
