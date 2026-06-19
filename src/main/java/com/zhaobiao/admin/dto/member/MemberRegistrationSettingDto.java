package com.zhaobiao.admin.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "会员注册开关")
public class MemberRegistrationSettingDto {

    @Schema(description = "是否开启门户会员自助注册", example = "true")
    private boolean registrationEnabled;

    public MemberRegistrationSettingDto() {
    }

    public MemberRegistrationSettingDto(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }
}
