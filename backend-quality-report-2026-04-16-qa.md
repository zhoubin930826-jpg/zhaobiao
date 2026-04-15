# 后端质检报告（仅后端）

- 项目路径: `/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao`
- 质检时间: `2026-04-16 02:19` `Asia/Shanghai`
- 质检范围: 仅后端代码、权限模型、接口安全、MySQL 运行态验证
- 数据库: Docker 容器 `zhaobiao-mysql`，MySQL `8.0.45`，库 `zhaobiao_admin`
- 运行实例:
  - 自动化测试: `mvn test`
  - 当前源码实例: `http://127.0.0.1:18080`

## 一、总体结论

后端主业务链路和你描述的业务模型大体一致:

- 门户会员不能在线注册，只能由后台创建账号
- 会员登录后按业务类型看招标
- 下载权限由管理员单独控制
- 超级管理员专管管理员账号
- 会员、类型、招标、附件、JWT 鉴权这些主链路已经打通

但是当前后端存在 **1 个已实证复现的高危越权漏洞**，以及 **多项默认配置级安全风险**。  
其中最严重的问题是:

- 普通系统管理员可以通过遗留的“旧用户管理”接口，把自己直接提权成 `SUPER_ADMIN`
- 提权后即可访问管理员账号管理接口，实际绕过“只有超级管理员才能操作管理员”的业务约束

这意味着当前版本 **不建议直接作为公网正式版本上线**。

## 二、执行记录

### 1. 数据库连接验证

已连接本地 Docker MySQL，并确认目标库与核心表存在:

- 库: `zhaobiao_admin`
- 核心表:
  - `portal_member_user`
  - `portal_member_business_type_rel`
  - `biz_business_type`
  - `biz_tender`
  - `biz_tender_attachment`
  - `biz_file_storage`
  - `sys_user`
  - `sys_role`
  - `sys_permission`
  - `sys_menu`
  - `sys_operation_log`

当时数据概况:

- `sys_user`: `2`
- `portal_member_user`: `1`
- `biz_business_type`: `3`
- `biz_tender`: `0`
- `sys_role`: `4`
- `sys_permission`: `36`
- `sys_menu`: `37`

### 2. 自动化测试结果

执行命令:

```bash
mvn test
```

结果:

- `Tests run: 5`
- `Failures: 0`
- `Errors: 0`
- `BUILD SUCCESS`

已覆盖的现有自动化能力主要包括:

- 超级管理员创建管理员
- 普通管理员不能访问管理员账号管理
- 会员由后台创建、修改、禁用、重置密码
- 会员按类型看招标
- 下载权限控制
- 门户未登录/越权访问拦截

### 3. 当前源码实例冒烟验证

我额外启动了当前源码实例到 `18080`，并直接连接 `zhaobiao_admin` 验证:

- 后台管理员登录成功
- 角色、菜单、权限初始化成功
- Swagger 匿名可访问
- 关键越权问题已实证复现

## 三、核心问题清单

### P0 严重: 系统管理员可通过遗留接口把自己提权为超级管理员

#### 现象

当前系统同时保留了两套管理后台用户的入口:

- 新的管理员账号管理: `/api/admin/admin-users`
- 遗留的旧用户管理: `/api/admin/users`

`SYSTEM_ADMIN` 虽然不能访问新的管理员账号管理接口，但仍然拥有遗留接口的 `user:view`、`user:role:update` 权限，并且遗留接口操作的也是 `sys_user` 表。  
结果就是普通系统管理员可以直接调用遗留接口，把自己的角色改成 `SUPER_ADMIN`。

#### 实证复现

在 `2026-04-16 02:18 +08:00`，我使用当前源码实例 `http://127.0.0.1:18080` 做了最小化验证:

1. 超级管理员创建临时系统管理员 `qaadmin1776277102`
2. 用该账号登录，确认初始角色为 `SYSTEM_ADMIN`
3. 该账号调用 `PUT /api/admin/users/{userId}/roles`
4. 把自己的角色更新为 `SUPER_ADMIN`
5. 再访问 `GET /api/admin/admin-users`，返回 `code = 0`

复现结果说明:

- 越权提权成功
- “只有超级管理员可以操作管理员”的约束已被绕过

临时测试账号已清理。

#### 代码定位

- 遗留用户角色修改入口开放给 `user:role:update`
  - [src/main/java/com/zhaobiao/admin/controller/AdminUserController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/AdminUserController.java:53)
- 遗留用户角色修改逻辑没有拦截“管理员账号”与“SUPER_ADMIN 角色分配”
  - [src/main/java/com/zhaobiao/admin/service/AdminUserService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/AdminUserService.java:84)
- `SYSTEM_ADMIN` 被初始化授予了 `user:view`、`user:role:update`，并带有“旧用户管理”菜单
  - [src/main/java/com/zhaobiao/admin/config/DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java:110)
  - [src/main/java/com/zhaobiao/admin/config/DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java:181)

#### 风险

- 普通管理员可自提权为超级管理员
- 可进一步操作其他管理员账号
- 可读取管理员列表、改管理员状态、改管理员角色、重置管理员密码

#### 建议

- 立即停用或删除 `/api/admin/users` 这套遗留后台用户管理接口
- 至少先在 `AdminUserService.updateUserRoles` 中禁止对管理员账号和 `SUPER_ADMIN` 角色的操作
- 为该问题补一条强制回归测试: `SYSTEM_ADMIN` 调用遗留接口给自己或别人赋 `SUPER_ADMIN` 必须失败

### P1 高: 默认超级管理员账号密码是硬编码且当前可直接登录

#### 现象

系统启动时，如果库里不存在 `admin`，会自动创建固定账号:

- 用户名: `admin`
- 密码: `adminqwert`

我本次质检过程中，已经用这组账号在本地 MySQL 实例上成功登录。

#### 代码定位

- [src/main/java/com/zhaobiao/admin/config/DataInitializer.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/DataInitializer.java:208)

#### 风险

- 新环境初始化后极易被默认口令接管
- 数据库被清空/重建后风险会再次出现
- 如果测试环境口令沿用到正式环境，风险更高

#### 建议

- 改为“首次启动强制从环境变量读取管理员初始密码”
- 若未配置，则启动失败，不要使用硬编码默认口令
- 首次创建后强制要求改密

### P1 高: 默认 JWT 密钥和数据库账号都带不安全回退值

#### 现象

默认配置中包含:

- JWT 默认密钥回退值
- MySQL 默认 `root/root`

这类回退值在开发环境方便，但当前写在默认配置里，没有做生产拦截。

#### 代码定位

- [src/main/resources/application.yml](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/resources/application.yml:6)
- [src/main/resources/application.yml](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/resources/application.yml:32)

#### 风险

- 如果生产环境漏配 `APP_JWT_SECRET`，令牌可被伪造
- 如果生产环境仍使用 `root` 账号，数据库最小权限原则失效

#### 建议

- 生产环境启动前对关键配置做 fail-fast 校验
- 禁止默认 JWT 密钥回退
- 数据库改用独立业务账号，不使用 `root`

### P2 中: Swagger 与 H2 Console 在默认安全配置中匿名开放

#### 现象

当前默认安全配置允许匿名访问:

- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/h2-console/**`

本次质检时，`http://127.0.0.1:18080/swagger-ui.html` 可直接匿名访问。

#### 代码定位

- [src/main/java/com/zhaobiao/admin/config/SecurityConfig.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/config/SecurityConfig.java:52)

#### 风险

- 暴露完整接口面，方便攻击者枚举接口
- H2 Console 路径在生产配置中没有存在必要
- 对公网系统的攻击面明显增大

#### 建议

- 仅在开发环境开放 Swagger/H2 Console
- 默认 profile 下关闭这些公开入口
- 如确需保留 Swagger，至少加管理员认证或内网限制

### P2 中: `sys_user` 中的任何用户都会被授予 `ROLE_ADMIN`

#### 现象

`LoginUser.from(User user)` 中无条件追加了 `ROLE_ADMIN`:

```java
Stream.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
```

这意味着只要账号来自 `sys_user`，哪怕角色实际上只是 `NORMAL_USER`，也会通过所有 `hasRole('ADMIN')` 判断。

#### 代码定位

- [src/main/java/com/zhaobiao/admin/security/LoginUser.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/security/LoginUser.java:51)

#### 风险

- 当前库里虽然只有管理员账号，但这是明显的权限模型污染
- 一旦未来又往 `sys_user` 放入普通用户或遗留账号，`hasRole('ADMIN')` 会形同失效

#### 建议

- 只有真正的后台管理员角色才授予 `ROLE_ADMIN`
- 如果保留 `NORMAL_USER`，应与后台账号彻底拆表或拆认证上下文

## 四、通过项

以下链路在代码和测试上基本成立:

- 门户在线注册已关闭
  - [src/main/java/com/zhaobiao/admin/controller/PortalAuthController.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/controller/PortalAuthController.java:37)
- 会员必须登录后才能访问门户招标
- 会员可见招标受业务类型约束
  - [src/main/java/com/zhaobiao/admin/service/PortalTenderService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/PortalTenderService.java:54)
- 会员附件下载受 `canDownloadFile` 控制
  - [src/main/java/com/zhaobiao/admin/service/PortalTenderService.java](/Users/zhoubin/work/ideaWorkSpace/zhou/zhaobiao/src/main/java/com/zhaobiao/admin/service/PortalTenderService.java:109)
- 管理员创建会员、控制状态、重置密码、控制下载权限的后端链路完整
- 超级管理员专属的 `/api/admin/admin-users` 接口本身做了 `hasRole('SUPER_ADMIN')` 限制
  - 问题出在旁路接口绕过，而不是这个新接口本身

## 五、建议修复顺序

### 第一优先级

1. 下线或封禁 `/api/admin/users` 遗留接口
2. 彻底阻断 `SYSTEM_ADMIN -> SUPER_ADMIN` 提权路径
3. 移除默认超级管理员口令
4. 移除默认 JWT 密钥回退

### 第二优先级

1. 关闭默认 Swagger/H2 Console 匿名访问
2. 修正 `ROLE_ADMIN` 的授予逻辑
3. 为权限边界补回归测试

## 六、最终判断

当前后端不是“功能不可用”，而是“**功能可用，但权限边界存在致命旁路**”。  
如果只看主流程，会员、招标、类型、下载权限这些业务已经能跑；但如果要作为正式招标系统后端使用，**必须先修复管理员提权问题和默认安全配置问题**。
