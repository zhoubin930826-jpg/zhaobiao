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
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class ControllerBoundaryIntegrationTests {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticationSecurityAndMalformedParametersReturnExpectedErrors() throws Exception {
        String tag = uniqueTag();
        String adminToken = loginAdmin("admin", "adminqwert");
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);
        Long memberId = createMember(adminToken, "boundm" + tag, tag, engineeringTypeId, false, LocalDateTime.now().plusDays(30), "123456");
        assertNotNull(memberId);
        String memberToken = loginMember("boundm" + tag, "123456");

        assertBusinessCode(postJson("/api/auth/login", "{\"username\":\"\"}"), 400);
        assertBusinessCode(postJson("/api/portal/auth/login", "{\"username\":\"portal\",\"password\":\"\"}"), 400);
        assertBusinessCode(postJson("/api/auth/login", "{\"username\":"), 400);

        mockMvc.perform(get("/api/admin/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        assertBusinessCode(mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(memberToken))), 403);
        assertBusinessCode(mockMvc.perform(get("/api/portal/auth/me").header("Authorization", bearer(adminToken))), 403);
        assertBusinessCode(mockMvc.perform(get("/api/admin/tenders")
                .header("Authorization", bearer(adminToken))
                .param("pageNum", "abc")), 400);
        assertBusinessCode(mockMvc.perform(get("/api/admin/tenders/{tenderId}", "not-a-number")
                .header("Authorization", bearer(adminToken))), 400);
        assertBusinessCode(mockMvc.perform(multipart("/api/admin/files/upload")
                .header("Authorization", bearer(adminToken))), 400);
    }

    @Test
    void disabledPermissionAndLegacyUserControllersAlwaysReturnGoneBusinessCode() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");

        assertBusinessCode(mockMvc.perform(get("/api/admin/permissions").header("Authorization", bearer(adminToken))), 410);
        assertBusinessCode(mockMvc.perform(post("/api/admin/permissions")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"disabled:permission\",\"name\":\"已停用权限\"}")), 410);
        assertBusinessCode(mockMvc.perform(put("/api/admin/permissions/{permissionId}", 1L)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"disabled:permission\",\"name\":\"已停用权限\"}")), 410);
        assertBusinessCode(mockMvc.perform(delete("/api/admin/permissions/{permissionId}", 1L)
                .header("Authorization", bearer(adminToken))), 410);

        assertBusinessCode(mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(adminToken))), 410);
        assertBusinessCode(mockMvc.perform(put("/api/admin/users/{userId}/audit", 1L)
                .header("Authorization", bearer(adminToken))), 410);
        assertBusinessCode(mockMvc.perform(put("/api/admin/users/{userId}/roles", 1L)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleIds\":[1]}")), 410);
        assertBusinessCode(mockMvc.perform(get("/api/admin/users/{userId}/audit-records", 1L)
                .header("Authorization", bearer(adminToken))), 410);
    }

    @Test
    void adminAccountControllerRejectsInvalidRolesDuplicatesAndInitialAdminMutation() throws Exception {
        String tag = uniqueTag();
        String adminToken = loginAdmin("admin", "adminqwert");
        Long systemAdminRoleId = findRoleIdByCode(adminToken, "SYSTEM_ADMIN");
        Long normalUserRoleId = findRoleIdByCode(adminToken, "NORMAL_USER");
        Long superAdminRoleId = findRoleIdByCode(adminToken, "SUPER_ADMIN");
        assertNotNull(systemAdminRoleId);
        assertNotNull(normalUserRoleId);
        assertNotNull(superAdminRoleId);

        assertBusinessCode(postAdminJson(adminToken, "/api/admin/admin-users", "{}"), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/admin-users",
                adminCreateBody("badadmin" + tag, "139" + phoneSeed(tag), "badadmin" + tag + "@zhaobiao.com", "12345678", "87654321", systemAdminRoleId)), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/admin-users",
                adminCreateBody("normalrole" + tag, "138" + phoneSeed(tag), "normalrole" + tag + "@zhaobiao.com", "12345678", "12345678", normalUserRoleId)), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/admin-users",
                adminCreateBodyWithRawRoles("nullrole" + tag, "137" + phoneSeed(tag), "nullrole" + tag + "@zhaobiao.com", "12345678", "12345678", "[" + systemAdminRoleId + ",null]")), 400);

        String username = "adminb" + tag;
        Long adminUserId = createAdmin(adminToken, username, tag, systemAdminRoleId);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/admin-users",
                adminCreateBody(username, "136" + phoneSeed(tag), "duplicate-" + username + "@zhaobiao.com", "12345678", "12345678", systemAdminRoleId)), 400);

        assertBusinessCode(mockMvc.perform(put("/api/admin/admin-users/{userId}", 99999999L)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminUpdateBody("135" + phoneSeed(tag), "missing-" + username + "@zhaobiao.com"))), 404);
        assertBusinessCode(mockMvc.perform(put("/api/admin/admin-users/{userId}", adminUserId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminUpdateBody("not-phone", "not-email"))), 400);

        Long initialAdminId = currentAdminId(adminToken);
        assertBusinessCode(mockMvc.perform(put("/api/admin/admin-users/{userId}/status", initialAdminId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DISABLED\"}")), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/admin-users/{userId}/roles", initialAdminId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleIds\":[" + systemAdminRoleId + "]}")), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/admin-users/{userId}/roles", adminUserId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleIds\":[]}")), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/admin-users/{userId}/password", adminUserId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"12345678\",\"confirmPassword\":\"87654321\"}")), 400);
    }

    @Test
    void menuAndRoleControllersRejectInvalidTreesAndMenuIdLists() throws Exception {
        String tag = uniqueTag();
        String adminToken = loginAdmin("admin", "adminqwert");
        Long dashboardMenuId = findMenuIdByCode(adminToken, "DASHBOARD");
        Long superAdminRoleId = findRoleIdByCode(adminToken, "SUPER_ADMIN");
        assertNotNull(dashboardMenuId);
        assertNotNull(superAdminRoleId);

        assertBusinessCode(postAdminJson(adminToken, "/api/admin/menus", "{}"), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/menus",
                menuBody("BAD_PARENT_" + tag, "坏父级菜单", "MENU", 99999999L, "/bad-" + tag, "bad/index", true, true, "LEGACY_PERMISSION")), 400);

        MvcResult parentResult = postAdminJson(adminToken, "/api/admin/menus",
                menuBody("QA_PARENT_" + tag, "边界父级", "DIRECTORY", null, "/qa-parent-" + tag, "", true, true, "ignored:code"))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        Long parentId = dataId(parentResult);
        JsonNode parent = objectMapper.readTree(parentResult.getResponse().getContentAsString()).path("data");
        assertTrue(parent.path("permissionCode").isMissingNode() || parent.path("permissionCode").isNull());

        Long childId = dataId(postAdminJson(adminToken, "/api/admin/menus",
                menuBody("QA_CHILD_" + tag, "边界子菜单", "MENU", parentId, "/qa-child-" + tag, "qa/child", true, true, null))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn());
        assertBusinessCode(mockMvc.perform(delete("/api/admin/menus/{menuId}", parentId)
                .header("Authorization", bearer(adminToken))), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/menus/{menuId}", childId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(menuBody("QA_CHILD_" + tag, "自父级菜单", "MENU", childId, "/qa-child-" + tag, "qa/child", true, true, null))), 400);

        assertBusinessCode(postAdminJson(adminToken, "/api/admin/roles", "{}"), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/roles",
                roleBody("QA_ROLE_EMPTY_" + tag, "空菜单角色", "[]")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/roles",
                roleBody("QA_ROLE_NULL_" + tag, "空ID菜单角色", "[" + dashboardMenuId + ",null]")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/roles",
                roleBody("QA_ROLE_MISSING_" + tag, "缺失菜单角色", "[" + dashboardMenuId + ",99999999]")), 400);

        Long roleId = dataId(postAdminJson(adminToken, "/api/admin/roles",
                roleBody("QA_ROLE_" + tag, "边界角色", "[" + dashboardMenuId + "]"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.menuCodes", hasItem("DASHBOARD")))
                .andReturn());
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/roles",
                roleBody("QA_ROLE_" + tag, "重复角色", "[" + dashboardMenuId + "]")), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/roles/{roleId}", superAdminRoleId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleBody("SUPER_ADMIN_RENAMED_" + tag, "超级管理员改码", "[" + dashboardMenuId + "]"))), 400);
        assertBusinessCode(mockMvc.perform(delete("/api/admin/roles/{roleId}", superAdminRoleId)
                .header("Authorization", bearer(adminToken))), 400);

        assertBusinessCode(mockMvc.perform(delete("/api/admin/roles/{roleId}", roleId)
                .header("Authorization", bearer(adminToken))), 0);
        assertBusinessCode(mockMvc.perform(delete("/api/admin/menus/{menuId}", childId)
                .header("Authorization", bearer(adminToken))), 0);
        assertBusinessCode(mockMvc.perform(delete("/api/admin/menus/{menuId}", parentId)
                .header("Authorization", bearer(adminToken))), 0);
    }

    @Test
    void businessTypeControllerRejectsInvalidDuplicatesDisabledAndReferencedDeletes() throws Exception {
        String tag = uniqueTag();
        String adminToken = loginAdmin("admin", "adminqwert");

        assertBusinessCode(postAdminJson(adminToken, "/api/admin/business-types", "{}"), 400);
        Long normalizedTypeId = dataId(postAdminJson(adminToken, "/api/admin/business-types",
                businessTypeBody("qa_lower_" + tag, "边界类型" + tag, true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.code").value(("qa_lower_" + tag).toUpperCase()))
                .andReturn());
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/business-types",
                businessTypeBody("QA_LOWER_" + tag, "另一个类型" + tag, true)), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/business-types",
                businessTypeBody("QA_DUP_NAME_" + tag, "边界类型" + tag, true)), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/business-types/{businessTypeId}/status", normalizedTypeId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\":null}")), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/business-types/{businessTypeId}", 99999999L)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(businessTypeBody("QA_MISSING_" + tag, "不存在类型", true))), 404);

        Long disabledTypeId = dataId(postAdminJson(adminToken, "/api/admin/business-types",
                businessTypeBody("QA_DISABLED_" + tag, "禁用类型" + tag, false))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn());
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/members",
                memberCreateBody("disabledm" + tag, tag, disabledTypeId, false, LocalDateTime.now().plusDays(30), "123456")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/tenders",
                tenderBody("禁用类型招标-" + tag, disabledTypeId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(2), "DISABLED-" + tag, null, "PUBLISHED")), 400);

        Long referencedTypeId = dataId(postAdminJson(adminToken, "/api/admin/business-types",
                businessTypeBody("QA_REF_" + tag, "引用类型" + tag, true))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn());
        createMember(adminToken, "refm" + tag, tag, referencedTypeId, false, LocalDateTime.now().plusDays(30), "123456");
        assertBusinessCode(mockMvc.perform(delete("/api/admin/business-types/{businessTypeId}", referencedTypeId)
                .header("Authorization", bearer(adminToken))), 400);
        assertBusinessCode(mockMvc.perform(delete("/api/admin/business-types/{businessTypeId}", normalizedTypeId)
                .header("Authorization", bearer(adminToken))), 0);
        assertBusinessCode(mockMvc.perform(delete("/api/admin/business-types/{businessTypeId}", disabledTypeId)
                .header("Authorization", bearer(adminToken))), 0);
    }

    @Test
    void memberControllerRejectsInvalidFieldsDuplicatesAndStatusUpdates() throws Exception {
        String tag = uniqueTag();
        String adminToken = loginAdmin("admin", "adminqwert");
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);
        Long disabledTypeId = dataId(postAdminJson(adminToken, "/api/admin/business-types",
                businessTypeBody("QA_MEMBER_DISABLED_" + tag, "会员禁用类型" + tag, false))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn());

        assertBusinessCode(postAdminJson(adminToken, "/api/admin/members", "{}"), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/members",
                memberCreateBody("mismatchm" + tag, tag, engineeringTypeId, false, LocalDateTime.now().plusDays(30), "123456").replace("\"confirmPassword\":\"123456\"", "\"confirmPassword\":\"654321\"")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/members",
                memberCreateBody("invalidm" + tag, tag, engineeringTypeId, false, LocalDateTime.now().plusDays(30), "123456").replace("\"phone\":\"138" + phoneSeed(tag) + "\"", "\"phone\":\"not-phone\"")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/members",
                memberCreateBodyWithRawTypes("nulltypem" + tag, tag, "[null]", false, LocalDateTime.now().plusDays(30), "123456")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/members",
                memberCreateBody("disabledtypem" + tag, tag, disabledTypeId, false, LocalDateTime.now().plusDays(30), "123456")), 400);

        String username = "memberb" + tag;
        Long memberId = createMember(adminToken, username, tag, engineeringTypeId, false, LocalDateTime.now().plusDays(30), "123456");
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/members",
                memberCreateBody(username, tag, engineeringTypeId, false, LocalDateTime.now().plusDays(30), "123456")), 400);

        assertBusinessCode(mockMvc.perform(get("/api/admin/members/{memberId}", 99999999L)
                .header("Authorization", bearer(adminToken))), 404);
        assertBusinessCode(mockMvc.perform(put("/api/admin/members/{memberId}", memberId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(memberUpdateBody("bad-phone", "bad-email", "91310000MB" + creditSeed(tag), engineeringTypeId, LocalDateTime.now().plusDays(30)))), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/members/{memberId}/download-access", memberId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"canDownloadFile\":null}")), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/members/{memberId}/status", memberId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":null}")), 400);
        assertBusinessCode(mockMvc.perform(put("/api/admin/members/{memberId}/password", memberId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"123456\",\"confirmPassword\":\"654321\"}")), 400);
    }

    @Test
    void tenderFileAndPortalControllersCoverValidationVisibilityAndDownloadBoundaries() throws Exception {
        String tag = uniqueTag();
        String adminToken = loginAdmin("admin", "adminqwert");
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        Long goodsTypeId = findBusinessTypeIdByCode(adminToken, "GOODS");
        assertNotNull(engineeringTypeId);
        assertNotNull(goodsTypeId);

        MockMultipartFile emptyFile = new MockMultipartFile("files", "empty-" + tag + ".txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]);
        assertBusinessCode(mockMvc.perform(multipart("/api/admin/files/upload")
                .file(emptyFile)
                .header("Authorization", bearer(adminToken))), 400);

        Long firstFileId = uploadFile(adminToken, "boundary-" + tag + "-1.txt", "file-one-" + tag);
        Long duplicateFileId = uploadFile(adminToken, "boundary-" + tag + "-duplicate.txt", "file-one-" + tag);
        assertEquals(firstFileId, duplicateFileId);
        Long secondFileId = uploadFile(adminToken, "boundary-" + tag + "-2.txt", "file-two-" + tag);

        assertBusinessCode(postAdminJson(adminToken, "/api/admin/tenders", "{}"), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/tenders",
                tenderBody("时间错误招标-" + tag, engineeringTypeId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), "DATE-BAD-" + tag, firstFileId, "PUBLISHED")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/tenders",
                tenderBody("发布时间错误招标-" + tag, engineeringTypeId, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1), "PUBLISH-BAD-" + tag, firstFileId, "PUBLISHED")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/tenders",
                tenderBody("不存在类型招标-" + tag, 99999999L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(2), "TYPE-BAD-" + tag, firstFileId, "PUBLISHED")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/tenders",
                tenderBody("附件不存在招标-" + tag, engineeringTypeId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(2), "FILE-BAD-" + tag, 99999999L, "PUBLISHED")), 400);
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/tenders",
                tenderBody("附件为空招标-" + tag, engineeringTypeId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(2), "FILE-NULL-" + tag, null, "PUBLISHED").replace("\"attachmentFileIds\":[]", "\"attachmentFileIds\":[null]")), 400);

        Long visibleTenderId = createTender(adminToken, "PortalVisible-" + tag, engineeringTypeId,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(3),
                "VISIBLE-" + tag, firstFileId, "PUBLISHED");
        assertBusinessCode(postAdminJson(adminToken, "/api/admin/tenders",
                tenderBody("重复编号招标-" + tag, engineeringTypeId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(2), "VISIBLE-" + tag, secondFileId, "PUBLISHED")), 400);

        assertBusinessCode(mockMvc.perform(post("/api/admin/tenders/{tenderId}/attachments", visibleTenderId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileIds\":[]}")), 400);
        assertBusinessCode(mockMvc.perform(post("/api/admin/tenders/{tenderId}/attachments", visibleTenderId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileIds\":[null]}")), 400);
        assertBusinessCode(mockMvc.perform(post("/api/admin/tenders/{tenderId}/attachments", visibleTenderId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileIds\":[99999999]}")), 400);
        assertBusinessCode(mockMvc.perform(post("/api/admin/tenders/{tenderId}/attachments", visibleTenderId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileIds\":[" + secondFileId + "]}")), 0);
        assertBusinessCode(mockMvc.perform(delete("/api/admin/tenders/{tenderId}/attachments/{attachmentId}", visibleTenderId, 99999999L)
                .header("Authorization", bearer(adminToken))), 404);

        Long attachmentId = findAttachmentIdByFileId(adminToken, visibleTenderId, firstFileId);
        assertNotNull(attachmentId);
        createTender(adminToken, "PortalFuture-" + tag, engineeringTypeId,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(3),
                "FUTURE-" + tag, null, "PUBLISHED");
        createTender(adminToken, "PortalClosed-" + tag, engineeringTypeId,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(3),
                "CLOSED-" + tag, null, "CLOSED");
        createTender(adminToken, "PortalOtherType-" + tag, goodsTypeId,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(3),
                "OTHER-" + tag, null, "PUBLISHED");

        Long memberId = createMember(adminToken, "portalm" + tag, tag, engineeringTypeId, false, LocalDateTime.now().plusDays(30), "123456");
        String memberToken = loginMember("portalm" + tag, "123456");
        mockMvc.perform(get("/api/admin/tenders")
                        .header("Authorization", bearer(adminToken))
                        .param("keyword", "PortalVisible-" + tag)
                        .param("pageNum", "0")
                        .param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(50))
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(1)));
        expectPortalTotal(memberToken, "PortalVisible-" + tag, 1);
        expectPortalTotal(memberToken, "PortalFuture-" + tag, 0);
        expectPortalTotal(memberToken, "PortalClosed-" + tag, 0);
        expectPortalTotal(memberToken, "PortalOtherType-" + tag, 0);

        assertBusinessCode(mockMvc.perform(get("/api/portal/tenders/{tenderId}", visibleTenderId)
                .header("Authorization", bearer(memberToken))), 0);
        assertBusinessCode(mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", visibleTenderId, attachmentId)
                .header("Authorization", bearer(memberToken))), 403);

        assertBusinessCode(mockMvc.perform(put("/api/admin/members/{memberId}/download-access", memberId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"canDownloadFile\":true}")), 0);
        String downloadToken = loginMember("portalm" + tag, "123456");
        assertBusinessCode(mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", visibleTenderId, 99999999L)
                .header("Authorization", bearer(downloadToken))), 404);
        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", visibleTenderId, attachmentId)
                        .header("Authorization", bearer(downloadToken)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().string("file-one-" + tag));
    }

    @Test
    void profileAndOperationLogControllersRejectInvalidInputAndRequireAuthority() throws Exception {
        String tag = uniqueTag();
        String adminToken = loginAdmin("admin", "adminqwert");

        assertBusinessCode(mockMvc.perform(get("/api/profile").header("Authorization", bearer(adminToken))), 0);
        assertBusinessCode(mockMvc.perform(put("/api/profile")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phone\":\"not-phone\",\"email\":\"not-email\"}")), 400);
        assertBusinessCode(mockMvc.perform(put("/api/profile")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"123456\",\"confirmPassword\":\"654321\"}")), 400);
        assertBusinessCode(mockMvc.perform(put("/api/profile")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phone\":\"139" + phoneSeed(tag) + "\",\"email\":\"profile-" + tag + "@zhaobiao.com\",\"realName\":\"边界测试管理员\",\"companyName\":\"平台运营中心\",\"contactPerson\":\"边界测试\",\"unifiedSocialCreditCode\":\"91310000MP" + creditSeed(tag) + "\"}")), 0);

        mockMvc.perform(get("/api/admin/operation-logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
        assertBusinessCode(mockMvc.perform(get("/api/admin/operation-logs")
                .header("Authorization", bearer(adminToken))), 0);
    }

    private ResultActions postJson(String path, String body) throws Exception {
        return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions postAdminJson(String adminToken, String path, String body) throws Exception {
        return mockMvc.perform(post(path)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void assertBusinessCode(ResultActions actions, int expectedCode) throws Exception {
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.code").value(expectedCode));
    }

    private String loginAdmin(String username, String password) throws Exception {
        MvcResult result = postJson("/api/auth/login", "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    private String loginMember(String username, String password) throws Exception {
        MvcResult result = postJson("/api/portal/auth/login", "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    private Long createAdmin(String adminToken, String username, String tag, Long roleId) throws Exception {
        return dataId(postAdminJson(adminToken, "/api/admin/admin-users",
                adminCreateBody(username, "139" + phoneSeed(tag), username + "@zhaobiao.com", "12345678", "12345678", roleId))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn());
    }

    private Long createMember(String adminToken,
                              String username,
                              String tag,
                              Long businessTypeId,
                              boolean canDownloadFile,
                              LocalDateTime expiresAt,
                              String password) throws Exception {
        return dataId(postAdminJson(adminToken, "/api/admin/members",
                memberCreateBody(username, tag, businessTypeId, canDownloadFile, expiresAt, password))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn());
    }

    private Long createTender(String adminToken,
                              String title,
                              Long businessTypeId,
                              LocalDateTime publishAt,
                              LocalDateTime deadline,
                              LocalDateTime signupDeadline,
                              String projectCode,
                              Long attachmentFileId,
                              String statusValue) throws Exception {
        return dataId(postAdminJson(adminToken, "/api/admin/tenders",
                tenderBody(title, businessTypeId, publishAt, deadline, signupDeadline, projectCode, attachmentFileId, statusValue))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn());
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

    private void expectPortalTotal(String memberToken, String keyword, int total) throws Exception {
        mockMvc.perform(get("/api/portal/tenders")
                        .header("Authorization", bearer(memberToken))
                        .param("keyword", keyword)
                        .param("pageNum", "0")
                        .param("pageSize", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(50))
                .andExpect(jsonPath("$.data.total").value(total));
    }

    private Long currentAdminId(String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private Long findRoleIdByCode(String adminToken, String code) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/roles").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode roles = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        for (JsonNode role : roles) {
            if (code.equals(role.path("code").asText())) {
                return role.path("id").asLong();
            }
        }
        return null;
    }

    private Long findBusinessTypeIdByCode(String adminToken, String code) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/business-types/options").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode types = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        for (JsonNode type : types) {
            if (code.equals(type.path("code").asText())) {
                return type.path("id").asLong();
            }
        }
        return null;
    }

    private Long findMenuIdByCode(String adminToken, String code) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/menus").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return findMenuId(objectMapper.readTree(result.getResponse().getContentAsString()).path("data"), code);
    }

    private Long findMenuId(JsonNode menus, String code) {
        if (menus == null || !menus.isArray()) {
            return null;
        }
        for (JsonNode menu : menus) {
            if (code.equals(menu.path("code").asText())) {
                return menu.path("id").asLong();
            }
            Long childId = findMenuId(menu.path("children"), code);
            if (childId != null) {
                return childId;
            }
        }
        return null;
    }

    private Long findAttachmentIdByFileId(String adminToken, Long tenderId, Long fileId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/tenders/{tenderId}", tenderId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode attachments = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("attachments");
        for (JsonNode attachment : attachments) {
            if (fileId.equals(attachment.path("fileId").asLong())) {
                return attachment.path("attachmentId").asLong();
            }
        }
        return null;
    }

    private Long dataId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private String adminCreateBody(String username,
                                   String phone,
                                   String email,
                                   String password,
                                   String confirmPassword,
                                   Long roleId) {
        return adminCreateBodyWithRawRoles(username, phone, email, password, confirmPassword, "[" + roleId + "]");
    }

    private String adminCreateBodyWithRawRoles(String username,
                                               String phone,
                                               String email,
                                               String password,
                                               String confirmPassword,
                                               String roleIdsJson) {
        return "{"
                + "\"username\":\"" + username + "\","
                + "\"phone\":\"" + phone + "\","
                + "\"email\":\"" + email + "\","
                + "\"realName\":\"边界管理员\","
                + "\"password\":\"" + password + "\","
                + "\"confirmPassword\":\"" + confirmPassword + "\","
                + "\"roleIds\":" + roleIdsJson
                + "}";
    }

    private String adminUpdateBody(String phone, String email) {
        return "{\"phone\":\"" + phone + "\",\"email\":\"" + email + "\",\"realName\":\"边界管理员更新\"}";
    }

    private String memberCreateBody(String username,
                                    String tag,
                                    Long businessTypeId,
                                    boolean canDownloadFile,
                                    LocalDateTime expiresAt,
                                    String password) {
        return memberCreateBodyWithRawTypes(username, tag, "[" + businessTypeId + "]", canDownloadFile, expiresAt, password);
    }

    private String memberCreateBodyWithRawTypes(String username,
                                                String tag,
                                                String businessTypeIdsJson,
                                                boolean canDownloadFile,
                                                LocalDateTime expiresAt,
                                                String password) {
        return "{"
                + "\"username\":\"" + username + "\","
                + "\"phone\":\"138" + phoneSeed(tag) + "\","
                + "\"email\":\"" + username + "@test.com\","
                + "\"companyName\":\"边界会员企业\","
                + "\"contactPerson\":\"李四\","
                + "\"unifiedSocialCreditCode\":\"91310000MB" + creditSeed(tag) + "\","
                + "\"realName\":\"李四\","
                + "\"password\":\"" + password + "\","
                + "\"confirmPassword\":\"" + password + "\","
                + "\"businessTypeIds\":" + businessTypeIdsJson + ","
                + "\"canDownloadFile\":" + canDownloadFile + ","
                + "\"status\":\"ENABLED\","
                + "\"expiresAt\":\"" + formatDateTime(expiresAt) + "\""
                + "}";
    }

    private String memberUpdateBody(String phone,
                                    String email,
                                    String creditCode,
                                    Long businessTypeId,
                                    LocalDateTime expiresAt) {
        return "{"
                + "\"phone\":\"" + phone + "\","
                + "\"email\":\"" + email + "\","
                + "\"companyName\":\"边界会员企业更新\","
                + "\"contactPerson\":\"王五\","
                + "\"unifiedSocialCreditCode\":\"" + creditCode + "\","
                + "\"realName\":\"王五\","
                + "\"businessTypeIds\":[" + businessTypeId + "],"
                + "\"expiresAt\":\"" + formatDateTime(expiresAt) + "\""
                + "}";
    }

    private String businessTypeBody(String code, String name, boolean enabled) {
        return "{"
                + "\"code\":\"" + code + "\","
                + "\"name\":\"" + name + "\","
                + "\"enabled\":" + enabled + ","
                + "\"sortOrder\":999,"
                + "\"description\":\"边界测试\""
                + "}";
    }

    private String menuBody(String code,
                            String name,
                            String type,
                            Long parentId,
                            String routePath,
                            String component,
                            boolean visible,
                            boolean enabled,
                            String permissionCode) {
        return "{"
                + "\"code\":\"" + code + "\","
                + "\"name\":\"" + name + "\","
                + "\"type\":\"" + type + "\","
                + "\"parentId\":" + (parentId == null ? "null" : parentId) + ","
                + "\"routePath\":\"" + routePath + "\","
                + "\"component\":\"" + component + "\","
                + "\"icon\":\"Menu\","
                + "\"sortOrder\":999,"
                + "\"visible\":" + visible + ","
                + "\"enabled\":" + enabled + ","
                + "\"permissionCode\":" + (permissionCode == null ? "null" : "\"" + permissionCode + "\"") + ","
                + "\"description\":\"边界测试\""
                + "}";
    }

    private String roleBody(String code, String name, String menuIdsJson) {
        return "{"
                + "\"code\":\"" + code + "\","
                + "\"name\":\"" + name + "\","
                + "\"description\":\"边界测试\","
                + "\"permissionIds\":[-1,-2],"
                + "\"menuIds\":" + menuIdsJson
                + "}";
    }

    private String tenderBody(String title,
                              Long businessTypeId,
                              LocalDateTime publishAt,
                              LocalDateTime deadline,
                              LocalDateTime signupDeadline,
                              String projectCode,
                              Long attachmentFileId,
                              String statusValue) {
        return "{"
                + "\"title\":\"" + title + "\","
                + "\"region\":\"浙江\","
                + "\"businessTypeId\":" + businessTypeId + ","
                + "\"publishAt\":\"" + formatDateTime(publishAt) + "\","
                + "\"content\":\"<p>" + title + " 正文</p>\","
                + "\"contactPerson\":\"张三\","
                + "\"budget\":\"100 万元\","
                + "\"contactPhone\":\"0571-88886666\","
                + "\"tenderUnit\":\"边界测试招标单位\","
                + "\"deadline\":\"" + formatDateTime(deadline) + "\","
                + "\"projectCode\":\"" + projectCode + "\","
                + "\"signupDeadline\":\"" + formatDateTime(signupDeadline) + "\","
                + "\"status\":\"" + statusValue + "\","
                + "\"attachmentFileIds\":" + (attachmentFileId == null ? "[]" : "[" + attachmentFileId + "]")
                + "}";
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
