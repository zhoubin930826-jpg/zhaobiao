package com.zhaobiao.admin.dto.user;

import com.zhaobiao.admin.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "管理员状态修改请求")
public class AdminUserStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    @Schema(description = "管理员状态，仅支持 APPROVED 或 DISABLED", example = "DISABLED")
    private UserStatus status;

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
