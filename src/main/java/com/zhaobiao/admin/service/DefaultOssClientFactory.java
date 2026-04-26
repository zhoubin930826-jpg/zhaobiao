package com.zhaobiao.admin.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import org.springframework.stereotype.Component;

@Component
public class DefaultOssClientFactory implements OssClientFactory {

    @Override
    public OSS buildWithAccessKey(String endpoint, String accessKeyId, String accessKeySecret) {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @Override
    public OSS buildWithCredentialsProvider(String endpoint, CredentialsProvider credentialsProvider) {
        return new OSSClientBuilder().build(endpoint, credentialsProvider);
    }
}
