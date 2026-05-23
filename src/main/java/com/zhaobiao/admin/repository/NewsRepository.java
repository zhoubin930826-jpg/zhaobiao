package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.News;
import com.zhaobiao.admin.entity.NewsCategory;
import com.zhaobiao.admin.entity.NewsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    long countByCoverFile_Id(Long fileId);

    @Query(value = "select n from News n left join fetch n.coverFile cf " +
            "where (:keyword is null or lower(n.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(n.summary) like lower(concat('%', :keyword, '%')) " +
            "or lower(n.source) like lower(concat('%', :keyword, '%'))) " +
            "and (:category is null or n.category = :category) " +
            "and (:status is null or n.status = :status)",
            countQuery = "select count(n) from News n " +
                    "where (:keyword is null or lower(n.title) like lower(concat('%', :keyword, '%')) " +
                    "or lower(n.summary) like lower(concat('%', :keyword, '%')) " +
                    "or lower(n.source) like lower(concat('%', :keyword, '%'))) " +
                    "and (:category is null or n.category = :category) " +
                    "and (:status is null or n.status = :status)")
    Page<News> searchAdmin(@Param("keyword") String keyword,
                           @Param("category") NewsCategory category,
                           @Param("status") NewsStatus status,
                           Pageable pageable);

    @Query(value = "select n from News n left join fetch n.coverFile cf " +
            "where n.status = :status and n.publishAt <= :now " +
            "and (:keyword is null or lower(n.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(n.summary) like lower(concat('%', :keyword, '%')) " +
            "or lower(n.source) like lower(concat('%', :keyword, '%'))) " +
            "and (:category is null or n.category = :category)",
            countQuery = "select count(n) from News n " +
                    "where n.status = :status and n.publishAt <= :now " +
                    "and (:keyword is null or lower(n.title) like lower(concat('%', :keyword, '%')) " +
                    "or lower(n.summary) like lower(concat('%', :keyword, '%')) " +
                    "or lower(n.source) like lower(concat('%', :keyword, '%'))) " +
                    "and (:category is null or n.category = :category)")
    Page<News> searchPortal(@Param("keyword") String keyword,
                            @Param("category") NewsCategory category,
                            @Param("status") NewsStatus status,
                            @Param("now") LocalDateTime now,
                            Pageable pageable);

    @Query("select n from News n left join fetch n.coverFile cf " +
            "where n.status = :status and n.publishAt <= :now " +
            "order by n.publishAt desc, n.id desc")
    List<News> findLatestPublished(@Param("status") NewsStatus status,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);

    @Query("select n from News n left join fetch n.coverFile cf where n.id = :id")
    Optional<News> findDetailById(@Param("id") Long id);

    @Query("select n from News n left join fetch n.coverFile cf " +
            "where n.id = :id and n.status = :status and n.publishAt <= :now")
    Optional<News> findPublicDetailById(@Param("id") Long id,
                                        @Param("status") NewsStatus status,
                                        @Param("now") LocalDateTime now);
}
