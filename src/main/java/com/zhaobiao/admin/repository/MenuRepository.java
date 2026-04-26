package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByCode(String code);

    boolean existsByParentId(Long parentId);

    Optional<Menu> findByCode(String code);
}
