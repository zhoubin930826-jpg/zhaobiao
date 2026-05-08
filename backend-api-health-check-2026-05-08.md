# 后端接口全量体检报告（2026-05-08）

## 体检范围

- 代码范围：`src/main/java/com/zhaobiao/admin/controller`、`service`、`dto`、`config/SecurityConfig.java`、`config/OpenApiConfig.java`。
- 接口范围：当前 Springdoc `/v3/api-docs` 暴露的全部后端接口。
- 判断口径：接口路径、权限、输入 DTO 校验、输出 DTO、核心业务规则、是否和现有功能面匹配。
- 本次只处理后端代码和后端文档；前端目录不改。

## 总体结论

- 后端接口功能面基本完整，覆盖后台认证、个人中心、管理员账号、会员、业务类型、招标、文件缩略图、角色、菜单、操作日志、门户会员认证、门户招标展示与下载。
- 已停用接口有明确保留路径：后台公开注册、门户公开注册、旧用户管理、权限管理。其中注册接口返回 `403`，旧用户和权限管理返回 `410`，这是当前业务规则，不是缺失实现。
- JSON 业务接口统一返回 `ApiResponse<T>`；业务失败、参数失败、权限失败多数是 HTTP 200 加 `code != 0`。Swagger 和 DTO 已补充说明，调用方必须判断 `code`。
- 文件预览能力后端已具备：上传响应和招标/会员资料 DTO 都返回缩略图 URL，`GET /api/files/{fileId}/thumbnail` 可直接返回图片流。
- 主要缺口不在后端可用性，而在调用方契约未完全对齐：门户资料更新和门户资料上传路径应使用后端现有 `/api/portal/auth/profile`、`/api/portal/auth/profile/files`；会员资料模型后端当前只支持营业执照和三年内业绩文件两个资料位，不支持任意附件列表和 `performanceDesc` 字段。

## 通用响应和错误码

| 项 | 当前行为 | 结论 |
| --- | --- | --- |
| 成功响应 | `ApiResponse<T>{code:0,message:"success",data,timestamp}` | 满足 |
| 业务失败 | `BusinessException` 返回 HTTP 200，`code` 为业务码 | 满足，但前端必须判断 `code` |
| 参数校验失败 | Bean Validation 返回 HTTP 200，`code=400` | 满足 |
| 权限不足 | `AccessDeniedException` 或安全处理器返回 `403` 业务响应 | 满足 |
| 未处理异常 | HTTP 500，`code=500` | 满足 |
| 分页结构 | `PageResult<T>{pageNum,pageSize,total,totalPages,list}` | 满足；招标列表最大 `pageSize=50` |

## 接口清单

### 认证接口

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/auth/login` | 公开 | `LoginRequest(username,password)` | `LoginResponse(token,tokenType,expireSeconds,user)` | 满足。只允许状态正常的后台管理员登录。 |
| GET | `/api/auth/me` | `ADMIN` | Bearer 管理员 JWT | `UserProfileDto` | 满足。返回角色、菜单、权限，供后台前端导航使用。 |
| POST | `/api/auth/register` | 公开 | `RegisterRequest` | 固定失败 `code=403` | 满足当前规则。后台公开注册已停用。 |

### 个人中心

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/profile` | `PROFILE` | Bearer 管理员 JWT | `UserProfileDto` | 满足。 |
| PUT | `/api/profile` | `PROFILE_EDIT_BUTTON` | `ProfileUpdateRequest` | `UserProfileDto` | 满足。支持手机号、邮箱、实名、公司、联系人、信用代码和可选改密。 |

### 管理员账号

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/admin/admin-users` | `SUPER_ADMIN` | 无 | `List<UserProfileDto>` | 满足。只列后台管理员。 |
| POST | `/api/admin/admin-users` | `SUPER_ADMIN` | `AdminUserCreateRequest` | `UserProfileDto` | 满足。校验账号唯一、密码一致、角色存在且非 `NORMAL_USER`。 |
| PUT | `/api/admin/admin-users/{userId}` | `SUPER_ADMIN` | `AdminUserUpdateRequest` | `UserProfileDto` | 满足。目标必须是管理员。 |
| PUT | `/api/admin/admin-users/{userId}/roles` | `SUPER_ADMIN` | `UserRoleUpdateRequest(roleIds)` | `UserProfileDto` | 满足。初始 `admin` 必须保留 `SUPER_ADMIN`。 |
| PUT | `/api/admin/admin-users/{userId}/status` | `SUPER_ADMIN` | `AdminUserStatusUpdateRequest(status)` | `UserProfileDto` | 满足。仅支持 `APPROVED`、`DISABLED`。 |
| PUT | `/api/admin/admin-users/{userId}/password` | `SUPER_ADMIN` | `AdminUserPasswordResetRequest` | `data=null` | 满足。 |

### 旧用户管理（已停用）

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/admin/users` | `ADMIN` | 无 | 固定失败 `code=410` | 满足当前规则。 |
| PUT | `/api/admin/users/{userId}/audit` | `ADMIN` | `userId` | 固定失败 `code=410` | 满足当前规则。 |
| PUT | `/api/admin/users/{userId}/roles` | `ADMIN` | `userId` | 固定失败 `code=410` | 满足当前规则。 |
| GET | `/api/admin/users/{userId}/audit-records` | `ADMIN` | `userId` | 固定失败 `code=410` | 满足当前规则。 |

### 会员管理

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/admin/members` | `SYSTEM_MEMBER_USER` | 无 | `List<MemberUserDto>` | 满足。未分页，前端当前做本地分页。 |
| GET | `/api/admin/members/{memberId}` | `SYSTEM_MEMBER_USER` | `memberId` | `MemberUserDto` | 满足。 |
| POST | `/api/admin/members` | `MEMBER_CREATE_BUTTON` | `MemberCreateRequest` | `MemberUserDto` | 满足。校验账号唯一、业务类型启用、有效期、密码一致。 |
| POST | `/api/admin/members/profile-files` | `MEMBER_CREATE_BUTTON` 或 `MEMBER_EDIT_BUTTON` | `multipart files` | `List<FileUploadResponse>` | 满足。用于营业执照、三年内业绩资料文件。 |
| PUT | `/api/admin/members/{memberId}` | `MEMBER_EDIT_BUTTON` | `MemberUpdateRequest` | `MemberUserDto` | 满足。资料文件 ID 不传或为 null 时保持原值。 |
| PUT | `/api/admin/members/{memberId}/download-access` | `MEMBER_DOWNLOAD_BUTTON` | `canDownloadFile` | `MemberUserDto` | 满足。 |
| PUT | `/api/admin/members/{memberId}/status` | `MEMBER_STATUS_BUTTON` | `status=ENABLED/DISABLED` | `MemberUserDto` | 满足。 |
| PUT | `/api/admin/members/{memberId}/password` | `MEMBER_PASSWORD_BUTTON` | `MemberPasswordResetRequest` | `data=null` | 满足。 |
| DELETE | `/api/admin/members/{memberId}` | `MEMBER_DELETE_BUTTON` | `memberId` | `data=null` | 满足。软删除。 |

注意：后端会员资料模型当前只有 `businessLicenseFileId` 和 `threeYearPerformanceFileId` 两个资料文件位。若产品需要“业绩描述”或多个任意附件，需要后端新增字段和迁移；当前接口不会持久化这些额外字段。

### 业务类型

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/admin/business-types` | `SYSTEM_BUSINESS_TYPE` | 无 | `List<BusinessTypeDto>` | 满足。 |
| GET | `/api/admin/business-types/options` | `SYSTEM_BUSINESS_TYPE` | 无 | `List<BusinessTypeOptionDto>` | 满足。只返回启用项。 |
| POST | `/api/admin/business-types` | `BUSINESS_TYPE_CREATE_BUTTON` | `BusinessTypeCreateRequest` | `BusinessTypeDto` | 满足。code 自动大写，code/name 唯一。 |
| PUT | `/api/admin/business-types/{businessTypeId}` | `BUSINESS_TYPE_EDIT_BUTTON` | `BusinessTypeUpdateRequest` | `BusinessTypeDto` | 满足。 |
| PUT | `/api/admin/business-types/{businessTypeId}/status` | `BUSINESS_TYPE_STATUS_BUTTON` | `enabled` | `BusinessTypeDto` | 满足。 |
| DELETE | `/api/admin/business-types/{businessTypeId}` | `BUSINESS_TYPE_DELETE_BUTTON` | `businessTypeId` | `data=null` | 满足。已被会员或招标引用时不能删除。 |

### 招标管理

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/admin/tenders` | `SYSTEM_TENDER` | `pageNum,pageSize,keyword,region,businessTypeId` | `PageResult<TenderListItemDto>` | 满足。按发布时间和 ID 倒序。 |
| GET | `/api/admin/tenders/{tenderId}` | `SYSTEM_TENDER` | `tenderId` | `TenderDto` | 满足。含正文 HTML 和附件缩略图字段。 |
| POST | `/api/admin/tenders` | `TENDER_CREATE_BUTTON` | `TenderUpsertRequest` | `TenderDto` | 满足。校验业务类型启用、项目编号唯一、时间顺序。 |
| PUT | `/api/admin/tenders/{tenderId}` | `TENDER_EDIT_BUTTON` | `TenderUpsertRequest` | `TenderDto` | 满足。`attachmentFileIds` 不传保留原附件，传空数组清空附件。 |
| DELETE | `/api/admin/tenders/{tenderId}` | `TENDER_DELETE_BUTTON` | `tenderId` | `data=null` | 满足。会清理无引用文件。 |
| POST | `/api/admin/tenders/{tenderId}/attachments` | `TENDER_CREATE_BUTTON` 或 `TENDER_EDIT_BUTTON` | `TenderAttachmentBindRequest(fileIds)` | `TenderDto` | 满足。追加并去重。 |
| DELETE | `/api/admin/tenders/{tenderId}/attachments/{attachmentId}` | `TENDER_EDIT_BUTTON` | `tenderId,attachmentId` | `TenderDto` | 满足。删除附件关系并清理无引用文件。 |

### 文件和预览

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/admin/files/upload` | `TENDER_UPLOAD_BUTTON` | `multipart files` | `List<FileUploadResponse>` | 满足。返回 `thumbnailUrl`、`thumbnailContentType`、`thumbnailStatus`。 |
| GET | `/api/files/{fileId}/thumbnail` | 公开 | `fileId` | 图片流 | 满足。可直接用于 `<img src>`；本地实测已生成并可返回上传图片缩略图。 |

预览返回链路：上传时存储服务生成缩略图并写入 `biz_file_storage.thumbnail_*` 字段；上传响应、招标附件 DTO、门户附件 DTO、会员资料 DTO 都带缩略图 URL；前端直接请求该 URL 即可展示。

### 角色和菜单

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/admin/roles` | `SYSTEM_ROLE` | 无 | `List<RoleDto>` | 满足。 |
| POST | `/api/admin/roles` | `ROLE_EDIT_BUTTON` | `RoleRequest` | `RoleDto` | 满足。至少分配一个菜单。 |
| PUT | `/api/admin/roles/{roleId}` | `ROLE_EDIT_BUTTON` | `RoleRequest` | `RoleDto` | 满足。内置角色不能改 code。 |
| DELETE | `/api/admin/roles/{roleId}` | `ROLE_EDIT_BUTTON` | `roleId` | `data=null` | 满足。内置角色、被用户使用角色不能删。 |
| GET | `/api/admin/menus` | `SYSTEM_MENU` | 无 | `List<MenuDto>` 树 | 满足。 |
| POST | `/api/admin/menus` | `MENU_EDIT_BUTTON` | `MenuRequest` | `MenuDto` | 满足。code 唯一，父级存在。 |
| PUT | `/api/admin/menus/{menuId}` | `MENU_EDIT_BUTTON` | `MenuRequest` | `MenuDto` | 满足。不能选择自己为父级。 |
| DELETE | `/api/admin/menus/{menuId}` | `MENU_EDIT_BUTTON` | `menuId` | `data=null` | 满足。有子菜单或被角色引用不能删。 |

### 权限管理（已停用）

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/admin/permissions` | `ADMIN` | 无 | 固定失败 `code=410` | 满足当前规则。 |
| POST | `/api/admin/permissions` | `ADMIN` | `PermissionRequest` | 固定失败 `code=410` | 满足当前规则。 |
| PUT | `/api/admin/permissions/{permissionId}` | `ADMIN` | `PermissionRequest` | 固定失败 `code=410` | 满足当前规则。 |
| DELETE | `/api/admin/permissions/{permissionId}` | `ADMIN` | `permissionId` | 固定失败 `code=410` | 满足当前规则。 |

### 操作日志

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/admin/operation-logs` | `SYSTEM_OPERATION_LOG` | 无 | `List<OperationLogDto>` | 满足。当前未分页，数据量大时可考虑后续分页。 |

### 门户会员认证和资料

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/portal/auth/login` | 公开 | `MemberLoginRequest` | `MemberLoginResponse` | 满足。校验启用、未过期、至少绑定启用业务类型；首次登录返回 `profileCompletionRequired=true`。 |
| GET | `/api/portal/auth/me` | `MEMBER` | 会员 JWT | `MemberUserDto` | 满足。 |
| POST | `/api/portal/auth/profile/files` | `MEMBER` | `multipart files` | `List<FileUploadResponse>` | 满足。用于会员自己上传资料文件。 |
| PUT | `/api/portal/auth/profile` | `MEMBER` | `MemberProfileUpdateRequest` | `MemberUserDto` | 满足。支持联系方式、公司信息、实名、营业执照文件、三年内业绩文件。 |
| POST | `/api/portal/auth/register` | 公开 | `MemberRegisterRequest` | 固定失败 `code=403` | 满足当前规则。会员在线注册已停用。 |

### 门户招标

| 方法 | 路径 | 权限 | 输入 | 输出 | 体检结论 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/portal/tenders/latest` | 公开 | 无 | `List<TenderListItemDto>` | 满足。最新 3 条公开招标，不按会员业务类型过滤。 |
| GET | `/api/portal/tenders` | `MEMBER` | `pageNum,pageSize,keyword,region` | `PageResult<TenderListItemDto>` | 满足。只返回会员业务类型范围内、已发布且已到发布时间的招标。 |
| GET | `/api/portal/tenders/{tenderId}` | 公开 | `tenderId`，可选会员 JWT | `TenderDetailDto` | 满足。公开查看详情；`canDownload` 根据会员和下载权限计算。 |
| GET | `/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download` | `MEMBER` | `tenderId,attachmentId` | 文件流 | 满足。要求会员可访问该业务类型、允许下载、附件属于该招标。 |

## 发现的契约风险

1. 门户资料更新调用方应调用 `PUT /api/portal/auth/profile`，不是 `PUT /api/portal/auth/me`。后端接口存在且 Swagger 已补说明。
2. 门户资料文件上传调用方应调用 `POST /api/portal/auth/profile/files`，不是 `/api/portal/files/upload`。后端接口存在且 Swagger 已补说明。
3. 后台会员资料文件应优先使用 `POST /api/admin/members/profile-files`，因为权限是会员新增/编辑权限；`POST /api/admin/files/upload` 是招标附件上传权限。
4. 后端当前会员资料只有营业执照和三年内业绩文件两个文件位。如果调用方提交 `performanceDesc` 或 `attachmentFileIds`，后端 DTO 不接收也不会持久化。
5. 操作日志和会员列表目前返回全量数组，没有分页。当前功能可用；如果数据量增长，需要新增分页参数和前端分页联调。

## Swagger 补充情况

- `OpenApiConfig` 已补全 API 总说明、认证方式、公共接口说明、全部接口 operation description、分组 tag description。
- `ApiResponse` 已补业务码和 HTTP 200 包业务失败的说明。
- `PageResult` 已补分页字段 schema 说明。
- Swagger UI 使用方式：打开 `/swagger-ui.html` 或 `/swagger-ui/index.html`，受保护接口先点击 Authorize，填入后台或门户登录接口返回的 Bearer Token。

