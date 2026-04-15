package com.zhaobiao.admin.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "会员下载权限修改请求")
public class MemberDownloadAccessUpdateRequest {

    @NotNull(message = "下载权限不能为空")
    @Schema(description = "是否允许下载文件", example = "true")
    private Boolean canDownloadFile;

    public Boolean getCanDownloadFile() {
        return canDownloadFile;
    }

    public void setCanDownloadFile(Boolean canDownloadFile) {
        this.canDownloadFile = canDownloadFile;
    }
}
