USE campus_evaluation_system;

-- Phase 4: 用户管理 - 为 auth_person_profile 添加组织绑定字段
ALTER TABLE auth_person_profile
  ADD COLUMN teaching_org_id BIGINT NULL COMMENT '教学组织ID' AFTER org_unit_id,
  ADD COLUMN service_org_id BIGINT NULL COMMENT '服务组织ID' AFTER teaching_org_id,
  ADD COLUMN class_id BIGINT NULL COMMENT '班级ID' AFTER service_org_id,
  ADD KEY idx_auth_profile_teaching_org (tenant_id, teaching_org_id),
  ADD KEY idx_auth_profile_service_org (tenant_id, service_org_id),
  ADD KEY idx_auth_profile_class (tenant_id, class_id);
