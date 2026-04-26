package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Tag(name = "管理员-文件管理")
@RestController
@RequestMapping("/api/admin/files")
@PreAuthorize("hasRole('ADMIN')")
public class FileAdminController {

    private final FileStorageService fileStorageService;

    public FileAdminController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "上传招标附件")
    @PreAuthorize("hasAuthority('TENDER_UPLOAD_BUTTON')")
    @OperationLogRecord(module = "招标管理", action = "上传招标附件")
    @PostMapping("/upload")
    public ApiResponse<List<FileUploadResponse>> upload(@RequestParam("files") MultipartFile[] files) {
        return ApiResponse.success(fileStorageService.store(Arrays.asList(files)));
    }
}
