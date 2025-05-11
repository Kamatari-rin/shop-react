package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("purchaseDatabase")
public class PurchaseDatabaseHealthIndicator extends DatabaseHealthIndicator {

    public PurchaseDatabaseHealthIndicator(
            R2dbcEntityTemplate r2dbcEntityTemplate,
            @Value("${health.purchase-service.schema:public}") String schemaName) {
        super(r2dbcEntityTemplate, schemaName, "purchaseDatabaseHealth");
    }

    @Override
    protected Map<String, Map<String, ColumnInfo>> getTablesAndColumns() {
        return Map.of(
                "purchases", Map.of(
                        "id", new ColumnInfo("id", "integer", false, null),
                        "order_id", new ColumnInfo("order_id", "integer", false, null),
                        "user_id", new ColumnInfo("user_id", "uuid", false, null),
                        "payment_status", new ColumnInfo("payment_status", "varchar", false, 50),
                        "transaction_date", new ColumnInfo("transaction_date", "timestamp", false, null),
                        "details", new ColumnInfo("details", "text", true, null)
                )
        );
    }
}