# ShopKart — Business Modules Specification

## Module Catalog

| # | Module | Core Responsibilities | Backend Controllers / Services | Frontend Views / Components |
|---|---|---|---|---|
| 1 | **Authentication & RBAC** | User registration, BCrypt password hashing, session management, role verification (`ADMIN`, `CUSTOMER`). | `AuthRestController`, `AuthService`, `AuthFilter`, `RoleFilter` | `LoginPage.tsx`, `RegisterPage.tsx`, `ForgotPasswordPage.tsx`, `ResetPasswordPage.tsx` |
| 2 | **Customer Profile & Addresses** | Profile management, multi-address book with default shipping flag. | `CustomerRestController`, `CustomerService`, `AddressService` | `ProfilePage.tsx`, `AddressBookPage.tsx` |
| 3 | **Category Management** | Hierarchical category taxonomy, slugs, active listing status. | `CategoryRestController`, `AdminRestController`, `CategoryService` | `AdminCategoriesPage.tsx`, `Navbar.tsx` |
| 4 | **Product Catalog & Search** | Storefront browsing, faceted filtering (category, price, sort), multi-image gallery, SEO slugs. | `ProductRestController`, `AdminRestController`, `ProductService` | `ProductListingPage.tsx`, `ProductDetailPage.tsx`, `ProductCard.tsx` |
| 5 | **Inventory & Stock Tracking** | Warehouse stock tracking, concurrency protection, low-stock alerts, audit logging. | `AdminRestController`, `InventoryService` | `AdminInventoryPage.tsx` |
| 6 | **Shopping Cart** | Cart persistence, quantity modification, server-side subtotal calculations, stock threshold checks. | `CartRestController`, `CartService` | `CartPage.tsx`, `CartDrawer.tsx` |
| 7 | **Wishlist** | Customer wishlist management, duplicate prevention, move-to-cart workflow. | `WishlistRestController`, `WishlistService` | `WishlistPage.tsx` |
| 8 | **Checkout & Order Processing** | Atomic order placement, stock decrement, historical pricing snapshot, coupon redemption. | `OrderRestController`, `AdminRestController`, `OrderService` | `CheckoutPage.tsx`, `OrderConfirmationPage.tsx`, `OrdersPage.tsx`, `OrderDetailPage.tsx` |
| 9 | **Razorpay Payment Gateway** | Online payment initiation, order creation, HMAC-SHA256 signature verification, COD fallback. | `PaymentRestController`, `RazorpayService`, `PaymentService` | `CheckoutPage.tsx` (Razorpay modal integration) |
| 10 | **Promotions & Coupons** | Percentage/fixed discount vouchers, minimum order value, usage limits, date windows. | `CartRestController`, `AdminRestController`, `CouponService` | `AdminCouponsPage.tsx`, `CartPage.tsx` |
| 11 | **Product Reviews & Ratings** | Customer verified-purchase reviews (1-5 stars), rating aggregation summaries. | `ReviewRestController`, `ReviewService` | `ProductDetailPage.tsx` (Review list & submission) |
| 12 | **Admin Dashboard & Audit Logging** | Real-time business KPIs (Revenue, orders, top products), monthly sales charts, immutable audit logging. | `AdminRestController`, `AuditLogService` | `AdminDashboardPage.tsx`, `AdminAuditLogsPage.tsx` |
