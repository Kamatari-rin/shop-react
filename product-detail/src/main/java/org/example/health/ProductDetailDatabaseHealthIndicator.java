package org.example.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("productDetailDatabase")
@Slf4j
@RequiredArgsConstructor
public class ProductDetailDatabaseHealthIndicator implements ReactiveHealthIndicator {
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<Health> health() {
        long startTime = System.currentTimeMillis();
        return checkDatabaseConnectivity()
                .flatMap(health -> checkTableExistence("products"))
                .flatMap(health -> checkTableExistence("categories"))
                .flatMap(health -> checkRowCount("products"))
                .flatMap(health -> checkRowCount("categories"))
                .map(health -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.info("Database health check completed for product-detail, responseTime: {}ms", responseTime);
                    return health
                            .withDetail("responseTime", responseTime + "ms")
                            .build();
                })
                .onErrorResume(e -> {
                    log.error("Error checking database health for product-detail", e);
                    long responseTime = System.currentTimeMillis() - startTime;
                    return Mono.just(Health.down(e)
                            .withDetail("responseTime", responseTime + "ms")
                            .build());
                });
    }

    private Mono<Health.Builder> checkDatabaseConnectivity() {
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("SELECT 1")
                .fetch()
                .one()
                .map(result -> {
                    log.info("Database connectivity check successful for productdb");
                    return Health.up().withDetail("database", "productdb reachable");
                });
    }

    private Mono<Health.Builder> checkTableExistence(String tableName) {
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = :tableName)")
                .bind("tableName", tableName)
                .fetch()
                .one()
                .map(row -> {
                    Boolean exists = (Boolean) row.get("exists");
                    log.info("Table {} exists: {}", tableName, exists);
                    return exists ? Health.up().withDetail("table." + tableName, "exists") : Health.down().withDetail("table." + tableName, "does not exist");
                });
    }

    private Mono<Health.Builder> checkRowCount(String tableName) {
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("SELECT COUNT(*) FROM " + tableName)
                .fetch()
                .one()
                .map(row -> {
                    Long count = (Long) row.get("count");
                    log.info("Row count for {}: {}", tableName, count);
                    return Health.up().withDetail("rowCount." + tableName, count);
                });
    }
}