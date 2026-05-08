# 会员软删除与附件缩略图前端对接说明

日期：2026-05-08

本次只改后端，前端需要按下面接口和字段接入。

## 会员删除

后台会员管理新增删除接口：

```http
DELETE /api/admin/members/{memberId}
Authorization: Bearer <adminToken>
```

权限码：

```text
MEMBER_DELETE_BUTTON
```

成功返回：

```json
{
  "code": 0,
  "message": "删除会员成功",
  "data": null
}
```

前端处理：

- 会员删除是软删除，不是物理删除。
- 删除成功后刷新会员列表即可。
- 默认会员列表 `GET /api/admin/members` 只返回未删除会员。
- 删除后的会员不能登录，旧 token 也不能继续访问会员接口。
- 删除后允许后台重新创建同名用户名、手机号、邮箱、统一社会信用代码的会员。
- 第一版后端没有回收站、恢复、查询已删除会员接口。

## 缩略图访问

所有缩略图统一返回可直接展示的 URL：

```text
/api/files/{fileId}/thumbnail
```

这个 URL 可以直接放到图片标签：

```html
<img :src="attachment.thumbnailUrl" />
```

生产环境前端在 `https://xiazhiyong.vip/ztbfb/` 或 `https://xiazhiyong.vip/ztbgl/` 下访问时，不需要拼后端 IP。浏览器会自动请求：

```text
https://xiazhiyong.vip/api/files/{fileId}/thumbnail
```

缩略图接口允许游客访问：

```http
GET /api/files/{fileId}/thumbnail
```

返回图片流，通常是：

```http
Content-Type: image/jpeg
```

附件下载权限不变。游客可以看缩略图和附件元信息，但不能下载附件。

## 缩略图生成规则

- 图片文件：生成真实图片缩略图。
- PDF：渲染第一页生成缩略图。
- Word、PPT、Excel、压缩包、未知文件等：生成带文件类型文字的封面图，例如 `DOCX`、`PPTX`、`XLSX`、`ZIP`。
- 历史文件如果数据库里还没有缩略图，访问缩略图 URL 时后端会尝试补生成。

`thumbnailStatus` 含义：

```text
READY        已生成真实缩略图，比如图片或 PDF 首页
UNSUPPORTED 不是图片/PDF，已生成文件类型封面图
FAILED       原文件缩略图生成失败，后端返回兜底封面图
```

前端建议：只要有 `thumbnailUrl` 就展示图片；`thumbnailStatus` 可用于调试或做特殊标识，不建议阻止展示。

## 文件上传响应新增字段

影响这些上传接口：

```http
POST /api/admin/files/upload
POST /api/admin/members/profile-files
POST /api/portal/auth/profile/files
```

返回的 `FileUploadResponse` 新增：

```json
{
  "fileId": 123,
  "fileName": "合同.pdf",
  "contentType": "application/pdf",
  "fileSize": 204800,
  "thumbnailUrl": "/api/files/123/thumbnail",
  "thumbnailContentType": "image/jpeg",
  "thumbnailStatus": "READY"
}
```

上传完成后，前端可以直接使用上传响应里的 `thumbnailUrl` 展示预览，不需要等详情接口重新查询。

## 招标附件字段

`TenderAttachmentDto` 新增：

```json
{
  "attachmentId": 10,
  "fileId": 123,
  "fileName": "招标文件.pdf",
  "contentType": "application/pdf",
  "fileSize": 204800,
  "thumbnailUrl": "/api/files/123/thumbnail",
  "thumbnailContentType": "image/jpeg",
  "thumbnailStatus": "READY"
}
```

影响这些返回招标附件的接口：

```http
GET /api/admin/tenders/{tenderId}
POST /api/admin/tenders
PUT /api/admin/tenders/{tenderId}
POST /api/admin/tenders/{tenderId}/attachments
DELETE /api/admin/tenders/{tenderId}/attachments/{attachmentId}
GET /api/portal/tenders/{tenderId}
```

门户游客访问 `GET /api/portal/tenders/{tenderId}` 时，也会拿到附件的 `thumbnailUrl`。

## 会员资料文件字段

`MemberUserDto` 的营业执照文件新增：

```json
{
  "businessLicenseFileId": 1,
  "businessLicenseFileName": "营业执照.pdf",
  "businessLicenseContentType": "application/pdf",
  "businessLicenseFileSize": 102400,
  "businessLicenseThumbnailUrl": "/api/files/1/thumbnail",
  "businessLicenseThumbnailContentType": "image/jpeg",
  "businessLicenseThumbnailStatus": "READY"
}
```

`MemberUserDto` 的三年业绩文件新增：

```json
{
  "threeYearPerformanceFileId": 2,
  "threeYearPerformanceFileName": "三年业绩.docx",
  "threeYearPerformanceContentType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  "threeYearPerformanceFileSize": 204800,
  "threeYearPerformanceThumbnailUrl": "/api/files/2/thumbnail",
  "threeYearPerformanceThumbnailContentType": "image/jpeg",
  "threeYearPerformanceThumbnailStatus": "UNSUPPORTED"
}
```

影响这些返回会员信息的接口：

```http
GET /api/admin/members
GET /api/admin/members/{memberId}
POST /api/admin/members
PUT /api/admin/members/{memberId}
PUT /api/admin/members/{memberId}/download-access
PUT /api/admin/members/{memberId}/status
POST /api/portal/auth/login
GET /api/portal/auth/me
PUT /api/portal/auth/profile
```

如果某个会员资料文件为空，对应的 `fileId/fileName/thumbnailUrl` 也为空，前端隐藏该缩略图即可。

## 本地开发注意

生产环境同域名时直接用 `/api/files/{fileId}/thumbnail`。

如果本地前端是 `localhost:5174` 或 `localhost:8081`，后端是 `localhost:8080`，属于不同端口。此时沿用前端现有代理规则，或者按现有 API base 拼接完整地址。
