package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select distinct u from User u " +
            "left join fetch u.roles r " +
            "left join fetch r.menus " +
            "where u.username = ?1")
    Optional<User> findDetailByUsername(String username);

    @Query("select distinct u from User u " +
            "left join fetch u.roles r " +
            "left join fetch r.menus " +
            "where u.id = ?1")
    Optional<User> findDetailById(Long id);

    @Query("select distinct u from User u " +
            "left join fetch u.roles r " +
            "left join fetch r.menus")
    List<User> findAllWithDetails();

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByUnifiedSocialCreditCode(String unifiedSocialCreditCode);

    long countByRoles_Id(Long roleId);
}
