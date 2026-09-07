package com.example.ecommerce.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.TimeZone;

/**
 * Application Lifecycle Listener.
 * Enforces IST (+05:30) Timezone and manages database pool startup and graceful
 * shutdown.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 1. Force Indian Standard Time (IST / Asia/Kolkata +05:30) across entire
        // application JVM
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        System.setProperty("user.timezone", "Asia/Kolkata");

        logger.info("===============================================================");
        logger.info("Starting Enterprise E-Commerce Web Application on Tomcat 11...");
        logger.info("Application TimeZone initialized to: {} ({})",
                TimeZone.getDefault().getID(), TimeZone.getDefault().getDisplayName());
        logger.info("===============================================================");

        logger.info("Application lifecycle listener initialized; Spring Boot manages DataSource lifecycle.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Shutting down Enterprise E-Commerce Web Application...");

        // 1. Shutdown HikariCP connection pool
        DBConnection.shutdown();

        // 2. Shutdown Redis connection pool
        RedisManager.shutdown();

        // 3. Deregister JDBC Drivers to prevent Tomcat classloader memory leaks
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                logger.info("Deregistered JDBC driver: {}", driver);
            } catch (SQLException e) {
                logger.error("Error deregistering JDBC driver: {}", driver, e);
            }
        }

        logger.info("Application context destroyed cleanly.");
    }
}