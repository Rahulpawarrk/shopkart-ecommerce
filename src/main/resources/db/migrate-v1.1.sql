-- ==============================================================================
-- Migration Patch v1.1 — Apply to existing ecommerce_db
-- Run this script against your live database. It is idempotent (safe to re-run).
-- ==============================================================================

USE ecommerce_db;
GO

-- ── 1. Add password_reset_tokens table (if not exists) ────────────────────
IF OBJECT_ID('dbo.password_reset_tokens', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.password_reset_tokens (
        token_id   INT IDENTITY(1,1) PRIMARY KEY,
        user_id    INT NOT NULL,
        token      VARCHAR(64)  NOT NULL UNIQUE,
        expires_at DATETIME2    NOT NULL,
        is_used    BIT          DEFAULT 0 NOT NULL,
        created_at DATETIME2    DEFAULT SYSDATETIME() NOT NULL,
        CONSTRAINT FK_prt_user FOREIGN KEY (user_id)
            REFERENCES dbo.users(user_id) ON DELETE CASCADE
    );
    CREATE NONCLUSTERED INDEX IX_prt_token ON dbo.password_reset_tokens(token);
    CREATE NONCLUSTERED INDEX IX_prt_user  ON dbo.password_reset_tokens(user_id);
    PRINT 'Created dbo.password_reset_tokens';
END
ELSE
    PRINT 'dbo.password_reset_tokens already exists — skipped';
GO

-- ── 2. Add phone column to users table (if not exists) ────────────────────
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'users' AND COLUMN_NAME = 'phone'
)
BEGIN
    ALTER TABLE dbo.users ADD phone VARCHAR(15) NULL;
    PRINT 'Added phone column to dbo.users';
END
ELSE
    PRINT 'phone column already exists in dbo.users — skipped';
GO

-- ── 3. Add gateway_response & updated_at to payments table (if missing) ───
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'gateway_response'
)
BEGIN
    ALTER TABLE dbo.payments ADD gateway_response NVARCHAR(500) NULL;
    PRINT 'Added gateway_response column to dbo.payments';
END
ELSE
    PRINT 'gateway_response column already exists in dbo.payments — skipped';

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'updated_at'
)
BEGIN
    ALTER TABLE dbo.payments ADD updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL;
    PRINT 'Added updated_at column to dbo.payments';
END
ELSE
    PRINT 'updated_at column already exists in dbo.payments — skipped';
GO

-- ── 4. Drop restrictive order_status and payment_method CHECK constraints ──
DECLARE @sqlOrdersStatus NVARCHAR(MAX) = N'';
SELECT @sqlOrdersStatus += N'ALTER TABLE dbo.orders DROP CONSTRAINT [' + cc.name + N']; '
FROM sys.check_constraints cc
INNER JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id
WHERE cc.parent_object_id = OBJECT_ID('dbo.orders') AND c.name = 'order_status';

IF @sqlOrdersStatus <> N''
BEGIN
    EXEC sp_executesql @sqlOrdersStatus;
    PRINT 'Dropped old order_status check constraints on dbo.orders';
END
GO

DECLARE @sqlPayMethod NVARCHAR(MAX) = N'';
SELECT @sqlPayMethod += N'ALTER TABLE dbo.payments DROP CONSTRAINT [' + cc.name + N']; '
FROM sys.check_constraints cc
INNER JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id
WHERE cc.parent_object_id = OBJECT_ID('dbo.payments') AND c.name = 'payment_method';

IF @sqlPayMethod <> N''
BEGIN
    EXEC sp_executesql @sqlPayMethod;
    PRINT 'Dropped old payment_method check constraints on dbo.payments';
END
GO

-- ── 5. Ensure missing columns exist in dbo.orders table ────────────────────
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'payment_method'
)
BEGIN
    ALTER TABLE dbo.orders ADD payment_method VARCHAR(30) DEFAULT 'COD' NOT NULL;
    PRINT 'Added payment_method to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_amount'
)
BEGIN
    ALTER TABLE dbo.orders ADD shipping_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL;
    PRINT 'Added shipping_amount to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'total_amount'
)
BEGIN
    ALTER TABLE dbo.orders ADD total_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL;
    PRINT 'Added total_amount to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'coupon_id'
)
BEGIN
    ALTER TABLE dbo.orders ADD coupon_id INT NULL;
    PRINT 'Added coupon_id to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_full_name'
)
BEGIN
    ALTER TABLE dbo.orders ADD shipping_full_name NVARCHAR(100) NULL;
    PRINT 'Added shipping_full_name to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_phone'
)
BEGIN
    ALTER TABLE dbo.orders ADD shipping_phone VARCHAR(20) NULL;
    PRINT 'Added shipping_phone to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_address_line1'
)
BEGIN
    ALTER TABLE dbo.orders ADD shipping_address_line1 NVARCHAR(255) NULL;
    PRINT 'Added shipping_address_line1 to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_address_line2'
)
BEGIN
    ALTER TABLE dbo.orders ADD shipping_address_line2 NVARCHAR(255) NULL;
    PRINT 'Added shipping_address_line2 to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_city'
)
BEGIN
    ALTER TABLE dbo.orders ADD shipping_city NVARCHAR(100) NULL;
    PRINT 'Added shipping_city to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_state'
)
BEGIN
    ALTER TABLE dbo.orders ADD shipping_state NVARCHAR(100) NULL;
    PRINT 'Added shipping_state to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_postal_code'
)
BEGIN
    ALTER TABLE dbo.orders ADD shipping_postal_code VARCHAR(20) NULL;
    PRINT 'Added shipping_postal_code to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_country'
)
BEGIN
    ALTER TABLE dbo.orders ADD shipping_country NVARCHAR(100) DEFAULT 'India' NULL;
    PRINT 'Added shipping_country to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'billing_address_snapshot'
)
BEGIN
    ALTER TABLE dbo.orders ADD billing_address_snapshot NVARCHAR(MAX) NULL;
    PRINT 'Added billing_address_snapshot to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'notes'
)
BEGIN
    ALTER TABLE dbo.orders ADD notes NVARCHAR(500) NULL;
    PRINT 'Added notes to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'courier_partner'
)
BEGIN
    ALTER TABLE dbo.orders ADD courier_partner VARCHAR(100) NULL;
    PRINT 'Added courier_partner to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'tracking_number'
)
BEGIN
    ALTER TABLE dbo.orders ADD tracking_number VARCHAR(100) NULL;
    PRINT 'Added tracking_number to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'delivery_agent_phone'
)
BEGIN
    ALTER TABLE dbo.orders ADD delivery_agent_phone VARCHAR(20) NULL;
    PRINT 'Added delivery_agent_phone to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'estimated_delivery_date'
)
BEGIN
    ALTER TABLE dbo.orders ADD estimated_delivery_date DATETIME2 NULL;
    PRINT 'Added estimated_delivery_date to dbo.orders';
END

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'delivered_at'
)
BEGIN
    ALTER TABLE dbo.orders ADD delivered_at DATETIME2 NULL;
    PRINT 'Added delivered_at to dbo.orders';
END

-- Ensure legacy not-null columns don't block order insertion
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_address_id' AND IS_NULLABLE = 'NO')
BEGIN
    ALTER TABLE dbo.orders ALTER COLUMN shipping_address_id INT NULL;
    PRINT 'Made shipping_address_id nullable in dbo.orders';
END

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'grand_total' AND IS_NULLABLE = 'NO')
BEGIN
    ALTER TABLE dbo.orders ALTER COLUMN grand_total DECIMAL(18,2) NULL;
    PRINT 'Made grand_total nullable in dbo.orders';
END
GO

-- ── 6. Ensure line_total exists in dbo.order_items table ─────────────────
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'line_total'
)
BEGIN
    ALTER TABLE dbo.order_items ADD line_total DECIMAL(18,2) DEFAULT 0.00 NOT NULL;
    PRINT 'Added line_total to dbo.order_items';
END

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'total_price' AND IS_NULLABLE = 'NO'
)
BEGIN
    ALTER TABLE dbo.order_items ALTER COLUMN total_price DECIMAL(18,2) NULL;
    PRINT 'Made total_price nullable in dbo.order_items';
END
GO

-- ── 7. Ensure remarks exists in dbo.order_status_history table ────────────
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'order_status_history' AND COLUMN_NAME = 'remarks'
)
BEGIN
    ALTER TABLE dbo.order_status_history ADD remarks NVARCHAR(500) NULL;
    PRINT 'Added remarks to dbo.order_status_history';
END
GO

-- ── 8. Ensure Relational Foreign Key Constraints exist ───────────────────
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_orders_user')
BEGIN
    ALTER TABLE dbo.orders ADD CONSTRAINT FK_orders_user 
        FOREIGN KEY (user_id) REFERENCES dbo.users(user_id);
    PRINT 'Created FK_orders_user';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_order_items_order')
BEGIN
    ALTER TABLE dbo.order_items ADD CONSTRAINT FK_order_items_order 
        FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id) ON DELETE CASCADE;
    PRINT 'Created FK_order_items_order';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_order_items_product')
BEGIN
    ALTER TABLE dbo.order_items ADD CONSTRAINT FK_order_items_product 
        FOREIGN KEY (product_id) REFERENCES dbo.products(product_id);
    PRINT 'Created FK_order_items_product';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_payments_order')
BEGIN
    ALTER TABLE dbo.payments ADD CONSTRAINT FK_payments_order 
        FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id);
    PRINT 'Created FK_payments_order';
END
GO

-- ── 9. Ensure Performance & Relational Indexes exist ─────────────────────
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_orders_user' AND object_id = OBJECT_ID('dbo.orders'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_orders_user ON dbo.orders(user_id);
    PRINT 'Created Index IX_orders_user';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_orders_order_number' AND object_id = OBJECT_ID('dbo.orders'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_orders_order_number ON dbo.orders(order_number);
    PRINT 'Created Index IX_orders_order_number';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_orders_status' AND object_id = OBJECT_ID('dbo.orders'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_orders_status ON dbo.orders(order_status);
    PRINT 'Created Index IX_orders_status';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_order_items_order' AND object_id = OBJECT_ID('dbo.order_items'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_order_items_order ON dbo.order_items(order_id);
    PRINT 'Created Index IX_order_items_order';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_order_items_product' AND object_id = OBJECT_ID('dbo.order_items'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_order_items_product ON dbo.order_items(product_id);
    PRINT 'Created Index IX_order_items_product';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_payments_order' AND object_id = OBJECT_ID('dbo.payments'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_payments_order ON dbo.payments(order_id);
    PRINT 'Created Index IX_payments_order';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_cart_items_cart' AND object_id = OBJECT_ID('dbo.cart_items'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_cart_items_cart ON dbo.cart_items(cart_id);
    PRINT 'Created Index IX_cart_items_cart';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_cart_items_product' AND object_id = OBJECT_ID('dbo.cart_items'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_cart_items_product ON dbo.cart_items(product_id);
    PRINT 'Created Index IX_cart_items_product';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_wishlist_items_wishlist' AND object_id = OBJECT_ID('dbo.wishlist_items'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_wishlist_items_wishlist ON dbo.wishlist_items(wishlist_id);
    PRINT 'Created Index IX_wishlist_items_wishlist';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_wishlist_items_product' AND object_id = OBJECT_ID('dbo.wishlist_items'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_wishlist_items_product ON dbo.wishlist_items(product_id);
    PRINT 'Created Index IX_wishlist_items_product';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_reviews_product' AND object_id = OBJECT_ID('dbo.reviews'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_reviews_product ON dbo.reviews(product_id);
    PRINT 'Created Index IX_reviews_product';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_reviews_user' AND object_id = OBJECT_ID('dbo.reviews'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_reviews_user ON dbo.reviews(user_id);
    PRINT 'Created Index IX_reviews_user';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_audit_logs_user' AND object_id = OBJECT_ID('dbo.audit_logs'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_audit_logs_user ON dbo.audit_logs(user_id);
    PRINT 'Created Index IX_audit_logs_user';
END
GO

PRINT 'Migration patch v1.1 complete.';
GO
