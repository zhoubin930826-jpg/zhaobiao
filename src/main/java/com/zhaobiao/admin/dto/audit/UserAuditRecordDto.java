package com.zhaobiao.admin.dto.audit;

import com.zhaobiao.admin.entity.AuditDecision;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "用户审核记录")
public class UserAuditRecordDto {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "审核结果")
    private AuditDecision decision;

    @Schema(description = "审核原因")
    private String reason;

    @Schema(description = "审核人")
    private String auditorUsername;

    @Schema(description = "审核时间")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AuditDecision getDecision() {
        return decision;
    }

    public void setDecision(AuditDecision decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAuditorUsername() {
        return auditorUsername;
    }

    public void setAuditorUsername(String auditorUsername) {
        this.auditorUsername = auditorUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

