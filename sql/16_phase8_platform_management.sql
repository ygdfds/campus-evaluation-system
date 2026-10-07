USE campus_evaluation_system;

CREATE TABLE IF NOT EXISTS pf_tenant_status_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '租户状态变更日志ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  operator_id BIGINT NULL COMMENT '操作人ID',
  from_status VARCHAR(32) NULL COMMENT '原状态',
  to_status VARCHAR(32) NOT NULL COMMENT '新状态',
  reason VARCHAR(1000) NULL COMMENT '原因',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_pf_tenant_status_log_tenant (tenant_id, created_at),
  KEY idx_pf_tenant_status_log_operator (operator_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户状态变更日志';

SET @schema_name = DATABASE();

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE pf_tenant_plan ADD COLUMN description VARCHAR(500) NULL COMMENT ''套餐说明'' AFTER plan_name',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'pf_tenant_plan' AND COLUMN_NAME = 'description'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE pf_tenant_plan ADD COLUMN price DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''年费价格'' AFTER features_json',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'pf_tenant_plan' AND COLUMN_NAME = 'price'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE pf_tenant ADD COLUMN frozen_reason VARCHAR(1000) NULL COMMENT ''冻结或停用原因'' AFTER status',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'pf_tenant' AND COLUMN_NAME = 'frozen_reason'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT IGNORE INTO auth_permission (permission_code, permission_name, resource_action, created_at, updated_at, deleted) VALUES
('admin.tenant.manage', '租户管理', 'platform:tenant:*', NOW(), NOW(), 0),
('admin.onboarding.audit', '入驻审核', 'platform:onboarding:audit', NOW(), NOW(), 0),
('admin.plan.manage', '套餐管理', 'platform:plan:*', NOW(), NOW(), 0),
('admin.role.manage', '平台角色管理', 'platform:role:*', NOW(), NOW(), 0),
('admin.audit.view', '审计查看', 'platform:audit:view', NOW(), NOW(), 0),
('admin.monitor.view', '监控查看', 'platform:monitor:view', NOW(), NOW(), 0);

INSERT IGNORE INTO auth_role_permission (role_id, permission_id, created_at, updated_at, deleted)
SELECT r.id, p.id, NOW(), NOW(), 0
FROM auth_role r
JOIN auth_permission p ON p.permission_code LIKE 'admin.%' AND p.deleted = 0
WHERE r.role_code = 'system_admin' AND r.deleted = 0;
