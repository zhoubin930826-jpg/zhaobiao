package com.zhaobiao.admin.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Component
@Profile("prod")
public class ProductionDatabaseSecurityValidator {

    private final DataSourceProperties dataSourceProperties;

    public ProductionDatabaseSecurityValidator(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @PostConstruct
    public void validate() {
        validateConfiguration(dataSourceProperties.getUsername(), dataSourceProperties.getPassword());
    }

    static void validateConfiguration(String username, String password) {
        String normalizedUsername = normalize(username);
        String normalizedPassword = normalize(password);

        if (!StringUtils.hasText(normalizedUsername)) {
            throw new IllegalStateException("prod 环境必须显式配置 MYSQL_USER 或 spring.datasource.username");
        }
        if (!StringUtils.hasText(normalizedPassword)) {
            throw new IllegalStateException("prod 环境必须显式配置 MYSQL_PASSWORD 或 spring.datasource.password");
        }
        if ("root".equalsIgnoreCase(normalizedUsername)) {
            throw new IllegalStateException("prod 环境禁止使用 root 数据库账号，请改用独立业务账号");
        }
        if ("root".equals(normalizedPassword)) {
            throw new IllegalStateException("prod 环境禁止使用默认数据库密码 root");
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}
