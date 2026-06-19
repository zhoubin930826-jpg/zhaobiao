-- Member registration switch migration for MySQL 8.
-- Run before deploying backend code that reads sys_application_setting.

SET NAMES utf8mb4;
SET @migration_now = NOW();

CREATE TABLE IF NOT EXISTS sys_application_setting (
    id BIGINT NOT NULL AUTO_INCREMENT,
    setting_key VARCHAR(64) NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_application_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_application_setting (setting_key, setting_value, description, created_at, updated_at)
SELECT
    'portal.member.registration.enabled',
    'true',
    '门户会员自助注册开关，true 表示允许注册，false 表示关闭注册',
    @migration_now,
    @migration_now
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_application_setting
    WHERE setting_key = 'portal.member.registration.enabled'
);

SET @menu_system_member_user = (
    SELECT id
    FROM sys_menu
    WHERE code = 'SYSTEM_MEMBER_USER'
    LIMIT 1
);

INSERT INTO sys_menu (
    code,
    name,
    type,
    parent_id,
    route_path,
    component,
    icon,
    sort_order,
    visible,
    enabled,
    permission_code,
    description,
    created_at,
    updated_at
)
SELECT
    'MEMBER_REGISTRATION_SETTING_BUTTON',
    '会员注册开关按钮',
    'BUTTON',
    @menu_system_member_user,
    '',
    '',
    '',
    9,
    0,
    1,
    NULL,
    '开启或关闭门户会员自助注册按钮',
    @migration_now,
    @migration_now
WHERE @menu_system_member_user IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE code = 'MEMBER_REGISTRATION_SETTING_BUTTON'
  );

UPDATE sys_menu
SET parent_id = @menu_system_member_user,
    name = '会员注册开关按钮',
    type = 'BUTTON',
    route_path = '',
    component = '',
    icon = '',
    sort_order = 9,
    visible = 0,
    enabled = 1,
    permission_code = NULL,
    description = '开启或关闭门户会员自助注册按钮',
    updated_at = @migration_now
WHERE code = 'MEMBER_REGISTRATION_SETTING_BUTTON'
  AND @menu_system_member_user IS NOT NULL;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
JOIN sys_menu menu ON menu.code = 'MEMBER_REGISTRATION_SETTING_BUTTON'
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN');

SELECT 'sys_application_setting' AS check_item,
       COUNT(*) AS exists_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'sys_application_setting';

SELECT 'portal.member.registration.enabled' AS check_item,
       setting_value
FROM sys_application_setting
WHERE setting_key = 'portal.member.registration.enabled';

SELECT 'MEMBER_REGISTRATION_SETTING_BUTTON roles' AS check_item,
       COUNT(*) AS role_count
FROM sys_role_menu role_menu
JOIN sys_role role ON role.id = role_menu.role_id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND menu.code = 'MEMBER_REGISTRATION_SETTING_BUTTON';
