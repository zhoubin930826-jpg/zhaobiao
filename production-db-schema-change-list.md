# 生产库表结构变更清单

更新时间：2026-04-25

## 1. 说明

- 本清单聚焦本轮“封闭式会员制 + 类型管理 + 会员多类型 + 招标按类型隔离”涉及的生产库变更。
- 2026-04-25 追加“会员账号过期时间”字段，历史会员默认补为当前时间 + 1 年。
- 这份清单不重复列出此前已经处理过的 `sys_user` 字段可空调整。
- 当前后端代码已在本地 Docker MySQL `zhaobiao_admin` 上实际启动验证通过。

本地验证信息：

- MySQL：`127.0.0.1:3306 / zhaobiao_admin / root / root`
- 验证方式：`spring.jpa.hibernate.ddl-auto=update`
- 验证结果：当前代码可正常启动，并自动补齐本轮新增表结构

## 2. 本轮需要同步到生产库的表结构变更

### 2.1 新增业务类型表

新增表：`biz_business_type`

用途：

- 后台“类型管理”菜单的主表
- 会员类型与招标类型统一引用此表

当前本地实际表结构：

```sql
CREATE TABLE `biz_business_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `code` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `name` varchar(64) NOT NULL,
  `sort_order` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_nqgrf9s9xtl1frai8exffd2ej` (`code`),
  UNIQUE KEY `UK_kdjqhc1n02nkap8bachkv4npv` (`name`)
);
```

需要关注：

- `code` 唯一
- `name` 唯一
- `enabled` 用于控制类型是否可用
- `sort_order` 用于前后台排序

### 2.2 新增会员与类型关联表

新增表：`portal_member_business_type_rel`

用途：

- 支撑“一个会员可绑定多个类型”的多对多关系

当前本地实际表结构：

```sql
CREATE TABLE `portal_member_business_type_rel` (
  `member_user_id` bigint NOT NULL,
  `business_type_id` bigint NOT NULL,
  PRIMARY KEY (`member_user_id`, `business_type_id`),
  KEY `FK6327s3ngigdr2ri3ulg474oag` (`business_type_id`),
  CONSTRAINT `FK5pjd8mamtbopmgk46miyrkat8`
    FOREIGN KEY (`member_user_id`) REFERENCES `portal_member_user` (`id`),
  CONSTRAINT `FK6327s3ngigdr2ri3ulg474oag`
    FOREIGN KEY (`business_type_id`) REFERENCES `biz_business_type` (`id`)
);
```

需要关注：

- 主键是复合主键：`(member_user_id, business_type_id)`
- 不允许同一个会员重复绑定同一个类型

### 2.3 招标表增加业务类型字段

修改表：`biz_tender`

新增字段：

- `business_type_id bigint`

新增索引与外键：

- 索引：`KEY FK1n4xqcodarjbknkworei6vua0 (business_type_id)`
- 外键：`FOREIGN KEY (business_type_id) REFERENCES biz_business_type(id)`

当前本地实际表结构中的新增部分：

```sql
ALTER TABLE `biz_tender`
  ADD COLUMN `business_type_id` bigint DEFAULT NULL,
  ADD KEY `FK1n4xqcodarjbknkworei6vua0` (`business_type_id`),
  ADD CONSTRAINT `FK1n4xqcodarjbknkworei6vua0`
    FOREIGN KEY (`business_type_id`) REFERENCES `biz_business_type` (`id`);
```

说明：

- 当前代码层面创建/修改招标时已强制要求传 `businessTypeId`
- 但数据库层目前仍是 `DEFAULT NULL`
- 这样做是为了兼容历史数据迁移，避免生产库已有旧数据时直接加非空失败
- 建议等历史招标全部回填类型后，再评估是否把该字段收紧为 `NOT NULL`

### 2.4 会员主表增加账号过期时间

修改表：`portal_member_user`

新增字段：

- `expires_at datetime(6)`

用途：

- 管理员创建或修改会员时维护账号有效期
- 会员登录前校验是否过期
- 已登录会员继续访问门户接口时，后端通过每次 JWT 鉴权重新加载会员信息并校验过期时间

建议生产库执行：

```sql
ALTER TABLE `portal_member_user`
  ADD COLUMN `expires_at` datetime(6) DEFAULT NULL COMMENT '会员账号过期时间';

UPDATE `portal_member_user`
SET `expires_at` = DATE_ADD(NOW(6), INTERVAL 1 YEAR)
WHERE `expires_at` IS NULL;

ALTER TABLE `portal_member_user`
  MODIFY COLUMN `expires_at` datetime(6) NOT NULL COMMENT '会员账号过期时间';
```

需要关注：

- 历史会员默认先补当前时间 + 1 年。
- 新增和修改会员接口已要求传 `expiresAt`。
- 如果生产库已经存在 `expires_at` 字段，不要重复执行 `ADD COLUMN`，只执行空值回填和非空收紧。

本轮对会员类型的扩展，仍然通过新增关联表实现：

- 主表继续保留基础信息、下载权限、状态
- 主表新增过期时间
- 类型改为走 `portal_member_business_type_rel`

## 3. 需要同步的初始化数据

以下内容不是“改表”，但属于上线必须同步的数据：

### 3.1 业务类型初始化数据

建议初始化：

- `ENGINEERING` / 工程
- `GOODS` / 货物
- `SERVICE` / 服务

当前代码会在启动时自动补齐。

### 3.2 权限与菜单初始化数据

本轮新增了以下后台权限语义：

- `business:type:view`
- `business:type:create`
- `business:type:edit`
- `business:type:status:update`
- `business:type:delete`
- `member:create`
- `member:edit`
- `member:password:reset`

本轮新增了以下后台菜单语义：

- `SYSTEM_BUSINESS_TYPE`
- 会员管理相关按钮权限
- 类型管理相关按钮权限

当前代码会在启动时通过初始化逻辑自动补齐，不需要手工改表结构。

### 3.3 后台菜单路由对齐

如果生产库已有旧菜单数据，需要把菜单路由同步到当前 `frontend/` 实际路由，否则后台侧边栏点击会进入 404。

```sql
UPDATE sys_menu SET route_path = '/dashboard/console', component = 'dashboard/console' WHERE code = 'DASHBOARD';
UPDATE sys_menu SET route_path = '/profile/index', component = 'profile/index' WHERE code = 'PROFILE';
UPDATE sys_menu SET route_path = '/system/user', component = 'sys/user' WHERE code = 'SYSTEM_ADMIN_USER';
UPDATE sys_menu SET route_path = '/system/member', component = 'sys/member' WHERE code = 'SYSTEM_MEMBER_USER';
UPDATE sys_menu SET route_path = '/system/business-type', component = 'sys/business-type' WHERE code = 'SYSTEM_BUSINESS_TYPE';
UPDATE sys_menu SET route_path = '/tenders', component = 'sys/tender' WHERE code = 'SYSTEM_TENDER';
UPDATE sys_menu SET visible = 0, enabled = 0 WHERE code IN ('SYSTEM_USER', 'SYSTEM_AUDIT_RECORD', 'USER_AUDIT_BUTTON', 'USER_ROLE_BUTTON');
UPDATE sys_menu SET route_path = '/system/role', component = 'sys/role' WHERE code = 'SYSTEM_ROLE';
UPDATE sys_menu SET route_path = '/system/permissions', component = 'sys/permissions' WHERE code = 'SYSTEM_PERMISSION';
UPDATE sys_menu SET route_path = '/system/menu', component = 'sys/menu' WHERE code = 'SYSTEM_MENU';
UPDATE sys_menu SET route_path = '/log', component = 'system/log' WHERE code = 'SYSTEM_OPERATION_LOG';
```

## 4. 上线前必须处理的数据迁移事项

### 4.1 历史会员需要补类型关系

影响：

- 当前代码要求会员必须绑定至少一个启用中的类型
- 如果生产库已有会员，但没有补 `portal_member_business_type_rel`，这些会员将无法正常登录门户
- 当前代码同时要求会员必须有未过期的 `expires_at`

建议处理：

- 上线前盘点历史会员
- 为每个历史会员至少补一条类型关系
- 为历史会员补 `expires_at = 当前时间 + 1 年`

### 4.2 历史招标需要补业务类型

影响：

- 门户查询现在按会员类型集合过滤
- 如果历史招标 `business_type_id` 为空，则不会出现在门户查询结果中

建议处理：

- 上线前为历史 `biz_tender` 全量回填 `business_type_id`
- 回填完成后，再决定是否把 `business_type_id` 改为 `NOT NULL`

## 5. 推荐上线顺序

1. 先在生产库执行本轮表结构变更
2. 再初始化 `biz_business_type` 基础数据
3. 再回填历史会员类型关系
4. 再回填历史会员 `expires_at`
5. 再回填历史招标 `business_type_id`
6. 再同步后台菜单路由数据
7. 最后部署当前后端代码并启动校验

## 6. 一句话结论

本轮真正需要同步到生产库的核心表结构变化有 4 项：

- 新增 `biz_business_type`
- 新增 `portal_member_business_type_rel`
- 给 `biz_tender` 增加 `business_type_id` 外键字段
- 给 `portal_member_user` 增加 `expires_at` 账号过期时间字段

另外，历史会员类型、历史会员过期时间、历史招标类型的数据回填是这次上线能否平稳切换的关键。
