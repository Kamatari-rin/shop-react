package org.example.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("orderDetailDatabase")
@Slf4j
@RequiredArgsConstructor
public class OrderDetailDatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        long startTime = System.currentTimeMillis();
        try {
            jdbcTemplate.execute("SELECT 1");
            long responseTime = System.currentTimeMillis() - startTime;
            return Health.up()
                    .withDetail("database", "ordersdb reachable")
                    .withDetail("responseTime", responseTime + "ms")
                    .build();
        } catch (Exception e) {
            log.error("Error checking database connectivity for order-detail", e);
            return Health.down(e)
                    .withDetail("responseTime", (System.currentTimeMillis() - startTime) + "ms")
                    .build();
        }
    }
}