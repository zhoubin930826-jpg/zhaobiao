package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.permission.PermissionDto;
import com.zhaobiao.admin.dto.permission.PermissionRequest;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.PermissionService;
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

@Tag(name = "管理员-权限管理")
@RestController
@RequestMapping("/api/admin/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Operation(summary = "查询权限列表")
    @PreAuthorize("hasAuthority('permission:view')")
    @GetMapping
    public ApiResponse<List<PermissionDto>> listPermissions() {
        return ApiResponse.success(permissionService.listPermissions());
    }

    @Operation(summary = "新增权限")
    @PreAuthorize("hasAuthority('permission:edit')")
    @OperationLogRecord(module = "权限管理", action = "新增权限")
    @PostMapping
    public ApiResponse<PermissionDto> createPermission(@Valid @RequestBody PermissionRequest request) {
        return ApiResponse.success(permissionService.createPermission(request));
    }

    @Operation(summary = "修改权限")
    @PreAuthorize("hasAuthority('permission:edit')")
    @OperationLogRecord(module = "权限管理", action = "修改权限")
    @PutMapping("/{permissionId}")
    public ApiResponse<PermissionDto> updatePermission(@PathVariable Long permissionId,
                                                       @Valid @RequestBody PermissionRequest request) {
        return ApiResponse.success(permissionService.updatePermission(permissionId, request));
    }

    @Operation(summary = "删除权限")
    @PreAuthorize("hasAuthority('permission:edit')")
    @OperationLogRecord(module = "权限管理", action = "删除权限")
    @DeleteMapping("/{permissionId}")
    public ApiResponse<Void> deletePermission(@PathVariable Long permissionId) {
        permissionService.deletePermission(permissionId);
        return ApiResponse.success("删除成功", null);
    }
}
