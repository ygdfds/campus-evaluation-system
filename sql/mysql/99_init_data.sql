USE campus_evaluation_system;

-- Minimal runnable seed data.
-- Password values are BCrypt hashes. Replace them according to the backend password policy before production deployment.

INSERT IGNORE INTO pf_tenant_plan (id, plan_code, plan_name, features_json, status, created_at, updated_at, deleted) VALUES
(1, 'basic', '基础版', JSON_OBJECT('maxUsers', 5000, 'modules', JSON_ARRAY('evaluation','complaint','appeal','announcement','help')), 'active', NOW(), NOW(), 0);

INSERT IGNORE INTO pf_tenant (id, tenant_code, plan_id, school_name, status, created_at, updated_at, deleted) VALUES
(1, 'tenant_demo_tsinghua', 1, '清华大学', 'active', NOW(), NOW(), 0);

INSERT IGNORE INTO sch_school_profile (id, tenant_id, name, address, website, logo_file_id, cover_file_id, intro, status, created_at, updated_at, deleted) VALUES
(1, 1, '清华大学', '北京市海淀区清华园', 'https://www.tsinghua.edu.cn', NULL, NULL, '校园服务质量在线评测系统演示学校。', 'active', NOW(), NOW(), 0);

INSERT IGNORE INTO auth_user_account (id, tenant_id, username, password_hash, phone, email, status, must_change_password, created_at, updated_at, deleted) VALUES
(1, NULL, 'sys_admin', '$2a$10$uY1sYwVp3vN4lN1xXwqG4e1K3Zb9pHndWjG0rW44E1oNk7V1qV9hS', '13000000000', 'sysadmin@example.com', 'active', 0, NOW(), NOW(), 0),
(101, 1, 'school_admin', '$2a$10$uY1sYwVp3vN4lN1xXwqG4e1K3Zb9pHndWjG0rW44E1oNk7V1qV9hS', '13100000000', 'schooladmin@example.com', 'active', 0, NOW(), NOW(), 0),
(102, 1, 'teacher_li', '$2a$10$uY1sYwVp3vN4lN1xXwqG4e1K3Zb9pHndWjG0rW44E1oNk7V1qV9hS', '13200000000', 'teacherli@example.com', 'active', 0, NOW(), NOW(), 0),
(103, 1, 'student_zhang', '$2a$10$uY1sYwVp3vN4lN1xXwqG4e1K3Zb9pHndWjG0rW44E1oNk7V1qV9hS', '13300000000', 'studentzhang@example.com', 'active', 0, NOW(), NOW(), 0);

INSERT IGNORE INTO auth_person_profile (id, tenant_id, user_id, real_name, role_type, no_work, no_student, gender, office_phone, intro, org_unit_id, department_name, class_name, created_at, updated_at, deleted) VALUES
(1, NULL, 1, '系统管理员', 'system_admin', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NOW(), NOW(), 0),
(101, 1, 101, '赵主任', 'school_admin', 'A2026001', NULL, 'male', '010-00000001', '学校管理员', NULL, '校级管理', NULL, NOW(), NOW(), 0),
(102, 1, 102, '李老师', 'staff', 'T2026001', NULL, 'female', '010-00000002', '教学与服务评价管理员', 1, '计算机科学与技术系', NULL, NOW(), NOW(), 0),
(103, 1, 103, '张同学', 'student', NULL, 'S2026001', 'male', NULL, NULL, 1, '计算机科学与技术系', '计科 2026-1 班', NOW(), NOW(), 0);

INSERT IGNORE INTO auth_role (id, tenant_id, role_code, role_name, scope_type, created_at, updated_at, deleted) VALUES
(1, NULL, 'system_admin', '系统管理员', 'platform', NOW(), NOW(), 0),
(2, 1, 'school_admin', '学校管理员', 'school', NOW(), NOW(), 0),
(3, 1, 'student', '学生', 'tenant', NOW(), NOW(), 0),
(4, 1, 'staff', '教职工', 'tenant', NOW(), NOW(), 0),
(5, 1, 'teaching_admin', '学院教学管理员', 'org', NOW(), NOW(), 0),
(6, 1, 'service_admin', '后勤部门管理员', 'org', NOW(), NOW(), 0),
(7, 1, 'feedback_handler', '反馈处理员', 'org', NOW(), NOW(), 0),
(8, 1, 'form_publisher', '评价表单发布员', 'org', NOW(), NOW(), 0);

INSERT IGNORE INTO auth_permission (id, permission_code, permission_name, resource_action, created_at, updated_at, deleted) VALUES
(1, 'admin.dashboard.view', '平台概览查看', 'admin:dashboard:view', NOW(), NOW(), 0),
(2, 'school.dashboard.view', '学校概览查看', 'school:dashboard:view', NOW(), NOW(), 0),
(3, 'school.audit.manage', '审核中心管理', 'school:audit:manage', NOW(), NOW(), 0),
(4, 'school.user.manage', '学校用户管理', 'school:user:manage', NOW(), NOW(), 0),
(5, 'school.org.manage', '组织架构管理', 'school:org:manage', NOW(), NOW(), 0),
(6, 'school.announcement.manage', '公告管理', 'school:announcement:manage', NOW(), NOW(), 0),
(7, 'school.help.manage', '帮助管理', 'school:help:manage', NOW(), NOW(), 0),
(8, 'staff.evaluation.manage', '教职工评价管理', 'staff:evaluation:manage', NOW(), NOW(), 0),
(9, 'staff.feedback.manage', '反馈处理', 'staff:feedback:manage', NOW(), NOW(), 0),
(10, 'staff.appeal.manage', '申诉处理', 'staff:appeal:manage', NOW(), NOW(), 0),
(11, 'student.evaluation.submit', '学生评价提交', 'student:evaluation:submit', NOW(), NOW(), 0),
(12, 'student.complaint.submit', '学生投诉建议', 'student:complaint:submit', NOW(), NOW(), 0);

INSERT IGNORE INTO auth_user_role (id, tenant_id, user_id, role_id, scope_json, created_at, updated_at, deleted) VALUES
(1, NULL, 1, 1, NULL, NOW(), NOW(), 0),
(2, 1, 101, 2, JSON_OBJECT('schoolIds', JSON_ARRAY(1)), NOW(), NOW(), 0),
(3, 1, 102, 4, NULL, NOW(), NOW(), 0),
(4, 1, 102, 5, JSON_OBJECT('teachingOrgIds', JSON_ARRAY(1)), NOW(), NOW(), 0),
(5, 1, 102, 6, JSON_OBJECT('serviceOrgIds', JSON_ARRAY(1)), NOW(), NOW(), 0),
(6, 1, 102, 7, JSON_OBJECT('serviceOrgIds', JSON_ARRAY(1)), NOW(), NOW(), 0),
(7, 1, 102, 8, JSON_OBJECT('teachingOrgIds', JSON_ARRAY(1), 'serviceOrgIds', JSON_ARRAY(1)), NOW(), NOW(), 0),
(8, 1, 103, 3, NULL, NOW(), NOW(), 0);

INSERT IGNORE INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
SELECT 1, id, NOW(), NOW(), 0 FROM auth_permission WHERE permission_code LIKE 'admin.%';
INSERT IGNORE INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
SELECT 2, id, NOW(), NOW(), 0 FROM auth_permission WHERE permission_code LIKE 'school.%';
INSERT IGNORE INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
SELECT 3, id, NOW(), NOW(), 0 FROM auth_permission WHERE permission_code LIKE 'student.%';
INSERT IGNORE INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
SELECT 4, id, NOW(), NOW(), 0 FROM auth_permission WHERE permission_code LIKE 'staff.%';
INSERT IGNORE INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
SELECT 5, id, NOW(), NOW(), 0 FROM auth_permission WHERE permission_code IN ('staff.evaluation.manage','staff.appeal.manage');
INSERT IGNORE INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
SELECT 6, id, NOW(), NOW(), 0 FROM auth_permission WHERE permission_code IN ('staff.feedback.manage');
INSERT IGNORE INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
SELECT 7, id, NOW(), NOW(), 0 FROM auth_permission WHERE permission_code IN ('staff.feedback.manage');
INSERT IGNORE INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
SELECT 8, id, NOW(), NOW(), 0 FROM auth_permission WHERE permission_code IN ('staff.evaluation.manage');

INSERT IGNORE INTO sch_teaching_org_unit (id, tenant_id, school_id, parent_id, name, code, type, status, created_at, updated_at, deleted) VALUES
(1, 1, 1, NULL, '计算机科学与技术系', 'CS', 'department', 'active', NOW(), NOW(), 0),
(2, 1, 1, NULL, '电子工程系', 'EE', 'department', 'active', NOW(), NOW(), 0);

INSERT IGNORE INTO sch_service_org_unit (id, tenant_id, school_id, parent_id, name, code, type, status, created_at, updated_at, deleted) VALUES
(1, 1, 1, NULL, '后勤服务中心', 'LOGISTICS', 'group', 'active', NOW(), NOW(), 0),
(2, 1, 1, 1, '食堂服务部', 'CANTEEN', 'department', 'active', NOW(), NOW(), 0),
(3, 1, 1, 1, '宿舍管理部', 'DORM', 'department', 'active', NOW(), NOW(), 0);

INSERT IGNORE INTO sch_class_group (id, tenant_id, school_id, teaching_org_id, grade_name, class_name, status, created_at, updated_at, deleted) VALUES
(1, 1, 1, 1, '2026级', '计科 2026-1 班', 'active', NOW(), NOW(), 0);

INSERT IGNORE INTO sch_course (id, tenant_id, school_id, teaching_org_id, course_code, course_name, term, start_at, end_at, created_at, updated_at, deleted) VALUES
(1, 1, 1, 1, 'CS101', '程序设计基础', '2026春季学期', '2026-03-01 00:00:00', '2026-07-01 00:00:00', NOW(), NOW(), 0);

INSERT IGNORE INTO sch_course_teacher (id, tenant_id, school_id, course_id, teacher_id, role_status, created_at, updated_at, deleted) VALUES
(1, 1, 1, 1, 102, 'active', NOW(), NOW(), 0);

INSERT IGNORE INTO sch_course_enrollment (id, tenant_id, school_id, course_id, student_id, class_group_id, status, created_at, updated_at, deleted) VALUES
(1, 1, 1, 1, 103, 1, 'active', NOW(), NOW(), 0);

INSERT IGNORE INTO sch_service_item (id, tenant_id, school_id, service_org_id, name, cover_file_id, type, status, created_at, updated_at, deleted) VALUES
(1, 1, 1, 2, '食堂服务满意度评价', NULL, 'canteen', 'active', NOW(), NOW(), 0),
(2, 1, 1, 3, '学生宿舍管理服务评价', NULL, 'dormitory', 'active', NOW(), NOW(), 0);

INSERT IGNORE INTO msg_announcement (id, tenant_id, school_id, title, summary, content, tag, cover_file_id, target_roles, status, publish_time, publisher_id, created_at, updated_at, deleted) VALUES
(1, 1, 1, '关于开展校园服务质量评价的通知', '请师生在评价窗口内完成相关服务质量评价。', '为持续改进校园服务质量，学校将开展本学期服务质量评价工作，请师生按时完成评价任务。', '评价安排', NULL, JSON_ARRAY('student','staff'), 'published', NOW(), 101, NOW(), NOW(), 0);

INSERT IGNORE INTO msg_help_faq (id, tenant_id, school_id, question, answer, category, target_roles, keywords, sort_order, enabled, status, created_at, updated_at, deleted) VALUES
(1, 1, 1, '如何提交评价？', '进入评价中心，选择进行中的评价任务，填写并提交即可。', '评价管理', JSON_ARRAY('student'), '评价,提交,任务', 1, 1, 'enabled', NOW(), NOW(), 0),
(2, 1, 1, '如何处理学生反馈？', '教职工进入反馈处理页面，查看详情后可受理、更新进度或办结。', '反馈处理', JSON_ARRAY('staff'), '反馈,处理,办结', 2, 1, 'enabled', NOW(), NOW(), 0);

INSERT IGNORE INTO msg_help_guide (id, tenant_id, school_id, title, module, summary, steps, target_roles, related_link, sort_order, enabled, created_at, updated_at, deleted) VALUES
(1, 1, 1, '学生评价提交流程', 'evaluation', '查看评价任务并完成提交。', JSON_ARRAY('进入评价中心','选择评价任务','填写题目并提交'), JSON_ARRAY('student'), '/student/evaluations', 1, 1, NOW(), NOW(), 0),
(2, 1, 1, '教职工反馈处理流程', 'feedback', '处理授权范围内的反馈工单。', JSON_ARRAY('进入反馈处理','查看反馈详情','受理并更新处理结果'), JSON_ARRAY('staff'), '/staff/feedback', 2, 1, NOW(), NOW(), 0);

INSERT IGNORE INTO eval_form (id, tenant_id, school_id, type, title, description, publisher_id, publish_scope, anonymous, service_item_id, status, created_at, updated_at, deleted) VALUES
(1, 1, 1, 'service', '食堂服务满意度评价', '用于收集学生对食堂服务的评价。', 102, 'school', 1, 1, 'published', NOW(), NOW(), 0);

INSERT IGNORE INTO eval_question (id, tenant_id, school_id, form_id, type, title, required, max_score, min_length, sort_order, created_at, updated_at, deleted) VALUES
(1, 1, 1, 1, 'rating', '请评价食堂整体服务质量', 1, 5, NULL, 1, NOW(), NOW(), 0),
(2, 1, 1, 1, 'text', '请填写意见或建议', 0, NULL, 5, 2, NOW(), NOW(), 0);

INSERT IGNORE INTO eval_window (id, tenant_id, school_id, form_id, type, start_at, end_at, modifiable_hours, status, created_at, updated_at, deleted) VALUES
(1, 1, 1, 1, 'service', '2026-06-01 00:00:00', '2026-07-31 23:59:59', 24, 'open', NOW(), NOW(), 0);

INSERT IGNORE INTO log_operation_log (id, tenant_id, school_id, user_id, module, action, target_type, target_id, target_name, content, result, ip, device, created_at, deleted) VALUES
(1, 1, 1, 101, 'system', 'init', 'tenant', 1, '清华大学', '初始化演示租户基础数据', 'success', '-', '-', NOW(), 0);
