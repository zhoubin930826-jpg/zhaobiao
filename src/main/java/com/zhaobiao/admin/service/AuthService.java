package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.config.JwtProperties;
import com.zhaobiao.admin.dto.auth.LoginRequest;
import com.zhaobiao.admin.dto.auth.LoginResponse;
import com.zhaobiao.admin.dto.auth.RegisterRequest;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.entity.UserStatus;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.UserRepository;
import com.zhaobiao.admin.security.JwtTokenProvider;
import com.zhaobiao.admin.security.LoginUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final ViewMapper viewMapper;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties,
                       ViewMapper viewMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.viewMapper = viewMapper;
    }

    @Transactional
    public void register(RegisterRequest request) {
        throw new BusinessException(403, "后台管理员不支持公开注册，请联系超级管理员创建账号");
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findDetailByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == UserStatus.PENDING) {
            throw new BusinessException(403, "账号待管理员审核通过后才可登录");
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new BusinessException(403, "账号审核未通过: " + (user.getAuditReason() == null ? "请联系管理员" : user.getAuditReason()));
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        LoginUser loginUser = LoginUser.from(user);
        LoginResponse response = new LoginResponse();
        response.setToken(jwtTokenProvider.generateToken(loginUser));
        response.setTokenType("Bearer");
        response.setExpireSeconds(jwtProperties.getExpireSeconds());
        response.setUser(viewMapper.toUserDto(user, true));
        return response;
    }

    @Transactional(readOnly = true)
    public UserProfileDto currentUser(LoginUser loginUser) {
        User user = userRepository.findDetailById(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return viewMapper.toUserDto(user, true);
    }

    public void ensureUserUnique(String username,
                                 String phone,
                                 String email,
                                 String unifiedSocialCreditCode,
                                 Long currentUserId) {
        Optional<User> currentUser = currentUserId == null ? Optional.empty() : userRepository.findDetailById(currentUserId);
        if (username != null && userRepository.existsByUsername(username)) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (phone != null && userRepository.existsByPhone(phone)
                && !currentUser.map(item -> phone.equals(item.getPhone())).orElse(false)) {
            throw new BusinessException(400, "手机号已存在");
        }
        if (email != null && userRepository.existsByEmail(email)
                && !currentUser.map(item -> email.equals(item.getEmail())).orElse(false)) {
            throw new BusinessException(400, "邮箱已存在");
        }
        if (unifiedSocialCreditCode != null
                && userRepository.existsByUnifiedSocialCreditCode(unifiedSocialCreditCode)
                && !currentUser.map(item -> unifiedSocialCreditCode.equals(item.getUnifiedSocialCreditCode())).orElse(false)) {
            throw new BusinessException(400, "统一社会信用代码已存在");
        }
    }

    private void validatePassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
    }
}
