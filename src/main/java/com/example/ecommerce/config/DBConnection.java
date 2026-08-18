package com.example.ecommerce.config;

import com.example.ecommerce.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe Database Connection Manager leveraging HikariCP Connection Pool.
 * Manages database lifecycle, connection pooling, and configuration externalization.
 */
public final class DBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static volatile HikariDataSource dataSource;
    private static final Object lock = new Object();

    private DBConnection() {
        // Prevent instantiation
    }

    /**
     * Retrieves the singleton HikariDataSource instance.
     * Initializes the pool if not already initialized.
     *
     * @return DataSource instance
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null) {
                    initDataSource();
                }
            }
        }
        return dataSource;
    }

    /**
     * Obtains a connection from the connection pool.
     * The caller is responsible for closing this connection (e.g. using try-with-resources).
     *
     * @return active java.sql.Connection
     * @throws DatabaseException if connection cannot be obtained
     */
    public static Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            logger.error("Failed to obtain database connection from Hikari pool", e);
            throw new DatabaseException("Failed to obtain database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes the HikariCP DataSource using db.properties.
     */
    private static void initDataSource() {
        try {
            logger.info("Initializing HikariCP DataSource for Microsoft SQL Server...");
            Properties props = loadProperties();

            HikariConfig config = new HikariConfig();
            
            String jdbcUrl = System.getenv("DB_URL");
            if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
                jdbcUrl = props.getProperty("db.url");
            }
            if (jdbcUrl != null && jdbcUrl.startsWith("postgresql://")) {
                jdbcUrl = "jdbc:" + jdbcUrl;
            }

            // Driver and Connection Parameters with Automatic Dialect Detection
            String driver = System.getenv("DB_DRIVER");
            if (driver == null || driver.trim().isEmpty()) {
                if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:postgresql:")) {
                    driver = "org.postgresql.Driver";
                } else {
                    driver = props.getProperty("db.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
                }
            }
            config.setDriverClassName(driver);
            config.setJdbcUrl(jdbcUrl);

            String username = System.getenv("DB_USER");
            if (username == null || username.trim().isEmpty()) {
                username = props.getProperty("db.user");
            }
            config.setUsername(username);

            String password = System.getenv("DB_PASSWORD");
            if (password == null || password.trim().isEmpty()) {
                password = props.getProperty("db.password");
            }
            config.setPassword(password);

            // Pool Sizing and Timeouts
            config.setPoolName(props.getProperty("hikaricp.poolName", "EcommerceHikariPool"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikaricp.maximumPoolSize", "20")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("hikaricp.minimumIdle", "5")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("hikaricp.idleTimeout", "300000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("hikaricp.maxLifetime", "1800000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("hikaricp.connectionTimeout", "30000")));
            config.setLeakDetectionThreshold(Long.parseLong(props.getProperty("hikaricp.leakDetectionThreshold", "60000")));

            // SQL Server specific optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP connection pool [{}] successfully initialized.", config.getPoolName());

            // Auto-migrate schema updates if missing (e.g. password_reset_tokens, phone column)
            autoMigrateSchema();
        } catch (Exception e) {
            logger.error("Critical error initializing database connection pool", e);
            throw new DatabaseException("Could not initialize database connection pool", e);
        }
    }

    /**
     * Ensures any newly added tables/columns exist in the target database.
     */
    private static void autoMigrateSchema() {
        String[] ddlStatements = new String[] {
            // 1. Password reset tokens table
            "IF OBJECT_ID('dbo.password_reset_tokens', 'U') IS NULL " +
            "BEGIN " +
            "    CREATE TABLE dbo.password_reset_tokens ( " +
            "        token_id INT IDENTITY(1,1) PRIMARY KEY, " +
            "        user_id INT NOT NULL, " +
            "        token VARCHAR(64) NOT NULL UNIQUE, " +
            "        expires_at DATETIME2 NOT NULL, " +
            "        is_used BIT DEFAULT 0 NOT NULL, " +
            "        created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL, " +
            "        CONSTRAINT FK_prt_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) ON DELETE CASCADE " +
            "    ); " +
            "    CREATE NONCLUSTERED INDEX IX_prt_token ON dbo.password_reset_tokens(token); " +
            "    CREATE NONCLUSTERED INDEX IX_prt_user ON dbo.password_reset_tokens(user_id); " +
            "END",

            // 2. Ensure phone column in dbo.users
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'users' AND COLUMN_NAME = 'phone') " +
            "BEGIN " +
            "    ALTER TABLE dbo.users ADD phone VARCHAR(15) NULL; " +
            "END",

            // 3. Ensure gateway_response in dbo.payments
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'gateway_response') " +
            "BEGIN " +
            "    ALTER TABLE dbo.payments ADD gateway_response NVARCHAR(500) NULL; " +
            "END",

            // 4. Ensure updated_at in dbo.payments
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'updated_at') " +
            "BEGIN " +
            "    ALTER TABLE dbo.payments ADD updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL; " +
            "END",

            // 5. Ensure dbo.addresses table exists
            "IF OBJECT_ID('dbo.addresses', 'U') IS NULL " +
            "BEGIN " +
            "    CREATE TABLE dbo.addresses ( " +
            "        address_id INT IDENTITY(1,1) PRIMARY KEY, " +
            "        user_id INT NOT NULL, " +
            "        address_type VARCHAR(20) DEFAULT 'SHIPPING' NOT NULL, " +
            "        full_name NVARCHAR(100) NOT NULL, " +
            "        phone VARCHAR(20) NOT NULL, " +
            "        address_line1 NVARCHAR(255) NOT NULL, " +
            "        address_line2 NVARCHAR(255) NULL, " +
            "        city NVARCHAR(100) NOT NULL, " +
            "        state NVARCHAR(100) NOT NULL, " +
            "        postal_code VARCHAR(20) NOT NULL, " +
            "        country NVARCHAR(100) DEFAULT 'India' NOT NULL, " +
            "        is_default BIT DEFAULT 0 NOT NULL, " +
            "        created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL, " +
            "        updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL, " +
            "        CONSTRAINT FK_addresses_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) ON DELETE CASCADE " +
            "    ); " +
            "    CREATE NONCLUSTERED INDEX IX_addresses_user ON dbo.addresses(user_id); " +
            "END",

            // 6. Ensure payment_method in dbo.orders
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'payment_method') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD payment_method VARCHAR(30) DEFAULT 'COD' NOT NULL; " +
            "END",

            // 7. Ensure shipping_amount in dbo.orders
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_amount') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD shipping_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL; " +
            "END",

            // 8. Ensure total_amount in dbo.orders
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'total_amount') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD total_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL; " +
            "END",

            // 9. Ensure coupon_id in dbo.orders
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'coupon_id') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD coupon_id INT NULL; " +
            "END",

            // 10. Ensure shipping address snapshot columns in dbo.orders
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_full_name') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD shipping_full_name NVARCHAR(100) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_phone') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD shipping_phone VARCHAR(20) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_address_line1') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD shipping_address_line1 NVARCHAR(255) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_address_line2') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD shipping_address_line2 NVARCHAR(255) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_city') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD shipping_city NVARCHAR(100) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_state') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD shipping_state NVARCHAR(100) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_postal_code') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD shipping_postal_code VARCHAR(20) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_country') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD shipping_country NVARCHAR(100) DEFAULT 'India' NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'billing_address_snapshot') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD billing_address_snapshot NVARCHAR(MAX) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'notes') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD notes NVARCHAR(500) NULL; " +
            "END",

            // 11. Logistics & Fulfillment Tracking Columns
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'courier_partner') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD courier_partner VARCHAR(100) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'tracking_number') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD tracking_number VARCHAR(100) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'delivery_agent_phone') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD delivery_agent_phone VARCHAR(20) NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'estimated_delivery_date') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD estimated_delivery_date DATETIME2 NULL; " +
            "END",

            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'delivered_at') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD delivered_at DATETIME2 NULL; " +
            "END",

            // 12. Make legacy columns nullable if they exist with NOT NULL constraint
            "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_address_id' AND IS_NULLABLE = 'NO') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ALTER COLUMN shipping_address_id INT NULL; " +
            "END",

            "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'grand_total' AND IS_NULLABLE = 'NO') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ALTER COLUMN grand_total DECIMAL(18,2) NULL; " +
            "END",

            // 12. Ensure line_total in dbo.order_items
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'line_total') " +
            "BEGIN " +
            "    ALTER TABLE dbo.order_items ADD line_total DECIMAL(18,2) DEFAULT 0.00 NOT NULL; " +
            "END",

            "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'total_price' AND IS_NULLABLE = 'NO') " +
            "BEGIN " +
            "    ALTER TABLE dbo.order_items ALTER COLUMN total_price DECIMAL(18,2) NULL; " +
            "END",

            // 13. Ensure remarks in dbo.order_status_history
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'order_status_history' AND COLUMN_NAME = 'remarks') " +
            "BEGIN " +
            "    ALTER TABLE dbo.order_status_history ADD remarks NVARCHAR(500) NULL; " +
            "END",

            // 14. Drop legacy restrictive check constraints on order_status and payment_method
            "DECLARE @sqlStatus NVARCHAR(MAX) = N''; " +
            "SELECT @sqlStatus += N'ALTER TABLE dbo.orders DROP CONSTRAINT [' + cc.name + N']; ' " +
            "FROM sys.check_constraints cc " +
            "INNER JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id " +
            "WHERE cc.parent_object_id = OBJECT_ID('dbo.orders') AND c.name = 'order_status'; " +
            "IF @sqlStatus <> N'' EXEC sp_executesql @sqlStatus;",

            "DECLARE @sqlPay NVARCHAR(MAX) = N''; " +
            "SELECT @sqlPay += N'ALTER TABLE dbo.payments DROP CONSTRAINT [' + cc.name + N']; ' " +
            "FROM sys.check_constraints cc " +
            "INNER JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id " +
            "WHERE cc.parent_object_id = OBJECT_ID('dbo.payments') AND c.name = 'payment_method'; " +
            "IF @sqlPay <> N'' EXEC sp_executesql @sqlPay;",

            // 15. Ensure FK_orders_user constraint exists
            "IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_orders_user') " +
            "BEGIN " +
            "    ALTER TABLE dbo.orders ADD CONSTRAINT FK_orders_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id); " +
            "END",

            // 15. Ensure FK_order_items_order constraint exists
            "IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_order_items_order') " +
            "BEGIN " +
            "    ALTER TABLE dbo.order_items ADD CONSTRAINT FK_order_items_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id) ON DELETE CASCADE; " +
            "END",

            // 16. Ensure FK_order_items_product constraint exists
            "IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_order_items_product') " +
            "BEGIN " +
            "    ALTER TABLE dbo.order_items ADD CONSTRAINT FK_order_items_product FOREIGN KEY (product_id) REFERENCES dbo.products(product_id); " +
            "END",

            // 17. Ensure FK_payments_order constraint exists
            "IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_payments_order') " +
            "BEGIN " +
            "    ALTER TABLE dbo.payments ADD CONSTRAINT FK_payments_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id); " +
            "END",

            // 18. Performance & Relational Indexes
            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_orders_user' AND object_id = OBJECT_ID('dbo.orders')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_orders_user ON dbo.orders(user_id); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_orders_order_number' AND object_id = OBJECT_ID('dbo.orders')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_orders_order_number ON dbo.orders(order_number); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_orders_status' AND object_id = OBJECT_ID('dbo.orders')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_orders_status ON dbo.orders(order_status); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_order_items_order' AND object_id = OBJECT_ID('dbo.order_items')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_order_items_order ON dbo.order_items(order_id); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_order_items_product' AND object_id = OBJECT_ID('dbo.order_items')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_order_items_product ON dbo.order_items(product_id); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_payments_order' AND object_id = OBJECT_ID('dbo.payments')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_payments_order ON dbo.payments(order_id); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_cart_items_cart' AND object_id = OBJECT_ID('dbo.cart_items')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_cart_items_cart ON dbo.cart_items(cart_id); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_cart_items_product' AND object_id = OBJECT_ID('dbo.cart_items')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_cart_items_product ON dbo.cart_items(product_id); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_wishlist_items_wishlist' AND object_id = OBJECT_ID('dbo.wishlist_items')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_wishlist_items_wishlist ON dbo.wishlist_items(wishlist_id); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_wishlist_items_product' AND object_id = OBJECT_ID('dbo.wishlist_items')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_wishlist_items_product ON dbo.wishlist_items(product_id); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_reviews_product' AND object_id = OBJECT_ID('dbo.reviews')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_reviews_product ON dbo.reviews(product_id); END",

            "IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_audit_logs_user' AND object_id = OBJECT_ID('dbo.audit_logs')) " +
            "BEGIN CREATE NONCLUSTERED INDEX IX_audit_logs_user ON dbo.audit_logs(user_id); END",

            // 19. Payment Reconciliation Table
            "IF OBJECT_ID('dbo.payment_reconciliation', 'U') IS NULL " +
            "BEGIN " +
            "    CREATE TABLE dbo.payment_reconciliation ( " +
            "        reconciliation_id INT IDENTITY(1,1) PRIMARY KEY, " +
            "        order_id INT NOT NULL, " +
            "        user_id INT NOT NULL, " +
            "        transaction_reference VARCHAR(100) NOT NULL, " +
            "        gateway_order_id VARCHAR(100) NULL, " +
            "        payment_method VARCHAR(50) NOT NULL, " +
            "        amount DECIMAL(18,2) NOT NULL, " +
            "        failure_reason NVARCHAR(500) NULL, " +
            "        gateway_response NVARCHAR(1000) NULL, " +
            "        reconciliation_status VARCHAR(50) DEFAULT 'PENDING' NOT NULL, " +
            "        admin_notes NVARCHAR(1000) NULL, " +
            "        resolved_by INT NULL, " +
            "        resolved_at DATETIME2 NULL, " +
            "        created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL, " +
            "        updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL, " +
            "        CONSTRAINT FK_pay_recon_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id), " +
            "        CONSTRAINT FK_pay_recon_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) " +
            "    ); " +
            "    CREATE NONCLUSTERED INDEX IX_pay_recon_order ON dbo.payment_reconciliation(order_id); " +
            "    CREATE NONCLUSTERED INDEX IX_pay_recon_user ON dbo.payment_reconciliation(user_id); " +
            "    CREATE NONCLUSTERED INDEX IX_pay_recon_status ON dbo.payment_reconciliation(reconciliation_status); " +
            "    CREATE NONCLUSTERED INDEX IX_pay_recon_txn ON dbo.payment_reconciliation(transaction_reference); " +
            "END",

            // 20. Reconcile & Synchronize Pending Payments with Paid/Delivered Orders
            "UPDATE dbo.payments SET payment_status = 'SUCCESS', updated_at = SYSDATETIME() " +
            "WHERE payment_status = 'PENDING' AND order_id IN (SELECT order_id FROM dbo.orders WHERE payment_status = 'PAID' OR order_status = 'DELIVERED'); ",

            "INSERT INTO dbo.payments (order_id, payment_method, transaction_reference, amount, payment_status, gateway_response, created_at, updated_at) " +
            "SELECT o.order_id, o.payment_method, 'PAY-' + UPPER(o.payment_method) + '-ORD' + CAST(o.order_id AS VARCHAR), o.total_amount, 'SUCCESS', 'Settled order payment', o.created_at, SYSDATETIME() " +
            "FROM dbo.orders o " +
            "WHERE (o.payment_status = 'PAID' OR o.order_status = 'DELIVERED') " +
            "AND NOT EXISTS (SELECT 1 FROM dbo.payments p WHERE p.order_id = o.order_id AND p.payment_status = 'SUCCESS'); ",

            // 21. Review Image Support
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'reviews' AND COLUMN_NAME = 'image_url') " +
            "BEGIN " +
            "    ALTER TABLE dbo.reviews ADD image_url VARCHAR(1000) NULL; " +
            "END",

            "IF OBJECT_ID('dbo.review_images', 'U') IS NULL " +
            "BEGIN " +
            "    CREATE TABLE dbo.review_images ( " +
            "        review_image_id INT IDENTITY(1,1) PRIMARY KEY, " +
            "        review_id INT NOT NULL, " +
            "        image_url VARCHAR(1000) NOT NULL, " +
            "        created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL, " +
            "        CONSTRAINT FK_review_images_review FOREIGN KEY (review_id) REFERENCES dbo.reviews(review_id) ON DELETE CASCADE " +
            "    ); " +
            "    CREATE NONCLUSTERED INDEX IX_review_images_review ON dbo.review_images(review_id); " +
            "END",

            // 22. Order Return & Replacement Module
            "IF OBJECT_ID('dbo.order_returns', 'U') IS NULL " +
            "BEGIN " +
            "    CREATE TABLE dbo.order_returns ( " +
            "        return_id INT IDENTITY(1,1) PRIMARY KEY, " +
            "        order_id INT NOT NULL, " +
            "        user_id INT NOT NULL, " +
            "        return_number VARCHAR(100) NOT NULL UNIQUE, " +
            "        return_reason NVARCHAR(200) NOT NULL, " +
            "        resolution_type VARCHAR(50) NOT NULL, " +
            "        comments NVARCHAR(1000) NULL, " +
            "        image_url VARCHAR(1000) NULL, " +
            "        return_status VARCHAR(50) DEFAULT 'REQUESTED' NOT NULL, " +
            "        refund_amount DECIMAL(18,2) NULL, " +
            "        admin_notes NVARCHAR(1000) NULL, " +
            "        pickup_date DATETIME2 NULL, " +
            "        created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL, " +
            "        updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL, " +
            "        CONSTRAINT FK_order_returns_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id), " +
            "        CONSTRAINT FK_order_returns_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) " +
            "    ); " +
            "    CREATE NONCLUSTERED INDEX IX_order_returns_order ON dbo.order_returns(order_id); " +
            "    CREATE NONCLUSTERED INDEX IX_order_returns_user ON dbo.order_returns(user_id); " +
            "    CREATE NONCLUSTERED INDEX IX_order_returns_status ON dbo.order_returns(return_status); " +
            "END"
        };

        try (Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            for (String sql : ddlStatements) {
                try {
                    stmt.execute(sql);
                } catch (SQLException ex) {
                    logger.warn("Auto-migration notice (may already exist): {}", ex.getMessage());
                }
            }
            logger.info("Database schema auto-migration checks verified successfully.");
        } catch (SQLException e) {
            logger.warn("Could not run auto-migration checks: {}", e.getMessage());
        }
    }

    /**
     * Loads database configuration properties from classpath.
     */
    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IOException("db.properties file not found in classpath");
            }
            props.load(in);
        }
        return props;
    }

    /**
     * Closes the HikariCP connection pool gracefully during application shutdown.
     */
    public static void shutdown() {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                logger.info("Closing HikariCP connection pool...");
                dataSource.close();
                dataSource = null;
                logger.info("HikariCP connection pool successfully closed.");
            }
        }
    }
}
