-- 2026-05-23 production migration: news module and tender publish permission.
-- 执行时机：生产数据库先执行本 SQL，再部署包含资讯模块、后台文件查看/下载接口、招标发布权限约束的后端应用。
-- 数据库变更范围：
--   1. 新增资讯表 biz_news。
--   2. 新增后管“资讯管理”菜单和按钮，并授权给 SUPER_ADMIN、SYSTEM_ADMIN。
--   3. 新增后管“发布招标按钮”权限，并授权给 SUPER_ADMIN、SYSTEM_ADMIN。
-- 文件下载/查看接口不需要新增数据库字段或表。
-- 招标新增默认 DRAFT/未发布，biz_tender.status 为 varchar 字段，不需要改表结构。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS biz_news (
    id bigint NOT NULL AUTO_INCREMENT,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    title varchar(200) NOT NULL COMMENT '资讯标题',
    cover_file_id bigint NULL COMMENT '封面文件ID',
    content longtext NOT NULL COMMENT '资讯正文',
    publish_at datetime(6) NOT NULL COMMENT '发布时间',
    source varchar(128) NOT NULL COMMENT '信息来源',
    summary varchar(500) NOT NULL COMMENT '内容总结',
    category varchar(32) NOT NULL COMMENT '资讯分类',
    status varchar(32) NOT NULL COMMENT '资讯状态',
    created_by varchar(64) NOT NULL COMMENT '创建人',
    updated_by varchar(64) NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_biz_news_status_publish_at (status, publish_at),
    KEY idx_biz_news_category (category),
    KEY idx_biz_news_cover_file (cover_file_id),
    CONSTRAINT fk_biz_news_cover_file
        FOREIGN KEY (cover_file_id) REFERENCES biz_file_storage (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯';

-- 后管菜单：资讯管理。依赖既有 SYSTEM_ROOT 菜单。
INSERT INTO sys_menu (
    code, name, type, parent_id, route_path, component, icon, sort_order,
    visible, enabled, permission_code, description, created_at, updated_at
)
SELECT
    'SYSTEM_NEWS',
    '资讯管理',
    'MENU',
    parent_menu.id,
    '/system/news',
    'sys/news',
    'Document',
    45,
    1,
    1,
    NULL,
    '资讯管理页面',
    NOW(),
    NOW()
FROM sys_menu parent_menu
WHERE parent_menu.code = 'SYSTEM_ROOT'
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

SET @menu_system_news = (SELECT id FROM sys_menu WHERE code = 'SYSTEM_NEWS');

-- 资讯按钮菜单。按钮编码同时作为后端 @PreAuthorize 权限编码使用。
INSERT INTO sys_menu (
    code, name, type, parent_id, route_path, component, icon, sort_order,
    visible, enabled, permission_code, description, created_at, updated_at
) VALUES
    ('NEWS_CREATE_BUTTON', '新增资讯按钮', 'BUTTON', @menu_system_news, '', '', '', 10, 0, 1, NULL, '新增资讯按钮', NOW(), NOW()),
    ('NEWS_EDIT_BUTTON', '编辑资讯按钮', 'BUTTON', @menu_system_news, '', '', '', 20, 0, 1, NULL, '编辑资讯按钮', NOW(), NOW()),
    ('NEWS_DELETE_BUTTON', '删除资讯按钮', 'BUTTON', @menu_system_news, '', '', '', 30, 0, 1, NULL, '删除资讯按钮', NOW(), NOW()),
    ('NEWS_PUBLISH_BUTTON', '发布资讯按钮', 'BUTTON', @menu_system_news, '', '', '', 35, 0, 1, NULL, '发布/下架资讯按钮', NOW(), NOW()),
    ('NEWS_UPLOAD_BUTTON', '上传资讯封面按钮', 'BUTTON', @menu_system_news, '', '', '', 40, 0, 1, NULL, '上传资讯封面按钮', NOW(), NOW())
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

-- 默认给超级管理员和系统管理员授权资讯菜单/按钮。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
JOIN sys_menu menu ON menu.code IN (
    'SYSTEM_NEWS',
    'NEWS_CREATE_BUTTON',
    'NEWS_EDIT_BUTTON',
    'NEWS_DELETE_BUTTON',
    'NEWS_PUBLISH_BUTTON',
    'NEWS_UPLOAD_BUTTON'
)
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu existing
      WHERE existing.role_id = role.id
        AND existing.menu_id = menu.id
  );

-- 招标发布按钮。按钮编码同时作为后端 @PreAuthorize 权限编码使用。
INSERT INTO sys_menu (
    code, name, type, parent_id, route_path, component, icon, sort_order,
    visible, enabled, permission_code, description, created_at, updated_at
)
SELECT
    'TENDER_PUBLISH_BUTTON',
    '发布招标按钮',
    'BUTTON',
    parent_menu.id,
    '',
    '',
    '',
    35,
    0,
    1,
    NULL,
    '发布/改为未发布招标按钮',
    NOW(),
    NOW()
FROM sys_menu parent_menu
WHERE parent_menu.code = 'SYSTEM_TENDER'
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

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
JOIN sys_menu menu ON menu.code = 'TENDER_PUBLISH_BUTTON'
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu existing
      WHERE existing.role_id = role.id
        AND existing.menu_id = menu.id
  );

-- 执行后检查：biz_news 应为 1；SYSTEM_NEWS 应为 1；NEWS_PUBLISH_BUTTON 应为 1；TENDER_PUBLISH_BUTTON 应为 1；
-- news_roles 正常情况下为 12（2 个角色 * 6 个菜单/按钮）。
SELECT 'biz_news' AS check_item, COUNT(*) AS exists_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'biz_news'
UNION ALL
SELECT 'sys_menu.SYSTEM_NEWS' AS check_item, COUNT(*) AS exists_count
FROM sys_menu
WHERE code = 'SYSTEM_NEWS'
UNION ALL
SELECT 'sys_menu.TENDER_PUBLISH_BUTTON' AS check_item, COUNT(*) AS exists_count
FROM sys_menu
WHERE code = 'TENDER_PUBLISH_BUTTON'
UNION ALL
SELECT 'sys_menu.NEWS_PUBLISH_BUTTON' AS check_item, COUNT(*) AS exists_count
FROM sys_menu
WHERE code = 'NEWS_PUBLISH_BUTTON'
UNION ALL
SELECT 'sys_role_menu.news_roles' AS check_item, COUNT(*) AS exists_count
FROM sys_role_menu role_menu
JOIN sys_role role ON role.id = role_menu.role_id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND menu.code IN ('SYSTEM_NEWS', 'NEWS_CREATE_BUTTON', 'NEWS_EDIT_BUTTON', 'NEWS_DELETE_BUTTON', 'NEWS_PUBLISH_BUTTON', 'NEWS_UPLOAD_BUTTON')
UNION ALL
SELECT 'sys_role_menu.tender_publish_roles' AS check_item, COUNT(*) AS exists_count
FROM sys_role_menu role_menu
JOIN sys_role role ON role.id = role_menu.role_id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND menu.code = 'TENDER_PUBLISH_BUTTON';
