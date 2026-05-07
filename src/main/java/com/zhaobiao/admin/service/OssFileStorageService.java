package com.zhaobiao.admin.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.InstanceProfileCredentialsProvider;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.config.FileStorageProperties;
import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "app.file", name = "type", havingValue = "oss")
public class OssFileStorageService extends AbstractFileStorageService implements FileStorageService {

    private static final String CREDENTIAL_MODE_ACCESS_KEY = "access-key";

    private static final String CREDENTIAL_MODE_ECS_RAM_ROLE = "ecs-ram-role";

    private final FileStorageProperties fileStorageProperties;
    private final TenderFileStorageRepository tenderFileStorageRepository;
    private final OssClientFactory ossClientFactory;
    private final FileThumbnailGenerator fileThumbnailGenerator;

    private OSS ossClient;

    private String bucketName;

    private String keyPrefix;

    public OssFileStorageService(FileStorageProperties fileStorageProperties,
                                 TenderFileStorageRepository tenderFileStorageRepository,
                                 OssClientFactory ossClientFactory,
                                 FileThumbnailGenerator fileThumbnailGenerator) {
        super(tenderFileStorageRepository);
        this.fileStorageProperties = fileStorageProperties;
        this.tenderFileStorageRepository = tenderFileStorageRepository;
        this.ossClientFactory = ossClientFactory;
        this.fileThumbnailGenerator = fileThumbnailGenerator;
    }

    @PostConstruct
    public void init() {
        String endpoint = normalizeEndpoint(fileStorageProperties.getOssEndpoint());
        String accessKeyId = normalize(fileStorageProperties.getOssAccessKeyId());
        String accessKeySecret = normalize(fileStorageProperties.getOssAccessKeySecret());
        this.bucketName = requireText(fileStorageProperties.getOssBucket(), "必须配置 app.file.oss-bucket 或 APP_FILE_OSS_BUCKET");
        this.keyPrefix = normalizePrefix(fileStorageProperties.getOssKeyPrefix());

        String credentialMode = normalizeCredentialMode(fileStorageProperties.getOssCredentialMode());
        if (CREDENTIAL_MODE_ECS_RAM_ROLE.equals(credentialMode)) {
            String roleName = requireText(fileStorageProperties.getOssRoleName(), "启用 OSS 的 ECS RAM Role 凭证模式时，必须配置 APP_FILE_OSS_ROLE_NAME");
            CredentialsProvider credentialsProvider = new InstanceProfileCredentialsProvider(roleName);
            this.ossClient = ossClientFactory.buildWithCredentialsProvider(endpoint, credentialsProvider);
            return;
        }

        if (CREDENTIAL_MODE_ACCESS_KEY.equals(credentialMode)) {
            if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
                throw new IllegalStateException("启用 OSS 存储时，必须配置 APP_FILE_OSS_ACCESS_KEY_ID 和 APP_FILE_OSS_ACCESS_KEY_SECRET");
            }
            this.ossClient = ossClientFactory.buildWithAccessKey(endpoint, accessKeyId, accessKeySecret);
            return;
        }

        throw new IllegalStateException("不支持的 OSS 凭证模式: " + fileStorageProperties.getOssCredentialMode());
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
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
        String objectKey = buildObjectKey(dateDir, storageName);
        String thumbnailObjectKey = buildObjectKey(dateDir, "thumbnails/" + stripExtension(storageName) + ".jpg");
        FileThumbnailPayload thumbnail = fileThumbnailGenerator.generate(originalName, file.getContentType(), content);

        boolean originalUploaded = false;
        boolean thumbnailUploaded = false;
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (StringUtils.hasText(file.getContentType())) {
                metadata.setContentType(file.getContentType());
            }

            PutObjectRequest request = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);
            ossClient.putObject(request);
            originalUploaded = true;
            putThumbnailObject(thumbnailObjectKey, thumbnail);
            thumbnailUploaded = true;

            TenderFileStorage storage = new TenderFileStorage();
            storage.setOriginalName(originalName);
            storage.setContentHash(contentHash);
            storage.setStorageName(storageName);
            storage.setStoragePath(objectKey);
            storage.setContentType(file.getContentType());
            storage.setFileSize(file.getSize());
            applyThumbnail(storage, thumbnailObjectKey, thumbnail);
            storage = saveStorage(storage, objectKey, thumbnailObjectKey, contentHash, content);
            return toUploadResponse(storage);
        } catch (BusinessException ex) {
            cleanupUploadedObjects(originalUploaded, objectKey, thumbnailUploaded, thumbnailObjectKey);
            throw ex;
        } catch (IOException ex) {
            cleanupUploadedObjects(originalUploaded, objectKey, thumbnailUploaded, thumbnailObjectKey);
            throw new BusinessException(500, "读取上传文件失败");
        } catch (OSSException ex) {
            cleanupUploadedObjects(originalUploaded, objectKey, thumbnailUploaded, thumbnailObjectKey);
            throw new BusinessException(500, "上传文件到 OSS 失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadAsResource(TenderFileStorage storage) {
        try {
            OSSObject object = ossClient.getObject(bucketName, storage.getStoragePath());
            return new InputStreamResource(object.getObjectContent());
        } catch (OSSException ex) {
            if ("NoSuchKey".equalsIgnoreCase(ex.getErrorCode())) {
                throw new BusinessException(404, "附件文件不存在或不可读取");
            }
            throw new BusinessException(500, "读取 OSS 附件文件失败");
        }
    }

    @Override
    @Transactional
    public FileThumbnailResource loadThumbnail(TenderFileStorage storage) {
        TenderFileStorage current = storage;
        if (!StringUtils.hasText(current.getThumbnailPath())) {
            current = ensureThumbnailFromOriginal(current);
        }
        try {
            OSSObject object = ossClient.getObject(bucketName, current.getThumbnailPath());
            long contentLength = current.getThumbnailSize() == null ? -1 : current.getThumbnailSize();
            return new FileThumbnailResource(new InputStreamResource(object.getObjectContent()), current.getThumbnailContentType(), contentLength);
        } catch (OSSException ex) {
            if (!"NoSuchKey".equalsIgnoreCase(ex.getErrorCode())) {
                throw new BusinessException(500, "读取 OSS 附件缩略图失败");
            }
            current = ensureThumbnailFromOriginal(current);
            try {
                OSSObject object = ossClient.getObject(bucketName, current.getThumbnailPath());
                long contentLength = current.getThumbnailSize() == null ? -1 : current.getThumbnailSize();
                return new FileThumbnailResource(new InputStreamResource(object.getObjectContent()), current.getThumbnailContentType(), contentLength);
            } catch (OSSException retryEx) {
                if ("NoSuchKey".equalsIgnoreCase(retryEx.getErrorCode())) {
                    throw new BusinessException(404, "附件缩略图不存在或不可读取");
                }
                throw new BusinessException(500, "读取 OSS 附件缩略图失败");
            }
        }
    }

    @Override
    public void deleteStoredFile(TenderFileStorage storage) {
        try {
            ossClient.deleteObject(bucketName, storage.getStoragePath());
            if (StringUtils.hasText(storage.getThumbnailPath())) {
                ossClient.deleteObject(bucketName, storage.getThumbnailPath());
            }
        } catch (OSSException ex) {
            throw new BusinessException(500, "删除 OSS 附件文件失败");
        }
    }

    private TenderFileStorage saveStorage(TenderFileStorage storage,
                                          String objectKey,
                                          String thumbnailObjectKey,
                                          String contentHash,
                                          byte[] content) {
        try {
            return tenderFileStorageRepository.saveAndFlush(storage);
        } catch (DataIntegrityViolationException ex) {
            deleteQuietly(objectKey);
            deleteQuietly(thumbnailObjectKey);
            TenderFileStorage existing = tenderFileStorageRepository.findByContentHash(contentHash).orElse(null);
            if (existing != null) {
                return ensureThumbnailFromBytes(existing, content);
            }
            throw new BusinessException(500, "保存文件记录失败");
        } catch (RuntimeException ex) {
            deleteQuietly(objectKey);
            deleteQuietly(thumbnailObjectKey);
            throw ex;
        }
    }

    private TenderFileStorage ensureThumbnailFromOriginal(TenderFileStorage storage) {
        return ensureThumbnailFromBytes(storage, readOssObject(storage.getStoragePath()));
    }

    private TenderFileStorage ensureThumbnailFromBytes(TenderFileStorage storage, byte[] content) {
        if (StringUtils.hasText(storage.getThumbnailPath()) && ossObjectExists(storage.getThumbnailPath())) {
            return storage;
        }
        FileThumbnailPayload thumbnail = fileThumbnailGenerator.generate(
                storage.getOriginalName(),
                storage.getContentType(),
                content
        );
        String thumbnailObjectKey = buildThumbnailObjectKey(storage);
        putThumbnailObject(thumbnailObjectKey, thumbnail);
        applyThumbnail(storage, thumbnailObjectKey, thumbnail);
        return tenderFileStorageRepository.saveAndFlush(storage);
    }

    private void putThumbnailObject(String objectKey, FileThumbnailPayload thumbnail) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(thumbnail.getContent().length);
        metadata.setContentType(thumbnail.getContentType());
        try (InputStream inputStream = new ByteArrayInputStream(thumbnail.getContent())) {
            ossClient.putObject(new PutObjectRequest(bucketName, objectKey, inputStream, metadata));
        } catch (IOException ex) {
            throw new BusinessException(500, "读取附件缩略图失败");
        } catch (OSSException ex) {
            throw new BusinessException(500, "上传附件缩略图到 OSS 失败");
        }
    }

    private byte[] readOssObject(String objectKey) {
        try {
            OSSObject object = ossClient.getObject(bucketName, objectKey);
            try (InputStream inputStream = object.getObjectContent()) {
                return toByteArray(inputStream);
            }
        } catch (OSSException ex) {
            if ("NoSuchKey".equalsIgnoreCase(ex.getErrorCode())) {
                throw new BusinessException(404, "附件文件不存在或不可读取");
            }
            throw new BusinessException(500, "读取 OSS 附件文件失败");
        } catch (IOException ex) {
            throw new BusinessException(500, "读取 OSS 附件文件失败");
        }
    }

    private boolean ossObjectExists(String objectKey) {
        try {
            return ossClient.doesObjectExist(bucketName, objectKey);
        } catch (OSSException ex) {
            return false;
        }
    }

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException(500, "读取上传文件失败");
        }
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private String buildThumbnailObjectKey(TenderFileStorage storage) {
        String storagePath = storage.getStoragePath();
        int index = storagePath.lastIndexOf('/');
        String directory = index < 0 ? "" : storagePath.substring(0, index + 1);
        return directory + "thumbnails/" + stripExtension(storage.getStorageName()) + ".jpg";
    }

    private String stripExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index <= 0) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    private void deleteQuietly(String objectKey) {
        try {
            ossClient.deleteObject(bucketName, objectKey);
        } catch (Exception ignored) {
            // 文件记录保存失败时优先返回主错误，忽略清理失败
        }
    }

    private void cleanupUploadedObjects(boolean originalUploaded,
                                        String objectKey,
                                        boolean thumbnailUploaded,
                                        String thumbnailObjectKey) {
        if (originalUploaded) {
            deleteQuietly(objectKey);
        }
        if (thumbnailUploaded) {
            deleteQuietly(thumbnailObjectKey);
        }
    }

    private String buildObjectKey(String dateDir, String storageName) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(keyPrefix)) {
            builder.append(keyPrefix).append('/');
        }
        builder.append(dateDir).append('/').append(storageName);
        return builder.toString();
    }

    private String normalizeEndpoint(String endpoint) {
        String value = requireText(endpoint, "必须配置 app.file.oss-endpoint 或 APP_FILE_OSS_ENDPOINT");
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return "https://" + value;
    }

    private String normalizePrefix(String prefix) {
        String value = normalize(prefix);
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value;
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String requireText(String value, String message) {
        String normalized = normalize(value);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalStateException(message);
        }
        return normalized;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeCredentialMode(String credentialMode) {
        String value = normalize(credentialMode);
        if (!StringUtils.hasText(value)) {
            return CREDENTIAL_MODE_ACCESS_KEY;
        }
        return value.toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
