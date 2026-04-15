package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.common.SystemConstants;
import com.zhaobiao.admin.dto.audit.UserAuditRecordDto;
import com.zhaobiao.admin.dto.user.UserAuditRequest;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import com.zhaobiao.admin.dto.user.UserRoleUpdateRequest;
import com.zhaobiao.admin.entity.AuditDecision;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.entity.UserAuditRecord;
import com.zhaobiao.admin.entity.UserStatus;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.RoleRepository;
import com.zhaobiao.admin.repository.UserAuditRecordRepository;
import com.zhaobiao.admin.repository.UserRepository;
import com.zhaobiao.admin.security.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserAuditRecordRepository userAuditRecordRepository;
    private final ViewMapper viewMapper;

    public AdminUserService(UserRepository userRepository,
                            RoleRepository roleRepository,
                            UserAuditRecordRepository userAuditRecordRepository,
                            ViewMapper viewMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userAuditRecordRepository = userAuditRecordRepository;
        this.viewMapper = viewMapper;
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> listUsers() {
        return userRepository.findAllWithDetails().stream()
                .filter(this::isLegacyPortalUser)
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .map(viewMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileDto auditUser(Long userId, UserAuditRequest request, LoginUser loginUser) {
        User user = getLegacyPortalUser(userId);
        if (!Boolean.TRUE.equals(request.getApproved()) && !StringUtils.hasText(request.getReason())) {
            throw new BusinessException(400, "驳回时必须填写原因");
        }

        boolean approved = Boolean.TRUE.equals(request.getApproved());
        user.setStatus(approved ? UserStatus.APPROVED : UserStatus.REJECTED);
        user.setAuditReason(approved ? null : request.getReason());
        user.setAuditedAt(LocalDateTime.now());
        user.setAuditedBy(loginUser.getUsername());
        userRepository.save(user);

        UserAuditRecord record = new UserAuditRecord();
        record.setUser(user);
        record.setDecision(approved ? AuditDecision.APPROVED : AuditDecision.REJECTED);
        record.setReason(request.getReason());
        record.setAuditorUsername(loginUser.getUsername());
        userAuditRecordRepository.save(record);

        return viewMapper.toUserDto(user);
    }

    @Transactional
    public UserProfileDto updateUserRoles(Long userId, UserRoleUpdateRequest request) {
        User user = getLegacyPortalUser(userId);
        Set<Role> roles = loadRoles(request.getRoleIds());
        if (roles.isEmpty()) {
            throw new BusinessException(400, "至少要保留一个角色");
        }
        if (roles.stream().anyMatch(this::isAdminRole)) {
            throw new BusinessException(403, "旧用户接口不允许分配管理员角色");
        }
        if ("admin".equals(user.getUsername()) && roles.stream().noneMatch(role -> SystemConstants.SUPER_ADMIN_ROLE.equals(role.getCode()))) {
            throw new BusinessException(400, "初始超级管理员必须保留超级管理员角色");
        }
        user.setRoles(roles);
        return viewMapper.toUserDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserAuditRecordDto> listAuditRecords(Long userId) {
        getLegacyPortalUser(userId);
        return userAuditRecordRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(viewMapper::toUserAuditRecordDto)
                .collect(Collectors.toList());
    }

    private Set<Role> loadRoles(List<Long> roleIds) {
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BusinessException(400, "包含不存在的角色");
        }
        return roles.stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean hasSuperAdminRole(User user) {
        return user.getRoles().stream().anyMatch(role -> SystemConstants.SUPER_ADMIN_ROLE.equals(role.getCode()));
    }

    private User getLegacyPortalUser(Long userId) {
        User user = userRepository.findDetailById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (!isLegacyPortalUser(user)) {
            throw new BusinessException(410, "旧用户管理接口已停用");
        }
        return user;
    }

    private boolean isLegacyPortalUser(User user) {
        return user.getRoles().stream().allMatch(role -> !isAdminRole(role));
    }

    private boolean isAdminRole(Role role) {
        return !SystemConstants.NORMAL_USER_ROLE.equals(role.getCode());
    }
}
