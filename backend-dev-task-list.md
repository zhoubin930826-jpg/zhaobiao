# 后端开发任务清单

更新时间：2026-04-15

说明：

- 本清单基于 [backend-change-analysis.md](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/backend-change-analysis.md) 进一步细化。
- 默认只做后端，不调整现有前端页面结构与交互。
- 任务按推荐实施顺序排列，前面的阶段尽量先完成再进入下一阶段。

## 1. 开发目标

本期后端改造目标：

- 保留现有后台管理员体系，管理员不再走注册流程。
- 超级管理员可新增后台管理员账号，并分配角色。
- 会员用户独立建表、独立注册登录。
- 新增后台“会员用户管理”和“招标管理”能力。
- 新增本地文件上传下载能力。
- 新增门户公开招标列表/详情接口，以及会员下载权限校验。

## 2. 里程碑总览

### M1 后台管理员语义收敛

- 去掉管理员公开注册语义
- 保留现有后台登录与 RBAC
- 为“超级管理员新增管理员”预留接口模型

### M2 会员用户域落地

- 新增会员表
- 新增会员注册/登录/当前用户接口
- 与后台管理员域彻底分开

### M3 后台管理能力补齐

- 管理员管理
- 会员用户管理
- 菜单/权限初始化补齐

### M4 招标业务域落地

- 招标主表
- 文件表
- 招标附件关联表
- 后台招标管理 CRUD

### M5 本地文件能力落地

- 本地上传
- 元数据入库
- 受控下载

### M6 门户接口落地

- 公开分页列表
- 公开详情
- 会员下载校验

### M7 测试、迁移、文档补齐

- 集成测试
- 数据迁移方案
- Swagger 与配置文档补齐

## 3. 分阶段任务清单

## 阶段一：后台管理员语义收敛

### T1.1 关闭后台管理员公开注册能力

- [ ] 明确 [AuthController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AuthController.java) 的 `/api/auth/register` 不再用于后台管理员注册。
- [ ] 选择处理方案：
  - 方案 A：直接废弃 `/api/auth/register`
  - 方案 B：迁移到门户会员注册后，后台域不再暴露该入口
- [ ] 调整 [AuthService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AuthService.java) 的职责，保留管理员登录与 `me` 能力。

验收标准：

- 后台管理员不再依赖公开注册流程。
- 现有后台登录 `POST /api/auth/login` 仍可正常使用。

### T1.2 收敛 `sys_user` 为管理员用户表

- [ ] 审视 [User.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/User.java) 字段，确认哪些字段继续服务后台管理员。
- [ ] 明确 [UserRepository.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/repository/UserRepository.java) 只作为管理员仓储使用。
- [ ] 调整注释、DTO 语义、接口命名，避免继续把 `sys_user` 当作“全站用户”。

验收标准：

- 后台用户域和门户会员域在命名和职责上清晰分离。

### T1.3 评估旧审核模型的保留范围

- [ ] 梳理 [UserStatus.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/UserStatus.java) 和 [UserAuditRecord.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/entity/UserAuditRecord.java) 的继续使用范围。
- [ ] 明确后台管理员是否还需要 `PENDING / APPROVED / REJECTED` 语义。
- [ ] 如果后台管理员不需要审核，清理 `AdminUserController / AdminUserService` 中旧审核主流程职责。

验收标准：

- 后台管理员的创建、启停、角色分配模型与“注册审核用户”模型不再混用。

## 阶段二：会员用户域落地

### T2.1 新增会员表与实体模型

- [ ] 新增 `portal_member_user` 表。
- [ ] 新增实体 `MemberUser.java`。
- [ ] 新增仓储 `MemberUserRepository.java`。
- [ ] 会员表至少包含：
  - `username`
  - `password`
  - `phone`
  - `email`
  - `realName`
  - `companyName`
  - `contactPerson`
  - `unifiedSocialCreditCode`
  - `canDownloadFile`
  - `status`
  - `lastLoginAt`

验收标准：

- 管理员和会员已分表。
- 会员表不接入后台 RBAC 关联表。

### T2.2 新增会员认证 DTO 和服务

- [ ] 新增 `MemberRegisterRequest.java`
- [ ] 新增 `MemberLoginRequest.java`
- [ ] 新增 `MemberUserDto.java`
- [ ] 新增 `PortalAuthService.java`
- [ ] 新增会员资料查询 DTO/Mapper

验收标准：

- 会员注册、登录、当前用户查询都有独立 DTO 和服务层。

### T2.3 新增门户认证控制器

- [ ] 新增 `PortalAuthController.java`
- [ ] 提供接口：
  - `POST /api/portal/auth/register`
  - `POST /api/portal/auth/login`
  - `GET /api/portal/auth/me`

验收标准：

- 门户会员注册登录链路独立可用。
- 不再复用后台 `/api/auth/register`。

## 阶段三：安全链路支持双用户域

### T3.1 JWT 增加用户类型

- [ ] 修改 [JwtTokenProvider.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/JwtTokenProvider.java)，在 token 中增加 `userType`。
- [ ] 明确 `userType` 枚举值：
  - `ADMIN`
  - `MEMBER`
- [ ] 确保 token 解析后能知道去哪个表查用户。

验收标准：

- 管理员 token 和会员 token 可区分。

### T3.2 鉴权过滤器支持双域

- [ ] 修改 [JwtAuthenticationFilter.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/JwtAuthenticationFilter.java)。
- [ ] 保留现有管理员加载逻辑。
- [ ] 新增会员加载逻辑。
- [ ] 评估是否需要新增 `MemberLoginUser` 或统一 principal 抽象。

验收标准：

- 管理员接口和会员接口都可通过 token 正确鉴权。

### T3.3 调整安全配置

- [ ] 修改 [SecurityConfig.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/SecurityConfig.java)。
- [ ] 放开门户公开接口：
  - `/api/portal/tenders/**` 中的公开列表与详情
  - `/api/portal/auth/**`
- [ ] 保持后台接口需要认证。
- [ ] 下载接口按会员认证放行，不对游客开放。

验收标准：

- 游客可以访问公开招标列表和详情。
- 下载接口需要会员登录。

## 阶段四：后台管理员管理

### T4.1 改造现有管理员管理服务

- [ ] 复用 [AdminUserController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AdminUserController.java)。
- [ ] 复用 [AdminUserService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AdminUserService.java)。
- [ ] 从旧的“注册审核用户”逻辑改造成“管理员管理”逻辑。

### T4.2 新增管理员管理接口

- [ ] `GET /api/admin/admin-users`
- [ ] `POST /api/admin/admin-users`
- [ ] `PUT /api/admin/admin-users/{id}`
- [ ] `PUT /api/admin/admin-users/{id}/status`
- [ ] `PUT /api/admin/admin-users/{id}/password/reset`
- [ ] `PUT /api/admin/admin-users/{id}/roles`

### T4.3 管理员创建请求体与校验

- [ ] 新增管理员创建请求 DTO。
- [ ] 校验：
  - 用户名唯一
  - 手机号唯一
  - 邮箱唯一
  - 至少分配一个角色

验收标准：

- 超级管理员可新增管理员账号。
- 管理员可被启用/禁用、重置密码、分配角色。

## 阶段五：后台会员用户管理

### T5.1 新增会员管理控制器与服务

- [ ] 新增 `MemberAdminController.java`
- [ ] 新增 `MemberAdminService.java`

### T5.2 新增会员管理接口

- [ ] `GET /api/admin/members`
- [ ] `GET /api/admin/members/{id}`
- [ ] `PUT /api/admin/members/{id}`
- [ ] `PUT /api/admin/members/{id}/download-permission`
- [ ] `PUT /api/admin/members/{id}/status`

### T5.3 会员状态模型落地

- [ ] 明确会员状态枚举：
  - `ENABLED`
  - `DISABLED`
  - 如果要审核，再扩展 `PENDING`
- [ ] 明确 `canDownloadFile` 的默认值。

验收标准：

- 后台可查看会员。
- 后台可控制会员启用/禁用。
- 后台可控制会员是否允许下载文件。

## 阶段六：菜单、权限、初始化数据补齐

### T6.1 新增后台权限点

- [ ] 在 [DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java) 中新增权限：
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

### T6.2 新增后台菜单

- [ ] 新增“管理员管理”
- [ ] 新增“会员用户管理”
- [ ] 新增“招标管理”

### T6.3 调整默认角色权限

- [ ] 超级管理员拥有全部权限。
- [ ] 系统管理员是否拥有管理员管理权限，需要明确。
- [ ] 普通后台管理员是否可管理会员，需要明确。

验收标准：

- 启动后新权限和新菜单可自动初始化。

## 阶段七：招标业务域

### T7.1 新增招标主表

- [ ] 新增 `biz_tender` 表。
- [ ] 新增实体 `Tender.java`。
- [ ] 新增仓储 `TenderRepository.java`。

字段至少包含：

- `title`
- `region`
- `publishAt`
- `content`
- `contactPerson`
- `budget`
- `contactPhone`
- `tenderUnit`
- `deadline`
- `projectCode`
- `signupDeadline`
- `status`
- `createdBy`
- `updatedBy`

### T7.2 新增文件元数据表

- [ ] 新增 `biz_file_storage` 表。
- [ ] 新增实体 `TenderFileStorage.java`
- [ ] 新增仓储 `TenderFileStorageRepository.java`

### T7.3 新增招标附件关联表

- [ ] 新增 `biz_tender_attachment` 表。
- [ ] 新增实体 `TenderAttachment.java`
- [ ] 新增仓储 `TenderAttachmentRepository.java`

验收标准：

- 一个招标可关联多个文件。
- 文件和招标是通过关联表维护关系。

## 阶段八：后台招标管理接口

### T8.1 新增 DTO 和请求体

- [ ] `TenderDto.java`
- [ ] `TenderListItemDto.java`
- [ ] `TenderDetailDto.java`
- [ ] `TenderUpsertRequest.java`

### T8.2 新增后台招标管理服务与控制器

- [ ] `TenderService.java`
- [ ] `TenderAdminController.java`

### T8.3 新增后台接口

- [ ] `GET /api/admin/tenders`
- [ ] `GET /api/admin/tenders/{id}`
- [ ] `POST /api/admin/tenders`
- [ ] `PUT /api/admin/tenders/{id}`
- [ ] `DELETE /api/admin/tenders/{id}`
- [ ] `POST /api/admin/tenders/{id}/attachments`
- [ ] `DELETE /api/admin/tenders/{id}/attachments/{attachmentId}`

验收标准：

- 后台可维护招标信息。
- 后台可为招标绑定多个附件。

## 阶段九：本地文件上传下载

### T9.1 新增文件存储配置

- [ ] 在 [application.yml](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/resources/application.yml) 中新增：
  - `app.file.storage-path`
  - `app.file.temp-path`
  - `spring.servlet.multipart.max-file-size`
  - `spring.servlet.multipart.max-request-size`

### T9.2 新增本地文件服务

- [ ] 新增 `LocalFileStorageService.java`
- [ ] 实现：
  - 文件名去重
  - 目录创建
  - 元数据保存
  - 删除与回收策略

### T9.3 新增上传接口

- [ ] 新增 `POST /api/admin/files/upload`
- [ ] 返回 `FileUploadResponse.java`

### T9.4 新增受控下载接口

- [ ] 新增下载控制器
- [ ] 门户下载接口：
  - `GET /api/portal/tenders/{tenderId}/attachments/{attachmentId}/download`
- [ ] 校验：
  - 当前用户是会员
  - 会员已启用
  - `canDownloadFile = true`
  - 该附件确实属于该招标

验收标准：

- 后台能上传文件。
- 游客不能下载。
- 已登录且有下载权限的会员可以下载。

## 阶段十：门户招标接口

### T10.1 新增门户招标控制器

- [ ] 新增 `PortalTenderController.java`

### T10.2 新增公开分页列表接口

- [ ] `GET /api/portal/tenders`
- [ ] 支持参数：
  - `pageNum`
  - `pageSize`
  - 后续可扩展 `keyword / region`
- [ ] 默认按 `publishAt desc, id desc`

### T10.3 新增公开详情接口

- [ ] `GET /api/portal/tenders/{id}`
- [ ] 返回字段：
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
  - `canDownload`

验收标准：

- 游客可查看招标列表和详情。
- 游客看得到附件文件名，但拿不到可直接下载的裸链接。

## 阶段十一：测试与迁移

### T11.1 重写集成测试

- [ ] 重写 [AuthFlowIntegrationTests.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/test/java/com/zhaobiao/admin/AuthFlowIntegrationTests.java)
- [ ] 新增测试覆盖：
  - 超级管理员新增管理员
  - 管理员登录
  - 会员注册
  - 会员登录
  - 会员状态控制
  - 会员下载权限控制
  - 招标列表分页倒序
  - 招标详情
  - 附件下载鉴权

### T11.2 迁移方案

- [ ] 明确旧 `/api/auth/register` 的迁移策略。
- [ ] 明确 `sys_user` 中原有非管理员数据如何处理。
- [ ] 评估是否引入 Flyway/Liquibase。

### T11.3 Swagger 与文档

- [ ] 为新增接口补 Swagger 注解。
- [ ] 更新 README 中的认证与业务接口说明。

验收标准：

- 核心链路有自动化测试。
- 新旧接口迁移关系明确。

## 4. 推荐执行顺序

推荐按下面顺序推进：

1. 先做阶段一、二、三，先把“后台管理员”和“门户会员”彻底拆开。
2. 再做阶段四、五、六，把后台管理能力补齐。
3. 再做阶段七、八、九，把招标和文件能力做出来。
4. 最后做阶段十、十一，把门户接口和测试补全。

## 5. 第一期开工建议

如果要从今天开始进入编码，建议第一期只做这些：

- [ ] 关闭后台管理员注册语义
- [ ] 会员独立建表
- [ ] JWT 增加 `userType`
- [ ] 门户会员注册/登录/`me`
- [ ] 后台管理员新增管理员接口
- [ ] 后台会员管理接口

原因：

- 这是所有后续招标、下载权限、门户接口的基础。
- 如果管理员域和会员域不先拆开，后面业务会越做越乱。

## 6. 第二期开工建议

- [ ] 招标三张核心表
- [ ] 后台招标管理接口
- [ ] 本地文件上传
- [ ] 门户列表/详情
- [ ] 下载鉴权

## 7. 完成定义

以下条件全部满足，才算本期后端改造完成：

- [ ] 后台管理员不再注册，只能由超级管理员创建
- [ ] 会员独立注册登录，且不进入后台 RBAC
- [ ] 后台可管理会员启用状态与下载权限
- [ ] 后台可维护招标内容并上传多个招标文件
- [ ] 门户游客可看列表和详情
- [ ] 只有符合条件的会员可以下载招标文件
- [ ] 关键链路有集成测试覆盖
