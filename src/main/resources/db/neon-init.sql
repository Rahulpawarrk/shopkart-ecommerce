-- ============================================================================
-- NEON.TECH POSTGRESQL PRODUCTION INITIALIZATION & SEED SCRIPT
-- Project: ShopKart E-Commerce Web Application (Tomcat 11 + Neon Cloud Postgres)
-- ============================================================================

-- 1. USERS & ROLES
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS review_images CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS payment_reconciliation CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS order_returns CASCADE;
DROP TABLE IF EXISTS order_status_history CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS coupon_usage CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS coupons CASCADE;
DROP TABLE IF EXISTS wishlist_items CASCADE;
DROP TABLE IF EXISTS wishlists CASCADE;
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS carts CASCADE;
DROP TABLE IF EXISTS inventory_transactions CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;
DROP TABLE IF EXISTS product_images CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS addresses CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE user_roles (
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE addresses (
    address_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    address_type VARCHAR(20) DEFAULT 'SHIPPING' NOT NULL CHECK (address_type IN ('SHIPPING', 'BILLING', 'BOTH')),
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255) NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) DEFAULT 'India' NOT NULL,
    is_default BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. CATEGORIES & PRODUCTS
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    parent_category_id INT NULL REFERENCES categories(category_id),
    category_name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500) NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    category_id INT NOT NULL REFERENCES categories(category_id),
    sku VARCHAR(100) NOT NULL UNIQUE,
    product_name VARCHAR(255) NOT NULL,
    slug VARCHAR(300) NOT NULL UNIQUE,
    description TEXT NULL,
    brand VARCHAR(100) NULL,
    price DECIMAL(18,2) NOT NULL CHECK (price >= 0),
    discount_percentage DECIMAL(5,2) DEFAULT 0.00 NOT NULL CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    tax_percentage DECIMAL(5,2) DEFAULT 0.00 NOT NULL CHECK (tax_percentage >= 0),
    weight_kg DECIMAL(8,3) DEFAULT 0.000 NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE product_images (
    image_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    alt_text VARCHAR(255) NULL,
    display_order INT DEFAULT 0 NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. INVENTORY
CREATE TABLE inventory (
    inventory_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL UNIQUE REFERENCES products(product_id) ON DELETE CASCADE,
    quantity INT DEFAULT 0 NOT NULL CHECK (quantity >= 0),
    low_stock_threshold INT DEFAULT 5 NOT NULL CHECK (low_stock_threshold >= 0),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE inventory_transactions (
    transaction_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id),
    previous_stock INT NOT NULL CHECK (previous_stock >= 0),
    quantity_changed INT NOT NULL,
    new_stock INT NOT NULL CHECK (new_stock >= 0),
    transaction_type VARCHAR(30) NOT NULL,
    reference_type VARCHAR(30) NULL,
    reference_id INT NULL,
    remarks VARCHAR(500) NULL,
    created_by INT NULL REFERENCES users(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 4. CARTS & WISHLISTS
CREATE TABLE carts (
    cart_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE cart_items (
    cart_item_id SERIAL PRIMARY KEY,
    cart_id INT NOT NULL REFERENCES carts(cart_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id),
    quantity INT DEFAULT 1 NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT UQ_cart_product UNIQUE (cart_id, product_id)
);

CREATE TABLE wishlists (
    wishlist_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE wishlist_items (
    wishlist_item_id SERIAL PRIMARY KEY,
    wishlist_id INT NOT NULL REFERENCES wishlists(wishlist_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT UQ_wishlist_product UNIQUE (wishlist_id, product_id)
);

-- 5. COUPONS
CREATE TABLE coupons (
    coupon_id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    discount_value DECIMAL(18,2) NOT NULL CHECK (discount_value > 0),
    min_order_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL CHECK (min_order_amount >= 0),
    max_discount_amount DECIMAL(18,2) NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    usage_limit INT DEFAULT 100 NOT NULL,
    current_usage INT DEFAULT 0 NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 6. ORDERS & PAYMENTS
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id INT NOT NULL REFERENCES users(user_id),
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
    shipping_full_name VARCHAR(100) NULL,
    shipping_phone VARCHAR(20) NULL,
    shipping_address_line1 VARCHAR(255) NULL,
    shipping_address_line2 VARCHAR(255) NULL,
    shipping_city VARCHAR(100) NULL,
    shipping_state VARCHAR(100) NULL,
    shipping_postal_code VARCHAR(20) NULL,
    shipping_country VARCHAR(100) DEFAULT 'India' NULL,
    billing_address_snapshot TEXT NULL,
    notes VARCHAR(500) NULL,
    courier_partner VARCHAR(100) NULL,
    tracking_number VARCHAR(100) NULL,
    delivery_agent_phone VARCHAR(20) NULL,
    estimated_delivery_date TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE coupon_usage (
    usage_id SERIAL PRIMARY KEY,
    coupon_id INT NOT NULL REFERENCES coupons(coupon_id),
    user_id INT NOT NULL REFERENCES users(user_id),
    order_id INT NOT NULL REFERENCES orders(order_id),
    discount_applied DECIMAL(18,2) NOT NULL,
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE order_items (
    order_item_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id),
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    discount_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL,
    tax_amount DECIMAL(18,2) DEFAULT 0.00 NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    line_total DECIMAL(18,2) NOT NULL CHECK (line_total >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE order_status_history (
    history_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    previous_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    remarks VARCHAR(500) NULL,
    changed_by INT NULL REFERENCES users(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE payments (
    payment_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id),
    payment_method VARCHAR(30) NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(18,2) NOT NULL CHECK (amount >= 0),
    payment_status VARCHAR(20) DEFAULT 'PENDING' NOT NULL,
    gateway_response VARCHAR(500) NULL,
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE payment_reconciliation (
    reconciliation_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id),
    user_id INT NOT NULL REFERENCES users(user_id),
    transaction_reference VARCHAR(100) NOT NULL,
    gateway_order_id VARCHAR(100) NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL CHECK (amount >= 0),
    failure_reason VARCHAR(500) NULL,
    gateway_response VARCHAR(1000) NULL,
    reconciliation_status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    admin_notes VARCHAR(1000) NULL,
    resolved_by INT NULL REFERENCES users(user_id),
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE reviews (
    review_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(user_id),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_title VARCHAR(150) NOT NULL,
    review_text TEXT NOT NULL,
    image_url VARCHAR(1000) NULL,
    status VARCHAR(20) DEFAULT 'APPROVED' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT UQ_user_product_review UNIQUE (product_id, user_id)
);

CREATE TABLE review_images (
    review_image_id SERIAL PRIMARY KEY,
    review_id INT NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE password_reset_tokens (
    token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE audit_logs (
    log_id SERIAL PRIMARY KEY,
    user_id INT NULL REFERENCES users(user_id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE order_returns (
    return_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id),
    user_id INT NOT NULL REFERENCES users(user_id),
    return_number VARCHAR(100) NOT NULL UNIQUE,
    return_reason VARCHAR(200) NOT NULL,
    resolution_type VARCHAR(50) NOT NULL,
    comments VARCHAR(1000) NULL,
    image_url VARCHAR(1000) NULL,
    return_status VARCHAR(50) DEFAULT 'REQUESTED' NOT NULL,
    refund_amount DECIMAL(18,2) NULL,
    admin_notes VARCHAR(1000) NULL,
    pickup_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================================================
-- SEED DATA (ROLES, USERS, CATEGORIES, PRODUCTS, INVENTORY, COUPONS)
-- ============================================================================

-- Roles
INSERT INTO roles (role_name, description) VALUES 
('ADMIN', 'System Administrator with full access to management consoles'),
('CUSTOMER', 'Registered Customer with ordering and account capabilities');

-- Users (BCrypt hash for 'Admin@123' and 'Customer@123')
INSERT INTO users (email, password_hash, first_name, last_name, phone, status) VALUES 
('admin@ecommerce.com', '$2a$10$wN9a.2ZqG0qfT.B6.FkKje0T4q7W.Ue8p9l6J7f6C2N6A9R6t.2Gy', 'System', 'Admin', '9876543210', 'ACTIVE'),
('customer@ecommerce.com', '$2a$10$wN9a.2ZqG0qfT.B6.FkKje0T4q7W.Ue8p9l6J7f6C2N6A9R6t.2Gy', 'Pooja', 'Pawar', '9876543211', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1), (2, 2);

-- Categories
INSERT INTO categories (category_name, slug, description, is_active) VALUES 
('Laptops & Computers', 'laptops-computers', 'High performance ultrabooks, workstations, and gaming machines', true),
('Smartphones & Tablets', 'smartphones-tablets', 'Next-gen flagship 5G mobile devices and multimedia tablets', true),
('Audio & Headphones', 'audio-headphones', 'Noise cancelling headphones, studio monitors, and wireless earbuds', true),
('Smartwatches & Wearables', 'smartwatches-wearables', 'Fitness trackers, AMOLED smartwatches, and smart rings', true),
('Men Fashion', 'men-fashion', 'Designer menswear, formal shirts, suits, and casual streetwear', true),
('Women Fashion', 'women-fashion', 'Haute couture, ethnic sarees, evening gowns, and winter jackets', true),
('Footwear & Sneakers', 'footwear-sneakers', 'Running sneakers, formal leather shoes, and daily footwear', true),
('Home & Kitchen', 'home-kitchen', 'Smart appliances, espresso machines, and ergonomic cookware', true);

-- Products
INSERT INTO products (category_id, sku, product_name, slug, description, brand, price, discount_percentage, tax_percentage, weight_kg, status) VALUES 
(1, 'LAP-MBP-M3M-001', 'Apple MacBook Pro 16" (M3 Max, 36GB, 1TB SSD) - Space Black', 'apple-macbook-pro-16-m3-max-space-black', 'Apple M3 Max 16-core CPU, 40-core GPU, 36GB Unified Memory, 1TB SSD Storage. Liquid Retina XDR 120Hz display with 1600 nits peak brightness.', 'Apple', 349900.00, 8.00, 18.00, 2.140, 'ACTIVE'),
(1, 'LAP-DELL-XPS15-002', 'Dell XPS 15 9530 OLED (i9-13900H, RTX 4070, 32GB, 1TB)', 'dell-xps-15-oled-i9-rtx4070', '15.6" 3.5K OLED InfinityEdge touch display, Intel Core i9-13900H, NVIDIA GeForce RTX 4070 8GB, 32GB DDR5 RAM, 1TB NVMe Gen4 SSD.', 'Dell', 264990.00, 12.00, 18.00, 1.920, 'ACTIVE'),
(2, 'MOB-SAM-S24U-001', 'Samsung Galaxy S24 Ultra 5G (Titanium Gray, 12GB+512GB)', 'samsung-galaxy-s24-ultra-5g-titanium-gray', 'Snapdragon 8 Gen 3 for Galaxy, 200MP Quad Telephoto Camera with AI Zoom, Built-in S-Pen, 6.8" QHD+ Dynamic AMOLED 2X 2600 nits, 5000mAh Battery.', 'Samsung', 139999.00, 10.00, 18.00, 0.232, 'ACTIVE'),
(2, 'MOB-APL-IP15PM-002', 'Apple iPhone 15 Pro Max (Natural Titanium, 256GB)', 'apple-iphone-15-pro-max-natural-titanium-256gb', 'Aerospace-grade titanium design, A17 Pro Chip with 6-core GPU, 48MP main camera with 5x optical zoom telephoto lens, Action Button, USB-C 3.0.', 'Apple', 159900.00, 5.00, 18.00, 0.221, 'ACTIVE'),
(3, 'AUD-SNY-WH1000XM5-001', 'Sony WH-1000XM5 Wireless Noise Cancelling Headphones - Silver', 'sony-wh-1000xm5-wireless-noise-cancelling-silver', 'Industry-leading noise cancellation with two processors and 8 microphones. Hi-Res Audio Wireless LDAC, 30-hour battery life, Multipoint connection.', 'Sony', 29990.00, 17.00, 18.00, 0.250, 'ACTIVE'),
(3, 'AUD-BSE-QCULT-002', 'Bose QuietComfort Ultra Wireless Earbuds with Spatial Audio', 'bose-quietcomfort-ultra-earbuds-black', 'Breakthrough spatialized audio for more immersive listening. CustomTune technology shapes sound specifically to your ear canal. IPX4 water resistance.', 'Bose', 25900.00, 15.00, 18.00, 0.058, 'ACTIVE'),
(4, 'WAT-APL-AWU2-001', 'Apple Watch Ultra 2 (GPS + Cellular, 49mm Titanium, Ocean Band)', 'apple-watch-ultra-2-49mm-titanium-ocean-band', 'Rugged 49mm aerospace titanium case, 3000-nit display, Precision Dual-Frequency GPS, 36-hour battery life, 100m water resistance with depth gauge.', 'Apple', 89900.00, 7.00, 18.00, 0.061, 'ACTIVE'),
(5, 'FAS-MEN-RL-BLAZER-001', 'Ralph Lauren Italian Wool Slim-Fit Navy Blazer', 'ralph-lauren-italian-wool-navy-blazer', 'Crafted in Italy from premium virgin wool. Notched lapels, two-button silhouette, gold-tone crest buttons, double vent back.', 'Ralph Lauren', 45000.00, 20.00, 12.00, 0.850, 'ACTIVE'),
(6, 'FAS-WOM-SL-DRESS-001', 'Self-Portrait Pleated Chiffon & Lace Maxi Evening Dress', 'self-portrait-pleated-chiffon-lace-maxi-dress', 'Delicate floral lace bodice with crystal embellishments and fluid pleated chiffon skirt. Concealed zip fastening, fully lined.', 'Self-Portrait', 38500.00, 15.00, 12.00, 0.650, 'ACTIVE'),
(8, 'KIT-BRV-BARISTA-001', 'Breville Barista Touch Impress Espresso Machine - Brushed Steel', 'breville-barista-touch-impress-espresso-machine', 'Automated touch screen espresso maker with assisted tamping, precision temperature extraction, and automatic microfoam milk texturing.', 'Breville', 115000.00, 10.00, 18.00, 10.500, 'ACTIVE');

-- Product Images
INSERT INTO product_images (product_id, image_url, alt_text, display_order, is_primary) VALUES 
(1, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80', 'Apple MacBook Pro 16 Space Black', 1, true),
(2, 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=800&q=80', 'Dell XPS 15 OLED Laptop', 1, true),
(3, 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=800&q=80', 'Samsung Galaxy S24 Ultra Titanium Gray', 1, true),
(4, 'https://images.unsplash.com/photo-1591337676887-a217a6970a8a?auto=format&fit=crop&w=800&q=80', 'Apple iPhone 15 Pro Max Natural Titanium', 1, true),
(5, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80', 'Sony WH-1000XM5 Silver Headphones', 1, true),
(6, 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=800&q=80', 'Bose QuietComfort Ultra Earbuds', 1, true),
(7, 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?auto=format&fit=crop&w=800&q=80', 'Apple Watch Ultra 2 Titanium', 1, true),
(8, 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80', 'Ralph Lauren Navy Blazer', 1, true),
(9, 'https://images.unsplash.com/photo-1539109136881-3be0616acf4b?auto=format&fit=crop&w=800&q=80', 'Self-Portrait Evening Dress', 1, true),
(10, 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=800&q=80', 'Breville Barista Espresso Machine', 1, true);

-- Inventory
INSERT INTO inventory (product_id, quantity, low_stock_threshold) VALUES 
(1, 25, 5),
(2, 18, 4),
(3, 45, 10),
(4, 50, 10),
(5, 60, 12),
(6, 40, 8),
(7, 30, 6),
(8, 20, 5),
(9, 15, 3),
(10, 12, 3);

-- Coupons
INSERT INTO coupons (code, discount_type, discount_value, min_order_amount, max_discount_amount, start_date, end_date, usage_limit, current_usage, is_active) VALUES 
('WELCOME10', 'PERCENTAGE', 10.00, 500.00, 1500.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '365 days', 1000, 0, true),
('SAVE20', 'PERCENTAGE', 20.00, 2000.00, 5000.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '180 days', 500, 0, true),
('FESTIVE50', 'FIXED_AMOUNT', 500.00, 2500.00, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '90 days', 300, 0, true);
