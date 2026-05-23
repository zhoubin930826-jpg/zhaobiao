package com.zhaobiao.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhaobiao.admin.config.DataInitializer;
import com.zhaobiao.admin.entity.Menu;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.entity.UserStatus;
import com.zhaobiao.admin.repository.MenuRepository;
import com.zhaobiao.admin.repository.RoleRepository;
import com.zhaobiao.admin.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DataInitializer.class)
class NewsIntegrationTests {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCanCreateListUpdateAndDeleteNews() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String tag = uniqueTag();
        Long coverFileId = uploadFile(adminToken, "news-cover-" + tag + ".jpg", "fake image " + tag);
        assertNotNull(coverFileId);

        MvcResult createResult = mockMvc.perform(post("/api/admin/news")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody(
                                "平台维护公告-" + tag,
                                coverFileId,
                                "<p>维护正文 " + tag + "</p>",
                                LocalDateTime.now().minusHours(1),
                                "平台运维中心",
                                "平台将进行维护 " + tag,
                                "PLATFORM_NOTICE",
                                "PUBLISHED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("平台维护公告-" + tag))
                .andExpect(jsonPath("$.data.coverFileId").value(coverFileId))
                .andExpect(jsonPath("$.data.coverUrl").value("/api/files/" + coverFileId + "/thumbnail"))
                .andExpect(jsonPath("$.data.category").value("PLATFORM_NOTICE"))
                .andExpect(jsonPath("$.data.categoryLabel").value("平台公告"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.statusLabel").value("草稿"))
                .andReturn();
        Long newsId = dataId(createResult);

        mockMvc.perform(get("/api/admin/news")
                        .header("Authorization", bearer(adminToken))
                        .param("keyword", tag)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].id").value(newsId))
                .andExpect(jsonPath("$.data.list[0].content").doesNotExist());

        mockMvc.perform(get("/api/admin/news/{newsId}", newsId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(newsId))
                .andExpect(jsonPath("$.data.content").value("<p>维护正文 " + tag + "</p>"));

        mockMvc.perform(put("/api/admin/news/{newsId}", newsId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody(
                                "行业动态-" + tag,
                                coverFileId,
                                "<p>行业正文 " + tag + "</p>",
                                LocalDateTime.now().minusMinutes(30),
                                "行业观察组",
                                "行业动态摘要 " + tag,
                                "INDUSTRY_NEWS",
                                "DRAFT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("行业动态-" + tag))
                .andExpect(jsonPath("$.data.categoryLabel").value("行业动态"))
                .andExpect(jsonPath("$.data.statusLabel").value("草稿"));

        mockMvc.perform(delete("/api/admin/news/{newsId}", newsId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/admin/news/{newsId}", newsId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void newsCreateDefaultsToDraftAndPublishStatusRequiresPublishAuthority() throws Exception {
        String superAdminToken = loginAdmin("admin", "adminqwert");
        String tag = uniqueTag();
        Long coverFileId = uploadFile(superAdminToken, "news-publish-" + tag + ".jpg", "fake image " + tag);

        MvcResult createResult = mockMvc.perform(post("/api/admin/news")
                        .header("Authorization", bearer(superAdminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody(
                                "资讯默认草稿-" + tag,
                                coverFileId,
                                "<p>资讯默认草稿正文 " + tag + "</p>",
                                LocalDateTime.now().minusHours(1),
                                "平台运维中心",
                                "资讯默认草稿摘要 " + tag,
                                "PLATFORM_NOTICE",
                                "PUBLISHED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();
        Long newsId = dataId(createResult);

        mockMvc.perform(put("/api/admin/news/{newsId}", newsId)
                        .header("Authorization", bearer(superAdminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody(
                                "资讯普通编辑改发布-" + tag,
                                coverFileId,
                                "<p>资讯普通编辑改发布正文 " + tag + "</p>",
                                LocalDateTime.now().minusMinutes(30),
                                "平台运维中心",
                                "资讯普通编辑改发布摘要 " + tag,
                                "PLATFORM_NOTICE",
                                "PUBLISHED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        String editorUsername = "newseditor" + tag.substring(Math.max(0, tag.length() - 8));
        String editorToken = createAdminWithMenus(
                editorUsername,
                "NEWS_EDITOR_" + tag.substring(Math.max(0, tag.length() - 12)),
                "SYSTEM_NEWS",
                "NEWS_CREATE_BUTTON",
                "NEWS_EDIT_BUTTON",
                "NEWS_DELETE_BUTTON",
                "NEWS_UPLOAD_BUTTON");

        mockMvc.perform(put("/api/admin/news/{newsId}/status", newsId)
                        .header("Authorization", bearer(editorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(put("/api/admin/news/{newsId}/status", newsId)
                        .header("Authorization", bearer(superAdminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        mockMvc.perform(put("/api/admin/news/{newsId}/status", newsId)
                        .header("Authorization", bearer(editorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DRAFT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void publishedNewsCannotBeMutatedUntilUnpublishedByPublisher() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String tag = uniqueTag();
        Long coverFileId = uploadFile(adminToken, "published-news-lock-" + tag + ".jpg", "fake image " + tag);
        Long newsId = createNews(adminToken,
                "已发布资讯锁定-" + tag,
                coverFileId,
                LocalDateTime.now().minusHours(1),
                "INDUSTRY_NEWS",
                "PUBLISHED");

        mockMvc.perform(put("/api/admin/news/{newsId}", newsId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody(
                                "已发布资讯锁定-修改-" + tag,
                                coverFileId,
                                "<p>已发布资讯锁定修改正文 " + tag + "</p>",
                                LocalDateTime.now().minusMinutes(30),
                                "行业观察组",
                                "已发布资讯锁定修改摘要 " + tag,
                                "INDUSTRY_NEWS",
                                "PUBLISHED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(delete("/api/admin/news/{newsId}", newsId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(put("/api/admin/news/{newsId}/status", newsId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DRAFT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(put("/api/admin/news/{newsId}", newsId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody(
                                "已发布资讯锁定-修改-" + tag,
                                coverFileId,
                                "<p>已发布资讯锁定修改正文 " + tag + "</p>",
                                LocalDateTime.now().minusMinutes(30),
                                "行业观察组",
                                "已发布资讯锁定修改摘要 " + tag,
                                "INDUSTRY_NEWS",
                                "DRAFT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("已发布资讯锁定-修改-" + tag));
    }

    @Test
    void portalOnlyShowsPublishedCurrentNewsAndRejectsInvalidCoverFile() throws Exception {
        String adminToken = loginAdmin("admin", "adminqwert");
        String tag = uniqueTag();
        Long coverFileId = uploadFile(adminToken, "portal-news-cover-" + tag + ".jpg", "fake image " + tag);
        assertNotNull(coverFileId);

        Long publishedId = createNews(adminToken,
                "门户可见资讯-" + tag,
                coverFileId,
                LocalDateTime.now().minusHours(2),
                "SERVICE_GUIDE",
                "PUBLISHED");
        Long draftId = createNews(adminToken,
                "门户草稿资讯-" + tag,
                coverFileId,
                LocalDateTime.now().minusHours(1),
                "POLICY_REGULATION",
                "DRAFT");
        Long futureId = createNews(adminToken,
                "门户未来资讯-" + tag,
                coverFileId,
                LocalDateTime.now().plusDays(1),
                "PLATFORM_NOTICE",
                "PUBLISHED");

        mockMvc.perform(get("/api/portal/news")
                        .param("keyword", tag)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].id").value(publishedId))
                .andExpect(jsonPath("$.data.list[0].content").doesNotExist());

        mockMvc.perform(get("/api/portal/news/latest")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[?(@.id==" + publishedId + ")]", not(empty())))
                .andExpect(jsonPath("$.data[?(@.id==" + draftId + ")]").isEmpty())
                .andExpect(jsonPath("$.data[?(@.id==" + futureId + ")]").isEmpty());

        mockMvc.perform(get("/api/portal/news/{newsId}", publishedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(publishedId))
                .andExpect(jsonPath("$.data.content").value("<p>资讯正文 门户可见资讯-" + tag + "</p>"));

        mockMvc.perform(get("/api/portal/news/{newsId}", draftId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(get("/api/portal/news/{newsId}", futureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(post("/api/admin/news")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody(
                                "无效封面-" + tag,
                                999999999L,
                                "<p>无效封面正文</p>",
                                LocalDateTime.now(),
                                "平台运维中心",
                                "无效封面摘要",
                                "PLATFORM_NOTICE",
                                "PUBLISHED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    private Long createNews(String adminToken,
                            String title,
                            Long coverFileId,
                            LocalDateTime publishAt,
                            String category,
                            String statusValue) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/news")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody(
                                title,
                                coverFileId,
                                "<p>资讯正文 " + title + "</p>",
                                publishAt,
                                "平台综合管理部",
                                "资讯摘要 " + title,
                                category,
                                statusValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        Long newsId = dataId(result);
        if ("PUBLISHED".equals(statusValue)) {
            mockMvc.perform(put("/api/admin/news/{newsId}/status", newsId)
                            .header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"PUBLISHED\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
        }
        return newsId;
    }

    private Long uploadFile(String adminToken, String fileName, String content) throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "files",
                fileName,
                MediaType.IMAGE_JPEG_VALUE,
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

    private String loginAdmin(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    private Long dataId(MvcResult result) throws Exception {
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("id").asLong();
    }

    private String createAdminWithMenus(String username, String roleCode, String... menuCodes) throws Exception {
        Role role = new Role();
        role.setCode(roleCode);
        role.setName(roleCode);
        role.setDescription("测试资讯权限角色");
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
        user.setRealName("资讯权限测试管理员");
        user.setCompanyName("权限测试单位");
        user.setContactPerson("资讯权限测试管理员");
        user.setUnifiedSocialCreditCode("91310000N" + username.substring(Math.max(0, username.length() - 9)));
        LinkedHashSet<Role> userRoles = new LinkedHashSet<>();
        userRoles.add(role);
        user.setRoles(userRoles);
        userRepository.saveAndFlush(user);
        return loginAdmin(username, "12345678");
    }

    private String newsBody(String title,
                            Long coverFileId,
                            String content,
                            LocalDateTime publishAt,
                            String source,
                            String summary,
                            String category,
                            String statusValue) {
        return "{"
                + "\"title\":\"" + title + "\","
                + "\"coverFileId\":" + coverFileId + ","
                + "\"content\":\"" + content + "\","
                + "\"publishAt\":\"" + publishAt.format(DATE_TIME_FORMATTER) + "\","
                + "\"source\":\"" + source + "\","
                + "\"summary\":\"" + summary + "\","
                + "\"category\":\"" + category + "\","
                + "\"status\":\"" + statusValue + "\""
                + "}";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String uniqueTag() {
        return String.valueOf(System.currentTimeMillis()) + System.nanoTime();
    }
}
