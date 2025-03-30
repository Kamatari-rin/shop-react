package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("userServiceDatabase")
public class UserServiceDatabaseHealthIndicator extends DatabaseHealthIndicator {

    public UserServiceDatabaseHealthIndicator(
            JdbcTemplate jdbcTemplate,
            @Value("${health.user-service.table:users}") String tableName,
            @Value("${health.user-service.schema:public}") String schemaName) {
        super(jdbcTemplate, tableName, schemaName, "userServiceDatabaseHealth");
    }

    @Override
    protected Map<String, ColumnInfo> getExpectedColumns() {
        return Map.of(
                "id", new ColumnInfo("id", "uuid", false, null),
                "username", new ColumnInfo("username", "varchar", false, 50),
                "email", new ColumnInfo("email", "varchar", false, 100),
                "password", new ColumnInfo("password", "varchar", false, 255),
                "role", new ColumnInfo("role", "varchar", true, 20),
                "created_at", new ColumnInfo("created_at", "timestamp", false, null),
                "updated_at", new ColumnInfo("updated_at", "timestamp", false, null)
        );
    }
}