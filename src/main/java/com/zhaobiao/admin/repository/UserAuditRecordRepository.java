package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.UserAuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAuditRecordRepository extends JpaRepository<UserAuditRecord, Long> {

    List<UserAuditRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}

