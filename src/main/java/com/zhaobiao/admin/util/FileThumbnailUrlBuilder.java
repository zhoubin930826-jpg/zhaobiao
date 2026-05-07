package com.zhaobiao.admin.util;

public final class FileThumbnailUrlBuilder {

    private FileThumbnailUrlBuilder() {
    }

    public static String build(Long fileId) {
        return fileId == null ? null : "/api/files/" + fileId + "/thumbnail";
    }
}
