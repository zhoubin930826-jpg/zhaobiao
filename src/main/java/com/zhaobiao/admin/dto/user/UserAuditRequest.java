package com.zhaobiao.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "用户审核请求")
public class UserAuditRequest {

    @NotNull(message = "审核结果不能为空")
    @Schema(description = "是否通过审核", example = "true")
    private Boolean approved;

    @Size(max = 255, message = "驳回原因不能超过 255 位")
    @Schema(description = "驳回原因，审核通过时可为空", example = "资料不完整，请补充企业信息")
    private String reason;

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
