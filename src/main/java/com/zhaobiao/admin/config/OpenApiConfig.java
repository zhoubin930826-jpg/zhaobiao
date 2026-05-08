package com.zhaobiao.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final String API_RESPONSE_NOTE =
            "返回：业务接口统一使用 ApiResponse<T>，code=0 表示成功；BusinessException、参数校验失败和权限不足大多仍返回 HTTP 200，但 code 为 400/403/404/410 等，前端必须判断 code。";

    @Bean
    public OpenAPI tenderOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("招标系统后台接口文档")
                        .description("招标平台后端接口。后台管理端使用管理员 JWT，门户端使用会员 JWT；除文件流接口外，JSON 响应统一包在 ApiResponse 中。"
                                + "\n\n公共接口：后台登录、门户登录、公开招标最新列表、公开招标详情、文件缩略图。"
                                + "\n\n受保护接口：在 Swagger UI 右上角 Authorize 填写 Bearer JWT 后调用；管理员接口依赖角色/权限码，会员接口依赖 MEMBER 角色。")
                        .version("v1.0.1"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomiser operationDescriptionCustomiser() {
        return openApi -> {
            openApi.setTags(Arrays.asList(
                    new Tag().name("认证接口").description("后台管理员登录、当前管理员信息，以及已停用的后台公开注册入口。"),
                    new Tag().name("个人中心").description("后台管理员查看和修改自己的手机号、邮箱、实名信息及密码。"),
                    new Tag().name("管理员-管理员账号管理").description("超级管理员专用的后台管理员账号维护接口。"),
                    new Tag().name("管理员-旧用户管理（已停用）").description("历史兼容接口，保留路径但统一返回业务码 410，不再承载功能。"),
                    new Tag().name("管理员-会员管理").description("后台维护门户会员、会员业务类型、下载权限、有效期、状态和资料文件。"),
                    new Tag().name("管理员-业务类型管理").description("招标和会员共同使用的业务类型字典。"),
                    new Tag().name("管理员-招标管理").description("后台招标公告的列表、详情、新增、编辑、删除和附件绑定。"),
                    new Tag().name("管理员-文件管理").description("后台招标附件上传，上传后返回文件 ID 和可直接展示的缩略图地址。"),
                    new Tag().name("文件缩略图").description("公开读取上传文件缩略图。图片/PDF 会尽量生成预览，其他类型返回类型封面或失败状态。"),
                    new Tag().name("管理员-角色管理").description("后台角色和菜单授权管理。权限实体不再单独开放编辑，角色通过菜单树授权。"),
                    new Tag().name("管理员-菜单管理").description("后台菜单树维护，菜单被角色引用或存在子菜单时不能删除。"),
                    new Tag().name("管理员-权限管理（已停用）").description("权限维护接口已停用，保留路径并统一返回业务码 410。"),
                    new Tag().name("管理员-操作日志").description("查看后台变更动作和登录等操作日志。"),
                    new Tag().name("门户会员认证").description("门户会员登录、当前会员信息、资料文件上传和资料更新。在线注册已停用。"),
                    new Tag().name("门户-招标公告").description("门户公开招标展示、会员范围内招标列表、详情和附件下载。")
            ));

            if (openApi.getPaths() == null) {
                return;
            }
            Map<String, String> descriptions = operationDescriptions();
            Set<String> publicOperations = publicOperations();
            openApi.getPaths().forEach((path, pathItem) -> {
                apply(path, "GET", pathItem.getGet(), descriptions, publicOperations);
                apply(path, "POST", pathItem.getPost(), descriptions, publicOperations);
                apply(path, "PUT", pathItem.getPut(), descriptions, publicOperations);
                apply(path, "DELETE", pathItem.getDelete(), descriptions, publicOperations);
            });
        };
    }

    private void apply(String path,
                       String method,
                       Operation operation,
                       Map<String, String> descriptions,
                       Set<String> publicOperations) {
        if (operation == null) {
            return;
        }
        String key = key(method, path);
        String description = descriptions.get(key);
        if (description != null) {
            operation.setDescription(description + "\n\n" + API_RESPONSE_NOTE);
        }
        if (publicOperations.contains(key)) {
            operation.setSecurity(Collections.emptyList());
        }
        if (isBinaryOperation(key) && operation.getResponses() != null && operation.getResponses().get("200") != null) {
            operation.getResponses().get("200").setDescription("HTTP 200 返回文件流；失败时可能返回 ApiResponse JSON，按 code 判断失败原因。");
        }
    }

    private boolean isBinaryOperation(String key) {
        return "GET /api/portal/tenders/{tenderId}/attachments/{attachmentId}/download".equals(key)
                || "GET /api/files/{fileId}/thumbnail".equals(key);
    }

    private Set<String> publicOperations() {
        Set<String> keys = new LinkedHashSet<>();
        add(keys, "POST", "/api/auth/register");
        add(keys, "POST", "/api/auth/login");
        add(keys, "POST", "/api/portal/auth/register");
        add(keys, "POST", "/api/portal/auth/login");
        add(keys, "GET", "/api/portal/tenders/latest");
        add(keys, "GET", "/api/portal/tenders/{tenderId}");
        add(keys, "GET", "/api/files/{fileId}/thumbnail");
        return keys;
    }

    private Map<String, String> operationDescriptions() {
        Map<String, String> map = new LinkedHashMap<>();

        put(map, "POST", "/api/auth/register", "功能：后台管理员公开注册入口。当前业务已停用，仅保留兼容路径。\n输入：RegisterRequest。\n输出：固定失败，code=403，提示联系超级管理员创建账号。\n权限：公开。");
        put(map, "POST", "/api/auth/login", "功能：后台管理员登录。\n输入：username、password。\n输出：token、tokenType=Bearer、expireSeconds、当前管理员 user。\n规则：仅 APPROVED 且未禁用的管理员可登录；PENDING、REJECTED、DISABLED 会返回业务失败。");
        put(map, "GET", "/api/auth/me", "功能：获取当前后台登录管理员信息。\n输入：Authorization: Bearer 管理员 token。\n输出：UserProfileDto，包含角色、菜单、权限等前端导航所需信息。\n权限：ADMIN 角色。");

        put(map, "GET", "/api/profile", "功能：获取后台管理员个人中心资料。\n输入：管理员 token。\n输出：UserProfileDto。\n权限：PROFILE。");
        put(map, "PUT", "/api/profile", "功能：修改后台管理员个人资料和可选新密码。\n输入：ProfileUpdateRequest；password/confirmPassword 同时传入才改密。\n输出：更新后的 UserProfileDto。\n权限：PROFILE_EDIT_BUTTON。");

        put(map, "GET", "/api/admin/admin-users", "功能：查询后台管理员账号列表。\n输入：无查询参数。\n输出：UserProfileDto 数组，按创建时间倒序。\n权限：SUPER_ADMIN 角色。");
        put(map, "POST", "/api/admin/admin-users", "功能：新增后台管理员账号。\n输入：AdminUserCreateRequest，含用户名、手机号、邮箱、密码、确认密码、角色 ID 列表。\n输出：新管理员 UserProfileDto。\n规则：账号创建后为 APPROVED；不能分配 NORMAL_USER 会员角色；用户名/手机号/邮箱唯一。权限：SUPER_ADMIN。");
        put(map, "PUT", "/api/admin/admin-users/{userId}", "功能：修改后台管理员基础资料。\n输入：userId 路径参数，AdminUserUpdateRequest。\n输出：更新后的 UserProfileDto。\n规则：目标必须是管理员；手机号、邮箱唯一。权限：SUPER_ADMIN。");
        put(map, "PUT", "/api/admin/admin-users/{userId}/roles", "功能：修改后台管理员角色。\n输入：userId，UserRoleUpdateRequest.roleIds。\n输出：更新后的 UserProfileDto。\n规则：至少一个角色；不得分配 NORMAL_USER；初始 admin 必须保留 SUPER_ADMIN。权限：SUPER_ADMIN。");
        put(map, "PUT", "/api/admin/admin-users/{userId}/status", "功能：启用或禁用后台管理员。\n输入：userId，AdminUserStatusUpdateRequest.status，仅支持 APPROVED 或 DISABLED。\n输出：更新后的 UserProfileDto。\n规则：初始 admin 不允许禁用。权限：SUPER_ADMIN。");
        put(map, "PUT", "/api/admin/admin-users/{userId}/password", "功能：重置后台管理员密码。\n输入：userId，AdminUserPasswordResetRequest。\n输出：data=null。\n规则：密码与确认密码必须一致。权限：SUPER_ADMIN。");

        put(map, "GET", "/api/admin/users", "功能：旧用户管理列表接口，已停用。\n输入：无。\n输出：固定失败，code=410。\n权限：ADMIN。");
        put(map, "PUT", "/api/admin/users/{userId}/audit", "功能：旧用户审核接口，已停用。\n输入：userId。\n输出：固定失败，code=410。\n权限：ADMIN。");
        put(map, "PUT", "/api/admin/users/{userId}/roles", "功能：旧用户角色接口，已停用。\n输入：userId。\n输出：固定失败，code=410。\n权限：ADMIN。");
        put(map, "GET", "/api/admin/users/{userId}/audit-records", "功能：旧用户审核记录接口，已停用。\n输入：userId。\n输出：固定失败，code=410。\n权限：ADMIN。");

        put(map, "GET", "/api/admin/members", "功能：查询未删除会员列表。\n输入：无分页参数，前端当前本地分页。\n输出：MemberUserDto 数组，含业务类型、下载权限、有效期、资料文件缩略图字段。\n权限：SYSTEM_MEMBER_USER。");
        put(map, "GET", "/api/admin/members/{memberId}", "功能：查询会员详情。\n输入：memberId。\n输出：MemberUserDto。\n权限：SYSTEM_MEMBER_USER。");
        put(map, "POST", "/api/admin/members", "功能：后台创建门户会员账号。\n输入：MemberCreateRequest，含账号资料、密码、业务类型 ID、有效期、可选资料文件 ID。\n输出：新会员 MemberUserDto。\n规则：业务类型至少一个且必须启用；账号字段唯一；默认不允许下载文件，除非 canDownloadFile=true。权限：MEMBER_CREATE_BUTTON。");
        put(map, "POST", "/api/admin/members/profile-files", "功能：后台上传会员资料文件。\n输入：multipart/form-data，字段名 files，可一次上传多个文件。\n输出：FileUploadResponse 数组，包含 fileId、fileName、contentType、fileSize、thumbnailUrl、thumbnailStatus。\n规则：返回的 fileId 可写入 businessLicenseFileId 或 threeYearPerformanceFileId。权限：MEMBER_CREATE_BUTTON 或 MEMBER_EDIT_BUTTON。");
        put(map, "PUT", "/api/admin/members/{memberId}", "功能：修改会员资料。\n输入：memberId，MemberUpdateRequest。\n输出：更新后的 MemberUserDto。\n规则：业务类型至少一个且必须启用；资料文件 ID 不传或为 null 表示保持原值。权限：MEMBER_EDIT_BUTTON。");
        put(map, "PUT", "/api/admin/members/{memberId}/download-access", "功能：修改会员附件下载权限。\n输入：memberId，canDownloadFile。\n输出：更新后的 MemberUserDto。\n权限：MEMBER_DOWNLOAD_BUTTON。");
        put(map, "PUT", "/api/admin/members/{memberId}/status", "功能：启用或禁用会员。\n输入：memberId，status=ENABLED 或 DISABLED。\n输出：更新后的 MemberUserDto。\n权限：MEMBER_STATUS_BUTTON。");
        put(map, "PUT", "/api/admin/members/{memberId}/password", "功能：重置会员密码。\n输入：memberId，MemberPasswordResetRequest。\n输出：data=null。\n规则：密码与确认密码必须一致。权限：MEMBER_PASSWORD_BUTTON。");
        put(map, "DELETE", "/api/admin/members/{memberId}", "功能：删除会员。\n输入：memberId。\n输出：data=null。\n规则：软删除，标记 deleted/deletedAt，不物理删除历史数据。权限：MEMBER_DELETE_BUTTON。");

        put(map, "GET", "/api/admin/business-types", "功能：查询全部业务类型。\n输入：无。\n输出：BusinessTypeDto 数组，含启用状态和排序。\n权限：SYSTEM_BUSINESS_TYPE。");
        put(map, "GET", "/api/admin/business-types/options", "功能：查询启用中的业务类型选项。\n输入：无。\n输出：BusinessTypeOptionDto 数组。\n用途：会员、招标表单下拉。权限：SYSTEM_BUSINESS_TYPE。");
        put(map, "POST", "/api/admin/business-types", "功能：新增业务类型。\n输入：BusinessTypeCreateRequest。\n输出：BusinessTypeDto。\n规则：code 自动转大写并要求唯一，name 唯一；sortOrder 不传时按最大排序加 10。权限：BUSINESS_TYPE_CREATE_BUTTON。");
        put(map, "PUT", "/api/admin/business-types/{businessTypeId}", "功能：修改业务类型。\n输入：businessTypeId，BusinessTypeUpdateRequest。\n输出：BusinessTypeDto。\n规则：code/name 不能与其他类型重复。权限：BUSINESS_TYPE_EDIT_BUTTON。");
        put(map, "PUT", "/api/admin/business-types/{businessTypeId}/status", "功能：启用或停用业务类型。\n输入：businessTypeId，enabled。\n输出：BusinessTypeDto。\n权限：BUSINESS_TYPE_STATUS_BUTTON。");
        put(map, "DELETE", "/api/admin/business-types/{businessTypeId}", "功能：删除业务类型。\n输入：businessTypeId。\n输出：data=null。\n规则：如果已被会员或招标引用不能删除，只能先停用。权限：BUSINESS_TYPE_DELETE_BUTTON。");

        put(map, "GET", "/api/admin/files/upload", "功能：无 GET 接口。请使用 POST /api/admin/files/upload。");
        put(map, "POST", "/api/admin/files/upload", "功能：上传招标附件文件。\n输入：multipart/form-data，字段名 files，可上传多个文件。\n输出：FileUploadResponse 数组。\n规则：上传成功即生成文件记录；图片和 PDF 尽量生成缩略图，前端可直接展示 thumbnailUrl。权限：TENDER_UPLOAD_BUTTON。");

        put(map, "GET", "/api/admin/tenders", "功能：分页查询后台招标列表。\n输入：pageNum、pageSize、keyword、region、businessTypeId。\n输出：PageResult<TenderListItemDto>。\n规则：按 publishAt、id 倒序；pageSize 最大 50。权限：SYSTEM_TENDER。");
        put(map, "GET", "/api/admin/tenders/{tenderId}", "功能：查询后台招标详情。\n输入：tenderId。\n输出：TenderDto，含正文 HTML 和附件列表，每个附件含 thumbnailUrl。\n权限：SYSTEM_TENDER。");
        put(map, "POST", "/api/admin/tenders", "功能：新增招标公告。\n输入：TenderUpsertRequest。\n输出：TenderDto。\n规则：业务类型必须启用；项目编号唯一；报名截止时间和发布时间不能晚于项目截止时间；状态不传默认 PUBLISHED。权限：TENDER_CREATE_BUTTON。");
        put(map, "PUT", "/api/admin/tenders/{tenderId}", "功能：修改招标公告。\n输入：tenderId，TenderUpsertRequest。\n输出：TenderDto。\n规则：attachmentFileIds 不传则保留原附件，传空数组则清空附件。权限：TENDER_EDIT_BUTTON。");
        put(map, "DELETE", "/api/admin/tenders/{tenderId}", "功能：删除招标公告。\n输入：tenderId。\n输出：data=null。\n规则：同时删除附件关系；文件若未被招标和会员资料引用，会清理存储记录和物理对象。权限：TENDER_DELETE_BUTTON。");
        put(map, "POST", "/api/admin/tenders/{tenderId}/attachments", "功能：为已有招标追加附件。\n输入：tenderId，TenderAttachmentBindRequest.fileIds。\n输出：更新后的 TenderDto。\n规则：自动去重并保持既有附件顺序。权限：TENDER_CREATE_BUTTON 或 TENDER_EDIT_BUTTON。");
        put(map, "DELETE", "/api/admin/tenders/{tenderId}/attachments/{attachmentId}", "功能：删除招标上的某个附件关系。\n输入：tenderId、attachmentId。\n输出：更新后的 TenderDto。\n规则：若文件无其他引用，会清理存储记录和物理对象。权限：TENDER_EDIT_BUTTON。");

        put(map, "GET", "/api/admin/roles", "功能：查询角色列表。\n输入：无。\n输出：RoleDto 数组，含已授权菜单。\n权限：SYSTEM_ROLE。");
        put(map, "POST", "/api/admin/roles", "功能：新增角色。\n输入：RoleRequest，含 code、name、description、menuIds。\n输出：RoleDto。\n规则：角色编码唯一；至少分配一个菜单。权限：ROLE_EDIT_BUTTON。");
        put(map, "PUT", "/api/admin/roles/{roleId}", "功能：修改角色。\n输入：roleId，RoleRequest。\n输出：RoleDto。\n规则：内置角色不允许修改 code；至少分配一个菜单。权限：ROLE_EDIT_BUTTON。");
        put(map, "DELETE", "/api/admin/roles/{roleId}", "功能：删除角色。\n输入：roleId。\n输出：data=null。\n规则：内置角色、已被用户使用的角色不能删除。权限：ROLE_EDIT_BUTTON。");

        put(map, "GET", "/api/admin/menus", "功能：查询后台菜单树。\n输入：无。\n输出：MenuDto 树。\n权限：SYSTEM_MENU。");
        put(map, "POST", "/api/admin/menus", "功能：新增菜单。\n输入：MenuRequest。\n输出：MenuDto。\n规则：菜单 code 唯一；parentId 必须存在。权限：MENU_EDIT_BUTTON。");
        put(map, "PUT", "/api/admin/menus/{menuId}", "功能：修改菜单。\n输入：menuId，MenuRequest。\n输出：MenuDto。\n规则：不能选择自己为父级；菜单 code 不能重复。权限：MENU_EDIT_BUTTON。");
        put(map, "DELETE", "/api/admin/menus/{menuId}", "功能：删除菜单。\n输入：menuId。\n输出：data=null。\n规则：存在子菜单或已被角色引用时不能删除。权限：MENU_EDIT_BUTTON。");

        put(map, "GET", "/api/admin/permissions", "功能：权限管理列表接口已停用。\n输入：无。\n输出：固定失败，code=410。\n权限：ADMIN。");
        put(map, "POST", "/api/admin/permissions", "功能：新增权限接口已停用。\n输入：PermissionRequest。\n输出：固定失败，code=410。\n权限：ADMIN。");
        put(map, "PUT", "/api/admin/permissions/{permissionId}", "功能：修改权限接口已停用。\n输入：permissionId，PermissionRequest。\n输出：固定失败，code=410。\n权限：ADMIN。");
        put(map, "DELETE", "/api/admin/permissions/{permissionId}", "功能：删除权限接口已停用。\n输入：permissionId。\n输出：固定失败，code=410。\n权限：ADMIN。");

        put(map, "GET", "/api/admin/operation-logs", "功能：查询后台操作日志。\n输入：无分页参数。\n输出：OperationLogDto 数组，含模块、动作、是否成功、操作人、请求路径、IP、明细、创建时间。\n权限：SYSTEM_OPERATION_LOG。");

        put(map, "GET", "/api/files/{fileId}/thumbnail", "功能：读取文件缩略图或文件类型预览图。\n输入：fileId。\n输出：image/jpeg 等图片流，Content-Type 来自 thumbnailContentType。\n规则：公共可访问；上传接口和附件 DTO 中的 thumbnailUrl 指向此接口，可直接放入 img src。");

        put(map, "POST", "/api/portal/auth/register", "功能：门户会员在线注册入口。当前业务已停用，仅保留兼容路径。\n输入：MemberRegisterRequest。\n输出：固定失败，code=403，提示联系管理员发放账号。\n权限：公开。");
        put(map, "POST", "/api/portal/auth/login", "功能：门户会员登录。\n输入：username、password。\n输出：token、tokenType、expireSeconds、profileCompletionRequired、会员 user。\n规则：会员必须启用、未过期且至少绑定一个启用业务类型；首次登录会写 firstLoginAt 并返回 profileCompletionRequired=true。");
        put(map, "GET", "/api/portal/auth/me", "功能：获取当前门户会员资料。\n输入：会员 token。\n输出：MemberUserDto，含业务类型、有效期、下载权限、营业执照和三年内业绩文件缩略图字段。\n权限：MEMBER。");
        put(map, "POST", "/api/portal/auth/profile/files", "功能：门户会员上传资料文件。\n输入：multipart/form-data，字段名 files。\n输出：FileUploadResponse 数组。\n规则：返回 fileId 后可用于更新 businessLicenseFileId 或 threeYearPerformanceFileId。权限：MEMBER。");
        put(map, "PUT", "/api/portal/auth/profile", "功能：门户会员更新自己的资料。\n输入：MemberProfileUpdateRequest，可更新联系方式、公司信息、实名、营业执照文件 ID、三年内业绩文件 ID。\n输出：更新后的 MemberUserDto。\n规则：资料字段不传表示保持原值；传空字符串的必填文本会被拒绝；唯一字段不能与其他会员重复。权限：MEMBER。");

        put(map, "GET", "/api/portal/tenders/latest", "功能：查询门户首页最新公开招标。\n输入：无。\n输出：最新 3 条 TenderListItemDto。\n规则：只返回 PUBLISHED 且 publishAt 不晚于当前时间的招标；不按会员业务类型过滤。权限：公开。");
        put(map, "GET", "/api/portal/tenders", "功能：会员分页查询可见招标列表。\n输入：pageNum、pageSize、keyword、region，会员 token。\n输出：PageResult<TenderListItemDto>。\n规则：只返回 PUBLISHED、已到发布时间、业务类型属于当前会员的招标。权限：MEMBER。");
        put(map, "GET", "/api/portal/tenders/{tenderId}", "功能：查询门户招标详情。\n输入：tenderId；可选会员 token。\n输出：TenderDetailDto，含正文、附件列表和 canDownload。\n规则：公开可读；只要求招标已发布且已到发布时间；未登录或无业务类型/下载权限时 canDownload=false。权限：公开。");
        put(map, "GET", "/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", "功能：下载门户招标附件。\n输入：tenderId、attachmentId、会员 token。\n输出：文件流，Content-Disposition 包含原始文件名。\n规则：会员必须能访问该招标业务类型、账号允许下载、附件属于该招标；失败返回 ApiResponse JSON。权限：MEMBER。");

        return map;
    }

    private void put(Map<String, String> map, String method, String path, String description) {
        map.put(key(method, path), description);
    }

    private void add(Set<String> keys, String method, String path) {
        keys.add(key(method, path));
    }

    private String key(String method, String path) {
        return method + " " + path;
    }
}
