package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.dto.member.MemberCreateRequest;
import com.zhaobiao.admin.dto.member.MemberDownloadAccessUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberPasswordResetRequest;
import com.zhaobiao.admin.dto.member.MemberStatusUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.FileStorageService;
import com.zhaobiao.admin.service.MemberAdminService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@Tag(name = "管理员-会员管理")
@RestController
@RequestMapping("/api/admin/members")
@PreAuthorize("hasRole('ADMIN')")
public class MemberAdminController {

    private final MemberAdminService memberAdminService;
    private final FileStorageService fileStorageService;

    public MemberAdminController(MemberAdminService memberAdminService,
                                 FileStorageService fileStorageService) {
        this.memberAdminService = memberAdminService;
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "查询会员列表")
    @PreAuthorize("hasAuthority('SYSTEM_MEMBER_USER')")
    @GetMapping
    public ApiResponse<List<MemberUserDto>> listMembers() {
        return ApiResponse.success(memberAdminService.listMembers());
    }

    @Operation(summary = "查询会员详情")
    @PreAuthorize("hasAuthority('SYSTEM_MEMBER_USER')")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberUserDto> detail(@PathVariable Long memberId) {
        return ApiResponse.success(memberAdminService.getMemberDetail(memberId));
    }

    @Operation(summary = "新增会员")
    @PreAuthorize("hasAuthority('MEMBER_CREATE_BUTTON')")
    @OperationLogRecord(module = "会员管理", action = "新增会员")
    @PostMapping
    public ApiResponse<MemberUserDto> create(@Valid @RequestBody MemberCreateRequest request) {
        return ApiResponse.success(memberAdminService.createMember(request));
    }

    @Operation(summary = "上传会员资料文件")
    @PreAuthorize("hasAnyAuthority('MEMBER_CREATE_BUTTON', 'MEMBER_EDIT_BUTTON')")
    @OperationLogRecord(module = "会员管理", action = "上传会员资料文件")
    @PostMapping("/profile-files")
    public ApiResponse<List<FileUploadResponse>> uploadProfileFiles(@RequestParam("files") MultipartFile[] files) {
        return ApiResponse.success(fileStorageService.store(Arrays.asList(files)));
    }

    @Operation(summary = "修改会员信息")
    @PreAuthorize("hasAuthority('MEMBER_EDIT_BUTTON')")
    @OperationLogRecord(module = "会员管理", action = "修改会员信息")
    @PutMapping("/{memberId}")
    public ApiResponse<MemberUserDto> update(@PathVariable Long memberId,
                                             @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResponse.success(memberAdminService.updateMember(memberId, request));
    }

    @Operation(summary = "修改会员下载权限")
    @PreAuthorize("hasAuthority('MEMBER_DOWNLOAD_BUTTON')")
    @OperationLogRecord(module = "会员管理", action = "修改会员下载权限")
    @PutMapping("/{memberId}/download-access")
    public ApiResponse<MemberUserDto> updateDownloadAccess(@PathVariable Long memberId,
                                                           @Valid @RequestBody MemberDownloadAccessUpdateRequest request) {
        return ApiResponse.success(memberAdminService.updateDownloadAccess(memberId, request));
    }

    @Operation(summary = "修改会员状态")
    @PreAuthorize("hasAuthority('MEMBER_STATUS_BUTTON')")
    @OperationLogRecord(module = "会员管理", action = "修改会员状态")
    @PutMapping("/{memberId}/status")
    public ApiResponse<MemberUserDto> updateStatus(@PathVariable Long memberId,
                                                   @Valid @RequestBody MemberStatusUpdateRequest request) {
        return ApiResponse.success(memberAdminService.updateStatus(memberId, request));
    }

    @Operation(summary = "重置会员密码")
    @PreAuthorize("hasAuthority('MEMBER_PASSWORD_BUTTON')")
    @OperationLogRecord(module = "会员管理", action = "重置会员密码")
    @PutMapping("/{memberId}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long memberId,
                                           @Valid @RequestBody MemberPasswordResetRequest request) {
        memberAdminService.resetPassword(memberId, request);
        return ApiResponse.success("重置密码成功", null);
    }

    @Operation(summary = "删除会员")
    @PreAuthorize("hasAuthority('MEMBER_DELETE_BUTTON')")
    @OperationLogRecord(module = "会员管理", action = "删除会员")
    @DeleteMapping("/{memberId}")
    public ApiResponse<Void> delete(@PathVariable Long memberId) {
        memberAdminService.deleteMember(memberId);
        return ApiResponse.success("删除会员成功", null);
    }
}
