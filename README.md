# 招标系统

当前仓库包含三个主要部分：

- 后端服务：Spring Boot 管理接口、门户接口、认证鉴权、文件存储和业务数据。
- 管理后台：`frontend/`，给后台管理员使用。
- 公开门户：`front/`，给会员登录后查看招标公告和下载附件使用。

## 技术栈

- 后端：Java 8、Spring Boot 2.7.18、Maven、Spring Security + JWT、Spring Data JPA、MySQL 8。
- 接口文档：springdoc-openapi，Swagger UI 地址为 `http://localhost:8080/swagger-ui.html`。
- 本地测试数据库：使用 Docker MySQL 8，默认测试库为 `zhaobiao_admin_test`。
- 文件存储：默认本地目录 `.uploads/`，设置 `APP_FILE_TYPE=oss` 后使用 Aliyun OSS。
- 管理后台：`frontend/`，Vue 2.6 + Vue CLI 3 + View Design/iView 风格模板，生产路径 `/ztbgl/`。
- 公开门户：`front/`，Vue 3.2 + Vite 2，生产路径 `/ztbfb/`。

> 说明：`src/main/resources/static` 里仍有一份静态资源，Spring Boot 可以托管它；但当前部署配置以 Nginx 分别托管 `frontend/dist` 和 `front/dist` 为准。

## 当前核心能力

### 后台管理

- 管理员登录：`/api/auth/login`。
- 管理员公开注册已停用：`/api/auth/register` 返回业务码 `403`。
- 超级管理员管理后台管理员账号：`/api/admin/admin-users`，仅 `SUPER_ADMIN` 可用。
- 会员管理：后台创建会员、修改资料、启停账号、重置密码、控制附件下载权限、分配可访问业务类型。
- 业务类型管理：工程、货物、服务是默认种子数据，后续可在后台维护。
- 招标管理：分页查询、新增、编辑、删除招标，绑定和删除附件；新增招标默认 `DRAFT`，发布和取消发布必须走 `PUT /api/admin/tenders/{tenderId}/status`，并要求 `TENDER_PUBLISH_BUTTON`。
- 资讯管理：`/api/admin/news`，支持分页、详情、新增、编辑、删除、发布、取消发布和封面上传；新增资讯默认 `DRAFT`，发布和取消发布要求 `NEWS_PUBLISH_BUTTON`。
- 文件上传、下载和查看：`/api/admin/files/upload`、`/api/admin/files/{fileId}/download`、`/api/admin/files/{fileId}/view`，支持本地和 OSS 存储，并按文件内容哈希去重。
- 角色、权限、菜单和操作日志管理。
- 个人中心维护。

### 门户端

- 会员在线注册：`/api/portal/auth/register` 使用 `multipart/form-data` 提交基础资料、营业执照、近三年业绩证明和注册验证码；成功后账号默认为“禁用”，需管理员补会员类型和有效期后启用。
- 会员登录：`/api/portal/auth/login`，需要先获取并提交登录验证码。
- 门户验证码：`/api/portal/auth/captcha?scene=register|login&captchaId=<uuid>`，5 分钟过期、一次性使用，点击验证码图片应刷新。
- 会员信息：`/api/portal/auth/me`。
- 招标公告：游客可调用 `/api/portal/tenders/latest` 查看最新公开招标，也可打开 `/api/portal/tenders/{tenderId}` 查看已发布且发布时间已到的详情；会员列表 `/api/portal/tenders` 需要登录，并按会员业务类型过滤。
- 招标分类过滤：会员列表支持 `businessTypeName` 查询参数，传业务类型名称。
- 附件下载：`/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download` 需要会员登录、业务类型匹配，并且会员账号 `canDownloadFile=true`。
- 资讯公告：`/api/portal/news`、`/api/portal/news/latest`、`/api/portal/news/{newsId}` 对游客开放，只返回已发布且发布时间不晚于当前时间的资讯。

## 权限和账号规则

- 所有接口统一返回 `ApiResponse`。业务错误通常是 HTTP 200 + 非 0 `code`，前端必须判断 `code`。
- JWT 请求头格式：`Authorization: Bearer <token>`。
- JWT 内含 `userType`，后台管理员和门户会员走不同的用户主体。
- 当前后台角色：
  - `SUPER_ADMIN`：超级管理员，拥有全部权限。
  - `SYSTEM_ADMIN`：系统管理员，负责会员、招标、角色、权限、菜单和日志等管理。
  - `USER_AUDITOR`：历史审核角色，当前只保留基础访问能力。
  - `NORMAL_USER`：历史普通用户角色，当前只保留基础访问能力。
- 旧接口 `/api/admin/users` 已停用，返回业务码 `410`，不要再用于提权或用户管理。
- 初始 `admin` 用户不能被禁用，也必须保留 `SUPER_ADMIN`。
- 普通管理员不能管理其他管理员账号；管理员账号也不能分配 `NORMAL_USER`。
- 发布权限独立于新增/编辑权限：只有 `TENDER_PUBLISH_BUTTON` 才能发布/取消发布招标，只有 `NEWS_PUBLISH_BUTTON` 才能发布/取消发布资讯。
- 已发布招标不能编辑、删除、增删附件；必须先由有发布权限的管理员取消发布。
- 已发布资讯不能编辑、删除；必须先由有发布权限的管理员取消发布。

## 后端运行

### 1. 创建 MySQL 数据库

```sql
CREATE DATABASE zhaobiao_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2. 配置环境变量

本地最小配置：

```bash
APP_JWT_SECRET=ChangeThisToAVeryLongRandomSecret123456 \
mvn spring-boot:run
```

常用环境变量：

- `MYSQL_HOST`，默认 `localhost`
- `MYSQL_PORT`，默认 `3306`
- `MYSQL_DB`，默认 `zhaobiao_admin`
- `MYSQL_USER`，默认 `root`
- `MYSQL_PASSWORD`，默认 `root`
- `APP_JWT_SECRET`，必须配置，至少 32 个字符
- `SPRING_PROFILES_ACTIVE=prod`
- `MYSQL_TEST_DB`，测试默认 `zhaobiao_admin_test`
- `MYSQL_CONTEXT_TEST_DB`，启动上下文测试默认 `zhaobiao_admin_context_test`
- `APP_FILE_TYPE=local|oss`
- 本地存储：`APP_FILE_STORAGE_PATH`、`APP_FILE_TEMP_PATH`
- OSS 存储：`APP_FILE_OSS_BUCKET`、`APP_FILE_OSS_ENDPOINT`、`APP_FILE_OSS_CREDENTIAL_MODE`、`APP_FILE_OSS_ROLE_NAME`、`APP_FILE_OSS_ACCESS_KEY_ID`、`APP_FILE_OSS_ACCESS_KEY_SECRET`、`APP_FILE_OSS_KEY_PREFIX`
- OSS 凭证模式：`APP_FILE_OSS_CREDENTIAL_MODE=access-key` 使用长期 AK/SK；`APP_FILE_OSS_CREDENTIAL_MODE=ecs-ram-role` 使用 ECS 实例 RAM 角色，此时配置 `APP_FILE_OSS_ROLE_NAME`，不需要配置 AK/SK。

生产环境额外要求：

- 设置 `SPRING_PROFILES_ACTIVE=prod`。
- 必须显式配置 `MYSQL_USER` 和 `MYSQL_PASSWORD`。
- `prod` 环境禁止使用 `root` 数据库账号，也禁止使用默认密码 `root`。

### 3. 初始化数据

当前源码里的 `DataInitializer` 不是主应用自动注册的 Spring Bean，测试用例通过 `@Import(DataInitializer.class)` 单独加载它。因此空 MySQL 不会在启动时自动补齐角色、权限、菜单、业务类型和初始管理员。

首次建库时，先让 Hibernate 建表，或先准备好等价 schema，然后执行：

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p zhaobiao_admin < sql/mysql8/data-initializer.sql
```

这个脚本包含默认 `admin` 账号的初始化哈希。生产环境执行后应立即重置初始管理员密码。

2026-05-23 相关生产/测试库脚本：

- `sql/mysql8/2026-05-23-complete-release.sql`：完整发布脚本，包含会员文件字段、软删除索引、缩略图字段、资讯表、菜单/按钮权限等。
- `sql/mysql8/2026-05-23-news-module.sql`：资讯模块和招标发布权限的专项脚本，适用于只补这部分能力的环境。
- `sql/mysql8/2026-05-23-news-publish-permission.sql`：在已经执行过完整脚本后，用来补齐或修正 `NEWS_PUBLISH_BUTTON` 的增量脚本。

### 4. 打包

```bash
mvn clean package -DskipTests
```

生成的后端包为：

```text
target/zhaobiao-admin-0.0.7-SNAPSHOT.jar
```

## 前端运行

### 管理后台 `frontend/`

```bash
cd frontend
npm install
VUE_APP_PROXY_TARGET=http://127.0.0.1:8080 PORT=8081 npm run dev
npm run build:prod
```

说明：

- `frontend` 是后台管理端，不是门户。
- Vue CLI 默认端口可能和后端 `8080` 冲突，建议本地显式设置 `PORT=8081`。
- 不设置 `VUE_APP_PROXY_TARGET` 时，开发代理默认指向 `https://xiazhiyong.vip`。
- 生产 API 基地址来自 `frontend/src/setting.env.js`，当前为 `https://xiazhiyong.vip/api`。
- 生产构建输出到 `frontend/dist`，Nginx 访问路径为 `/ztbgl/`。

### 公开门户 `front/`

```bash
cd front
npm install
VITE_PROXY_TARGET=http://127.0.0.1:8080 npm run dev
npm run build
```

说明：

- `front` 是会员门户，不是后台管理端。
- 开发端口为 `5174`。
- 不设置 `VITE_PROXY_TARGET` 时，开发代理默认指向 `https://xiazhiyong.vip`。
- API 基地址默认 `/api`，可通过 `VITE_API_BASE_URL` 覆盖。
- 生产构建输出到 `front/dist`，Nginx 访问路径为 `/ztbfb/`。

## 访问地址

本地后端：

- API 根地址：`http://localhost:8080`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

本地前端：

- 管理后台开发地址通常为 `http://localhost:8081/`。
- 门户开发地址为 `http://localhost:5174/`。

生产路径：

- 管理后台：`/ztbgl/`
- 公开门户：`/ztbfb/`
- Nginx 根路径 `/` 当前重定向到 `/ztbfb/`。

## 自动化验证

后端：

```bash
mvn test
```

管理后台：

```bash
cd frontend
npm run build:prod
```

公开门户：

```bash
cd front
npm run build
```

当前测试覆盖的重点包括：

- 后台公开注册停用。
- 超级管理员创建和管理后台管理员。
- 普通管理员不能访问管理员账号管理接口。
- 旧 `/api/admin/users` 停用并不能用于提权。
- 后台创建、审核和管理会员账号。
- 门户会员自助注册、验证码校验、未启用账号拦截。
- 会员业务类型隔离、附件下载权限控制。
- 重复上传按内容哈希复用文件记录。
- 招标新增默认未发布、发布权限独立控制、已发布招标禁止编辑/删除/维护附件。
- 资讯新增默认草稿、发布权限独立控制、门户只展示已发布且发布时间已到的资讯。
- 后台文件下载和查看接口权限边界。
- `prod` 数据库账号安全校验。

## 部署要点

- 管理后台脚本默认部署到 `114.55.166.12:/usr/share/nginx/ztbgl`。
- 门户脚本默认部署到 `114.55.166.12:/usr/share/nginx/ztbfb`。
- 捕获的 Nginx 配置见 `nginx.conf.from-server`。
- 已知后端部署目录为 `/opt/zhaobiao/app`。
- 服务器运行环境变量文件通常是 `/opt/zhaobiao/app/app.env`。
- `DEPLOY_LINUX.md` 中有通用 `zhaobiao.service` 示例；真实服务器此前使用过 `zhaobiao-admin.service`，操作前要先查当前 systemd 单元。
- 线上后端应保持单个 systemd 托管进程，避免手动 `nohup java -jar` 留下重复实例。

常用服务器检查命令：

```bash
systemctl list-units --type=service --all | grep -i zhaobiao
systemctl status zhaobiao-admin
ss -ltnp | grep ':8080'
curl -I http://127.0.0.1:8080/swagger-ui.html
curl -I http://127.0.0.1:8080/v3/api-docs
```

## 关键目录

- `src/main/java/com/zhaobiao/admin`：后端源码。
- `src/test/java/com/zhaobiao/admin`：后端集成测试。
- `src/main/resources/application.yml`：默认后端配置。
- `frontend/`：管理后台。
- `front/`：公开门户。
- `sql/mysql8/data-initializer.sql`：MySQL 初始化数据脚本。
- `sql/mysql8/2026-05-23-*.sql`：2026-05-23 发布相关 MySQL 迁移脚本。
- `docs/production-db-schema-change-list.md`：生产库结构/数据变更记录。
- `docs/frontend-change-guide-2026-05-23.md`：2026-05-23 后端改动对应的前端开发交接指南。
- `docs/frontend-change-guide-2026-05-23.html`：同一份前端交接指南的 HTML 版本。
- `AGENTS.md`：给编码代理使用的仓库操作指南。

## 注意事项

- 不要提交或粘贴真实密码、JWT secret、数据库凭据、OSS key。
- `server-config.md` 被 Git 忽略，可能包含真实服务器信息，只在明确做生产/服务器操作时读取。
- `target/`、`.uploads/`、`.test-uploads/`、`frontend/dist/`、`front/dist/`、`node_modules/` 都是生成或本地目录，不应提交。
- 改数据库结构或生产初始化数据时，同步更新 `sql/mysql8/` 下的迁移脚本、`docs/production-db-schema-change-list.md` 和相关交接文档。
- 改权限、菜单、角色或管理能力时，同时改后端鉴权、种子数据、管理后台菜单/按钮和测试。
