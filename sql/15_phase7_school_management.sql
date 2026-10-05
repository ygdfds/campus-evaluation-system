USE campus_evaluation_system;

-- Add the lifecycle state required by school course management.
SET @course_status_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sch_course'
    AND column_name = 'status'
);
SET @course_status_sql = IF(
  @course_status_exists = 0,
  'ALTER TABLE sch_course ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT ''active'' AFTER end_at',
  'SELECT 1'
);
PREPARE course_status_stmt FROM @course_status_sql;
EXECUTE course_status_stmt;
DEALLOCATE PREPARE course_status_stmt;

UPDATE sch_course SET status = 'active' WHERE status IS NULL OR status = '';

-- Keep historical class/course relationships queryable while accelerating
-- association checks before deletion.
SET @enrollment_course_index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'sch_course_enrollment'
    AND index_name = 'idx_sch_enrollment_course_active'
);
SET @enrollment_course_index_sql = IF(
  @enrollment_course_index_exists = 0,
  'CREATE INDEX idx_sch_enrollment_course_active ON sch_course_enrollment (tenant_id, course_id, deleted)',
  'SELECT 1'
);
PREPARE enrollment_course_index_stmt FROM @enrollment_course_index_sql;
EXECUTE enrollment_course_index_stmt;
DEALLOCATE PREPARE enrollment_course_index_stmt;

SET @course_teacher_index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'sch_course_teacher'
    AND index_name = 'idx_sch_course_teacher_course_active'
);
SET @course_teacher_index_sql = IF(
  @course_teacher_index_exists = 0,
  'CREATE INDEX idx_sch_course_teacher_course_active ON sch_course_teacher (tenant_id, course_id, deleted)',
  'SELECT 1'
);
PREPARE course_teacher_index_stmt FROM @course_teacher_index_sql;
EXECUTE course_teacher_index_stmt;
DEALLOCATE PREPARE course_teacher_index_stmt;
