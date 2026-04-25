package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.user.ProfileUpdateRequest;
import com.zhaobiao.admin.dto.user.UserProfileDto;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.security.LoginUser;
import com.zhaobiao.admin.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "获取个人信息")
    @PreAuthorize("hasAuthority('PROFILE')")
    @GetMapping
    public ApiResponse<UserProfileDto> getProfile(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(profileService.getProfile(loginUser));
    }

    @Operation(summary = "修改个人信息")
    @OperationLogRecord(module = "个人中心", action = "修改个人信息")
    @PreAuthorize("hasAuthority('PROFILE_EDIT_BUTTON')")
    @PutMapping
    public ApiResponse<UserProfileDto> updateProfile(@AuthenticationPrincipal LoginUser loginUser,
                                                     @Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.success(profileService.updateProfile(loginUser, request));
    }
}
