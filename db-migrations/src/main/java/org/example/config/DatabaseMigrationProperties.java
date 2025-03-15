package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "db.migrations")
@Data
public class DatabaseMigrationProperties {
    private MigrationProperties orders;
    private MigrationProperties product;
    private MigrationProperties user;
    private MigrationProperties cart;
    private MigrationProperties purchase;

    @Data
    public static class MigrationProperties {
        private String url;
        private String username;
        private String password;
        private String changelog;
    }
}
