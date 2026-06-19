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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "app.file", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService extends AbstractFileStorageService implements FileStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final FileStorageProperties fileStorageProperties;
    private final FileThumbnailGenerator fileThumbnailGenerator;

    private Path storageRoot;

    private Path tempRoot;

    public LocalFileStorageService(FileStorageProperties fileStorageProperties,
                                   TenderFileStorageRepository tenderFileStorageRepository,
                                   FileThumbnailGenerator fileThumbnailGenerator) {
        super(tenderFileStorageRepository);
        this.fileStorageProperties = fileStorageProperties;
        this.fileThumbnailGenerator = fileThumbnailGenerator;
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
        byte[] content = readFileBytes(file);
        String contentHash = calculateContentHash(content);
        TenderFileStorage existing = tenderFileStorageRepository.findByContentHash(contentHash).orElse(null);
        if (existing != null) {
            ensureThumbnailFromBytes(existing, content);
            return toUploadResponse(existing);
        }
        String extension = resolveExtension(originalName);
        String storageName = UUID.randomUUID().toString().replace("-", "") + extension;
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String relativePath = dateDir + "/" + storageName;
        String thumbnailRelativePath = dateDir + "/thumbnails/" + stripExtension(storageName) + ".jpg";
        FileThumbnailPayload thumbnail = fileThumbnailGenerator.generate(originalName, file.getContentType(), content);

        Path target = null;
        Path thumbnailTarget = null;
        try {
            Path targetDir = storageRoot.resolve(dateDir);
            Files.createDirectories(targetDir);
            target = targetDir.resolve(storageName);
            Files.write(target, content);

            thumbnailTarget = storageRoot.resolve(thumbnailRelativePath).normalize();
            Files.createDirectories(thumbnailTarget.getParent());
            Files.write(thumbnailTarget, thumbnail.getContent());

            TenderFileStorage storage = new TenderFileStorage();
            storage.setOriginalName(originalName);
            storage.setContentHash(contentHash);
            storage.setStorageName(storageName);
            storage.setStoragePath(relativePath);
            storage.setContentType(file.getContentType());
            storage.setFileSize(file.getSize());
            applyThumbnail(storage, thumbnailRelativePath, thumbnail);
            storage = saveStorage(storage, target, thumbnailTarget, contentHash, content);
            return toUploadResponse(storage);
        } catch (IOException ex) {
            if (target != null) {
                deleteQuietly(target);
            }
            if (thumbnailTarget != null) {
                deleteQuietly(thumbnailTarget);
            }
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
    @Transactional
    public FileThumbnailResource loadThumbnail(TenderFileStorage storage) {
        TenderFileStorage current = ensureThumbnailAvailable(storage);
        try {
            Path filePath = storageRoot.resolve(current.getThumbnailPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(404, "附件缩略图不存在或不可读取");
            }
            long contentLength = current.getThumbnailSize() == null ? resource.contentLength() : current.getThumbnailSize();
            return new FileThumbnailResource(resource, current.getThumbnailContentType(), contentLength);
        } catch (MalformedURLException ex) {
            throw new BusinessException(500, "读取附件缩略图失败");
        } catch (IOException ex) {
            throw new BusinessException(500, "读取附件缩略图失败");
        }
    }

    @Override
    public void deleteStoredFile(TenderFileStorage storage) {
        try {
            Files.deleteIfExists(storageRoot.resolve(storage.getStoragePath()).normalize());
            if (StringUtils.hasText(storage.getThumbnailPath())) {
                Files.deleteIfExists(storageRoot.resolve(storage.getThumbnailPath()).normalize());
            }
        } catch (IOException ex) {
            throw new BusinessException(500, "删除附件文件失败");
        }
    }

    private TenderFileStorage saveStorage(TenderFileStorage storage,
                                          Path target,
                                          Path thumbnailTarget,
                                          String contentHash,
                                          byte[] content) {
        try {
            return tenderFileStorageRepository.saveAndFlush(storage);
        } catch (DataIntegrityViolationException ex) {
            deleteQuietly(target);
            deleteQuietly(thumbnailTarget);
            TenderFileStorage existing = tenderFileStorageRepository.findByContentHash(contentHash).orElse(null);
            if (existing != null) {
                return ensureThumbnailFromBytes(existing, content);
            }
            throw new BusinessException(500, "保存文件记录失败");
        } catch (RuntimeException ex) {
            deleteQuietly(target);
            deleteQuietly(thumbnailTarget);
            throw ex;
        }
    }

    private TenderFileStorage ensureThumbnailAvailable(TenderFileStorage storage) {
        if (StringUtils.hasText(storage.getThumbnailPath())) {
            Path thumbnail = storageRoot.resolve(storage.getThumbnailPath()).normalize();
            if (Files.exists(thumbnail) && Files.isReadable(thumbnail)) {
                return storage;
            }
        }
        try {
            Path original = storageRoot.resolve(storage.getStoragePath()).normalize();
            if (!Files.exists(original) || !Files.isReadable(original)) {
                throw new BusinessException(404, "附件文件不存在或不可读取");
            }
            return ensureThumbnailFromBytes(storage, Files.readAllBytes(original));
        } catch (IOException ex) {
            throw new BusinessException(500, "生成附件缩略图失败");
        }
    }

    private TenderFileStorage ensureThumbnailFromBytes(TenderFileStorage storage, byte[] content) {
        if (StringUtils.hasText(storage.getThumbnailPath())) {
            Path existing = storageRoot.resolve(storage.getThumbnailPath()).normalize();
            if (Files.exists(existing) && Files.isReadable(existing)) {
                return storage;
            }
        }
        FileThumbnailPayload thumbnail = fileThumbnailGenerator.generate(
                storage.getOriginalName(),
                storage.getContentType(),
                content
        );
        String thumbnailRelativePath = buildThumbnailRelativePath(storage);
        try {
            Path thumbnailTarget = storageRoot.resolve(thumbnailRelativePath).normalize();
            Files.createDirectories(thumbnailTarget.getParent());
            Files.write(thumbnailTarget, thumbnail.getContent());
            applyThumbnail(storage, thumbnailRelativePath, thumbnail);
            return tenderFileStorageRepository.saveAndFlush(storage);
        } catch (IOException ex) {
            throw new BusinessException(500, "保存附件缩略图失败");
        }
    }

    private String buildThumbnailRelativePath(TenderFileStorage storage) {
        Path relative = Paths.get(storage.getStoragePath());
        Path parent = relative.getParent();
        String directory = parent == null
                ? LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                : parent.toString().replace('\\', '/');
        return directory + "/thumbnails/" + stripExtension(storage.getStorageName()) + ".jpg";
    }

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException(500, "读取上传文件失败");
        }
    }

    private String stripExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index <= 0) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    private void deleteQuietly(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            LOGGER.warn("清理本地文件失败: path={}", target, ex);
        }
    }
}
