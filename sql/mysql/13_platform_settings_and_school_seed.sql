USE campus_evaluation_system;

CREATE TABLE IF NOT EXISTS pf_system_setting (
  id BIGINT NOT NULL AUTO_INCREMENT,
  setting_key VARCHAR(120) NOT NULL,
  setting_value TEXT NULL,
  updated_by BIGINT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pf_system_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台全局设置';

INSERT INTO pf_system_setting
  (setting_key, setting_value, updated_by, updated_at, deleted)
VALUES
  ('onboardingNotificationTemplate', '您的学校入驻申请已提交，请等待平台审核。', 1, NOW(), 0),
  ('expirationWarningTemplate', '您的套餐即将到期，请及时联系平台管理员续费。', 1, NOW(), 0),
  ('sensitiveWords', '', 1, NOW(), 0),
  ('extremeLowScoreThreshold', '2', 1, NOW(), 0),
  ('manualReviewEnabled', 'true', 1, NOW(), 0),
  ('maxFileSize', '50', 1, NOW(), 0),
  ('attachmentExpiryDays', '180', 1, NOW(), 0)
ON DUPLICATE KEY UPDATE
  setting_value = VALUES(setting_value),
  updated_by = VALUES(updated_by),
  updated_at = NOW(),
  deleted = 0;

INSERT INTO sch_course
  (id, tenant_id, school_id, teaching_org_id, course_code, course_name, term, start_at, end_at, status, created_at, updated_at, deleted)
VALUES
  (101, 1, 1, 1, 'CS101', '程序设计基础', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (102, 1, 1, 1, 'CS102', '数据结构', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (103, 1, 1, 1, 'CS201', '数据库系统原理', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (104, 1, 1, 1, 'CS202', '操作系统', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (105, 1, 1, 1, 'CS203', '计算机网络', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (106, 1, 1, 1, 'CS204', '软件工程', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (107, 1, 1, 1, 'CS205', 'Web应用开发', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (108, 1, 1, 1, 'CS206', 'Java程序设计', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (109, 1, 1, 1, 'CS207', 'Python数据分析', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (110, 1, 1, 1, 'CS208', '人工智能导论', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (111, 1, 1, 1, 'CS209', '机器学习基础', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (112, 1, 1, 1, 'CS210', '信息安全基础', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (113, 1, 1, 1, 'CS211', '移动应用开发', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (114, 1, 1, 1, 'CS212', '云计算与大数据', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0),
  (115, 1, 1, 1, 'CS213', '项目实践', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', 'active', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  school_id = VALUES(school_id),
  teaching_org_id = VALUES(teaching_org_id),
  course_name = VALUES(course_name),
  term = VALUES(term),
  start_at = VALUES(start_at),
  end_at = VALUES(end_at),
  status = VALUES(status),
  updated_at = NOW(),
  deleted = 0;

INSERT INTO sch_service_org_unit
  (id, tenant_id, school_id, parent_id, name, code, type, status, created_at, updated_at, deleted)
VALUES
  (201, 1, 1, NULL, '餐饮服务', 'SERVICE_CANTEEN', 'department', 'active', NOW(), NOW(), 0),
  (202, 1, 1, NULL, '宿舍服务', 'SERVICE_DORMITORY', 'department', 'active', NOW(), NOW(), 0),
  (203, 1, 1, NULL, '图书馆服务', 'SERVICE_LIBRARY', 'department', 'active', NOW(), NOW(), 0),
  (204, 1, 1, NULL, '校园网络', 'SERVICE_NETWORK', 'department', 'active', NOW(), NOW(), 0),
  (205, 1, 1, NULL, '教务服务', 'SERVICE_ACADEMIC', 'department', 'active', NOW(), NOW(), 0),
  (206, 1, 1, NULL, '后勤维修', 'SERVICE_MAINTENANCE', 'department', 'active', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  school_id = VALUES(school_id),
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  type = VALUES(type),
  status = VALUES(status),
  updated_at = NOW(),
  deleted = 0;

INSERT INTO sch_service_item
  (id, tenant_id, school_id, service_org_id, name, cover_file_id, type, status, created_at, updated_at, deleted)
VALUES
  (301, 1, 1, 201, '食堂餐饮', NULL, 'logistics', 'active', NOW(), NOW(), 0),
  (302, 1, 1, 201, '食品卫生', NULL, 'logistics', 'active', NOW(), NOW(), 0),
  (303, 1, 1, 202, '宿舍报修', NULL, 'logistics', 'active', NOW(), NOW(), 0),
  (304, 1, 1, 202, '宿舍管理', NULL, 'logistics', 'active', NOW(), NOW(), 0),
  (305, 1, 1, 203, '图书借阅', NULL, 'service', 'active', NOW(), NOW(), 0),
  (306, 1, 1, 203, '自习空间', NULL, 'service', 'active', NOW(), NOW(), 0),
  (307, 1, 1, 204, '校园网络', NULL, 'service', 'active', NOW(), NOW(), 0),
  (308, 1, 1, 204, '信息化平台', NULL, 'service', 'active', NOW(), NOW(), 0),
  (309, 1, 1, 205, '教务咨询', NULL, 'service', 'active', NOW(), NOW(), 0),
  (310, 1, 1, 205, '成绩与学籍服务', NULL, 'service', 'active', NOW(), NOW(), 0),
  (311, 1, 1, 206, '教室设备维修', NULL, 'logistics', 'active', NOW(), NOW(), 0),
  (312, 1, 1, 206, '校园卫生保洁', NULL, 'logistics', 'active', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  school_id = VALUES(school_id),
  service_org_id = VALUES(service_org_id),
  name = VALUES(name),
  cover_file_id = VALUES(cover_file_id),
  type = VALUES(type),
  status = VALUES(status),
  updated_at = NOW(),
  deleted = 0;
