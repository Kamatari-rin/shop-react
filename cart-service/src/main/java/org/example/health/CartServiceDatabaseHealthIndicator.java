package org.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Component("cartServiceDatabase")
public class CartServiceDatabaseHealthIndicator extends DatabaseHealthIndicator {

    private final String cartItemsTableName;

    public CartServiceDatabaseHealthIndicator(
            R2dbcEntityTemplate r2dbcEntityTemplate,
            @Value("${health.cart-service.table.carts:carts}") String cartsTableName,
            @Value("${health.cart-service.table.cart-items:cart_items}") String cartItemsTableName,
            @Value("${health.cart-service.schema:public}") String schemaName) {
        super(r2dbcEntityTemplate, cartsTableName, schemaName, "cartServiceDatabaseHealth");
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
    public Mono<Health> health() {
        return super.health()
                .flatMap(cartsHealth -> {
                    long startTime = System.currentTimeMillis();
                    log.info("Checking existence of table {}.{}", schemaName, cartItemsTableName);
                    return r2dbcEntityTemplate.getDatabaseClient()
                            .sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = :schema AND table_name = :table")
                            .bind("schema", schemaName)
                            .bind("table", cartItemsTableName)
                            .map(row -> row.get(0, Integer.class))
                            .one()
                            .doOnNext(tableExists -> log.info("Table {}.{} exists: {}", schemaName, cartItemsTableName, tableExists))
                            .flatMap(tableExists -> {
                                if (tableExists == null || tableExists == 0) {
                                    return Mono.just(Health.down()
                                            .withDetail("carts", cartsHealth.getDetails())
                                            .withDetail("cart_items", "Table " + schemaName + "." + cartItemsTableName + " does not exist")
                                            .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                                            .build());
                                }

                                log.info("Querying columns for {}.{}", schemaName, cartItemsTableName);
                                return r2dbcEntityTemplate.getDatabaseClient()
                                        .sql("SELECT column_name, data_type, is_nullable, character_maximum_length " +
                                                "FROM information_schema.columns " +
                                                "WHERE table_schema = :schema AND table_name = :table")
                                        .bind("schema", schemaName)
                                        .bind("table", cartItemsTableName)
                                        .map(row -> new ColumnInfo(
                                                row.get("column_name", String.class),
                                                row.get("data_type", String.class),
                                                "YES".equalsIgnoreCase(row.get("is_nullable", String.class)),
                                                row.get("character_maximum_length", Integer.class)
                                        ))
                                        .all()
                                        .collectList()
                                        .map(columns -> {
                                            long responseTime = System.currentTimeMillis() - startTime;
                                            log.info("Found {} columns for {}.{}", columns.size(), schemaName, cartItemsTableName);

                                            Map<String, ColumnInfo> actualColumns = columns.stream()
                                                    .collect(Collectors.toMap(ColumnInfo::getColumnName, col -> col));

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
                                            return r2dbcEntityTemplate.getDatabaseClient()
                                                    .sql("SELECT COUNT(*) FROM " + schemaName + "." + cartItemsTableName)
                                                    .map(row -> row.get(0, Integer.class))
                                                    .one()
                                                    .doOnNext(rowCount -> log.info("Row count for {}.{}: {}", schemaName, cartItemsTableName, rowCount))
                                                    .defaultIfEmpty(0)
                                                    .map(rowCount -> {
                                                        cartItemsBuilder
                                                                .withDetail("rowCount", rowCount)
                                                                .withDetail("table", cartItemsTableName + " exists")
                                                                .withDetail("responseTime", responseTime + "ms")
                                                                .withDetail("columnCount", columns.size())
                                                                .withDetail("errors", errors);
                                                        Health cartItemsHealth = cartItemsBuilder.build();

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
                                                    })
                                                    .onErrorResume(e -> {
                                                        log.warn("Failed to get row count for {}.{}", schemaName, cartItemsTableName, e);
                                                        cartItemsBuilder
                                                                .withDetail("rowCount", "unknown due to " + e.getMessage())
                                                                .withDetail("table", cartItemsTableName + " exists")
                                                                .withDetail("responseTime", responseTime + "ms")
                                                                .withDetail("columnCount", columns.size())
                                                                .withDetail("errors", errors);
                                                        Health cartItemsHealth = cartItemsBuilder.build();

                                                        Health.Builder combined = Health.down();
                                                        combined.withDetail("carts", cartsHealth.getDetails());
                                                        combined.withDetail("cart_items", cartItemsHealth.getDetails());
                                                        return Mono.just(combined.build());
                                                    });
                                        })
                                        .flatMap(mono -> mono);
                            })
                            .onErrorResume(e -> {
                                log.error("Error checking cart_items table health for {}.{}", schemaName, cartItemsTableName, e);
                                return Mono.just(Health.down(e)
                                        .withDetail("carts", cartsHealth.getDetails())
                                        .withDetail("cart_items", "Failed to check: " + e.getMessage())
                                        .build());
                            });
                });
    }
}