package com.zhaobiao.admin.repository;

import com.zhaobiao.admin.entity.ApplicationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationSettingRepository extends JpaRepository<ApplicationSetting, Long> {

    Optional<ApplicationSetting> findBySettingKey(String settingKey);
}
