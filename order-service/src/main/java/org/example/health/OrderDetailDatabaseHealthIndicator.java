package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("orderDetailDatabase")
public class OrderDetailDatabaseHealthIndicator extends DatabaseHealthIndicator {

    public OrderDetailDatabaseHealthIndicator(
            R2dbcEntityTemplate r2dbcEntityTemplate,
            @Value("${health.order-service.schema:public}") String schemaName) {
        super(r2dbcEntityTemplate, schemaName, "orderDetailDatabaseHealth");
    }

    @Override
    protected Map<String, Map<String, ColumnInfo>> getTablesAndColumns() {
        return Map.of(
                "orders", Map.of(
                        "id", new ColumnInfo("id", "integer", false, null),
                        "user_id", new ColumnInfo("user_id", "uuid", false, null),
                        "order_date", new ColumnInfo("order_date", "timestamp", true, null),
                        "status", new ColumnInfo("status", "varchar", false, 20),
                        "total_amount", new ColumnInfo("total_amount", "numeric", true, null)
                ),
                "order_items", Map.of(
                        "id", new ColumnInfo("id", "integer", false, null),
                        "order_id", new ColumnInfo("order_id", "integer", false, null),
                        "product_id", new ColumnInfo("product_id", "integer", false, null),
                        "quantity", new ColumnInfo("quantity", "integer", false, null),
                        "price", new ColumnInfo("price", "numeric", true, null),
                        "image_url", new ColumnInfo("image_url", "varchar", true, 255)
                )
        );
    }
}