# REST接口清单

## 统一约定

- 后端统一前缀由部署反向代理决定，本地 API 根为 `http://localhost:8080`。
- JSON 响应统一为 `ApiResponse`。
- 成功通常为 `code=0`。
- 业务失败通常仍是 HTTP 200，但 `code` 非 0；前端必须判断业务 `code`。
- 认证头为 `Authorization: Bearer <token>`。
- 管理员和会员 token 通过 JWT `userType` 区分。

## 认证接口

| 方法 | 路径 | 认证 | 说明 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | 公开 | 后台管理员公开注册已停用，返回 403 业务错误 |
| `POST` | `/api/auth/login` | 公开 | 管理员登录 |
| `GET` | `/api/auth/me` | ADMIN | 当前管理员信息、角色、菜单、权限 code |
| `GET` | `/api/profile` | `PROFILE` | 后台个人中心 |
| `PUT` | `/api/profile` | `PROFILE_EDIT_BUTTON` | 修改后台个人信息 |

## 管理员账号

类级权限：`hasRole('SUPER_ADMIN')`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/admin/admin-users` | 查询管理员账号列表 |
| `POST` | `/api/admin/admin-users` | 新增管理员账号 |
| `PUT` | `/api/admin/admin-users/{userId}` | 修改管理员账号基本信息 |
| `PUT` | `/api/admin/admin-users/{userId}/roles` | 修改管理员账号角色 |
| `PUT` | `/api/admin/admin-users/{userId}/status` | 启用/禁用管理员账号 |
| `PUT` | `/api/admin/admin-users/{userId}/password` | 重置管理员密码 |

关键规则：

- 管理员不能分配 `NORMAL_USER`。
- 初始 `admin` 不能禁用，不能移除 `SUPER_ADMIN`。

## 旧用户与权限接口

| 方法 | 路径 | 当前状态 |
| --- | --- | --- |
| `GET` | `/api/admin/users` | 停用，返回业务码 410 |
| `PUT` | `/api/admin/users/{userId}/audit` | 停用，返回业务码 410 |
| `PUT` | `/api/admin/users/{userId}/roles` | 停用，返回业务码 410 |
| `GET` | `/api/admin/users/{userId}/audit-records` | 停用，返回业务码 410 |
| `GET/POST/PUT/DELETE` | `/api/admin/permissions/**` | 停用，返回业务码 410 |

## 业务类型

类级权限：ADMIN。

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/admin/business-types` | `SYSTEM_BUSINESS_TYPE` | 查询全部业务类型 |
| `GET` | `/api/admin/business-types/options` | `SYSTEM_BUSINESS_TYPE` | 查询启用中的选项 |
| `POST` | `/api/admin/business-types` | `BUSINESS_TYPE_CREATE_BUTTON` | 新增业务类型 |
| `PUT` | `/api/admin/business-types/{businessTypeId}` | `BUSINESS_TYPE_EDIT_BUTTON` | 修改业务类型 |
| `PUT` | `/api/admin/business-types/{businessTypeId}/status` | `BUSINESS_TYPE_STATUS_BUTTON` | 启用/停用 |
| `DELETE` | `/api/admin/business-types/{businessTypeId}` | `BUSINESS_TYPE_DELETE_BUTTON` | 删除未被引用的类型 |

## 会员管理

类级权限：ADMIN。

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/admin/members` | `SYSTEM_MEMBER_USER` | 查询未删除会员列表 |
| `GET` | `/api/admin/members/{memberId}` | `SYSTEM_MEMBER_USER` | 查询会员详情 |
| `POST` | `/api/admin/members` | `MEMBER_CREATE_BUTTON` | 后台创建会员 |
| `POST` | `/api/admin/members/profile-files` | `MEMBER_CREATE_BUTTON` 或 `MEMBER_EDIT_BUTTON` | 上传会员资料文件 |
| `PUT` | `/api/admin/members/{memberId}` | `MEMBER_EDIT_BUTTON` | 修改会员资料、业务类型、过期时间 |
| `PUT` | `/api/admin/members/{memberId}/download-access` | `MEMBER_DOWNLOAD_BUTTON` | 开关附件下载权限 |
| `PUT` | `/api/admin/members/{memberId}/status` | `MEMBER_STATUS_BUTTON` | 启用/禁用会员 |
| `PUT` | `/api/admin/members/{memberId}/password` | `MEMBER_PASSWORD_BUTTON` | 重置会员密码 |
| `DELETE` | `/api/admin/members/{memberId}` | `MEMBER_DELETE_BUTTON` | 软删除会员 |

## 招标管理

类级权限：ADMIN。

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/admin/tenders` | `SYSTEM_TENDER` | 后台分页查询招标，支持 `keyword`、`region`、`businessTypeId` |
| `GET` | `/api/admin/tenders/{tenderId}` | `SYSTEM_TENDER` | 查询招标详情 |
| `POST` | `/api/admin/tenders` | `TENDER_CREATE_BUTTON` | 新增草稿招标 |
| `PUT` | `/api/admin/tenders/{tenderId}` | `TENDER_EDIT_BUTTON` | 编辑草稿招标 |
| `PUT` | `/api/admin/tenders/{tenderId}/status` | `TENDER_PUBLISH_BUTTON` | 发布或改为未发布 |
| `DELETE` | `/api/admin/tenders/{tenderId}` | `TENDER_DELETE_BUTTON` | 删除草稿招标 |
| `POST` | `/api/admin/tenders/{tenderId}/attachments` | `TENDER_CREATE_BUTTON` 或 `TENDER_EDIT_BUTTON` | 追加附件 |
| `DELETE` | `/api/admin/tenders/{tenderId}/attachments/{attachmentId}` | `TENDER_EDIT_BUTTON` | 删除附件 |

## 资讯管理

类级权限：ADMIN。

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/admin/news` | `SYSTEM_NEWS` | 后台分页查询资讯，支持 `keyword`、`category`、`status` |
| `GET` | `/api/admin/news/{newsId}` | `SYSTEM_NEWS` | 查询资讯详情 |
| `POST` | `/api/admin/news` | `NEWS_CREATE_BUTTON` | 新增草稿资讯 |
| `PUT` | `/api/admin/news/{newsId}` | `NEWS_EDIT_BUTTON` | 编辑草稿资讯 |
| `PUT` | `/api/admin/news/{newsId}/status` | `NEWS_PUBLISH_BUTTON` | 发布或下架 |
| `DELETE` | `/api/admin/news/{newsId}` | `NEWS_DELETE_BUTTON` | 删除草稿资讯 |

## 文件接口

| 方法 | 路径 | 认证/权限 | 说明 |
| --- | --- | --- | --- |
| `POST` | `/api/admin/files/upload` | ADMIN，`TENDER_UPLOAD_BUTTON` 或 `NEWS_UPLOAD_BUTTON` | 后台通用文件上传 |
| `GET` | `/api/admin/files/{fileId}/download` | ADMIN，上传/会员维护相关按钮 | 附件下载，`attachment` |
| `GET` | `/api/admin/files/{fileId}/view` | ADMIN，上传/会员维护相关按钮 | 页内查看，`inline` |
| `GET` | `/api/files/{fileId}/thumbnail` | 公开 | 公开缩略图 |

## 门户认证与会员资料

| 方法 | 路径 | 认证 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/portal/auth/captcha` | 公开 | 获取验证码图片，参数 `scene=register|login`、`captchaId` |
| `POST` | `/api/portal/auth/register` | 公开 | 会员注册，`multipart/form-data` |
| `POST` | `/api/portal/auth/login` | 公开 | 会员登录 |
| `GET` | `/api/portal/auth/me` | MEMBER | 当前会员信息 |
| `POST` | `/api/portal/auth/profile/files` | MEMBER | 上传会员资料文件 |
| `PUT` | `/api/portal/auth/profile` | MEMBER | 更新会员资料 |
| `PUT` | `/api/portal/auth/password` | MEMBER | 修改当前会员密码，需 `oldPassword`、`password`、`confirmPassword` |

## 门户招标

| 方法 | 路径 | 认证 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/portal/tenders/latest` | 公开 | 最新 3 条公开招标 |
| `GET` | `/api/portal/tenders` | MEMBER | 会员授权范围内的招标列表，支持 `keyword`、`region`、`businessTypeName` |
| `GET` | `/api/portal/tenders/{tenderId}` | 公开，可带会员 token | 公开招标详情，返回 `canDownload` |
| `GET` | `/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download` | MEMBER | 下载招标附件 |

## 门户资讯

| 方法 | 路径 | 认证 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/portal/news` | 公开 | 门户资讯分页，支持 `keyword`、`category` |
| `GET` | `/api/portal/news/latest` | 公开 | 最新资讯，`limit` 默认 6，最大 20 |
| `GET` | `/api/portal/news/{newsId}` | 公开 | 资讯详情 |

## 菜单、角色、日志

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/admin/roles` | `SYSTEM_ROLE` | 查询角色 |
| `POST` | `/api/admin/roles` | `ROLE_EDIT_BUTTON` | 新增角色 |
| `PUT` | `/api/admin/roles/{roleId}` | `ROLE_EDIT_BUTTON` | 修改角色 |
| `DELETE` | `/api/admin/roles/{roleId}` | `ROLE_EDIT_BUTTON` | 删除角色 |
| `GET` | `/api/admin/menus` | `SYSTEM_MENU` | 查询菜单树 |
| `POST` | `/api/admin/menus` | `MENU_EDIT_BUTTON` | 新增菜单 |
| `PUT` | `/api/admin/menus/{menuId}` | `MENU_EDIT_BUTTON` | 修改菜单 |
| `DELETE` | `/api/admin/menus/{menuId}` | `MENU_EDIT_BUTTON` | 删除菜单 |
| `GET` | `/api/admin/operation-logs` | `SYSTEM_OPERATION_LOG` | 查询操作日志 |
