package com.zhaobiao.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAuditAssignMultiRolesAndLoginFlowWorks() throws Exception {
        String username = "user1001";
        String phone = "13800138001";
        String email = "user1001@test.com";
        String creditCode = "91310000MA1K123451";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"" + phone + "\",\"email\":\"" + email + "\",\"companyName\":\"测试企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"" + creditCode + "\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        String adminToken = login("admin", "adminqwert");

        Long userId = findUserIdByUsername(adminToken, username);
        assertNotNull(userId);

        mockMvc.perform(put("/api/admin/users/{userId}/audit", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        Long normalUserRoleId = findRoleIdByCode(adminToken, "NORMAL_USER");
        Long auditorRoleId = findRoleIdByCode(adminToken, "USER_AUDITOR");

        mockMvc.perform(put("/api/admin/users/{userId}/roles", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[" + normalUserRoleId + "," + auditorRoleId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roleCodes", hasSize(2)))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("NORMAL_USER")))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("USER_AUDITOR")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.user.username").value(username))
                .andExpect(jsonPath("$.data.user.roleCodes", hasSize(2)))
                .andExpect(jsonPath("$.data.user.permissions", hasItem("user:audit")))
                .andExpect(jsonPath("$.data.user.menus", hasSize(3)));
    }

    @Test
    void rejectAuditMustProvideReasonAndAuditRecordShouldBeSaved() throws Exception {
        String username = "user2001";
        String phone = "13800138002";
        String email = "user2001@test.com";
        String creditCode = "91310000MA1K123452";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"" + phone + "\",\"email\":\"" + email + "\",\"companyName\":\"审核企业\",\"contactPerson\":\"王五\",\"unifiedSocialCreditCode\":\"" + creditCode + "\",\"realName\":\"王五\",\"password\":\"123456\",\"confirmPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String adminToken = login("admin", "adminqwert");
        Long userId = findUserIdByUsername(adminToken, username);

        mockMvc.perform(put("/api/admin/users/{userId}/audit", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(put("/api/admin/users/{userId}/audit", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":false,\"reason\":\"资料不完整\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.auditReason").value("资料不完整"));

        mockMvc.perform(get("/api/admin/users/{userId}/audit-records", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].decision").value("REJECTED"))
                .andExpect(jsonPath("$.data[0].reason").value("资料不完整"));
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    private Long findUserIdByUsername(String adminToken, String username) throws Exception {
        MvcResult userListResult = mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode users = objectMapper.readTree(userListResult.getResponse().getContentAsString()).path("data");
        for (JsonNode user : users) {
            if (username.equals(user.path("username").asText())) {
                return user.path("id").asLong();
            }
        }
        return null;
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
}
