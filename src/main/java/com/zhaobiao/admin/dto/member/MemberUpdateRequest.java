package com.zhaobiao.admin.dto.member;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "后台修改会员请求")
public class MemberUpdateRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "member@test.com")
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

    @NotEmpty(message = "会员类型至少选择一个")
    @Schema(description = "会员类型ID列表")
    private List<Long> businessTypeIds;

    @NotNull(message = "会员过期时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "会员过期时间", example = "2026-05-25 23:59:59")
    private LocalDateTime expiresAt;

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

    public List<Long> getBusinessTypeIds() {
        return businessTypeIds;
    }

    public void setBusinessTypeIds(List<Long> businessTypeIds) {
        this.businessTypeIds = businessTypeIds;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
