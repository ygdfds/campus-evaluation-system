USE campus_evaluation_system;

SELECT 'table_count' AS item, COUNT(*) AS value
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE();

SELECT table_name, column_name, is_nullable, column_type
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND table_name IN (
    'pf_tenant', 'auth_user_account', 'auth_role', 'auth_permission',
    'auth_person_profile', 'sch_school_profile', 'eval_form',
    'file_resource', 'sys_schema_migration'
  )
ORDER BY table_name, ordinal_position;

SELECT 'roles' AS item, GROUP_CONCAT(role_code ORDER BY role_code) AS value
FROM auth_role
WHERE deleted = 0;

SELECT 'active_tenants' AS item, COUNT(*) AS value
FROM pf_tenant
WHERE status = 'active' AND deleted = 0;

SELECT version, description, applied_at
FROM sys_schema_migration
ORDER BY version;
