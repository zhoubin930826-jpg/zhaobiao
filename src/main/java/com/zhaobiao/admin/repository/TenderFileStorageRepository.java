package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.TenderFileStorage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenderFileStorageRepository extends JpaRepository<TenderFileStorage, Long> {

    Optional<TenderFileStorage> findByContentHash(String contentHash);
}
