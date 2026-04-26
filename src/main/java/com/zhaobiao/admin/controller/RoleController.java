package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.role.RoleDto;
import com.zhaobiao.admin.dto.role.RoleRequest;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.RoleService;
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

@Tag(name = "管理员-角色管理")
@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "查询角色列表")
    @PreAuthorize("hasAuthority('SYSTEM_ROLE')")
    @GetMapping
    public ApiResponse<List<RoleDto>> listRoles() {
        return ApiResponse.success(roleService.listRoles());
    }

    @Operation(summary = "新增角色")
    @PreAuthorize("hasAuthority('ROLE_EDIT_BUTTON')")
    @OperationLogRecord(module = "角色管理", action = "新增角色")
    @PostMapping
    public ApiResponse<RoleDto> createRole(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @Operation(summary = "修改角色")
    @PreAuthorize("hasAuthority('ROLE_EDIT_BUTTON')")
    @OperationLogRecord(module = "角色管理", action = "修改角色")
    @PutMapping("/{roleId}")
    public ApiResponse<RoleDto> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.updateRole(roleId, request));
    }

    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('ROLE_EDIT_BUTTON')")
    @OperationLogRecord(module = "角色管理", action = "删除角色")
    @DeleteMapping("/{roleId}")
    public ApiResponse<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ApiResponse.success("删除成功", null);
    }
}
