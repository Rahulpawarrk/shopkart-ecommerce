package com.example.ecommerce.health.dao;

import com.example.ecommerce.config.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO responsible for performing low-level database health verification.
 */
public class HealthCheckDAO {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckDAO.class);

    /**
     * Executes a ping query against SQL Server and extracts metadata.
     *
     * @return Map containing metadata (connected, productName, productVersion, url, error)
     */
    public Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> status = new HashMap<>();

        // Test connection using try-with-resources
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                // Execute a lightweight validation query
                try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 AS ping");
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        status.put("connected", true);
                    }
                }

                // Retrieve Database Metadata
                DatabaseMetaData metaData = conn.getMetaData();
                status.put("productName", metaData.getDatabaseProductName());
                status.put("productVersion", metaData.getDatabaseProductVersion());
                status.put("url", metaData.getURL());
            } else {
                status.put("connected", false);
                status.put("error", "Database connection returned null or closed state");
            }
        } catch (SQLException e) {
            logger.error("Health check query failed against SQL Server", e);
            status.put("connected", false);
            status.put("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during database health check", e);
            status.put("connected", false);
            status.put("error", e.getMessage());
        }

        return status;
    }
}
