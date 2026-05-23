package com.zhaobiao.admin.dto.news;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhaobiao.admin.entity.NewsCategory;
import com.zhaobiao.admin.entity.NewsStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "资讯新增/修改请求")
public class NewsUpsertRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过 200 位")
    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面文件ID")
    private Long coverFileId;

    @NotBlank(message = "正文不能为空")
    @Schema(description = "正文，支持富文本 HTML")
    private String content;

    @NotNull(message = "发布时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发布时间", example = "2026-05-23 09:00:00")
    private LocalDateTime publishAt;

    @NotBlank(message = "信息来源不能为空")
    @Size(max = 128, message = "信息来源不能超过 128 位")
    @Schema(description = "信息来源")
    private String source;

    @NotBlank(message = "内容总结不能为空")
    @Size(max = 500, message = "内容总结不能超过 500 位")
    @Schema(description = "内容总结")
    private String summary;

    @NotNull(message = "资讯分类不能为空")
    @Schema(description = "资讯分类")
    private NewsCategory category;

    @Schema(description = "资讯状态")
    private NewsStatus status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCoverFileId() {
        return coverFileId;
    }

    public void setCoverFileId(Long coverFileId) {
        this.coverFileId = coverFileId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(LocalDateTime publishAt) {
        this.publishAt = publishAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public NewsCategory getCategory() {
        return category;
    }

    public void setCategory(NewsCategory category) {
        this.category = category;
    }

    public NewsStatus getStatus() {
        return status;
    }

    public void setStatus(NewsStatus status) {
        this.status = status;
    }
}
