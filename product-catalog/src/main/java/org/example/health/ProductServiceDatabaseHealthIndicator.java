package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("productCatalogDatabase")
public class ProductServiceDatabaseHealthIndicator extends DatabaseHealthIndicator {

    public ProductServiceDatabaseHealthIndicator(
            R2dbcEntityTemplate r2dbcEntityTemplate,
            @Value("${health.product-service.schema:public}") String schemaName) {
        super(r2dbcEntityTemplate, schemaName, "productCatalogDatabaseHealth");
    }

    @Override
    protected Map<String, Map<String, ColumnInfo>> getTablesAndColumns() {
        return Map.of(
                "products", Map.of(
                        "id", new ColumnInfo("id", "integer", false, null),
                        "name", new ColumnInfo("name", "varchar", false, 255),
                        "description", new ColumnInfo("description", "text", true, null),
                        "price", new ColumnInfo("price", "numeric", false, null),
                        "image_url", new ColumnInfo("image_url", "varchar", true, 255),
                        "category_id", new ColumnInfo("category_id", "integer", true, null),
                        "created_at", new ColumnInfo("created_at", "timestamp", true, null),
                        "updated_at", new ColumnInfo("updated_at", "timestamp", true, null)
                ),
                "categories", Map.of(
                        "id", new ColumnInfo("id", "integer", false, null),
                        "name", new ColumnInfo("name", "varchar", false, 100)
                )
        );
    }
}