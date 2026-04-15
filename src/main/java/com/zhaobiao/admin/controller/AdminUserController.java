package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.audit.UserAuditRecordDto;
import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员-旧用户管理（已停用）")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final String LEGACY_INTERFACE_DISABLED_MESSAGE = "旧用户管理接口已停用，请使用会员管理或管理员账号管理接口";

    @Operation(summary = "查询旧用户列表（已停用）")
    @GetMapping
    public ApiResponse<List<UserProfileDto>> listUsers() {
        throw legacyInterfaceDisabled();
    }

    @Operation(summary = "审核旧用户（已停用）")
    @PutMapping("/{userId}/audit")
    public ApiResponse<UserProfileDto> auditUser(@PathVariable Long userId) {
        throw legacyInterfaceDisabled();
    }

    @Operation(summary = "修改旧用户角色（已停用）")
    @PutMapping("/{userId}/roles")
    public ApiResponse<UserProfileDto> updateUserRoles(@PathVariable Long userId) {
        throw legacyInterfaceDisabled();
    }

    @Operation(summary = "查询旧用户审核记录（已停用）")
    @GetMapping("/{userId}/audit-records")
    public ApiResponse<List<UserAuditRecordDto>> listAuditRecords(@PathVariable Long userId) {
        throw legacyInterfaceDisabled();
    }

    private BusinessException legacyInterfaceDisabled() {
        return new BusinessException(410, LEGACY_INTERFACE_DISABLED_MESSAGE);
    }
}
