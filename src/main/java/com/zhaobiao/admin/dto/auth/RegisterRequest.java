package com.zhaobiao.admin.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(description = "注册请求")
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 64, message = "用户名长度需在 4-64 位之间")
    @Schema(description = "用户名", example = "user001")
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "demo@company.com")
    private String email;

    @NotBlank(message = "公司名称不能为空")
    @Size(max = 128, message = "公司名称不能超过 128 位")
    @Schema(description = "公司名称", example = "某某科技有限公司")
    private String companyName;

    @NotBlank(message = "联系人不能为空")
    @Size(max = 64, message = "联系人不能超过 64 位")
    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @NotBlank(message = "统一社会信用代码不能为空")
    @Pattern(regexp = "^[0-9A-Z]{18}$", message = "统一社会信用代码格式不正确")
    @Schema(description = "统一社会信用代码", example = "91310000MA1K123456")
    private String unifiedSocialCreditCode;

    @Size(max = 64, message = "真实姓名不能超过 64 位")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "123456")
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getUnifiedSocialCreditCode() {
        return unifiedSocialCreditCode;
    }

    public void setUnifiedSocialCreditCode(String unifiedSocialCreditCode) {
        this.unifiedSocialCreditCode = unifiedSocialCreditCode;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
