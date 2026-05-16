package com.zhaobiao.admin.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Schema(description = "会员登录请求")
public class MemberLoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "member001")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "验证码标识不能为空")
    @Schema(description = "验证码标识", example = "login-uuid")
    private String captchaId;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "8K3D")
    private String captchaCode;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
}
