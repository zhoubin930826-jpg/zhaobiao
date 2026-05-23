package com.zhaobiao.admin.dto.tender;

import com.zhaobiao.admin.entity.TenderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "招标发布状态修改请求")
public class TenderStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态，仅支持 DRAFT 或 PUBLISHED", example = "PUBLISHED")
    private TenderStatus status;

    public TenderStatus getStatus() {
        return status;
    }

    public void setStatus(TenderStatus status) {
        this.status = status;
    }
}
