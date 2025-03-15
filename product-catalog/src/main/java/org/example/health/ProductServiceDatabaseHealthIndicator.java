package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("productServiceDatabase")
public class ProductServiceDatabaseHealthIndicator extends DatabaseHealthIndicator {

    public ProductServiceDatabaseHealthIndicator(
            JdbcTemplate jdbcTemplate,
            @Value("${health.product-service.table:products}") String tableName,
            @Value("${health.product-service.schema:public}") String schemaName) {
        super(jdbcTemplate, tableName, schemaName, "productServiceDatabaseHealth");
    }

    @Override
    protected Map<String, ColumnInfo> getExpectedColumns() {
        return Map.of(
                "id", new ColumnInfo("id", "integer", false, null),
                "name", new ColumnInfo("name", "varchar", false, 255),
                "description", new ColumnInfo("description", "text", true, null),
                "price", new ColumnInfo("price", "numeric", false, null),
                "image_url", new ColumnInfo("image_url", "varchar", true, 255),
                "category_id", new ColumnInfo("category_id", "integer", true, null),
                "created_at", new ColumnInfo("created_at", "timestamp", true, null),
                "updated_at", new ColumnInfo("updated_at", "timestamp", true, null)
        );
    }
}