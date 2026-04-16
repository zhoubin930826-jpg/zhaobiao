package com.zhaobiao.admin;

import com.zhaobiao.admin.config.DataInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DataInitializer.class)
class AuthFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminPublicRegisterIsDisabledAndSuperAdminCanCreateAdmin() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"legacy001\",\"phone\":\"13800138101\",\"email\":\"legacy001@test.com\",\"companyName\":\"测试企业\",\"contactPerson\":\"张三\",\"unifiedSocialCreditCode\":\"91310000MA1K111111\",\"realName\":\"张三\",\"password\":\"123456\",\"confirmPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long systemAdminRoleId = findRoleIdByCode(superAdminToken, "SYSTEM_ADMIN");
        assertNotNull(systemAdminRoleId);

        MvcResult createResult = mockMvc.perform(post("/api/admin/admin-users")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"manager1001\",\"phone\":\"13800138102\",\"email\":\"manager1001@zhaobiao.com\",\"realName\":\"系统管理员甲\",\"password\":\"123456\",\"confirmPassword\":\"123456\",\"roleIds\":[" + systemAdminRoleId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("manager1001"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("SYSTEM_ADMIN")))
                .andReturn();

        Long adminUserId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
        assertNotNull(adminUserId);

        String managerToken = loginAdmin("manager1001", "123456");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("manager1001"))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("SYSTEM_ADMIN")));

        mockMvc.perform(get("/api/admin/admin-users")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(put("/api/admin/admin-users/{userId}/status", adminUserId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"manager1001\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void memberAccountIsCreatedByAdminAndCanBeManaged() throws Exception {
        mockMvc.perform(post("/api/portal/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"member1001\",\"phone\":\"13800138111\",\"email\":\"member1001@test.com\",\"companyName\":\"会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MA1K222222\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        String uniqueTag = String.valueOf(System.currentTimeMillis());
        String username = "member" + uniqueTag;
        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long engineeringTypeId = findBusinessTypeIdByCode(superAdminToken, "ENGINEERING");
        Long goodsTypeId = findBusinessTypeIdByCode(superAdminToken, "GOODS");
        assertNotNull(engineeringTypeId);
        assertNotNull(goodsTypeId);

        MvcResult createResult = mockMvc.perform(post("/api/admin/members")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"139" + uniqueTag.substring(uniqueTag.length() - 8) + "\",\"email\":\"" + username + "@test.com\",\"companyName\":\"会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MA1K" + uniqueTag.substring(uniqueTag.length() - 6) + "\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\",\"businessTypeIds\":[" + engineeringTypeId + "],\"canDownloadFile\":false,\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.businessTypes", hasSize(1)))
                .andExpect(jsonPath("$.data.businessTypes[0].code").value("ENGINEERING"))
                .andReturn();

        Long memberId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
        assertNotNull(memberId);

        String memberToken = loginMember(username, "123456");

        mockMvc.perform(get("/api/portal/auth/me")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.businessTypes", hasSize(1)))
                .andExpect(jsonPath("$.data.businessTypes[0].code").value("ENGINEERING"))
                .andExpect(jsonPath("$.data.canDownloadFile").value(false))
                .andExpect(jsonPath("$.data.status").value("ENABLED"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(put("/api/admin/members/{memberId}", memberId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"137" + uniqueTag.substring(uniqueTag.length() - 8) + "\",\"email\":\"updated-" + username + "@test.com\",\"companyName\":\"更新后的会员企业\",\"contactPerson\":\"王五\",\"unifiedSocialCreditCode\":\"91310000MA2K" + uniqueTag.substring(uniqueTag.length() - 6) + "\",\"realName\":\"王五\",\"businessTypeIds\":[" + engineeringTypeId + "," + goodsTypeId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.businessTypes", hasSize(2)));

        mockMvc.perform(put("/api/admin/members/{memberId}/download-access", memberId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canDownloadFile\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownloadFile").value(true));

        mockMvc.perform(put("/api/admin/members/{memberId}/password", memberId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"654321\",\"confirmPassword\":\"654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String refreshedToken = loginMember(username, "654321");
        mockMvc.perform(get("/api/portal/auth/me")
                        .header("Authorization", "Bearer " + refreshedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownloadFile").value(true))
                .andExpect(jsonPath("$.data.businessTypes", hasSize(2)));

        mockMvc.perform(put("/api/admin/members/{memberId}/status", memberId)
                        .header("Authorization", "Bearer " + superAdminToken)
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

    @Test
    void legacyUserManagementEndpointIsDisabledAndCannotBeUsedForPrivilegeEscalation() throws Exception {
        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long systemAdminRoleId = findRoleIdByCode(superAdminToken, "SYSTEM_ADMIN");
        Long superAdminRoleId = findRoleIdByCode(superAdminToken, "SUPER_ADMIN");
        assertNotNull(systemAdminRoleId);
        assertNotNull(superAdminRoleId);

        String username = "legacylock" + System.currentTimeMillis();
        String phoneSuffix = username.substring(username.length() - 8);
        MvcResult createResult = mockMvc.perform(post("/api/admin/admin-users")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"138" + phoneSuffix + "\",\"email\":\"" + username + "@zhaobiao.com\",\"realName\":\"遗留接口回归测试\",\"password\":\"12345678\",\"confirmPassword\":\"12345678\",\"roleIds\":[" + systemAdminRoleId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        Long adminUserId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
        assertNotNull(adminUserId);

        String managerToken = loginAdmin(username, "12345678");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(410));

        mockMvc.perform(put("/api/admin/users/{userId}/roles", adminUserId)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[" + superAdminRoleId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(410));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("SYSTEM_ADMIN")))
                .andExpect(jsonPath("$.data.roleCodes", not(hasItem("SUPER_ADMIN"))));

        mockMvc.perform(get("/api/admin/admin-users")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
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

    private Long findRoleIdByCode(String adminToken, String roleCode) throws Exception {
        MvcResult roleListResult = mockMvc.perform(get("/api/admin/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode roles = objectMapper.readTree(roleListResult.getResponse().getContentAsString()).path("data");
        for (JsonNode role : roles) {
            if (roleCode.equals(role.path("code").asText())) {
                return role.path("id").asLong();
            }
        }
        return null;
    }

    private Long findBusinessTypeIdByCode(String adminToken, String code) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/business-types/options")
                        .header("Authorization", "Bearer " + adminToken))
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
}
