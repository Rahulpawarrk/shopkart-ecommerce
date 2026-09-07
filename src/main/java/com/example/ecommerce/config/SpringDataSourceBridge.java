package com.example.ecommerce.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Bridges Spring Boot's Hikari DataSource bean to the legacy DBConnection manager,
 * ensuring all legacy JDBC DAOs and Spring Data JPA / Hibernate share the same
 * high-performance connection pool and transaction context.
 */
@Configuration
public class SpringDataSourceBridge {

    private static final Logger logger = LoggerFactory.getLogger(SpringDataSourceBridge.class);

    private final DataSource dataSource;

    public SpringDataSourceBridge(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        logger.info("Connecting Spring Boot DataSource bean to DBConnection bridge...");
        DBConnection.setDataSource(dataSource);
    }
}
