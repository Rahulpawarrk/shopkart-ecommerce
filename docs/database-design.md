# Microsoft SQL Server Database Design & Schema

## 1. Database Specifications

- **DBMS**: Microsoft SQL Server 2019 / 2022
- **Database Name**: `ecommerce_db`
- **Collation**: `SQL_Latin1_General_CP1_CI_AS` (Unicode support via `NVARCHAR`)
- **Connection Pool**: HikariCP (Max: 20, Min Idle: 5)
- **Isolation Level**: READ COMMITTED with Explicit Row-Level Updatable Locks (`WITH (UPDLOCK, ROWLOCK)`) on critical inventory transactions.

---

## 2. Relational Entity Relationship (ER) Model

```
                    ┌─────────────────────────┐
                    │          roles          │
                    └────────────┬────────────┘
                                 │ 1
                                 │
                                 │ N
                    ┌────────────┴────────────┐
                    │       user_roles        │
                    └────────────┬────────────┘
                                 │ N
                                 │
                                 │ 1
                    ┌────────────┴────────────┐
                    │          users          │◀─────────────────────────┐
                    └──┬──────┬──────┬──────┬─┘                          │
                       │ 1    │ 1    │ 1    │ 1                          │
                       │      │      │      │                            │
             ┌─────────┘      │      │      └──────────┐                 │
             │ N              │ 1    │ 1               │ N               │
  ┌──────────▼──────────┐ ┌───▼──┐ ┌─▼────────┐ ┌──────▼──────┐          │
  │      addresses      │ │carts │ │wishlists │ │   orders    │          │
  └──────────┬──────────┘ └───┬──┘ └───┬──────┘ └──────┬──────┘          │
             │ 1              │ 1      │ 1             │ 1               │
             │                │        │               │                 │
             │ N              │ N      │ N             │ N               │
             │           ┌────▼─────┐┌─▼────────┐ ┌────▼──────────┐      │
             │           │cart_items││wishlist_ │ │  order_items  │      │
             │           │          ││  items   │ └────┬──────────┘      │
             │           └────┬─────┘└──┬───────┘      │ N               │
             │                │ N       │ N            │                 │
             │                │         │              │                 │
             │ 1              │ 1       │ 1            │ 1               │
             │           ┌────▼─────────▼──────────────▼────┐            │
             │           │             products             │            │
             │           └────┬───────────┬─────────────┬───┘            │
             │                │ 1         │ 1           │ 1              │
             │                │           │             │                │
             │                │ N         │ 1           │ N              │
             │           ┌────▼─────┐┌────▼────────┐┌───▼──────┐         │
             │           │ product_ ││  inventory  ││ reviews  ├─────────┘
             │           │  images  │└────┬────────┘└──────────┘
             │           └──────────┘     │ 1
             │                            │
             │                            │ N
             │                       ┌────▼─────────────────┐
             │                       │inventory_transactions│
             │                       └──────────────────────┘
             │
             └───────────────────────────────────────────────────────────┐
                                                                         │
                                                             ┌───────────▼───────────┐
                                                             │     order_payments    │
                                                             └───────────────────────┘
```

---

## 3. Table Catalog & Schema Definitions

### 1. `dbo.roles`
Stores application roles (`ADMIN`, `CUSTOMER`, `INVENTORY_MANAGER`, `ORDER_MANAGER`).
- `role_id` (INT, IDENTITY, PK)
- `role_name` (VARCHAR(50), UNIQUE, NOT NULL)
- `description` (NVARCHAR(255), NULL)
- `created_at` (DATETIME2, DEFAULT SYSUTCDATETIME())

### 2. `dbo.users`
Stores user authentication credentials and account status.
- `user_id` (INT, IDENTITY, PK)
- `email` (VARCHAR(255), UNIQUE, NOT NULL) - *Indexed*
- `password_hash` (VARCHAR(255), NOT NULL) - *BCrypt ($2a$10$...)*
- `first_name` (NVARCHAR(100), NOT NULL)
- `last_name` (NVARCHAR(100), NOT NULL)
- `phone` (VARCHAR(20), NULL)
- `status` (VARCHAR(20), CHECK IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION')) - *Indexed*
- `created_at` (DATETIME2, DEFAULT SYSUTCDATETIME())
- `updated_at` (DATETIME2, DEFAULT SYSUTCDATETIME())

### 3. `dbo.user_roles`
Composite join table establishing many-to-many relationship between users and roles.
- `user_id` (INT, FK -> `dbo.users(user_id) ON DELETE CASCADE`)
- `role_id` (INT, FK -> `dbo.roles(role_id) ON DELETE CASCADE`)
- `assigned_at` (DATETIME2, DEFAULT SYSUTCDATETIME())
- **Primary Key**: `(user_id, role_id)`

### 4. `dbo.categories`
Hierarchical category tree supporting unlimited nested subcategories.
- `category_id` (INT, IDENTITY, PK)
- `parent_category_id` (INT, NULL, FK -> `dbo.categories(category_id)`)
- `category_name` (NVARCHAR(100), NOT NULL)
- `slug` (VARCHAR(120), UNIQUE, NOT NULL)
- `description` (NVARCHAR(500), NULL)
- `is_active` (BIT, DEFAULT 1)

### 5. `dbo.products`
Product catalog storing SKU, pricing, tax, weight, and discount percentages.
- `product_id` (INT, IDENTITY, PK)
- `category_id` (INT, NOT NULL, FK -> `dbo.categories(category_id)`) - *Indexed*
- `sku` (VARCHAR(100), UNIQUE, NOT NULL) - *Indexed*
- `product_name` (NVARCHAR(255), NOT NULL)
- `slug` (VARCHAR(300), UNIQUE, NOT NULL)
- `description` (NVARCHAR(MAX), NULL)
- `brand` (NVARCHAR(100), NULL) - *Indexed*
- `price` (DECIMAL(18,2), NOT NULL CHECK >= 0) - *Indexed*
- `discount_percentage` (DECIMAL(5,2), DEFAULT 0.00 CHECK (0 <= val <= 100))
- `tax_percentage` (DECIMAL(5,2), DEFAULT 0.00 CHECK >= 0)
- `weight_kg` (DECIMAL(8,3), DEFAULT 0.000)
- `status` (VARCHAR(20), DEFAULT 'ACTIVE' CHECK IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')) - *Indexed*
- `created_at` / `updated_at` (DATETIME2)

### 6. `dbo.product_images`
Multiple gallery images per product with primary flag and display ordering.
- `image_id` (INT, IDENTITY, PK)
- `product_id` (INT, NOT NULL, FK -> `dbo.products(product_id) ON DELETE CASCADE`)
- `image_url` (VARCHAR(1000), NOT NULL)
- `alt_text` (NVARCHAR(255), NULL)
- `display_order` (INT, DEFAULT 0)
- `is_primary` (BIT, DEFAULT 0)
- `created_at` (DATETIME2)

### 7. `dbo.inventory` & `dbo.inventory_transactions`
Current stock balance and immutable historical transaction log.
- `inventory_id` (INT, IDENTITY, PK)
- `product_id` (INT, UNIQUE, NOT NULL, FK -> `dbo.products(product_id) ON DELETE CASCADE`)
- `quantity` (INT, NOT NULL CHECK >= 0)
- `low_stock_threshold` (INT, DEFAULT 5 CHECK >= 0)
- `last_updated` (DATETIME2)

*Transactions Log*:
- `transaction_id` (INT, IDENTITY, PK)
- `product_id` (INT, NOT NULL, FK -> `dbo.products(product_id)`)
- `previous_stock` (INT, NOT NULL CHECK >= 0)
- `quantity_changed` (INT, NOT NULL)
- `new_stock` (INT, NOT NULL CHECK >= 0)
- `transaction_type` (VARCHAR(30), CHECK IN ('PURCHASE', 'SALE', 'RETURN', 'CANCELLATION', 'ADJUSTMENT'))
- `reference_type` (VARCHAR(30), NULL) -- 'ORDER', 'SUPPLIER_RESTOCK', 'ADMIN_ADJUSTMENT'
- `reference_id` (INT, NULL)
- `remarks` (NVARCHAR(500), NULL)
- `created_by` (INT, NULL, FK -> `dbo.users(user_id)`)
- `created_at` (DATETIME2)

### 8. `dbo.carts` & `dbo.cart_items`
Persistent customer shopping cart.
- `cart_id` (INT, IDENTITY, PK)
- `user_id` (INT, UNIQUE, NOT NULL, FK -> `dbo.users(user_id) ON DELETE CASCADE`)
- `cart_items`: `(cart_item_id, cart_id, product_id, quantity, created_at, updated_at)` with `UNIQUE(cart_id, product_id)`.

### 9. `dbo.orders`, `dbo.order_items`, `dbo.order_status_history`
Complete order management ledger with preserved historical price/tax snapshots.
- `orders`: `(order_id, order_number, user_id, shipping_address_id, order_status, payment_status, subtotal, discount_amount, coupon_discount_amount, tax_amount, shipping_charge, grand_total, customer_notes, created_at, updated_at)`
- `order_items`: `(order_item_id, order_id, product_id, product_name, sku, unit_price, discount_amount, tax_amount, quantity, total_price, created_at)`
- `order_status_history`: `(history_id, order_id, previous_status, new_status, comment, changed_by, created_at)`

### 10. `dbo.payments`
- `payment_id` (INT, IDENTITY, PK)
- `order_id` (INT, NOT NULL, FK -> `dbo.orders(order_id)`)
- `payment_method` (VARCHAR(30), CHECK IN ('CASH_ON_DELIVERY', 'ONLINE_PAYMENT', 'CREDIT_CARD', 'UPI'))
- `transaction_reference` (VARCHAR(100), UNIQUE, NOT NULL)
- `amount` (DECIMAL(18,2), NOT NULL CHECK >= 0)
- `payment_status` (VARCHAR(20), CHECK IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'))
- `paid_at` / `created_at` (DATETIME2)

### 11. `dbo.coupons` & `dbo.coupon_usage`
- `coupons`: `(coupon_id, code, discount_type, discount_value, min_order_amount, max_discount_amount, start_date, end_date, usage_limit, current_usage, is_active)`
- `coupon_usage`: `(usage_id, coupon_id, user_id, order_id, discount_applied, used_at)`

### 12. `dbo.reviews` & `dbo.audit_logs`
- `reviews`: `(review_id, product_id, user_id, rating (1-5), review_title, review_text, status, created_at, updated_at)` with `UNIQUE(product_id, user_id)`
- `audit_logs`: `(log_id, user_id, entity_type, entity_id, action, old_value, new_value, ip_address, created_at)`

---

## 4. Performance Indexes

```sql
CREATE NONCLUSTERED INDEX IX_users_email ON dbo.users(email);
CREATE NONCLUSTERED INDEX IX_users_status ON dbo.users(status);
CREATE NONCLUSTERED INDEX IX_products_sku ON dbo.products(sku);
CREATE NONCLUSTERED INDEX IX_products_category ON dbo.products(category_id);
CREATE NONCLUSTERED INDEX IX_products_status ON dbo.products(status);
CREATE NONCLUSTERED INDEX IX_products_brand ON dbo.products(brand);
CREATE NONCLUSTERED INDEX IX_products_price ON dbo.products(price);
CREATE NONCLUSTERED INDEX IX_inventory_product ON dbo.inventory(product_id);
CREATE NONCLUSTERED INDEX IX_inv_trans_product ON dbo.inventory_transactions(product_id);
CREATE NONCLUSTERED INDEX IX_orders_user ON dbo.orders(user_id);
CREATE NONCLUSTERED INDEX IX_orders_status ON dbo.orders(order_status);
CREATE NONCLUSTERED INDEX IX_orders_created ON dbo.orders(created_at DESC);
CREATE NONCLUSTERED INDEX IX_order_items_order ON dbo.order_items(order_id);
CREATE NONCLUSTERED INDEX IX_reviews_product ON dbo.reviews(product_id);
CREATE NONCLUSTERED INDEX IX_audit_created ON dbo.audit_logs(created_at DESC);
```
