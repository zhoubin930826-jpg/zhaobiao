package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.auth.LoginRequest;
import com.zhaobiao.admin.dto.auth.LoginResponse;
import com.zhaobiao.admin.dto.auth.RegisterRequest;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.security.LoginUser;
import com.zhaobiao.admin.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "认证接口")
@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户注册")
    @OperationLogRecord(module = "认证", action = "用户注册")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("注册成功，请等待管理员审核", null);
    }

    @Operation(summary = "用户登录")
    @OperationLogRecord(module = "认证", action = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public ApiResponse<UserProfileDto> me(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(authService.currentUser(loginUser));
    }
}
