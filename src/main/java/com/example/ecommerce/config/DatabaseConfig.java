package com.example.ecommerce.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Enterprise Database Configuration for Spring Boot.
 * 
 * Provides seamless cloud deployment on platforms such as Render, Railway,
 * Heroku, Neon, and AWS RDS by automatically parsing and converting
 * standard cloud database URIs (postgres:// and postgresql://) into
 * compliant JDBC connection strings with extracted credentials.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/ecommerce_db}") String configuredUrl,
            @Value("${spring.datasource.username:postgres}") String configuredUsername,
            @Value("${spring.datasource.password:}") String configuredPassword,
            @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}") String driverClassName,
            @Value("${spring.datasource.hikari.maximum-pool-size:20}") int maxPoolSize,
            @Value("${spring.datasource.hikari.minimum-idle:5}") int minIdle,
            @Value("${spring.datasource.hikari.idle-timeout:300000}") long idleTimeout,
            @Value("${spring.datasource.hikari.max-lifetime:1800000}") long maxLifetime,
            @Value("${spring.datasource.hikari.connection-timeout:30000}") long connectionTimeout,
            @Value("${spring.datasource.hikari.leak-detection-threshold:60000}") long leakThreshold) {

        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            rawUrl = System.getenv("DB_URL");
        }
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            rawUrl = configuredUrl;
        }

        String jdbcUrl = rawUrl != null ? rawUrl.trim() : "jdbc:postgresql://localhost:5432/ecommerce_db";
        String username = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : configuredUsername;
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : configuredPassword;

        // Auto-convert standard Render / Cloud PostgreSQL URI (postgres://user:pass@host:port/db)
        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                String normalizedUri = rawUrl.startsWith("postgres://")
                        ? "http://" + rawUrl.substring("postgres://".length())
                        : "http://" + rawUrl.substring("postgresql://".length());

                URI uri = new URI(normalizedUri);

                if (uri.getUserInfo() != null && !uri.getUserInfo().isEmpty()) {
                    String[] parts = uri.getUserInfo().split(":", 2);
                    username = parts[0];
                    if (parts.length > 1) {
                        password = parts[1];
                    }
                }

                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                if (path == null || path.isEmpty()) {
                    path = "/ecommerce_db";
                }
                String query = uri.getQuery() != null && !uri.getQuery().isEmpty() ? "?" + uri.getQuery() : "";
                jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + path + query;
                logger.info("Successfully normalized Cloud PostgreSQL connection URL: {}", jdbcUrl);
            } catch (Exception e) {
                logger.warn("Could not parse cloud database URI with java.net.URI, falling back to prefix replacement", e);
                if (rawUrl.startsWith("postgres://")) {
                    jdbcUrl = "jdbc:postgresql://" + rawUrl.substring("postgres://".length());
                } else if (rawUrl.startsWith("postgresql://")) {
                    jdbcUrl = "jdbc:" + rawUrl;
                }
            }
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("EcommerceHikariPool");
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName(driverClassName);

        if (username != null && !username.trim().isEmpty()) {
            config.setUsername(username);
        }
        if (password != null && !password.trim().isEmpty()) {
            config.setPassword(password);
        }

        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setConnectionTimeout(connectionTimeout);
        config.setLeakDetectionThreshold(leakThreshold);

        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            config.setConnectionTestQuery("SELECT 1");
        }

        logger.info("Initializing HikariCP primary DataSource for: {}", jdbcUrl);
        HikariDataSource ds = new HikariDataSource(config);

        // Share the configured DataSource with legacy JDBC DBConnection manager
        DBConnection.setDataSource(ds);

        return ds;
    }
}
