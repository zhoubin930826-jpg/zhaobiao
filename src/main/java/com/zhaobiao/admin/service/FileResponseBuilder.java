package com.zhaobiao.admin.service;

import com.zhaobiao.admin.entity.TenderFileStorage;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class FileResponseBuilder {

    private final FileStorageService fileStorageService;

    public FileResponseBuilder(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public ResponseEntity<Resource> download(TenderFileStorage storage) {
        return build(storage, "attachment");
    }

    public ResponseEntity<Resource> inline(TenderFileStorage storage) {
        return build(storage, "inline");
    }

    private ResponseEntity<Resource> build(TenderFileStorage storage, String dispositionType) {
        Resource resource = fileStorageService.loadAsResource(storage);
        return ResponseEntity.ok()
                .contentType(resolveMediaType(storage.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(dispositionType, storage.getOriginalName()))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(storage.getFileSize()))
                .body(resource);
    }

    private MediaType resolveMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String buildContentDisposition(String dispositionType, String fileName) {
        try {
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
            return dispositionType + "; filename*=UTF-8''" + encoded;
        } catch (UnsupportedEncodingException ex) {
            return dispositionType;
        }
    }
}
