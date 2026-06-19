package com.zhaobiao.admin.service;

import com.zhaobiao.admin.dto.member.MemberRegistrationSettingDto;
import com.zhaobiao.admin.dto.member.MemberRegistrationSettingUpdateRequest;
import com.zhaobiao.admin.entity.ApplicationSetting;
import com.zhaobiao.admin.repository.ApplicationSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberRegistrationSettingService {

    public static final String REGISTRATION_ENABLED_KEY = "portal.member.registration.enabled";

    private static final String DESCRIPTION = "门户会员自助注册开关，true 表示允许注册，false 表示关闭注册";

    private final ApplicationSettingRepository applicationSettingRepository;

    public MemberRegistrationSettingService(ApplicationSettingRepository applicationSettingRepository) {
        this.applicationSettingRepository = applicationSettingRepository;
    }

    @Transactional(readOnly = true)
    public MemberRegistrationSettingDto getRegistrationSetting() {
        return new MemberRegistrationSettingDto(isRegistrationEnabled());
    }

    @Transactional(readOnly = true)
    public boolean isRegistrationEnabled() {
        return applicationSettingRepository.findBySettingKey(REGISTRATION_ENABLED_KEY)
                .map(setting -> parseRegistrationEnabled(setting.getSettingValue()))
                .orElse(true);
    }

    @Transactional
    public MemberRegistrationSettingDto updateRegistrationSetting(MemberRegistrationSettingUpdateRequest request) {
        ApplicationSetting setting = applicationSettingRepository.findBySettingKey(REGISTRATION_ENABLED_KEY)
                .orElseGet(ApplicationSetting::new);
        setting.setSettingKey(REGISTRATION_ENABLED_KEY);
        setting.setSettingValue(Boolean.toString(Boolean.TRUE.equals(request.getRegistrationEnabled())));
        setting.setDescription(DESCRIPTION);
        applicationSettingRepository.save(setting);
        return new MemberRegistrationSettingDto(Boolean.TRUE.equals(request.getRegistrationEnabled()));
    }

    private boolean parseRegistrationEnabled(String settingValue) {
        return !"false".equalsIgnoreCase(settingValue == null ? null : settingValue.trim());
    }
}
