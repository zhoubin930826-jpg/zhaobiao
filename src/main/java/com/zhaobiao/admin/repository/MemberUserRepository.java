package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.MemberUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberUserRepository extends JpaRepository<MemberUser, Long> {

    Optional<MemberUser> findByUsernameAndDeletedFalse(String username);

    @EntityGraph(attributePaths = "businessTypes")
    Optional<MemberUser> findDetailByUsernameAndDeletedFalse(String username);

    @EntityGraph(attributePaths = "businessTypes")
    Optional<MemberUser> findDetailByIdAndDeletedFalse(Long id);

    @Query("select distinct mu from MemberUser mu left join fetch mu.businessTypes where mu.deleted = false")
    List<MemberUser> findAllActiveWithDetails();

    boolean existsByUsernameAndDeletedFalse(String username);

    boolean existsByPhoneAndDeletedFalse(String phone);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByUnifiedSocialCreditCodeAndDeletedFalse(String unifiedSocialCreditCode);

    boolean existsByUnifiedSocialCreditCodeAndIdNotAndDeletedFalse(String unifiedSocialCreditCode, Long id);

    boolean existsByBusinessTypes_Id(Long businessTypeId);

    @Query("select count(mu) from MemberUser mu " +
            "where mu.businessLicenseFile.id = :fileId or mu.threeYearPerformanceFile.id = :fileId")
    long countProfileFileReferences(@Param("fileId") Long fileId);
}
