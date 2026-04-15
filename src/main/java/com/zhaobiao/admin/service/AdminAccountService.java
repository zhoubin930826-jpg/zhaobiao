package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.common.SystemConstants;
import com.zhaobiao.admin.dto.user.AdminUserCreateRequest;
import com.zhaobiao.admin.dto.user.AdminUserPasswordResetRequest;
import com.zhaobiao.admin.dto.user.AdminUserStatusUpdateRequest;
import com.zhaobiao.admin.dto.user.AdminUserUpdateRequest;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import com.zhaobiao.admin.dto.user.UserRoleUpdateRequest;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.entity.UserStatus;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.RoleRepository;
import com.zhaobiao.admin.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminAccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ViewMapper viewMapper;

    public AdminAccountService(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder,
                               ViewMapper viewMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.viewMapper = viewMapper;
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> listAdminUsers() {
        return userRepository.findAllWithDetails().stream()
                .filter(this::isAdminUser)
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .map(viewMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileDto createAdminUser(AdminUserCreateRequest request) {
        validatePassword(request.getPassword(), request.getConfirmPassword());
        ensureUserUnique(request.getUsername(), request.getPhone(), request.getEmail(), null);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.APPROVED);
        user.setRoles(loadAdminRoles(request.getRoleIds()));

        return viewMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserProfileDto updateAdminUser(Long userId, AdminUserUpdateRequest request) {
        User user = getAdminUser(userId);
        ensureUserUnique(null, request.getPhone(), request.getEmail(), user.getId());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        return viewMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserProfileDto updateAdminUserRoles(Long userId, UserRoleUpdateRequest request) {
        User user = getAdminUser(userId);
        Set<Role> roles = loadAdminRoles(request.getRoleIds());
        if (isInitialSuperAdmin(user)
                && roles.stream().noneMatch(role -> SystemConstants.SUPER_ADMIN_ROLE.equals(role.getCode()))) {
            throw new BusinessException(400, "初始超级管理员必须保留超级管理员角色");
        }
        user.setRoles(roles);
        return viewMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserProfileDto updateAdminUserStatus(Long userId, AdminUserStatusUpdateRequest request) {
        User user = getAdminUser(userId);
        if (request.getStatus() != UserStatus.APPROVED && request.getStatus() != UserStatus.DISABLED) {
            throw new BusinessException(400, "管理员状态仅支持启用或禁用");
        }
        if (isInitialSuperAdmin(user) && request.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(400, "初始超级管理员不允许被禁用");
        }
        user.setStatus(request.getStatus());
        return viewMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public void resetAdminUserPassword(Long userId, AdminUserPasswordResetRequest request) {
        User user = getAdminUser(userId);
        validatePassword(request.getPassword(), request.getConfirmPassword());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    private User getAdminUser(Long userId) {
        User user = userRepository.findDetailById(userId)
                .orElseThrow(() -> new BusinessException(404, "管理员账号不存在"));
        if (!isAdminUser(user)) {
            throw new BusinessException(400, "目标账号不是后台管理员");
        }
        return user;
    }

    private Set<Role> loadAdminRoles(List<Long> roleIds) {
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BusinessException(400, "包含不存在的角色");
        }
        if (roles.stream().anyMatch(role -> SystemConstants.NORMAL_USER_ROLE.equals(role.getCode()))) {
            throw new BusinessException(400, "管理员账号不能分配会员角色");
        }
        return roles.stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void ensureUserUnique(String username, String phone, String email, Long currentUserId) {
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
    }

    private void validatePassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
    }

    private boolean isAdminUser(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> !SystemConstants.NORMAL_USER_ROLE.equals(role.getCode()));
    }

    private boolean isInitialSuperAdmin(User user) {
        return "admin".equals(user.getUsername())
                && user.getRoles().stream().anyMatch(role -> SystemConstants.SUPER_ADMIN_ROLE.equals(role.getCode()));
    }
}
