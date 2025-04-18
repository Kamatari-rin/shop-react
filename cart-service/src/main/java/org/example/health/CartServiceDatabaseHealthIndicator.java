package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("cartServiceDatabase")
public class CartServiceDatabaseHealthIndicator extends DatabaseHealthIndicator {

    public CartServiceDatabaseHealthIndicator(
            R2dbcEntityTemplate r2dbcEntityTemplate,
            @Value("${health.cart-service.schema:public}") String schemaName) {
        super(r2dbcEntityTemplate, schemaName, "cartServiceDatabaseHealth");
    }

    @Override
    protected Map<String, Map<String, ColumnInfo>> getTablesAndColumns() {
        return Map.of(
                "carts", Map.of(
                        "id", new ColumnInfo("id", "integer", false, null),
                        "user_id", new ColumnInfo("user_id", "uuid", false, null),
                        "created_at", new ColumnInfo("created_at", "timestamp", true, null)
                ),
                "cart_items", Map.of(
                        "id", new ColumnInfo("id", "integer", false, null),
                        "cart_id", new ColumnInfo("cart_id", "integer", false, null),
                        "product_id", new ColumnInfo("product_id", "integer", false, null),
                        "quantity", new ColumnInfo("quantity", "integer", false, null),
                        "price_at_time", new ColumnInfo("price_at_time", "numeric", true, null)
                )
        );
    }
}