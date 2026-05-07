package com.zhaobiao.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhaobiao.admin.config.DataInitializer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DataInitializer.class)
class FileThumbnailIntegrationTests {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadingImagePdfAndOtherFilesCreatesReadableThumbnails() throws Exception {
        String adminToken = loginAdmin();
        String tag = uniqueTag();

        JsonNode image = uploadFile(adminToken, "/api/admin/files/upload", "image-" + tag + ".png", "image/png", pngBytes());
        assertThumbnailFields(image, "READY");
        assertThumbnailCanBeReadWithoutToken(image.path("thumbnailUrl").asText());

        JsonNode pdf = uploadFile(adminToken, "/api/admin/files/upload", "document-" + tag + ".pdf", "application/pdf", pdfBytes());
        assertThumbnailFields(pdf, "READY");
        assertThumbnailCanBeReadWithoutToken(pdf.path("thumbnailUrl").asText());

        byte[] textContent = ("plain text thumbnail " + tag).getBytes(StandardCharsets.UTF_8);
        JsonNode text = uploadFile(adminToken, "/api/admin/files/upload", "notes-" + tag + ".txt", MediaType.TEXT_PLAIN_VALUE, textContent);
        assertThumbnailFields(text, "UNSUPPORTED");
        assertThumbnailCanBeReadWithoutToken(text.path("thumbnailUrl").asText());

        JsonNode duplicateText = uploadFile(adminToken, "/api/admin/files/upload", "notes-" + tag + ".txt", MediaType.TEXT_PLAIN_VALUE, textContent);
        assertEquals(text.path("fileId").asLong(), duplicateText.path("fileId").asLong());
        assertThumbnailFields(duplicateText, "UNSUPPORTED");
    }

    @Test
    void tenderAndMemberFileResponsesIncludeThumbnailFields() throws Exception {
        String adminToken = loginAdmin();
        String tag = uniqueTag();
        Long engineeringTypeId = findBusinessTypeIdByCode(adminToken, "ENGINEERING");
        assertNotNull(engineeringTypeId);

        JsonNode tenderFile = uploadFile(
                adminToken,
                "/api/admin/files/upload",
                "tender-" + tag + ".txt",
                MediaType.TEXT_PLAIN_VALUE,
                ("tender attachment " + tag).getBytes(StandardCharsets.UTF_8)
        );
        Long tenderFileId = tenderFile.path("fileId").asLong();
        Long tenderId = createTender(adminToken, tag, engineeringTypeId, tenderFileId);

        MvcResult adminTenderDetail = mockMvc.perform(get("/api/admin/tenders/{tenderId}", tenderId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.attachments[0].thumbnailUrl").value("/api/files/" + tenderFileId + "/thumbnail"))
                .andExpect(jsonPath("$.data.attachments[0].thumbnailStatus").value("UNSUPPORTED"))
                .andReturn();
        String adminAttachmentThumbnail = objectMapper.readTree(adminTenderDetail.getResponse().getContentAsString())
                .path("data")
                .path("attachments")
                .path(0)
                .path("thumbnailUrl")
                .asText();
        assertThumbnailCanBeReadWithoutToken(adminAttachmentThumbnail);

        mockMvc.perform(get("/api/portal/tenders/{tenderId}", tenderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.canDownload").value(false))
                .andExpect(jsonPath("$.data.attachments[0].thumbnailUrl").value("/api/files/" + tenderFileId + "/thumbnail"))
                .andExpect(jsonPath("$.data.attachments[0].thumbnailStatus").value("UNSUPPORTED"));

        JsonNode licenseFile = uploadFile(
                adminToken,
                "/api/admin/members/profile-files",
                "license-" + tag + ".png",
                "image/png",
                pngBytes()
        );
        Long licenseFileId = licenseFile.path("fileId").asLong();
        String username = "thumbmember" + tag;
        Long memberId = createMember(adminToken, username, tag, engineeringTypeId, licenseFileId);

        mockMvc.perform(get("/api/admin/members/{memberId}", memberId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.businessLicenseFileId").value(licenseFileId))
                .andExpect(jsonPath("$.data.businessLicenseThumbnailUrl").value("/api/files/" + licenseFileId + "/thumbnail"))
                .andExpect(jsonPath("$.data.businessLicenseThumbnailStatus").value("READY"));

        String memberToken = loginMember(username, "123456");
        mockMvc.perform(get("/api/portal/auth/me")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.businessLicenseFileId").value(licenseFileId))
                .andExpect(jsonPath("$.data.businessLicenseThumbnailUrl").value("/api/files/" + licenseFileId + "/thumbnail"))
                .andExpect(jsonPath("$.data.businessLicenseThumbnailStatus").value("READY"));
    }

    private JsonNode uploadFile(String token, String path, String fileName, String contentType, byte[] content) throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("files", fileName, contentType, content);
        MvcResult result = mockMvc.perform(multipart(path)
                        .file(multipartFile)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].thumbnailUrl").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path(0);
    }

    private void assertThumbnailFields(JsonNode file, String status) {
        assertTrue(file.path("fileId").asLong() > 0);
        assertEquals("/api/files/" + file.path("fileId").asLong() + "/thumbnail", file.path("thumbnailUrl").asText());
        assertEquals("image/jpeg", file.path("thumbnailContentType").asText());
        assertEquals(status, file.path("thumbnailStatus").asText());
    }

    private void assertThumbnailCanBeReadWithoutToken(String thumbnailUrl) throws Exception {
        MvcResult result = mockMvc.perform(get(thumbnailUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG))
                .andReturn();
        assertTrue(result.getResponse().getContentAsByteArray().length > 0);
    }

    private Long createTender(String adminToken, String tag, Long businessTypeId, Long attachmentFileId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/tenders")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"title\":\"缩略图测试项目-" + tag + "\","
                                + "\"region\":\"杭州\","
                                + "\"businessTypeId\":" + businessTypeId + ","
                                + "\"publishAt\":\"" + formatDateTime(LocalDateTime.now().minusHours(1)) + "\","
                                + "\"content\":\"<p>缩略图测试正文</p>\","
                                + "\"contactPerson\":\"张三\","
                                + "\"budget\":\"100 万元\","
                                + "\"contactPhone\":\"0571-88886666\","
                                + "\"tenderUnit\":\"测试招标单位\","
                                + "\"deadline\":\"" + formatDateTime(LocalDateTime.now().plusDays(10)) + "\","
                                + "\"projectCode\":\"THUMB-" + tag + "\","
                                + "\"signupDeadline\":\"" + formatDateTime(LocalDateTime.now().plusDays(3)) + "\","
                                + "\"status\":\"PUBLISHED\","
                                + "\"attachmentFileIds\":[" + attachmentFileId + "]"
                                + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private Long createMember(String adminToken,
                              String username,
                              String tag,
                              Long businessTypeId,
                              Long licenseFileId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/members")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\","
                                + "\"phone\":\"137" + phoneSeed(tag) + "\","
                                + "\"email\":\"" + username + "@test.com\","
                                + "\"companyName\":\"缩略图会员企业\","
                                + "\"contactPerson\":\"李四\","
                                + "\"unifiedSocialCreditCode\":\"91310000MT" + creditSeed(tag) + "00\","
                                + "\"realName\":\"李四\","
                                + "\"password\":\"123456\","
                                + "\"confirmPassword\":\"123456\","
                                + "\"businessTypeIds\":[" + businessTypeId + "],"
                                + "\"canDownloadFile\":false,"
                                + "\"status\":\"ENABLED\","
                                + "\"expiresAt\":\"" + formatDateTime(LocalDateTime.now().plusDays(30)) + "\","
                                + "\"businessLicenseFileId\":" + licenseFileId
                                + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private String loginAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"adminqwert\"}"))
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

    private Long findBusinessTypeIdByCode(String adminToken, String code) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/business-types/options")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        for (JsonNode item : data) {
            if (code.equals(item.path("code").asText())) {
                return item.path("id").asLong();
            }
        }
        return null;
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(80, 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(20, 120, 210));
            graphics.fillRect(0, 0, 80, 60);
            graphics.setColor(Color.WHITE);
            graphics.fillOval(20, 10, 40, 40);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private byte[] pdfBytes() throws Exception {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        }
    }

    private String formatDateTime(LocalDateTime value) {
        return value.format(DATE_TIME_FORMATTER);
    }

    private String uniqueTag() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String phoneSeed(String tag) {
        return tag.substring(Math.max(0, tag.length() - 8));
    }

    private String creditSeed(String tag) {
        return tag.substring(Math.max(0, tag.length() - 6));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
