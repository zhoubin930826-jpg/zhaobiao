-- 2026-05-23 incremental migration: news publish permission.
-- 执行时机：已执行过 sql/mysql8/2026-05-23-complete-release.sql 后，再部署包含资讯发布/下架权限控制的后端应用前执行。
-- 变更范围：
--   1. 新增或修正后管“发布资讯按钮”权限 NEWS_PUBLISH_BUTTON。
--   2. 默认授权给 SUPER_ADMIN、SYSTEM_ADMIN。
-- 注意：
--   - 本脚本不自动给自定义资讯角色授权，避免把发布/下架权限误发给只有新增、编辑权限的账号。
--   - 自定义资讯管理员如需发布/下架能力，请在后管角色管理中显式勾选 NEWS_PUBLISH_BUTTON。
--   - 本脚本幂等，可重复执行。

SET NAMES utf8mb4;

-- 发布资讯按钮。按钮编码同时作为后端 @PreAuthorize 权限编码使用。
INSERT INTO sys_menu (
    code, name, type, parent_id, route_path, component, icon, sort_order,
    visible, enabled, permission_code, description, created_at, updated_at
)
SELECT
    'NEWS_PUBLISH_BUTTON',
    '发布资讯按钮',
    'BUTTON',
    parent_menu.id,
    '',
    '',
    '',
    35,
    0,
    1,
    NULL,
    '发布/下架资讯按钮',
    NOW(),
    NOW()
FROM sys_menu parent_menu
WHERE parent_menu.code = 'SYSTEM_NEWS'
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    type = VALUES(type),
    parent_id = VALUES(parent_id),
    route_path = VALUES(route_path),
    component = VALUES(component),
    icon = VALUES(icon),
    sort_order = VALUES(sort_order),
    visible = VALUES(visible),
    enabled = VALUES(enabled),
    permission_code = NULL,
    description = VALUES(description),
    updated_at = NOW();

-- 默认只给内置超级管理员、系统管理员授权。自定义角色请在后管显式分配。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
JOIN sys_menu menu ON menu.code = 'NEWS_PUBLISH_BUTTON'
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu existing
      WHERE existing.role_id = role.id
        AND existing.menu_id = menu.id
  );

-- 执行后检查：
--   sys_menu.NEWS_PUBLISH_BUTTON 应为 1；
--   sys_role_menu.news_publish_builtin_roles 正常为 2，即 SUPER_ADMIN、SYSTEM_ADMIN。
--   custom_roles_with_news_publish 用来列出已拥有该按钮的自定义角色数量，正常可为 0。
SELECT 'sys_menu.NEWS_PUBLISH_BUTTON' AS check_item, COUNT(*) AS exists_count
FROM sys_menu
WHERE code = 'NEWS_PUBLISH_BUTTON'
UNION ALL
SELECT 'sys_role_menu.news_publish_builtin_roles' AS check_item, COUNT(*) AS exists_count
FROM sys_role_menu role_menu
JOIN sys_role role ON role.id = role_menu.role_id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND menu.code = 'NEWS_PUBLISH_BUTTON'
UNION ALL
SELECT 'sys_role_menu.custom_roles_with_news_publish' AS check_item, COUNT(*) AS exists_count
FROM sys_role_menu role_menu
JOIN sys_role role ON role.id = role_menu.role_id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE role.code NOT IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND menu.code = 'NEWS_PUBLISH_BUTTON';
