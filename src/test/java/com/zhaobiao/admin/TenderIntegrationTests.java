package com.zhaobiao.admin;

import com.zhaobiao.admin.config.DataInitializer;
import com.zhaobiao.admin.entity.Menu;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.entity.UserStatus;
import com.zhaobiao.admin.repository.MenuRepository;
import com.zhaobiao.admin.repository.OperationLogRepository;
import com.zhaobiao.admin.repository.RoleRepository;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import com.zhaobiao.admin.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
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
class TenderIntegrationTests {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenderFileStorageRepository tenderFileStorageRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Test
    void anonymousCanViewLatestThreeAndPublicDetailButCannotDownloadAttachments() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        Long goodsTypeId = findBusinessTypeIdByCode(adminToken, "GOODS");
        assertNotNull(engineeringTypeId);
        assertNotNull(goodsTypeId);

        Long fileId = uploadFile(adminToken, "公开附件-" + uniqueTag + ".txt", "public file content");
        Long oldestTenderId = createTender(adminToken,
                "公开最新三条-" + uniqueTag + "-1",
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().minusSeconds(4),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                "PUBLIC-" + uniqueTag + "-1",
                fileId);
        Long secondTenderId = createTender(adminToken,
                "公开最新三条-" + uniqueTag + "-2",
                "浙江",
                goodsTypeId,
                LocalDateTime.now().minusSeconds(3),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                "PUBLIC-" + uniqueTag + "-2",
                fileId);
        Long thirdTenderId = createTender(adminToken,
                "公开最新三条-" + uniqueTag + "-3",
                "浙江",
                goodsTypeId,
                LocalDateTime.now().minusSeconds(2),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                "PUBLIC-" + uniqueTag + "-3",
                fileId);
        Long newestTenderId = createTender(adminToken,
                "公开最新三条-" + uniqueTag + "-4",
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                "PUBLIC-" + uniqueTag + "-4",
                fileId);
        createTender(adminToken,
                "公开最新三条-" + uniqueTag + "-future",
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                "PUBLIC-" + uniqueTag + "-future",
                fileId);
        createTender(adminToken,
                "公开最新三条-" + uniqueTag + "-closed",
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                "PUBLIC-" + uniqueTag + "-closed",
                fileId,
                "CLOSED");

        mockMvc.perform(get("/api/portal/tenders/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].id").value(newestTenderId))
                .andExpect(jsonPath("$.data[1].id").value(thirdTenderId))
                .andExpect(jsonPath("$.data[2].id").value(secondTenderId));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", oldestTenderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(oldestTenderId))
                .andExpect(jsonPath("$.data.canDownload").value(false))
                .andExpect(jsonPath("$.data.attachments", hasSize(1)));

        Long attachmentId = findAttachmentId(adminToken, oldestTenderId);
        assertNotNull(attachmentId);
        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", oldestTenderId, attachmentId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

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

        mockMvc.perform(get("/api/portal/tenders")
                        .header("Authorization", "Bearer " + memberToken)
                        .param("keyword", keyword)
                        .param("businessTypeName", "货物")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(newerTenderId))
                .andExpect(jsonPath("$.data.list[0].businessType.code").value("GOODS"));

        mockMvc.perform(get("/api/portal/tenders")
                        .header("Authorization", "Bearer " + memberToken)
                        .param("keyword", keyword)
                        .param("businessTypeName", "工程")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(olderTenderId))
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
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(goodsTenderId))
                .andExpect(jsonPath("$.data.canDownload").value(false));

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

        Long goodsAttachmentId = findAttachmentId(adminToken, goodsTenderId);
        assertNotNull(goodsAttachmentId);
        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", goodsTenderId, goodsAttachmentId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(put("/api/admin/members/{memberId}/status", memberId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownload").value(false));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void adminCanDownloadAndViewStoredFilesAndPortalDownloadUsesPermissions() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);

        Long fileId = uploadFile(adminToken, "查看测试-" + uniqueTag + ".txt", "inline view content");
        Long newsId = createNewsWithCover(adminToken, fileId, uniqueTag);

        long adminFileDownloadLogsBefore = countOperationLogs("文件管理", "下载文件", true);
        mockMvc.perform(get("/api/admin/files/{fileId}/download", fileId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().string("inline view content"));
        assertEquals(adminFileDownloadLogsBefore + 1, countOperationLogs("文件管理", "下载文件", true));

        mockMvc.perform(get("/api/admin/files/{fileId}/view", fileId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")))
                .andExpect(content().string("inline view content"));

        Long tenderId = createTender(adminToken,
                "查看测试项目-" + uniqueTag,
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(1),
                "VIEW-" + uniqueTag,
                fileId);
        Long attachmentId = findAttachmentId(adminToken, tenderId);
        assertNotNull(attachmentId);

        String memberUsername = "viewmember" + uniqueTag;
        String memberToken = createMemberAndLogin(adminToken, memberUsername, uniqueTag, engineeringTypeId);

        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        Long memberId = findMemberIdByUsername(adminToken, memberUsername);
        assertNotNull(memberId);
        mockMvc.perform(put("/api/admin/members/{memberId}/download-access", memberId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canDownloadFile\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        long portalAttachmentDownloadLogsBefore = countOperationLogs("门户招标", "会员下载招标附件", true);
        mockMvc.perform(get("/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download", tenderId, attachmentId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().string("inline view content"));
        assertEquals(portalAttachmentDownloadLogsBefore + 1, countOperationLogs("门户招标", "会员下载招标附件", true));

        mockMvc.perform(put("/api/admin/tenders/{tenderId}/status", tenderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DRAFT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertTrue(tenderFileStorageRepository.findById(fileId).isPresent());
        mockMvc.perform(get("/api/admin/files/{fileId}/view", fileId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")))
                .andExpect(content().string("inline view content"));

        mockMvc.perform(delete("/api/admin/news/{newsId}", newsId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
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

    @Test
    void deletingTenderAttachmentDoesNotDeleteFileReferencedByMemberProfile() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);

        Long sharedFileId = uploadFile(adminToken, "会员资料共用-" + uniqueTag + ".txt", "shared profile file");
        Long tenderId = createTender(adminToken,
                "会员资料共用项目-" + uniqueTag,
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(1),
                "SHARED-" + uniqueTag,
                sharedFileId,
                "DRAFT");
        Long attachmentId = findAttachmentId(adminToken, tenderId);
        assertNotNull(attachmentId);

        createMember(adminToken, "profilefile" + uniqueTag, uniqueTag, sharedFileId, null, engineeringTypeId);

        mockMvc.perform(delete("/api/admin/tenders/{tenderId}/attachments/{attachmentId}", tenderId, attachmentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertTrue(tenderFileStorageRepository.findById(sharedFileId).isPresent());
    }

    @Test
    void tenderCreateDefaultsToDraftAndPublishStatusRequiresPublishAuthority() throws Exception {
        String superAdminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        Long engineeringTypeId = findBusinessTypeIdByCode(superAdminToken, "ENGINEERING");
        Long fileId = uploadFile(superAdminToken, "默认未发布-" + uniqueTag + ".txt", "draft tender file");

        MvcResult createResult = mockMvc.perform(post("/api/admin/tenders")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tenderBody(
                                "默认未发布-" + uniqueTag,
                                "浙江",
                                engineeringTypeId,
                                LocalDateTime.now().minusMinutes(1),
                                LocalDateTime.now().plusDays(10),
                                LocalDateTime.now().plusDays(5),
                                "DRAFT-" + uniqueTag,
                                fileId,
                                "PUBLISHED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();
        Long tenderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        String editorUsername = "tendereditor" + uniqueTag.substring(Math.max(0, uniqueTag.length() - 8));
        String editorToken = createAdminWithMenus(
                editorUsername,
                "TENDER_EDITOR_" + uniqueTag,
                "SYSTEM_TENDER",
                "TENDER_CREATE_BUTTON",
                "TENDER_EDIT_BUTTON",
                "TENDER_UPLOAD_BUTTON");

        mockMvc.perform(put("/api/admin/tenders/{tenderId}/status", tenderId)
                        .header("Authorization", "Bearer " + editorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(put("/api/admin/tenders/{tenderId}/status", tenderId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        mockMvc.perform(put("/api/admin/tenders/{tenderId}/status", tenderId)
                        .header("Authorization", "Bearer " + editorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DRAFT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void publishedTenderCannotBeMutatedUntilUnpublishedByPublisher() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String uniqueTag = String.valueOf(System.currentTimeMillis());
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        Long fileId = uploadFile(adminToken, "已发布锁定-" + uniqueTag + ".txt", "published tender file");
        Long extraFileId = uploadFile(adminToken, "已发布追加-" + uniqueTag + ".txt", "extra tender file");
        Long tenderId = createTender(adminToken,
                "已发布锁定-" + uniqueTag,
                "浙江",
                engineeringTypeId,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(5),
                "LOCK-" + uniqueTag,
                fileId,
                "PUBLISHED");
        Long attachmentId = findAttachmentId(adminToken, tenderId);

        mockMvc.perform(put("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tenderBody(
                                "已发布锁定-修改-" + uniqueTag,
                                "浙江",
                                engineeringTypeId,
                                LocalDateTime.now().minusMinutes(1),
                                LocalDateTime.now().plusDays(10),
                                LocalDateTime.now().plusDays(5),
                                "LOCK-" + uniqueTag,
                                fileId,
                                "PUBLISHED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(post("/api/admin/tenders/{tenderId}/attachments", tenderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileIds\":[" + extraFileId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(delete("/api/admin/tenders/{tenderId}/attachments/{attachmentId}", tenderId, attachmentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(delete("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(put("/api/admin/tenders/{tenderId}/status", tenderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DRAFT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(put("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tenderBody(
                                "已发布锁定-修改-" + uniqueTag,
                                "浙江",
                                engineeringTypeId,
                                LocalDateTime.now().minusMinutes(1),
                                LocalDateTime.now().plusDays(10),
                                LocalDateTime.now().plusDays(5),
                                "LOCK-" + uniqueTag,
                                fileId,
                                "DRAFT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("已发布锁定-修改-" + uniqueTag));
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
        String requestBody = tenderBody(title, region, businessTypeId, publishAt, deadline, signupDeadline, projectCode, attachmentFileId, statusValue);
        MvcResult result = mockMvc.perform(post("/api/admin/tenders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        Long tenderId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
        if ("PUBLISHED".equals(statusValue)) {
            mockMvc.perform(put("/api/admin/tenders/{tenderId}/status", tenderId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"PUBLISHED\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
        }
        return tenderId;
    }

    private String createAdminWithMenus(String username, String roleCode, String... menuCodes) throws Exception {
        Role role = new Role();
        role.setCode(roleCode);
        role.setName(roleCode);
        role.setDescription("测试招标权限角色");
        role.setBuiltIn(false);
        LinkedHashSet<Menu> menus = new LinkedHashSet<>();
        for (String menuCode : menuCodes) {
            menus.add(menuRepository.findByCode(menuCode).orElseThrow(AssertionError::new));
        }
        role.setMenus(menus);
        role = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setUsername(username);
        user.setPhone("139" + username.substring(Math.max(0, username.length() - 8)));
        user.setEmail(username + "@zhaobiao.test");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setStatus(UserStatus.APPROVED);
        user.setRealName("招标权限测试管理员");
        user.setCompanyName("权限测试单位");
        user.setContactPerson("权限测试管理员");
        user.setUnifiedSocialCreditCode("91310000T" + username.substring(Math.max(0, username.length() - 9)));
        LinkedHashSet<Role> userRoles = new LinkedHashSet<>();
        userRoles.add(role);
        user.setRoles(userRoles);
        userRepository.saveAndFlush(user);
        return loginAdmin(username, "12345678");
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

    private Long createNewsWithCover(String adminToken, Long coverFileId, String uniqueTag) throws Exception {
        String requestBody = "{"
                + "\"title\":\"文件引用资讯-" + uniqueTag + "\","
                + "\"coverFileId\":" + coverFileId + ","
                + "\"content\":\"<p>文件引用资讯正文</p>\","
                + "\"publishAt\":\"" + LocalDateTime.now().minusHours(1).format(DATE_TIME_FORMATTER) + "\","
                + "\"source\":\"平台综合管理部\","
                + "\"summary\":\"文件引用资讯摘要\","
                + "\"category\":\"PLATFORM_NOTICE\","
                + "\"status\":\"PUBLISHED\""
                + "}";
        MvcResult result = mockMvc.perform(post("/api/admin/news")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private String createMemberAndLogin(String adminToken,
                                        String username,
                                        String uniqueTag,
                                        Long... businessTypeIds) throws Exception {
        createMember(adminToken, username, uniqueTag, null, null, businessTypeIds);
        return loginMember(username, "123456");
    }

    private Long createMember(String adminToken,
                              String username,
                              String uniqueTag,
                              Long businessLicenseFileId,
                              Long threeYearPerformanceFileId,
                              Long... businessTypeIds) throws Exception {
        String phoneSeed = uniqueTag.substring(Math.max(0, uniqueTag.length() - 8));
        String creditSeed = uniqueTag.substring(Math.max(0, uniqueTag.length() - 6));
        String joinedTypeIds = Arrays.stream(businessTypeIds)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String expiresAt = LocalDateTime.now().plusYears(1).format(DATE_TIME_FORMATTER);
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{\"username\":\"").append(username).append("\",")
                .append("\"phone\":\"138").append(phoneSeed).append("\",")
                .append("\"email\":\"").append(username).append("@test.com\",")
                .append("\"companyName\":\"测试会员企业\",")
                .append("\"contactPerson\":\"李四\",")
                .append("\"unifiedSocialCreditCode\":\"91310000MA1K").append(creditSeed).append("\",")
                .append("\"realName\":\"李四\",")
                .append("\"password\":\"123456\",")
                .append("\"confirmPassword\":\"123456\",")
                .append("\"businessTypeIds\":[").append(joinedTypeIds).append("],")
                .append("\"canDownloadFile\":false,")
                .append("\"status\":\"ENABLED\",")
                .append("\"expiresAt\":\"").append(expiresAt).append("\"");
        if (businessLicenseFileId != null) {
            requestBody.append(",\"businessLicenseFileId\":").append(businessLicenseFileId);
        }
        if (threeYearPerformanceFileId != null) {
            requestBody.append(",\"threeYearPerformanceFileId\":").append(threeYearPerformanceFileId);
        }
        requestBody.append("}");
        MvcResult result = mockMvc.perform(post("/api/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
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

    private long countOperationLogs(String module, String action, boolean success) {
        return operationLogRepository.findAll().stream()
                .filter(log -> module.equals(log.getModule()))
                .filter(log -> action.equals(log.getAction()))
                .filter(log -> log.isSuccess() == success)
                .count();
    }
}
