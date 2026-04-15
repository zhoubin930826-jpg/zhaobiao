package com.zhaobiao.admin.dto.member;

import com.zhaobiao.admin.entity.MemberUserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "会员状态修改请求")
public class MemberStatusUpdateRequest {

    @NotNull(message = "会员状态不能为空")
    @Schema(description = "会员状态", example = "DISABLED")
    private MemberUserStatus status;

    public MemberUserStatus getStatus() {
        return status;
    }

    public void setStatus(MemberUserStatus status) {
        this.status = status;
    }
}
