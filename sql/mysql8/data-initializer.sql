-- DataInitializer manual seed SQL for MySQL 8
-- Source: src/main/java/com/zhaobiao/admin/config/DataInitializer.java
-- Run this after the schema has been created.
-- The script is idempotent and uses INSERT IGNORE for seed rows.
-- Initial admin username: admin
-- Initial admin password: adminqwert

SET NAMES utf8mb4;
SET @seed_now = NOW();
SET @admin_password_hash = '$2a$10$pVve8MtdwO0AadSEbiOMq.bOda8O2HqBNvE/r9pjBPrttkkVZK5Oq';

START TRANSACTION;

-- 1. Permissions
INSERT IGNORE INTO sys_permission (code, name, description, created_at, updated_at) VALUES
    ('dashboard:view', CONVERT(0xE69FA5E79C8BE5B7A5E4BD9CE58FB0 USING utf8mb4), CONVERT(0xE69FA5E79C8BE5B7A5E4BD9CE58FB0 USING utf8mb4), @seed_now, @seed_now),
    ('profile:view', CONVERT(0xE69FA5E79C8BE4B8AAE4BABAE4BFA1E681AF USING utf8mb4), CONVERT(0xE69FA5E79C8BE4B8AAE4BABAE4BFA1E681AF USING utf8mb4), @seed_now, @seed_now),
    ('profile:edit', CONVERT(0xE4BFAEE694B9E4B8AAE4BABAE4BFA1E681AF USING utf8mb4), CONVERT(0xE4BFAEE694B9E4B8AAE4BABAE4BFA1E681AF USING utf8mb4), @seed_now, @seed_now),
    ('admin:user:view', CONVERT(0xE69FA5E79C8BE7AEA1E79086E59198E8B4A6E58FB7 USING utf8mb4), CONVERT(0xE69FA5E79C8BE7AEA1E79086E59198E8B4A6E58FB7 USING utf8mb4), @seed_now, @seed_now),
    ('admin:user:create', CONVERT(0xE696B0E5A29EE7AEA1E79086E59198E8B4A6E58FB7 USING utf8mb4), CONVERT(0xE696B0E5A29EE7AEA1E79086E59198E8B4A6E58FB7 USING utf8mb4), @seed_now, @seed_now),
    ('admin:user:edit', CONVERT(0xE4BFAEE694B9E7AEA1E79086E59198E8B4A6E58FB7 USING utf8mb4), CONVERT(0xE4BFAEE694B9E7AEA1E79086E59198E8B4A6E58FB7 USING utf8mb4), @seed_now, @seed_now),
    ('admin:user:status:update', CONVERT(0xE4BFAEE694B9E7AEA1E79086E59198E78AB6E68081 USING utf8mb4), CONVERT(0xE4BFAEE694B9E7AEA1E79086E59198E78AB6E68081 USING utf8mb4), @seed_now, @seed_now),
    ('admin:user:password:reset', CONVERT(0xE9878DE7BDAEE7AEA1E79086E59198E5AF86E7A081 USING utf8mb4), CONVERT(0xE9878DE7BDAEE7AEA1E79086E59198E5AF86E7A081 USING utf8mb4), @seed_now, @seed_now),
    ('admin:user:role:update', CONVERT(0xE4BFAEE694B9E7AEA1E79086E59198E8A792E889B2 USING utf8mb4), CONVERT(0xE4BFAEE694B9E7AEA1E79086E59198E8A792E889B2 USING utf8mb4), @seed_now, @seed_now),
    ('member:view', CONVERT(0xE69FA5E79C8BE4BC9AE59198 USING utf8mb4), CONVERT(0xE69FA5E79C8BE4BC9AE59198 USING utf8mb4), @seed_now, @seed_now),
    ('member:create', CONVERT(0xE696B0E5A29EE4BC9AE59198 USING utf8mb4), CONVERT(0xE696B0E5A29EE4BC9AE59198 USING utf8mb4), @seed_now, @seed_now),
    ('member:edit', CONVERT(0xE4BFAEE694B9E4BC9AE59198 USING utf8mb4), CONVERT(0xE4BFAEE694B9E4BC9AE59198 USING utf8mb4), @seed_now, @seed_now),
    ('member:download:update', CONVERT(0xE4BFAEE694B9E4BC9AE59198E4B88BE8BDBDE69D83E99990 USING utf8mb4), CONVERT(0xE4BFAEE694B9E4BC9AE59198E4B88BE8BDBDE69D83E99990 USING utf8mb4), @seed_now, @seed_now),
    ('member:status:update', CONVERT(0xE4BFAEE694B9E4BC9AE59198E78AB6E68081 USING utf8mb4), CONVERT(0xE4BFAEE694B9E4BC9AE59198E78AB6E68081 USING utf8mb4), @seed_now, @seed_now),
    ('member:password:reset', CONVERT(0xE9878DE7BDAEE4BC9AE59198E5AF86E7A081 USING utf8mb4), CONVERT(0xE9878DE7BDAEE4BC9AE59198E5AF86E7A081 USING utf8mb4), @seed_now, @seed_now),
    ('business:type:view', CONVERT(0xE69FA5E79C8BE4B89AE58AA1E7B1BBE59E8B USING utf8mb4), CONVERT(0xE69FA5E79C8BE4B89AE58AA1E7B1BBE59E8B USING utf8mb4), @seed_now, @seed_now),
    ('business:type:create', CONVERT(0xE696B0E5A29EE4B89AE58AA1E7B1BBE59E8B USING utf8mb4), CONVERT(0xE696B0E5A29EE4B89AE58AA1E7B1BBE59E8B USING utf8mb4), @seed_now, @seed_now),
    ('business:type:edit', CONVERT(0xE4BFAEE694B9E4B89AE58AA1E7B1BBE59E8B USING utf8mb4), CONVERT(0xE4BFAEE694B9E4B89AE58AA1E7B1BBE59E8B USING utf8mb4), @seed_now, @seed_now),
    ('business:type:status:update', CONVERT(0xE4BFAEE694B9E4B89AE58AA1E7B1BBE59E8BE78AB6E68081 USING utf8mb4), CONVERT(0xE4BFAEE694B9E4B89AE58AA1E7B1BBE59E8BE78AB6E68081 USING utf8mb4), @seed_now, @seed_now),
    ('business:type:delete', CONVERT(0xE588A0E999A4E4B89AE58AA1E7B1BBE59E8B USING utf8mb4), CONVERT(0xE588A0E999A4E4B89AE58AA1E7B1BBE59E8B USING utf8mb4), @seed_now, @seed_now),
    ('tender:view', CONVERT(0xE69FA5E79C8BE68B9BE6A087 USING utf8mb4), CONVERT(0xE69FA5E79C8BE68B9BE6A087 USING utf8mb4), @seed_now, @seed_now),
    ('tender:create', CONVERT(0xE696B0E5A29EE68B9BE6A087 USING utf8mb4), CONVERT(0xE696B0E5A29EE68B9BE6A087 USING utf8mb4), @seed_now, @seed_now),
    ('tender:edit', CONVERT(0xE4BFAEE694B9E68B9BE6A087 USING utf8mb4), CONVERT(0xE4BFAEE694B9E68B9BE6A087 USING utf8mb4), @seed_now, @seed_now),
    ('tender:delete', CONVERT(0xE588A0E999A4E68B9BE6A087 USING utf8mb4), CONVERT(0xE588A0E999A4E68B9BE6A087 USING utf8mb4), @seed_now, @seed_now),
    ('tender:file:upload', CONVERT(0xE4B88AE4BCA0E68B9BE6A087E99984E4BBB6 USING utf8mb4), CONVERT(0xE4B88AE4BCA0E68B9BE6A087E99984E4BBB6 USING utf8mb4), @seed_now, @seed_now),
    ('user:view', CONVERT(0xE69FA5E79C8BE794A8E688B7 USING utf8mb4), CONVERT(0xE69FA5E79C8BE794A8E688B7 USING utf8mb4), @seed_now, @seed_now),
    ('user:audit', CONVERT(0xE5AEA1E6A0B8E794A8E688B7 USING utf8mb4), CONVERT(0xE5AEA1E6A0B8E794A8E688B7 USING utf8mb4), @seed_now, @seed_now),
    ('user:role:update', CONVERT(0xE4BFAEE694B9E794A8E688B7E8A792E889B2 USING utf8mb4), CONVERT(0xE4BFAEE694B9E794A8E688B7E8A792E889B2 USING utf8mb4), @seed_now, @seed_now),
    ('user:audit:record:view', CONVERT(0xE69FA5E79C8BE794A8E688B7E5AEA1E6A0B8E8AEB0E5BD95 USING utf8mb4), CONVERT(0xE69FA5E79C8BE794A8E688B7E5AEA1E6A0B8E8AEB0E5BD95 USING utf8mb4), @seed_now, @seed_now),
    ('role:view', CONVERT(0xE69FA5E79C8BE8A792E889B2 USING utf8mb4), CONVERT(0xE69FA5E79C8BE8A792E889B2 USING utf8mb4), @seed_now, @seed_now),
    ('role:edit', CONVERT(0xE7BBB4E68AA4E8A792E889B2 USING utf8mb4), CONVERT(0xE7BBB4E68AA4E8A792E889B2 USING utf8mb4), @seed_now, @seed_now),
    ('permission:view', CONVERT(0xE69FA5E79C8BE69D83E99990 USING utf8mb4), CONVERT(0xE69FA5E79C8BE69D83E99990 USING utf8mb4), @seed_now, @seed_now),
    ('permission:edit', CONVERT(0xE7BBB4E68AA4E69D83E99990 USING utf8mb4), CONVERT(0xE7BBB4E68AA4E69D83E99990 USING utf8mb4), @seed_now, @seed_now),
    ('menu:view', CONVERT(0xE69FA5E79C8BE88F9CE58D95 USING utf8mb4), CONVERT(0xE69FA5E79C8BE88F9CE58D95 USING utf8mb4), @seed_now, @seed_now),
    ('menu:edit', CONVERT(0xE7BBB4E68AA4E88F9CE58D95 USING utf8mb4), CONVERT(0xE7BBB4E68AA4E88F9CE58D95 USING utf8mb4), @seed_now, @seed_now),
    ('operation:log:view', CONVERT(0xE69FA5E79C8BE6938DE4BD9CE697A5E5BF97 USING utf8mb4), CONVERT(0xE69FA5E79C8BE6938DE4BD9CE697A5E5BF97 USING utf8mb4), @seed_now, @seed_now);

-- 2. Root menus
INSERT IGNORE INTO sys_menu (code, name, type, parent_id, route_path, component, icon, sort_order, visible, enabled, permission_code, description, created_at, updated_at) VALUES
    ('DASHBOARD', CONVERT(0xE5B7A5E4BD9CE58FB0 USING utf8mb4), 'MENU', NULL, '/dashboard', 'dashboard/index', 'House', 10, 1, 1, 'dashboard:view', CONVERT(0xE7B3BBE7BB9FE9A696E9A1B5 USING utf8mb4), @seed_now, @seed_now),
    ('PROFILE', CONVERT(0xE4B8AAE4BABAE4B8ADE5BF83 USING utf8mb4), 'MENU', NULL, '/profile', 'profile/index', 'User', 20, 1, 1, 'profile:view', CONVERT(0xE4B8AAE4BABAE4BFA1E681AFE7BBB4E68AA4 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_ROOT', CONVERT(0xE7B3BBE7BB9FE7AEA1E79086 USING utf8mb4), 'DIRECTORY', NULL, '/system', '', 'Setting', 30, 1, 1, NULL, CONVERT(0xE7B3BBE7BB9FE7AEA1E79086E79BAEE5BD95 USING utf8mb4), @seed_now, @seed_now);

-- 3. Second-level menus under SYSTEM_ROOT
INSERT IGNORE INTO sys_menu (code, name, type, parent_id, route_path, component, icon, sort_order, visible, enabled, permission_code, description, created_at, updated_at) VALUES
    ('SYSTEM_ADMIN_USER', CONVERT(0xE7AEA1E79086E59198E7AEA1E79086 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/admin-users', 'admin-users/index', 'UserFilled', 10, 1, 1, 'admin:user:view', CONVERT(0xE7AEA1E79086E59198E8B4A6E58FB7E7AEA1E79086E9A1B5E99DA2 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_MEMBER_USER', CONVERT(0xE4BC9AE59198E7AEA1E79086 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/member-users', 'member-users/index', 'User', 20, 1, 1, 'member:view', CONVERT(0xE4BC9AE59198E8B4A6E58FB7E7AEA1E79086E9A1B5E99DA2 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_BUSINESS_TYPE', CONVERT(0xE7B1BBE59E8BE7AEA1E79086 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/business-types', 'business-types/index', 'Collection', 30, 1, 1, 'business:type:view', CONVERT(0xE4B89AE58AA1E7B1BBE59E8BE7AEA1E79086E9A1B5E99DA2 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_TENDER', CONVERT(0xE68B9BE6A087E7AEA1E79086 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/tenders', 'tenders/index', 'Document', 40, 1, 1, 'tender:view', CONVERT(0xE68B9BE6A087E4BFA1E681AFE7AEA1E79086E9A1B5E99DA2 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_USER', CONVERT(0xE697A7E794A8E688B7E7AEA1E79086 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/users', 'users/index', 'UserFilled', 50, 0, 0, 'user:view', CONVERT(0xE58E86E58FB2E794A8E688B7E7AEA1E79086E9A1B5E99DA2EFBC88E5B7B2E5819CE794A8EFBC89 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_AUDIT_RECORD', CONVERT(0xE5AEA1E6A0B8E8AEB0E5BD95 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/audit-records', 'audit-records/index', 'Document', 60, 0, 0, 'user:audit:record:view', CONVERT(0xE58E86E58FB2E794A8E688B7E5AEA1E6A0B8E8AEB0E5BD95E9A1B5E99DA2EFBC88E5B7B2E5819CE794A8EFBC89 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_ROLE', CONVERT(0xE8A792E889B2E7AEA1E79086 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/roles', 'roles/index', 'Avatar', 70, 1, 1, 'role:view', CONVERT(0xE8A792E889B2E7AEA1E79086E9A1B5E99DA2 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_PERMISSION', CONVERT(0xE69D83E99990E7AEA1E79086 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/permissions', 'permissions/index', 'Lock', 80, 1, 1, 'permission:view', CONVERT(0xE69D83E99990E7AEA1E79086E9A1B5E99DA2 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_MENU', CONVERT(0xE88F9CE58D95E7AEA1E79086 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/menus', 'menus/index', 'Menu', 90, 1, 1, 'menu:view', CONVERT(0xE88F9CE58D95E7AEA1E79086E9A1B5E99DA2 USING utf8mb4), @seed_now, @seed_now),
    ('SYSTEM_OPERATION_LOG', CONVERT(0xE6938DE4BD9CE697A5E5BF97 USING utf8mb4), 'MENU', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT'), '/operation-logs', 'operation-logs/index', 'Tickets', 100, 1, 1, 'operation:log:view', CONVERT(0xE6938DE4BD9CE697A5E5BF97E9A1B5E99DA2 USING utf8mb4), @seed_now, @seed_now);

-- 4. Button menus
INSERT IGNORE INTO sys_menu (code, name, type, parent_id, route_path, component, icon, sort_order, visible, enabled, permission_code, description, created_at, updated_at) VALUES
    ('MEMBER_CREATE_BUTTON', CONVERT(0xE696B0E5A29EE4BC9AE59198E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_MEMBER_USER'), '', '', '', 5, 0, 1, 'member:create', CONVERT(0xE696B0E5A29EE4BC9AE59198E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('MEMBER_EDIT_BUTTON', CONVERT(0xE7BC96E8BE91E4BC9AE59198E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_MEMBER_USER'), '', '', '', 8, 0, 1, 'member:edit', CONVERT(0xE7BC96E8BE91E4BC9AE59198E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('ADMIN_USER_CREATE_BUTTON', CONVERT(0xE696B0E5A29EE7AEA1E79086E59198E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ADMIN_USER'), '', '', '', 10, 0, 1, 'admin:user:create', CONVERT(0xE696B0E5A29EE7AEA1E79086E59198E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('ADMIN_USER_EDIT_BUTTON', CONVERT(0xE7BC96E8BE91E7AEA1E79086E59198E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ADMIN_USER'), '', '', '', 20, 0, 1, 'admin:user:edit', CONVERT(0xE7BC96E8BE91E7AEA1E79086E59198E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('ADMIN_USER_STATUS_BUTTON', CONVERT(0xE7AEA1E79086E59198E78AB6E68081E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ADMIN_USER'), '', '', '', 30, 0, 1, 'admin:user:status:update', CONVERT(0xE4BFAEE694B9E7AEA1E79086E59198E78AB6E68081E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('ADMIN_USER_PASSWORD_BUTTON', CONVERT(0xE7AEA1E79086E59198E9878DE7BDAEE5AF86E7A081E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ADMIN_USER'), '', '', '', 40, 0, 1, 'admin:user:password:reset', CONVERT(0xE7AEA1E79086E59198E9878DE7BDAEE5AF86E7A081E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('ADMIN_USER_ROLE_BUTTON', CONVERT(0xE7AEA1E79086E59198E8A792E889B2E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ADMIN_USER'), '', '', '', 50, 0, 1, 'admin:user:role:update', CONVERT(0xE7AEA1E79086E59198E8A792E889B2E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('MEMBER_DOWNLOAD_BUTTON', CONVERT(0xE4BC9AE59198E4B88BE8BDBDE69D83E99990E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_MEMBER_USER'), '', '', '', 10, 0, 1, 'member:download:update', CONVERT(0xE4BC9AE59198E4B88BE8BDBDE69D83E99990E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('MEMBER_STATUS_BUTTON', CONVERT(0xE4BC9AE59198E78AB6E68081E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_MEMBER_USER'), '', '', '', 20, 0, 1, 'member:status:update', CONVERT(0xE4BC9AE59198E78AB6E68081E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('MEMBER_PASSWORD_BUTTON', CONVERT(0xE4BC9AE59198E9878DE7BDAEE5AF86E7A081E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_MEMBER_USER'), '', '', '', 30, 0, 1, 'member:password:reset', CONVERT(0xE4BC9AE59198E9878DE7BDAEE5AF86E7A081E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('BUSINESS_TYPE_CREATE_BUTTON', CONVERT(0xE696B0E5A29EE7B1BBE59E8BE68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_BUSINESS_TYPE'), '', '', '', 10, 0, 1, 'business:type:create', CONVERT(0xE696B0E5A29EE7B1BBE59E8BE68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('BUSINESS_TYPE_EDIT_BUTTON', CONVERT(0xE7BC96E8BE91E7B1BBE59E8BE68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_BUSINESS_TYPE'), '', '', '', 20, 0, 1, 'business:type:edit', CONVERT(0xE7BC96E8BE91E7B1BBE59E8BE68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('BUSINESS_TYPE_STATUS_BUTTON', CONVERT(0xE7B1BBE59E8BE78AB6E68081E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_BUSINESS_TYPE'), '', '', '', 30, 0, 1, 'business:type:status:update', CONVERT(0xE7B1BBE59E8BE78AB6E68081E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('BUSINESS_TYPE_DELETE_BUTTON', CONVERT(0xE588A0E999A4E7B1BBE59E8BE68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_BUSINESS_TYPE'), '', '', '', 40, 0, 1, 'business:type:delete', CONVERT(0xE588A0E999A4E7B1BBE59E8BE68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('TENDER_CREATE_BUTTON', CONVERT(0xE696B0E5A29EE68B9BE6A087E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_TENDER'), '', '', '', 10, 0, 1, 'tender:create', CONVERT(0xE696B0E5A29EE68B9BE6A087E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('TENDER_EDIT_BUTTON', CONVERT(0xE7BC96E8BE91E68B9BE6A087E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_TENDER'), '', '', '', 20, 0, 1, 'tender:edit', CONVERT(0xE7BC96E8BE91E68B9BE6A087E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('TENDER_DELETE_BUTTON', CONVERT(0xE588A0E999A4E68B9BE6A087E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_TENDER'), '', '', '', 30, 0, 1, 'tender:delete', CONVERT(0xE588A0E999A4E68B9BE6A087E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('TENDER_UPLOAD_BUTTON', CONVERT(0xE4B88AE4BCA0E99984E4BBB6E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_TENDER'), '', '', '', 40, 0, 1, 'tender:file:upload', CONVERT(0xE4B88AE4BCA0E68B9BE6A087E99984E4BBB6E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('USER_AUDIT_BUTTON', CONVERT(0xE5AEA1E6A0B8E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_USER'), '', '', '', 10, 0, 0, 'user:audit', CONVERT(0xE697A7E794A8E688B7E5AEA1E6A0B8E68C89E992AEEFBC88E5B7B2E5819CE794A8EFBC89 USING utf8mb4), @seed_now, @seed_now),
    ('USER_ROLE_BUTTON', CONVERT(0xE58886E9858DE8A792E889B2E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_USER'), '', '', '', 20, 0, 0, 'user:role:update', CONVERT(0xE697A7E794A8E688B7E58886E9858DE8A792E889B2E68C89E992AEEFBC88E5B7B2E5819CE794A8EFBC89 USING utf8mb4), @seed_now, @seed_now),
    ('ROLE_EDIT_BUTTON', CONVERT(0xE8A792E889B2E7BBB4E68AA4E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROLE'), '', '', '', 10, 0, 1, 'role:edit', CONVERT(0xE8A792E889B2E7BBB4E68AA4E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('PERMISSION_EDIT_BUTTON', CONVERT(0xE69D83E99990E7BBB4E68AA4E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_PERMISSION'), '', '', '', 10, 0, 1, 'permission:edit', CONVERT(0xE69D83E99990E7BBB4E68AA4E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('MENU_EDIT_BUTTON', CONVERT(0xE88F9CE58D95E7BBB4E68AA4E68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'SYSTEM_MENU'), '', '', '', 10, 0, 1, 'menu:edit', CONVERT(0xE88F9CE58D95E7BBB4E68AA4E68C89E992AE USING utf8mb4), @seed_now, @seed_now),
    ('PROFILE_EDIT_BUTTON', CONVERT(0xE7BC96E8BE91E4B8AAE4BABAE4BFA1E681AFE68C89E992AE USING utf8mb4), 'BUTTON', (SELECT id FROM sys_menu WHERE code = 'PROFILE'), '', '', '', 10, 0, 1, 'profile:edit', CONVERT(0xE7BC96E8BE91E4B8AAE4BABAE4BFA1E681AFE68C89E992AE USING utf8mb4), @seed_now, @seed_now);

-- 5. Roles
INSERT IGNORE INTO sys_role (code, name, description, built_in, created_at, updated_at) VALUES
    ('SUPER_ADMIN', CONVERT(0xE8B685E7BAA7E7AEA1E79086E59198 USING utf8mb4), CONVERT(0xE68BA5E69C89E7B3BBE7BB9FE585A8E983A8E69D83E99990 USING utf8mb4), 1, @seed_now, @seed_now),
    ('SYSTEM_ADMIN', CONVERT(0xE7B3BBE7BB9FE7AEA1E79086E59198 USING utf8mb4), CONVERT(0xE8B49FE8B4A3E4BC9AE59198E38081E68B9BE6A087E38081E8A792E889B2E38081E69D83E99990E38081E88F9CE58D95E5928CE697A5E5BF97E7AEA1E79086 USING utf8mb4), 1, @seed_now, @seed_now),
    ('USER_AUDITOR', CONVERT(0xE794A8E688B7E5AEA1E6A0B8E59198 USING utf8mb4), CONVERT(0xE58E86E58FB2E794A8E688B7E5AEA1E6A0B8E8A792E889B2EFBC8CE5BD93E5898DE4BB85E4BF9DE79599E59FBAE7A180E8AEBFE997AEE883BDE58A9B USING utf8mb4), 1, @seed_now, @seed_now),
    ('NORMAL_USER', CONVERT(0xE699AEE9809AE794A8E688B7 USING utf8mb4), CONVERT(0xE9BB98E8AEA4E6B3A8E5868CE794A8E688B7E8A792E889B2EFBC8CE4BB85E58581E8AEB8E69FA5E79C8BE5928CE7BBB4E68AA4E4B8AAE4BABAE4BFA1E681AF USING utf8mb4), 1, @seed_now, @seed_now);

-- 6. SUPER_ADMIN role bindings
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) SELECT role.id, permission.id FROM sys_role role JOIN sys_permission permission ON role.code = 'SUPER_ADMIN';
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT role.id, menu.id FROM sys_role role JOIN sys_menu menu ON role.code = 'SUPER_ADMIN';

-- 7. Other role-permission bindings
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'dashboard:view')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'profile:view')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'profile:edit')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'member:view')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'member:create')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'member:edit')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'member:download:update')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'member:status:update')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'member:password:reset')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'business:type:view')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'business:type:create')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'business:type:edit')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'business:type:status:update')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'business:type:delete')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'tender:view')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'tender:create')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'tender:edit')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'tender:delete')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'tender:file:upload')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'role:view')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'role:edit')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'permission:view')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'permission:edit')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'menu:view')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'menu:edit')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_permission WHERE code = 'operation:log:view')),
    ((SELECT id FROM sys_role WHERE code = 'USER_AUDITOR'), (SELECT id FROM sys_permission WHERE code = 'dashboard:view')),
    ((SELECT id FROM sys_role WHERE code = 'USER_AUDITOR'), (SELECT id FROM sys_permission WHERE code = 'profile:view')),
    ((SELECT id FROM sys_role WHERE code = 'USER_AUDITOR'), (SELECT id FROM sys_permission WHERE code = 'profile:edit')),
    ((SELECT id FROM sys_role WHERE code = 'NORMAL_USER'), (SELECT id FROM sys_permission WHERE code = 'dashboard:view')),
    ((SELECT id FROM sys_role WHERE code = 'NORMAL_USER'), (SELECT id FROM sys_permission WHERE code = 'profile:view')),
    ((SELECT id FROM sys_role WHERE code = 'NORMAL_USER'), (SELECT id FROM sys_permission WHERE code = 'profile:edit'));

-- 8. Other role-menu bindings
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'DASHBOARD')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'PROFILE')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROOT')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'SYSTEM_MEMBER_USER')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'SYSTEM_BUSINESS_TYPE')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'SYSTEM_TENDER')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'SYSTEM_ROLE')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'SYSTEM_PERMISSION')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'SYSTEM_MENU')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'SYSTEM_OPERATION_LOG')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'MEMBER_CREATE_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'MEMBER_EDIT_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'MEMBER_DOWNLOAD_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'MEMBER_STATUS_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'MEMBER_PASSWORD_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'BUSINESS_TYPE_CREATE_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'BUSINESS_TYPE_EDIT_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'BUSINESS_TYPE_STATUS_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'BUSINESS_TYPE_DELETE_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'TENDER_CREATE_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'TENDER_EDIT_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'TENDER_DELETE_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'TENDER_UPLOAD_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'ROLE_EDIT_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'PERMISSION_EDIT_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'MENU_EDIT_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'SYSTEM_ADMIN'), (SELECT id FROM sys_menu WHERE code = 'PROFILE_EDIT_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'USER_AUDITOR'), (SELECT id FROM sys_menu WHERE code = 'DASHBOARD')),
    ((SELECT id FROM sys_role WHERE code = 'USER_AUDITOR'), (SELECT id FROM sys_menu WHERE code = 'PROFILE')),
    ((SELECT id FROM sys_role WHERE code = 'USER_AUDITOR'), (SELECT id FROM sys_menu WHERE code = 'PROFILE_EDIT_BUTTON')),
    ((SELECT id FROM sys_role WHERE code = 'NORMAL_USER'), (SELECT id FROM sys_menu WHERE code = 'DASHBOARD')),
    ((SELECT id FROM sys_role WHERE code = 'NORMAL_USER'), (SELECT id FROM sys_menu WHERE code = 'PROFILE')),
    ((SELECT id FROM sys_role WHERE code = 'NORMAL_USER'), (SELECT id FROM sys_menu WHERE code = 'PROFILE_EDIT_BUTTON'));

-- 9. Initial admin user
INSERT IGNORE INTO sys_user (username, phone, email, password, real_name, company_name, contact_person, unified_social_credit_code, status, created_at, updated_at) VALUES
    ('admin', '13900000000', 'admin@zhaobiao.com', @admin_password_hash, CONVERT(0xE8B685E7BAA7E7AEA1E79086E59198 USING utf8mb4), CONVERT(0xE5B9B3E58FB0E8BF90E890A5E4B8ADE5BF83 USING utf8mb4), CONVERT(0xE8B685E7BAA7E7AEA1E79086E59198 USING utf8mb4), '91310000MA1KADMIN00', 'APPROVED', @seed_now, @seed_now);

-- 10. Initial admin role binding
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES ((SELECT id FROM sys_user WHERE username = 'admin'), (SELECT id FROM sys_role WHERE code = 'SUPER_ADMIN'));

-- 11. Business types
INSERT IGNORE INTO biz_business_type (code, name, enabled, sort_order, description, created_at, updated_at) VALUES
    ('ENGINEERING', CONVERT(0xE5B7A5E7A88B USING utf8mb4), 1, 10, CONVERT(0xE5B7A5E7A88BE7B1BBE4B89AE58AA1 USING utf8mb4), @seed_now, @seed_now),
    ('GOODS', CONVERT(0xE8B4A7E789A9 USING utf8mb4), 1, 20, CONVERT(0xE8B4A7E789A9E7B1BBE4B89AE58AA1 USING utf8mb4), @seed_now, @seed_now),
    ('SERVICE', CONVERT(0xE69C8DE58AA1 USING utf8mb4), 1, 30, CONVERT(0xE69C8DE58AA1E7B1BBE4B89AE58AA1 USING utf8mb4), @seed_now, @seed_now);

COMMIT;
