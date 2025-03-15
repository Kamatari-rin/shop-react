package org.example.health;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class DatabaseHealthIndicator implements org.springframework.boot.actuate.health.HealthIndicator {

    protected final JdbcTemplate jdbcTemplate;
    protected final String tableName;
    protected final String schemaName;
    protected final String cacheName;

    protected final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

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
    public Health health() {
        long startTime = System.currentTimeMillis();
        try {
            log.info("Checking existence of table {}.{}", schemaName, tableName);
            Integer tableExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                    Integer.class, schemaName, tableName
            );
            log.info("Table {}.{} exists: {}", schemaName, tableName, tableExists);
            if (tableExists == null || tableExists == 0) {
                return Health.down()
                        .withDetail("error", "Table " + schemaName + "." + tableName + " does not exist")
                        .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                        .build();
            }

            log.info("Querying columns for {}.{}", schemaName, tableName);
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type, is_nullable, character_maximum_length " +
                            "FROM information_schema.columns " +
                            "WHERE table_schema = ? AND table_name = ?",
                    schemaName, tableName
            );
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("Found {} columns for {}.{}", columns.size(), schemaName, tableName);

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
            try {
                log.info("Querying row count for {}.{}", schemaName, tableName);
                Integer rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + schemaName + "." + tableName, Integer.class);
                log.info("Row count for {}.{}: {}", schemaName, tableName, rowCount);
                builder.withDetail("rowCount", rowCount != null ? rowCount : 0);
            } catch (Exception e) {
                log.warn("Failed to get row count for {}.{}", schemaName, tableName, e);
                builder.withDetail("rowCount", "unknown due to " + e.getMessage());
            }

            return builder
                    .withDetail("table", tableName + " exists")
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("columnCount", columns.size())
                    .withDetail("errors", errors)
                    .build();
        } catch (Exception e) {
            log.error("Error checking database health for table {}.{}", schemaName, tableName, e);
            return Health.down(e)
                    .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                    .build();
        }
    }
}