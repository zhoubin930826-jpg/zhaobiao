package com.zhaobiao.admin.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.InstanceProfileCredentialsProvider;
import com.zhaobiao.admin.config.FileStorageProperties;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OssFileStorageServiceCredentialTests {

    @Test
    void ecsRamRoleModeBuildsOssClientWithoutAccessKeys() {
        FileStorageProperties properties = baseProperties();
        properties.setOssCredentialMode("ecs-ram-role");
        properties.setOssRoleName("ECSOSS");
        CapturingOssClientFactory factory = new CapturingOssClientFactory();

        OssFileStorageService service = new OssFileStorageService(
                properties,
                mock(TenderFileStorageRepository.class),
                factory);

        service.init();
        service.destroy();

        assertEquals("https://oss-cn-hangzhou-internal.aliyuncs.com", factory.endpoint);
        assertEquals(0, factory.accessKeyBuildCount);
        assertEquals(1, factory.credentialsProviderBuildCount);
        assertInstanceOf(InstanceProfileCredentialsProvider.class, factory.credentialsProvider);
        verify(factory.ossClient).shutdown();
    }

    @Test
    void ecsRamRoleModeRequiresRoleName() {
        FileStorageProperties properties = baseProperties();
        properties.setOssCredentialMode("ecs-ram-role");

        OssFileStorageService service = new OssFileStorageService(
                properties,
                mock(TenderFileStorageRepository.class),
                new CapturingOssClientFactory());

        IllegalStateException exception = assertThrows(IllegalStateException.class, service::init);
        assertEquals("启用 OSS 的 ECS RAM Role 凭证模式时，必须配置 APP_FILE_OSS_ROLE_NAME", exception.getMessage());
    }

    @Test
    void accessKeyModeStillBuildsClientWithConfiguredAccessKeys() {
        FileStorageProperties properties = baseProperties();
        properties.setOssCredentialMode("access-key");
        properties.setOssAccessKeyId("test-access-key-id");
        properties.setOssAccessKeySecret("test-access-key-secret");
        CapturingOssClientFactory factory = new CapturingOssClientFactory();

        OssFileStorageService service = new OssFileStorageService(
                properties,
                mock(TenderFileStorageRepository.class),
                factory);

        service.init();

        assertEquals(1, factory.accessKeyBuildCount);
        assertEquals(0, factory.credentialsProviderBuildCount);
        assertEquals("test-access-key-id", factory.accessKeyId);
        assertEquals("test-access-key-secret", factory.accessKeySecret);
    }

    private FileStorageProperties baseProperties() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setOssBucket("wuhan-test-test-2222");
        properties.setOssEndpoint("oss-cn-hangzhou-internal.aliyuncs.com");
        properties.setOssKeyPrefix("zb/files");
        return properties;
    }

    private static class CapturingOssClientFactory implements OssClientFactory {

        private final OSS ossClient = mock(OSS.class);

        private String endpoint;

        private String accessKeyId;

        private String accessKeySecret;

        private CredentialsProvider credentialsProvider;

        private int accessKeyBuildCount;

        private int credentialsProviderBuildCount;

        @Override
        public OSS buildWithAccessKey(String endpoint, String accessKeyId, String accessKeySecret) {
            this.endpoint = endpoint;
            this.accessKeyId = accessKeyId;
            this.accessKeySecret = accessKeySecret;
            this.accessKeyBuildCount++;
            return ossClient;
        }

        @Override
        public OSS buildWithCredentialsProvider(String endpoint, CredentialsProvider credentialsProvider) {
            this.endpoint = endpoint;
            this.credentialsProvider = credentialsProvider;
            this.credentialsProviderBuildCount++;
            return ossClient;
        }
    }
}
