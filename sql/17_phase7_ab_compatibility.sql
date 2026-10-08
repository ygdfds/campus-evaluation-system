USE campus_evaluation_system;

-- Upgrade an existing A/B database in place.
-- This migration changes schema only and does not insert demo or test data.

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
DROP PROCEDURE IF EXISTS rename_column_if_needed;
DROP PROCEDURE IF EXISTS migrate_legacy_complaint_records;

DELIMITER $$

CREATE PROCEDURE add_column_if_missing(
  IN p_table VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_definition VARCHAR(1000)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table
      AND column_name = p_column
  ) THEN
    SET @ddl = CONCAT(
      'ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition
    );
    PREPARE statement FROM @ddl;
    EXECUTE statement;
    DEALLOCATE PREPARE statement;
  END IF;
END$$

CREATE PROCEDURE add_index_if_missing(
  IN p_table VARCHAR(64),
  IN p_index VARCHAR(64),
  IN p_definition VARCHAR(1000)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = p_table
      AND index_name = p_index
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD ', p_definition);
    PREPARE statement FROM @ddl;
    EXECUTE statement;
    DEALLOCATE PREPARE statement;
  END IF;
END$$

CREATE PROCEDURE rename_column_if_needed(
  IN p_table VARCHAR(64),
  IN p_old_column VARCHAR(64),
  IN p_new_column VARCHAR(64),
  IN p_definition VARCHAR(1000)
)
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table
      AND column_name = p_old_column
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table
      AND column_name = p_new_column
  ) THEN
    SET @ddl = CONCAT(
      'ALTER TABLE `', p_table, '` CHANGE COLUMN `', p_old_column,
      '` `', p_new_column, '` ', p_definition
    );
    PREPARE statement FROM @ddl;
    EXECUTE statement;
    DEALLOCATE PREPARE statement;
  END IF;
END$$

CREATE PROCEDURE migrate_legacy_complaint_records()
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'cmp_process_record'
  ) THEN
    INSERT INTO cmp_complaint_process_record
      (tenant_id, school_id, complaint_id, handler_id, from_status, to_status,
       content, created_at, deleted)
    SELECT old.tenant_id, complaint.school_id, old.complaint_id, old.handler_id,
           old.from_status, old.to_status, old.content, old.created_at, old.deleted
    FROM cmp_process_record old
    LEFT JOIN cmp_complaint complaint ON complaint.id = old.complaint_id
    WHERE NOT EXISTS (
      SELECT 1
      FROM cmp_complaint_process_record current_record
      WHERE current_record.tenant_id = old.tenant_id
        AND current_record.complaint_id = old.complaint_id
        AND current_record.created_at = old.created_at
        AND COALESCE(current_record.handler_id, 0) = COALESCE(old.handler_id, 0)
    );
  END IF;
END$$

DELIMITER ;

CALL add_column_if_missing(
  'eval_submission', 'evaluator_hash',
  'VARCHAR(128) NULL AFTER `evaluator_user_id`'
);
UPDATE eval_submission
SET evaluator_hash = SHA2(
  CONCAT('campus-evaluation:', tenant_id, ':', COALESCE(evaluator_user_id, id)),
  256
)
WHERE evaluator_hash IS NULL OR evaluator_hash = '';
ALTER TABLE eval_submission MODIFY COLUMN evaluator_hash VARCHAR(128) NOT NULL;
CALL add_index_if_missing(
  'eval_submission',
  'uk_eval_submission_once',
  'UNIQUE KEY `uk_eval_submission_once` (`tenant_id`,`window_id`,`target_type`,`target_id`,`evaluator_hash`)'
);

CALL add_column_if_missing(
  'eval_score', 'indicator_id', 'BIGINT NULL AFTER `question_id`'
);
CALL add_column_if_missing(
  'eval_attachment', 'answer_id', 'BIGINT NULL AFTER `question_id`'
);
CALL add_column_if_missing(
  'eval_attachment', 'updated_at',
  'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`'
);

CALL add_column_if_missing(
  'cmp_complaint_process_record', 'school_id',
  'BIGINT NULL AFTER `tenant_id`'
);
CALL add_column_if_missing(
  'cmp_complaint_process_record', 'deleted',
  'TINYINT(1) NOT NULL DEFAULT 0 AFTER `created_at`'
);

CALL migrate_legacy_complaint_records();

CALL rename_column_if_needed(
  'msg_notification', 'business_type', 'biz_type', 'VARCHAR(64) NULL'
);
CALL add_column_if_missing(
  'msg_notification', 'sender_user_id', 'BIGINT NULL AFTER `school_id`'
);
CALL add_column_if_missing(
  'msg_notification', 'tag', 'VARCHAR(64) NULL AFTER `read_at`'
);
CALL add_column_if_missing(
  'msg_notification', 'cover_file_id', 'BIGINT NULL AFTER `tag`'
);
CALL add_column_if_missing(
  'msg_notification', 'publish_time', 'DATETIME NULL AFTER `cover_file_id`'
);
CALL add_column_if_missing(
  'msg_notification', 'status',
  'VARCHAR(32) NOT NULL DEFAULT ''published'' AFTER `publish_time`'
);
CALL add_column_if_missing(
  'msg_notification', 'notice_type', 'VARCHAR(64) NULL AFTER `status`'
);
CALL add_index_if_missing(
  'msg_notification',
  'idx_msg_notification_biz',
  'KEY `idx_msg_notification_biz` (`tenant_id`,`biz_type`,`biz_id`,`deleted`)'
);

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
DROP PROCEDURE IF EXISTS rename_column_if_needed;
DROP PROCEDURE IF EXISTS migrate_legacy_complaint_records;
