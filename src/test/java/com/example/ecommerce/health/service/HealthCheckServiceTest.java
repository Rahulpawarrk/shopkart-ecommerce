package com.example.ecommerce.health.service;

import com.example.ecommerce.health.dao.HealthCheckDAO;
import com.example.ecommerce.health.model.SystemHealth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckService Unit Tests with Mockito")
class HealthCheckServiceTest {

    @Mock
    private HealthCheckDAO healthCheckDAO;

    @InjectMocks
    private HealthCheckService healthCheckService;

    @Test
    @DisplayName("Should return healthy system status when database ping succeeds")
    void testGetSystemHealthSuccess() {
        Map<String, Object> mockDbStatus = new HashMap<>();
        mockDbStatus.put("connected", true);
        mockDbStatus.put("productName", "Microsoft SQL Server");
        mockDbStatus.put("productVersion", "16.0.1000.6");
        mockDbStatus.put("url", "jdbc:sqlserver://localhost:1433;databaseName=ecommerce_db");

        when(healthCheckDAO.checkDatabaseHealth()).thenReturn(mockDbStatus);

        SystemHealth health = healthCheckService.getSystemHealth("Apache Tomcat/11.0.0");

        assertNotNull(health);
        assertEquals("Enterprise E-Commerce Platform", health.getAppName());
        assertTrue(health.isDatabaseConnected());
        assertEquals("Microsoft SQL Server", health.getDatabaseProductName());
        assertEquals("Apache Tomcat/11.0.0", health.getServletVersion());
        assertTrue(health.getTotalMemoryMb() > 0);
        assertTrue(health.getAvailableProcessors() >= 1);
        assertNull(health.getDatabaseErrorMessage());
    }

    @Test
    @DisplayName("Should populate error message when database connection fails")
    void testGetSystemHealthDatabaseFailure() {
        Map<String, Object> mockDbStatus = new HashMap<>();
        mockDbStatus.put("connected", false);
        mockDbStatus.put("error", "Login failed for user 'sa'");

        when(healthCheckDAO.checkDatabaseHealth()).thenReturn(mockDbStatus);

        SystemHealth health = healthCheckService.getSystemHealth("Apache Tomcat/11.0.0");

        assertNotNull(health);
        assertFalse(health.isDatabaseConnected());
        assertEquals("Login failed for user 'sa'", health.getDatabaseErrorMessage());
    }
}
