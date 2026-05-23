package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.common.PageResult;
import com.zhaobiao.admin.dto.news.NewsDto;
import com.zhaobiao.admin.dto.news.NewsListItemDto;
import com.zhaobiao.admin.entity.NewsCategory;
import com.zhaobiao.admin.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "门户-资讯")
@RestController
@RequestMapping("/api/portal/news")
public class PortalNewsController {

    private final NewsService newsService;

    public PortalNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @Operation(summary = "分页查询门户资讯")
    @GetMapping
    public ApiResponse<PageResult<NewsListItemDto>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) NewsCategory category) {
        return ApiResponse.success(newsService.listPortalNews(pageNum, pageSize, keyword, category));
    }

    @Operation(summary = "查询最新门户资讯")
    @GetMapping("/latest")
    public ApiResponse<List<NewsListItemDto>> latest(@RequestParam(defaultValue = "6") int limit) {
        return ApiResponse.success(newsService.listLatestNews(limit));
    }

    @Operation(summary = "查询门户资讯详情")
    @GetMapping("/{newsId}")
    public ApiResponse<NewsDto> detail(@PathVariable Long newsId) {
        return ApiResponse.success(newsService.getPortalNews(newsId));
    }
}
