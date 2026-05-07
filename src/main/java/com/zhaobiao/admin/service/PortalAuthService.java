package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.config.JwtProperties;
import com.zhaobiao.admin.dto.member.MemberLoginRequest;
import com.zhaobiao.admin.dto.member.MemberLoginResponse;
import com.zhaobiao.admin.dto.member.MemberProfileUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.entity.BusinessType;
import com.zhaobiao.admin.entity.MemberUser;
import com.zhaobiao.admin.entity.MemberUserStatus;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.MemberUserRepository;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import com.zhaobiao.admin.security.JwtTokenProvider;
import com.zhaobiao.admin.security.MemberLoginUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class PortalAuthService {

    private final MemberUserRepository memberUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final ViewMapper viewMapper;
    private final TenderFileStorageRepository tenderFileStorageRepository;

    public PortalAuthService(MemberUserRepository memberUserRepository,
                             PasswordEncoder passwordEncoder,
                             JwtTokenProvider jwtTokenProvider,
                             JwtProperties jwtProperties,
                             ViewMapper viewMapper,
                             TenderFileStorageRepository tenderFileStorageRepository) {
        this.memberUserRepository = memberUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.viewMapper = viewMapper;
        this.tenderFileStorageRepository = tenderFileStorageRepository;
    }

    @Transactional
    public MemberLoginResponse login(MemberLoginRequest request) {
        MemberUser user = memberUserRepository.findDetailByUsernameAndDeletedFalse(request.getUsername())
                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == MemberUserStatus.DISABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (isExpired(user.getExpiresAt())) {
            throw new BusinessException(403, "账号已过期，请联系管理员");
        }
        if (resolveActiveBusinessTypeIds(user).isEmpty()) {
            throw new BusinessException(403, "账号未分配可用业务类型，请联系管理员");
        }
        boolean firstLogin = user.getFirstLoginAt() == null;
        LocalDateTime now = LocalDateTime.now();
        if (firstLogin) {
            user.setFirstLoginAt(now);
        }
        user.setLastLoginAt(now);
        memberUserRepository.save(user);

        MemberLoginResponse response = new MemberLoginResponse();
        response.setToken(jwtTokenProvider.generateToken(MemberLoginUser.from(user)));
        response.setTokenType("Bearer");
        response.setExpireSeconds(jwtProperties.getExpireSeconds());
        response.setProfileCompletionRequired(firstLogin);
        response.setUser(toMemberDto(user));
        return response;
    }

    @Transactional(readOnly = true)
    public MemberUserDto currentMember(MemberLoginUser loginUser) {
        MemberUser user = memberUserRepository.findDetailByIdAndDeletedFalse(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException(404, "会员不存在"));
        return viewMapper.toMemberUserDto(user);
    }

    @Transactional
    public MemberUserDto updateCurrentMember(MemberLoginUser loginUser, MemberProfileUpdateRequest request) {
        MemberUser user = memberUserRepository.findDetailByIdAndDeletedFalse(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException(404, "会员不存在"));
        ensureMemberUnique(
                null,
                request.getPhone(),
                request.getEmail(),
                request.getUnifiedSocialCreditCode(),
                user.getId()
        );
        applyRequiredTextIfPresent(request.getPhone(), user::setPhone, "手机号不能为空");
        applyRequiredTextIfPresent(request.getEmail(), user::setEmail, "邮箱不能为空");
        applyRequiredTextIfPresent(request.getCompanyName(), user::setCompanyName, "公司名称不能为空");
        applyRequiredTextIfPresent(request.getContactPerson(), user::setContactPerson, "联系人不能为空");
        applyRequiredTextIfPresent(request.getUnifiedSocialCreditCode(), user::setUnifiedSocialCreditCode, "统一社会信用代码不能为空");
        if (request.getRealName() != null) {
            user.setRealName(StringUtils.hasText(request.getRealName()) ? request.getRealName().trim() : null);
        }
        if (request.getBusinessLicenseFileId() != null) {
            user.setBusinessLicenseFile(loadProfileFile(request.getBusinessLicenseFileId()));
        }
        if (request.getThreeYearPerformanceFileId() != null) {
            user.setThreeYearPerformanceFile(loadProfileFile(request.getThreeYearPerformanceFileId()));
        }
        return viewMapper.toMemberUserDto(memberUserRepository.save(user));
    }

    public void ensureMemberUnique(String username,
                                   String phone,
                                   String email,
                                   String unifiedSocialCreditCode,
                                   Long currentUserId) {
        username = normalizeForUnique(username);
        phone = normalizeForUnique(phone);
        email = normalizeForUnique(email);
        unifiedSocialCreditCode = normalizeForUnique(unifiedSocialCreditCode);
        if (username != null && memberUserRepository.existsByUsernameAndDeletedFalse(username)
                && !isCurrentUserUsername(currentUserId, username)) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (phone != null && memberUserRepository.existsByPhoneAndDeletedFalse(phone)
                && !isCurrentUserPhone(currentUserId, phone)) {
            throw new BusinessException(400, "手机号已存在");
        }
        if (email != null && memberUserRepository.existsByEmailAndDeletedFalse(email)
                && !isCurrentUserEmail(currentUserId, email)) {
            throw new BusinessException(400, "邮箱已存在");
        }
        if (unifiedSocialCreditCode != null && memberUserRepository.existsByUnifiedSocialCreditCodeAndDeletedFalse(unifiedSocialCreditCode)
                && !isCurrentUserCreditCode(currentUserId, unifiedSocialCreditCode)) {
            throw new BusinessException(400, "统一社会信用代码已存在");
        }
    }

    private boolean isCurrentUserUsername(Long currentUserId, String username) {
        return currentUserId != null && memberUserRepository.findDetailByIdAndDeletedFalse(currentUserId)
                .map(item -> username.equals(item.getUsername()))
                .orElse(false);
    }

    private boolean isCurrentUserPhone(Long currentUserId, String phone) {
        return currentUserId != null && memberUserRepository.findDetailByIdAndDeletedFalse(currentUserId)
                .map(item -> phone.equals(item.getPhone()))
                .orElse(false);
    }

    private boolean isCurrentUserEmail(Long currentUserId, String email) {
        return currentUserId != null && memberUserRepository.findDetailByIdAndDeletedFalse(currentUserId)
                .map(item -> email.equals(item.getEmail()))
                .orElse(false);
    }

    private boolean isCurrentUserCreditCode(Long currentUserId, String unifiedSocialCreditCode) {
        return currentUserId != null && memberUserRepository.findDetailByIdAndDeletedFalse(currentUserId)
                .map(item -> unifiedSocialCreditCode.equals(item.getUnifiedSocialCreditCode()))
                .orElse(false);
    }

    private void validatePassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
    }

    private MemberUserDto toMemberDto(MemberUser user) {
        return viewMapper.toMemberUserDto(user);
    }

    private boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt == null || !expiresAt.isAfter(LocalDateTime.now());
    }

    private java.util.List<Long> resolveActiveBusinessTypeIds(MemberUser user) {
        return user.getBusinessTypes().stream()
                .filter(BusinessType::isEnabled)
                .map(BusinessType::getId)
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    private TenderFileStorage loadProfileFile(Long fileId) {
        return tenderFileStorageRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(400, "资料文件不存在"));
    }

    private void applyRequiredTextIfPresent(String value,
                                            java.util.function.Consumer<String> consumer,
                                            String blankMessage) {
        if (value == null) {
            return;
        }
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, blankMessage);
        }
        consumer.accept(value.trim());
    }

    private String normalizeForUnique(String value) {
        return value == null ? null : value.trim();
    }
}
