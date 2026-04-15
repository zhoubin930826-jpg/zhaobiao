package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.member.MemberCreateRequest;
import com.zhaobiao.admin.dto.member.MemberDownloadAccessUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberPasswordResetRequest;
import com.zhaobiao.admin.dto.member.MemberStatusUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.MemberAdminService;
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

    @Operation(summary = "查询会员详情")
    @PreAuthorize("hasAuthority('member:view')")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberUserDto> detail(@PathVariable Long memberId) {
        return ApiResponse.success(memberAdminService.getMemberDetail(memberId));
    }

    @Operation(summary = "新增会员")
    @PreAuthorize("hasAuthority('member:create')")
    @OperationLogRecord(module = "会员管理", action = "新增会员")
    @PostMapping
    public ApiResponse<MemberUserDto> create(@Valid @RequestBody MemberCreateRequest request) {
        return ApiResponse.success(memberAdminService.createMember(request));
    }

    @Operation(summary = "修改会员信息")
    @PreAuthorize("hasAuthority('member:edit')")
    @OperationLogRecord(module = "会员管理", action = "修改会员信息")
    @PutMapping("/{memberId}")
    public ApiResponse<MemberUserDto> update(@PathVariable Long memberId,
                                             @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResponse.success(memberAdminService.updateMember(memberId, request));
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

    @Operation(summary = "重置会员密码")
    @PreAuthorize("hasAuthority('member:password:reset')")
    @OperationLogRecord(module = "会员管理", action = "重置会员密码")
    @PutMapping("/{memberId}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long memberId,
                                           @Valid @RequestBody MemberPasswordResetRequest request) {
        memberAdminService.resetPassword(memberId, request);
        return ApiResponse.success("重置密码成功", null);
    }
}
