package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.MemberUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberUserRepository extends JpaRepository<MemberUser, Long> {

    Optional<MemberUser> findByUsername(String username);

    @EntityGraph(attributePaths = "businessTypes")
    Optional<MemberUser> findDetailByUsername(String username);

    @EntityGraph(attributePaths = "businessTypes")
    Optional<MemberUser> findDetailById(Long id);

    @Query("select distinct mu from MemberUser mu left join fetch mu.businessTypes")
    List<MemberUser> findAllWithDetails();

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByUnifiedSocialCreditCode(String unifiedSocialCreditCode);

    boolean existsByUnifiedSocialCreditCodeAndIdNot(String unifiedSocialCreditCode, Long id);

    boolean existsByBusinessTypes_Id(Long businessTypeId);
}
