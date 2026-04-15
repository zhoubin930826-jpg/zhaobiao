package com.zhaobiao.admin.dto.tender;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhaobiao.admin.entity.TenderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "招标新增/修改请求")
public class TenderUpsertRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过 200 位")
    @Schema(description = "标题", example = "某市智慧政务云平台建设项目公开招标公告")
    private String title;

    @NotBlank(message = "地区不能为空")
    @Size(max = 64, message = "地区不能超过 64 位")
    @Schema(description = "地区", example = "浙江")
    private String region;

    @NotNull(message = "发布时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发布时间", example = "2026-04-15 10:00:00")
    private LocalDateTime publishAt;

    @NotBlank(message = "正文不能为空")
    @Schema(description = "正文，支持富文本 HTML")
    private String content;

    @NotBlank(message = "联系人不能为空")
    @Size(max = 64, message = "联系人不能超过 64 位")
    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @NotBlank(message = "预算不能为空")
    @Size(max = 64, message = "预算不能超过 64 位")
    @Schema(description = "预算", example = "580 万元")
    private String budget;

    @NotBlank(message = "联系方式不能为空")
    @Pattern(regexp = "^[0-9+\\-()\\s]{6,32}$", message = "联系方式格式不正确")
    @Schema(description = "联系方式", example = "0571-88886666")
    private String contactPhone;

    @NotBlank(message = "招标单位不能为空")
    @Size(max = 128, message = "招标单位不能超过 128 位")
    @Schema(description = "招标单位", example = "某市大数据发展管理局")
    private String tenderUnit;

    @NotNull(message = "截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "截止时间", example = "2026-05-01 09:30:00")
    private LocalDateTime deadline;

    @NotBlank(message = "项目编号不能为空")
    @Size(max = 64, message = "项目编号不能超过 64 位")
    @Schema(description = "项目编号", example = "ZB-2026-001")
    private String projectCode;

    @NotNull(message = "报名截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "报名截止时间", example = "2026-04-28 18:00:00")
    private LocalDateTime signupDeadline;

    @Schema(description = "状态，默认 PUBLISHED", example = "PUBLISHED")
    private TenderStatus status;

    @Schema(description = "附件文件ID列表")
    private List<Long> attachmentFileIds;

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

    public List<Long> getAttachmentFileIds() {
        return attachmentFileIds;
    }

    public void setAttachmentFileIds(List<Long> attachmentFileIds) {
        this.attachmentFileIds = attachmentFileIds;
    }
}
