package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.config.FileStorageProperties;
import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocalFileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private final TenderFileStorageRepository tenderFileStorageRepository;

    private Path storageRoot;

    private Path tempRoot;

    public LocalFileStorageService(FileStorageProperties fileStorageProperties,
                                   TenderFileStorageRepository tenderFileStorageRepository) {
        this.fileStorageProperties = fileStorageProperties;
        this.tenderFileStorageRepository = tenderFileStorageRepository;
    }

    @PostConstruct
    public void init() {
        try {
            this.storageRoot = Paths.get(fileStorageProperties.getStoragePath()).toAbsolutePath().normalize();
            this.tempRoot = Paths.get(fileStorageProperties.getTempPath()).toAbsolutePath().normalize();
            Files.createDirectories(storageRoot);
            Files.createDirectories(tempRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("初始化文件存储目录失败", ex);
        }
    }

    @Transactional
    public List<FileUploadResponse> store(List<MultipartFile> files) {
        return files.stream().map(this::store).collect(Collectors.toList());
    }

    @Transactional
    public FileUploadResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String extension = resolveExtension(originalName);
        String storageName = UUID.randomUUID().toString().replace("-", "") + extension;
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String relativePath = dateDir + "/" + storageName;

        try {
            Path targetDir = storageRoot.resolve(dateDir);
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(storageName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            TenderFileStorage storage = new TenderFileStorage();
            storage.setOriginalName(originalName);
            storage.setStorageName(storageName);
            storage.setStoragePath(relativePath);
            storage.setContentType(file.getContentType());
            storage.setFileSize(file.getSize());
            storage = tenderFileStorageRepository.save(storage);
            return toUploadResponse(storage);
        } catch (IOException ex) {
            throw new BusinessException(500, "保存文件失败");
        }
    }

    @Transactional(readOnly = true)
    public TenderFileStorage getFile(Long fileId) {
        return tenderFileStorageRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "附件文件不存在"));
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(TenderFileStorage storage) {
        try {
            Path filePath = storageRoot.resolve(storage.getStoragePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(404, "附件文件不存在或不可读取");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new BusinessException(500, "读取附件文件失败");
        }
    }

    public void deleteStoredFile(TenderFileStorage storage) {
        try {
            Files.deleteIfExists(storageRoot.resolve(storage.getStoragePath()).normalize());
        } catch (IOException ex) {
            throw new BusinessException(500, "删除附件文件失败");
        }
    }

    private FileUploadResponse toUploadResponse(TenderFileStorage storage) {
        FileUploadResponse response = new FileUploadResponse();
        response.setFileId(storage.getId());
        response.setFileName(storage.getOriginalName());
        response.setContentType(storage.getContentType());
        response.setFileSize(storage.getFileSize());
        return response;
    }

    private String sanitizeOriginalName(String originalFilename) {
        String fileName = StringUtils.hasText(originalFilename) ? Paths.get(originalFilename).getFileName().toString() : "file";
        String cleaned = fileName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return cleaned.length() == 0 ? "file" : cleaned;
    }

    private String resolveExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index);
    }
}
