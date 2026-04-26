package com.zhaobiao.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

    private String type = "local";

    private String storagePath;

    private String tempPath;

    private String ossBucket;

    private String ossEndpoint;

    private String ossAccessKeyId;

    private String ossAccessKeySecret;

    private String ossCredentialMode = "access-key";

    private String ossRoleName;

    private String ossKeyPrefix = "zb/files";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public String getOssBucket() {
        return ossBucket;
    }

    public void setOssBucket(String ossBucket) {
        this.ossBucket = ossBucket;
    }

    public String getOssEndpoint() {
        return ossEndpoint;
    }

    public void setOssEndpoint(String ossEndpoint) {
        this.ossEndpoint = ossEndpoint;
    }

    public String getOssAccessKeyId() {
        return ossAccessKeyId;
    }

    public void setOssAccessKeyId(String ossAccessKeyId) {
        this.ossAccessKeyId = ossAccessKeyId;
    }

    public String getOssAccessKeySecret() {
        return ossAccessKeySecret;
    }

    public void setOssAccessKeySecret(String ossAccessKeySecret) {
        this.ossAccessKeySecret = ossAccessKeySecret;
    }

    public String getOssCredentialMode() {
        return ossCredentialMode;
    }

    public void setOssCredentialMode(String ossCredentialMode) {
        this.ossCredentialMode = ossCredentialMode;
    }

    public String getOssRoleName() {
        return ossRoleName;
    }

    public void setOssRoleName(String ossRoleName) {
        this.ossRoleName = ossRoleName;
    }

    public String getOssKeyPrefix() {
        return ossKeyPrefix;
    }

    public void setOssKeyPrefix(String ossKeyPrefix) {
        this.ossKeyPrefix = ossKeyPrefix;
    }
}
