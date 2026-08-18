# Application Modules Breakdown

## Module Catalog

| # | Module | Core Responsibilities | Key Java Classes | Key Views |
|---|---|---|---|---|
| 1 | **Authentication & RBAC** | User registration, BCrypt password hashing, session management, role filters (`ADMIN`, `CUSTOMER`). | `AuthService`, `UserDAO`, `LoginServlet`, `RegisterServlet`, `AuthFilter`, `RoleFilter` | `login.jsp`, `register.jsp`, `change-password.jsp` |
| 2 | **Customer Profile & Addresses** | Customer profile view, address book management (Multiple shipping/billing addresses, default flag). | `AddressService`, `AddressDAO`, `AddressServlet`, `ProfileServlet` | `profile.jsp`, `address-list.jsp`, `address-form.jsp` |
| 3 | **Category Management** | Hierarchical category tree (Categories and Subcategories), slugs, active status. | `CategoryService`, `CategoryDAO`, `AdminCategoryServlet` | `category-list.jsp`, `category-form.jsp` |
| 4 | **Product Catalog & Search** | Storefront product browsing, dynamic filtering (keyword, category, price range, sort), multi-image gallery. | `ProductService`, `ProductDAO`, `ProductListServlet`, `ProductDetailServlet` | `product/list.jsp`, `product/detail.jsp` |
| 5 | **Inventory & Stock Tracking** | Current balance tracking, stock protection (`UPDLOCK`), immutable inventory ledger (`PURCHASE`, `SALE`, `RETURN`, `ADJUSTMENT`). | `InventoryService`, `InventoryDAO`, `AdminInventoryServlet` | `inventory-list.jsp`, `inventory-transactions.jsp` |
| 6 | **Shopping Cart** | Customer cart persistence, quantity modifications, server-side subtotal calculations, stock threshold checks. | `CartService`, `CartDAO`, `CartServlet` | `cart/cart.jsp` |
| 7 | **Wishlist** | Customer wishlist management, duplicate prevention via `UNIQUE(user_id, product_id)`, move-to-cart workflow. | `WishlistService`, `WishlistDAO`, `WishlistServlet` | `wishlist/wishlist.jsp` |
| 8 | **Checkout & Order Processing** | Atomic transaction order placement, stock decrement, historical pricing snapshot, coupon redemption, cart clearing. | `OrderService`, `OrderDAO`, `CheckoutServlet`, `CustomerOrderServlet`, `OrderConfirmationServlet` | `checkout.jsp`, `confirmation.jsp`, `order-list.jsp`, `order-detail.jsp` |
| 9 | **Payment Gateway Simulation** | Simulates online and COD payment flows (`PENDING` -> `SUCCESS` / `FAILED`), transaction references, status callbacks. | `PaymentService`, `PaymentDAO`, `PaymentGatewayServlet`, `PaymentCallbackServlet` | `gateway.jsp`, `failure.jsp`, `admin/payment-list.jsp` |
| 10 | **Coupon & Promotion Engine** | Server-side validation of discount codes (Percentage/Fixed amount, minimum order threshold, usage limits, date windows). | `CouponService`, `CouponDAO`, `CouponServlet`, `AdminCouponServlet` | `coupon-list.jsp`, `coupon-form.jsp` |
| 11 | **Product Reviews & Ratings** | Customer verified-purchase reviews (1-5 stars), rating aggregation summaries, admin moderation. | `ReviewService`, `ReviewDAO`, `ReviewServlet`, `AdminReviewServlet` | `product/detail.jsp`, `admin/review-list.jsp` |
| 12 | **Admin Dashboard & Audit Logging** | Executive KPI dashboard (Total sales, today's orders, low stock items, top products), sales reports, granular audit logging. | `DashboardService`, `AuditLogService`, `AdminDashboardServlet`, `AdminSalesReportServlet`, `AdminAuditLogServlet` | `admin/dashboard.jsp`, `admin/sales-report.jsp`, `admin/audit-list.jsp` |

---

## Detailed Module Implementations

### 1. Authentication & RBAC Module
- **Registration**: Checks email uniqueness in `dbo.users`, hashes password with `jbcrypt` (work factor 10), assigns `CUSTOMER` role.
- **Login**: Verifies credentials, prevents inactive/suspended users from logging in, establishes `UserSession` in `HttpSession`.
- **Security Filters**: 
  - `AuthFilter` protects `/profile`, `/cart`, `/wishlist`, `/checkout`, `/orders`, `/addresses`, and `/admin`.
  - `RoleFilter` intercepts `/admin/*` and ensures the logged-in user possesses the `ADMIN` role.

### 2. Product Catalog & Search Module
- **Search Criteria DTO**: `ProductSearchCriteria` captures keyword, category ID, price range, brand, stock filter, sorting order (`price_asc`, `price_desc`, `name_asc`, `newest`), and pagination page number.
- **Pagination Object**: Generic `Pagination<T>` object encapsulates items list, current page, page size, total items count, total pages, and navigation helpers (`hasPrevious()`, `hasNext()`).

### 3. Inventory & Concurrency Protection Module
- **Ledger Invariant**: Current stock in `dbo.inventory` must always equal the sum of historical inventory transactions.
- **Transaction Types**:
  - `PURCHASE`: Supplier restock (+quantity).
  - `SALE`: Customer order placed (-quantity).
  - `RETURN`: Customer returned product (+quantity).
  - `CANCELLATION`: Order cancelled prior to delivery (+quantity).
  - `ADJUSTMENT`: Inventory audit correction (+/- quantity).

### 4. Atomic Checkout & Order Processing Module
- Wrapped in a single database transaction across `OrderDAO`, `InventoryDAO`, `PaymentDAO`, `CouponDAO`, and `CartDAO`.
- If an item goes out of stock between cart view and order submission, the transaction rolls back cleanly without dirty state.

### 5. Verified Product Reviews Module
- Customers may only submit a review if they have purchased the product in an order with status `DELIVERED`.
- Prevents fake reviews and duplicate ratings through database constraint `UNIQUE(product_id, user_id)`.
