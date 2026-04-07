package com.zhaobiao.admin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "sys_user_audit_record")
public class UserAuditRecord extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AuditDecision decision;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false, length = 64)
    private String auditorUsername;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}

