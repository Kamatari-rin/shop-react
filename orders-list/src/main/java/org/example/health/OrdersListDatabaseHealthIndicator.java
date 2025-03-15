package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("ordersListDatabase")
public class OrdersListDatabaseHealthIndicator extends DatabaseHealthIndicator {

    private final String orderItemsTableName;

    public OrdersListDatabaseHealthIndicator(
            JdbcTemplate jdbcTemplate,
            @Value("${health.orders-list.table.orders:orders}") String ordersTableName,
            @Value("${health.orders-list.table.order-items:order_items}") String orderItemsTableName,
            @Value("${health.orders-list.schema:public}") String schemaName) {
        super(jdbcTemplate, ordersTableName, schemaName, "ordersListDatabaseHealth");
        this.orderItemsTableName = orderItemsTableName;
    }

    @Override
    protected Map<String, ColumnInfo> getExpectedColumns() {
        return Map.of(
                "id", new ColumnInfo("id", "integer", false, null),
                "user_id", new ColumnInfo("user_id", "uuid", false, null),
                "order_date", new ColumnInfo("order_date", "timestamp", true, null),
                "status", new ColumnInfo("status", "varchar", false, 20),
                "total_amount", new ColumnInfo("total_amount", "numeric", true, null)
        );
    }

    private Map<String, ColumnInfo> getExpectedOrderItemsColumns() {
        return Map.of(
                "id", new ColumnInfo("id", "integer", false, null),
                "order_id", new ColumnInfo("order_id", "integer", false, null),
                "product_id", new ColumnInfo("product_id", "integer", false, null),
                "quantity", new ColumnInfo("quantity", "integer", false, null),
                "price", new ColumnInfo("price", "numeric", true, null),
                "image_url", new ColumnInfo("image_url", "varchar", true, 255)
        );
    }

    @Override
    public Health health() {
        Health ordersHealth = super.health();

        long startTime = System.currentTimeMillis();
        try {
            log.info("Checking existence of table {}.{}", schemaName, orderItemsTableName);
            Integer tableExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                    Integer.class, schemaName, orderItemsTableName
            );
            log.info("Table {}.{} exists: {}", schemaName, orderItemsTableName, tableExists);
            if (tableExists == null || tableExists == 0) {
                return Health.down()
                        .withDetail("orders", ordersHealth.getDetails())
                        .withDetail("order_items", "Table " + schemaName + "." + orderItemsTableName + " does not exist")
                        .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                        .build();
            }

            log.info("Querying columns for {}.{}", schemaName, orderItemsTableName);
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type, is_nullable, character_maximum_length " +
                            "FROM information_schema.columns " +
                            "WHERE table_schema = ? AND table_name = ?",
                    schemaName, orderItemsTableName
            );
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("Found {} columns for {}.{}", columns.size(), schemaName, orderItemsTableName);

            Map<String, ColumnInfo> actualColumns = columns.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row.get("column_name"),
                            row -> {
                                Object charMaxLength = row.get("character_maximum_length");
                                Integer maxLength = (charMaxLength instanceof Number) ? ((Number) charMaxLength).intValue() : null;
                                return new ColumnInfo(
                                        (String) row.get("column_name"),
                                        (String) row.get("data_type"),
                                        "YES".equalsIgnoreCase((String) row.get("is_nullable")),
                                        maxLength
                                );
                            }
                    ));

            Map<String, ColumnInfo> expectedColumns = getExpectedOrderItemsColumns();

            List<String> errors = expectedColumns.entrySet().stream()
                    .map(entry -> {
                        String key = entry.getKey();
                        ColumnInfo expected = entry.getValue();
                        ColumnInfo actual = actualColumns.get(key);
                        if (actual == null) return "Missing column: " + key;

                        List<String> errs = new ArrayList<>();
                        String actualType = normalizeDataType(actual.getDataType());
                        if (!expected.getDataType().equalsIgnoreCase(actualType))
                            errs.add(String.format("data type mismatch: actual '%s', expected '%s'", actual.getDataType(), expected.getDataType()));
                        if (expected.getCharMaxLength() != null && !expected.getCharMaxLength().equals(actual.getCharMaxLength()))
                            errs.add("max length is " + actual.getCharMaxLength() + ", expected " + expected.getCharMaxLength());
                        if (expected.isNullable() != actual.isNullable())
                            errs.add("nullable is " + actual.isNullable() + ", expected " + expected.isNullable());
                        return errs.isEmpty() ? null : key + " -> " + String.join("; ", errs);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Health.Builder orderItemsBuilder = errors.isEmpty() ? Health.up() : Health.down();
            try {
                log.info("Querying row count for {}.{}", schemaName, orderItemsTableName);
                Integer rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + schemaName + "." + orderItemsTableName, Integer.class);
                log.info("Row count for {}.{}: {}", schemaName, orderItemsTableName, rowCount);
                orderItemsBuilder.withDetail("rowCount", rowCount != null ? rowCount : 0);
            } catch (Exception e) {
                log.warn("Failed to get row count for {}.{}", schemaName, orderItemsTableName, e);
                orderItemsBuilder.withDetail("rowCount", "unknown due to " + e.getMessage());
            }

            Health orderItemsHealth = orderItemsBuilder
                    .withDetail("table", orderItemsTableName + " exists")
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("columnCount", columns.size())
                    .withDetail("errors", errors)
                    .build();

            if (ordersHealth.getStatus().equals(Status.UP) && orderItemsHealth.getStatus().equals(Status.UP)) {
                return Health.up()
                        .withDetail("orders", ordersHealth.getDetails())
                        .withDetail("order_items", orderItemsHealth.getDetails())
                        .build();
            } else {
                Health.Builder combined = Health.down();
                if (!ordersHealth.getStatus().equals(Status.UP)) combined.withDetail("orders", ordersHealth.getDetails());
                if (!orderItemsHealth.getStatus().equals(Status.UP)) combined.withDetail("order_items", orderItemsHealth.getDetails());
                return combined.build();
            }
        } catch (Exception e) {
            log.error("Error checking order_items table health for {}.{}", schemaName, orderItemsTableName, e);
            return Health.down(e)
                    .withDetail("orders", ordersHealth.getDetails())
                    .withDetail("order_items", "Failed to check: " + e.getMessage())
                    .build();
        }
    }
}