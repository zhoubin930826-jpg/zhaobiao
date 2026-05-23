package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.common.PageResult;
import com.zhaobiao.admin.dto.news.NewsDto;
import com.zhaobiao.admin.dto.news.NewsListItemDto;
import com.zhaobiao.admin.dto.news.NewsStatusUpdateRequest;
import com.zhaobiao.admin.dto.news.NewsUpsertRequest;
import com.zhaobiao.admin.entity.NewsCategory;
import com.zhaobiao.admin.entity.NewsStatus;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "管理员-资讯管理")
@RestController
@RequestMapping("/api/admin/news")
@PreAuthorize("hasRole('ADMIN')")
public class NewsAdminController {

    private final NewsService newsService;

    public NewsAdminController(NewsService newsService) {
        this.newsService = newsService;
    }

    @Operation(summary = "分页查询资讯列表")
    @PreAuthorize("hasAuthority('SYSTEM_NEWS')")
    @GetMapping
    public ApiResponse<PageResult<NewsListItemDto>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) NewsCategory category,
                                                         @RequestParam(required = false) NewsStatus status) {
        return ApiResponse.success(newsService.listAdminNews(pageNum, pageSize, keyword, category, status));
    }

    @Operation(summary = "查询资讯详情")
    @PreAuthorize("hasAuthority('SYSTEM_NEWS')")
    @GetMapping("/{newsId}")
    public ApiResponse<NewsDto> detail(@PathVariable Long newsId) {
        return ApiResponse.success(newsService.getNews(newsId));
    }

    @Operation(summary = "新增资讯")
    @PreAuthorize("hasAuthority('NEWS_CREATE_BUTTON')")
    @OperationLogRecord(module = "资讯管理", action = "新增资讯")
    @PostMapping
    public ApiResponse<NewsDto> create(@Valid @RequestBody NewsUpsertRequest request) {
        return ApiResponse.success(newsService.createNews(request));
    }

    @Operation(summary = "修改资讯")
    @PreAuthorize("hasAuthority('NEWS_EDIT_BUTTON')")
    @OperationLogRecord(module = "资讯管理", action = "修改资讯")
    @PutMapping("/{newsId}")
    public ApiResponse<NewsDto> update(@PathVariable Long newsId,
                                       @Valid @RequestBody NewsUpsertRequest request) {
        return ApiResponse.success(newsService.updateNews(newsId, request));
    }

    @Operation(summary = "发布或下架资讯")
    @PreAuthorize("hasAuthority('NEWS_PUBLISH_BUTTON')")
    @OperationLogRecord(module = "资讯管理", action = "发布/下架资讯")
    @PutMapping("/{newsId}/status")
    public ApiResponse<NewsDto> updateStatus(@PathVariable Long newsId,
                                             @Valid @RequestBody NewsStatusUpdateRequest request) {
        return ApiResponse.success(newsService.updateNewsStatus(newsId, request.getStatus()));
    }

    @Operation(summary = "删除资讯")
    @PreAuthorize("hasAuthority('NEWS_DELETE_BUTTON')")
    @OperationLogRecord(module = "资讯管理", action = "删除资讯")
    @DeleteMapping("/{newsId}")
    public ApiResponse<Void> delete(@PathVariable Long newsId) {
        newsService.deleteNews(newsId);
        return ApiResponse.success("删除成功", null);
    }
}
