package org.example.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("userServiceDatabase")
@Slf4j
public class UserServiceDatabaseHealthIndicator extends DatabaseHealthIndicator {

    public UserServiceDatabaseHealthIndicator(
            R2dbcEntityTemplate r2dbcEntityTemplate,
            @Value("${health.user-service.schema:public}") String schemaName) {
        super(r2dbcEntityTemplate, schemaName, "userServiceDatabaseHealth");
    }

    @Override
    protected Map<String, Map<String, ColumnInfo>> getTablesAndColumns() {
        return Map.of(
                "users", Map.of(
                        "id", new ColumnInfo("id", "uuid", false, null),
                        "username", new ColumnInfo("username", "varchar", false, 50),
                        "email", new ColumnInfo("email", "varchar", false, 100),
                        "password", new ColumnInfo("password", "varchar", false, 255),
                        "role", new ColumnInfo("role", "varchar", true, 20),
                        "created_at", new ColumnInfo("created_at", "timestamp", false, null),
                        "updated_at", new ColumnInfo("updated_at", "timestamp", false, null)
                )
        );
    }
}