package com.zhaobiao.admin.dto.member;

import com.zhaobiao.admin.entity.MemberUserStatus;
import com.zhaobiao.admin.entity.FileThumbnailStatus;
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

    @Schema(description = "首次登录时间")
    private LocalDateTime firstLoginAt;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "营业执照文件ID")
    private Long businessLicenseFileId;

    @Schema(description = "营业执照文件名")
    private String businessLicenseFileName;

    @Schema(description = "营业执照文件类型")
    private String businessLicenseContentType;

    @Schema(description = "营业执照文件大小，单位字节")
    private Long businessLicenseFileSize;

    @Schema(description = "营业执照缩略图地址")
    private String businessLicenseThumbnailUrl;

    @Schema(description = "营业执照缩略图类型")
    private String businessLicenseThumbnailContentType;

    @Schema(description = "营业执照缩略图状态")
    private FileThumbnailStatus businessLicenseThumbnailStatus;

    @Schema(description = "三年内业绩文件ID")
    private Long threeYearPerformanceFileId;

    @Schema(description = "三年内业绩文件名")
    private String threeYearPerformanceFileName;

    @Schema(description = "三年内业绩文件类型")
    private String threeYearPerformanceContentType;

    @Schema(description = "三年内业绩文件大小，单位字节")
    private Long threeYearPerformanceFileSize;

    @Schema(description = "三年内业绩缩略图地址")
    private String threeYearPerformanceThumbnailUrl;

    @Schema(description = "三年内业绩缩略图类型")
    private String threeYearPerformanceThumbnailContentType;

    @Schema(description = "三年内业绩缩略图状态")
    private FileThumbnailStatus threeYearPerformanceThumbnailStatus;

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

    public LocalDateTime getFirstLoginAt() {
        return firstLoginAt;
    }

    public void setFirstLoginAt(LocalDateTime firstLoginAt) {
        this.firstLoginAt = firstLoginAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Long getBusinessLicenseFileId() {
        return businessLicenseFileId;
    }

    public void setBusinessLicenseFileId(Long businessLicenseFileId) {
        this.businessLicenseFileId = businessLicenseFileId;
    }

    public String getBusinessLicenseFileName() {
        return businessLicenseFileName;
    }

    public void setBusinessLicenseFileName(String businessLicenseFileName) {
        this.businessLicenseFileName = businessLicenseFileName;
    }

    public String getBusinessLicenseContentType() {
        return businessLicenseContentType;
    }

    public void setBusinessLicenseContentType(String businessLicenseContentType) {
        this.businessLicenseContentType = businessLicenseContentType;
    }

    public Long getBusinessLicenseFileSize() {
        return businessLicenseFileSize;
    }

    public void setBusinessLicenseFileSize(Long businessLicenseFileSize) {
        this.businessLicenseFileSize = businessLicenseFileSize;
    }

    public String getBusinessLicenseThumbnailUrl() {
        return businessLicenseThumbnailUrl;
    }

    public void setBusinessLicenseThumbnailUrl(String businessLicenseThumbnailUrl) {
        this.businessLicenseThumbnailUrl = businessLicenseThumbnailUrl;
    }

    public String getBusinessLicenseThumbnailContentType() {
        return businessLicenseThumbnailContentType;
    }

    public void setBusinessLicenseThumbnailContentType(String businessLicenseThumbnailContentType) {
        this.businessLicenseThumbnailContentType = businessLicenseThumbnailContentType;
    }

    public FileThumbnailStatus getBusinessLicenseThumbnailStatus() {
        return businessLicenseThumbnailStatus;
    }

    public void setBusinessLicenseThumbnailStatus(FileThumbnailStatus businessLicenseThumbnailStatus) {
        this.businessLicenseThumbnailStatus = businessLicenseThumbnailStatus;
    }

    public Long getThreeYearPerformanceFileId() {
        return threeYearPerformanceFileId;
    }

    public void setThreeYearPerformanceFileId(Long threeYearPerformanceFileId) {
        this.threeYearPerformanceFileId = threeYearPerformanceFileId;
    }

    public String getThreeYearPerformanceFileName() {
        return threeYearPerformanceFileName;
    }

    public void setThreeYearPerformanceFileName(String threeYearPerformanceFileName) {
        this.threeYearPerformanceFileName = threeYearPerformanceFileName;
    }

    public String getThreeYearPerformanceContentType() {
        return threeYearPerformanceContentType;
    }

    public void setThreeYearPerformanceContentType(String threeYearPerformanceContentType) {
        this.threeYearPerformanceContentType = threeYearPerformanceContentType;
    }

    public Long getThreeYearPerformanceFileSize() {
        return threeYearPerformanceFileSize;
    }

    public void setThreeYearPerformanceFileSize(Long threeYearPerformanceFileSize) {
        this.threeYearPerformanceFileSize = threeYearPerformanceFileSize;
    }

    public String getThreeYearPerformanceThumbnailUrl() {
        return threeYearPerformanceThumbnailUrl;
    }

    public void setThreeYearPerformanceThumbnailUrl(String threeYearPerformanceThumbnailUrl) {
        this.threeYearPerformanceThumbnailUrl = threeYearPerformanceThumbnailUrl;
    }

    public String getThreeYearPerformanceThumbnailContentType() {
        return threeYearPerformanceThumbnailContentType;
    }

    public void setThreeYearPerformanceThumbnailContentType(String threeYearPerformanceThumbnailContentType) {
        this.threeYearPerformanceThumbnailContentType = threeYearPerformanceThumbnailContentType;
    }

    public FileThumbnailStatus getThreeYearPerformanceThumbnailStatus() {
        return threeYearPerformanceThumbnailStatus;
    }

    public void setThreeYearPerformanceThumbnailStatus(FileThumbnailStatus threeYearPerformanceThumbnailStatus) {
        this.threeYearPerformanceThumbnailStatus = threeYearPerformanceThumbnailStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
