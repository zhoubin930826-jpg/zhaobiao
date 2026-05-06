package com.zhaobiao.admin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "portal_member_user")
public class MemberUser extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true, length = 32)
    private String phone;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 64)
    private String realName;

    @Column(nullable = false, length = 128)
    private String companyName;

    @Column(nullable = false, length = 64)
    private String contactPerson;

    @Column(nullable = false, unique = true, length = 32)
    private String unifiedSocialCreditCode;

    @Column(nullable = false)
    private boolean canDownloadFile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MemberUserStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime lastLoginAt;

    @Column(name = "first_login_at")
    private LocalDateTime firstLoginAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_license_file_id")
    private TenderFileStorage businessLicenseFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "three_year_performance_file_id")
    private TenderFileStorage threeYearPerformanceFile;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "portal_member_business_type_rel",
            joinColumns = @JoinColumn(name = "member_user_id"),
            inverseJoinColumns = @JoinColumn(name = "business_type_id")
    )
    @OrderBy("sortOrder asc, id asc")
    private Set<BusinessType> businessTypes = new LinkedHashSet<>();

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getFirstLoginAt() {
        return firstLoginAt;
    }

    public void setFirstLoginAt(LocalDateTime firstLoginAt) {
        this.firstLoginAt = firstLoginAt;
    }

    public TenderFileStorage getBusinessLicenseFile() {
        return businessLicenseFile;
    }

    public void setBusinessLicenseFile(TenderFileStorage businessLicenseFile) {
        this.businessLicenseFile = businessLicenseFile;
    }

    public TenderFileStorage getThreeYearPerformanceFile() {
        return threeYearPerformanceFile;
    }

    public void setThreeYearPerformanceFile(TenderFileStorage threeYearPerformanceFile) {
        this.threeYearPerformanceFile = threeYearPerformanceFile;
    }

    public Set<BusinessType> getBusinessTypes() {
        return businessTypes;
    }

    public void setBusinessTypes(Set<BusinessType> businessTypes) {
        this.businessTypes = businessTypes;
    }
}
