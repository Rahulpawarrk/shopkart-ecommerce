-- ============================================================================
-- NEON POSTGRESQL 100% COMPATIBILITY SETUP FOR SHOPKART E-COMMERCE
-- Creates 'dbo' schema and compatibility functions so all DAOs run natively!
-- ============================================================================

-- 1. Create 'dbo' schema
CREATE SCHEMA IF NOT EXISTS dbo;
SET search_path TO dbo, public;

-- 2. T-SQL to PostgreSQL Compatibility Functions
CREATE OR REPLACE FUNCTION dbo.isnull(val anyelement, fallback anyelement) 
RETURNS anyelement AS $$
BEGIN
    RETURN COALESCE(val, fallback);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION public.isnull(val anyelement, fallback anyelement) 
RETURNS anyelement AS $$
BEGIN
    RETURN COALESCE(val, fallback);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION dbo.getdate() RETURNS timestamp AS $$
BEGIN
    RETURN CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION public.getdate() RETURNS timestamp AS $$
BEGIN
    RETURN CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- 3. DROP AND CREATE TABLES IN DBO SCHEMA
DROP TABLE IF EXISTS dbo.user_roles, dbo.roles, dbo.addresses, dbo.password_reset_tokens, dbo.audit_logs, dbo.review_images, dbo.reviews, dbo.payment_reconciliation, dbo.payments, dbo.order_returns, dbo.order_status_history, dbo.order_items, dbo.coupon_usage, dbo.orders, dbo.coupons, dbo.wishlist_items, dbo.wishlists, dbo.cart_items, dbo.carts, dbo.inventory_transactions, dbo.inventory, dbo.product_images, dbo.products, dbo.categories, dbo.users CASCADE;

CREATE TABLE dbo.roles (role_id SERIAL PRIMARY KEY, role_name VARCHAR(50) NOT NULL UNIQUE, description VARCHAR(255) NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.users (user_id SERIAL PRIMARY KEY, email VARCHAR(255) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, phone VARCHAR(20) NULL, status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.user_roles (user_id INT NOT NULL REFERENCES dbo.users(user_id) ON DELETE CASCADE, role_id INT NOT NULL REFERENCES dbo.roles(role_id) ON DELETE CASCADE, assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, PRIMARY KEY (user_id, role_id));
CREATE TABLE dbo.addresses (address_id SERIAL PRIMARY KEY, user_id INT NOT NULL REFERENCES dbo.users(user_id) ON DELETE CASCADE, address_type VARCHAR(20) DEFAULT 'SHIPPING' NOT NULL, full_name VARCHAR(150) NOT NULL, phone VARCHAR(20) NOT NULL, address_line1 VARCHAR(255) NOT NULL, address_line2 VARCHAR(255) NULL, city VARCHAR(100) NOT NULL, state VARCHAR(100) NOT NULL, postal_code VARCHAR(20) NOT NULL, country VARCHAR(100) DEFAULT 'India' NOT NULL, is_default BOOLEAN DEFAULT FALSE NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);

CREATE TABLE dbo.categories (category_id SERIAL PRIMARY KEY, parent_category_id INT NULL REFERENCES dbo.categories(category_id), category_name VARCHAR(100) NOT NULL, slug VARCHAR(120) NOT NULL UNIQUE, description VARCHAR(500) NULL, is_active BOOLEAN DEFAULT TRUE NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.products (product_id SERIAL PRIMARY KEY, category_id INT NOT NULL REFERENCES dbo.categories(category_id), sku VARCHAR(100) NOT NULL UNIQUE, product_name VARCHAR(255) NOT NULL, slug VARCHAR(300) NOT NULL UNIQUE, description TEXT NULL, brand VARCHAR(100) NULL, price DECIMAL(18,2) NOT NULL, discount_percentage DECIMAL(5,2) DEFAULT 0.00 NOT NULL, tax_percentage DECIMAL(5,2) DEFAULT 0.00 NOT NULL, weight_kg DECIMAL(8,3) DEFAULT 0.000 NULL, status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.product_images (image_id SERIAL PRIMARY KEY, product_id INT NOT NULL REFERENCES dbo.products(product_id) ON DELETE CASCADE, image_url VARCHAR(1000) NOT NULL, alt_text VARCHAR(255) NULL, display_order INT DEFAULT 0 NOT NULL, is_primary BOOLEAN DEFAULT FALSE NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.inventory (inventory_id SERIAL PRIMARY KEY, product_id INT NOT NULL UNIQUE REFERENCES dbo.products(product_id) ON DELETE CASCADE, quantity INT DEFAULT 0 NOT NULL, low_stock_threshold INT DEFAULT 5 NOT NULL, last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.inventory_transactions (transaction_id SERIAL PRIMARY KEY, product_id INT NOT NULL REFERENCES dbo.products(product_id), previous_stock INT NOT NULL, quantity_changed INT NOT NULL, new_stock INT NOT NULL, transaction_type VARCHAR(30) NOT NULL, reference_type VARCHAR(30) NULL, reference_id INT NULL, remarks VARCHAR(500) NULL, created_by INT NULL REFERENCES dbo.users(user_id), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);

CREATE TABLE dbo.carts (cart_id SERIAL PRIMARY KEY, user_id INT NOT NULL UNIQUE REFERENCES dbo.users(user_id) ON DELETE CASCADE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.cart_items (cart_item_id SERIAL PRIMARY KEY, cart_id INT NOT NULL REFERENCES dbo.carts(cart_id) ON DELETE CASCADE, product_id INT NOT NULL REFERENCES dbo.products(product_id), quantity INT DEFAULT 1 NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, CONSTRAINT UQ_cart_product UNIQUE (cart_id, product_id));
CREATE TABLE dbo.wishlists (wishlist_id SERIAL PRIMARY KEY, user_id INT NOT NULL UNIQUE REFERENCES dbo.users(user_id) ON DELETE CASCADE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.wishlist_items (wishlist_item_id SERIAL PRIMARY KEY, wishlist_id INT NOT NULL REFERENCES dbo.wishlists(wishlist_id) ON DELETE CASCADE, product_id INT NOT NULL REFERENCES dbo.products(product_id), added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, CONSTRAINT UQ_wishlist_product UNIQUE (wishlist_id, product_id));
CREATE TABLE dbo.coupons (coupon_id SERIAL PRIMARY KEY, code VARCHAR(50) NOT NULL UNIQUE, discount_type VARCHAR(20) NOT NULL, discount_value DECIMAL(18,2) NOT NULL, min_order_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL, max_discount_amount DECIMAL(18,2) NULL, start_date TIMESTAMP NOT NULL, end_date TIMESTAMP NOT NULL, usage_limit INT DEFAULT 100 NOT NULL, current_usage INT DEFAULT 0 NOT NULL, is_active BOOLEAN DEFAULT TRUE NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);

CREATE TABLE dbo.orders (order_id SERIAL PRIMARY KEY, order_number VARCHAR(50) NOT NULL UNIQUE, user_id INT NOT NULL REFERENCES dbo.users(user_id), shipping_address_id INT NULL, order_status VARCHAR(30) DEFAULT 'PROCESSING' NOT NULL, payment_status VARCHAR(20) DEFAULT 'PENDING' NOT NULL, payment_method VARCHAR(30) DEFAULT 'COD' NOT NULL, subtotal DECIMAL(18,2) NOT NULL, discount_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL, tax_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL, shipping_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL, total_amount DECIMAL(18,2) NOT NULL, coupon_id INT NULL, shipping_full_name VARCHAR(100) NULL, shipping_phone VARCHAR(20) NULL, shipping_address_line1 VARCHAR(255) NULL, shipping_address_line2 VARCHAR(255) NULL, shipping_city VARCHAR(100) NULL, shipping_state VARCHAR(100) NULL, shipping_postal_code VARCHAR(20) NULL, shipping_country VARCHAR(100) DEFAULT 'India' NULL, billing_address_snapshot TEXT NULL, notes VARCHAR(500) NULL, courier_partner VARCHAR(100) NULL, tracking_number VARCHAR(100) NULL, delivery_agent_phone VARCHAR(20) NULL, estimated_delivery_date TIMESTAMP NULL, delivered_at TIMESTAMP NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.coupon_usage (usage_id SERIAL PRIMARY KEY, coupon_id INT NOT NULL REFERENCES dbo.coupons(coupon_id), user_id INT NOT NULL REFERENCES dbo.users(user_id), order_id INT NOT NULL REFERENCES dbo.orders(order_id), discount_applied DECIMAL(18,2) NOT NULL, used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.order_items (order_item_id SERIAL PRIMARY KEY, order_id INT NOT NULL REFERENCES dbo.orders(order_id) ON DELETE CASCADE, product_id INT NOT NULL REFERENCES dbo.products(product_id), product_name VARCHAR(255) NOT NULL, sku VARCHAR(100) NOT NULL, unit_price DECIMAL(18,2) NOT NULL, discount_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL, tax_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL, quantity INT NOT NULL, line_total DECIMAL(18,2) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.order_status_history (history_id SERIAL PRIMARY KEY, order_id INT NOT NULL REFERENCES dbo.orders(order_id) ON DELETE CASCADE, previous_status VARCHAR(30) NULL, new_status VARCHAR(30) NOT NULL, remarks VARCHAR(500) NULL, changed_by INT NULL REFERENCES dbo.users(user_id), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.payments (payment_id SERIAL PRIMARY KEY, order_id INT NOT NULL REFERENCES dbo.orders(order_id), payment_method VARCHAR(30) NOT NULL, transaction_reference VARCHAR(100) NOT NULL UNIQUE, amount DECIMAL(18,2) NOT NULL, payment_status VARCHAR(20) DEFAULT 'PENDING' NOT NULL, gateway_response VARCHAR(500) NULL, paid_at TIMESTAMP NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.payment_reconciliation (reconciliation_id SERIAL PRIMARY KEY, order_id INT NOT NULL REFERENCES dbo.orders(order_id), user_id INT NOT NULL REFERENCES dbo.users(user_id), transaction_reference VARCHAR(100) NOT NULL, gateway_order_id VARCHAR(100) NULL, payment_method VARCHAR(50) NOT NULL, amount DECIMAL(18,2) NOT NULL, failure_reason VARCHAR(500) NULL, gateway_response VARCHAR(1000) NULL, reconciliation_status VARCHAR(50) DEFAULT 'PENDING' NOT NULL, admin_notes VARCHAR(1000) NULL, resolved_by INT NULL REFERENCES dbo.users(user_id), resolved_at TIMESTAMP NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.reviews (review_id SERIAL PRIMARY KEY, product_id INT NOT NULL REFERENCES dbo.products(product_id) ON DELETE CASCADE, user_id INT NOT NULL REFERENCES dbo.users(user_id), rating INT NOT NULL, review_title VARCHAR(150) NOT NULL, review_text TEXT NOT NULL, image_url VARCHAR(1000) NULL, status VARCHAR(20) DEFAULT 'APPROVED' NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, CONSTRAINT UQ_user_product_review UNIQUE (product_id, user_id));
CREATE TABLE dbo.review_images (review_image_id SERIAL PRIMARY KEY, review_id INT NOT NULL REFERENCES dbo.reviews(review_id) ON DELETE CASCADE, image_url VARCHAR(1000) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.password_reset_tokens (token_id SERIAL PRIMARY KEY, user_id INT NOT NULL REFERENCES dbo.users(user_id) ON DELETE CASCADE, token VARCHAR(64) NOT NULL UNIQUE, expires_at TIMESTAMP NOT NULL, is_used BOOLEAN DEFAULT FALSE NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.audit_logs (log_id SERIAL PRIMARY KEY, user_id INT NULL REFERENCES dbo.users(user_id), entity_type VARCHAR(50) NOT NULL, entity_id VARCHAR(50) NOT NULL, action VARCHAR(50) NOT NULL, old_value TEXT NULL, new_value TEXT NULL, ip_address VARCHAR(45) NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);
CREATE TABLE dbo.order_returns (return_id SERIAL PRIMARY KEY, order_id INT NOT NULL REFERENCES dbo.orders(order_id), user_id INT NOT NULL REFERENCES dbo.users(user_id), return_number VARCHAR(100) NOT NULL UNIQUE, return_reason VARCHAR(200) NOT NULL, resolution_type VARCHAR(50) NOT NULL, comments VARCHAR(1000) NULL, image_url VARCHAR(1000) NULL, return_status VARCHAR(50) DEFAULT 'REQUESTED' NOT NULL, refund_amount DECIMAL(18,2) NULL, admin_notes VARCHAR(1000) NULL, pickup_date TIMESTAMP NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);

-- 4. SEED DATA
INSERT INTO dbo.roles (role_name, description) VALUES ('ADMIN', 'Administrator with full console privileges'), ('CUSTOMER', 'Shopper account');
INSERT INTO dbo.users (email, password_hash, first_name, last_name, phone, status) VALUES 
('admin@ecommerce.com', '$2a$10$wN9a.2ZqG0qfT.B6.FkKje0T4q7W.Ue8p9l6J7f6C2N6A9R6t.2Gy', 'System', 'Admin', '9876543210', 'ACTIVE'),
('customer@ecommerce.com', '$2a$10$wN9a.2ZqG0qfT.B6.FkKje0T4q7W.Ue8p9l6J7f6C2N6A9R6t.2Gy', 'Pooja', 'Pawar', '9876543211', 'ACTIVE');
INSERT INTO dbo.user_roles (user_id, role_id) VALUES (1, 1), (2, 2);

INSERT INTO dbo.categories (category_name, slug, description, is_active) VALUES 
('Laptops & Computers', 'laptops-computers', 'High performance ultrabooks, workstations, and gaming machines', true),
('Smartphones & Tablets', 'smartphones-tablets', 'Next-gen flagship 5G mobile devices and multimedia tablets', true),
('Audio & Headphones', 'audio-headphones', 'Noise cancelling headphones, studio monitors, and wireless earbuds', true),
('Smartwatches & Wearables', 'smartwatches-wearables', 'Fitness trackers, AMOLED smartwatches, and smart rings', true),
('Men Fashion', 'men-fashion', 'Designer menswear, formal shirts, suits, and casual streetwear', true),
('Women Fashion', 'women-fashion', 'Haute couture, ethnic sarees, evening gowns, and winter jackets', true),
('Footwear & Sneakers', 'footwear-sneakers', 'Running sneakers, formal leather shoes, and daily footwear', true),
('Home & Kitchen', 'home-kitchen', 'Smart appliances, espresso machines, and ergonomic cookware', true);

INSERT INTO dbo.products (category_id, sku, product_name, slug, description, brand, price, discount_percentage, tax_percentage, weight_kg, status) VALUES 
(1, 'LAP-MBP-M3M-001', 'Apple MacBook Pro 16" (M3 Max, 36GB, 1TB SSD) - Space Black', 'apple-macbook-pro-16-m3-max-space-black', 'Apple M3 Max 16-core CPU, 40-core GPU, 36GB Unified Memory, 1TB SSD Storage.', 'Apple', 349900.00, 8.00, 18.00, 2.140, 'ACTIVE'),
(1, 'LAP-DELL-XPS15-002', 'Dell XPS 15 9530 OLED (i9-13900H, RTX 4070, 32GB, 1TB)', 'dell-xps-15-oled-i9-rtx4070', '15.6" 3.5K OLED InfinityEdge touch display, Intel Core i9, RTX 4070.', 'Dell', 264990.00, 12.00, 18.00, 1.920, 'ACTIVE'),
(2, 'MOB-SAM-S24U-001', 'Samsung Galaxy S24 Ultra 5G (Titanium Gray, 12GB+512GB)', 'samsung-galaxy-s24-ultra-5g-titanium-gray', 'Snapdragon 8 Gen 3, 200MP Quad Telephoto Camera, S-Pen.', 'Samsung', 139999.00, 10.00, 18.00, 0.232, 'ACTIVE'),
(2, 'MOB-APL-IP15PM-002', 'Apple iPhone 15 Pro Max (Natural Titanium, 256GB)', 'apple-iphone-15-pro-max-natural-titanium-256gb', 'Aerospace-grade titanium, A17 Pro Chip, 48MP main camera.', 'Apple', 159900.00, 5.00, 18.00, 0.221, 'ACTIVE'),
(3, 'AUD-SNY-WH1000XM5-001', 'Sony WH-1000XM5 Wireless Noise Cancelling Headphones - Silver', 'sony-wh-1000xm5-wireless-noise-cancelling-silver', 'Industry-leading noise cancellation, 30-hour battery life.', 'Sony', 29990.00, 17.00, 18.00, 0.250, 'ACTIVE'),
(4, 'WAT-APL-AWU2-001', 'Apple Watch Ultra 2 (GPS + Cellular, 49mm Titanium)', 'apple-watch-ultra-2-49mm-titanium-ocean-band', 'Rugged 49mm titanium case, 3000-nit display, Precision GPS.', 'Apple', 89900.00, 7.00, 18.00, 0.061, 'ACTIVE');

INSERT INTO dbo.product_images (product_id, image_url, alt_text, display_order, is_primary) VALUES 
(1, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80', 'MacBook Pro', 1, true),
(2, 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=800&q=80', 'Dell XPS 15', 1, true),
(3, 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=800&q=80', 'Samsung S24 Ultra', 1, true),
(4, 'https://images.unsplash.com/photo-1591337676887-a217a6970a8a?auto=format&fit=crop&w=800&q=80', 'iPhone 15 Pro Max', 1, true),
(5, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80', 'Sony Headphones', 1, true),
(6, 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?auto=format&fit=crop&w=800&q=80', 'Apple Watch Ultra 2', 1, true);

INSERT INTO dbo.inventory (product_id, quantity, low_stock_threshold) VALUES (1, 25, 5), (2, 18, 4), (3, 45, 10), (4, 50, 10), (5, 60, 12), (6, 30, 6);
INSERT INTO dbo.coupons (code, discount_type, discount_value, min_order_amount, max_discount_amount, start_date, end_date, usage_limit, current_usage, is_active) VALUES 
('WELCOME10', 'PERCENTAGE', 10.00, 500.00, 1500.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '365 days', 1000, 0, true),
('SAVE20', 'PERCENTAGE', 20.00, 2000.00, 5000.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '180 days', 500, 0, true);
