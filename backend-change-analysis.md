# 后端改造分析清单

更新时间：2026-04-15

## 1. 结论先看

按你刚确认的新口径：

- 管理后台不需要注册
- 管理员账号只由超级管理员新增
- 会员才需要注册
- 现有后台管理能力尽量复用

基于这个口径，当前后端应该这样判断：

- 后台管理这部分复用度很高，可以继续以现有 `sys_user + 角色/权限/菜单/JWT` 为基础演进。
- 真正需要新做的重点，不在后台骨架本身，而在“会员用户域、招标业务域、本地文件域、门户公开接口”。
- 这次改造不是把后台推倒重来，而是“保留现有后台体系，去掉后台注册语义，再新增门户会员和招标业务能力”。

也就是说，后端目标应调整为：

- 管理员域：复用现有后台体系
- 会员域：新增独立用户表和认证链路
- 招标业务域：新增
- 本地文件存储域：新增
- 门户公开访问接口：新增

## 2. 现有后端现状

### 2.1 用户域现状

当前只有一张用户表：

- `sys_user` 对应 [User.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/User.java)

这张表在当前实现里同时承担了：

- 后台登录主体
- 旧版公开注册主体
- 旧版审核主体
- 后台角色分配主体

相关链路全部建立在这一个实体上：

- [UserRepository.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/repository/UserRepository.java)
- [AuthService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AuthService.java)
- [AdminUserService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AdminUserService.java)
- [ProfileService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/ProfileService.java)
- [CustomUserDetailsService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/CustomUserDetailsService.java)
- [LoginUser.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/LoginUser.java)

它当前的问题不是“后台不能用”，而是“后台管理员和门户会员混在了一张表里”。

按你最新确认，正确方向应是：

- `sys_user` 继续保留给后台管理员
- 门户会员单独新增一张表

### 2.2 权限模型现状

当前 RBAC 模型是完整可用的，适合后台管理员：

- 角色表 `sys_role`
- 权限表 `sys_permission`
- 菜单表 `sys_menu`
- 用户角色关联 `sys_user_role`
- 角色权限关联 `sys_role_permission`
- 角色菜单关联 `sys_role_menu`

相关代码：

- [Role.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/Role.java)
- [Permission.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/Permission.java)
- [Menu.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/Menu.java)
- [DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java)

这套模型建议继续只服务“管理员后台”，不要让会员用户混进这套角色菜单体系。

### 2.3 安全链路现状

当前安全配置只有一套：

- [SecurityConfig.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/SecurityConfig.java)
- [JwtTokenProvider.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/JwtTokenProvider.java)
- [JwtAuthenticationFilter.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/JwtAuthenticationFilter.java)

当前公开放行的只有：

- `/api/auth/**`
- Swagger
- 静态资源

这不满足门户需求，因为门户需要：

- 游客可访问招标列表和详情
- 会员可登录门户
- 会员下载文件时做权限校验

### 2.4 业务域现状

当前后端没有以下内容：

- 招标公告表
- 招标附件表
- 文件存储表
- 本地文件上传接口
- 本地文件下载接口
- 门户招标列表接口
- 门户招标详情接口

也就是说，“招标管理”和“门户公告浏览”在后端目前是 0 起步。

### 2.5 可直接复用的后台部分

按“管理员不注册、后台继续复用”的前提，下面这些后端能力都建议继续保留：

- `sys_user` 管理员表及其多角色模型
- `sys_role / sys_permission / sys_menu` 这套 RBAC
- `/api/auth/login` 和 `/api/auth/me` 这套管理员登录链路
- Spring Security + JWT 鉴权骨架
- 角色管理、权限管理、菜单管理、操作日志
- 管理员个人信息维护
- 数据初始化里的超级管理员、默认角色、默认权限、默认菜单

最需要调整的不是“后台骨架”，而是：

- 去掉管理员公开注册语义
- 把原“用户管理”从混合用户模型改成“管理员管理”
- 新增“会员用户管理”
- 新增“招标管理”

### 2.6 测试现状

当前测试只覆盖了旧的混合用户模型：

- [AuthFlowIntegrationTests.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/test/java/com/zhaobiao/admin/AuthFlowIntegrationTests.java)

测试逻辑目前还是旧模型：

- 用户公开注册
- 管理员审核
- 给用户分角色
- 用户再登录

这套测试在你现在的新方案下也需要重写，因为后台管理员不再走“公开注册 + 审核”流程。

## 3. 需求对照后的必须改造项

## 3.1 后台管理员继续复用 `sys_user`，会员单独新增一张表

### 当前问题

当前 `sys_user` 同时承载管理员和门户用户，不符合“不能串”的要求。

### 建议方案

建议采用“后台管理员继续使用 `sys_user` + 门户会员新增独立表”的最小风险方案。

原因：

- 现有后台 RBAC、菜单、角色、权限都已经绑定 `sys_user`
- 超级管理员和后台权限体系已经围绕 `sys_user` 成型
- 后台登录、后台鉴权、后台菜单能力基本都已经可用
- 直接把 `sys_user` 继续当管理员表，改动最小，复用度最高

### 建议新增/保留的数据表

建议保留：

- `sys_user`：管理员用户表
- `sys_user_role`：管理员用户与角色关系

建议新增：

- `portal_member_user`：会员用户表

会员表示例字段建议：

| 字段 | 说明 |
| --- | --- |
| id | 主键 |
| username | 登录名 |
| password | 密码 |
| phone | 手机号 |
| email | 邮箱 |
| real_name | 真实姓名 |
| company_name | 公司名称 |
| contact_person | 联系人 |
| unified_social_credit_code | 统一社会信用代码 |
| can_download_file | 是否允许下载招标文件 |
| status | 启用/禁用状态 |
| last_login_at | 最后登录时间 |
| created_at | 创建时间 |
| updated_at | 更新时间 |

### 对现有后台代码的复用判断

可以直接复用或轻改：

- [User.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/User.java)
- [UserRepository.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/repository/UserRepository.java)
- [LoginUser.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/LoginUser.java)
- [CustomUserDetailsService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/CustomUserDetailsService.java)
- [ProfileService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/ProfileService.java)
- [DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java)
- [ViewMapper.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/mapper/ViewMapper.java)

需要修改的重点不是“后台还能不能用”，而是要去掉旧的公开注册语义，并把它和会员域拆开。

### 需要新增/修改的现有后端类

必须修改：

- [User.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/User.java)
- [UserRepository.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/repository/UserRepository.java)
- [AuthService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AuthService.java)
- [AuthController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AuthController.java)
- [CustomUserDetailsService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/CustomUserDetailsService.java)
- [LoginUser.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/LoginUser.java)
- [JwtTokenProvider.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/JwtTokenProvider.java)
- [JwtAuthenticationFilter.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/JwtAuthenticationFilter.java)
- [ProfileService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/ProfileService.java)
- [ViewMapper.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/mapper/ViewMapper.java)

必须新增：

- `MemberUser.java`
- `MemberUserRepository.java`
- `MemberUserDto.java`
- `MemberLoginRequest.java`
- `MemberRegisterRequest.java`
- `PortalAuthService.java`
- `PortalAuthController.java`
- `PortalMemberProfileController.java`

### 认证链路必须拆分

当前只有 `/api/auth/*` 一套认证。

建议改造为：

- 管理后台继续使用 `/api/auth/*`
- 门户会员新增 `/api/portal/auth/*`

建议接口分组：

- `POST /api/auth/login`：管理员登录
- `GET /api/auth/me`：管理员当前用户
- `POST /api/admin/admin-users`：超级管理员创建管理员
- `POST /api/portal/auth/register`：会员注册
- `POST /api/portal/auth/login`：会员登录
- `GET /api/portal/auth/me`：当前会员信息

需要特别说明：

- 现有 `POST /api/auth/register` 不应再用于后台管理员注册
- 这个接口建议废弃、关闭，或改造成仅门户会员注册并迁移到 `/api/portal/auth/register`

### JWT 需要修改的原因

当前 JWT 只存：

- username
- userId
- roleCodes

这在双表模型下不够，因为以后可能出现：

- 管理员用户名和会员用户名重名
- 同一个 token 解析后不知道去哪个表查

建议 JWT 至少增加：

- `userType`：`ADMIN` / `MEMBER`

更稳妥的做法：

- `sub` 不再只存 username
- `sub` 或 claims 里明确存 `userType + id`

## 3.2 后台管理模块大部分复用，但“用户管理”语义要重构

### 当前问题

当前“用户管理”实际上是旧模型下的混合用户管理。

现有代码：

- [AdminUserController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AdminUserController.java)
- [AdminUserService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AdminUserService.java)

当前能力是：

- 查用户
- 审核用户
- 给用户分角色
- 查审核记录

这与新需求不一致。

按你现在的新要求，这里要改成：

- 管理员管理：复用现有后台用户管理骨架
- 会员用户管理：新增独立后台菜单和接口

### 新需求落地后，后台应拆成两个菜单

建议后台新增两个明确模块：

- 管理员管理
- 会员用户管理

### 后台管理里哪些东西可以直接复用

这几块基本可以直接延续：

- 角色管理
- 权限管理
- 菜单管理
- 操作日志
- 管理员登录
- 管理员个人资料维护
- 超级管理员初始化逻辑

真正要重构的，是原“用户管理”模块的语义。

### 管理员管理模块需要的后端能力

超级管理员应能创建其他后台管理员。

因此需要新增接口：

- 管理员列表
- 新增管理员
- 编辑管理员基本信息
- 启用/禁用管理员
- 重置管理员密码
- 给管理员分配角色

建议接口组：

- `GET /api/admin/admin-users`
- `POST /api/admin/admin-users`
- `PUT /api/admin/admin-users/{id}`
- `PUT /api/admin/admin-users/{id}/status`
- `PUT /api/admin/admin-users/{id}/password/reset`
- `PUT /api/admin/admin-users/{id}/roles`

这里建议复用现有后台模型，而不是新起一套管理员域。

建议对现有代码的处理方式：

- [AdminUserController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AdminUserController.java) 保留，但改语义
- [AdminUserService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AdminUserService.java) 保留，但改逻辑

旧的这些能力建议删除或废弃：

- 管理员公开注册
- 管理员审核
- 管理员审核记录主流程

### 会员用户管理模块需要的后端能力

根据你的描述，会员管理重点不在 RBAC，而在业务属性：

- 是否允许下载文件
- 启用/禁用状态

因此需要接口：

- 会员列表
- 查看会员详情
- 修改会员是否可下载文件
- 修改会员启用/禁用状态

建议接口组：

- `GET /api/admin/members`
- `GET /api/admin/members/{id}`
- `PUT /api/admin/members/{id}`
- `PUT /api/admin/members/{id}/download-permission`
- `PUT /api/admin/members/{id}/status`

### 需要调整的初始化权限和菜单

[DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java) 需要新增一批权限和菜单种子数据。

建议新增权限：

- `admin:user:view`
- `admin:user:create`
- `admin:user:edit`
- `admin:user:status:update`
- `admin:user:role:update`
- `member:view`
- `member:edit`
- `member:download:update`
- `member:status:update`
- `tender:view`
- `tender:create`
- `tender:edit`
- `tender:delete`
- `tender:file:upload`

建议新增后台菜单：

- 管理员管理
- 会员用户管理
- 招标管理

### 当前审核模型需要重新定义

当前存在：

- [UserStatus.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/UserStatus.java)
- [UserAuditRecord.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/UserAuditRecord.java)

这套模型是“公开注册后，管理员审核，再赋后台角色”的旧设计。

新需求下不再自然成立：

- 管理员不是公开注册，而是超级管理员创建
- 会员需求里只明确提到了“启用/禁用”和“是否可下载”

所以可以先明确一条：

- 后台管理员这条线，`审核` 不再是主流程能力

这里需要你后续确认一个业务点：

- 会员注册后是否还要审核

如果“不审核”，则：

- `UserAuditRecord` 这套逻辑不再适用于会员域
- 后台管理员的 `PENDING / APPROVED / REJECTED` 这套状态语义应弱化或移除

如果“保留会员审核”，则建议新增：

- `portal_member_audit_record`

而不是继续复用后台管理员这套旧逻辑。

## 3.3 招标管理需要新增完整业务域

### 当前问题

当前后端没有任何招标相关实体、表、接口、服务。

### 建议新增数据表

建议至少新增 3 张表：

#### 1. `biz_tender`

保存招标主信息。

建议字段：

| 字段 | 说明 |
| --- | --- |
| id | 主键 |
| title | 标题 |
| region | 地区 |
| publish_at | 发布时间 |
| content | 富文本正文，建议 `LONGTEXT` |
| contact_person | 联系人 |
| budget | 预算 |
| contact_phone | 联系方式 |
| tender_unit | 招标单位 |
| deadline | 截止时间 |
| project_code | 项目编号 |
| signup_deadline | 报名截止时间 |
| status | 草稿/已发布/已关闭，建议增加 |
| created_by | 创建人管理员 ID |
| updated_by | 修改人管理员 ID |
| created_at | 创建时间 |
| updated_at | 更新时间 |

#### 2. `biz_file_storage`

保存本地文件元数据。

建议字段：

| 字段 | 说明 |
| --- | --- |
| id | 主键 |
| original_name | 原始文件名 |
| stored_name | 磁盘存储名 |
| stored_path | 磁盘完整路径或相对路径 |
| content_type | MIME 类型 |
| file_size | 文件大小 |
| file_ext | 扩展名 |
| sha256 | 文件摘要，建议增加 |
| created_by | 上传人管理员 ID |
| created_at | 创建时间 |
| updated_at | 更新时间 |

#### 3. `biz_tender_attachment`

保存招标与文件的关联。

建议字段：

| 字段 | 说明 |
| --- | --- |
| id | 主键 |
| tender_id | 招标 ID |
| file_id | 文件 ID |
| sort_order | 排序 |
| created_at | 创建时间 |
| updated_at | 更新时间 |

### 建议新增的后端模块

建议新增：

- `Tender.java`
- `TenderFileStorage.java`
- `TenderAttachment.java`
- `TenderRepository.java`
- `TenderFileStorageRepository.java`
- `TenderAttachmentRepository.java`
- `TenderDto.java`
- `TenderListItemDto.java`
- `TenderDetailDto.java`
- `TenderUpsertRequest.java`
- `FileUploadResponse.java`
- `TenderService.java`
- `TenderAdminController.java`
- `PortalTenderController.java`
- `LocalFileStorageService.java`
- `FileDownloadController.java`

### 后台招标管理需要的接口

建议后台提供：

- 招标分页列表
- 招标详情
- 新增招标
- 修改招标
- 删除招标
- 上传附件
- 绑定/解绑附件

建议接口组：

- `GET /api/admin/tenders`
- `GET /api/admin/tenders/{id}`
- `POST /api/admin/tenders`
- `PUT /api/admin/tenders/{id}`
- `DELETE /api/admin/tenders/{id}`
- `POST /api/admin/files/upload`
- `POST /api/admin/tenders/{id}/attachments`
- `DELETE /api/admin/tenders/{id}/attachments/{attachmentId}`

### 富文本内容存储建议

你要求内容是富文本。

后端建议：

- 数据库存 HTML 字符串
- 字段用 `LONGTEXT`
- 接口层不要做富文本结构解析
- 仅做基础长度校验和 XSS 风险控制

## 3.4 文件上传下载需要新增本地文件模块

### 当前问题

当前后端没有本地文件上传下载能力。

虽然项目里有静态目录和门户演示附件：

- [ztbfb/attachments/tender-bidding-file-sample.txt](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/ztbfb/attachments/tender-bidding-file-sample.txt)

但这只是前端演示资源，不是后端文件管理模块。

### 必须新增的后端能力

- `multipart/form-data` 上传
- 保存文件到本地目录
- 保存文件元数据到数据库
- 下载时按权限校验
- 不向前端暴露真实磁盘路径

### 建议新增配置

建议在配置里新增：

- `app.file.storage-path`
- `app.file.temp-path`
- `spring.servlet.multipart.max-file-size`
- `spring.servlet.multipart.max-request-size`

### 下载权限规则建议

门户下载文件时，必须满足：

- 已登录会员
- 会员状态为启用
- `canDownloadFile = true`
- 文件确实属于该招标公告

下载不要直接返回物理文件 URL，建议统一走受控接口：

- `GET /api/portal/tenders/{tenderId}/attachments/{attachmentId}/download`

## 3.5 门户公开访问接口需要新增

### 当前问题

当前 [SecurityConfig.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/SecurityConfig.java) 没有为门户业务预留公开接口。

### 新需求对应的门户接口

游客可访问：

- 招标公告分页列表
- 招标公告详情

会员登录后可访问：

- 当前会员信息
- 文件下载

建议接口组：

- `GET /api/portal/tenders`
- `GET /api/portal/tenders/{id}`
- `POST /api/portal/auth/register`
- `POST /api/portal/auth/login`
- `GET /api/portal/auth/me`
- `GET /api/portal/tenders/{tenderId}/attachments/{attachmentId}/download`

### 门户列表接口要求

你的要求里明确提到：

- 分页
- 倒序排列

因此列表查询必须支持：

- `pageNum`
- `pageSize`
- 默认按 `publishAt desc, id desc`

### 门户详情接口要求

游客进入详情页时，应返回：

- 标题
- 地区
- 发布时间
- 富文本正文
- 联系人
- 预算
- 联系方式
- 招标单位
- 截止时间
- 项目编号
- 报名截止时间
- 附件文件名列表

但不应直接给游客返回可下载的裸链接。

附件 DTO 建议返回：

| 字段 | 说明 |
| --- | --- |
| id | 附件 ID |
| fileName | 文件名 |
| canDownload | 当前登录用户是否可下载 |

## 4. 现有代码中必须重点修改的文件

| 文件 | 当前职责 | 需要调整 |
| --- | --- | --- |
| [User.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/User.java) | 旧混合用户实体 | 明确收敛为管理员实体，后台继续复用 |
| [UserRepository.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/repository/UserRepository.java) | 旧混合用户仓储 | 明确收敛为管理员仓储，后台继续复用 |
| [AuthController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AuthController.java) | 单一认证入口 | 保留管理员登录/当前用户，关闭管理员注册，新增门户认证控制器 |
| [AuthService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AuthService.java) | 注册/登录/当前用户 | 保留管理员登录能力，去掉管理员公开注册语义，门户会员另起服务 |
| [AdminUserController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AdminUserController.java) | 旧用户管理 | 复用为管理员管理主控制器，会员管理另起控制器 |
| [AdminUserService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AdminUserService.java) | 旧用户管理服务 | 复用为管理员管理主服务，去掉旧注册审核语义 |
| [ProfileService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/ProfileService.java) | 单一用户资料维护 | 仅处理管理员资料，门户会员资料另起服务 |
| [CustomUserDetailsService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/CustomUserDetailsService.java) | 只查一张用户表 | 可继续作为管理员专用，再新增会员鉴权加载逻辑 |
| [LoginUser.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/LoginUser.java) | 后台用户 principal | 增加用户类型，或新增会员 principal |
| [JwtTokenProvider.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/JwtTokenProvider.java) | JWT 只认一类用户 | 增加 `userType` claim |
| [JwtAuthenticationFilter.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/JwtAuthenticationFilter.java) | 单一用户鉴权 | 支持管理员/会员双域解析 |
| [SecurityConfig.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/SecurityConfig.java) | 只开放后台认证入口 | 开放门户公开接口与门户认证接口 |
| [DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java) | 初始化旧权限和角色 | 增加管理员管理、会员管理、招标管理权限与菜单 |
| [ViewMapper.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/mapper/ViewMapper.java) | 只映射旧用户/角色/菜单 | 增加会员、招标、附件 DTO 映射 |
| [AuthFlowIntegrationTests.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/test/java/com/zhaobiao/admin/AuthFlowIntegrationTests.java) | 测试旧混合用户流程 | 需要重写成管理员域和会员域分开的测试 |

## 5. 当前后端还缺少的新增文件类型

必须新增的代码类型大致如下：

- 新实体：会员、招标、文件、附件关系
- 新仓储：会员仓储、招标仓储、文件仓储、附件关系仓储
- 新 DTO：会员列表/详情、招标列表/详情、文件上传响应
- 新请求体：会员注册、管理员创建、会员状态修改、招标新增/修改
- 新 Service：门户认证、会员管理、招标管理、本地文件存储
- 新 Controller：门户认证、门户招标、后台会员管理、后台招标管理、文件上传下载
- 新配置：文件存储目录、Multipart 限制
- 新测试：管理员管理、会员注册登录、招标列表详情、下载权限校验

## 6. 实施顺序建议

建议按下面顺序做，不要一口气混着改：

### 第一阶段：先收敛后台管理员语义，再拆会员域

- 保留 `sys_user` 作为管理员表
- 去掉后台管理员公开注册语义
- 把 `/api/auth/*` 收敛为管理员登录链路
- 新增 `portal_member_user`
- 改 JWT 支持双域
- 改安全配置
- 把管理员认证和门户认证拆开

### 第二阶段：再补后台管理能力

- 管理员管理接口
- 会员用户管理接口
- 原“审核用户”逻辑下线或改造
- 数据初始化新增菜单和权限

### 第三阶段：再补招标业务

- 招标表
- 文件表
- 附件关系表
- 后台招标管理接口
- 本地文件上传接口

### 第四阶段：最后补门户接口

- 门户公告分页列表
- 门户公告详情
- 门户登录注册
- 下载权限校验

### 第五阶段：补测试和迁移

- 重写集成测试
- 补初始化数据脚本
- 评估数据库迁移方案

## 7. 需要你确认的业务点

这些点会直接影响后端设计，建议在真正开工前确定：

### 7.1 会员是否需要审核

当前需求只明确了：

- 启用/禁用
- 是否可下载文件

没有明确“注册后是否还要审核”。

### 7.2 会员注册字段最终要哪些

当前旧系统注册字段很多：

- 手机号
- 邮箱
- 公司名称
- 联系人
- 统一社会信用代码

而门户前端演示登录目前只有用户名密码，没有真实注册页。

### 7.3 招标是否需要草稿/发布状态

如果需要后台先保存草稿再发布，就要加：

- `draft`
- `published`
- `closed`

否则只能直接录入即对外可见。

### 7.4 管理员是否允许多角色

当前后台已经支持多角色，建议保留，不要退回单角色。

## 8. 复用结论

结合你这次的新澄清，可以把“复用度”明确分成三档：

### 8.1 高复用

这些建议直接保留并在原基础上改：

- 管理员表 `sys_user`
- 角色、权限、菜单、操作日志
- 后台登录认证骨架
- Spring Security + JWT
- 角色管理、权限管理、菜单管理
- 超级管理员初始化

### 8.2 中复用

这些建议保留类和主体结构，但改语义：

- `AuthController / AuthService`
- `AdminUserController / AdminUserService`
- `ProfileService`
- `LoginUser / JwtTokenProvider / JwtAuthenticationFilter`
- `AuthFlowIntegrationTests`

### 8.3 低复用或新增

这些基本要新做：

- 会员表及会员认证
- 会员用户管理
- 招标公告业务
- 招标附件关系
- 本地文件上传下载
- 门户公开列表/详情
- 门户下载权限校验

## 9. 额外建议

### 9.1 不建议继续依赖 `ddl-auto: update` 做大改造

当前配置：

- [application.yml](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/resources/application.yml) 使用 `spring.jpa.hibernate.ddl-auto=update`

对于这次这种级别的表结构拆分，建议后续切到明确的 SQL 迁移脚本或 Flyway/Liquibase。

### 9.2 不建议让会员用户进入后台 RBAC

会员只需要业务属性控制：

- 是否启用
- 是否允许下载

不建议把会员也塞进后台角色、菜单、权限体系。

### 9.3 文件下载必须走受控接口

不要把磁盘路径或静态 URL 直接暴露给门户前端，否则无法做下载鉴权。

## 10. 最终判断

如果只从“当前后端需要新增和修改哪些东西”来回答，按你现在的新方案，核心就是 5 句话：

1. 后台管理员继续复用现有 `sys_user` 和 RBAC，不再走注册流程。
2. 后台公开注册接口要废弃或迁走，超级管理员改为直接创建管理员账号。
3. 会员必须新增独立用户表、独立登录注册链路、独立后台管理菜单。
4. 后端要新增“招标表 + 文件表 + 招标附件关联表 + 本地文件存储模块”。
5. 门户要新增“公开招标列表/详情 + 会员登录注册 + 下载权限校验”整套接口。

---

如果你确认这份分析方向没问题，下一步我可以继续帮你把这份文档细化成“开发任务拆分版”，直接列出第一期应该先改哪些 Java 类、哪些表、哪些接口。
