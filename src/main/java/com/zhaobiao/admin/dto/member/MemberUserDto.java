package com.zhaobiao.admin.dto.member;

import com.zhaobiao.admin.entity.MemberUserStatus;
import com.zhaobiao.admin.dto.business.BusinessTypeOptionDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "会员用户信息")
public class MemberUserDto {

    @Schema(description = "会员ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "统一社会信用代码")
    private String unifiedSocialCreditCode;

    @Schema(description = "是否允许下载文件")
    private boolean canDownloadFile;

    @Schema(description = "会员状态")
    private MemberUserStatus status;

    @Schema(description = "会员过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "会员是否已过期")
    private boolean expired;

    @Schema(description = "会员类型列表")
    private List<BusinessTypeOptionDto> businessTypes;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
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

    public boolean isCanDownloadFile() {
        return canDownloadFile;
    }

    public void setCanDownloadFile(boolean canDownloadFile) {
        this.canDownloadFile = canDownloadFile;
    }

    public MemberUserStatus getStatus() {
        return status;
    }

    public void setStatus(MemberUserStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public List<BusinessTypeOptionDto> getBusinessTypes() {
        return businessTypes;
    }

    public void setBusinessTypes(List<BusinessTypeOptionDto> businessTypes) {
        this.businessTypes = businessTypes;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
