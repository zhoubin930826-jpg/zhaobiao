package com.zhaobiao.admin.dto.business;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "业务类型状态修改请求")
public class BusinessTypeStatusUpdateRequest {

    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用", example = "false")
    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
