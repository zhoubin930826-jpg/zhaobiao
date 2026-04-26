package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("select distinct r from Role r left join fetch r.menus where r.code = ?1")
    Optional<Role> findDetailByCode(String code);

    @Query("select distinct r from Role r left join fetch r.menus where r.id = ?1")
    Optional<Role> findDetailById(Long id);

    @Query("select distinct r from Role r left join fetch r.menus")
    List<Role> findAllWithDetails();

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);
}
