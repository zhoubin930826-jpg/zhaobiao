package com.zhaobiao.admin.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "会员注册开关更新请求")
public class MemberRegistrationSettingUpdateRequest {

    @NotNull(message = "是否开启会员注册不能为空")
    @Schema(description = "是否开启门户会员自助注册", example = "false", required = true)
    private Boolean registrationEnabled;

    public Boolean getRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(Boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }
}
