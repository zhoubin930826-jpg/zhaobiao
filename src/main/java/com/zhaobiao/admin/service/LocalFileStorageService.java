package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.config.FileStorageProperties;
import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        String contentHash = calculateContentHash(file);
        TenderFileStorage existing = tenderFileStorageRepository.findByContentHash(contentHash).orElse(null);
        if (existing != null) {
            return toUploadResponse(existing);
        }
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
            storage.setContentHash(contentHash);
            storage.setStorageName(storageName);
            storage.setStoragePath(relativePath);
            storage.setContentType(file.getContentType());
            storage.setFileSize(file.getSize());
            storage = saveStorage(storage, target, contentHash);
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

    private String calculateContentHash(MultipartFile file) {
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

    private TenderFileStorage saveStorage(TenderFileStorage storage, Path target, String contentHash) {
        try {
            return tenderFileStorageRepository.saveAndFlush(storage);
        } catch (DataIntegrityViolationException ex) {
            deleteQuietly(target);
            TenderFileStorage existing = tenderFileStorageRepository.findByContentHash(contentHash).orElse(null);
            if (existing != null) {
                return existing;
            }
            throw new BusinessException(500, "保存文件记录失败");
        } catch (RuntimeException ex) {
            deleteQuietly(target);
            throw ex;
        }
    }

    private void deleteQuietly(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 文件记录保存失败时优先返回主错误，忽略清理失败
        }
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
