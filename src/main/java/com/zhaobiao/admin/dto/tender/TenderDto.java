package com.zhaobiao.admin.dto.tender;

import com.zhaobiao.admin.entity.TenderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "后台招标详情")
public class TenderDto {

    @Schema(description = "招标ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "发布时间")
    private LocalDateTime publishAt;

    @Schema(description = "正文")
    private String content;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "预算")
    private String budget;

    @Schema(description = "联系方式")
    private String contactPhone;

    @Schema(description = "招标单位")
    private String tenderUnit;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "项目编号")
    private String projectCode;

    @Schema(description = "报名截止时间")
    private LocalDateTime signupDeadline;

    @Schema(description = "状态")
    private TenderStatus status;

    @Schema(description = "附件列表")
    private List<TenderAttachmentDto> attachments;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新人")
    private String updatedBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(LocalDateTime publishAt) {
        this.publishAt = publishAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getTenderUnit() {
        return tenderUnit;
    }

    public void setTenderUnit(String tenderUnit) {
        this.tenderUnit = tenderUnit;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public LocalDateTime getSignupDeadline() {
        return signupDeadline;
    }

    public void setSignupDeadline(LocalDateTime signupDeadline) {
        this.signupDeadline = signupDeadline;
    }

    public TenderStatus getStatus() {
        return status;
    }

    public void setStatus(TenderStatus status) {
        this.status = status;
    }

    public List<TenderAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<TenderAttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
