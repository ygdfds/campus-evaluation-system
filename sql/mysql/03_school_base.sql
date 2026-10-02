USE campus_evaluation_system;

-- School base domain: school profile, teaching structure, service structure,
-- courses, class groups, enrollments, and service items.

CREATE TABLE IF NOT EXISTS sch_school_profile (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '学校资料ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  name VARCHAR(180) NOT NULL COMMENT '学校名称',
  address VARCHAR(255) NULL COMMENT '地址',
  website VARCHAR(255) NULL COMMENT '官网',
  logo_file_id BIGINT NULL COMMENT 'Logo文件ID',
  cover_file_id BIGINT NULL COMMENT '封面文件ID',
  intro VARCHAR(2000) NULL COMMENT '学校简介',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sch_school_tenant (tenant_id),
  KEY idx_sch_school_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学校资料';

CREATE TABLE IF NOT EXISTS sch_teaching_org_unit (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '教学组织ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  parent_id BIGINT NULL COMMENT '父级ID',
  name VARCHAR(120) NOT NULL COMMENT '组织名称',
  code VARCHAR(64) NOT NULL COMMENT '组织编码',
  type VARCHAR(32) NOT NULL COMMENT 'school/college/department/major',
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sch_teaching_org_code (tenant_id, code),
  KEY idx_sch_teaching_org_parent (tenant_id, parent_id, deleted),
  KEY idx_sch_teaching_org_type (tenant_id, type, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教学组织';

CREATE TABLE IF NOT EXISTS sch_service_org_unit (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务组织ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  parent_id BIGINT NULL COMMENT '父级ID',
  name VARCHAR(120) NOT NULL COMMENT '组织名称',
  code VARCHAR(64) NOT NULL COMMENT '组织编码',
  type VARCHAR(32) NOT NULL COMMENT 'group/department/window',
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sch_service_org_code (tenant_id, code),
  KEY idx_sch_service_org_parent (tenant_id, parent_id, deleted),
  KEY idx_sch_service_org_type (tenant_id, type, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务组织';

CREATE TABLE IF NOT EXISTS sch_class_group (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  teaching_org_id BIGINT NOT NULL COMMENT '所属教学组织ID',
  grade_name VARCHAR(64) NULL COMMENT '年级',
  class_name VARCHAR(120) NOT NULL COMMENT '班级名称',
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_sch_class_org (tenant_id, teaching_org_id, deleted),
  KEY idx_sch_class_status (tenant_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级';

CREATE TABLE IF NOT EXISTS sch_course (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  teaching_org_id BIGINT NOT NULL COMMENT '教学组织ID',
  course_code VARCHAR(64) NOT NULL COMMENT '课程编码',
  course_name VARCHAR(160) NOT NULL COMMENT '课程名称',
  term VARCHAR(64) NOT NULL COMMENT '学期',
  start_at DATETIME NULL COMMENT '开始时间',
  end_at DATETIME NULL COMMENT '结束时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sch_course_code (tenant_id, course_code),
  KEY idx_sch_course_org (tenant_id, teaching_org_id, deleted),
  KEY idx_sch_course_term (tenant_id, term, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程';

CREATE TABLE IF NOT EXISTS sch_course_teacher (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程教师关系ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  teacher_id BIGINT NOT NULL COMMENT '教师用户ID',
  role_status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '任课状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sch_course_teacher (tenant_id, course_id, teacher_id),
  KEY idx_sch_course_teacher_teacher (tenant_id, teacher_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程教师';

CREATE TABLE IF NOT EXISTS sch_course_enrollment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '选课ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  student_id BIGINT NOT NULL COMMENT '学生用户ID',
  class_group_id BIGINT NULL COMMENT '班级ID',
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sch_course_enrollment (tenant_id, course_id, student_id),
  KEY idx_sch_enrollment_student (tenant_id, student_id, deleted),
  KEY idx_sch_enrollment_class (tenant_id, class_group_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生选课';

CREATE TABLE IF NOT EXISTS sch_service_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务项目ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  service_org_id BIGINT NOT NULL COMMENT '服务组织ID',
  name VARCHAR(160) NOT NULL COMMENT '服务项目名称',
  cover_file_id BIGINT NULL COMMENT '封面文件ID',
  type VARCHAR(64) NOT NULL COMMENT '服务类型',
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_sch_service_item_org (tenant_id, service_org_id, deleted),
  KEY idx_sch_service_item_type (tenant_id, type, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目';
