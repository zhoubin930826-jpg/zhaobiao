package com.zhaobiao.admin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "biz_file_storage")
public class TenderFileStorage extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(length = 64, unique = true)
    private String contentHash;

    @Column(nullable = false, length = 255)
    private String storageName;

    @Column(nullable = false, length = 512)
    private String storagePath;

    @Column(length = 128)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(name = "thumbnail_path", length = 512)
    private String thumbnailPath;

    @Column(name = "thumbnail_content_type", length = 128)
    private String thumbnailContentType;

    @Column(name = "thumbnail_size")
    private Long thumbnailSize;

    @Column(name = "thumbnail_width")
    private Integer thumbnailWidth;

    @Column(name = "thumbnail_height")
    private Integer thumbnailHeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "thumbnail_status", length = 32)
    private FileThumbnailStatus thumbnailStatus;

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getThumbnailContentType() {
        return thumbnailContentType;
    }

    public void setThumbnailContentType(String thumbnailContentType) {
        this.thumbnailContentType = thumbnailContentType;
    }

    public Long getThumbnailSize() {
        return thumbnailSize;
    }

    public void setThumbnailSize(Long thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    public Integer getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(Integer thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public Integer getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(Integer thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public FileThumbnailStatus getThumbnailStatus() {
        return thumbnailStatus;
    }

    public void setThumbnailStatus(FileThumbnailStatus thumbnailStatus) {
        this.thumbnailStatus = thumbnailStatus;
    }
}
