USE campus_evaluation_system;

-- Keep the school demo tenants usable after a partial or old database import.
INSERT INTO auth_role
  (tenant_id, role_code, role_name, scope_type, created_at, updated_at, deleted)
VALUES
  (1, 'teaching_admin', 'Teaching Administrator', 'org', NOW(), NOW(), 0),
  (1, 'service_admin', 'Service Administrator', 'org', NOW(), NOW(), 0),
  (1, 'feedback_handler', 'Feedback Handler', 'org', NOW(), NOW(), 0),
  (1, 'form_publisher', 'Form Publisher', 'org', NOW(), NOW(), 0),
  (2, 'teaching_admin', 'Teaching Administrator', 'org', NOW(), NOW(), 0),
  (2, 'service_admin', 'Service Administrator', 'org', NOW(), NOW(), 0),
  (2, 'feedback_handler', 'Feedback Handler', 'org', NOW(), NOW(), 0),
  (2, 'form_publisher', 'Form Publisher', 'org', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  scope_type = VALUES(scope_type),
  deleted = 0,
  updated_at = NOW();

INSERT INTO sch_teaching_org_unit
  (id, tenant_id, school_id, parent_id, name, code, type, status, created_at, updated_at, deleted)
VALUES
  (11, 1, 1, NULL, 'Computer Science', 'CS_DEPT', 'department', 'active', NOW(), NOW(), 0),
  (12, 1, 1, NULL, 'Business Administration', 'BIZ_DEPT', 'department', 'active', NOW(), NOW(), 0),
  (21, 2, 2, NULL, 'Computer Science', 'PKU_CS_DEPT', 'department', 'active', NOW(), NOW(), 0),
  (22, 2, 2, NULL, 'Business Administration', 'PKU_BIZ_DEPT', 'department', 'active', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  school_id = VALUES(school_id),
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  type = VALUES(type),
  status = VALUES(status),
  deleted = 0,
  updated_at = NOW();

INSERT INTO sch_service_org_unit
  (id, tenant_id, school_id, parent_id, name, code, type, status, created_at, updated_at, deleted)
VALUES
  (221, 2, 2, NULL, 'Campus Services', 'PKU_SERVICE', 'service_center', 'active', NOW(), NOW(), 0),
  (222, 2, 2, 221, 'Dining Services', 'PKU_DINING', 'service_department', 'active', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  school_id = VALUES(school_id),
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  type = VALUES(type),
  status = VALUES(status),
  deleted = 0,
  updated_at = NOW();

INSERT INTO sch_class_group
  (id, tenant_id, school_id, teaching_org_id, grade_name, class_name, status, created_at, updated_at, deleted)
VALUES
  (1001, 1, 1, 11, '2026', 'Computer Science Class 1', 'active', NOW(), NOW(), 0),
  (1002, 1, 1, 11, '2026', 'Computer Science Class 2', 'active', NOW(), NOW(), 0),
  (1003, 1, 1, 12, '2026', 'Business Administration Class 1', 'active', NOW(), NOW(), 0),
  (1004, 1, 1, 12, '2025', 'Business Administration Class 2', 'active', NOW(), NOW(), 0),
  (2001, 2, 2, 21, '2026', 'Computer Science Class 1', 'active', NOW(), NOW(), 0),
  (2002, 2, 2, 21, '2026', 'Computer Science Class 2', 'active', NOW(), NOW(), 0),
  (2003, 2, 2, 22, '2026', 'Business Administration Class 1', 'active', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  school_id = VALUES(school_id),
  teaching_org_id = VALUES(teaching_org_id),
  grade_name = VALUES(grade_name),
  class_name = VALUES(class_name),
  status = VALUES(status),
  deleted = 0,
  updated_at = NOW();

-- Give the existing demo staff accounts useful staff-portal roles.
INSERT IGNORE INTO auth_user_role
  (tenant_id, user_id, role_id, created_at, updated_at, deleted)
SELECT 1, 4, r.id, NOW(), NOW(), 0
FROM auth_role r
WHERE r.tenant_id = 1
  AND r.role_code IN ('teaching_admin', 'service_admin', 'feedback_handler', 'form_publisher')
  AND r.deleted = 0;

INSERT IGNORE INTO auth_user_role
  (tenant_id, user_id, role_id, created_at, updated_at, deleted)
SELECT 2, 6, r.id, NOW(), NOW(), 0
FROM auth_role r
WHERE r.tenant_id = 2
  AND r.role_code IN ('teaching_admin', 'service_admin', 'feedback_handler', 'form_publisher')
  AND r.deleted = 0;

UPDATE auth_person_profile
SET teaching_org_id = 11,
    service_org_id = 201,
    updated_at = NOW()
WHERE tenant_id = 1 AND user_id = 4;

UPDATE auth_person_profile
SET class_id = 1001,
    class_name = 'Computer Science Class 1',
    updated_at = NOW()
WHERE tenant_id = 1 AND user_id = 5;

UPDATE auth_person_profile
SET teaching_org_id = 21,
    service_org_id = 221,
    updated_at = NOW()
WHERE tenant_id = 2 AND user_id = 6;

UPDATE auth_person_profile
SET class_id = 2001,
    class_name = 'Computer Science Class 1',
    updated_at = NOW()
WHERE tenant_id = 2 AND user_id = 7;

INSERT IGNORE INTO sch_course_teacher
  (tenant_id, school_id, course_id, teacher_id, role_status, created_at, updated_at, deleted)
SELECT 1, 1, c.id, 4, 'active', NOW(), NOW(), 0
FROM sch_course c
WHERE c.tenant_id = 1 AND c.deleted = 0
ORDER BY c.id
LIMIT 5;

INSERT IGNORE INTO sch_course_enrollment
  (tenant_id, school_id, course_id, student_id, class_group_id, status, created_at, updated_at, deleted)
SELECT 1, 1, c.id, 5, 1001, 'active', NOW(), NOW(), 0
FROM sch_course c
WHERE c.tenant_id = 1 AND c.deleted = 0
ORDER BY c.id
LIMIT 5;
