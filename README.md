# 招标系统后台基础版

当前版本已经从最初的“单角色 + 原生静态页”升级为更适合正式业务扩展的基础后台。

## 当前技术栈

- 后端: Spring Boot 2.7.18
- Java: Java 8
- 安全: Spring Security + JWT
- 数据访问: Spring Data JPA
- 数据库: MySQL 8
- 接口文档: springdoc-openapi + Swagger UI
- 前端: Vue 3 + Vite + Element Plus + Pinia + Vue Router

说明:
- 当前机器环境是 `Java 8`，所以后端选择 `Spring Boot 2.7.x`。
- 前端源码位于 [frontend](D:/work/workspace/zhaobiao/frontend)，生产构建输出到 [src/main/resources/static](D:/work/workspace/zhaobiao/src/main/resources/static)。

## 已实现的核心能力

### 账号与认证

- 初始化超级管理员
  - 用户名固定为: `admin`
  - 首次启动且数据库中不存在 `admin` 时，必须通过环境变量 `APP_BOOTSTRAP_ADMIN_PASSWORD` 提供初始密码
- JWT 鉴权
  - 请求头格式: `Authorization: Bearer <token>`
- 用户注册
  - 用户名
  - 手机号
  - 邮箱
  - 公司名称
  - 联系人
  - 统一社会信用代码
  - 真实姓名
  - 密码
  - 确认密码
- 注册后默认待审核
- 审核通过后才允许登录
- 审核驳回必须填写原因

### 权限模型

- 一个用户可绑定多个角色
- 一个角色可绑定多个权限
- 一个角色可绑定多个菜单
- 菜单支持三级模型
  - 目录
  - 菜单
  - 按钮
- 按钮菜单可绑定权限编码
- 接口鉴权已从“只认超级管理员角色”调整为“按权限编码控制”

### 用户与审核

- 用户列表
- 用户审核
- 审核记录查询
- 多角色分配
- 个人中心维护
- 企业信息维护

### 系统管理

- 角色管理
- 权限管理
- 菜单管理
- 操作日志

### 前端

- 登录/注册页面
- 工作台
- 个人中心
- 用户管理
- 角色管理
- 权限管理
- 菜单管理
- 审核记录
- 操作日志

## 当前内置角色

- `SUPER_ADMIN`
  - 超级管理员，拥有全部权限
- `SYSTEM_ADMIN`
  - 系统管理员，负责用户、角色、权限、菜单和日志管理
- `USER_AUDITOR`
  - 用户审核员，负责审核注册用户
- `NORMAL_USER`
  - 普通用户，仅可查看和维护个人信息

## 当前默认权限

- `dashboard:view`
- `profile:view`
- `profile:edit`
- `user:view`
- `user:audit`
- `user:role:update`
- `user:audit:record:view`
- `role:view`
- `role:edit`
- `permission:view`
- `permission:edit`
- `menu:view`
- `menu:edit`
- `operation:log:view`

## 当前默认菜单

- 工作台
- 个人中心
- 系统管理
  - 用户管理
  - 审核记录
  - 角色管理
  - 权限管理
  - 菜单管理
  - 操作日志
- 对应按钮权限节点
  - 用户审核
  - 分配角色
  - 角色维护
  - 权限维护
  - 菜单维护
  - 个人信息编辑

## 访问地址

- 后端接口: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- 构建后的前端: `http://localhost:8080/`
- 前端开发模式: `http://localhost:5173/`

## 后端运行方式

### 1. 创建数据库

```sql
CREATE DATABASE zhaobiao_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2. 配置环境变量

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_DB`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `APP_JWT_SECRET`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`

默认配置文件见 [application.yml](D:/work/workspace/zhaobiao/src/main/resources/application.yml)。

### 3. 启动后端

```bash
mvn spring-boot:run
```

## 前端运行方式

### 开发模式

```bash
cd frontend
npm install
npm run dev
```

开发模式下已通过 Vite 代理转发 `/api` 到 `http://localhost:8080`。

### 生产构建

```bash
cd frontend
npm run build
```

构建结果会输出到 [src/main/resources/static](D:/work/workspace/zhaobiao/src/main/resources/static)，由 Spring Boot 直接托管。

## 自动化验证

### 后端测试

```bash
mvn test
```

当前已覆盖的主流程:

- 注册必填企业信息
- 注册后未审核不可登录
- 管理员审核通过
- 管理员给用户分配多个角色
- 审核驳回必须填写原因
- 审核记录可查询

## 关键目录

- [src/main/java/com/zhaobiao/admin](D:/work/workspace/zhaobiao/src/main/java/com/zhaobiao/admin)
- [src/main/resources/application.yml](D:/work/workspace/zhaobiao/src/main/resources/application.yml)
- [frontend](D:/work/workspace/zhaobiao/frontend)
- [src/test/java/com/zhaobiao/admin/AuthFlowIntegrationTests.java](D:/work/workspace/zhaobiao/src/test/java/com/zhaobiao/admin/AuthFlowIntegrationTests.java)

## 这次重点优化掉的不合理点

### 1. 用户不再限制为单角色

原先问题:
- 后续拆分“系统管理员、审核员、业务管理员”会很别扭。

当前优化:
- 改成 `用户-角色多对多`
- 一个用户可以同时拥有多个角色

### 2. 后台能力不再只绑死到超级管理员

原先问题:
- 所有管理能力都压在超级管理员角色上，权限扩展性差。

当前优化:
- 改成基于权限编码的接口鉴权
- 只要角色拥有对应权限，就可以访问对应模块

### 3. 企业主体信息提前纳入用户模型

原先问题:
- 只有用户名、手机号、密码，后续审核和业务归档信息不够。

当前优化:
- 新增 `公司名称`
- 新增 `联系人`
- 新增 `邮箱`
- 新增 `统一社会信用代码`

### 4. 审核流程可追溯

原先问题:
- 只有审核状态，没有审核原因和审核历史。

当前优化:
- 驳回必须填写原因
- 新增审核记录表
- 记录审核结果、原因、审核人、时间

### 5. 菜单与按钮权限可配置

原先问题:
- 只有权限，没有菜单资源模型，前端动态菜单和按钮控制不完整。

当前优化:
- 新增菜单表
- 支持目录/菜单/按钮三种类型
- 角色可绑定菜单
- 按钮可绑定权限编码

### 6. 系统操作可审计

原先问题:
- 管理动作无日志，不利于排查问题。

当前优化:
- 新增操作日志
- 记录模块、动作、请求路径、操作人、结果、详情

## 当前还建议继续补的内容

- 文件上传组件
  - 招标系统通常很快会进入附件上传、资质材料上传、公告文件上传场景
- Refresh Token
  - 当前只有 access token，正式环境建议做双 token
- 数据权限
  - 当前是功能权限，还没有“只能看自己公司/自己项目/自己部门”的数据范围控制
- 字典管理
  - 招标方式、项目状态、供应商类型等通常会需要字典管理
- 操作日志检索
  - 当前是列表查询，还可以继续补时间筛选、操作人筛选、模块筛选

## 下一步最建议继续做的业务模块

1. 文件上传与附件管理
2. 招标项目管理
3. 招标公告管理
4. 供应商管理
5. 数据权限模型
