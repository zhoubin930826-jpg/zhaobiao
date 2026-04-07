package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.audit.UserAuditRecordDto;
import com.zhaobiao.admin.dto.user.UserAuditRequest;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import com.zhaobiao.admin.dto.user.UserRoleUpdateRequest;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.security.LoginUser;
import com.zhaobiao.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "管理员-用户管理")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "查询用户列表")
    @PreAuthorize("hasAuthority('user:view')")
    @GetMapping
    public ApiResponse<List<UserProfileDto>> listUsers() {
        return ApiResponse.success(adminUserService.listUsers());
    }

    @Operation(summary = "审核用户")
    @PreAuthorize("hasAuthority('user:audit')")
    @OperationLogRecord(module = "用户管理", action = "审核用户")
    @PutMapping("/{userId}/audit")
    public ApiResponse<UserProfileDto> auditUser(@PathVariable Long userId,
                                                 @Valid @RequestBody UserAuditRequest request,
                                                 @AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(adminUserService.auditUser(userId, request, loginUser));
    }

    @Operation(summary = "修改用户角色")
    @PreAuthorize("hasAuthority('user:role:update')")
    @OperationLogRecord(module = "用户管理", action = "修改用户角色")
    @PutMapping("/{userId}/roles")
    public ApiResponse<UserProfileDto> updateUserRoles(@PathVariable Long userId,
                                                       @Valid @RequestBody UserRoleUpdateRequest request) {
        return ApiResponse.success(adminUserService.updateUserRoles(userId, request));
    }

    @Operation(summary = "查询用户审核记录")
    @PreAuthorize("hasAuthority('user:audit:record:view')")
    @GetMapping("/{userId}/audit-records")
    public ApiResponse<List<UserAuditRecordDto>> listAuditRecords(@PathVariable Long userId) {
        return ApiResponse.success(adminUserService.listAuditRecords(userId));
    }
}
