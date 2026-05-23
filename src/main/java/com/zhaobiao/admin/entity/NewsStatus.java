package com.zhaobiao.admin.entity;

public enum NewsStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布");

    private final String label;

    NewsStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
