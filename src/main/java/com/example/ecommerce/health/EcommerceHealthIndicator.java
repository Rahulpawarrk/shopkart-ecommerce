package com.example.ecommerce.health;

import com.example.ecommerce.config.DBConnection;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Spring Boot Actuator HealthIndicator for real-time monitoring and liveness/readiness probes.
 */
@Component
public class EcommerceHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            DataSource ds = DBConnection.getDataSource();
            if (ds != null) {
                try (Connection conn = ds.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("SELECT 1");
                    return Health.up()
                            .withDetail("database", "ONLINE")
                            .withDetail("product", conn.getMetaData().getDatabaseProductName())
                            .build();
                }
            }
            return Health.up().withDetail("database", "POOL_INITIALIZING").build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("database", "OFFLINE")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
