package com.zhaobiao.admin;

import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.config.DataInitializer;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class TenderIntegrationTests {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenderFileStorageRepository tenderFileStorageRepository;

    @Test
    void adminCanUploadCreateTendersAndPortalListIsPagedInDescendingOrderForMultiTypeMember() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        Long goodsTypeId = findBusinessTypeIdByCode(adminToken, "GOODS");
        assertNotNull(engineeringTypeId);
        assertNotNull(goodsTypeId);

        Long firstFileId = uploadFile(adminToken, "招标文件-" + uniqueTag + "-1.txt", "first tender file");
        Long secondFileId = uploadFile(adminToken, "招标文件-" + uniqueTag + "-2.txt", "second tender file");

        String keyword = "项目-" + uniqueTag;
        Long olderTenderId = createTender(adminToken,
                "项目-" + uniqueTag + "-A",
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                "ZB-" + uniqueTag + "-A",
                firstFileId);
        Long newerTenderId = createTender(adminToken,
                "项目-" + uniqueTag + "-B",
                "浙江",
                goodsTypeId,
                LocalDateTime.now().minusHours(4),
                LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(7),
                "ZB-" + uniqueTag + "-B",
                secondFileId);
        createTender(adminToken,
                "项目-" + uniqueTag + "-CLOSED",
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(7),
                "ZB-" + uniqueTag + "-CLOSED",
                secondFileId,
                "CLOSED");

        String memberToken = createMemberAndLogin(adminToken, "multi" + uniqueTag, uniqueTag, engineeringTypeId, goodsTypeId);

        mockMvc.perform(get("/api/portal/tenders")
                        .header("Authorization", "Bearer " + memberToken)
                        .param("keyword", keyword)
                        .param("pageNum", "1")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].id").value(newerTenderId))
                .andExpect(jsonPath("$.data.list[0].title").value("项目-" + uniqueTag + "-B"))
                .andExpect(jsonPath("$.data.list[0].businessType.code").value("GOODS"));

        mockMvc.perform(get("/api/portal/tenders")
                        .header("Authorization", "Bearer " + memberToken)
                        .param("keyword", keyword)
                        .param("pageNum", "2")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value(olderTenderId))
                .andExpect(jsonPath("$.data.list[0].title").value("项目-" + uniqueTag + "-A"))
                .andExpect(jsonPath("$.data.list[0].businessType.code").value("ENGINEERING"));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", newerTenderId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(newerTenderId))
                .andExpect(jsonPath("$.data.businessType.code").value("GOODS"))
                .andExpect(jsonPath("$.data.canDownload").value(false))
                .andExpect(jsonPath("$.data.attachments", hasSize(1)))
                .andExpect(jsonPath("$.data.attachments[0].fileName").value("招标文件-" + uniqueTag + "-2.txt"));

        mockMvc.perform(get("/api/portal/tenders")
                        .header("Authorization", "Bearer " + memberToken)
                        .param("keyword", "CLOSED")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void memberDownloadPermissionControlsAttachmentDownloadAndTypeIsolationStillWorks() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        Long goodsTypeId = findBusinessTypeIdByCode(adminToken, "GOODS");
        assertNotNull(engineeringTypeId);
        assertNotNull(goodsTypeId);

        Long fileId = uploadFile(adminToken, "下载测试-" + uniqueTag + ".txt", "downloadable content");
        Long tenderId = createTender(adminToken,
                "下载测试项目-" + uniqueTag,
                "上海",
                engineeringTypeId,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(1),
                "DL-" + uniqueTag,
                fileId);
        Long goodsTenderId = createTender(adminToken,
                "货物下载测试项目-" + uniqueTag,
                "上海",
                goodsTypeId,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(1),
                "GOODS-" + uniqueTag,
                fileId);

        Long attachmentId = findAttachmentId(adminToken, tenderId);
        assertNotNull(attachmentId);

        String memberUsername = "member" + uniqueTag;
        String memberToken = createMemberAndLogin(adminToken, memberUsername, uniqueTag, engineeringTypeId);

        mockMvc.perform(get("/api/portal/tenders")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownload").value(false));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", goodsTenderId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        Long memberId = findMemberIdByUsername(adminToken, memberUsername);
        assertNotNull(memberId);

        mockMvc.perform(put("/api/admin/members/{memberId}/download-access", memberId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canDownloadFile\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownloadFile").value(true));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownload").value(true));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().string("downloadable content"));

        mockMvc.perform(put("/api/admin/members/{memberId}/status", memberId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void duplicateUploadReusesExistingFileStorageAndKeepsFirstFileName() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        String firstFileName = "duplicate-" + uniqueTag + "-first.txt";
        String secondFileName = "duplicate-" + uniqueTag + "-second.txt";
        String content = "same-content-" + uniqueTag;

        JsonNode firstUpload = uploadFileResponse(adminToken, firstFileName, content);
        JsonNode secondUpload = uploadFileResponse(adminToken, secondFileName, content);

        Long firstFileId = firstUpload.path("fileId").asLong();
        Long secondFileId = secondUpload.path("fileId").asLong();
        assertEquals(firstFileId, secondFileId);
        assertEquals(firstFileName, firstUpload.path("fileName").asText());
        assertEquals(firstFileName, secondUpload.path("fileName").asText());

        TenderFileStorage storage = tenderFileStorageRepository.findById(firstFileId).orElseThrow(AssertionError::new);
        assertEquals(firstFileName, storage.getOriginalName());
        assertEquals(sha256(content), storage.getContentHash());
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

    private Long uploadFile(String adminToken, String fileName, String content) throws Exception {
        return uploadFileResponse(adminToken, fileName, content).path("fileId").asLong();
    }

    private JsonNode uploadFileResponse(String adminToken, String fileName, String content) throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "files",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes(StandardCharsets.UTF_8)
        );
        MvcResult result = mockMvc.perform(multipart("/api/admin/files/upload")
                        .file(multipartFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path(0);
    }

    private Long createTender(String adminToken,
                              String title,
                              String region,
                              Long businessTypeId,
                              LocalDateTime publishAt,
                              LocalDateTime deadline,
                              LocalDateTime signupDeadline,
                              String projectCode,
                              Long attachmentFileId) throws Exception {
        return createTender(adminToken, title, region, businessTypeId, publishAt, deadline, signupDeadline, projectCode, attachmentFileId, "PUBLISHED");
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
        String requestBody = "{"
                + "\"title\":\"" + title + "\","
                + "\"region\":\"" + region + "\","
                + "\"businessTypeId\":" + businessTypeId + ","
                + "\"publishAt\":\"" + publishAt.format(DATE_TIME_FORMATTER) + "\","
                + "\"content\":\"<p>这是 " + title + " 的正文</p>\","
                + "\"contactPerson\":\"张三\","
                + "\"budget\":\"100 万元\","
                + "\"contactPhone\":\"0571-88886666\","
                + "\"tenderUnit\":\"测试招标单位\","
                + "\"deadline\":\"" + deadline.format(DATE_TIME_FORMATTER) + "\","
                + "\"projectCode\":\"" + projectCode + "\","
                + "\"signupDeadline\":\"" + signupDeadline.format(DATE_TIME_FORMATTER) + "\","
                + "\"status\":\"" + statusValue + "\","
                + "\"attachmentFileIds\":[" + attachmentFileId + "]"
                + "}";
        MvcResult result = mockMvc.perform(post("/api/admin/tenders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private Long findAttachmentId(String adminToken, Long tenderId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode attachments = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("attachments");
        if (attachments.isArray() && attachments.size() > 0) {
            return attachments.get(0).path("attachmentId").asLong();
        }
        return null;
    }

    private String createMemberAndLogin(String adminToken,
                                        String username,
                                        String uniqueTag,
                                        Long... businessTypeIds) throws Exception {
        String phoneSeed = uniqueTag.substring(Math.max(0, uniqueTag.length() - 8));
        String creditSeed = uniqueTag.substring(Math.max(0, uniqueTag.length() - 6));
        String joinedTypeIds = Arrays.stream(businessTypeIds)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String expiresAt = LocalDateTime.now().plusYears(1).format(DATE_TIME_FORMATTER);
        mockMvc.perform(post("/api/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"138" + phoneSeed + "\",\"email\":\"" + username + "@test.com\",\"companyName\":\"测试会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MA1K" + creditSeed + "\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\",\"businessTypeIds\":[" + joinedTypeIds + "],\"canDownloadFile\":false,\"status\":\"ENABLED\",\"expiresAt\":\"" + expiresAt + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        return loginMember(username, "123456");
    }

    private Long findMemberIdByUsername(String adminToken, String username) throws Exception {
        MvcResult memberListResult = mockMvc.perform(get("/api/admin/members")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode members = objectMapper.readTree(memberListResult.getResponse().getContentAsString()).path("data");
        for (JsonNode member : members) {
            if (username.equals(member.path("username").asText())) {
                return member.path("id").asLong();
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

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                builder.append(Character.forDigit((item >> 4) & 0xF, 16));
                builder.append(Character.forDigit(item & 0xF, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
