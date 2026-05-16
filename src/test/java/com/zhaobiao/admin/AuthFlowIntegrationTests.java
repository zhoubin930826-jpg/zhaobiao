package com.zhaobiao.admin;

import com.zhaobiao.admin.config.DataInitializer;
import com.zhaobiao.admin.service.CaptchaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class AuthFlowIntegrationTests {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaptchaService captchaService;

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
    void portalCaptchaSelfRegisterCreatesDisabledMemberAndBlocksLoginUntilEnabled() throws Exception {
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        String username = "selfreg" + uniqueTag;
        String phoneSeed = uniqueTag.substring(uniqueTag.length() - 8);
        String creditSeed = uniqueTag.substring(uniqueTag.length() - 6);

        mockMvc.perform(get("/api/portal/auth/captcha")
                        .param("scene", "register")
                        .param("captchaId", "image-" + uniqueTag))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsByteArray().length > 100));

        mockMvc.perform(multipart("/api/portal/auth/register")
                        .file(profileFile("businessLicenseFile", "营业执照-" + uniqueTag + ".pdf", "license-nocaptcha-" + uniqueTag))
                        .file(profileFile("threeYearPerformanceFile", "三年业绩-" + uniqueTag + ".pdf", "performance-nocaptcha-" + uniqueTag))
                        .param("username", username + "nocaptcha")
                        .param("phone", "137" + phoneSeed)
                        .param("email", "nocaptcha-" + username + "@test.com")
                        .param("companyName", "自助注册企业")
                        .param("contactPerson", "李四")
                        .param("unifiedSocialCreditCode", "91310000MR1K" + creditSeed)
                        .param("realName", "李四")
                        .param("password", "123456")
                        .param("confirmPassword", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        String wrongCaptchaId = "wrong-" + uniqueTag;
        captchaService.create("register", wrongCaptchaId);
        mockMvc.perform(multipart("/api/portal/auth/register")
                        .file(profileFile("businessLicenseFile", "营业执照-" + uniqueTag + ".pdf", "license-wrong-" + uniqueTag))
                        .file(profileFile("threeYearPerformanceFile", "三年业绩-" + uniqueTag + ".pdf", "performance-wrong-" + uniqueTag))
                        .param("username", username + "wrong")
                        .param("phone", "136" + phoneSeed)
                        .param("email", "wrong-" + username + "@test.com")
                        .param("companyName", "自助注册企业")
                        .param("contactPerson", "李四")
                        .param("unifiedSocialCreditCode", "91310000MR2K" + creditSeed)
                        .param("realName", "李四")
                        .param("password", "123456")
                        .param("confirmPassword", "123456")
                        .param("captchaId", wrongCaptchaId)
                        .param("captchaCode", "0000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        String missingFileCaptchaId = "missing-file-" + uniqueTag;
        String missingFileCaptchaCode = captchaService.create("register", missingFileCaptchaId).getCode();
        mockMvc.perform(multipart("/api/portal/auth/register")
                        .file(profileFile("businessLicenseFile", "营业执照-" + uniqueTag + ".pdf", "license-missing-file-" + uniqueTag))
                        .param("username", username + "missingfile")
                        .param("phone", "135" + phoneSeed)
                        .param("email", "missingfile-" + username + "@test.com")
                        .param("companyName", "自助注册企业")
                        .param("contactPerson", "李四")
                        .param("unifiedSocialCreditCode", "91310000MR3K" + creditSeed)
                        .param("realName", "李四")
                        .param("password", "123456")
                        .param("confirmPassword", "123456")
                        .param("captchaId", missingFileCaptchaId)
                        .param("captchaCode", missingFileCaptchaCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        String registerCaptchaId = "register-" + uniqueTag;
        String registerCaptchaCode = captchaService.create("register", registerCaptchaId).getCode();
        MvcResult registerResult = mockMvc.perform(multipart("/api/portal/auth/register")
                        .file(profileFile("businessLicenseFile", "营业执照-" + uniqueTag + ".pdf", "license-success-" + uniqueTag))
                        .file(profileFile("threeYearPerformanceFile", "三年业绩-" + uniqueTag + ".pdf", "performance-success-" + uniqueTag))
                        .param("username", username)
                        .param("phone", "134" + phoneSeed)
                        .param("email", username + "@test.com")
                        .param("companyName", "自助注册企业")
                        .param("contactPerson", "李四")
                        .param("unifiedSocialCreditCode", "91310000MR4K" + creditSeed)
                        .param("realName", "李四")
                        .param("password", "123456")
                        .param("confirmPassword", "123456")
                        .param("captchaId", registerCaptchaId)
                        .param("captchaCode", registerCaptchaCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("注册成功，请等待管理员启用账号"))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.status").value("DISABLED"))
                .andExpect(jsonPath("$.data.canDownloadFile").value(false))
                .andExpect(jsonPath("$.data.expiresAt").doesNotExist())
                .andExpect(jsonPath("$.data.businessTypes", hasSize(0)))
                .andExpect(jsonPath("$.data.businessLicenseFileId").exists())
                .andExpect(jsonPath("$.data.threeYearPerformanceFileId").exists())
                .andReturn();

        Long memberId = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
        assertNotNull(memberId);

        mockMvc.perform(post("/api/portal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(portalLoginBody(username, "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("账号未启用，请联系管理员"));

        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long engineeringTypeId = findBusinessTypeIdByCode(superAdminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);
        mockMvc.perform(put("/api/admin/members/{memberId}", memberId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"134" + phoneSeed + "\",\"email\":\"approved-" + username + "@test.com\",\"companyName\":\"自助注册企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MR5K" + creditSeed + "\",\"realName\":\"李四\",\"businessTypeIds\":[" + engineeringTypeId + "],\"expiresAt\":\"" + formatDateTime(LocalDateTime.now().plusMonths(1)) + "\",\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ENABLED"))
                .andExpect(jsonPath("$.data.businessTypes", hasSize(1)))
                .andExpect(jsonPath("$.data.expired").value(false));

        String memberToken = loginMember(username, "123456");
        assertNotNull(memberToken);
    }

    @Test
    void memberAccountIsCreatedByAdminAndCanBeManaged() throws Exception {
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        String username = "member" + uniqueTag;
        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long engineeringTypeId = findBusinessTypeIdByCode(superAdminToken, "ENGINEERING");
        Long goodsTypeId = findBusinessTypeIdByCode(superAdminToken, "GOODS");
        assertNotNull(engineeringTypeId);
        assertNotNull(goodsTypeId);
        String futureExpiresAt = formatDateTime(LocalDateTime.now().plusMonths(1));
        String updatedExpiresAt = formatDateTime(LocalDateTime.now().plusMonths(2));

        MvcResult createResult = mockMvc.perform(post("/api/admin/members")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"139" + uniqueTag.substring(uniqueTag.length() - 8) + "\",\"email\":\"" + username + "@test.com\",\"companyName\":\"会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MA1K" + uniqueTag.substring(uniqueTag.length() - 6) + "\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\",\"businessTypeIds\":[" + engineeringTypeId + "],\"canDownloadFile\":false,\"status\":\"ENABLED\",\"expiresAt\":\"" + futureExpiresAt + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.expiresAt").exists())
                .andExpect(jsonPath("$.data.expired").value(false))
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
                .andExpect(jsonPath("$.data.expired").value(false))
                .andExpect(jsonPath("$.data.status").value("ENABLED"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(put("/api/admin/members/{memberId}", memberId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"137" + uniqueTag.substring(uniqueTag.length() - 8) + "\",\"email\":\"updated-" + username + "@test.com\",\"companyName\":\"更新后的会员企业\",\"contactPerson\":\"王五\",\"unifiedSocialCreditCode\":\"91310000MA2K" + uniqueTag.substring(uniqueTag.length() - 6) + "\",\"realName\":\"王五\",\"businessTypeIds\":[" + engineeringTypeId + "," + goodsTypeId + "],\"expiresAt\":\"" + updatedExpiresAt + "\"}"))
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
                        .content(portalLoginBody(username, "654321")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void memberFirstLoginIsRecordedAndProfileFilesCanBeManagedByAdminAndMember() throws Exception {
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        String username = "profile" + uniqueTag;
        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long engineeringTypeId = findBusinessTypeIdByCode(superAdminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);
        String phoneSeed = uniqueTag.substring(uniqueTag.length() - 8);
        String creditSeed = uniqueTag.substring(uniqueTag.length() - 6);
        String futureExpiresAt = formatDateTime(LocalDateTime.now().plusMonths(1));

        Long licenseFileId = uploadAdminMemberProfileFile(superAdminToken, "营业执照-" + uniqueTag + ".pdf", "license");
        MvcResult createResult = mockMvc.perform(post("/api/admin/members")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"139" + phoneSeed + "\",\"email\":\"" + username + "@test.com\",\"companyName\":\"会员资料企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MF1K" + creditSeed + "\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\",\"businessTypeIds\":[" + engineeringTypeId + "],\"canDownloadFile\":false,\"status\":\"ENABLED\",\"expiresAt\":\"" + futureExpiresAt + "\",\"businessLicenseFileId\":" + licenseFileId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.firstLoginAt").doesNotExist())
                .andExpect(jsonPath("$.data.businessLicenseFileId").value(licenseFileId))
                .andExpect(jsonPath("$.data.businessLicenseFileName").value("营业执照-" + uniqueTag + ".pdf"))
                .andReturn();
        Long memberId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        MvcResult firstLogin = mockMvc.perform(post("/api/portal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(portalLoginBody(username, "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.profileCompletionRequired").value(true))
                .andExpect(jsonPath("$.data.user.firstLoginAt").exists())
                .andExpect(jsonPath("$.data.user.businessLicenseFileId").value(licenseFileId))
                .andReturn();
        JsonNode firstLoginData = objectMapper.readTree(firstLogin.getResponse().getContentAsString()).path("data");
        String memberToken = firstLoginData.path("token").asText();
        String firstLoginAt = firstLoginData.path("user").path("firstLoginAt").asText();

        MvcResult secondLogin = mockMvc.perform(post("/api/portal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(portalLoginBody(username, "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.profileCompletionRequired").value(false))
                .andReturn();
        String secondFirstLoginAt = objectMapper.readTree(secondLogin.getResponse().getContentAsString())
                .path("data")
                .path("user")
                .path("firstLoginAt")
                .asText();
        assertEquals(firstLoginAt, secondFirstLoginAt);

        Long performanceFileId = uploadPortalMemberProfileFile(memberToken, "三年业绩-" + uniqueTag + ".pdf", "performance");
        mockMvc.perform(put("/api/portal/auth/profile")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"132" + phoneSeed + "\",\"email\":\"profile-updated-" + username + "@test.com\",\"companyName\":\"会员资料企业更新\",\"contactPerson\":\"王五\",\"unifiedSocialCreditCode\":\"91310000MF2K" + creditSeed + "\",\"realName\":\"王五\",\"threeYearPerformanceFileId\":" + performanceFileId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.businessLicenseFileId").value(licenseFileId))
                .andExpect(jsonPath("$.data.threeYearPerformanceFileId").value(performanceFileId))
                .andExpect(jsonPath("$.data.threeYearPerformanceFileName").value("三年业绩-" + uniqueTag + ".pdf"));

        Long newLicenseFileId = uploadAdminMemberProfileFile(superAdminToken, "新营业执照-" + uniqueTag + ".pdf", "new license");
        mockMvc.perform(put("/api/admin/members/{memberId}", memberId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"131" + phoneSeed + "\",\"email\":\"admin-updated-" + username + "@test.com\",\"companyName\":\"管理员更新会员资料企业\",\"contactPerson\":\"赵六\",\"unifiedSocialCreditCode\":\"91310000MF3K" + creditSeed + "\",\"realName\":\"赵六\",\"businessTypeIds\":[" + engineeringTypeId + "],\"expiresAt\":\"" + futureExpiresAt + "\",\"businessLicenseFileId\":" + newLicenseFileId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.businessLicenseFileId").value(newLicenseFileId))
                .andExpect(jsonPath("$.data.threeYearPerformanceFileId").value(performanceFileId));
    }

    @Test
    void memberExpirationIsRequiredAndBlocksLoginAndPortalAccess() throws Exception {
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        String username = "expire" + uniqueTag;
        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long engineeringTypeId = findBusinessTypeIdByCode(superAdminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);
        String futureExpiresAt = formatDateTime(LocalDateTime.now().plusDays(30));
        String pastExpiresAt = formatDateTime(LocalDateTime.now().minusMinutes(1));

        mockMvc.perform(post("/api/admin/members")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "missing\",\"phone\":\"136" + uniqueTag.substring(uniqueTag.length() - 8) + "\",\"email\":\"missing-" + username + "@test.com\",\"companyName\":\"会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MA3K" + uniqueTag.substring(uniqueTag.length() - 6) + "\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\",\"businessTypeIds\":[" + engineeringTypeId + "],\"canDownloadFile\":false,\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        MvcResult createResult = mockMvc.perform(post("/api/admin/members")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"135" + uniqueTag.substring(uniqueTag.length() - 8) + "\",\"email\":\"" + username + "@test.com\",\"companyName\":\"会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MA4K" + uniqueTag.substring(uniqueTag.length() - 6) + "\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\",\"businessTypeIds\":[" + engineeringTypeId + "],\"canDownloadFile\":false,\"status\":\"ENABLED\",\"expiresAt\":\"" + futureExpiresAt + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.expiresAt").exists())
                .andExpect(jsonPath("$.data.expired").value(false))
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
                .andExpect(jsonPath("$.data.expired").value(false));

        mockMvc.perform(put("/api/admin/members/{memberId}", memberId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"134" + uniqueTag.substring(uniqueTag.length() - 8) + "\",\"email\":\"expired-" + username + "@test.com\",\"companyName\":\"会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MA5K" + uniqueTag.substring(uniqueTag.length() - 6) + "\",\"realName\":\"李四\",\"businessTypeIds\":[" + engineeringTypeId + "],\"expiresAt\":\"" + pastExpiresAt + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.expired").value(true));

        mockMvc.perform(post("/api/portal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(portalLoginBody(username, "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/portal/auth/me")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
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

    @Test
    void roleAuthoritiesUseMenuCodesAndIgnoreSubmittedPermissionIds() throws Exception {
        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long dashboardMenuId = findMenuIdByCode(superAdminToken, "DASHBOARD");
        Long memberViewMenuId = findMenuIdByCode(superAdminToken, "SYSTEM_MEMBER_USER");
        Long memberCreateButtonId = findMenuIdByCode(superAdminToken, "MEMBER_CREATE_BUTTON");
        assertNotNull(dashboardMenuId);
        assertNotNull(memberViewMenuId);
        assertNotNull(memberCreateButtonId);

        Long roleId = createRoleWithMenus(
                superAdminToken,
                "MENU_ONLY_" + System.currentTimeMillis(),
                Arrays.asList(dashboardMenuId, memberViewMenuId, memberCreateButtonId),
                Arrays.asList(-1L, -2L)
        );

        JsonNode role = findRoleById(superAdminToken, roleId);
        assertNotNull(role);
        assertTrue(hasTextValue(role.path("menuCodes"), "DASHBOARD"));
        assertTrue(hasTextValue(role.path("menuCodes"), "SYSTEM_MEMBER_USER"));
        assertTrue(hasTextValue(role.path("menuCodes"), "MEMBER_CREATE_BUTTON"));
        assertFalse(role.path("permissionCodes").elements().hasNext());
    }

    @Test
    void loginUserAuthoritiesUseMenuCodes() throws Exception {
        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long dashboardMenuId = findMenuIdByCode(superAdminToken, "DASHBOARD");
        Long memberViewMenuId = findMenuIdByCode(superAdminToken, "SYSTEM_MEMBER_USER");
        Long memberCreateButtonId = findMenuIdByCode(superAdminToken, "MEMBER_CREATE_BUTTON");
        assertNotNull(dashboardMenuId);
        assertNotNull(memberViewMenuId);
        assertNotNull(memberCreateButtonId);
        Long roleId = createRoleWithMenus(
                superAdminToken,
                "MENU_AUTH_" + System.currentTimeMillis(),
                Arrays.asList(dashboardMenuId, memberViewMenuId, memberCreateButtonId),
                Collections.emptyList()
        );

        String username = "menuauth" + System.currentTimeMillis();
        createAdminWithRole(superAdminToken, username, roleId);
        String menuAdminToken = loginAdmin(username, "12345678");

        MvcResult result = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + menuAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode permissions = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("permissions");
        assertTrue(hasTextValue(permissions, "DASHBOARD"));
        assertTrue(hasTextValue(permissions, "SYSTEM_MEMBER_USER"));
        assertTrue(hasTextValue(permissions, "MEMBER_CREATE_BUTTON"));
        assertFalse(hasTextValue(permissions, "member:create"));
    }

    @Test
    void roleWithoutButtonMenuDoesNotReceiveButtonAuthority() throws Exception {
        String superAdminToken = loginAdmin("admin", "adminqwert");
        Long dashboardMenuId = findMenuIdByCode(superAdminToken, "DASHBOARD");
        Long memberViewMenuId = findMenuIdByCode(superAdminToken, "SYSTEM_MEMBER_USER");
        assertNotNull(dashboardMenuId);
        assertNotNull(memberViewMenuId);

        Long roleId = createRoleWithMenus(
                superAdminToken,
                "MENU_VIEW_ONLY_" + System.currentTimeMillis(),
                Arrays.asList(dashboardMenuId, memberViewMenuId),
                Collections.emptyList()
        );

        JsonNode role = findRoleById(superAdminToken, roleId);
        assertNotNull(role);
        assertTrue(hasTextValue(role.path("menuCodes"), "DASHBOARD"));
        assertTrue(hasTextValue(role.path("menuCodes"), "SYSTEM_MEMBER_USER"));
        assertFalse(hasTextValue(role.path("menuCodes"), "MEMBER_CREATE_BUTTON"));
        assertFalse(role.path("permissionCodes").elements().hasNext());
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
                        .content(portalLoginBody(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    private String portalLoginBody(String username, String password) {
        String captchaId = "login-" + System.nanoTime();
        String captchaCode = captchaService.create("login", captchaId).getCode();
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"captchaId\":\"" + captchaId + "\",\"captchaCode\":\"" + captchaCode + "\"}";
    }

    private MockMultipartFile profileFile(String fieldName, String fileName, String content) {
        return new MockMultipartFile(
                fieldName,
                fileName,
                MediaType.APPLICATION_PDF_VALUE,
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Long createRoleWithMenus(String adminToken, String roleCode, List<Long> menuIds, List<Long> permissionIds) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + roleCode + "\",\"name\":\"菜单授权测试角色\",\"description\":\"menu derived\",\"permissionIds\":" + toJsonArray(permissionIds) + ",\"menuIds\":" + toJsonArray(menuIds) + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private Long createAdminWithRole(String adminToken, String username, Long roleId) throws Exception {
        String phoneSuffix = username.substring(username.length() - 8);
        MvcResult createResult = mockMvc.perform(post("/api/admin/admin-users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"138" + phoneSuffix + "\",\"email\":\"" + username + "@zhaobiao.com\",\"realName\":\"菜单授权测试管理员\",\"password\":\"12345678\",\"confirmPassword\":\"12345678\",\"roleIds\":[" + roleId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private Long uploadAdminMemberProfileFile(String adminToken, String fileName, String content) throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "files",
                fileName,
                MediaType.APPLICATION_PDF_VALUE,
                content.getBytes(StandardCharsets.UTF_8)
        );
        MvcResult result = mockMvc.perform(multipart("/api/admin/members/profile-files")
                        .file(multipartFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path(0).path("fileId").asLong();
    }

    private Long uploadPortalMemberProfileFile(String memberToken, String fileName, String content) throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "files",
                fileName,
                MediaType.APPLICATION_PDF_VALUE,
                content.getBytes(StandardCharsets.UTF_8)
        );
        MvcResult result = mockMvc.perform(multipart("/api/portal/auth/profile/files")
                        .file(multipartFile)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path(0).path("fileId").asLong();
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

    private JsonNode findRoleById(String adminToken, Long roleId) throws Exception {
        MvcResult roleListResult = mockMvc.perform(get("/api/admin/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode roles = objectMapper.readTree(roleListResult.getResponse().getContentAsString()).path("data");
        for (JsonNode role : roles) {
            if (role.path("id").asLong() == roleId) {
                return role;
            }
        }
        return null;
    }

    private Long findMenuIdByCode(String adminToken, String code) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/menus")
                        .header("Authorization", "Bearer " + adminToken))
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

    private boolean hasTextValue(JsonNode values, String expected) {
        if (values == null || !values.isArray()) {
            return false;
        }
        for (JsonNode value : values) {
            if (expected.equals(value.asText())) {
                return true;
            }
        }
        return false;
    }

    private String toJsonArray(List<Long> values) {
        return values == null ? "[]" : values.toString();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
