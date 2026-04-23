package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.config.FileStorageProperties;
import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@ConditionalOnProperty(prefix = "app.file", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService extends AbstractFileStorageService implements FileStorageService {

    private final FileStorageProperties fileStorageProperties;

    private Path storageRoot;

    private Path tempRoot;

    public LocalFileStorageService(FileStorageProperties fileStorageProperties,
                                   TenderFileStorageRepository tenderFileStorageRepository) {
        super(tenderFileStorageRepository);
        this.fileStorageProperties = fileStorageProperties;
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

    @Override
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

    @Override
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

    @Override
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
}
