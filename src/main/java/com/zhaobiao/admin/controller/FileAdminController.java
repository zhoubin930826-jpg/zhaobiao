package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import com.zhaobiao.admin.service.FileResponseBuilder;
import com.zhaobiao.admin.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final TenderFileStorageRepository tenderFileStorageRepository;
    private final FileResponseBuilder fileResponseBuilder;

    public FileAdminController(FileStorageService fileStorageService,
                               TenderFileStorageRepository tenderFileStorageRepository,
                               FileResponseBuilder fileResponseBuilder) {
        this.fileStorageService = fileStorageService;
        this.tenderFileStorageRepository = tenderFileStorageRepository;
        this.fileResponseBuilder = fileResponseBuilder;
    }

    @Operation(summary = "上传后台文件")
    @PreAuthorize("hasAnyAuthority('TENDER_UPLOAD_BUTTON','NEWS_UPLOAD_BUTTON')")
    @OperationLogRecord(module = "文件管理", action = "上传文件")
    @PostMapping("/upload")
    public ApiResponse<List<FileUploadResponse>> upload(@RequestParam("files") MultipartFile[] files) {
        return ApiResponse.success(fileStorageService.store(Arrays.asList(files)));
    }

    @Operation(summary = "下载管理员已上传文件")
    @PreAuthorize("hasAnyAuthority('TENDER_UPLOAD_BUTTON','NEWS_UPLOAD_BUTTON','MEMBER_CREATE_BUTTON','MEMBER_EDIT_BUTTON')")
    @OperationLogRecord(module = "文件管理", action = "下载文件")
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        return fileResponseBuilder.download(loadFile(fileId));
    }

    @Operation(summary = "查看管理员已上传文件")
    @PreAuthorize("hasAnyAuthority('TENDER_UPLOAD_BUTTON','NEWS_UPLOAD_BUTTON','MEMBER_CREATE_BUTTON','MEMBER_EDIT_BUTTON')")
    @GetMapping("/{fileId}/view")
    public ResponseEntity<Resource> view(@PathVariable Long fileId) {
        return fileResponseBuilder.inline(loadFile(fileId));
    }

    private TenderFileStorage loadFile(Long fileId) {
        return tenderFileStorageRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
    }
}
