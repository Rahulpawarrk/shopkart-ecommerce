# URL Routing & API Route Catalog

## 1. Public & Customer Routes

| HTTP Method | Route / Pattern | Controller | Access Level | Description |
|---|---|---|---|---|
| `GET` | `/` | `index.jsp` | Public | Homepage showcasing hero banner, system metrics, and quick navigation. |
| `GET` | `/products` | `ProductListServlet` | Public | Search catalog with filtering, sorting, and pagination. |
| `GET` | `/product` | `ProductDetailServlet` | Public | View individual product details, image gallery, stock, and reviews. |
| `GET` / `POST` | `/login` | `LoginServlet` | Anonymous | Display login page / Authenticate user credentials. |
| `GET` / `POST` | `/register` | `RegisterServlet` | Anonymous | Display registration page / Create new customer account. |
| `GET` | `/logout` | `LogoutServlet` | Authenticated | Invalidate HTTP session and redirect to homepage. |
| `GET` / `POST` | `/change-password` | `ChangePasswordServlet` | Authenticated | Change customer password with verification. |
| `GET` | `/profile` | `ProfileServlet` | Customer | View customer profile and order history shortcuts. |
| `GET` / `POST` | `/addresses` | `AddressServlet` | Customer | View address book, add, edit, delete, or set default address. |
| `GET` / `POST` | `/cart` | `CartServlet` | Customer | View cart, add product, update item quantity, remove item. |
| `GET` / `POST` | `/wishlist` | `WishlistServlet` | Customer | View wishlist, add item, remove item, move item to cart. |
| `GET` / `POST` | `/checkout` | `CheckoutServlet` | Customer | Render checkout form, apply coupon, and place order atomically. |
| `GET` | `/order/confirmation` | `OrderConfirmationServlet` | Customer | Render order success screen with order tracking ID. |
| `GET` | `/orders` | `CustomerOrderServlet` | Customer | View customer order history list. |
| `GET` | `/order` | `CustomerOrderServlet` | Customer | View individual order details and tracking timeline. |
| `POST` | `/reviews` | `ReviewServlet` | Customer | Submit a 1-5 star review for a verified delivered purchase. |
| `GET` / `POST` | `/payment/gateway` | `PaymentGatewayServlet` | Customer | Render simulated payment gateway for online orders. |
| `POST` | `/payment/callback` | `PaymentCallbackServlet` | Customer | Process payment authorization status callback. |
| `GET` | `/health` | `HealthCheckServlet` | Public | System and database diagnostic dashboard. |
| `GET` | `/api/health` | `HealthCheckServlet` | Public | JSON health payload for monitoring services. |

---

## 2. Admin Console Routes (`/admin/*`)

*All administrative routes require the user to be logged in with the `ADMIN` role. Intercepted by `RoleFilter`.*

| HTTP Method | Route / Pattern | Controller | Description |
|---|---|---|---|
| `GET` | `/admin/dashboard` | `AdminDashboardServlet` | Main administration dashboard with live KPI counters and alerts. |
| `GET` | `/admin/products` | `AdminProductServlet` | Product catalog management list with status filters. |
| `GET` / `POST` | `/admin/products/add` | `AdminProductServlet` | Create new product and configure pricing/tax/stock. |
| `GET` / `POST` | `/admin/products/edit` | `AdminProductServlet` | Edit existing product or toggle active/archived status. |
| `POST` | `/admin/products/delete` | `AdminProductServlet` | Soft delete / deactivate product safely. |
| `GET` | `/admin/categories` | `AdminCategoryServlet` | Category hierarchy list. |
| `GET` / `POST` | `/admin/categories/add` | `AdminCategoryServlet` | Create category or subcategory. |
| `GET` / `POST` | `/admin/categories/edit` | `AdminCategoryServlet` | Edit category details and status. |
| `GET` | `/admin/inventory` | `AdminInventoryServlet` | Stock management dashboard with low-stock warnings. |
| `POST` | `/admin/inventory/adjust` | `AdminInventoryServlet` | Restock or manually adjust inventory with audit logging. |
| `GET` | `/admin/inventory/transactions` | `AdminInventoryServlet` | Immutable inventory transaction history ledger. |
| `GET` | `/admin/orders` | `AdminOrderServlet` | Manage and search all customer orders across statuses. |
| `GET` | `/admin/orders/detail` | `AdminOrderServlet` | Order detail view with customer info, line items, and payment info. |
| `POST` | `/admin/orders/update-status`| `AdminOrderServlet` | Transition order status (`CONFIRMED`, `SHIPPED`, `DELIVERED`, etc.). |
| `GET` | `/admin/coupons` | `AdminCouponServlet` | Promotional coupon catalog list. |
| `GET` / `POST` | `/admin/coupons/add` | `AdminCouponServlet` | Create discount code with limits and date window. |
| `POST` | `/admin/coupons/toggle` | `AdminCouponServlet` | Enable or disable coupon code. |
| `GET` | `/admin/reviews` | `AdminReviewServlet` | Customer review moderation queue (`APPROVED` / `REJECTED`). |
| `POST` | `/admin/reviews/moderate` | `AdminReviewServlet` | Moderate customer review status. |
| `GET` | `/admin/payments` | `AdminPaymentServlet` | Payment transaction log and reconciliation table. |
| `GET` | `/admin/reports/sales` | `AdminSalesReportServlet`| Sales analytics report with revenue breakdown and top sellers. |
| `GET` | `/admin/audit-logs` | `AdminAuditLogServlet` | System-wide audit log for compliance and operational tracking. |
