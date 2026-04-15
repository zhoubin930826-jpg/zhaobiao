package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.BusinessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {

    Optional<BusinessType> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<BusinessType> findAllByOrderBySortOrderAscIdAsc();

    List<BusinessType> findByEnabledTrueOrderBySortOrderAscIdAsc();

    Optional<BusinessType> findTopByOrderBySortOrderDescIdDesc();
}
