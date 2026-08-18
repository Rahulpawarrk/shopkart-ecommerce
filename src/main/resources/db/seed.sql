-- ============================================================================
-- SEED DATA SCRIPT FOR MICROSOFT SQL SERVER
-- Project: Enterprise E-Commerce Web Application (Tomcat 11 + Jakarta EE 11)
-- Database: ecommerce_db
-- ============================================================================

USE ecommerce_db;
GO

-- Clean up existing data in correct FK order
DELETE FROM dbo.audit_logs;
DELETE FROM dbo.reviews;
DELETE FROM dbo.payments;
DELETE FROM dbo.order_status_history;
DELETE FROM dbo.order_items;
DELETE FROM dbo.coupon_usage;
DELETE FROM dbo.orders;
DELETE FROM dbo.wishlist_items;
DELETE FROM dbo.wishlists;
DELETE FROM dbo.cart_items;
DELETE FROM dbo.carts;
DELETE FROM dbo.inventory_transactions;
DELETE FROM dbo.inventory;
DELETE FROM dbo.product_images;
DELETE FROM dbo.products;
DELETE FROM dbo.categories;
DELETE FROM dbo.addresses;
DELETE FROM dbo.password_reset_tokens;
DELETE FROM dbo.user_roles;
DELETE FROM dbo.users;
DELETE FROM dbo.roles;
DELETE FROM dbo.coupons;
GO

-- 1. Seed Roles
SET IDENTITY_INSERT dbo.roles ON;
INSERT INTO dbo.roles (role_id, role_name, description)
VALUES 
    (1, 'ADMIN', 'System Administrator with full access to management features'),
    (2, 'CUSTOMER', 'Registered customer with shopping, ordering, and profile access'),
    (3, 'INVENTORY_MANAGER', 'Inventory staff managing stock and suppliers'),
    (4, 'ORDER_MANAGER', 'Staff responsible for processing and tracking fulfillment');
SET IDENTITY_INSERT dbo.roles OFF;
GO

-- 2. Seed Default Admin & Customer Accounts (Password: Admin@123)
-- BCrypt hash for "Admin@123": $2a$12$obLoD3hKXbn5rTW2gYWSYO8Y6yap0IVHV6FV.N2zgdrOZiTyjEl1m
SET IDENTITY_INSERT dbo.users ON;
INSERT INTO dbo.users (user_id, email, password_hash, first_name, last_name, phone, status)
VALUES 
    (1, 'admin@ecommerce.com', '$2a$12$obLoD3hKXbn5rTW2gYWSYO8Y6yap0IVHV6FV.N2zgdrOZiTyjEl1m', 'System', 'Administrator', '+919876543210', 'ACTIVE'),
    (2, 'customer@ecommerce.com', '$2a$12$obLoD3hKXbn5rTW2gYWSYO8Y6yap0IVHV6FV.N2zgdrOZiTyjEl1m', 'Rahul', 'Prasad', '+919876543211', 'ACTIVE'),
    (3, 'sarah.j@example.com', '$2a$12$obLoD3hKXbn5rTW2gYWSYO8Y6yap0IVHV6FV.N2zgdrOZiTyjEl1m', 'Sarah', 'Jenkins', '+919876543212', 'ACTIVE');
SET IDENTITY_INSERT dbo.users OFF;
GO

-- Assign Roles
INSERT INTO dbo.user_roles (user_id, role_id)
VALUES 
    (1, 1), -- Admin User -> ADMIN
    (2, 2), -- Customer User -> CUSTOMER
    (3, 2); -- Sarah -> CUSTOMER
GO

-- Seed Addresses for Customers
SET IDENTITY_INSERT dbo.addresses ON;
INSERT INTO dbo.addresses (address_id, user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default)
VALUES
    (1, 2, 'SHIPPING', 'Rahul Prasad', '+919876543211', 'Tower 4, Flat 502, Silicon Heights', 'Tech Park Road', 'Bengaluru', 'Karnataka', '560100', 'India', 1),
    (2, 2, 'BILLING', 'Rahul Prasad', '+919876543211', 'Tower 4, Flat 502, Silicon Heights', 'Tech Park Road', 'Bengaluru', 'Karnataka', '560100', 'India', 0);
SET IDENTITY_INSERT dbo.addresses OFF;
GO

-- 3. Seed Categories
SET IDENTITY_INSERT dbo.categories ON;
INSERT INTO dbo.categories (category_id, parent_category_id, category_name, slug, description, is_active)
VALUES 
    (1, NULL, 'Electronics', 'electronics', 'Cutting-edge gadgets, computing, and consumer electronics', 1),
    (2, 1, 'Laptops & Computers', 'laptops-computers', 'High performance workstations, ultraportables, and accessories', 1),
    (3, 1, 'Smartphones & Tablets', 'smartphones-tablets', 'Flagship 5G smartphones, iPads, and mobile gear', 1),
    (4, 1, 'Audio & Wearables', 'audio-wearables', 'Active noise cancelling headphones, earbuds, and smartwatches', 1),
    (5, NULL, 'Fashion & Apparel', 'fashion', 'Designer apparel, premium denim, and streetwear for all', 1),
    (6, 5, 'Mens Collection', 'mens-collection', 'Shirts, denim jeans, leather jackets, and casuals', 1),
    (7, 5, 'Womens Collection', 'womens-collection', 'Designer dresses, outerwear, and modern fashion', 1),
    (8, 5, 'Footwear & Sneakers', 'footwear', 'Athletic running shoes, luxury sneakers, and boots', 1),
    (9, NULL, 'Home & Kitchen', 'home-kitchen', 'Smart kitchen appliances, espresso makers, and cookware', 1);
SET IDENTITY_INSERT dbo.categories OFF;
GO

-- 4. Seed 14 Real-World Products
SET IDENTITY_INSERT dbo.products ON;
INSERT INTO dbo.products (product_id, category_id, sku, product_name, slug, description, brand, price, discount_percentage, tax_percentage, weight_kg, status)
VALUES 
    (1, 2, 'LAP-MBP-01', 'Apple MacBook Pro 16" (M3 Max, 36GB, 1TB SSD)', 'apple-macbook-pro-16-m3-max', 'Liquid Retina XDR display, blazing fast M3 Max 14-core CPU and 30-core GPU. Engineered for power users, developers, and creative pros.', 'Apple', 249900.00, 8.00, 18.00, 2.140, 'ACTIVE'),
    (2, 2, 'LAP-XPS-02', 'Dell XPS 15 9530 (i9-13900H, RTX 4070, 32GB RAM)', 'dell-xps-15-oled-touch', '15.6" 3.5K OLED InfinityEdge touch display, Intel Core i9 processor, NVIDIA GeForce RTX 4070 graphics with CNC machined aluminum chassis.', 'Dell', 189999.00, 12.00, 18.00, 1.920, 'ACTIVE'),
    (3, 2, 'LAP-ROG-03', 'ASUS ROG Strix SCAR 16 Gaming Laptop', 'asus-rog-strix-scar-16', 'Nebula HDR Mini-LED 240Hz display, Intel Core i9-14900HX, NVIDIA RTX 4080 12GB graphics, 32GB DDR5 RAM, RGB Aura Sync.', 'ASUS', 214990.00, 15.00, 18.00, 2.500, 'ACTIVE'),
    (4, 3, 'PHN-IPH-04', 'Apple iPhone 15 Pro Max (256GB - Natural Titanium)', 'iphone-15-pro-max-256gb', 'Aerospace-grade titanium design, A17 Pro chip, Action button, 48MP camera system with 5x telephoto optical zoom and USB-C speed.', 'Apple', 159900.00, 5.00, 18.00, 0.221, 'ACTIVE'),
    (5, 3, 'PHN-SGS-05', 'Samsung Galaxy S24 Ultra 5G (512GB - Titanium Black)', 'samsung-galaxy-s24-ultra', 'Galaxy AI features, integrated S-Pen, 200MP quad camera with 100x Space Zoom, Snapdragon 8 Gen 3 processor, and Gorilla Armor glass.', 'Samsung', 139999.00, 10.00, 18.00, 0.232, 'ACTIVE'),
    (6, 4, 'AUD-SNY-06', 'Sony WH-1000XM5 Wireless ANC Headphones', 'sony-wh-1000xm5-noise-cancelling', 'Industry-leading noise cancellation with two processors and 8 microphones. 30-hour battery life, speak-to-chat, and ultra-comfortable lightweight fit.', 'Sony', 29990.00, 20.00, 18.00, 0.250, 'ACTIVE'),
    (7, 4, 'AUD-APP-07', 'Apple AirPods Pro (2nd Gen) with MagSafe Case USB-C', 'apple-airpods-pro-2nd-gen', 'Up to 2x more Active Noise Cancellation, Adaptive Audio, Transparency mode, Personalized Spatial Audio, and precision tracking case.', 'Apple', 24900.00, 15.00, 18.00, 0.051, 'ACTIVE'),
    (8, 4, 'WRB-WAT-08', 'Apple Watch Ultra 2 (49mm Titanium - Ocean Band)', 'apple-watch-ultra-2-gps-cellular', 'Rugged 49mm titanium case, precision dual-frequency GPS, up to 36-hour battery life, 3000-nit brightest display, and dive computer certification.', 'Apple', 89900.00, 6.00, 18.00, 0.061, 'ACTIVE'),
    (9, 6, 'FAS-OXF-09', 'Ralph Lauren Classic Fit Oxford Cotton Shirt', 'ralph-lauren-classic-oxford-shirt', 'Pure yarn-dyed combed cotton, button-down point collar, signature embroidered pony, tailored for all-day comfort and sophistication.', 'Polo Ralph Lauren', 4999.00, 25.00, 5.00, 0.280, 'ACTIVE'),
    (10, 6, 'FAS-LEA-10', 'AllSaints Vintage Leather Bomber Biker Jacket', 'allsaints-vintage-leather-bomber-jacket', 'Handcrafted 100% lambskin leather with asymmetric zip closure, matte metal hardware, and soft quilted thermal lining.', 'AllSaints', 28500.00, 30.00, 5.00, 1.450, 'ACTIVE'),
    (11, 7, 'FAS-MAX-11', 'Zimmermann Floral Silk Chiffon Maxi Dress', 'zimmermann-floral-silk-maxi-dress', 'Tiered silk chiffon with vibrant botanical print, belted waist, blouson sleeves, and elegant flutter hemline.', 'Zimmermann', 18999.00, 20.00, 5.00, 0.420, 'ACTIVE'),
    (12, 8, 'FAS-NIK-12', 'Nike Air Max 270 React Running Shoes', 'nike-air-max-270-react-sneakers', 'Max Air 270 unit delivers unrivaled, all-day comfort. Lightweight woven fabric upper with foam midsole for energetic bounce.', 'Nike', 11995.00, 18.00, 12.00, 0.780, 'ACTIVE'),
    (13, 9, 'HOM-ESP-13', 'DeLonghi Magnifica S Automatic Espresso Coffee Machine', 'delonghi-magnifica-s-espresso-maker', '15-bar professional pressure, integrated conical burr bean grinder, manual cappuccino milk frothing wand, one-touch brewing.', 'DeLonghi', 42999.00, 22.00, 18.00, 9.000, 'ACTIVE'),
    (14, 9, 'HOM-FRY-14', 'Philips Digital XXL Air Fryer (7.2L Rapid Air)', 'philips-digital-air-fryer-xxl', 'Rapid Air technology for 90% less fat, digital touch display with 16 preset cooking programs, dishwasher-safe non-stick basket.', 'Philips', 14999.00, 25.00, 18.00, 6.250, 'ACTIVE');
SET IDENTITY_INSERT dbo.products OFF;
GO

-- 5. Seed High-Definition Product Photography
SET IDENTITY_INSERT dbo.product_images ON;
INSERT INTO dbo.product_images (image_id, product_id, image_url, alt_text, display_order, is_primary)
VALUES 
    (1, 1, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80', 'Apple MacBook Pro 16 Space Black', 1, 1),
    (2, 2, 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=800&q=80', 'Dell XPS 15 OLED Laptop', 1, 1),
    (3, 3, 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=800&q=80', 'ASUS ROG Strix Gaming Laptop', 1, 1),
    (4, 4, 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=800&q=80', 'Apple iPhone 15 Pro Max Natural Titanium', 1, 1),
    (5, 5, 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=800&q=80', 'Samsung Galaxy S24 Ultra Titanium', 1, 1),
    (6, 6, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80', 'Sony WH-1000XM5 ANC Headphones', 1, 1),
    (7, 7, 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=800&q=80', 'Apple AirPods Pro 2 with Wireless Case', 1, 1),
    (8, 8, 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=800&q=80', 'Apple Watch Ultra 2 Titanium Case', 1, 1),
    (9, 9, 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=800&q=80', 'Ralph Lauren Oxford Cotton Shirt', 1, 1),
    (10, 10, 'https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80', 'AllSaints Vintage Leather Bomber Jacket', 1, 1),
    (11, 11, 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=800&q=80', 'Zimmermann Floral Silk Maxi Dress', 1, 1),
    (12, 12, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 'Nike Air Max 270 Athletic Sneakers', 1, 1),
    (13, 13, 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=800&q=80', 'DeLonghi Automatic Espresso Coffee Machine', 1, 1),
    (14, 14, 'https://images.unsplash.com/photo-1584992236310-6edddc08acff?auto=format&fit=crop&w=800&q=80', 'Philips Digital Air Fryer XXL', 1, 1);
SET IDENTITY_INSERT dbo.product_images OFF;
GO

-- 6. Seed Inventory Stock & Audit Records
SET IDENTITY_INSERT dbo.inventory ON;
INSERT INTO dbo.inventory (inventory_id, product_id, quantity, low_stock_threshold)
VALUES 
    (1, 1, 20, 5),
    (2, 2, 15, 3),
    (3, 3, 12, 3),
    (4, 4, 30, 8),
    (5, 5, 25, 5),
    (6, 6, 45, 10),
    (7, 7, 60, 12),
    (8, 8, 18, 4),
    (9, 9, 80, 15),
    (10, 10, 14, 3),
    (11, 11, 22, 5),
    (12, 12, 50, 10),
    (13, 13, 16, 4),
    (14, 14, 35, 6);
SET IDENTITY_INSERT dbo.inventory OFF;
GO

INSERT INTO dbo.inventory_transactions (product_id, previous_stock, quantity_changed, new_stock, transaction_type, reference_type, reference_id, remarks, created_by)
VALUES 
    (1, 0, 20, 20, 'PURCHASE', 'SUPPLIER_RESTOCK', NULL, 'Initial inventory restock', 1),
    (4, 0, 30, 30, 'PURCHASE', 'SUPPLIER_RESTOCK', NULL, 'Initial inventory restock', 1),
    (6, 0, 45, 45, 'PURCHASE', 'SUPPLIER_RESTOCK', NULL, 'Initial inventory restock', 1),
    (12, 0, 50, 50, 'PURCHASE', 'SUPPLIER_RESTOCK', NULL, 'Initial inventory restock', 1);
GO

-- 7. Seed Active Promotions & Coupons
SET IDENTITY_INSERT dbo.coupons ON;
INSERT INTO dbo.coupons (coupon_id, code, discount_type, discount_value, min_order_amount, max_discount_amount, start_date, end_date, usage_limit, current_usage, is_active)
VALUES 
    (1, 'WELCOME10', 'PERCENTAGE', 10.00, 1000.00, 1500.00, SYSDATETIME(), DATEADD(year, 1, SYSDATETIME()), 5000, 0, 1),
    (2, 'FLAT500', 'FIXED_AMOUNT', 500.00, 3000.00, 500.00, SYSDATETIME(), DATEADD(year, 1, SYSDATETIME()), 2000, 0, 1),
    (3, 'SUPER20', 'PERCENTAGE', 20.00, 10000.00, 4000.00, SYSDATETIME(), DATEADD(year, 1, SYSDATETIME()), 1000, 0, 1),
    (4, 'FREESHIP', 'FIXED_AMOUNT', 100.00, 500.00, 100.00, SYSDATETIME(), DATEADD(year, 1, SYSDATETIME()), 10000, 0, 1);
SET IDENTITY_INSERT dbo.coupons OFF;
GO

-- 8. Seed Verified Customer Reviews
SET IDENTITY_INSERT dbo.reviews ON;
INSERT INTO dbo.reviews (review_id, product_id, user_id, rating, review_title, review_text, status, created_at, updated_at)
VALUES 
    (1, 1, 2, 5, 'Unmatched Power & Battery Life', 'The M3 Max compiles large Java codebases in seconds. The Liquid Retina screen is breathtaking. Highly recommend!', 'APPROVED', DATEADD(day, -5, SYSDATETIME()), DATEADD(day, -5, SYSDATETIME())),
    (2, 4, 2, 5, 'Titanium feels so light in hand', 'The 5x telephoto camera is crisp and battery easily lasts 1.5 days. Dynamic island is very useful.', 'APPROVED', DATEADD(day, -3, SYSDATETIME()), DATEADD(day, -3, SYSDATETIME())),
    (3, 6, 3, 5, 'Best Active Noise Cancelling on the Market', 'Blocks out all ambient airplane and office noise completely. Sound stage is wide and rich with deep bass.', 'APPROVED', DATEADD(day, -2, SYSDATETIME()), DATEADD(day, -2, SYSDATETIME())),
    (4, 12, 3, 4, 'Very comfortable for daily walking & running', 'Air cushion is bouncy and responsive. Looks great with joggers and jeans.', 'APPROVED', DATEADD(day, -1, SYSDATETIME()), DATEADD(day, -1, SYSDATETIME()));
SET IDENTITY_INSERT dbo.reviews OFF;
GO
