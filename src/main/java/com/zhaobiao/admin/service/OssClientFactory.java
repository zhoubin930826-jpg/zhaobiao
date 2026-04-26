package com.zhaobiao.admin.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.auth.CredentialsProvider;

public interface OssClientFactory {

    OSS buildWithAccessKey(String endpoint, String accessKeyId, String accessKeySecret);

    OSS buildWithCredentialsProvider(String endpoint, CredentialsProvider credentialsProvider);
}
