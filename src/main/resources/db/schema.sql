-- ============================================================================
-- DATABASE INITIALIZATION SCRIPT FOR MICROSOFT SQL SERVER
-- Project: Enterprise E-Commerce Web Application (Tomcat 11 + Jakarta EE 11)
-- Database: ecommerce_db
-- ============================================================================

-- Switch to database context if it already exists, or create database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'ecommerce_db')
BEGIN
    CREATE DATABASE ecommerce_db;
END
GO

USE ecommerce_db;
GO

-- ============================================================================
-- 1. USERS, ROLES & AUTHENTICATION
-- ============================================================================

-- Table: roles
IF OBJECT_ID('dbo.user_roles', 'U') IS NOT NULL DROP TABLE dbo.user_roles;
IF OBJECT_ID('dbo.roles', 'U') IS NOT NULL DROP TABLE dbo.roles;

CREATE TABLE dbo.roles (
    role_id INT IDENTITY(1,1) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255) NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL
);

-- Table: users
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;

CREATE TABLE dbo.users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION')),
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL
);

-- Table: user_roles (Many-to-Many between Users and Roles)
CREATE TABLE dbo.user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    assigned_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES dbo.roles(role_id) ON DELETE CASCADE
);

-- ============================================================================
-- 2. CUSTOMER ADDRESSES
-- ============================================================================

IF OBJECT_ID('dbo.addresses', 'U') IS NOT NULL DROP TABLE dbo.addresses;

CREATE TABLE dbo.addresses (
    address_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    address_type VARCHAR(20) DEFAULT 'SHIPPING' NOT NULL CHECK (address_type IN ('SHIPPING', 'BILLING', 'BOTH')),
    full_name NVARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address_line1 NVARCHAR(255) NOT NULL,
    address_line2 NVARCHAR(255) NULL,
    city NVARCHAR(100) NOT NULL,
    state NVARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country NVARCHAR(100) DEFAULT 'India' NOT NULL,
    is_default BIT DEFAULT 0 NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_addresses_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) ON DELETE CASCADE
);

-- ============================================================================
-- 3. CATEGORIES & PRODUCTS
-- ============================================================================

IF OBJECT_ID('dbo.categories', 'U') IS NOT NULL DROP TABLE dbo.categories;

CREATE TABLE dbo.categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    parent_category_id INT NULL,
    category_name NVARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description NVARCHAR(500) NULL,
    is_active BIT DEFAULT 1 NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_categories_parent FOREIGN KEY (parent_category_id) REFERENCES dbo.categories(category_id)
);

IF OBJECT_ID('dbo.products', 'U') IS NOT NULL DROP TABLE dbo.products;

CREATE TABLE dbo.products (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    category_id INT NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    product_name NVARCHAR(255) NOT NULL,
    slug VARCHAR(300) NOT NULL UNIQUE,
    description NVARCHAR(MAX) NULL,
    brand NVARCHAR(100) NULL,
    price DECIMAL(18,2) NOT NULL CHECK (price >= 0),
    discount_percentage DECIMAL(5,2) DEFAULT 0.00 NOT NULL CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    tax_percentage DECIMAL(5,2) DEFAULT 0.00 NOT NULL CHECK (tax_percentage >= 0),
    weight_kg DECIMAL(8,3) DEFAULT 0.000 NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_products_category FOREIGN KEY (category_id) REFERENCES dbo.categories(category_id)
);

-- Table: product_images
IF OBJECT_ID('dbo.product_images', 'U') IS NOT NULL DROP TABLE dbo.product_images;

CREATE TABLE dbo.product_images (
    image_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    alt_text NVARCHAR(255) NULL,
    display_order INT DEFAULT 0 NOT NULL,
    is_primary BIT DEFAULT 0 NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_product_images_product FOREIGN KEY (product_id) REFERENCES dbo.products(product_id) ON DELETE CASCADE
);

-- ============================================================================
-- 4. INVENTORY & INVENTORY TRANSACTIONS
-- ============================================================================

IF OBJECT_ID('dbo.inventory', 'U') IS NOT NULL DROP TABLE dbo.inventory;

CREATE TABLE dbo.inventory (
    inventory_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    quantity INT DEFAULT 0 NOT NULL CHECK (quantity >= 0),
    low_stock_threshold INT DEFAULT 5 NOT NULL CHECK (low_stock_threshold >= 0),
    last_updated DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_inventory_product FOREIGN KEY (product_id) REFERENCES dbo.products(product_id) ON DELETE CASCADE
);

IF OBJECT_ID('dbo.inventory_transactions', 'U') IS NOT NULL DROP TABLE dbo.inventory_transactions;

CREATE TABLE dbo.inventory_transactions (
    transaction_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    previous_stock INT NOT NULL CHECK (previous_stock >= 0),
    quantity_changed INT NOT NULL, -- positive for increment, negative for decrement
    new_stock INT NOT NULL CHECK (new_stock >= 0),
    transaction_type VARCHAR(30) NOT NULL CHECK (transaction_type IN ('PURCHASE', 'SALE', 'RETURN', 'CANCELLATION', 'ADJUSTMENT')),
    reference_type VARCHAR(30) NULL, -- 'ORDER', 'SUPPLIER_RESTOCK', 'ADMIN_ADJUSTMENT'
    reference_id INT NULL,           -- e.g. order_id
    remarks NVARCHAR(500) NULL,
    created_by INT NULL,             -- user_id of admin or NULL for system/customer order
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_inv_trans_product FOREIGN KEY (product_id) REFERENCES dbo.products(product_id),
    CONSTRAINT FK_inv_trans_user FOREIGN KEY (created_by) REFERENCES dbo.users(user_id)
);

-- ============================================================================
-- 5. SHOPPING CART
-- ============================================================================

IF OBJECT_ID('dbo.cart_items', 'U') IS NOT NULL DROP TABLE dbo.cart_items;
IF OBJECT_ID('dbo.carts', 'U') IS NOT NULL DROP TABLE dbo.carts;

CREATE TABLE dbo.carts (
    cart_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_carts_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) ON DELETE CASCADE
);

CREATE TABLE dbo.cart_items (
    cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT DEFAULT 1 NOT NULL CHECK (quantity > 0),
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT UQ_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT FK_cart_items_cart FOREIGN KEY (cart_id) REFERENCES dbo.carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT FK_cart_items_product FOREIGN KEY (product_id) REFERENCES dbo.products(product_id)
);

-- ============================================================================
-- 6. WISHLIST
-- ============================================================================

IF OBJECT_ID('dbo.wishlist_items', 'U') IS NOT NULL DROP TABLE dbo.wishlist_items;
IF OBJECT_ID('dbo.wishlists', 'U') IS NOT NULL DROP TABLE dbo.wishlists;

CREATE TABLE dbo.wishlists (
    wishlist_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_wishlists_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) ON DELETE CASCADE
);

CREATE TABLE dbo.wishlist_items (
    wishlist_item_id INT IDENTITY(1,1) PRIMARY KEY,
    wishlist_id INT NOT NULL,
    product_id INT NOT NULL,
    added_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT UQ_wishlist_product UNIQUE (wishlist_id, product_id),
    CONSTRAINT FK_wishlist_items_wishlist FOREIGN KEY (wishlist_id) REFERENCES dbo.wishlists(wishlist_id) ON DELETE CASCADE,
    CONSTRAINT FK_wishlist_items_product FOREIGN KEY (product_id) REFERENCES dbo.products(product_id)
);

-- ============================================================================
-- 7. COUPONS & PROMOTIONS
-- ============================================================================

IF OBJECT_ID('dbo.coupon_usage', 'U') IS NOT NULL DROP TABLE dbo.coupon_usage;
IF OBJECT_ID('dbo.coupons', 'U') IS NOT NULL DROP TABLE dbo.coupons;

CREATE TABLE dbo.coupons (
    coupon_id INT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    discount_value DECIMAL(18,2) NOT NULL CHECK (discount_value > 0),
    min_order_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL CHECK (min_order_amount >= 0),
    max_discount_amount DECIMAL(18,2) NULL, -- For percentage cap
    start_date DATETIME2 NOT NULL,
    end_date DATETIME2 NOT NULL,
    usage_limit INT DEFAULT 100 NOT NULL,
    current_usage INT DEFAULT 0 NOT NULL,
    is_active BIT DEFAULT 1 NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL
);

-- ============================================================================
-- 8. ORDERS & ORDER LINE ITEMS
-- ============================================================================

IF OBJECT_ID('dbo.order_status_history', 'U') IS NOT NULL DROP TABLE dbo.order_status_history;
IF OBJECT_ID('dbo.order_items', 'U') IS NOT NULL DROP TABLE dbo.order_items;
IF OBJECT_ID('dbo.payments', 'U') IS NOT NULL DROP TABLE dbo.payments;
IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL DROP TABLE dbo.orders;

CREATE TABLE dbo.orders (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    shipping_address_id INT NULL,
    order_status VARCHAR(30) DEFAULT 'PROCESSING' NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING' NOT NULL,
    payment_method VARCHAR(30) DEFAULT 'COD' NOT NULL,
    subtotal DECIMAL(18,2) NOT NULL CHECK (subtotal >= 0),
    discount_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL CHECK (discount_amount >= 0),
    tax_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL CHECK (tax_amount >= 0),
    shipping_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL CHECK (shipping_amount >= 0),
    total_amount DECIMAL(18,2) NOT NULL CHECK (total_amount >= 0),
    coupon_id INT NULL,
    shipping_full_name NVARCHAR(100) NULL,
    shipping_phone VARCHAR(20) NULL,
    shipping_address_line1 NVARCHAR(255) NULL,
    shipping_address_line2 NVARCHAR(255) NULL,
    shipping_city NVARCHAR(100) NULL,
    shipping_state NVARCHAR(100) NULL,
    shipping_postal_code VARCHAR(20) NULL,
    shipping_country NVARCHAR(100) DEFAULT 'India' NULL,
    billing_address_snapshot NVARCHAR(MAX) NULL,
    notes NVARCHAR(500) NULL,
    courier_partner VARCHAR(100) NULL,
    tracking_number VARCHAR(100) NULL,
    delivery_agent_phone VARCHAR(20) NULL,
    estimated_delivery_date DATETIME2 NULL,
    delivered_at DATETIME2 NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_orders_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);

-- Table: coupon_usage (Tracks which user redeemed which coupon on which order)
CREATE TABLE dbo.coupon_usage (
    usage_id INT IDENTITY(1,1) PRIMARY KEY,
    coupon_id INT NOT NULL,
    user_id INT NOT NULL,
    order_id INT NOT NULL,
    discount_applied DECIMAL(18,2) NOT NULL,
    used_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_coupon_usage_coupon FOREIGN KEY (coupon_id) REFERENCES dbo.coupons(coupon_id),
    CONSTRAINT FK_coupon_usage_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
    CONSTRAINT FK_coupon_usage_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id)
);

-- Table: order_items (Preserves historical snapshots of product metadata & price)
CREATE TABLE dbo.order_items (
    order_item_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name NVARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    discount_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL,
    tax_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    line_total DECIMAL(18,2) NOT NULL CHECK (line_total >= 0),
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_order_items_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id) ON DELETE CASCADE,
    CONSTRAINT FK_order_items_product FOREIGN KEY (product_id) REFERENCES dbo.products(product_id)
);

-- Table: order_status_history (Audit trail for order lifecycle transitions)
CREATE TABLE dbo.order_status_history (
    history_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    previous_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    remarks NVARCHAR(500) NULL,
    changed_by INT NULL, -- user_id of admin or customer or NULL for system
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_order_status_hist_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id) ON DELETE CASCADE,
    CONSTRAINT FK_order_status_hist_user FOREIGN KEY (changed_by) REFERENCES dbo.users(user_id)
);

-- ============================================================================
-- 9. PAYMENTS
-- ============================================================================

CREATE TABLE dbo.payments (
    payment_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method VARCHAR(30) NOT NULL CHECK (payment_method IN ('CASH_ON_DELIVERY', 'COD', 'ONLINE_PAYMENT', 'CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING')),
    transaction_reference VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(18,2) NOT NULL CHECK (amount >= 0),
    payment_status VARCHAR(20) DEFAULT 'PENDING' NOT NULL CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    gateway_response NVARCHAR(500) NULL,
    paid_at DATETIME2 NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_payments_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id)
);

-- ============================================================================
-- 10. PRODUCT REVIEWS & RATINGS
-- ============================================================================

IF OBJECT_ID('dbo.reviews', 'U') IS NOT NULL DROP TABLE dbo.reviews;

CREATE TABLE dbo.reviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_title NVARCHAR(150) NOT NULL,
    review_text NVARCHAR(2000) NOT NULL,
    image_url VARCHAR(1000) NULL,
    status VARCHAR(20) DEFAULT 'APPROVED' NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT UQ_user_product_review UNIQUE (product_id, user_id),
    CONSTRAINT FK_reviews_product FOREIGN KEY (product_id) REFERENCES dbo.products(product_id) ON DELETE CASCADE,
    CONSTRAINT FK_reviews_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);

IF OBJECT_ID('dbo.review_images', 'U') IS NOT NULL DROP TABLE dbo.review_images;

CREATE TABLE dbo.review_images (
    review_image_id INT IDENTITY(1,1) PRIMARY KEY,
    review_id INT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_review_images_review FOREIGN KEY (review_id) REFERENCES dbo.reviews(review_id) ON DELETE CASCADE
);

-- ============================================================================
-- 12. PAYMENT FAILURES & RECONCILIATION
-- ============================================================================

IF OBJECT_ID('dbo.payment_reconciliation', 'U') IS NOT NULL DROP TABLE dbo.payment_reconciliation;

CREATE TABLE dbo.payment_reconciliation (
    reconciliation_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL,
    gateway_order_id VARCHAR(100) NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL CHECK (amount >= 0),
    failure_reason NVARCHAR(500) NULL,
    gateway_response NVARCHAR(1000) NULL,
    reconciliation_status VARCHAR(50) DEFAULT 'PENDING' NOT NULL 
        CHECK (reconciliation_status IN ('PENDING', 'VERIFIED_DEBITED', 'REFUND_INITIATED', 'REFUND_COMPLETED', 'RESOLVED', 'MANUALLY_CREDITED', 'NOT_DEBITED')),
    admin_notes NVARCHAR(1000) NULL,
    resolved_by INT NULL,
    resolved_at DATETIME2 NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_pay_recon_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id),
    CONSTRAINT FK_pay_recon_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
    CONSTRAINT FK_pay_recon_admin FOREIGN KEY (resolved_by) REFERENCES dbo.users(user_id)
);

CREATE INDEX IX_pay_recon_order ON dbo.payment_reconciliation(order_id);
CREATE INDEX IX_pay_recon_user ON dbo.payment_reconciliation(user_id);
CREATE INDEX IX_pay_recon_status ON dbo.payment_reconciliation(reconciliation_status);
CREATE INDEX IX_pay_recon_txn ON dbo.payment_reconciliation(transaction_reference);

-- ============================================================================
-- 11. AUDIT LOGS
-- ============================================================================

IF OBJECT_ID('dbo.audit_logs', 'U') IS NOT NULL DROP TABLE dbo.audit_logs;

CREATE TABLE dbo.audit_logs (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NULL,
    entity_type VARCHAR(50) NOT NULL, -- 'PRODUCT', 'CATEGORY', 'ORDER', 'INVENTORY', 'USER'
    entity_id VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,      -- 'CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE'
    old_value NVARCHAR(MAX) NULL,
    new_value NVARCHAR(MAX) NULL,
    ip_address VARCHAR(45) NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_audit_logs_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);

-- ============================================================================
-- 12. PASSWORD RESET TOKENS
-- ============================================================================

IF OBJECT_ID('dbo.password_reset_tokens', 'U') IS NOT NULL DROP TABLE dbo.password_reset_tokens;

CREATE TABLE dbo.password_reset_tokens (
    token_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME2 NOT NULL,
    is_used BIT DEFAULT 0 NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_prt_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) ON DELETE CASCADE
);

-- ============================================================================
-- 13. ORDER RETURNS & REPLACEMENTS
-- ============================================================================

IF OBJECT_ID('dbo.order_returns', 'U') IS NOT NULL DROP TABLE dbo.order_returns;

CREATE TABLE dbo.order_returns (
    return_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    return_number VARCHAR(100) NOT NULL UNIQUE,
    return_reason NVARCHAR(200) NOT NULL,
    resolution_type VARCHAR(50) NOT NULL CHECK (resolution_type IN ('REFUND', 'REPLACEMENT', 'EXCHANGE')),
    comments NVARCHAR(1000) NULL,
    image_url VARCHAR(1000) NULL,
    return_status VARCHAR(50) DEFAULT 'REQUESTED' NOT NULL CHECK (return_status IN ('REQUESTED', 'APPROVED', 'PICKUP_SCHEDULED', 'ITEM_RECEIVED', 'REFUNDED', 'REPLACED', 'REJECTED')),
    refund_amount DECIMAL(18,2) NULL,
    admin_notes NVARCHAR(1000) NULL,
    pickup_date DATETIME2 NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    updated_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT FK_order_returns_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id),
    CONSTRAINT FK_order_returns_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);

-- ============================================================================
-- 13. PERFORMANCE & RELATIONAL INDEXES
-- ============================================================================

-- Users & Auth
CREATE NONCLUSTERED INDEX IX_users_email ON dbo.users(email);
CREATE NONCLUSTERED INDEX IX_users_status ON dbo.users(status);

-- Addresses
CREATE NONCLUSTERED INDEX IX_addresses_user ON dbo.addresses(user_id);

-- Products & Catalog Search
CREATE NONCLUSTERED INDEX IX_products_sku ON dbo.products(sku);
CREATE NONCLUSTERED INDEX IX_products_category ON dbo.products(category_id);
CREATE NONCLUSTERED INDEX IX_products_status ON dbo.products(status);
CREATE NONCLUSTERED INDEX IX_products_brand ON dbo.products(brand);
CREATE NONCLUSTERED INDEX IX_products_price ON dbo.products(price);
CREATE NONCLUSTERED INDEX IX_product_images_product ON dbo.product_images(product_id);

-- Inventory
CREATE NONCLUSTERED INDEX IX_inventory_product ON dbo.inventory(product_id);
CREATE NONCLUSTERED INDEX IX_inv_trans_product ON dbo.inventory_transactions(product_id);
CREATE NONCLUSTERED INDEX IX_inv_trans_user ON dbo.inventory_transactions(created_by);

-- Cart & Wishlist
CREATE NONCLUSTERED INDEX IX_cart_items_cart ON dbo.cart_items(cart_id);
CREATE NONCLUSTERED INDEX IX_cart_items_product ON dbo.cart_items(product_id);
CREATE NONCLUSTERED INDEX IX_wishlist_items_wishlist ON dbo.wishlist_items(wishlist_id);
CREATE NONCLUSTERED INDEX IX_wishlist_items_product ON dbo.wishlist_items(product_id);

-- Orders & Line Items
CREATE NONCLUSTERED INDEX IX_orders_user ON dbo.orders(user_id);
CREATE NONCLUSTERED INDEX IX_orders_order_number ON dbo.orders(order_number);
CREATE NONCLUSTERED INDEX IX_orders_status ON dbo.orders(order_status);
CREATE NONCLUSTERED INDEX IX_orders_created ON dbo.orders(created_at DESC);
CREATE NONCLUSTERED INDEX IX_order_items_order ON dbo.order_items(order_id);
CREATE NONCLUSTERED INDEX IX_order_items_product ON dbo.order_items(product_id);
CREATE NONCLUSTERED INDEX IX_order_status_hist_order ON dbo.order_status_history(order_id);

-- Payments
CREATE NONCLUSTERED INDEX IX_payments_order ON dbo.payments(order_id);
CREATE NONCLUSTERED INDEX IX_payments_txn_ref ON dbo.payments(transaction_reference);

-- Reviews
CREATE NONCLUSTERED INDEX IX_reviews_product ON dbo.reviews(product_id);
CREATE NONCLUSTERED INDEX IX_reviews_user ON dbo.reviews(user_id);

-- Audit
CREATE NONCLUSTERED INDEX IX_audit_logs_user ON dbo.audit_logs(user_id);
CREATE NONCLUSTERED INDEX IX_audit_created ON dbo.audit_logs(created_at DESC);

-- Password Reset Tokens
CREATE NONCLUSTERED INDEX IX_prt_token ON dbo.password_reset_tokens(token);
CREATE NONCLUSTERED INDEX IX_prt_user ON dbo.password_reset_tokens(user_id);
GO
