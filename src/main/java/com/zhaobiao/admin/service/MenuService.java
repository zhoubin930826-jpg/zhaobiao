package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.menu.MenuDto;
import com.zhaobiao.admin.dto.menu.MenuRequest;
import com.zhaobiao.admin.entity.Menu;
import com.zhaobiao.admin.entity.MenuType;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.MenuRepository;
import com.zhaobiao.admin.repository.PermissionRepository;
import com.zhaobiao.admin.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final ViewMapper viewMapper;

    public MenuService(MenuRepository menuRepository,
                       PermissionRepository permissionRepository,
                       RoleRepository roleRepository,
                       ViewMapper viewMapper) {
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.viewMapper = viewMapper;
    }

    @Transactional(readOnly = true)
    public List<MenuDto> listMenus() {
        return viewMapper.toMenuTree(menuRepository.findAllByOrderBySortOrderAscIdAsc());
    }

    @Transactional
    public MenuDto createMenu(MenuRequest request) {
        if (menuRepository.existsByCode(request.getCode())) {
            throw new BusinessException(400, "菜单编码已存在");
        }
        Menu menu = new Menu();
        applyMenuChanges(menu, request, false);
        return viewMapper.toMenuDto(menuRepository.save(menu));
    }

    @Transactional
    public MenuDto updateMenu(Long menuId, MenuRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(404, "菜单不存在"));
        if (!menu.getCode().equals(request.getCode()) && menuRepository.existsByCode(request.getCode())) {
            throw new BusinessException(400, "菜单编码已存在");
        }
        applyMenuChanges(menu, request, true);
        return viewMapper.toMenuDto(menuRepository.save(menu));
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(404, "菜单不存在"));
        if (menuRepository.existsByParentId(menuId)) {
            throw new BusinessException(400, "请先删除子菜单");
        }
        boolean used = roleRepository.findAllWithDetails().stream()
                .anyMatch(role -> role.getMenus().stream().anyMatch(item -> menuId.equals(item.getId())));
        if (used) {
            throw new BusinessException(400, "菜单已被角色引用，不能删除");
        }
        menuRepository.delete(menu);
    }

    private void applyMenuChanges(Menu menu, MenuRequest request, boolean update) {
        validateParent(request.getParentId(), update ? menu.getId() : null);
        validatePermissionCode(request);

        menu.setCode(request.getCode());
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setParentId(request.getParentId());
        menu.setRoutePath(request.getRoutePath());
        menu.setComponent(request.getComponent());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder());
        menu.setVisible(Boolean.TRUE.equals(request.getVisible()));
        menu.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        menu.setPermissionCode(request.getPermissionCode());
        menu.setDescription(request.getDescription());
    }

    private void validateParent(Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }
        if (currentId != null && currentId.equals(parentId)) {
            throw new BusinessException(400, "菜单不能选择自己作为父级");
        }
        menuRepository.findById(parentId).orElseThrow(() -> new BusinessException(400, "父级菜单不存在"));
    }

    private void validatePermissionCode(MenuRequest request) {
        if (request.getType() == MenuType.BUTTON && !StringUtils.hasText(request.getPermissionCode())) {
            throw new BusinessException(400, "按钮类型菜单必须绑定权限编码");
        }
        if (StringUtils.hasText(request.getPermissionCode()) && !permissionRepository.existsByCode(request.getPermissionCode())) {
            throw new BusinessException(400, "绑定的权限编码不存在");
        }
    }
}

