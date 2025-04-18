package org.example.health;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class DatabaseHealthIndicator implements ReactiveHealthIndicator {
    protected final R2dbcEntityTemplate r2dbcEntityTemplate;
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

    protected abstract Map<String, Map<String, ColumnInfo>> getTablesAndColumns();

    @Cacheable(value = "healthCache", key = "#root.target.cacheName")
    @Override
    public Mono<Health> health() {
        long startTime = System.currentTimeMillis();
        log.debug("Starting health check for database in schema {}", schemaName);

        return checkDatabaseConnectivity()
                .flatMap(connectivityHealth -> {
                    if (!connectivityHealth.getStatus().equals(Status.UP)) {
                        log.error("Database connectivity check failed");
                        return Mono.just(Health.down()
                                .withDetail("database", connectivityHealth.getDetails().get("database"))
                                .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                                .build());
                    }

                    Map<String, Map<String, ColumnInfo>> tablesAndColumns = getTablesAndColumns();
                    List<Mono<Health>> tableChecks = tablesAndColumns.keySet().stream()
                            .map(table -> checkTableHealth(table, tablesAndColumns.get(table), startTime))
                            .toList();

                    return Mono.zip(tableChecks, results -> {
                        Map<String, Object> tableDetails = new LinkedHashMap<>();
                        boolean allUp = true;

                        for (int i = 0; i < results.length; i++) {
                            Health tableHealth = (Health) results[i];
                            String tableName = tablesAndColumns.keySet().toArray(new String[0])[i];
                            tableDetails.put(tableName, tableHealth.getDetails());
                            if (!tableHealth.getStatus().equals(Status.UP)) {
                                allUp = false;
                            }
                        }

                        Health.Builder builder = allUp ? Health.up() : Health.down();
                        return builder
                                .withDetail("database", connectivityHealth.getDetails().get("database"))
                                .withDetail("tables", tableDetails)
                                .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                                .build();
                    });
                })
                .onErrorResume(e -> {
                    log.error("Error during database health check", e);
                    return Mono.just(Health.down(e)
                            .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                            .build());
                });
    }

    private Mono<Health> checkDatabaseConnectivity() {
        log.debug("Checking database connectivity");
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("SELECT 1")
                .fetch()
                .one()
                .map(result -> {
                    log.debug("Database connectivity check successful");
                    return Health.up()
                            .withDetail("database", "reachable")
                            .build();
                })
                .onErrorResume(e -> {
                    log.error("Database connectivity check failed", e);
                    return Mono.just(Health.down(e)
                            .withDetail("database", "unreachable")
                            .build());
                });
    }

    private Mono<Health> checkTableHealth(String tableName, Map<String, ColumnInfo> expectedColumns, long startTime) {
        log.debug("Checking health of table {}.{}", schemaName, tableName);
        return checkTableExistence(tableName)
                .flatMap(existenceHealth -> {
                    if (!existenceHealth.getStatus().equals(Status.UP)) {
                        log.warn("Table {}.{} does not exist", schemaName, tableName);
                        Health.Builder builder = existenceHealth.getStatus().equals(Status.UP) ? Health.up() : Health.down();
                        existenceHealth.getDetails().forEach(builder::withDetail);
                        return Mono.just(builder
                                .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                                .build());
                    }

                    return checkTableColumns(tableName, expectedColumns)
                            .flatMap(columnsHealth -> {
                                if (!columnsHealth.getStatus().equals(Status.UP)) {
                                    Health.Builder builder = columnsHealth.getStatus().equals(Status.UP) ? Health.up() : Health.down();
                                    columnsHealth.getDetails().forEach(builder::withDetail);
                                    return Mono.just(builder
                                            .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                                            .build());
                                }

                                return checkRowCount(tableName)
                                        .map(rowCountHealth -> {
                                            Health.Builder builder = rowCountHealth.getStatus().equals(Status.UP) ? Health.up() : Health.down();
                                            rowCountHealth.getDetails().forEach(builder::withDetail);
                                            return builder
                                                    .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                                                    .build();
                                        });
                            });
                });
    }

    private Mono<Health> checkTableExistence(String tableName) {
        log.debug("Checking existence of table {}.{}", schemaName, tableName);
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = :schema AND table_name = :table")
                .bind("schema", schemaName)
                .bind("table", tableName)
                .map(row -> row.get(0, Integer.class))
                .one()
                .map(count -> {
                    boolean exists = count != null && count > 0;
                    log.debug("Table {}.{} exists: {}", schemaName, tableName, exists);
                    Health.Builder builder = exists ? Health.up() : Health.down();
                    return builder
                            .withDetail("table", tableName + (exists ? " exists" : " does not exist"))
                            .build();
                })
                .onErrorResume(e -> {
                    log.error("Error checking existence of table {}.{}", schemaName, tableName, e);
                    return Mono.just(Health.down(e)
                            .withDetail("table", "failed to check")
                            .build());
                });
    }

    private Mono<Health> checkTableColumns(String tableName, Map<String, ColumnInfo> expectedColumns) {
        log.debug("Checking columns for table {}.{}", schemaName, tableName);
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
                    log.debug("Found {} columns for {}.{}", columns.size(), schemaName, tableName);
                    Map<String, ColumnInfo> actualColumns = columns.stream()
                            .collect(Collectors.toMap(ColumnInfo::getColumnName, col -> col));

                    List<String> errors = expectedColumns.entrySet().stream()
                            .map(entry -> {
                                String key = entry.getKey();
                                ColumnInfo expected = entry.getValue();
                                ColumnInfo actual = actualColumns.get(key);
                                if (actual == null) {
                                    return "Missing column: " + key;
                                }

                                List<String> errs = new ArrayList<>();
                                String actualType = normalizeDataType(actual.getDataType());
                                if (!expected.getDataType().equalsIgnoreCase(actualType)) {
                                    errs.add(String.format("data type mismatch: actual '%s', expected '%s'",
                                            actual.getDataType(), expected.getDataType()));
                                }
                                if (expected.getCharMaxLength() != null && !expected.getCharMaxLength().equals(actual.getCharMaxLength())) {
                                    errs.add("max length is " + actual.getCharMaxLength() + ", expected " + expected.getCharMaxLength());
                                }
                                if (expected.isNullable() != actual.isNullable()) {
                                    errs.add("nullable is " + actual.isNullable() + ", expected " + expected.isNullable());
                                }
                                return errs.isEmpty() ? null : key + " -> " + String.join("; ", errs);
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    Health.Builder builder = errors.isEmpty() ? Health.up() : Health.down();
                    return builder
                            .withDetail("columnCount", columns.size())
                            .withDetail("errors", errors)
                            .build();
                })
                .onErrorResume(e -> {
                    log.error("Error checking columns for table {}.{}", schemaName, tableName, e);
                    return Mono.just(Health.down(e)
                            .withDetail("columns", "failed to check")
                            .build());
                });
    }

    private Mono<Health> checkRowCount(String tableName) {
        log.debug("Checking row count for table {}.{}", schemaName, tableName);
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("SELECT COUNT(*) FROM " + schemaName + "." + tableName)
                .map(row -> row.get(0, Long.class))
                .one()
                .doOnNext(rowCount -> log.debug("Row count for {}.{}: {}", schemaName, tableName, rowCount))
                .map(rowCount -> Health.up()
                        .withDetail("rowCount", rowCount)
                        .build())
                .defaultIfEmpty(Health.up()
                        .withDetail("rowCount", 0)
                        .build())
                .onErrorResume(e -> {
                    log.warn("Failed to get row count for {}.{}", schemaName, tableName, e);
                    return Mono.just(Health.up()
                            .withDetail("rowCount", "unknown due to " + e.getMessage())
                            .build());
                });
    }
}