package com.example.ecommerce;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

/**
 * Main Spring Boot Application Entry Point for ShopKart E-Commerce Platform.
 *
 * Configured with:
 * - @ServletComponentScan: Automatically registers existing Jakarta Servlets,
 *   Filters, and WebListeners into the embedded/external Tomcat container.
 * - SpringBootServletInitializer: Enables dual-runtime support (standalone executable
 *   jar/war or deployed to external Tomcat / cloud containers).
 * - Timezone Enforcement: Locks Asia/Kolkata (IST +05:30) on startup.
 */
@SpringBootApplication
@ServletComponentScan(basePackages = "com.example.ecommerce")
@EnableTransactionManagement
public class EcommerceApplication extends SpringBootServletInitializer {

    private static final Logger logger = LoggerFactory.getLogger(EcommerceApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(EcommerceApplication.class);
    }

    @PostConstruct
    public void initTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        System.setProperty("user.timezone", "Asia/Kolkata");
        logger.info("Application JVM Timezone initialized to: {} ({})",
                TimeZone.getDefault().getID(), TimeZone.getDefault().getDisplayName());
    }

    public static void main(String[] args) {
        // Enforce IST before Spring context boots
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        System.setProperty("user.timezone", "Asia/Kolkata");

        SpringApplication.run(EcommerceApplication.class, args);
    }
}
