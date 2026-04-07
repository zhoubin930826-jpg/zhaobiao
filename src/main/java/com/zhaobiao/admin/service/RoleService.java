package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.role.RoleDto;
import com.zhaobiao.admin.dto.role.RoleRequest;
import com.zhaobiao.admin.entity.Menu;
import com.zhaobiao.admin.entity.MenuType;
import com.zhaobiao.admin.entity.Permission;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.MenuRepository;
import com.zhaobiao.admin.repository.PermissionRepository;
import com.zhaobiao.admin.repository.RoleRepository;
import com.zhaobiao.admin.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final ViewMapper viewMapper;

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       MenuRepository menuRepository,
                       UserRepository userRepository,
                       ViewMapper viewMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.viewMapper = viewMapper;
    }

    @Transactional(readOnly = true)
    public List<RoleDto> listRoles() {
        return roleRepository.findAllWithDetails().stream()
                .sorted(Comparator.comparing(Role::getId))
                .map(viewMapper::toRoleDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleDto createRole(RoleRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            throw new BusinessException(400, "角色编码已存在");
        }
        Role role = new Role();
        applyRoleChanges(role, request, false);
        role.setBuiltIn(false);
        return viewMapper.toRoleDto(roleRepository.save(role));
    }

    @Transactional
    public RoleDto updateRole(Long roleId, RoleRequest request) {
        Role role = roleRepository.findDetailById(roleId)
                .orElseThrow(() -> new BusinessException(404, "角色不存在"));
        if (!role.getCode().equals(request.getCode()) && roleRepository.existsByCode(request.getCode())) {
            throw new BusinessException(400, "角色编码已存在");
        }
        applyRoleChanges(role, request, role.isBuiltIn());
        return viewMapper.toRoleDto(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findDetailById(roleId)
                .orElseThrow(() -> new BusinessException(404, "角色不存在"));
        if (role.isBuiltIn()) {
            throw new BusinessException(400, "内置角色不允许删除");
        }
        if (userRepository.countByRoles_Id(roleId) > 0) {
            throw new BusinessException(400, "角色已被用户使用，不能删除");
        }
        roleRepository.delete(role);
    }

    private void applyRoleChanges(Role role, RoleRequest request, boolean builtIn) {
        if (builtIn && !role.getCode().equals(request.getCode())) {
            throw new BusinessException(400, "内置角色不允许修改编码");
        }
        Set<Permission> permissions = loadPermissions(request.getPermissionIds());
        Set<Menu> menus = loadMenus(request.getMenuIds());
        validateButtonPermissionConsistency(permissions, menus);

        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(permissions);
        role.setMenus(menus);
    }

    private Set<Permission> loadPermissions(List<Long> permissionIds) {
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new BusinessException(400, "包含不存在的权限");
        }
        return permissions.stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Menu> loadMenus(List<Long> menuIds) {
        List<Menu> menus = menuRepository.findAllById(menuIds);
        if (menus.size() != menuIds.size()) {
            throw new BusinessException(400, "包含不存在的菜单");
        }
        return menus.stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateButtonPermissionConsistency(Set<Permission> permissions, Set<Menu> menus) {
        Set<String> permissionCodes = permissions.stream().map(Permission::getCode).collect(Collectors.toSet());
        List<String> missingCodes = menus.stream()
                .filter(menu -> menu.getType() == MenuType.BUTTON && StringUtils.hasText(menu.getPermissionCode()))
                .map(Menu::getPermissionCode)
                .filter(code -> !permissionCodes.contains(code))
                .distinct()
                .collect(Collectors.toList());
        if (!missingCodes.isEmpty()) {
            throw new BusinessException(400, "按钮菜单缺少对应权限: " + String.join(", ", missingCodes));
        }
    }
}
