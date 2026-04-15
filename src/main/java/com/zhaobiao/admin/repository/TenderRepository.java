package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.Tender;
import com.zhaobiao.admin.entity.TenderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TenderRepository extends JpaRepository<Tender, Long> {

    boolean existsByProjectCode(String projectCode);

    boolean existsByProjectCodeAndIdNot(String projectCode, Long id);

    @Query(value = "select t from Tender t " +
            "where (:keyword is null or lower(t.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(t.tenderUnit) like lower(concat('%', :keyword, '%')) " +
            "or lower(t.projectCode) like lower(concat('%', :keyword, '%'))) " +
            "and (:region is null or t.region = :region)",
            countQuery = "select count(t) from Tender t " +
                    "where (:keyword is null or lower(t.title) like lower(concat('%', :keyword, '%')) " +
                    "or lower(t.tenderUnit) like lower(concat('%', :keyword, '%')) " +
                    "or lower(t.projectCode) like lower(concat('%', :keyword, '%'))) " +
                    "and (:region is null or t.region = :region)")
    Page<Tender> searchAdmin(@Param("keyword") String keyword,
                             @Param("region") String region,
                             Pageable pageable);

    @Query(value = "select t from Tender t " +
            "where t.status = :status " +
            "and t.publishAt <= :now " +
            "and (:keyword is null or lower(t.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(t.tenderUnit) like lower(concat('%', :keyword, '%')) " +
            "or lower(t.projectCode) like lower(concat('%', :keyword, '%'))) " +
            "and (:region is null or t.region = :region)",
            countQuery = "select count(t) from Tender t " +
                    "where t.status = :status " +
                    "and t.publishAt <= :now " +
                    "and (:keyword is null or lower(t.title) like lower(concat('%', :keyword, '%')) " +
                    "or lower(t.tenderUnit) like lower(concat('%', :keyword, '%')) " +
                    "or lower(t.projectCode) like lower(concat('%', :keyword, '%'))) " +
                    "and (:region is null or t.region = :region)")
    Page<Tender> searchPortal(@Param("keyword") String keyword,
                              @Param("region") String region,
                              @Param("status") TenderStatus status,
                              @Param("now") LocalDateTime now,
                              Pageable pageable);

    Optional<Tender> findByIdAndStatusAndPublishAtLessThanEqual(Long id, TenderStatus status, LocalDateTime now);
}
