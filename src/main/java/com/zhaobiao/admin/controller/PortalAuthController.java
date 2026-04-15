package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.member.MemberLoginRequest;
import com.zhaobiao.admin.dto.member.MemberLoginResponse;
import com.zhaobiao.admin.dto.member.MemberRegisterRequest;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.security.MemberLoginUser;
import com.zhaobiao.admin.service.PortalAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "门户会员认证")
@Validated
@RestController
@RequestMapping("/api/portal/auth")
public class PortalAuthController {

    private final PortalAuthService portalAuthService;

    public PortalAuthController(PortalAuthService portalAuthService) {
        this.portalAuthService = portalAuthService;
    }

    @Operation(summary = "会员注册")
    @OperationLogRecord(module = "门户会员", action = "会员注册")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody MemberRegisterRequest request) {
        throw new BusinessException(403, "会员在线注册已停用，请联系管理员发放账号");
    }

    @Operation(summary = "会员登录")
    @OperationLogRecord(module = "门户会员", action = "会员登录")
    @PostMapping("/login")
    public ApiResponse<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        return ApiResponse.success(portalAuthService.login(request));
    }

    @Operation(summary = "获取当前会员信息")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ApiResponse<MemberUserDto> me(@AuthenticationPrincipal MemberLoginUser loginUser) {
        return ApiResponse.success(portalAuthService.currentMember(loginUser));
    }
}
