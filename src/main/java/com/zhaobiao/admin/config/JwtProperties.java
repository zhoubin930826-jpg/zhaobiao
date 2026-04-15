package com.zhaobiao.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @NotBlank(message = "必须配置 app.jwt.secret 或 APP_JWT_SECRET")
    @Size(min = 32, message = "JWT 密钥长度至少需要 32 个字符")
    private String secret;

    @Min(value = 1, message = "JWT 过期时间必须大于 0")
    private long expireSeconds;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }
}
