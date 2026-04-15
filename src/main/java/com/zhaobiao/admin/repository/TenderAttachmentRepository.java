package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.TenderAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TenderAttachmentRepository extends JpaRepository<TenderAttachment, Long> {

    @Query("select ta from TenderAttachment ta " +
            "join fetch ta.fileStorage fs " +
            "where ta.tender.id = ?1 order by ta.sortOrder asc, ta.id asc")
    List<TenderAttachment> findDetailsByTenderId(Long tenderId);

    @Query("select ta from TenderAttachment ta join fetch ta.fileStorage where ta.id = ?1 and ta.tender.id = ?2")
    Optional<TenderAttachment> findDetailByIdAndTenderId(Long attachmentId, Long tenderId);

    long countByFileStorage_Id(Long fileId);
}
