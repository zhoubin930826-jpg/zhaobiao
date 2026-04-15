package com.zhaobiao.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.hasSize;
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
class TenderIntegrationTests {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanUploadCreateTendersAndPortalListIsPagedInDescendingOrder() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());

        Long firstFileId = uploadFile(adminToken, "招标文件-" + uniqueTag + "-1.txt", "first tender file");
        Long secondFileId = uploadFile(adminToken, "招标文件-" + uniqueTag + "-2.txt", "second tender file");

        String keyword = "项目-" + uniqueTag;
        Long olderTenderId = createTender(adminToken,
                "项目-" + uniqueTag + "-A",
                "浙江",
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                "ZB-" + uniqueTag + "-A",
                firstFileId);
        Long newerTenderId = createTender(adminToken,
                "项目-" + uniqueTag + "-B",
                "浙江",
                LocalDateTime.now().minusHours(4),
                LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(7),
                "ZB-" + uniqueTag + "-B",
                secondFileId);
        createTender(adminToken,
                "项目-" + uniqueTag + "-CLOSED",
                "浙江",
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(7),
                "ZB-" + uniqueTag + "-CLOSED",
                secondFileId,
                "CLOSED");

        mockMvc.perform(get("/api/portal/tenders")
                        .param("keyword", keyword)
                        .param("pageNum", "1")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].id").value(newerTenderId))
                .andExpect(jsonPath("$.data.list[0].title").value("项目-" + uniqueTag + "-B"));

        mockMvc.perform(get("/api/portal/tenders")
                        .param("keyword", keyword)
                        .param("pageNum", "2")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value(olderTenderId))
                .andExpect(jsonPath("$.data.list[0].title").value("项目-" + uniqueTag + "-A"));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", newerTenderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(newerTenderId))
                .andExpect(jsonPath("$.data.canDownload").value(false))
                .andExpect(jsonPath("$.data.attachments", hasSize(1)))
                .andExpect(jsonPath("$.data.attachments[0].fileName").value("招标文件-" + uniqueTag + "-2.txt"));

        mockMvc.perform(get("/api/portal/tenders")
                        .param("keyword", "CLOSED")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void memberDownloadPermissionControlsAttachmentDownloadAndDisabledTokenBecomesInvalid() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        Long fileId = uploadFile(adminToken, "下载测试-" + uniqueTag + ".txt", "downloadable content");
        Long tenderId = createTender(adminToken,
                "下载测试项目-" + uniqueTag,
                "上海",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(1),
                "DL-" + uniqueTag,
                fileId);

        Long attachmentId = findAttachmentId(tenderId);
        assertNotNull(attachmentId);

        String memberUsername = "member" + uniqueTag;
        registerMember(memberUsername, uniqueTag);
        String memberToken = loginMember(memberUsername, "123456");

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownload").value(false));

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

        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId)
                        .header("Authorization", "Bearer " + memberToken))
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

    private void registerMember(String username, String uniqueTag) throws Exception {
        mockMvc.perform(post("/api/portal/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"phone\":\"139" + uniqueTag.substring(uniqueTag.length() - 8) + "\",\"email\":\"" + username + "@test.com\",\"companyName\":\"测试会员企业\",\"contactPerson\":\"李四\",\"unifiedSocialCreditCode\":\"91310000MA1K" + uniqueTag.substring(uniqueTag.length() - 6) + "\",\"realName\":\"李四\",\"password\":\"123456\",\"confirmPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
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
                        .header("Authorization", "Bearer " + adminToken))
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
                              LocalDateTime publishAt,
                              LocalDateTime deadline,
                              LocalDateTime signupDeadline,
                              String projectCode,
                              Long attachmentFileId) throws Exception {
        return createTender(adminToken, title, region, publishAt, deadline, signupDeadline, projectCode, attachmentFileId, "PUBLISHED");
    }

    private Long createTender(String adminToken,
                              String title,
                              String region,
                              LocalDateTime publishAt,
                              LocalDateTime deadline,
                              LocalDateTime signupDeadline,
                              String projectCode,
                              Long attachmentFileId,
                              String statusValue) throws Exception {
        String requestBody = "{"
                + "\"title\":\"" + title + "\","
                + "\"region\":\"" + region + "\","
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

    private Long findAttachmentId(Long tenderId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/portal/tenders/{tenderId}", tenderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode attachments = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("attachments");
        if (attachments.isArray() && attachments.size() > 0) {
            return attachments.get(0).path("attachmentId").asLong();
        }
        return null;
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
}
