-- 2026-05-08 portal member soft-delete and file thumbnail fields.
-- Run once in production before deploying code that maps these columns.
-- This script also adds the admin member delete button menu data.
-- If Hibernate ddl-auto=update already created any of these columns locally,
-- do not rerun the duplicate ADD COLUMN statement against that database.

-- 1. Add soft-delete fields to portal members.
ALTER TABLE portal_member_user
    ADD COLUMN deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否软删除',
    ADD COLUMN deleted_at datetime(6) NULL COMMENT '软删除时间';

UPDATE portal_member_user
SET deleted = b'0'
WHERE deleted IS NULL;

-- 2. Replace old unique indexes with active-member-only unique indexes.
-- Current production index names checked on 2026-05-08:
-- username: UK_pru47c8jsq3auv3e6ycxl3moc
-- phone: UK_21yxn7wos5tgx839ugpybuthq
-- email: UK_e0m2byoy32rcompmsv4sw553d
-- unified_social_credit_code: UK_oonbfrfkxvg6d5brdfqcy3l70
ALTER TABLE portal_member_user
    DROP INDEX UK_pru47c8jsq3auv3e6ycxl3moc,
    DROP INDEX UK_21yxn7wos5tgx839ugpybuthq,
    DROP INDEX UK_e0m2byoy32rcompmsv4sw553d,
    DROP INDEX UK_oonbfrfkxvg6d5brdfqcy3l70;

ALTER TABLE portal_member_user
    ADD COLUMN active_username varchar(64)
        GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN username ELSE NULL END) STORED
        COMMENT '未删除会员用户名唯一键',
    ADD COLUMN active_phone varchar(32)
        GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN phone ELSE NULL END) STORED
        COMMENT '未删除会员手机号唯一键',
    ADD COLUMN active_email varchar(128)
        GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN email ELSE NULL END) STORED
        COMMENT '未删除会员邮箱唯一键',
    ADD COLUMN active_unified_social_credit_code varchar(32)
        GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN unified_social_credit_code ELSE NULL END) STORED
        COMMENT '未删除会员统一社会信用代码唯一键';

CREATE UNIQUE INDEX uk_portal_member_user_active_username
    ON portal_member_user (active_username);

CREATE UNIQUE INDEX uk_portal_member_user_active_phone
    ON portal_member_user (active_phone);

CREATE UNIQUE INDEX uk_portal_member_user_active_email
    ON portal_member_user (active_email);

CREATE UNIQUE INDEX uk_portal_member_user_active_credit_code
    ON portal_member_user (active_unified_social_credit_code);

-- 3. Add thumbnail metadata fields to stored files.
ALTER TABLE biz_file_storage
    ADD COLUMN thumbnail_path varchar(512) NULL COMMENT '缩略图存储路径',
    ADD COLUMN thumbnail_content_type varchar(128) NULL COMMENT '缩略图内容类型',
    ADD COLUMN thumbnail_size bigint NULL COMMENT '缩略图大小，单位字节',
    ADD COLUMN thumbnail_width int NULL COMMENT '缩略图宽度，单位像素',
    ADD COLUMN thumbnail_height int NULL COMMENT '缩略图高度，单位像素',
    ADD COLUMN thumbnail_status varchar(32) NULL COMMENT '缩略图状态：READY/UNSUPPORTED/FAILED';

-- 4. Add admin member delete button menu data.
INSERT INTO sys_menu (
    code, name, type, parent_id, route_path, component, icon, sort_order,
    visible, enabled, permission_code, description, created_at, updated_at
)
SELECT
    'MEMBER_DELETE_BUTTON',
    '删除会员按钮',
    'BUTTON',
    parent_menu.id,
    '',
    '',
    '',
    40,
    0,
    1,
    NULL,
    '删除会员按钮',
    NOW(),
    NOW()
FROM sys_menu parent_menu
WHERE parent_menu.code = 'SYSTEM_MEMBER_USER'
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

UPDATE sys_menu
SET permission_code = NULL,
    updated_at = NOW()
WHERE code = 'MEMBER_DELETE_BUTTON';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
JOIN sys_menu menu ON menu.code = 'MEMBER_DELETE_BUTTON'
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu existing
      WHERE existing.role_id = role.id
        AND existing.menu_id = menu.id
  );

-- 5. Optional checks after execution.
SELECT 'portal_member_user.deleted' AS check_item, COUNT(*) AS exists_count
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'portal_member_user'
  AND column_name = 'deleted'
UNION ALL
SELECT 'biz_file_storage.thumbnail_path' AS check_item, COUNT(*) AS exists_count
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'biz_file_storage'
  AND column_name = 'thumbnail_path'
UNION ALL
SELECT 'sys_menu.MEMBER_DELETE_BUTTON' AS check_item, COUNT(*) AS exists_count
FROM sys_menu
WHERE code = 'MEMBER_DELETE_BUTTON'
UNION ALL
SELECT 'sys_role_menu.delete_button_roles' AS check_item, COUNT(*) AS exists_count
FROM sys_role_menu role_menu
JOIN sys_role role ON role.id = role_menu.role_id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND menu.code = 'MEMBER_DELETE_BUTTON';
