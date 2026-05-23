package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.common.PageResult;
import com.zhaobiao.admin.dto.news.NewsDto;
import com.zhaobiao.admin.dto.news.NewsListItemDto;
import com.zhaobiao.admin.dto.news.NewsUpsertRequest;
import com.zhaobiao.admin.entity.News;
import com.zhaobiao.admin.entity.NewsCategory;
import com.zhaobiao.admin.entity.NewsStatus;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.repository.NewsRepository;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import com.zhaobiao.admin.security.LoginUser;
import com.zhaobiao.admin.util.FileThumbnailUrlBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final TenderFileStorageRepository tenderFileStorageRepository;

    public NewsService(NewsRepository newsRepository,
                       TenderFileStorageRepository tenderFileStorageRepository) {
        this.newsRepository = newsRepository;
        this.tenderFileStorageRepository = tenderFileStorageRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<NewsListItemDto> listAdminNews(int pageNum,
                                                     int pageSize,
                                                     String keyword,
                                                     NewsCategory category,
                                                     NewsStatus status) {
        Pageable pageable = buildPageable(pageNum, pageSize);
        Page<News> page = newsRepository.searchAdmin(normalize(keyword), category, status, pageable);
        return toPageResult(page, pageNum, pageSize);
    }

    @Transactional(readOnly = true)
    public NewsDto getNews(Long newsId) {
        News news = newsRepository.findDetailById(newsId)
                .orElseThrow(() -> new BusinessException(404, "资讯不存在"));
        return toDto(news);
    }

    @Transactional
    public NewsDto createNews(NewsUpsertRequest request) {
        News news = new News();
        applyRequest(news, request, true);
        news = newsRepository.save(news);
        return getNews(news.getId());
    }

    @Transactional
    public NewsDto updateNews(Long newsId, NewsUpsertRequest request) {
        News news = newsRepository.findDetailById(newsId)
                .orElseThrow(() -> new BusinessException(404, "资讯不存在"));
        ensureEditable(news);
        ensureStatusNotChanged(news, request);
        applyRequest(news, request, false);
        news = newsRepository.save(news);
        return getNews(news.getId());
    }

    @Transactional
    public NewsDto updateNewsStatus(Long newsId, NewsStatus status) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException(404, "资讯不存在"));
        if (status == null) {
            throw new BusinessException(400, "状态不能为空");
        }
        if (news.getStatus() != status) {
            news.setStatus(status);
            news.setUpdatedBy(currentOperatorUsername());
            newsRepository.save(news);
        }
        return getNews(newsId);
    }

    @Transactional
    public void deleteNews(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException(404, "资讯不存在"));
        ensureEditable(news);
        newsRepository.delete(news);
    }

    @Transactional(readOnly = true)
    public PageResult<NewsListItemDto> listPortalNews(int pageNum,
                                                      int pageSize,
                                                      String keyword,
                                                      NewsCategory category) {
        Pageable pageable = buildPageable(pageNum, pageSize);
        Page<News> page = newsRepository.searchPortal(
                normalize(keyword),
                category,
                NewsStatus.PUBLISHED,
                LocalDateTime.now(),
                pageable
        );
        return toPageResult(page, pageNum, pageSize);
    }

    @Transactional(readOnly = true)
    public List<NewsListItemDto> listLatestNews(int limit) {
        int safeLimit = limit <= 0 ? 6 : Math.min(limit, 20);
        return newsRepository.findLatestPublished(
                        NewsStatus.PUBLISHED,
                        LocalDateTime.now(),
                        PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toListItemDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NewsDto getPortalNews(Long newsId) {
        News news = newsRepository.findPublicDetailById(
                        newsId,
                        NewsStatus.PUBLISHED,
                        LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(404, "资讯不存在"));
        return toDto(news);
    }

    private void applyRequest(News news, NewsUpsertRequest request, boolean creating) {
        String operator = currentOperatorUsername();
        news.setTitle(request.getTitle().trim());
        news.setCoverFile(loadCoverFile(request.getCoverFileId()));
        news.setContent(request.getContent());
        news.setPublishAt(request.getPublishAt());
        news.setSource(request.getSource().trim());
        news.setSummary(request.getSummary().trim());
        news.setCategory(request.getCategory());
        if (creating) {
            news.setStatus(NewsStatus.DRAFT);
            news.setCreatedBy(operator);
        }
        news.setUpdatedBy(operator);
    }

    private void ensureEditable(News news) {
        if (news.getStatus() == NewsStatus.PUBLISHED) {
            throw new BusinessException(400, "已发布资讯请先改为未发布后再操作");
        }
    }

    private void ensureStatusNotChanged(News news, NewsUpsertRequest request) {
        if (request.getStatus() != null && request.getStatus() != news.getStatus()) {
            throw new BusinessException(400, "资讯发布状态请通过发布状态接口调整");
        }
    }

    private TenderFileStorage loadCoverFile(Long fileId) {
        if (fileId == null) {
            return null;
        }
        return tenderFileStorageRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(400, "封面文件不存在"));
    }

    private Pageable buildPageable(int pageNum, int pageSize) {
        int safePageNum = pageNum <= 0 ? 1 : pageNum;
        int safePageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 50);
        return PageRequest.of(safePageNum - 1, safePageSize, Sort.by(Sort.Order.desc("publishAt"), Sort.Order.desc("id")));
    }

    private PageResult<NewsListItemDto> toPageResult(Page<News> page, int pageNum, int pageSize) {
        PageResult<NewsListItemDto> result = new PageResult<>();
        result.setPageNum(pageNum <= 0 ? 1 : pageNum);
        result.setPageSize(pageSize <= 0 ? 10 : Math.min(pageSize, 50));
        result.setTotal(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setList(page.getContent().stream().map(this::toListItemDto).collect(Collectors.toList()));
        return result;
    }

    private NewsDto toDto(News news) {
        NewsDto dto = new NewsDto();
        applyCommonFields(dto, news);
        dto.setContent(news.getContent());
        return dto;
    }

    private NewsListItemDto toListItemDto(News news) {
        NewsListItemDto dto = new NewsListItemDto();
        applyCommonFields(dto, news);
        return dto;
    }

    private void applyCommonFields(NewsListItemDto dto, News news) {
        TenderFileStorage coverFile = news.getCoverFile();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setCoverFileId(coverFile == null ? null : coverFile.getId());
        dto.setCoverUrl(coverFile == null ? null : FileThumbnailUrlBuilder.build(coverFile.getId()));
        dto.setPublishAt(news.getPublishAt());
        dto.setSource(news.getSource());
        dto.setSummary(news.getSummary());
        dto.setCategory(news.getCategory());
        dto.setCategoryLabel(news.getCategory() == null ? null : news.getCategory().getLabel());
        dto.setStatus(news.getStatus());
        dto.setStatusLabel(news.getStatus() == null ? null : news.getStatus().getLabel());
        dto.setCreatedBy(news.getCreatedBy());
        dto.setUpdatedBy(news.getUpdatedBy());
        dto.setCreatedAt(news.getCreatedAt());
        dto.setUpdatedAt(news.getUpdatedAt());
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String currentOperatorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUsername();
        }
        return "system";
    }
}
