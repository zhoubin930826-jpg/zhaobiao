package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.member.MemberDownloadAccessUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberStatusUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.MemberAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "管理员-会员管理")
@RestController
@RequestMapping("/api/admin/members")
@PreAuthorize("hasRole('ADMIN')")
public class MemberAdminController {

    private final MemberAdminService memberAdminService;

    public MemberAdminController(MemberAdminService memberAdminService) {
        this.memberAdminService = memberAdminService;
    }

    @Operation(summary = "查询会员列表")
    @PreAuthorize("hasAuthority('member:view')")
    @GetMapping
    public ApiResponse<List<MemberUserDto>> listMembers() {
        return ApiResponse.success(memberAdminService.listMembers());
    }

    @Operation(summary = "修改会员下载权限")
    @PreAuthorize("hasAuthority('member:download:update')")
    @OperationLogRecord(module = "会员管理", action = "修改会员下载权限")
    @PutMapping("/{memberId}/download-access")
    public ApiResponse<MemberUserDto> updateDownloadAccess(@PathVariable Long memberId,
                                                           @Valid @RequestBody MemberDownloadAccessUpdateRequest request) {
        return ApiResponse.success(memberAdminService.updateDownloadAccess(memberId, request));
    }

    @Operation(summary = "修改会员状态")
    @PreAuthorize("hasAuthority('member:status:update')")
    @OperationLogRecord(module = "会员管理", action = "修改会员状态")
    @PutMapping("/{memberId}/status")
    public ApiResponse<MemberUserDto> updateStatus(@PathVariable Long memberId,
                                                   @Valid @RequestBody MemberStatusUpdateRequest request) {
        return ApiResponse.success(memberAdminService.updateStatus(memberId, request));
    }
}
