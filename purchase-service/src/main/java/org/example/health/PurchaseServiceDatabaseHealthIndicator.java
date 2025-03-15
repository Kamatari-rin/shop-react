package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("purchaseServiceDatabase")
public class PurchaseServiceDatabaseHealthIndicator extends DatabaseHealthIndicator {

    public PurchaseServiceDatabaseHealthIndicator(
            JdbcTemplate jdbcTemplate,
            @Value("${health.purchase-service.table.purchases:purchases}") String purchasesTableName,
            @Value("${health.purchase-service.schema:public}") String schemaName) {
        super(jdbcTemplate, purchasesTableName, schemaName, "purchaseServiceDatabaseHealth");
    }

    @Override
    protected Map<String, ColumnInfo> getExpectedColumns() {
        return Map.of(
                "id", new ColumnInfo("id", "integer", false, null),
                "order_id", new ColumnInfo("order_id", "integer", true, null),
                "user_id", new ColumnInfo("user_id", "uuid", true, null),
                "payment_status", new ColumnInfo("payment_status", "varchar", false, 20),
                "transaction_date", new ColumnInfo("transaction_date", "timestamp", true, null),
                "details", new ColumnInfo("details", "text", true, null)
        );
    }
}