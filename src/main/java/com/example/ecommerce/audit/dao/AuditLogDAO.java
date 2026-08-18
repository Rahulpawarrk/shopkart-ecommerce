package com.example.ecommerce.audit.dao;

import com.example.ecommerce.audit.model.AuditLog;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for security and mutation audit logs in Microsoft SQL Server.
 */
public class AuditLogDAO {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogDAO.class);

    public long createAuditLog(AuditLog log) {
        try (Connection conn = DBConnection.getConnection()) {
            return createAuditLog(log, conn);
        } catch (SQLException e) {
            logger.error("Error creating standalone audit log", e);
            throw new DatabaseException("Failed to record audit log", e);
        }
    }

    public long createAuditLog(AuditLog log, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.audit_logs (user_id, action, entity_name, entity_id, " +
                     "old_value, new_value, ip_address, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATETIME())";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null) {
                stmt.setInt(1, log.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getEntityName());
            if (log.getEntityId() != null) {
                stmt.setInt(4, log.getEntityId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, log.getOldValue());
            stmt.setString(6, log.getNewValue());
            stmt.setString(7, log.getIpAddress());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    log.setAuditId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to record audit log, no ID obtained.");
        }
    }

    public Pagination<AuditLog> findAll(String action, String entityName, String keyword, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (action != null && !action.trim().isEmpty()) {
            where.append("AND a.action = ? ");
            params.add(action.trim());
        }

        if (entityName != null && !entityName.trim().isEmpty()) {
            where.append("AND a.entity_name = ? ");
            params.add(entityName.trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (u.email LIKE ? OR a.old_value LIKE ? OR a.new_value LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        String countSql = "SELECT COUNT(*) FROM dbo.audit_logs a " +
                          "LEFT JOIN dbo.users u ON a.user_id = u.user_id " + where;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            int idx = 1;
            for (Object p : params) countStmt.setObject(idx++, p);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting audit logs", e);
            throw new DatabaseException("Error counting audit logs", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT a.*, u.email AS user_email " +
                         "FROM dbo.audit_logs a " +
                         "LEFT JOIN dbo.users u ON a.user_id = u.user_id " +
                         where +
                         " ORDER BY a.created_at DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            int idx = 1;
            for (Object p : params) stmt.setObject(idx++, p);
            stmt.setInt(idx++, (page - 1) * pageSize);
            stmt.setInt(idx, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setAuditId(rs.getLong("audit_id"));
                    int uid = rs.getInt("user_id");
                    if (!rs.wasNull()) log.setUserId(uid);
                    log.setUserEmail(rs.getString("user_email"));
                    log.setAction(rs.getString("action"));
                    log.setEntityName(rs.getString("entity_name"));
                    int eid = rs.getInt("entity_id");
                    if (!rs.wasNull()) log.setEntityId(eid);
                    log.setOldValue(rs.getString("old_value"));
                    log.setNewValue(rs.getString("new_value"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);

                    list.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving audit logs", e);
            throw new DatabaseException("Error retrieving audit logs", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }
}
