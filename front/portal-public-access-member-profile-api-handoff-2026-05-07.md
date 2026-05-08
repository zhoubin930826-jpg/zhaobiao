# 门户游客访问与会员资料字段前端对接说明

日期：2026-05-07

## 门户招标列表

未登录时按游客处理，只展示最新 3 条公开招标：

```http
GET /api/portal/tenders/latest
```

返回 `PUBLISHED` 且 `publishAt <= 当前时间` 的最新 3 条，排序为 `publishAt desc, id desc`，不按业务类型过滤。

已登录后列表页仍停留在同一个页面，但应刷新为会员列表：

```http
GET /api/portal/tenders
Authorization: Bearer <memberToken>
```

会员列表仍按会员业务类型过滤。

## 招标详情与附件下载

详情文字公开：

```http
GET /api/portal/tenders/{tenderId}
```

游客、已登录会员都能查看已发布且发布时间已到的正文和附件元信息。返回字段里的 `canDownload` 表示当前登录态是否可下载；游客固定为 `false`。

附件下载仍必须登录：

```http
GET /api/portal/tenders/{tenderId}/attachments/{attachmentId}/download
Authorization: Bearer <memberToken>
```

前端处理：

- 未登录下载：后端返回 HTTP `401`，前端提示需要登录。
- 已登录但无下载权限、业务类型不匹配或账号不允许下载：业务 `code=403`，提示“当前账号暂无附件下载权限”。
- 只有会员业务类型匹配、`canDownloadFile=true`、附件属于该招标时才返回文件流。

## 会员登录

会员登录响应新增：

```json
{
  "profileCompletionRequired": true,
  "user": {
    "firstLoginAt": "2026-05-07T10:00:00"
  }
}
```

当本次登录前 `firstLoginAt` 为空时，后端会写入当前时间，并返回 `profileCompletionRequired=true`。前端收到 true 后直接进入个人信息填写页；第二次及以后登录返回 false。

## 会员资料文件字段

`MemberUserDto` 新增：

```json
{
  "firstLoginAt": "2026-05-07T10:00:00",
  "businessLicenseFileId": 1,
  "businessLicenseFileName": "license.pdf",
  "businessLicenseContentType": "application/pdf",
  "businessLicenseFileSize": 1024,
  "threeYearPerformanceFileId": 2,
  "threeYearPerformanceFileName": "performance.pdf",
  "threeYearPerformanceContentType": "application/pdf",
  "threeYearPerformanceFileSize": 2048
}
```

两个资料文件都不是必填。

## 后台会员管理端

管理员上传会员资料文件：

```http
POST /api/admin/members/profile-files
Content-Type: multipart/form-data
Authorization: Bearer <adminToken>

files=<file>
```

返回 `fileId/fileName/contentType/fileSize`。创建或编辑会员时传：

```json
{
  "businessLicenseFileId": 1,
  "threeYearPerformanceFileId": 2
}
```

编辑会员时不传或传 `null` 表示保持原值；当前后端没有把字段清空的接口语义。

## 门户会员个人资料

会员上传资料文件：

```http
POST /api/portal/auth/profile/files
Content-Type: multipart/form-data
Authorization: Bearer <memberToken>

files=<file>
```

会员更新自己的资料：

```http
PUT /api/portal/auth/profile
Authorization: Bearer <memberToken>
Content-Type: application/json
```

请求体可选字段：

```json
{
  "phone": "13800000000",
  "email": "member@example.com",
  "companyName": "会员企业",
  "contactPerson": "联系人",
  "unifiedSocialCreditCode": "91310000MA1K123456",
  "realName": "真实姓名",
  "businessLicenseFileId": 1,
  "threeYearPerformanceFileId": 2
}
```

会员不能通过该接口修改用户名、状态、过期时间、业务类型和附件下载权限。
