package com.zhaobiao.admin.service;

import org.springframework.core.io.Resource;

public class FileThumbnailResource {

    private final Resource resource;
    private final String contentType;
    private final long contentLength;

    public FileThumbnailResource(Resource resource, String contentType, long contentLength) {
        this.resource = resource;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    public Resource getResource() {
        return resource;
    }

    public String getContentType() {
        return contentType;
    }

    public long getContentLength() {
        return contentLength;
    }
}
