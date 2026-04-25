package com.zhaobiao.admin.mapper;

import com.zhaobiao.admin.dto.business.BusinessTypeDto;
import com.zhaobiao.admin.dto.business.BusinessTypeOptionDto;
import com.zhaobiao.admin.dto.audit.UserAuditRecordDto;
import com.zhaobiao.admin.dto.log.OperationLogDto;
import com.zhaobiao.admin.dto.menu.MenuDto;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.dto.permission.PermissionDto;
import com.zhaobiao.admin.dto.role.RoleDto;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import com.zhaobiao.admin.entity.BusinessType;
import com.zhaobiao.admin.entity.Menu;
import com.zhaobiao.admin.entity.MemberUser;
import com.zhaobiao.admin.entity.OperationLog;
import com.zhaobiao.admin.entity.Permission;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.entity.UserAuditRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ViewMapper {

    public UserProfileDto toUserDto(User user) {
        return toUserDto(user, false);
    }

    public UserProfileDto toUserDto(User user, boolean includeMenus) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setCompanyName(user.getCompanyName());
        dto.setContactPerson(user.getContactPerson());
        dto.setUnifiedSocialCreditCode(user.getUnifiedSocialCreditCode());
        dto.setStatus(user.getStatus());
        dto.setRoleIds(user.getRoles().stream().map(Role::getId).sorted().collect(Collectors.toList()));
        dto.setRoleCodes(user.getRoles().stream().map(Role::getCode).sorted().collect(Collectors.toList()));
        dto.setRoleNames(user.getRoles().stream().map(Role::getName).sorted().collect(Collectors.toList()));
        dto.setPermissions(user.getRoles().stream()
                .map(Role::getPermissions)
                .flatMap(Collection::stream)
                .map(Permission::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
        if (includeMenus) {
            List<Menu> menus = user.getRoles().stream()
                    .map(Role::getMenus)
                    .flatMap(Collection::stream)
                    .filter(Menu::isEnabled)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(Menu::getId, item -> item, (left, right) -> left, LinkedHashMap::new),
                            map -> map.values().stream()
                                    .sorted(Comparator.comparing(Menu::getSortOrder).thenComparing(Menu::getId))
                                    .collect(Collectors.toList())
                    ));
            dto.setMenus(toMenuTree(menus));
        }
        dto.setAuditReason(user.getAuditReason());
        dto.setAuditedAt(user.getAuditedAt());
        dto.setAuditedBy(user.getAuditedBy());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        return dto;
    }

    public BusinessTypeDto toBusinessTypeDto(BusinessType businessType) {
        BusinessTypeDto dto = new BusinessTypeDto();
        dto.setId(businessType.getId());
        dto.setCode(businessType.getCode());
        dto.setName(businessType.getName());
        dto.setEnabled(businessType.isEnabled());
        dto.setSortOrder(businessType.getSortOrder());
        dto.setDescription(businessType.getDescription());
        dto.setCreatedAt(businessType.getCreatedAt());
        dto.setUpdatedAt(businessType.getUpdatedAt());
        return dto;
    }

    public BusinessTypeOptionDto toBusinessTypeOptionDto(BusinessType businessType) {
        BusinessTypeOptionDto dto = new BusinessTypeOptionDto();
        dto.setId(businessType.getId());
        dto.setCode(businessType.getCode());
        dto.setName(businessType.getName());
        dto.setEnabled(businessType.isEnabled());
        dto.setSortOrder(businessType.getSortOrder());
        return dto;
    }

    public MemberUserDto toMemberUserDto(MemberUser user) {
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
        dto.setExpiresAt(user.getExpiresAt());
        dto.setExpired(isExpired(user.getExpiresAt()));
        dto.setBusinessTypes(user.getBusinessTypes().stream()
                .sorted(Comparator.comparing(BusinessType::getSortOrder).thenComparing(BusinessType::getId))
                .map(this::toBusinessTypeOptionDto)
                .collect(Collectors.toList()));
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public RoleDto toRoleDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setBuiltIn(role.isBuiltIn());
        dto.setPermissionIds(role.getPermissions().stream().map(Permission::getId).sorted().collect(Collectors.toList()));
        dto.setPermissionCodes(role.getPermissions().stream().map(Permission::getCode).sorted().collect(Collectors.toList()));
        dto.setMenuIds(role.getMenus().stream().map(Menu::getId).sorted().collect(Collectors.toList()));
        dto.setMenuCodes(role.getMenus().stream().map(Menu::getCode).sorted().collect(Collectors.toList()));
        return dto;
    }

    public PermissionDto toPermissionDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        return dto;
    }

    public MenuDto toMenuDto(Menu menu) {
        MenuDto dto = new MenuDto();
        dto.setId(menu.getId());
        dto.setCode(menu.getCode());
        dto.setName(menu.getName());
        dto.setType(menu.getType());
        dto.setParentId(menu.getParentId());
        dto.setRoutePath(menu.getRoutePath());
        dto.setComponent(menu.getComponent());
        dto.setIcon(menu.getIcon());
        dto.setSortOrder(menu.getSortOrder());
        dto.setVisible(menu.isVisible());
        dto.setEnabled(menu.isEnabled());
        dto.setPermissionCode(menu.getPermissionCode());
        dto.setDescription(menu.getDescription());
        return dto;
    }

    public List<MenuDto> toMenuTree(List<Menu> menus) {
        Map<Long, MenuDto> dtoMap = new LinkedHashMap<>();
        for (Menu menu : menus) {
            dtoMap.put(menu.getId(), toMenuDto(menu));
        }

        List<MenuDto> roots = new ArrayList<>();
        for (Menu menu : menus) {
            MenuDto current = dtoMap.get(menu.getId());
            Long parentId = menu.getParentId();
            if (parentId == null || !dtoMap.containsKey(parentId)) {
                roots.add(current);
                continue;
            }
            MenuDto parent = dtoMap.get(parentId);
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(current);
        }
        sortMenuTree(roots);
        return roots;
    }

    public UserAuditRecordDto toUserAuditRecordDto(UserAuditRecord record) {
        UserAuditRecordDto dto = new UserAuditRecordDto();
        dto.setId(record.getId());
        dto.setUserId(record.getUser().getId());
        dto.setUsername(record.getUser().getUsername());
        dto.setDecision(record.getDecision());
        dto.setReason(record.getReason());
        dto.setAuditorUsername(record.getAuditorUsername());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }

    public OperationLogDto toOperationLogDto(OperationLog operationLog) {
        OperationLogDto dto = new OperationLogDto();
        dto.setId(operationLog.getId());
        dto.setModule(operationLog.getModule());
        dto.setAction(operationLog.getAction());
        dto.setSuccess(operationLog.isSuccess());
        dto.setOperatorUsername(operationLog.getOperatorUsername());
        dto.setRequestMethod(operationLog.getRequestMethod());
        dto.setRequestUri(operationLog.getRequestUri());
        dto.setIpAddress(operationLog.getIpAddress());
        dto.setDetail(operationLog.getDetail());
        dto.setCreatedAt(operationLog.getCreatedAt());
        return dto;
    }

    private boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt == null || !expiresAt.isAfter(LocalDateTime.now());
    }

    private void sortMenuTree(List<MenuDto> menus) {
        menus.sort(Comparator.comparing(MenuDto::getSortOrder).thenComparing(MenuDto::getId));
        for (MenuDto menu : menus) {
            if (menu.getChildren() != null) {
                sortMenuTree(menu.getChildren());
            } else {
                menu.setChildren(Collections.emptyList());
            }
        }
    }
}
