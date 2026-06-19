package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.service.CaptchaChallenge;
import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.dto.member.MemberLoginRequest;
import com.zhaobiao.admin.dto.member.MemberLoginResponse;
import com.zhaobiao.admin.dto.member.MemberPasswordChangeRequest;
import com.zhaobiao.admin.dto.member.MemberProfileUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberRegistrationSettingDto;
import com.zhaobiao.admin.dto.member.MemberRegisterRequest;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.security.MemberLoginUser;
import com.zhaobiao.admin.service.FileStorageService;
import com.zhaobiao.admin.service.PortalAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

@Tag(name = "门户会员认证")
@Validated
@RestController
@RequestMapping("/api/portal/auth")
public class PortalAuthController {

    private final PortalAuthService portalAuthService;
    private final FileStorageService fileStorageService;

    public PortalAuthController(PortalAuthService portalAuthService,
                                FileStorageService fileStorageService) {
        this.portalAuthService = portalAuthService;
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "获取门户验证码图片")
    @GetMapping(value = "/captcha", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> captcha(@RequestParam String scene,
                                          @RequestParam String captchaId) {
        CaptchaChallenge challenge = portalAuthService.createCaptcha(scene, captchaId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .contentType(MediaType.IMAGE_PNG)
                .body(challenge.getImage());
    }

    @Operation(summary = "查询会员注册开关")
    @GetMapping("/registration-status")
    public ApiResponse<MemberRegistrationSettingDto> registrationStatus() {
        return ApiResponse.success(portalAuthService.getRegistrationSetting());
    }

    @Operation(summary = "会员注册")
    @OperationLogRecord(module = "门户会员", action = "会员注册")
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MemberUserDto> register(@Valid @ModelAttribute MemberRegisterRequest request) {
        return ApiResponse.success("注册成功，请等待管理员启用账号", portalAuthService.register(request));
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

    @Operation(summary = "上传会员资料文件")
    @OperationLogRecord(module = "门户会员", action = "上传会员资料文件")
    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/profile/files")
    public ApiResponse<List<FileUploadResponse>> uploadProfileFiles(@RequestParam("files") MultipartFile[] files) {
        return ApiResponse.success(fileStorageService.store(Arrays.asList(files)));
    }

    @Operation(summary = "更新当前会员资料")
    @OperationLogRecord(module = "门户会员", action = "更新当前会员资料")
    @PreAuthorize("hasRole('MEMBER')")
    @PutMapping("/profile")
    public ApiResponse<MemberUserDto> updateProfile(@AuthenticationPrincipal MemberLoginUser loginUser,
                                                    @Valid @RequestBody MemberProfileUpdateRequest request) {
        return ApiResponse.success(portalAuthService.updateCurrentMember(loginUser, request));
    }

    @Operation(summary = "修改当前会员密码")
    @OperationLogRecord(module = "门户会员", action = "修改当前会员密码")
    @PreAuthorize("hasRole('MEMBER')")
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal MemberLoginUser loginUser,
                                            @Valid @RequestBody MemberPasswordChangeRequest request) {
        portalAuthService.changeCurrentMemberPassword(loginUser, request);
        return ApiResponse.success("修改密码成功", null);
    }
}
