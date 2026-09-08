# ShopKart — Enterprise E-Commerce Platform

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-21-blue.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)]()
[![Frontend](https://img.shields.io/badge/React%2019-TypeScript%20%2B%20Vite%20%2B%20Tailwind-blueviolet.svg)]()
[![Database](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)]()
[![Payment](https://img.shields.io/badge/Payment-Razorpay%20HMAC--SHA256-blue.svg)]()
[![Tests](https://img.shields.io/badge/Tests-133%20Passed-success.svg)]()

> An enterprise-grade, high-performance E-Commerce web application engineered with **Spring Boot 3, Hibernate 6 / Spring Data JPA, PostgreSQL, Razorpay payment gateway**, and a modern decoupled frontend built with **React 19, TypeScript, Vite, Tailwind CSS, and Redux Toolkit**.

---

## 🌟 Key Capabilities & Highlights

- **Decoupled Modern Architecture**: Decoupled single-page application (SPA) communicating over clean, type-safe REST APIs (`/api/**`) with CORS and CSRF protection.
- **Modern React 19 & TypeScript Frontend**:
  - Storefront catalog with real-time search, category hierarchy, price filtering, and pagination.
  - Slide-over shopping cart drawer, coupon code application, and interactive wishlist.
  - Multi-step checkout with address selection, Cash on Delivery (COD), and dynamic Razorpay payment modal invocation (`checkout.js`).
  - Customer order management with live milestone tracking, printable invoices, cancel dialog, and verified-purchase returns.
  - Complete **Admin Console** (`/admin`) with executive KPI analytics, sales velocity bar charts, product/category/inventory CRUD, order dispatch workflows, coupon management, return request approvals, and immutable security audit logs.
- **Defensive Backend & Security**:
  - **Spring Boot 3.4.3** on Java 21 with Spring Data JPA & Hibernate 6.
  - Role-Based Access Control (`AuthFilter`, `RoleFilter`) returning structured JSON `401 Unauthorized` / `403 Forbidden` for API requests.
  - BCrypt password hashing, session hijacking protection (`HttpOnly`, `SameSite=Lax`), and parameterized queries.
  - Production-ready **Razorpay Payment Gateway** integration with server-side HMAC-SHA256 signature verification.
- **Relational Data & Concurrency**:
  - **PostgreSQL** relational database with HikariCP connection pooling.
  - Concurrency safeguards and row-level locking protecting inventory transactions from overselling.
- **Test Suite**: **133 unit and integration tests** passing with **JUnit 5** and **Mockito**.

---

## 📐 High-Level Architecture Flow

```
[ React SPA Client (Vite + TS + Redux) ]
                │
                ▼  (REST API calls: /api/** with Credentials & CSRF)
[ Servlet Filters (AuthFilter, RoleFilter, UTF-8) ]
                │
                ▼
[ Spring REST Controllers (com.example.ecommerce.api.*) ]
                │  (DTO Validation via Jakarta Bean Validation)
                ▼
[ Service Layer (Transactions, Validations, Pricing Calculations) ]
                │  (Spring Data JPA / Hibernate 6)
                ▼
[ HikariCP Connection Pool ]
                │
                ▼
[ PostgreSQL Database (ecommerce_db) ]
```

---

## 📂 Project Structure

```text
ecommerce-web/
├── docs/                                # Technical Architectural Documentation
│   ├── architecture.md                  # Decoupled SPA & Spring Boot Architecture
│   ├── database-design.md               # PostgreSQL Schema, Table Specifications & ER Model
│   ├── modules.md                       # Business Modules Specification
│   ├── api-routes.md                    # Complete Spring REST API Catalog
│   ├── security.md                      # BCrypt, RBAC, XSS, CSRF, and SQLi Defenses
│   ├── business-rules.md                # Pricing Formulas, Concurrency Locking & Coupon Rules
│   └── deployment.md                    # Render, Docker & Production Setup
│
├── frontend/                            # React 19 + TypeScript + Vite Single-Page Application
│   ├── src/
│   │   ├── components/
│   │   │   ├── common/                  # ToastContainer, RatingStars, Modals
│   │   │   ├── layout/                  # Navbar, Footer, CartDrawer
│   │   │   └── product/                 # ProductCard, ReviewSection
│   │   ├── pages/
│   │   │   ├── admin/                   # AdminLayout, Dashboard, Products, Orders, Inventory, Coupons, Returns, AuditLogs
│   │   │   ├── auth/                    # LoginPage, RegisterPage, ForgotPasswordPage, ResetPasswordPage
│   │   │   ├── cart/                    # CartPage
│   │   │   ├── checkout/                # CheckoutPage (Razorpay Integration), OrderConfirmationPage
│   │   │   ├── customer/                # OrdersPage, OrderDetailPage, TrackOrderPage, ProfilePage, AddressBookPage, WishlistPage
│   │   │   └── store/                   # HomePage, ProductListingPage, ProductDetailPage
│   │   ├── routes/                      # AppRoutes (ProtectedRoute, AdminRoute)
│   │   ├── services/                    # Axios API Client, AuthService, ProductService, OrderService, AdminService
│   │   ├── store/                       # Redux Toolkit Store (authSlice, cartSlice, wishlistSlice, uiSlice)
│   │   └── types/                       # Comprehensive TypeScript Type Definitions
│   ├── tailwind.config.js               # Tailwind CSS styling configuration
│   └── vite.config.ts                   # Vite proxy configuration (/api -> http://localhost:8080)
│
├── src/
│   ├── main/
│   │   ├── java/com/example/ecommerce/
│   │   │   ├── api/                     # 11 Spring REST Controllers & 28 Request/Response DTOs
│   │   │   ├── admin/                   # Admin Services & DAOs
│   │   │   ├── audit/                   # Audit Logging Engine
│   │   │   ├── auth/                    # User Authentication & Security Services
│   │   │   ├── cart/                    # Cart Management & Pricing Engine
│   │   │   ├── category/                # Category Services
│   │   │   ├── config/                  # DBConnection (HikariCP), WebMvcConfig (CORS & SPA routing)
│   │   │   ├── coupon/                  # Coupon & Discount Engine
│   │   │   ├── customer/                # Customer Profile & Address Book Services
│   │   │   ├── filter/                  # AuthFilter & RoleFilter
│   │   │   ├── inventory/               # Stock Protection & Transactions
│   │   │   ├── order/                   # Atomic Checkout & Fulfillment Services
│   │   │   ├── payment/                 # Razorpay Gateway & Signature Verification
│   │   │   ├── product/                 # Product Catalog & Search
│   │   │   └── review/                  # Reviews & Ratings
│   │   └── resources/
│   │       ├── application.properties   # Spring Boot & HikariCP Configuration
│   │       └── logback.xml              # Structured Logging Configuration
│   └── test/java/                       # 133 Unit & Integration Tests (JUnit 5 & Mockito)
└── pom.xml                              # Maven Configuration (Java 21, Spring Boot 3.4.3)
```

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21** or later
- **Maven 3.9+**
- **Node.js 20+** and **npm**
- **PostgreSQL** running on port `5432` with database `ecommerce_db`

### 1. Run the Spring Boot Backend
```bash
# Clone the repository
git clone https://github.com/Rahulpawarrk/shopkart-ecommerce.git
cd shopkart-ecommerce

# Run tests
mvn test

# Start the Spring Boot server (port 8080)
mvn spring-boot:run
```

### 2. Run the React Frontend
```bash
cd frontend

# Install frontend dependencies
npm install

# Start the Vite development server (port 5173 with proxy to 8080)
npm run dev
```

Visit **`http://localhost:5173`** in your browser.

---

## 🛡️ Payment Gateway Integration (Razorpay)

1. Client checkout requests payment initiation via `POST /api/payments/initiate/{orderId}`.
2. Server calls Razorpay API to generate a server-side `razorpay_order_id` and returns the public `keyId`.
3. Client opens the official Razorpay Checkout modal via injected `checkout.js`.
4. Upon successful payment authorization, client sends `razorpay_payment_id`, `razorpay_order_id`, and `razorpay_signature` to `POST /api/payments/verify`.
5. Server verifies signature strictly using HMAC-SHA256 (`RAZORPAY_KEY_SECRET`). Only upon valid signature is the order marked `PAID` and inventory finalized.
