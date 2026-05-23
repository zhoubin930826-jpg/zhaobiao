package com.zhaobiao.admin.dto.news;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "资讯详情")
public class NewsDto extends NewsListItemDto {

    @Schema(description = "正文，支持富文本 HTML")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
