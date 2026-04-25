package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.menu.MenuDto;
import com.zhaobiao.admin.dto.menu.MenuRequest;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "管理员-菜单管理")
@RestController
@RequestMapping("/api/admin/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "查询菜单树")
    @PreAuthorize("hasAuthority('SYSTEM_MENU')")
    @GetMapping
    public ApiResponse<List<MenuDto>> listMenus() {
        return ApiResponse.success(menuService.listMenus());
    }

    @Operation(summary = "新增菜单")
    @PreAuthorize("hasAuthority('MENU_EDIT_BUTTON')")
    @OperationLogRecord(module = "菜单管理", action = "新增菜单")
    @PostMapping
    public ApiResponse<MenuDto> createMenu(@Valid @RequestBody MenuRequest request) {
        return ApiResponse.success(menuService.createMenu(request));
    }

    @Operation(summary = "修改菜单")
    @PreAuthorize("hasAuthority('MENU_EDIT_BUTTON')")
    @OperationLogRecord(module = "菜单管理", action = "修改菜单")
    @PutMapping("/{menuId}")
    public ApiResponse<MenuDto> updateMenu(@PathVariable Long menuId, @Valid @RequestBody MenuRequest request) {
        return ApiResponse.success(menuService.updateMenu(menuId, request));
    }

    @Operation(summary = "删除菜单")
    @PreAuthorize("hasAuthority('MENU_EDIT_BUTTON')")
    @OperationLogRecord(module = "菜单管理", action = "删除菜单")
    @DeleteMapping("/{menuId}")
    public ApiResponse<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ApiResponse.success("删除成功", null);
    }
}

