-- 2026-05-23 menu parent and role binding fix
-- Applicable after sql/mysql8/2026-05-23-complete-release.sql has already been executed.
-- Purpose:
--   1. Restore the admin "系统管理" parent directory in the backend menu tree.
--   2. Re-parent existing system menus, including 招标管理, under SYSTEM_ROOT.
--   3. Bind SYSTEM_ROOT to roles that already have direct child menus under SYSTEM_ROOT.
-- Scope:
--   - This script repairs menu hierarchy only.
--   - It does not grant page/button capabilities other than the parent directory needed for display.
--   - It normalizes known frontend route/component metadata for existing child menus.

SET NAMES utf8mb4;
SET @fix_now = NOW();

START TRANSACTION;

-- 1. Ensure SYSTEM_ROOT exists and is enabled.
INSERT INTO sys_menu (
    code, name, type, parent_id, route_path, component, icon,
    sort_order, visible, enabled, permission_code, description, created_at, updated_at
) VALUES (
    'SYSTEM_ROOT', '系统管理', 'DIRECTORY', NULL, '/system', '', 'Setting',
    30, 1, 1, NULL, '系统管理目录', @fix_now, @fix_now
) ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    type = VALUES(type),
    parent_id = NULL,
    route_path = VALUES(route_path),
    component = VALUES(component),
    icon = VALUES(icon),
    visible = 1,
    enabled = 1,
    permission_code = NULL,
    description = VALUES(description),
    updated_at = @fix_now;

SET @menu_system_root = (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT');

-- 2. Re-parent existing system menus under SYSTEM_ROOT.
-- Production check before this fix showed SYSTEM_TENDER parent_id was NULL,
-- while other system children already pointed to SYSTEM_ROOT.
UPDATE sys_menu
SET parent_id = @menu_system_root,
    permission_code = NULL,
    updated_at = @fix_now
WHERE code IN (
    'SYSTEM_ADMIN_USER',
    'SYSTEM_MEMBER_USER',
    'SYSTEM_BUSINESS_TYPE',
    'SYSTEM_TENDER',
    'SYSTEM_NEWS',
    'SYSTEM_ROLE',
    'SYSTEM_PERMISSION',
    'SYSTEM_MENU',
    'SYSTEM_OPERATION_LOG',
    'SYSTEM_USER',
    'SYSTEM_AUDIT_RECORD'
)
  AND id <> @menu_system_root;

-- 3. Keep historical menus disabled if they exist.
UPDATE sys_menu
SET visible = 0,
    enabled = 0,
    updated_at = @fix_now
WHERE code IN ('SYSTEM_USER', 'SYSTEM_AUDIT_RECORD', 'SYSTEM_PERMISSION', 'PERMISSION_EDIT_BUTTON');

-- 4. Normalize route/component metadata for current frontend routes.
UPDATE sys_menu
SET route_path = CASE code
        WHEN 'SYSTEM_TENDER' THEN '/tenders'
        WHEN 'SYSTEM_OPERATION_LOG' THEN '/log'
        ELSE route_path
    END,
    component = CASE code
        WHEN 'SYSTEM_TENDER' THEN 'sys/tender'
        WHEN 'SYSTEM_OPERATION_LOG' THEN 'system/log'
        ELSE component
    END,
    updated_at = @fix_now
WHERE (code = 'SYSTEM_TENDER' AND (route_path <> '/tenders' OR component <> 'sys/tender'))
   OR (code = 'SYSTEM_OPERATION_LOG' AND (route_path <> '/log' OR component <> 'system/log'));

-- 5. Add only the SYSTEM_ROOT parent directory to roles that already have child menus.
-- This repairs display hierarchy without granting new business operations.
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT child_binding.role_id, @menu_system_root
FROM sys_role_menu child_binding
JOIN sys_menu child_menu ON child_menu.id = child_binding.menu_id
WHERE child_menu.parent_id = @menu_system_root
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu root_binding
      WHERE root_binding.role_id = child_binding.role_id
        AND root_binding.menu_id = @menu_system_root
  );

COMMIT;

-- Verification query 1: menu parent state.
SELECT
    menu.code,
    menu.name,
    parent.code AS parent_code,
    menu.parent_id,
    menu.route_path,
    menu.component,
    menu.sort_order,
    menu.visible,
    menu.enabled
FROM sys_menu menu
LEFT JOIN sys_menu parent ON parent.id = menu.parent_id
WHERE menu.code IN (
    'SYSTEM_ROOT',
    'SYSTEM_ADMIN_USER',
    'SYSTEM_MEMBER_USER',
    'SYSTEM_BUSINESS_TYPE',
    'SYSTEM_TENDER',
    'SYSTEM_NEWS',
    'SYSTEM_ROLE',
    'SYSTEM_PERMISSION',
    'SYSTEM_MENU',
    'SYSTEM_OPERATION_LOG'
)
ORDER BY menu.sort_order, menu.id;

-- Verification query 2: roles with SYSTEM_ROOT binding.
SELECT
    role.code AS role_code,
    role.name AS role_name,
    COUNT(*) AS system_root_binding_count
FROM sys_role role
JOIN sys_role_menu role_menu ON role_menu.role_id = role.id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE menu.code = 'SYSTEM_ROOT'
GROUP BY role.id, role.code, role.name
ORDER BY role.code;

-- Verification query 3: should return 0 rows after the fix.
SELECT
    role.code AS role_code,
    role.name AS role_name,
    GROUP_CONCAT(DISTINCT child_menu.code ORDER BY child_menu.sort_order, child_menu.id SEPARATOR ',') AS child_menus_without_root
FROM sys_role role
JOIN sys_role_menu role_menu ON role_menu.role_id = role.id
JOIN sys_menu child_menu ON child_menu.id = role_menu.menu_id
WHERE child_menu.parent_id = @menu_system_root
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu root_binding
      WHERE root_binding.role_id = role.id
        AND root_binding.menu_id = @menu_system_root
  )
GROUP BY role.id, role.code, role.name
ORDER BY role.code;
