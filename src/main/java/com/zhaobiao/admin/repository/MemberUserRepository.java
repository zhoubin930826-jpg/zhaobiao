package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.MemberUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberUserRepository extends JpaRepository<MemberUser, Long> {

    Optional<MemberUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByUnifiedSocialCreditCode(String unifiedSocialCreditCode);
}

