package com.zhaobiao.admin.dto.tender;

import com.zhaobiao.admin.dto.business.BusinessTypeOptionDto;
import com.zhaobiao.admin.entity.TenderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "招标列表项")
public class TenderListItemDto {

    @Schema(description = "招标ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "业务类型")
    private BusinessTypeOptionDto businessType;

    @Schema(description = "招标单位")
    private String tenderUnit;

    @Schema(description = "预算")
    private String budget;

    @Schema(description = "项目编号")
    private String projectCode;

    @Schema(description = "发布时间")
    private LocalDateTime publishAt;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "状态")
    private TenderStatus status;

    @Schema(description = "摘要")
    private String summary;

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

    public BusinessTypeOptionDto getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessTypeOptionDto businessType) {
        this.businessType = businessType;
    }

    public String getTenderUnit() {
        return tenderUnit;
    }

    public void setTenderUnit(String tenderUnit) {
        this.tenderUnit = tenderUnit;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public LocalDateTime getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(LocalDateTime publishAt) {
        this.publishAt = publishAt;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public TenderStatus getStatus() {
        return status;
    }

    public void setStatus(TenderStatus status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
