package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.permission.PermissionDto;
import com.zhaobiao.admin.dto.permission.PermissionRequest;
import com.zhaobiao.admin.entity.Permission;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.MenuRepository;
import com.zhaobiao.admin.repository.PermissionRepository;
import com.zhaobiao.admin.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final ViewMapper viewMapper;

    public PermissionService(PermissionRepository permissionRepository,
                             RoleRepository roleRepository,
                             MenuRepository menuRepository,
                             ViewMapper viewMapper) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.viewMapper = viewMapper;
    }

    @Transactional(readOnly = true)
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getId))
                .map(viewMapper::toPermissionDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PermissionDto createPermission(PermissionRequest request) {
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new BusinessException(400, "权限编码已存在");
        }
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        return viewMapper.toPermissionDto(permissionRepository.save(permission));
    }

    @Transactional
    public PermissionDto updatePermission(Long permissionId, PermissionRequest request) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException(404, "权限不存在"));
        if (!permission.getCode().equals(request.getCode()) && permissionRepository.existsByCode(request.getCode())) {
            throw new BusinessException(400, "权限编码已存在");
        }
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        return viewMapper.toPermissionDto(permissionRepository.save(permission));
    }

    @Transactional
    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException(404, "权限不存在"));
        boolean usedByRole = roleRepository.findAllWithDetails().stream()
                .map(Role::getPermissions)
                .anyMatch(items -> items.stream().anyMatch(item -> permissionId.equals(item.getId())));
        if (usedByRole || menuRepository.existsByPermissionCode(permission.getCode())) {
            throw new BusinessException(400, "权限已被角色或菜单引用，不能删除");
        }
        permissionRepository.delete(permission);
    }
}

