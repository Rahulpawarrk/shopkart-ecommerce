# ShopKart — REST API Catalog & Route Reference

All client-to-server communication is conducted via structured JSON REST endpoints under `/api/**`.

---

## 1. Authentication Endpoints (`/api/auth`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Anonymous | Authenticate customer or admin credentials. |
| `POST` | `/api/auth/register` | Anonymous | Create new customer account with validation. |
| `GET` | `/api/auth/me` | Authenticated | Retrieve active authenticated session user profile. |
| `POST` | `/api/auth/logout` | Authenticated | Invalidate current session and clear security context. |
| `POST` | `/api/auth/change-password` | Authenticated | Update user password with current password confirmation. |
| `POST` | `/api/auth/forgot-password` | Anonymous | Request password reset token via email. |
| `POST` | `/api/auth/reset-password` | Anonymous | Reset account password using token. |

---

## 2. Storefront Catalog & Categories (`/api/products`, `/api/categories`, `/api/home`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/home/showcase` | Public | Featured categories, hero deals, bestsellers, and new arrivals. |
| `GET` | `/api/products` | Public | Paginated product listing with filters (category, price, sort). |
| `GET` | `/api/products/{id}` | Public | Detailed product view by numeric ID. |
| `GET` | `/api/products/slug/{slug}` | Public | Detailed product view by SEO slug. |
| `GET` | `/api/categories` | Public | List all active product categories and subcategories. |
| `GET` | `/api/categories/{id}` | Public | Retrieve category metadata. |

---

## 3. Cart & Wishlist (`/api/cart`, `/api/wishlist`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/cart` | Public / User | Retrieve current shopping cart and calculated totals. |
| `POST` | `/api/cart/items` | Public / User | Add product to cart with requested quantity. |
| `PUT` | `/api/cart/items/{id}` | Public / User | Update quantity of item in cart. |
| `DELETE`| `/api/cart/items/{id}` | Public / User | Remove item from cart. |
| `POST` | `/api/cart/coupon` | Public / User | Apply promotional coupon code. |
| `DELETE`| `/api/cart/coupon` | Public / User | Remove applied coupon from cart. |
| `GET` | `/api/wishlist` | Authenticated | Retrieve customer wishlist. |
| `POST` | `/api/wishlist/toggle` | Authenticated | Add or remove product from wishlist. |
| `POST` | `/api/wishlist/move-to-cart/{id}` | Authenticated | Move item directly from wishlist to shopping cart. |

---

## 4. Customer Profile & Addresses (`/api/customer`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/customer/profile` | Customer | Get customer account profile details. |
| `PUT` | `/api/customer/profile` | Customer | Update customer contact and personal information. |
| `GET` | `/api/customer/addresses` | Customer | Retrieve all saved delivery addresses. |
| `POST` | `/api/customer/addresses` | Customer | Add new delivery address to address book. |
| `PUT` | `/api/customer/addresses/{id}`| Customer | Edit existing delivery address. |
| `DELETE`| `/api/customer/addresses/{id}`| Customer | Delete delivery address. |
| `PUT` | `/api/customer/addresses/{id}/default` | Customer | Set address as default for checkout. |

---

## 5. Orders & Payments (`/api/orders`, `/api/payments`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/orders/checkout` | Customer | Place atomic order from cart items. |
| `POST` | `/api/orders/direct-buy` | Customer | Instant single-item purchase bypassing cart. |
| `GET` | `/api/orders/my-orders` | Customer | Paginated list of customer order history. |
| `GET` | `/api/orders/{id}` | Customer | Detailed order receipt with line items and shipping info. |
| `POST` | `/api/orders/{id}/cancel` | Customer | Cancel pending or processing order with reason. |
| `POST` | `/api/orders/{id}/return` | Customer | Request return claim for delivered order item. |
| `GET` | `/api/orders/{id}/tracking`| Customer | Retrieve live milestone shipment tracking events. |
| `POST` | `/api/payments/initiate/{orderId}` | Customer | Create Razorpay order and get public payment credentials. |
| `POST` | `/api/payments/verify` | Customer | Verify payment via HMAC-SHA256 signature and finalize order. |
| `POST` | `/api/payments/failure` | Customer | Record payment failure event for telemetry. |

---

## 6. Admin Management Console (`/api/admin/*`)

*All administrative endpoints require authentication with `ROLE_ADMIN`.*

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/dashboard/stats` | High-level business KPIs (Revenue, Orders, Low-Stock Count). |
| `GET` | `/api/admin/reports/sales` | Monthly sales velocity and category revenue distribution. |
| `GET` | `/api/admin/products` | Paginated product table with inventory and status filters. |
| `POST` | `/api/admin/products` | Create new product in catalog. |
| `PUT` | `/api/admin/products/{id}` | Update product pricing, description, and details. |
| `PUT` | `/api/admin/products/{id}/status` | Toggle product active/inactive listing status. |
| `GET` | `/api/admin/categories` | Manage category taxonomy tree. |
| `POST` | `/api/admin/categories` | Create new product category. |
| `PUT` | `/api/admin/categories/{id}` | Update category metadata or parent category. |
| `GET` | `/api/admin/orders` | Search and filter all customer orders across fulfillment states. |
| `GET` | `/api/admin/orders/{id}` | Inspect full customer order, items, and address snapshot. |
| `PUT` | `/api/admin/orders/{id}/status` | Advance fulfillment status and assign courier partner / AWB. |
| `GET` | `/api/admin/inventory` | Monitor warehouse stock levels and reorder thresholds. |
| `POST` | `/api/admin/inventory/adjust` | Record manual inventory adjustments with audit notes. |
| `GET` | `/api/admin/coupons` | Manage discount vouchers and promotion rules. |
| `POST` | `/api/admin/coupons` | Create new promotional discount coupon. |
| `DELETE`| `/api/admin/coupons/{id}` | Delete / deactivate coupon code. |
| `GET` | `/api/admin/returns` | Review customer return requests. |
| `PUT` | `/api/admin/returns/{id}/status`| Approve/reject return and authorize refund amount. |
| `GET` | `/api/admin/audit-logs` | Immutable audit trail of system and admin events. |
