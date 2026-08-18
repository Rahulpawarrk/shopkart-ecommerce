package com.example.ecommerce.health.service;

import com.example.ecommerce.health.dao.HealthCheckDAO;
import com.example.ecommerce.health.model.SystemHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Service Layer for aggregating system environment, JVM metrics, and database health.
 */
public class HealthCheckService {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    private final HealthCheckDAO healthCheckDAO;

    public HealthCheckService() {
        this.healthCheckDAO = new HealthCheckDAO();
    }

    public HealthCheckService(HealthCheckDAO healthCheckDAO) {
        this.healthCheckDAO = healthCheckDAO;
    }

    /**
     * Gathers comprehensive runtime and database diagnostics.
     *
     * @param servletVersion version of the Jakarta Servlet container
     * @return SystemHealth populated model
     */
    public SystemHealth getSystemHealth(String servletVersion) {
        logger.debug("Collecting system health metrics...");
        SystemHealth health = new SystemHealth();

        // Application and Runtime Info
        health.setAppName("Enterprise E-Commerce Platform");
        health.setAppVersion("1.0.0");
        health.setJavaVersion(System.getProperty("java.version"));
        health.setOsName(System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        health.setServletVersion(servletVersion != null ? servletVersion : "Jakarta Servlet 6.0+");

        // JVM Memory Info (Convert bytes to Megabytes)
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        health.setAvailableProcessors(runtime.availableProcessors());
        health.setMaxMemoryMb(maxMemory / (1024 * 1024));
        health.setTotalMemoryMb(totalMemory / (1024 * 1024));
        health.setFreeMemoryMb(freeMemory / (1024 * 1024));
        health.setUsedMemoryMb(usedMemory / (1024 * 1024));

        // Database Diagnostics from DAO
        Map<String, Object> dbStatus = healthCheckDAO.checkDatabaseHealth();
        boolean connected = Boolean.TRUE.equals(dbStatus.get("connected"));
        health.setDatabaseConnected(connected);

        if (connected) {
            health.setDatabaseProductName((String) dbStatus.get("productName"));
            health.setDatabaseProductVersion((String) dbStatus.get("productVersion"));
            health.setDatabaseUrl((String) dbStatus.get("url"));
        } else {
            health.setDatabaseErrorMessage((String) dbStatus.get("error"));
        }

        return health;
    }
}
