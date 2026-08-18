package com.example.ecommerce.audit.service;

import com.example.ecommerce.audit.dao.AuditLogDAO;
import com.example.ecommerce.audit.model.AuditLog;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service Layer recording and exploring system audit logs.
 */
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogDAO auditLogDAO;

    public AuditLogService() {
        this.auditLogDAO = new AuditLogDAO();
    }

    public AuditLogService(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
    }

    public void logAction(Integer userId, String action, String entityName, Integer entityId,
                          String oldValue, String newValue, String ipAddress) {
        try {
            AuditLog log = new AuditLog(userId, action, entityName, entityId, oldValue, newValue, ipAddress);
            auditLogDAO.createAuditLog(log);
            logger.debug("Recorded audit log: [{}] on {} [{}]", action, entityName, entityId);
        } catch (Exception e) {
            logger.error("Failed to record audit log asynchronously", e);
        }
    }

    public void logActionTransactional(Integer userId, String action, String entityName, Integer entityId,
                                       String oldValue, String newValue, String ipAddress, Connection conn) throws SQLException {
        AuditLog log = new AuditLog(userId, action, entityName, entityId, oldValue, newValue, ipAddress);
        auditLogDAO.createAuditLog(log, conn);
    }

    public Pagination<AuditLog> getAuditLogs(String action, String entityName, String keyword, int page, int pageSize) {
        return auditLogDAO.findAll(action, entityName, keyword, page, pageSize);
    }
}
