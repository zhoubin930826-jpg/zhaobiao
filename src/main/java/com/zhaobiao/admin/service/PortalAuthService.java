package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.config.JwtProperties;
import com.zhaobiao.admin.dto.member.MemberLoginRequest;
import com.zhaobiao.admin.dto.member.MemberLoginResponse;
import com.zhaobiao.admin.dto.member.MemberRegisterRequest;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.entity.MemberUser;
import com.zhaobiao.admin.entity.MemberUserStatus;
import com.zhaobiao.admin.repository.MemberUserRepository;
import com.zhaobiao.admin.security.JwtTokenProvider;
import com.zhaobiao.admin.security.MemberLoginUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PortalAuthService {

    private final MemberUserRepository memberUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public PortalAuthService(MemberUserRepository memberUserRepository,
                             PasswordEncoder passwordEncoder,
                             JwtTokenProvider jwtTokenProvider,
                             JwtProperties jwtProperties) {
        this.memberUserRepository = memberUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public void register(MemberRegisterRequest request) {
        validatePassword(request.getPassword(), request.getConfirmPassword());
        ensureMemberUnique(request.getUsername(), request.getPhone(), request.getEmail(), request.getUnifiedSocialCreditCode(), null);

        MemberUser user = new MemberUser();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setCompanyName(request.getCompanyName());
        user.setContactPerson(request.getContactPerson());
        user.setUnifiedSocialCreditCode(request.getUnifiedSocialCreditCode());
        user.setRealName(request.getRealName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(MemberUserStatus.ENABLED);
        user.setCanDownloadFile(false);
        memberUserRepository.save(user);
    }

    @Transactional
    public MemberLoginResponse login(MemberLoginRequest request) {
        MemberUser user = memberUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == MemberUserStatus.DISABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }
        user.setLastLoginAt(LocalDateTime.now());
        memberUserRepository.save(user);

        MemberLoginResponse response = new MemberLoginResponse();
        response.setToken(jwtTokenProvider.generateToken(MemberLoginUser.from(user)));
        response.setTokenType("Bearer");
        response.setExpireSeconds(jwtProperties.getExpireSeconds());
        response.setUser(toMemberDto(user));
        return response;
    }

    @Transactional(readOnly = true)
    public MemberUserDto currentMember(MemberLoginUser loginUser) {
        MemberUser user = memberUserRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException(404, "会员不存在"));
        return toMemberDto(user);
    }

    public void ensureMemberUnique(String username,
                                   String phone,
                                   String email,
                                   String unifiedSocialCreditCode,
                                   Long currentUserId) {
        if (username != null && memberUserRepository.existsByUsername(username)
                && !isCurrentUserUsername(currentUserId, username)) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (phone != null && memberUserRepository.existsByPhone(phone)
                && !isCurrentUserPhone(currentUserId, phone)) {
            throw new BusinessException(400, "手机号已存在");
        }
        if (email != null && memberUserRepository.existsByEmail(email)
                && !isCurrentUserEmail(currentUserId, email)) {
            throw new BusinessException(400, "邮箱已存在");
        }
        if (unifiedSocialCreditCode != null && memberUserRepository.existsByUnifiedSocialCreditCode(unifiedSocialCreditCode)
                && !isCurrentUserCreditCode(currentUserId, unifiedSocialCreditCode)) {
            throw new BusinessException(400, "统一社会信用代码已存在");
        }
    }

    private boolean isCurrentUserUsername(Long currentUserId, String username) {
        return currentUserId != null && memberUserRepository.findById(currentUserId)
                .map(item -> username.equals(item.getUsername()))
                .orElse(false);
    }

    private boolean isCurrentUserPhone(Long currentUserId, String phone) {
        return currentUserId != null && memberUserRepository.findById(currentUserId)
                .map(item -> phone.equals(item.getPhone()))
                .orElse(false);
    }

    private boolean isCurrentUserEmail(Long currentUserId, String email) {
        return currentUserId != null && memberUserRepository.findById(currentUserId)
                .map(item -> email.equals(item.getEmail()))
                .orElse(false);
    }

    private boolean isCurrentUserCreditCode(Long currentUserId, String unifiedSocialCreditCode) {
        return currentUserId != null && memberUserRepository.findById(currentUserId)
                .map(item -> unifiedSocialCreditCode.equals(item.getUnifiedSocialCreditCode()))
                .orElse(false);
    }

    private void validatePassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
    }

    private MemberUserDto toMemberDto(MemberUser user) {
        MemberUserDto dto = new MemberUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setCompanyName(user.getCompanyName());
        dto.setContactPerson(user.getContactPerson());
        dto.setUnifiedSocialCreditCode(user.getUnifiedSocialCreditCode());
        dto.setCanDownloadFile(user.isCanDownloadFile());
        dto.setStatus(user.getStatus());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}

