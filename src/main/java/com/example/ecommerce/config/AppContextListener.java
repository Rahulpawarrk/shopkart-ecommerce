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

/**
 * Application Lifecycle Listener.
 * Manages database pool startup and graceful shutdown on Tomcat deployment/undeployment.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("===============================================================");
        logger.info("Starting Enterprise E-Commerce Web Application on Tomcat 11...");
        logger.info("===============================================================");
        try {
            // Eagerly initialize DB pool to detect connectivity issues early on startup
            DBConnection.getDataSource();
            logger.info("Database connection pool initialized successfully.");
        } catch (Exception e) {
            logger.warn("Database connection could not be established on startup. Will attempt on first request. Cause: {}", e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Shutting down Enterprise E-Commerce Web Application...");
        
        // 1. Shutdown HikariCP connection pool
        DBConnection.shutdown();

        // 2. Deregister JDBC Drivers to prevent Tomcat classloader memory leaks
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
