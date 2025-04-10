package org.example.health;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class DatabaseHealthIndicator implements ReactiveHealthIndicator {
    protected final R2dbcEntityTemplate r2dbcEntityTemplate;
    protected final String tableName;
    protected final String schemaName;
    protected final String cacheName;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Data
    @AllArgsConstructor
    protected static class ColumnInfo {
        private String columnName;
        private String dataType;
        private boolean isNullable;
        private Integer charMaxLength;
    }

    protected String normalizeDataType(String dbType) {
        return switch (dbType.toLowerCase()) {
            case "character varying" -> "varchar";
            case "timestamp without time zone" -> "timestamp";
            default -> dbType;
        };
    }

    protected abstract Map<String, ColumnInfo> getExpectedColumns();

    @Cacheable(value = "healthCache", key = "#root.target.cacheName")
    @Override
    public Mono<Health> health() {
        long startTime = System.currentTimeMillis();
        log.info("Checking existence of table {}.{}", schemaName, tableName);
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = :schema AND table_name = :table")
                .bind("schema", schemaName)
                .bind("table", tableName)
                .map(row -> row.get(0, Integer.class))
                .one()
                .doOnNext(tableExists -> log.info("Table {}.{} exists: {}", schemaName, tableName, tableExists))
                .flatMap(tableExists -> {
                    if (tableExists == null || tableExists == 0) {
                        return Mono.just(Health.down()
                                .withDetail("error", "Table " + schemaName + "." + tableName + " does not exist")
                                .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                                .build());
                    }

                    log.info("Querying columns for {}.{}", schemaName, tableName);
                    return r2dbcEntityTemplate.getDatabaseClient()
                            .sql("SELECT column_name, data_type, is_nullable, character_maximum_length " +
                                    "FROM information_schema.columns " +
                                    "WHERE table_schema = :schema AND table_name = :table")
                            .bind("schema", schemaName)
                            .bind("table", tableName)
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
                                log.info("Found {} columns for {}.{}", columns.size(), schemaName, tableName);

                                Map<String, ColumnInfo> actualColumns = columns.stream()
                                        .collect(Collectors.toMap(ColumnInfo::getColumnName, col -> col));

                                Map<String, ColumnInfo> expectedColumns = getExpectedColumns();

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

                                Health.Builder builder = errors.isEmpty() ? Health.up() : Health.down();
                                return r2dbcEntityTemplate.getDatabaseClient()
                                        .sql("SELECT COUNT(*) FROM " + schemaName + "." + tableName)
                                        .map(row -> row.get(0, Integer.class))
                                        .one()
                                        .doOnNext(rowCount -> log.info("Row count for {}.{}: {}", schemaName, tableName, rowCount))
                                        .defaultIfEmpty(0)
                                        .map(rowCount -> builder
                                                .withDetail("rowCount", rowCount)
                                                .withDetail("table", tableName + " exists")
                                                .withDetail("responseTime", responseTime + "ms")
                                                .withDetail("columnCount", columns.size())
                                                .withDetail("errors", errors)
                                                .build())
                                        .onErrorResume(e -> {
                                            log.warn("Failed to get row count for {}.{}", schemaName, tableName, e);
                                            return Mono.just(builder
                                                    .withDetail("rowCount", "unknown due to " + e.getMessage())
                                                    .withDetail("table", tableName + " exists")
                                                    .withDetail("responseTime", responseTime + "ms")
                                                    .withDetail("columnCount", columns.size())
                                                    .withDetail("errors", errors)
                                                    .build());
                                        });
                            })
                            .flatMap(mono -> mono);
                })
                .onErrorResume(e -> {
                    log.error("Error checking database health for table {}.{}", schemaName, tableName, e);
                    return Mono.just(Health.down(e)
                            .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                            .build());
                });
    }
}