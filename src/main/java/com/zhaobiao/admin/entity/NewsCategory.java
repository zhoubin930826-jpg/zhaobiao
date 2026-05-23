package com.zhaobiao.admin.entity;

public enum NewsCategory {
    PLATFORM_NOTICE("平台公告"),
    INDUSTRY_NEWS("行业动态"),
    SERVICE_GUIDE("办事指南"),
    POLICY_REGULATION("政策法规");

    private final String label;

    NewsCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
