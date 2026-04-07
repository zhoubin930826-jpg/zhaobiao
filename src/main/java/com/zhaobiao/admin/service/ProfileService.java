package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.user.ProfileUpdateRequest;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.UserRepository;
import com.zhaobiao.admin.security.LoginUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ViewMapper viewMapper;
    private final AuthService authService;

    public ProfileService(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          ViewMapper viewMapper,
                          AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.viewMapper = viewMapper;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(LoginUser loginUser) {
        User user = getCurrentUser(loginUser);
        return viewMapper.toUserDto(user, true);
    }

    @Transactional
    public UserProfileDto updateProfile(LoginUser loginUser, ProfileUpdateRequest request) {
        User user = getCurrentUser(loginUser);

        authService.ensureUserUnique(
                null,
                StringUtils.hasText(request.getPhone()) ? request.getPhone() : null,
                StringUtils.hasText(request.getEmail()) ? request.getEmail() : null,
                StringUtils.hasText(request.getUnifiedSocialCreditCode()) ? request.getUnifiedSocialCreditCode() : null,
                user.getId()
        );

        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getCompanyName())) {
            user.setCompanyName(request.getCompanyName());
        }
        if (StringUtils.hasText(request.getContactPerson())) {
            user.setContactPerson(request.getContactPerson());
        }
        if (StringUtils.hasText(request.getUnifiedSocialCreditCode())) {
            user.setUnifiedSocialCreditCode(request.getUnifiedSocialCreditCode());
        }
        if (StringUtils.hasText(request.getPassword()) || StringUtils.hasText(request.getConfirmPassword())) {
            if (!StringUtils.hasText(request.getPassword()) || !request.getPassword().equals(request.getConfirmPassword())) {
                throw new BusinessException(400, "两次输入的新密码不一致");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return viewMapper.toUserDto(userRepository.save(user), true);
    }

    private User getCurrentUser(LoginUser loginUser) {
        return userRepository.findDetailById(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }
}
