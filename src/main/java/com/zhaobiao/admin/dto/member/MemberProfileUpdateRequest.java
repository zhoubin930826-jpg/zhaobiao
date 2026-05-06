package com.zhaobiao.admin.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(description = "门户会员资料更新请求")
public class MemberProfileUpdateRequest {

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "member@test.com")
    private String email;

    @Size(max = 128, message = "公司名称不能超过 128 位")
    @Schema(description = "公司名称", example = "某某科技有限公司")
    private String companyName;

    @Size(max = 64, message = "联系人不能超过 64 位")
    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @Pattern(regexp = "^[0-9A-Z]{18}$", message = "统一社会信用代码格式不正确")
    @Schema(description = "统一社会信用代码", example = "91310000MA1K123456")
    private String unifiedSocialCreditCode;

    @Size(max = 64, message = "真实姓名不能超过 64 位")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "营业执照文件ID，不传或传 null 保持原值")
    private Long businessLicenseFileId;

    @Schema(description = "三年内业绩文件ID，不传或传 null 保持原值")
    private Long threeYearPerformanceFileId;

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

    public Long getBusinessLicenseFileId() {
        return businessLicenseFileId;
    }

    public void setBusinessLicenseFileId(Long businessLicenseFileId) {
        this.businessLicenseFileId = businessLicenseFileId;
    }

    public Long getThreeYearPerformanceFileId() {
        return threeYearPerformanceFileId;
    }

    public void setThreeYearPerformanceFileId(Long threeYearPerformanceFileId) {
        this.threeYearPerformanceFileId = threeYearPerformanceFileId;
    }
}
