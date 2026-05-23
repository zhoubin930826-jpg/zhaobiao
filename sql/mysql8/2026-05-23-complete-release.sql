-- 2026-05-23 complete release migration for test and production.
-- 执行时机：部署本次后端应用前执行。
-- 覆盖范围：
--   1. 会员首次登录和资料文件字段。
--   2. 会员软删除字段、未删除会员唯一键、文件缩略图字段。
--   3. 会员状态从 PENDING_REVIEW 兼容迁移到 DISABLED。
--   4. 资讯表 biz_news。
--   5. 后管“资讯管理”“发布招标”“删除会员”等菜单/按钮权限。
-- 文件本体仍由后端按 APP_FILE_TYPE 决定写入本地目录或 OSS，本 SQL 不迁移文件对象。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS zb_20260523_add_column_if_missing;
DROP PROCEDURE IF EXISTS zb_20260523_add_index_if_missing;
DROP PROCEDURE IF EXISTS zb_20260523_add_constraint_if_missing;
DROP PROCEDURE IF EXISTS zb_20260523_drop_member_base_unique_indexes;

DELIMITER $$

CREATE PROCEDURE zb_20260523_add_column_if_missing(
    IN p_table_name varchar(64),
    IN p_column_name varchar(64),
    IN p_column_definition text
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @ddl_sql = CONCAT('ALTER TABLE `', REPLACE(p_table_name, '`', '``'), '` ADD COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE zb_20260523_add_index_if_missing(
    IN p_table_name varchar(64),
    IN p_index_name varchar(128),
    IN p_index_definition text
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @ddl_sql = CONCAT('ALTER TABLE `', REPLACE(p_table_name, '`', '``'), '` ADD ', p_index_definition);
        PREPARE stmt FROM @ddl_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE zb_20260523_add_constraint_if_missing(
    IN p_table_name varchar(64),
    IN p_constraint_name varchar(128),
    IN p_constraint_sql text
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_schema = DATABASE()
          AND table_name = p_table_name
          AND constraint_name = p_constraint_name
    ) THEN
        SET @ddl_sql = p_constraint_sql;
        PREPARE stmt FROM @ddl_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE zb_20260523_drop_member_base_unique_indexes()
BEGIN
    DECLARE done int DEFAULT 0;
    DECLARE v_index_name varchar(128);
    DECLARE cur CURSOR FOR
        SELECT s.index_name
        FROM information_schema.statistics s
        JOIN (
            SELECT index_name, COUNT(*) AS column_count
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'portal_member_user'
            GROUP BY index_name
        ) idx ON idx.index_name = s.index_name
        WHERE s.table_schema = DATABASE()
          AND s.table_name = 'portal_member_user'
          AND s.non_unique = 0
          AND s.index_name <> 'PRIMARY'
          AND s.column_name IN ('username', 'phone', 'email', 'unified_social_credit_code')
          AND idx.column_count = 1;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_index_name;
        IF done THEN
            LEAVE read_loop;
        END IF;
        SET @ddl_sql = CONCAT('ALTER TABLE portal_member_user DROP INDEX `', REPLACE(v_index_name, '`', '``'), '`');
        PREPARE stmt FROM @ddl_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END LOOP;
    CLOSE cur;
END$$

DELIMITER ;

-- 1. 会员资料字段。
CALL zb_20260523_add_column_if_missing(
    'portal_member_user',
    'first_login_at',
    'first_login_at datetime(6) NULL COMMENT ''会员首次登录时间'''
);
CALL zb_20260523_add_column_if_missing(
    'portal_member_user',
    'business_license_file_id',
    'business_license_file_id bigint NULL COMMENT ''营业执照文件ID'''
);
CALL zb_20260523_add_column_if_missing(
    'portal_member_user',
    'three_year_performance_file_id',
    'three_year_performance_file_id bigint NULL COMMENT ''三年内业绩文件ID'''
);
CALL zb_20260523_add_index_if_missing(
    'portal_member_user',
    'idx_portal_member_business_license_file',
    'INDEX idx_portal_member_business_license_file (business_license_file_id)'
);
CALL zb_20260523_add_index_if_missing(
    'portal_member_user',
    'idx_portal_member_three_year_performance_file',
    'INDEX idx_portal_member_three_year_performance_file (three_year_performance_file_id)'
);
CALL zb_20260523_add_constraint_if_missing(
    'portal_member_user',
    'fk_portal_member_business_license_file',
    'ALTER TABLE portal_member_user ADD CONSTRAINT fk_portal_member_business_license_file FOREIGN KEY (business_license_file_id) REFERENCES biz_file_storage (id)'
);
CALL zb_20260523_add_constraint_if_missing(
    'portal_member_user',
    'fk_portal_member_three_year_performance_file',
    'ALTER TABLE portal_member_user ADD CONSTRAINT fk_portal_member_three_year_performance_file FOREIGN KEY (three_year_performance_file_id) REFERENCES biz_file_storage (id)'
);

-- 2. 会员软删除和未删除会员唯一键。
CALL zb_20260523_add_column_if_missing(
    'portal_member_user',
    'deleted',
    'deleted bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否软删除'''
);
CALL zb_20260523_add_column_if_missing(
    'portal_member_user',
    'deleted_at',
    'deleted_at datetime(6) NULL COMMENT ''软删除时间'''
);

UPDATE portal_member_user
SET deleted = b'0'
WHERE deleted IS NULL;

-- 删除旧的“全表唯一”单列索引，改用 active_* 生成列保证未删除会员唯一。
CALL zb_20260523_drop_member_base_unique_indexes();

CALL zb_20260523_add_column_if_missing(
    'portal_member_user',
    'active_username',
    'active_username varchar(64) GENERATED ALWAYS AS (CASE WHEN deleted = b''0'' THEN username ELSE NULL END) STORED COMMENT ''未删除会员用户名唯一键'''
);
CALL zb_20260523_add_column_if_missing(
    'portal_member_user',
    'active_phone',
    'active_phone varchar(32) GENERATED ALWAYS AS (CASE WHEN deleted = b''0'' THEN phone ELSE NULL END) STORED COMMENT ''未删除会员手机号唯一键'''
);
CALL zb_20260523_add_column_if_missing(
    'portal_member_user',
    'active_email',
    'active_email varchar(128) GENERATED ALWAYS AS (CASE WHEN deleted = b''0'' THEN email ELSE NULL END) STORED COMMENT ''未删除会员邮箱唯一键'''
);
CALL zb_20260523_add_column_if_missing(
    'portal_member_user',
    'active_unified_social_credit_code',
    'active_unified_social_credit_code varchar(32) GENERATED ALWAYS AS (CASE WHEN deleted = b''0'' THEN unified_social_credit_code ELSE NULL END) STORED COMMENT ''未删除会员统一社会信用代码唯一键'''
);
CALL zb_20260523_add_index_if_missing(
    'portal_member_user',
    'uk_portal_member_user_active_username',
    'UNIQUE INDEX uk_portal_member_user_active_username (active_username)'
);
CALL zb_20260523_add_index_if_missing(
    'portal_member_user',
    'uk_portal_member_user_active_phone',
    'UNIQUE INDEX uk_portal_member_user_active_phone (active_phone)'
);
CALL zb_20260523_add_index_if_missing(
    'portal_member_user',
    'uk_portal_member_user_active_email',
    'UNIQUE INDEX uk_portal_member_user_active_email (active_email)'
);
CALL zb_20260523_add_index_if_missing(
    'portal_member_user',
    'uk_portal_member_user_active_credit_code',
    'UNIQUE INDEX uk_portal_member_user_active_credit_code (active_unified_social_credit_code)'
);

-- 3. 文件缩略图字段。
CALL zb_20260523_add_column_if_missing(
    'biz_file_storage',
    'thumbnail_path',
    'thumbnail_path varchar(512) NULL COMMENT ''缩略图存储路径'''
);
CALL zb_20260523_add_column_if_missing(
    'biz_file_storage',
    'thumbnail_content_type',
    'thumbnail_content_type varchar(128) NULL COMMENT ''缩略图内容类型'''
);
CALL zb_20260523_add_column_if_missing(
    'biz_file_storage',
    'thumbnail_size',
    'thumbnail_size bigint NULL COMMENT ''缩略图大小，单位字节'''
);
CALL zb_20260523_add_column_if_missing(
    'biz_file_storage',
    'thumbnail_width',
    'thumbnail_width int NULL COMMENT ''缩略图宽度，单位像素'''
);
CALL zb_20260523_add_column_if_missing(
    'biz_file_storage',
    'thumbnail_height',
    'thumbnail_height int NULL COMMENT ''缩略图高度，单位像素'''
);
CALL zb_20260523_add_column_if_missing(
    'biz_file_storage',
    'thumbnail_status',
    'thumbnail_status varchar(32) NULL COMMENT ''缩略图状态：READY/UNSUPPORTED/FAILED'''
);

-- 4. 会员状态兼容迁移。
UPDATE portal_member_user
SET status = 'DISABLED'
WHERE status = 'PENDING_REVIEW';

-- 5. 资讯表。
CREATE TABLE IF NOT EXISTS biz_news (
    id bigint NOT NULL AUTO_INCREMENT,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    title varchar(200) NOT NULL COMMENT '资讯标题',
    cover_file_id bigint NULL COMMENT '封面文件ID',
    content longtext NOT NULL COMMENT '资讯正文',
    publish_at datetime(6) NOT NULL COMMENT '发布时间',
    source varchar(128) NOT NULL COMMENT '信息来源',
    summary varchar(500) NOT NULL COMMENT '内容摘要',
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

CALL zb_20260523_add_index_if_missing(
    'biz_news',
    'idx_biz_news_status_publish_at',
    'INDEX idx_biz_news_status_publish_at (status, publish_at)'
);
CALL zb_20260523_add_index_if_missing(
    'biz_news',
    'idx_biz_news_category',
    'INDEX idx_biz_news_category (category)'
);
CALL zb_20260523_add_index_if_missing(
    'biz_news',
    'idx_biz_news_cover_file',
    'INDEX idx_biz_news_cover_file (cover_file_id)'
);
CALL zb_20260523_add_constraint_if_missing(
    'biz_news',
    'fk_biz_news_cover_file',
    'ALTER TABLE biz_news ADD CONSTRAINT fk_biz_news_cover_file FOREIGN KEY (cover_file_id) REFERENCES biz_file_storage (id)'
);

-- 6. 菜单和按钮权限。
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
SET @menu_system_tender = (SELECT id FROM sys_menu WHERE code = 'SYSTEM_TENDER');
SET @menu_system_member_user = (SELECT id FROM sys_menu WHERE code = 'SYSTEM_MEMBER_USER');

INSERT INTO sys_menu (
    code, name, type, parent_id, route_path, component, icon, sort_order,
    visible, enabled, permission_code, description, created_at, updated_at
) VALUES
    ('NEWS_CREATE_BUTTON', '新增资讯按钮', 'BUTTON', @menu_system_news, '', '', '', 10, 0, 1, NULL, '新增资讯按钮', NOW(), NOW()),
    ('NEWS_EDIT_BUTTON', '编辑资讯按钮', 'BUTTON', @menu_system_news, '', '', '', 20, 0, 1, NULL, '编辑资讯按钮', NOW(), NOW()),
    ('NEWS_DELETE_BUTTON', '删除资讯按钮', 'BUTTON', @menu_system_news, '', '', '', 30, 0, 1, NULL, '删除资讯按钮', NOW(), NOW()),
    ('NEWS_PUBLISH_BUTTON', '发布资讯按钮', 'BUTTON', @menu_system_news, '', '', '', 35, 0, 1, NULL, '发布/下架资讯按钮', NOW(), NOW()),
    ('NEWS_UPLOAD_BUTTON', '上传资讯封面按钮', 'BUTTON', @menu_system_news, '', '', '', 40, 0, 1, NULL, '上传资讯封面按钮', NOW(), NOW()),
    ('TENDER_PUBLISH_BUTTON', '发布招标按钮', 'BUTTON', @menu_system_tender, '', '', '', 35, 0, 1, NULL, '发布/改为未发布招标按钮', NOW(), NOW()),
    ('MEMBER_DELETE_BUTTON', '删除会员按钮', 'BUTTON', @menu_system_member_user, '', '', '', 40, 0, 1, NULL, '删除会员按钮', NOW(), NOW())
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
WHERE code IN (
    'SYSTEM_NEWS',
    'NEWS_CREATE_BUTTON',
    'NEWS_EDIT_BUTTON',
    'NEWS_DELETE_BUTTON',
    'NEWS_PUBLISH_BUTTON',
    'NEWS_UPLOAD_BUTTON',
    'TENDER_PUBLISH_BUTTON',
    'MEMBER_DELETE_BUTTON'
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
JOIN sys_menu menu ON menu.code IN (
    'SYSTEM_NEWS',
    'NEWS_CREATE_BUTTON',
    'NEWS_EDIT_BUTTON',
    'NEWS_DELETE_BUTTON',
    'NEWS_PUBLISH_BUTTON',
    'NEWS_UPLOAD_BUTTON',
    'TENDER_PUBLISH_BUTTON',
    'MEMBER_DELETE_BUTTON'
)
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu existing
      WHERE existing.role_id = role.id
        AND existing.menu_id = menu.id
  );

-- 7. 执行后检查。
SELECT 'portal_member_user.first_login_at' AS check_item, COUNT(*) AS exists_count
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'portal_member_user'
  AND column_name = 'first_login_at'
UNION ALL
SELECT 'portal_member_user.deleted', COUNT(*)
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'portal_member_user'
  AND column_name = 'deleted'
UNION ALL
SELECT 'portal_member_user.active_unique_indexes', COUNT(*)
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'portal_member_user'
  AND index_name IN (
      'uk_portal_member_user_active_username',
      'uk_portal_member_user_active_phone',
      'uk_portal_member_user_active_email',
      'uk_portal_member_user_active_credit_code'
  )
UNION ALL
SELECT 'portal_member_user.remaining_base_unique_indexes', COUNT(*)
FROM (
    SELECT s.index_name
    FROM information_schema.statistics s
    JOIN (
        SELECT index_name, COUNT(*) AS column_count
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'portal_member_user'
        GROUP BY index_name
    ) idx ON idx.index_name = s.index_name
    WHERE s.table_schema = DATABASE()
      AND s.table_name = 'portal_member_user'
      AND s.non_unique = 0
      AND s.index_name <> 'PRIMARY'
      AND s.column_name IN ('username', 'phone', 'email', 'unified_social_credit_code')
      AND idx.column_count = 1
    GROUP BY s.index_name
) remaining_member_base_unique_indexes
UNION ALL
SELECT 'biz_file_storage.thumbnail_path', COUNT(*)
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'biz_file_storage'
  AND column_name = 'thumbnail_path'
UNION ALL
SELECT 'biz_news', COUNT(*)
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'biz_news'
UNION ALL
SELECT 'sys_menu.SYSTEM_NEWS', COUNT(*)
FROM sys_menu
WHERE code = 'SYSTEM_NEWS'
UNION ALL
SELECT 'sys_menu.TENDER_PUBLISH_BUTTON', COUNT(*)
FROM sys_menu
WHERE code = 'TENDER_PUBLISH_BUTTON'
UNION ALL
SELECT 'sys_menu.NEWS_PUBLISH_BUTTON', COUNT(*)
FROM sys_menu
WHERE code = 'NEWS_PUBLISH_BUTTON'
UNION ALL
SELECT 'sys_menu.MEMBER_DELETE_BUTTON', COUNT(*)
FROM sys_menu
WHERE code = 'MEMBER_DELETE_BUTTON'
UNION ALL
SELECT 'sys_role_menu.news_roles', COUNT(*)
FROM sys_role_menu role_menu
JOIN sys_role role ON role.id = role_menu.role_id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND menu.code IN ('SYSTEM_NEWS', 'NEWS_CREATE_BUTTON', 'NEWS_EDIT_BUTTON', 'NEWS_DELETE_BUTTON', 'NEWS_PUBLISH_BUTTON', 'NEWS_UPLOAD_BUTTON')
UNION ALL
SELECT 'sys_role_menu.tender_publish_roles', COUNT(*)
FROM sys_role_menu role_menu
JOIN sys_role role ON role.id = role_menu.role_id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND menu.code = 'TENDER_PUBLISH_BUTTON'
UNION ALL
SELECT 'sys_role_menu.member_delete_roles', COUNT(*)
FROM sys_role_menu role_menu
JOIN sys_role role ON role.id = role_menu.role_id
JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE role.code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN')
  AND menu.code = 'MEMBER_DELETE_BUTTON';

DROP PROCEDURE IF EXISTS zb_20260523_add_column_if_missing;
DROP PROCEDURE IF EXISTS zb_20260523_add_index_if_missing;
DROP PROCEDURE IF EXISTS zb_20260523_add_constraint_if_missing;
DROP PROCEDURE IF EXISTS zb_20260523_drop_member_base_unique_indexes;
