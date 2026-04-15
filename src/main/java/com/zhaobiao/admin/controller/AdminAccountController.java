package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.user.AdminUserCreateRequest;
import com.zhaobiao.admin.dto.user.AdminUserPasswordResetRequest;
import com.zhaobiao.admin.dto.user.AdminUserStatusUpdateRequest;
import com.zhaobiao.admin.dto.user.AdminUserUpdateRequest;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import com.zhaobiao.admin.dto.user.UserRoleUpdateRequest;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.AdminAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "管理员-管理员账号管理")
@RestController
@RequestMapping("/api/admin/admin-users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @Operation(summary = "查询管理员账号列表")
    @GetMapping
    public ApiResponse<List<UserProfileDto>> listAdminUsers() {
        return ApiResponse.success(adminAccountService.listAdminUsers());
    }

    @Operation(summary = "新增管理员账号")
    @OperationLogRecord(module = "管理员管理", action = "新增管理员账号")
    @PostMapping
    public ApiResponse<UserProfileDto> createAdminUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return ApiResponse.success(adminAccountService.createAdminUser(request));
    }

    @Operation(summary = "修改管理员账号信息")
    @OperationLogRecord(module = "管理员管理", action = "修改管理员账号信息")
    @PutMapping("/{userId}")
    public ApiResponse<UserProfileDto> updateAdminUser(@PathVariable Long userId,
                                                       @Valid @RequestBody AdminUserUpdateRequest request) {
        return ApiResponse.success(adminAccountService.updateAdminUser(userId, request));
    }

    @Operation(summary = "修改管理员账号角色")
    @OperationLogRecord(module = "管理员管理", action = "修改管理员账号角色")
    @PutMapping("/{userId}/roles")
    public ApiResponse<UserProfileDto> updateAdminUserRoles(@PathVariable Long userId,
                                                            @Valid @RequestBody UserRoleUpdateRequest request) {
        return ApiResponse.success(adminAccountService.updateAdminUserRoles(userId, request));
    }

    @Operation(summary = "修改管理员账号状态")
    @OperationLogRecord(module = "管理员管理", action = "修改管理员账号状态")
    @PutMapping("/{userId}/status")
    public ApiResponse<UserProfileDto> updateAdminUserStatus(@PathVariable Long userId,
                                                             @Valid @RequestBody AdminUserStatusUpdateRequest request) {
        return ApiResponse.success(adminAccountService.updateAdminUserStatus(userId, request));
    }

    @Operation(summary = "重置管理员账号密码")
    @OperationLogRecord(module = "管理员管理", action = "重置管理员账号密码")
    @PutMapping("/{userId}/password")
    public ApiResponse<Void> resetAdminUserPassword(@PathVariable Long userId,
                                                    @Valid @RequestBody AdminUserPasswordResetRequest request) {
        adminAccountService.resetAdminUserPassword(userId, request);
        return ApiResponse.success("重置密码成功", null);
    }
}
