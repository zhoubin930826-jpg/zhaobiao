package com.zhaobiao.admin.dto.news;

import com.zhaobiao.admin.entity.NewsStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "资讯发布状态更新请求")
public class NewsStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    @Schema(description = "资讯状态")
    private NewsStatus status;

    public NewsStatus getStatus() {
        return status;
    }

    public void setStatus(NewsStatus status) {
        this.status = status;
    }
}
