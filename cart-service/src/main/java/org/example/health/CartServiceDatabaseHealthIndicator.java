package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("cartServiceDatabase")
public class CartServiceDatabaseHealthIndicator extends DatabaseHealthIndicator {

    private final String cartItemsTableName;

    public CartServiceDatabaseHealthIndicator(
            JdbcTemplate jdbcTemplate,
            @Value("${health.cart-service.table.carts:carts}") String cartsTableName,
            @Value("${health.cart-service.table.cart-items:cart_items}") String cartItemsTableName,
            @Value("${health.cart-service.schema:public}") String schemaName) {
        super(jdbcTemplate, cartsTableName, schemaName, "cartServiceDatabaseHealth");
        this.cartItemsTableName = cartItemsTableName;
    }

    @Override
    protected Map<String, ColumnInfo> getExpectedColumns() {
        return Map.of(
                "id", new ColumnInfo("id", "integer", false, null),
                "user_id", new ColumnInfo("user_id", "uuid", false, null),
                "created_at", new ColumnInfo("created_at", "timestamp", true, null)
        );
    }

    private Map<String, ColumnInfo> getExpectedCartItemsColumns() {
        return Map.of(
                "id", new ColumnInfo("id", "integer", false, null),
                "cart_id", new ColumnInfo("cart_id", "integer", false, null),
                "product_id", new ColumnInfo("product_id", "integer", false, null),
                "quantity", new ColumnInfo("quantity", "integer", false, null),
                "price_at_time", new ColumnInfo("price_at_time", "numeric", true, null)
        );
    }

    @Override
    public Health health() {
        Health cartsHealth = super.health();

        long startTime = System.currentTimeMillis();
        try {
            log.info("Checking existence of table {}.{}", schemaName, cartItemsTableName);
            Integer tableExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                    Integer.class, schemaName, cartItemsTableName
            );
            log.info("Table {}.{} exists: {}", schemaName, cartItemsTableName, tableExists);
            if (tableExists == null || tableExists == 0) {
                return Health.down()
                        .withDetail("carts", cartsHealth.getDetails())
                        .withDetail("cart_items", "Table " + schemaName + "." + cartItemsTableName + " does not exist")
                        .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                        .build();
            }

            log.info("Querying columns for {}.{}", schemaName, cartItemsTableName);
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type, is_nullable, character_maximum_length " +
                            "FROM information_schema.columns " +
                            "WHERE table_schema = ? AND table_name = ?",
                    schemaName, cartItemsTableName
            );
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("Found {} columns for {}.{}", columns.size(), schemaName, cartItemsTableName);

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

            Map<String, ColumnInfo> expectedColumns = getExpectedCartItemsColumns();

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

            Health.Builder cartItemsBuilder = errors.isEmpty() ? Health.up() : Health.down();
            try {
                log.info("Querying row count for {}.{}", schemaName, cartItemsTableName);
                Integer rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + schemaName + "." + cartItemsTableName, Integer.class);
                log.info("Row count for {}.{}: {}", schemaName, cartItemsTableName, rowCount);
                cartItemsBuilder.withDetail("rowCount", rowCount != null ? rowCount : 0);
            } catch (Exception e) {
                log.warn("Failed to get row count for {}.{}", schemaName, cartItemsTableName, e);
                cartItemsBuilder.withDetail("rowCount", "unknown due to " + e.getMessage());
            }

            Health cartItemsHealth = cartItemsBuilder
                    .withDetail("table", cartItemsTableName + " exists")
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("columnCount", columns.size())
                    .withDetail("errors", errors)
                    .build();

            if (cartsHealth.getStatus().equals(Status.UP) && cartItemsHealth.getStatus().equals(Status.UP)) {
                return Health.up()
                        .withDetail("carts", cartsHealth.getDetails())
                        .withDetail("cart_items", cartItemsHealth.getDetails())
                        .build();
            } else {
                Health.Builder combined = Health.down();
                if (!cartsHealth.getStatus().equals(Status.UP)) combined.withDetail("carts", cartsHealth.getDetails());
                if (!cartItemsHealth.getStatus().equals(Status.UP)) combined.withDetail("cart_items", cartItemsHealth.getDetails());
                return combined.build();
            }
        } catch (Exception e) {
            log.error("Error checking cart_items table health for {}.{}", schemaName, cartItemsTableName, e);
            return Health.down(e)
                    .withDetail("carts", cartsHealth.getDetails())
                    .withDetail("cart_items", "Failed to check: " + e.getMessage())
                    .build();
        }
    }
}