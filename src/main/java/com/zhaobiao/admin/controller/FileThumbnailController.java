package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import com.zhaobiao.admin.service.FileStorageService;
import com.zhaobiao.admin.service.FileThumbnailResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Tag(name = "文件缩略图")
@RestController
@RequestMapping("/api/files")
public class FileThumbnailController {

    private final TenderFileStorageRepository tenderFileStorageRepository;
    private final FileStorageService fileStorageService;

    public FileThumbnailController(TenderFileStorageRepository tenderFileStorageRepository,
                                   FileStorageService fileStorageService) {
        this.tenderFileStorageRepository = tenderFileStorageRepository;
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "读取文件缩略图")
    @GetMapping("/{fileId}/thumbnail")
    public ResponseEntity<Resource> thumbnail(@PathVariable Long fileId) {
        TenderFileStorage storage = tenderFileStorageRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
        FileThumbnailResource thumbnail = fileStorageService.loadThumbnail(storage);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(resolveMediaType(thumbnail.getContentType()))
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
        if (thumbnail.getContentLength() >= 0) {
            builder.contentLength(thumbnail.getContentLength());
        }
        return builder.body(thumbnail.getResource());
    }

    private MediaType resolveMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.IMAGE_JPEG;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            return MediaType.IMAGE_JPEG;
        }
    }
}
