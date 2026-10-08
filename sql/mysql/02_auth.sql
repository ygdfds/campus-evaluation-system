USE campus_evaluation_system;

-- Authentication and authorization domain. Tenant-scoped roles use tenant_id;
-- platform roles may keep tenant_id NULL.

CREATE TABLE IF NOT EXISTS auth_user_account (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户账号ID',
  tenant_id BIGINT NULL COMMENT '租户ID，平台账号可为空',
  username VARCHAR(80) NOT NULL COMMENT '用户名',
  password_hash VARCHAR(120) NOT NULL COMMENT 'BCrypt密码哈希',
  phone VARCHAR(32) NULL COMMENT '手机号',
  email VARCHAR(120) NULL COMMENT '邮箱',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT 'active/disabled/locked',
  must_change_password TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否必须修改密码',
  last_login_at DATETIME NULL COMMENT '最近登录时间',
  avatar_file_id BIGINT NULL COMMENT '头像文件ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_user_username (username),
  UNIQUE KEY uk_auth_user_phone (phone),
  KEY idx_auth_user_tenant (tenant_id, deleted),
  KEY idx_auth_user_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号';

CREATE TABLE IF NOT EXISTS auth_person_profile (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '人员档案ID',
  tenant_id BIGINT NULL COMMENT '租户ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  real_name VARCHAR(80) NOT NULL COMMENT '真实姓名',
  role_type VARCHAR(32) NOT NULL COMMENT 'student/staff/school_admin/system_admin',
  no_work VARCHAR(64) NULL COMMENT '工号',
  no_student VARCHAR(64) NULL COMMENT '学号',
  gender VARCHAR(16) NULL COMMENT '性别',
  office_phone VARCHAR(32) NULL COMMENT '办公电话',
  intro VARCHAR(1000) NULL COMMENT '个人简介',
  avatar_file_id BIGINT NULL COMMENT '头像文件ID',
  org_unit_id BIGINT NULL COMMENT '组织ID',
  department_name VARCHAR(120) NULL COMMENT '部门名称快照',
  class_name VARCHAR(120) NULL COMMENT '班级名称快照',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_profile_user (user_id),
  KEY idx_auth_profile_tenant_role (tenant_id, role_type, deleted),
  KEY idx_auth_profile_work_no (tenant_id, no_work),
  KEY idx_auth_profile_student_no (tenant_id, no_student)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员档案';

CREATE TABLE IF NOT EXISTS auth_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  tenant_id BIGINT NULL COMMENT '租户ID，平台角色可为空',
  role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
  role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
  scope_type VARCHAR(32) NOT NULL DEFAULT 'tenant' COMMENT 'platform/tenant/school/org',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_role_tenant_code (tenant_id, role_code),
  KEY idx_auth_role_scope (scope_type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色';

CREATE TABLE IF NOT EXISTS auth_permission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  permission_code VARCHAR(120) NOT NULL COMMENT '权限编码',
  permission_name VARCHAR(120) NOT NULL COMMENT '权限名称',
  resource_action VARCHAR(120) NULL COMMENT '资源动作',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_permission_code (permission_code),
  KEY idx_auth_permission_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限';

CREATE TABLE IF NOT EXISTS auth_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户角色ID',
  tenant_id BIGINT NULL COMMENT '租户ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  scope_json JSON NULL COMMENT '授权范围JSON，如组织范围',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_user_role (tenant_id, user_id, role_id),
  KEY idx_auth_user_role_user (user_id, deleted),
  KEY idx_auth_user_role_role (role_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关系';

CREATE TABLE IF NOT EXISTS auth_role_permission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色权限ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_role_permission (role_id, permission_id),
  KEY idx_auth_role_permission_perm (permission_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关系';

CREATE TABLE IF NOT EXISTS auth_login_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '登录日志ID',
  tenant_id BIGINT NULL COMMENT '租户ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  ip VARCHAR(64) NULL COMMENT 'IP',
  device VARCHAR(255) NULL COMMENT '设备',
  location VARCHAR(120) NULL COMMENT '位置',
  result VARCHAR(32) NOT NULL COMMENT '登录结果',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_auth_login_user (user_id, created_at),
  KEY idx_auth_login_tenant (tenant_id, created_at),
  KEY idx_auth_login_result (result, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志';
