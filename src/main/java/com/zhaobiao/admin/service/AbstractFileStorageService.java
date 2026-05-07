package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import com.zhaobiao.admin.util.FileThumbnailUrlBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

abstract class AbstractFileStorageService {

    protected final TenderFileStorageRepository tenderFileStorageRepository;

    protected AbstractFileStorageService(TenderFileStorageRepository tenderFileStorageRepository) {
        this.tenderFileStorageRepository = tenderFileStorageRepository;
    }

    protected String sanitizeOriginalName(String originalFilename) {
        String fileName = StringUtils.hasText(originalFilename) ? Paths.get(originalFilename).getFileName().toString() : "file";
        String cleaned = fileName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return cleaned.length() == 0 ? "file" : cleaned;
    }

    protected String resolveExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index);
    }

    protected String calculateContentHash(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return toHex(digest.digest());
        } catch (IOException ex) {
            throw new BusinessException(500, "计算文件摘要失败");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", ex);
        }
    }

    protected String calculateContentHash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(content));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", ex);
        }
    }

    protected void applyThumbnail(TenderFileStorage storage, String thumbnailPath, FileThumbnailPayload payload) {
        storage.setThumbnailPath(thumbnailPath);
        storage.setThumbnailContentType(payload.getContentType());
        storage.setThumbnailSize((long) payload.getContent().length);
        storage.setThumbnailWidth(payload.getWidth());
        storage.setThumbnailHeight(payload.getHeight());
        storage.setThumbnailStatus(payload.getStatus());
    }

    protected FileUploadResponse toUploadResponse(TenderFileStorage storage) {
        FileUploadResponse response = new FileUploadResponse();
        response.setFileId(storage.getId());
        response.setFileName(storage.getOriginalName());
        response.setContentType(storage.getContentType());
        response.setFileSize(storage.getFileSize());
        response.setThumbnailUrl(FileThumbnailUrlBuilder.build(storage.getId()));
        response.setThumbnailContentType(storage.getThumbnailContentType());
        response.setThumbnailStatus(storage.getThumbnailStatus());
        return response;
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte item : bytes) {
            builder.append(Character.forDigit((item >> 4) & 0xF, 16));
            builder.append(Character.forDigit(item & 0xF, 16));
        }
        return builder.toString();
    }
}
