# 后端开发任务清单

更新时间：2026-04-16

说明：

- 本清单已按 2026-04-16 最新沟通口径重新整理。
- 已废弃“会员自注册、会员线上上传资质、后台线上审核入库”这一套旧设定。
- 当前最终口径是：
  - 会员账号由后台管理员直接创建并线下发放
  - 资质在线下确认，不做线上资质上传与审批
  - 会员类型由管理员创建账号时直接选择，可选一个或多个
  - 会员与类型是多对多关系
  - 会员类型不是枚举写死，而是数据库可配置

## 1. 本期最终目标

本期后端目标：

- 系统改为封闭式会员制，非登录用户不能查看任何内容。
- 会员账号不再在线注册，由后台管理员直接创建并线下发放。
- 会员分为多种业务类型，类型数据存数据库，可通过后台“类型管理”菜单配置。
- 工程/货物/服务只是初始化类型数据，不再写死为枚举。
- 管理员创建会员账号时必须选择至少一个会员类型。
- 一个会员可同时绑定多个会员类型。
- 会员登录后只能查看与自己类型集合匹配的招标信息。
- 招标信息必须绑定业务类型，门户按会员类型集合做隔离。
- 超级管理员继续管理后台管理员账号。
- 会员是否允许下载文件、会员是否启用/禁用，仍由后台控制。
- 文件存储当前可复用本地存储实现，后续可平滑切 OSS。

## 2. 已确认的业务口径

### 2.1 会员准入方式

- 不做门户在线注册
- 不做会员在线资质上传
- 不做会员线上审批流
- 资质由线下确认
- 线下确认完成后，管理员在后台创建会员账号并发放给会员

### 2.2 会员类型机制

- 会员类型来源于数据库
- 后台要新增“类型管理”菜单
- 管理员可通过页面维护类型数据
- 工程/货物/服务只是初始化数据
- 后续若要新增新类型，可直接通过后台新增
- 一个会员可绑定多个类型
- 会员与类型应按多对多关系设计

### 2.3 招标类型机制

- 当前建议一个招标只归属一个业务类型
- 门户隔离时，会员按“类型集合”匹配招标
- 也就是：会员有多个类型时，可看到这些类型对应招标的并集

### 2.4 访问隔离机制

- 门户必须登录后访问
- 不同类型会员只能看自己类型集合覆盖到的招标
- 招标详情和附件下载也必须按会员类型集合校验

### 2.5 会员后台管理

- 管理员可创建会员账号
- 管理员创建会员时可直接勾选一个或多个类型
- 管理员可修改会员类型集合
- 管理员可启用/禁用会员
- 管理员可设置会员是否允许下载文件
- 管理员可重置会员密码

## 3. 当前后端代码哪些必须调整

### 3.1 必须停用的旧能力

- 会员自注册必须停用：
  - [PortalAuthController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/PortalAuthController.java:36)
  - [PortalAuthService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/PortalAuthService.java:38)
- 门户匿名访问必须关闭：
  - [SecurityConfig.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/SecurityConfig.java:52)
  - [PortalTenderController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/PortalTenderController.java:32)

### 3.2 必须扩展的现有模型

- 会员主表需要配合“会员-类型关联表”支持多类型：
  - [MemberUser.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/MemberUser.java:12)
- 招标表必须补类型字段：
  - [Tender.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/Tender.java:13)

### 3.3 必须扩展的现有后台能力

- 会员管理现在能力不够，只能查列表、改下载权限、改状态：
  - [MemberAdminController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/MemberAdminController.java:22)
  - [MemberAdminService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/MemberAdminService.java:16)
- 需要扩展成完整的“会员账号管理”，并支持为会员绑定多个类型

### 3.4 必须改造的门户隔离逻辑

- 当前门户招标查询没有按会员类型集合过滤：
  - [TenderRepository.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/repository/TenderRepository.java:34)
  - [PortalTenderService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/PortalTenderService.java:57)

### 3.5 应废弃或清理的旧语义

- 旧的“用户审核”模块与当前新口径不符，应迁移或废弃：
  - [AdminUserController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AdminUserController.java:25)
  - [AdminUserService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AdminUserService.java:30)
- 初始化权限/菜单里仍保留旧 `user:*` 语义，需要重整：
  - [DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java:51)

## 4. 建议的数据模型调整

## 4.1 新增业务类型表

建议新增表：`biz_business_type`

用途：

- 统一维护会员类型和招标类型
- 后台“类型管理”菜单直接维护此表
- 工程/货物/服务作为初始化数据写入数据库

建议字段：

- `id`
- `code`
- `name`
- `enabled`
- `sort_order`
- `description`
- `created_at`
- `updated_at`

建议规则：

- `code` 唯一
- `name` 唯一
- 允许新增新类型
- 允许启用/禁用
- 若已被会员或招标引用，则不允许物理删除，建议只禁用

初始化建议：

- `ENGINEERING` / 工程
- `GOODS` / 货物
- `SERVICE` / 服务

## 4.2 调整会员主表与会员类型关联表

当前表：`portal_member_user`

当前字段见 [MemberUser.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/MemberUser.java:12)

建议：

- `portal_member_user` 主表中不再增加单一 `business_type_id`
- 新增会员与类型关联表，例如：
  - `portal_member_business_type`
  - 或 `portal_member_business_type_rel`

关联表建议字段：

- `id`
- `member_user_id`
- `business_type_id`
- `created_at`

主表可选新增字段：

- `initial_password_reset_required`：可选，首次登录是否强制修改密码

建议保留字段：

- `can_download_file`
- `status`

说明：

- 会员与类型应按多对多建模，而不是单字段建模。
- 当前资质确认是线下完成，因此不需要新增“资质材料表”“审核状态表”“线上审核记录表”。
- 如果后续想在系统里保留线下确认备注，可选增加：
  - `remark`
  - `offline_verified_at`
  - `offline_verified_by`
  但这不是当前必需项。

## 4.3 调整招标表

当前表：`biz_tender`

当前字段见 [Tender.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/Tender.java:13)

建议新增字段：

- `business_type_id`

用途：

- 让每条招标归属某个业务类型
- 门户侧按会员类型集合隔离显示

说明：

- 当前建议“一个招标对应一个业务类型”。
- 如果未来明确提出“一个招标同时属于多个类型”，再额外新增招标与类型关联表。

## 5. 完整开发任务清单

## M1 会员体系口径调整

### T1.1 停用会员自注册

- [ ] 停用 `POST /api/portal/auth/register`
- [ ] 停用 [PortalAuthService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/PortalAuthService.java:38) 中直接创建会员的逻辑
- [ ] Swagger 文档中明确该接口停用
- [ ] 测试中移除“会员自注册成功”的旧断言

验收标准：

- 会员不能自行在线注册
- 会员账号只能由后台创建

### T1.2 门户改为封闭访问

- [ ] 修改 [SecurityConfig.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/SecurityConfig.java:52)，移除门户匿名放行
- [ ] 修改 [PortalTenderController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/PortalTenderController.java:32)，列表/详情也要求会员登录
- [ ] 校验门户所有接口都基于会员 token 访问

验收标准：

- 非登录用户不能查看招标列表、详情、附件
- 只有会员登录后才能访问门户内容

## M2 类型管理模块

### T2.1 新增业务类型实体、仓储、表结构

- [ ] 新增 `BusinessType.java`
- [ ] 新增 `BusinessTypeRepository.java`
- [ ] 新建 `biz_business_type` 表
- [ ] 初始化工程/货物/服务 3 条类型数据

验收标准：

- 业务类型存储在数据库中
- 不再通过枚举硬编码类型值

### T2.2 新增后台“类型管理”菜单与权限

- [ ] 在 [DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java:51) 中新增菜单：
  - `SYSTEM_BUSINESS_TYPE`
- [ ] 新增权限：
  - `business:type:view`
  - `business:type:create`
  - `business:type:edit`
  - `business:type:status:update`
  - `business:type:delete`
- [ ] 将权限分配给超级管理员和系统管理员

验收标准：

- 后台具备独立“类型管理”菜单
- 管理员可配置业务类型

### T2.3 新增类型管理接口

- [ ] `GET /api/admin/business-types`
- [ ] `GET /api/admin/business-types/options`
- [ ] `POST /api/admin/business-types`
- [ ] `PUT /api/admin/business-types/{id}`
- [ ] `PUT /api/admin/business-types/{id}/status`
- [ ] `DELETE /api/admin/business-types/{id}`

建议规则：

- 删除前检查是否已被会员或招标引用
- 被引用时只允许禁用，不允许物理删除

验收标准：

- 业务类型可通过后台页面配置
- 新增类型后可被会员与招标直接引用

## M3 会员账号后台发放

### T3.1 扩展会员主表与会员类型关联表结构

- [ ] 新增会员类型关联表，例如 `portal_member_business_type`
- [ ] 明确会员创建时必须选择至少一个类型
- [ ] 明确会员编辑时支持多选类型
- [ ] 如采用首次改密策略，增加首次登录标记字段

验收标准：

- 每个会员都必须绑定至少一个业务类型
- 一个会员可绑定多个业务类型
- 管理员创建会员时即可配置类型集合

### T3.2 改造后台会员管理接口

当前 [MemberAdminController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/MemberAdminController.java:22) 只有查列表、改下载权限、改状态。

需要新增：

- [ ] `GET /api/admin/members`
- [ ] `GET /api/admin/members/{id}`
- [ ] `POST /api/admin/members`
- [ ] `PUT /api/admin/members/{id}`
- [ ] `PUT /api/admin/members/{id}/status`
- [ ] `PUT /api/admin/members/{id}/download-access`
- [ ] `PUT /api/admin/members/{id}/password`

验收标准：

- 管理员可直接创建会员账号
- 管理员可设置会员类型集合
- 管理员可重置密码、禁用账号、修改下载权限

### T3.3 扩展会员管理服务

需要改造：

- [ ] [MemberAdminService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/MemberAdminService.java:16)

新增职责：

- [ ] 创建会员
- [ ] 编辑会员资料
- [ ] 绑定多个业务类型
- [ ] 重置会员密码
- [ ] 修改下载权限
- [ ] 修改启用禁用状态

验收标准：

- 会员后台管理链路完整闭环

## M4 会员登录与会员可见范围控制

### T4.1 保留会员登录，移除会员自注册

- [ ] 保留 `POST /api/portal/auth/login`
- [ ] 保留 `GET /api/portal/auth/me`
- [ ] 删除或停用 `/api/portal/auth/register`

验收标准：

- 会员只能登录，不能自行注册

### T4.2 登录时校验会员状态和类型

需要改造：

- [ ] [PortalAuthService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/PortalAuthService.java:57)

登录校验建议：

- [ ] 账号禁用不能登录
- [ ] 未分配任何业务类型的账号不能登录，或登录后不能访问业务接口
- [ ] 若会员绑定的类型已被禁用，需明确登录与访问策略

验收标准：

- 会员登录后权限边界清晰

### T4.3 会员可见范围由业务类型决定

- [ ] 登录态中可获取当前会员的 `businessTypeIds`
- [ ] 门户业务统一按会员类型集合过滤数据
- [ ] 若会员未绑定任何类型，则禁止查看招标内容

验收标准：

- 工程类会员只能看工程类招标
- 货物类会员只能看货物类招标
- 多类型会员可同时看到其类型集合覆盖到的所有招标
- 服务类会员后续启用后可独立生效

## M5 招标管理按类型隔离

### T5.1 扩展招标表与 DTO

- [ ] 给 `biz_tender` 增加 `business_type_id`
- [ ] 扩展招标 DTO，返回类型信息
- [ ] 新增类型校验，确保招标创建时必须绑定业务类型

涉及代码：

- [ ] [Tender.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/Tender.java:13)
- [ ] `TenderDto / TenderDetailDto / TenderListItemDto / TenderUpsertRequest`

验收标准：

- 每条招标必须归属某个业务类型

### T5.2 改造后台招标管理接口

- [ ] 后台列表支持按类型筛选
- [ ] 创建/编辑招标时支持选择类型
- [ ] 招标详情返回类型信息

验收标准：

- 后台可按业务类型维护招标

## M6 门户招标查询改造

### T6.1 门户列表按会员类型过滤

需要改造：

- [ ] [TenderRepository.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/repository/TenderRepository.java:34)
- [ ] [PortalTenderService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/PortalTenderService.java:57)

过滤规则：

- [ ] 只查询 `status=PUBLISHED`
- [ ] 只查询 `publishAt <= now`
- [ ] 只查询 `businessTypeId in 当前会员.businessTypeIds`

验收标准：

- 不同类型会员看到的数据完全隔离
- 多类型会员可看到多个类型的并集数据

### T6.2 门户详情与下载也按类型校验

- [ ] 详情查询增加业务类型集合校验
- [ ] 下载接口增加业务类型集合校验
- [ ] 下载仍保留 `canDownloadFile` 控制

验收标准：

- 会员无法查看或下载非本类型招标附件
- 多类型会员可查看其任一已绑定类型下的招标与附件

## M7 旧模块迁移与清理

### T7.1 旧“用户审核”模块迁移

当前旧模块：

- [AdminUserController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AdminUserController.java:25)
- [AdminUserService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AdminUserService.java:30)

处理建议：

- [ ] 将旧 `user:*` 权限标记为遗留
- [ ] 将旧用户审核接口逐步废弃
- [ ] 不再新增任何会员线上审核逻辑到该模块

验收标准：

- 后台不存在与现行口径冲突的“审核用户”逻辑

### T7.2 初始化权限与菜单重整

需要改造：

- [ ] [DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java:51)

调整内容：

- [ ] 保留：管理员管理、会员管理、类型管理、招标管理、角色管理、权限管理、菜单管理、操作日志
- [ ] 新增：类型管理菜单与权限
- [ ] 新增：会员创建、会员编辑、会员重置密码等权限
- [ ] 废弃：`SYSTEM_USER`、`SYSTEM_AUDIT_RECORD`、`user:*`

验收标准：

- 菜单与权限语义与新系统一致

## M8 文件存储与部署能力

### T8.1 文件存储能力抽象

当前能力：

- [LocalFileStorageService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/LocalFileStorageService.java:28)

建议改造：

- [ ] 抽象 `FileStorageService` 接口
- [ ] 保留本地实现
- [ ] 预留 OSS 实现

验收标准：

- 当前可先本地存储
- 后续切 OSS 时不影响业务层

### T8.2 增加部署配置说明

- [ ] 明确生产文件目录
- [ ] 明确未来切 OSS 时的配置项
- [ ] 明确测试环境与正式环境的存储差异

验收标准：

- 测试环境和正式环境部署有清晰配置说明

## M9 测试与交付

### T9.1 集成测试补齐

至少补这些测试：

- [ ] 超级管理员创建管理员
- [ ] 后台创建会员账号
- [ ] 创建会员时必须选择至少一个类型
- [ ] 创建会员时可选择多个类型
- [ ] 会员登录
- [ ] 门户未登录不可访问
- [ ] 工程类会员只能看工程类招标
- [ ] 货物类会员只能看货物类招标
- [ ] 多类型会员可同时看到多个类型的招标
- [ ] 多类型会员可下载其任一已绑定类型下的附件
- [ ] 下载权限开关生效
- [ ] 禁用账号后 token 失效
- [ ] 类型管理增删改查
- [ ] 类型被会员或招标引用时不可删除

### T9.2 Swagger 与文档更新

- [ ] 更新 Swagger 分组
- [ ] 更新后端分析文档
- [ ] 更新部署说明

### T9.3 数据迁移方案

- [ ] 历史会员如何补类型集合
- [ ] 历史招标如何补业务类型
- [ ] 旧 `user:*` 相关菜单和权限如何清理

## 6. 推荐实施顺序

推荐按以下顺序推进：

1. 先做 `biz_business_type` 和“类型管理”模块
2. 再新增会员与类型关联表，完成会员-类型多对多建模
3. 再改后台会员创建、编辑、重置密码、下载权限、启停状态
4. 再停用会员自注册，关闭门户匿名访问
5. 再给招标补 `business_type_id`
6. 再改门户查询与下载隔离逻辑
7. 最后清理旧 `user:*` 模块、补测试和部署文档

## 7. 完成标准

满足以下条件即可认为这一期后端完成：

- 后台可配置业务类型，工程/货物/服务只是初始化数据
- 超级管理员可管理管理员账号
- 后台可创建会员账号并在创建时选择一个或多个类型
- 会员只能登录，不能自注册
- 门户必须登录后访问
- 会员只能看到自己类型集合覆盖到的招标
- 会员下载仍受下载权限控制
- 旧“公开注册会员”和“游客查看门户”的链路被关闭
- 不再包含线上资质上传与线上审批逻辑

## 8. 一句话结论

这次后端改造的正确方向，不是“做会员上传资质审批流”，而是“做封闭式会员制 + 后台发放账号 + 会员类型多对多可配置化 + 招标按类型平行隔离”。
