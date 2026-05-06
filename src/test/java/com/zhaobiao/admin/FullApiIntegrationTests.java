package com.zhaobiao.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhaobiao.admin.config.DataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DataInitializer.class)
class FullApiIntegrationTests {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void allAdminAndPublicApiEndpointsCanHandleExpectedAuthCrudAndValidationCases() throws Exception {
        String tag = uniqueTag();
        String superAdminToken = loginAdmin("admin", "adminqwert");

        mockMvc.perform(get("/api/admin/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"legacy" + tag + "\",\"phone\":\"139" + phoneSeed(tag) + "\",\"email\":\"legacy" + tag + "@test.com\",\"companyName\":\"测试企业\",\"contactPerson\":\"张三\",\"unifiedSocialCreditCode\":\"91310000MA" + creditSeed(tag) + "\",\"realName\":\"张三\",\"password\":\"123456\",\"confirmPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/portal/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"portal" + tag + "\",\"phone\":\"138" + phoneSeed(tag) + "\",\"email\":\"portal" + tag + "@test.com\",\"companyName\":\"会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MB" + creditSeed(tag) + "\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearer(superAdminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("admin"));

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", bearer(superAdminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", bearer(superAdminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"超级管理员\",\"companyName\":\"平台运营中心\",\"contactPerson\":\"超级管理员\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        exercisePermissionMenuAndRoleApis(superAdminToken, tag);
        exerciseBusinessTypeApis(superAdminToken, tag);
        exerciseAdminAccountApis(superAdminToken, tag);
        exerciseMemberAdminApis(superAdminToken, tag);
        exerciseTenderAndFileApis(superAdminToken, tag);

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", bearer(superAdminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(410));

        mockMvc.perform(put("/api/admin/users/{userId}/audit", 1L)
                        .header("Authorization", bearer(superAdminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(410));

        mockMvc.perform(put("/api/admin/users/{userId}/roles", 1L)
                        .header("Authorization", bearer(superAdminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(410));

        mockMvc.perform(get("/api/admin/users/{userId}/audit-records", 1L)
                        .header("Authorization", bearer(superAdminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(410));

        mockMvc.perform(get("/api/admin/operation-logs")
                        .header("Authorization", bearer(superAdminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void memberTokenIsBlockedAfterExpirationAndWorksAgainAfterAdminExtendsExpiration() throws Exception {
        String tag = uniqueTag();
        String adminToken = loginAdmin("admin", "adminqwert");
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);

        Long fileId = uploadFile(adminToken, "expire-flow-" + tag + ".txt", "expiration download content " + tag);
        Long tenderId = createTender(
                adminToken,
                "过期续期测试项目-" + tag,
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(2),
                "EXP-" + tag,
                fileId,
                "PUBLISHED");
        Long attachmentId = findAttachmentIdByFileId(adminToken, tenderId, fileId);
        assertNotNull(attachmentId);

        String username = "expiremember" + tag;
        String password = "123456";
        Long memberId = createMember(adminToken, username, tag, engineeringTypeId, true, LocalDateTime.now().plusDays(7), password);
        String memberToken = loginMember(username, password);

        assertMemberPortalOperationsWork(memberToken, tenderId, attachmentId, "expiration download content " + tag);

        updateMemberExpiration(adminToken, memberId, tag, engineeringTypeId, LocalDateTime.now().minusMinutes(1), "expired");

        assertMemberPortalOperationsAreUnauthorized(memberToken, tenderId, attachmentId);
        mockMvc.perform(post("/api/portal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        updateMemberExpiration(adminToken, memberId, tag, engineeringTypeId, LocalDateTime.now().plusDays(30), "renewed");

        assertMemberPortalOperationsWork(memberToken, tenderId, attachmentId, "expiration download content " + tag);
        String renewedToken = loginMember(username, password);
        assertMemberPortalOperationsWork(renewedToken, tenderId, attachmentId, "expiration download content " + tag);
    }

    private void exercisePermissionMenuAndRoleApis(String adminToken, String tag) throws Exception {
        mockMvc.perform(get("/api/admin/permissions")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(410));
        mockMvc.perform(post("/api/admin/permissions")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"qa:perm:" + tag + "\",\"name\":\"接口测试权限\",\"description\":\"create\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(410));

        String menuCode = "QA_MENU_" + tag;
        MvcResult menuResult = mockMvc.perform(post("/api/admin/menus")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + menuCode + "\",\"name\":\"接口测试菜单\",\"type\":\"MENU\",\"routePath\":\"/qa-" + tag + "\",\"component\":\"qa/index\",\"icon\":\"Menu\",\"sortOrder\":999,\"visible\":true,\"enabled\":true,\"description\":\"create\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        Long menuId = dataId(menuResult);

        mockMvc.perform(put("/api/admin/menus/{menuId}", menuId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + menuCode + "\",\"name\":\"接口测试菜单更新\",\"type\":\"MENU\",\"routePath\":\"/qa-updated-" + tag + "\",\"component\":\"qa/updated\",\"icon\":\"Menu\",\"sortOrder\":998,\"visible\":true,\"enabled\":true,\"description\":\"update\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("接口测试菜单更新"));

        mockMvc.perform(get("/api/admin/menus")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String roleCode = "QA_ROLE_" + tag;
        MvcResult roleResult = mockMvc.perform(post("/api/admin/roles")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + roleCode + "\",\"name\":\"接口测试角色\",\"description\":\"create\",\"permissionIds\":[-1],\"menuIds\":[" + menuId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.menuCodes", hasItem(menuCode)))
                .andExpect(jsonPath("$.data.permissionCodes").isEmpty())
                .andReturn();
        Long roleId = dataId(roleResult);

        mockMvc.perform(put("/api/admin/roles/{roleId}", roleId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + roleCode + "\",\"name\":\"接口测试角色更新\",\"description\":\"update\",\"permissionIds\":[-1],\"menuIds\":[" + menuId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("接口测试角色更新"));

        mockMvc.perform(get("/api/admin/roles")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/admin/roles/{roleId}", roleId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/admin/menus/{menuId}", menuId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private void exerciseBusinessTypeApis(String adminToken, String tag) throws Exception {
        String code = "QA_TYPE_" + tag;
        MvcResult createResult = mockMvc.perform(post("/api/admin/business-types")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"name\":\"接口测试类型" + tag + "\",\"enabled\":true,\"sortOrder\":888,\"description\":\"create\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.code").value(code))
                .andReturn();
        Long businessTypeId = dataId(createResult);

        mockMvc.perform(get("/api/admin/business-types")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/admin/business-types/options")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/api/admin/business-types/{businessTypeId}", businessTypeId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"name\":\"接口测试类型更新" + tag + "\",\"enabled\":true,\"sortOrder\":889,\"description\":\"update\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("接口测试类型更新" + tag));

        mockMvc.perform(put("/api/admin/business-types/{businessTypeId}/status", businessTypeId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(delete("/api/admin/business-types/{businessTypeId}", businessTypeId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private void exerciseAdminAccountApis(String adminToken, String tag) throws Exception {
        Long systemAdminRoleId = findRoleIdByCode(adminToken, "SYSTEM_ADMIN");
        Long auditorRoleId = findRoleIdByCode(adminToken, "USER_AUDITOR");
        assertNotNull(systemAdminRoleId);
        assertNotNull(auditorRoleId);

        String username = "apiadmin" + tag;
        MvcResult createResult = mockMvc.perform(post("/api/admin/admin-users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"139" + phoneSeed(tag) + "\",\"email\":\"" + username + "@zhaobiao.com\",\"realName\":\"接口测试管理员\",\"password\":\"12345678\",\"confirmPassword\":\"12345678\",\"roleIds\":[" + systemAdminRoleId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username))
                .andReturn();
        Long adminUserId = dataId(createResult);

        String systemAdminToken = loginAdmin(username, "12345678");
        mockMvc.perform(get("/api/admin/admin-users")
                        .header("Authorization", bearer(systemAdminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/admin/admin-users")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/api/admin/admin-users/{userId}", adminUserId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"137" + phoneSeed(tag) + "\",\"email\":\"updated-" + username + "@zhaobiao.com\",\"realName\":\"接口测试管理员更新\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.realName").value("接口测试管理员更新"));

        mockMvc.perform(put("/api/admin/admin-users/{userId}/roles", adminUserId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[" + auditorRoleId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("USER_AUDITOR")));

        mockMvc.perform(put("/api/admin/admin-users/{userId}/password", adminUserId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"87654321\",\"confirmPassword\":\"87654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        loginAdmin(username, "87654321");

        mockMvc.perform(put("/api/admin/admin-users/{userId}/status", adminUserId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"87654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    private void exerciseMemberAdminApis(String adminToken, String tag) throws Exception {
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        Long goodsTypeId = findBusinessTypeIdByCode(adminToken, "GOODS");
        assertNotNull(engineeringTypeId);
        assertNotNull(goodsTypeId);

        String username = "apimember" + tag;
        Long memberId = createMember(adminToken, username, tag, engineeringTypeId, false, LocalDateTime.now().plusMonths(1), "123456");

        mockMvc.perform(get("/api/admin/members")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/admin/members/{memberId}", memberId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.expired").value(false));

        mockMvc.perform(put("/api/admin/members/{memberId}", memberId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberUpdateBody(tag, engineeringTypeId, goodsTypeId, LocalDateTime.now().plusMonths(2), "managed")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.businessTypes", hasSize(2)));

        mockMvc.perform(put("/api/admin/members/{memberId}/download-access", memberId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canDownloadFile\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownloadFile").value(true));

        mockMvc.perform(put("/api/admin/members/{memberId}/password", memberId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"654321\",\"confirmPassword\":\"654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        loginMember(username, "654321");

        mockMvc.perform(put("/api/admin/members/{memberId}/status", memberId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        mockMvc.perform(post("/api/portal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    private void exerciseTenderAndFileApis(String adminToken, String tag) throws Exception {
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);

        Long firstFileId = uploadFile(adminToken, "full-api-" + tag + "-1.txt", "first file " + tag);
        Long secondFileId = uploadFile(adminToken, "full-api-" + tag + "-2.txt", "second file " + tag);

        Long tenderId = createTender(
                adminToken,
                "全量接口测试项目-" + tag,
                "杭州",
                engineeringTypeId,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(3),
                "FULL-" + tag,
                firstFileId,
                "PUBLISHED");

        mockMvc.perform(get("/api/admin/tenders")
                        .header("Authorization", bearer(adminToken))
                        .param("keyword", "全量接口测试项目-" + tag)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("全量接口测试项目-" + tag))
                .andExpect(jsonPath("$.data.attachments", hasSize(1)));

        mockMvc.perform(post("/api/admin/tenders/{tenderId}/attachments", tenderId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileIds\":[" + secondFileId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.attachments", hasSize(2)));

        Long secondAttachmentId = findAttachmentIdByFileId(adminToken, tenderId, secondFileId);
        assertNotNull(secondAttachmentId);
        mockMvc.perform(delete("/api/admin/tenders/{tenderId}/attachments/{attachmentId}", tenderId, secondAttachmentId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.attachments", hasSize(1)));

        mockMvc.perform(put("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tenderBody(
                                "全量接口测试项目更新-" + tag,
                                "宁波",
                                engineeringTypeId,
                                LocalDateTime.now().minusHours(1),
                                LocalDateTime.now().plusDays(11),
                                LocalDateTime.now().plusDays(4),
                                "FULL-UPDATED-" + tag,
                                firstFileId,
                                "PUBLISHED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("全量接口测试项目更新-" + tag));

        mockMvc.perform(delete("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    private void assertMemberPortalOperationsWork(String memberToken,
                                                  Long tenderId,
                                                  Long attachmentId,
                                                  String expectedContent) throws Exception {
        mockMvc.perform(get("/api/portal/auth/me")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.expired").value(false));

        mockMvc.perform(get("/api/portal/tenders")
                        .header("Authorization", bearer(memberToken))
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", tenderId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownload").value(true));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().string(expectedContent));
    }

    private void assertMemberPortalOperationsAreUnauthorized(String memberToken,
                                                            Long tenderId,
                                                            Long attachmentId) throws Exception {
        mockMvc.perform(get("/api/portal/auth/me")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/portal/tenders")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", tenderId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownload").value(false));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    private String loginAdmin(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    private String loginMember(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/portal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    private Long createMember(String adminToken,
                              String username,
                              String tag,
                              Long businessTypeId,
                              boolean canDownloadFile,
                              LocalDateTime expiresAt,
                              String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/members")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"138" + phoneSeed(tag) + "\",\"email\":\"" + username + "@test.com\",\"companyName\":\"测试会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MC" + creditSeed(tag) + "\",\"realName\":\"李四\",\"password\":\"" + password + "\",\"confirmPassword\":\"" + password + "\",\"businessTypeIds\":[" + businessTypeId + "],\"canDownloadFile\":" + canDownloadFile + ",\"status\":\"ENABLED\",\"expiresAt\":\"" + formatDateTime(expiresAt) + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return dataId(result);
    }

    private void updateMemberExpiration(String adminToken,
                                        Long memberId,
                                        String tag,
                                        Long businessTypeId,
                                        LocalDateTime expiresAt,
                                        String phase) throws Exception {
        mockMvc.perform(put("/api/admin/members/{memberId}", memberId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberUpdateBody(tag, businessTypeId, null, expiresAt, phase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private String memberUpdateBody(String tag,
                                    Long primaryBusinessTypeId,
                                    Long secondBusinessTypeId,
                                    LocalDateTime expiresAt,
                                    String phase) {
        String businessTypeIds = secondBusinessTypeId == null
                ? String.valueOf(primaryBusinessTypeId)
                : primaryBusinessTypeId + "," + secondBusinessTypeId;
        return "{\"phone\":\"136" + phoneSeed(tag) + "\",\"email\":\"" + phase + "-member" + tag + "@test.com\",\"companyName\":\"测试会员企业" + phase + "\",\"contactPerson\":\"王五\",\"unifiedSocialCreditCode\":\"91310000MD" + creditSeed(tag) + "\",\"realName\":\"王五\",\"businessTypeIds\":[" + businessTypeIds + "],\"expiresAt\":\"" + formatDateTime(expiresAt) + "\"}";
    }

    private Long uploadFile(String adminToken, String fileName, String content) throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "files",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes(StandardCharsets.UTF_8)
        );
        MvcResult result = mockMvc.perform(multipart("/api/admin/files/upload")
                        .file(multipartFile)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path(0)
                .path("fileId")
                .asLong();
    }

    private Long createTender(String adminToken,
                              String title,
                              String region,
                              Long businessTypeId,
                              LocalDateTime publishAt,
                              LocalDateTime deadline,
                              LocalDateTime signupDeadline,
                              String projectCode,
                              Long attachmentFileId,
                              String statusValue) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/tenders")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tenderBody(title, region, businessTypeId, publishAt, deadline, signupDeadline, projectCode, attachmentFileId, statusValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return dataId(result);
    }

    private String tenderBody(String title,
                              String region,
                              Long businessTypeId,
                              LocalDateTime publishAt,
                              LocalDateTime deadline,
                              LocalDateTime signupDeadline,
                              String projectCode,
                              Long attachmentFileId,
                              String statusValue) {
        return "{"
                + "\"title\":\"" + title + "\","
                + "\"region\":\"" + region + "\","
                + "\"businessTypeId\":" + businessTypeId + ","
                + "\"publishAt\":\"" + formatDateTime(publishAt) + "\","
                + "\"content\":\"<p>这是 " + title + " 的正文</p>\","
                + "\"contactPerson\":\"张三\","
                + "\"budget\":\"100 万元\","
                + "\"contactPhone\":\"0571-88886666\","
                + "\"tenderUnit\":\"测试招标单位\","
                + "\"deadline\":\"" + formatDateTime(deadline) + "\","
                + "\"projectCode\":\"" + projectCode + "\","
                + "\"signupDeadline\":\"" + formatDateTime(signupDeadline) + "\","
                + "\"status\":\"" + statusValue + "\","
                + "\"attachmentFileIds\":[" + attachmentFileId + "]"
                + "}";
    }

    private Long findAttachmentIdByFileId(String adminToken, Long tenderId, Long fileId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode attachments = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("attachments");
        for (JsonNode attachment : attachments) {
            if (fileId.equals(attachment.path("fileId").asLong())) {
                return attachment.path("attachmentId").asLong();
            }
        }
        return null;
    }

    private Long findRoleIdByCode(String adminToken, String code) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/roles")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return findIdByCode(objectMapper.readTree(result.getResponse().getContentAsString()).path("data"), code);
    }

    private Long findBusinessTypeIdByCode(String adminToken, String code) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/business-types/options")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return findIdByCode(objectMapper.readTree(result.getResponse().getContentAsString()).path("data"), code);
    }

    private Long findIdByCode(JsonNode nodes, String code) {
        if (nodes == null || !nodes.isArray()) {
            return null;
        }
        for (JsonNode node : nodes) {
            if (code.equals(node.path("code").asText())) {
                return node.path("id").asLong();
            }
            Long childId = findIdByCode(node.path("children"), code);
            if (childId != null) {
                return childId;
            }
        }
        return null;
    }

    private Long dataId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private String uniqueTag() {
        return String.valueOf(System.nanoTime());
    }

    private String phoneSeed(String tag) {
        return rightDigits(tag, 8);
    }

    private String creditSeed(String tag) {
        return rightDigits(tag, 8);
    }

    private String rightDigits(String value, int length) {
        String digits = Arrays.stream(value.split(""))
                .filter(item -> item.charAt(0) >= '0' && item.charAt(0) <= '9')
                .collect(Collectors.joining());
        if (digits.length() >= length) {
            return digits.substring(digits.length() - length);
        }
        StringBuilder builder = new StringBuilder(digits);
        while (builder.length() < length) {
            builder.insert(0, '0');
        }
        return builder.toString();
    }
}
