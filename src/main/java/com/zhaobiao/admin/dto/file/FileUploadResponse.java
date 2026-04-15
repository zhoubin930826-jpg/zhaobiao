package com.zhaobiao.admin.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "文件上传响应")
public class FileUploadResponse {

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件类型")
    private String contentType;

    @Schema(description = "文件大小，单位字节")
    private long fileSize;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
}
