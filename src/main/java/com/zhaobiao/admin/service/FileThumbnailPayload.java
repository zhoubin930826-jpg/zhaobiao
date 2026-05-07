package com.zhaobiao.admin.service;

import com.zhaobiao.admin.entity.FileThumbnailStatus;

public class FileThumbnailPayload {

    private final byte[] content;
    private final String contentType;
    private final int width;
    private final int height;
    private final FileThumbnailStatus status;

    public FileThumbnailPayload(byte[] content,
                                String contentType,
                                int width,
                                int height,
                                FileThumbnailStatus status) {
        this.content = content;
        this.contentType = contentType;
        this.width = width;
        this.height = height;
        this.status = status;
    }

    public byte[] getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public FileThumbnailStatus getStatus() {
        return status;
    }
}
