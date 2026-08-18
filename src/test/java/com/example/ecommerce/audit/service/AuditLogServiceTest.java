package com.example.ecommerce.audit.service;

import com.example.ecommerce.audit.dao.AuditLogDAO;
import com.example.ecommerce.audit.model.AuditLog;
import com.example.ecommerce.util.Pagination;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService Unit Tests with Mockito")
class AuditLogServiceTest {

    @Mock
    private AuditLogDAO auditLogDAO;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    @DisplayName("Should successfully record audit log event")
    void testLogAction() {
        when(auditLogDAO.createAuditLog(any(AuditLog.class))).thenReturn(101L);

        auditLogService.logAction(1, "UPDATE_PRICE", "PRODUCT", 5, "₹1000", "₹1200", "192.168.1.1");

        verify(auditLogDAO, times(1)).createAuditLog(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should retrieve paginated audit logs")
    void testGetAuditLogs() {
        Pagination<AuditLog> page = new Pagination<>(Collections.singletonList(new AuditLog()), 1, 10, 1);
        when(auditLogDAO.findAll(null, null, null, 1, 10)).thenReturn(page);

        Pagination<AuditLog> result = auditLogService.getAuditLogs(null, null, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalItems());
    }
}
