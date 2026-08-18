# Enterprise E-Commerce Web Application

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-21%20%2F%2025-blue.svg)]()
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-11%20(Servlets%206.0)-orange.svg)]()
[![Tomcat](https://img.shields.io/badge/Tomcat-11.0-yellow.svg)]()
[![Database](https://img.shields.io/badge/Database-Microsoft%20SQL%20Server-red.svg)]()
[![Tests](https://img.shields.io/badge/Tests-52%20Passed-success.svg)]()

> A robust, production-style, multi-tier E-Commerce platform engineered with **pure Java, Jakarta Servlets, JSP, JSTL, JDBC, and Microsoft SQL Server**. Designed to master enterprise software patterns without heavy frameworks before transitioning to Spring Boot.

---

## 🌟 Key Capabilities & Highlights

- **Pure Jakarta EE 11 on Tomcat 11**: Fully compatible with `jakarta.servlet.*` and modern standard container specifications.
- **Strict MVC Architecture**: Clear separation of responsibilities between **View (JSP)**, **Controller (Servlets)**, **Business Service Layer**, **DAO Data Access Layer**, and **Domain Models**.
- **Robust Database Engine**: MS SQL Server relational schema with ACID transaction isolation, foreign key constraints, indexes, and **HikariCP** connection pooling.
- **Enterprise Inventory Ledger**: Immutable inventory audit log (`dbo.inventory_transactions`) tracking all stock movements (`PURCHASE`, `SALE`, `RETURN`, `ADJUSTMENT`) with explicit row-level locking (`UPDLOCK`) against negative stock race conditions.
- **Atomic Checkout & Historical Snapshots**: Transaction-protected multi-step order placement preserving historic price and tax rates in `dbo.order_items`.
- **Defensive Security Suite**:
  - **BCrypt** password hashing (No plaintext credentials).
  - Parameterized `PreparedStatement` everywhere (Zero SQL injection).
  - Context-aware XSS output escaping.
  - Role-Based Access Control (`AuthFilter`, `RoleFilter`).
  - Session hijacking protection with `HttpOnly`, `SameSite=Lax`, and inactivity timeouts.
- **Customer & Admin Experience**:
  - Storefront catalog with multi-criteria dynamic search, category tree, brand filtering, sorting, and pagination.
  - Shopping cart, wishlist, address book, coupon discount engine, and verified purchase reviews.
  - Full Admin Console with KPI analytics, product/category/coupon CRUD, order fulfillment timeline, and audit logging.
- **Comprehensive Testing Suite**: 52 unit and mock tests with **JUnit 5** and **Mockito**.

---

## 📐 High-Level Architecture Flow

```
[ Browser Client ]
        │
        ▼ (HTTP GET / POST)
[ Servlet Filters (AuthFilter, RoleFilter, UTF-8) ]
        │
        ▼
[ Jakarta Servlets (Controllers) ]
        │
        ▼
[ Service Layer (Transactions, Validations, Pricing Calculations) ]
        │
        ▼
[ Data Access Objects (DAO Layer via JDBC PreparedStatements) ]
        │
        ▼
[ HikariCP Connection Pool ]
        │
        ▼
[ Microsoft SQL Server Database (ecommerce_db) ]
```

---

## 📂 Project Structure

```text
ecommerce-web/
├── docs/                                # Detailed Architectural Documentation
│   ├── architecture.md                  # MVC Flow, Service/DAO Layers & Request Lifecycles
│   ├── database-design.md               # SQL Server Schema, Table Specifications & ER Model
│   ├── modules.md                       # Deep Dive into all 12 Business Modules
│   ├── api-routes.md                    # Complete Routing and Endpoint Table
│   ├── security.md                      # BCrypt, RBAC, XSS, CSRF, and SQLi Defenses
│   ├── business-rules.md                # Pricing Formulas, Concurrency Locking & Coupon Rules
│   └── deployment.md                    # Tomcat 11, SQL Server & Build Instructions
│
├── src/
│   ├── main/
│   │   ├── java/com/example/ecommerce/
│   │   │   ├── admin/                   # Admin Controllers, Services, DAOs
│   │   │   ├── audit/                   # Audit Logging Engine
│   │   │   ├── auth/                    # Registration, Login, Sessions, Security
│   │   │   ├── cart/                    # Cart Management & Subtotal Calculations
│   │   │   ├── category/                # Hierarchical Categories
│   │   │   ├── config/                  # DBConnection (HikariCP) & AppContextListener
│   │   │   ├── coupon/                  # Discount Engine
│   │   │   ├── customer/                # Address Management & Profiles
│   │   │   ├── exception/               # Centralized Custom Exceptions
│   │   │   ├── filter/                  # AuthFilter & RoleFilter
│   │   │   ├── health/                  # System Health & Diagnostic Endpoints
│   │   │   ├── inventory/               # Stock Protection & Transactions
│   │   │   ├── order/                   # Atomic Checkout & Orders
│   │   │   ├── payment/                 # Payment Simulation & Gateway
│   │   │   ├── product/                 # Products, Images, Search & Pagination
│   │   │   ├── review/                  # Verified Reviews & Ratings
│   │   │   ├── util/                    # PasswordUtil, Pagination Helper
│   │   │   └── wishlist/                # Wishlist & Move-to-Cart
│   │   │
│   │   ├── resources/
│   │   │   ├── db.properties            # Externalized Database & Pool Config
│   │   │   ├── logback.xml              # Logging Configuration
│   │   │   └── db/
│   │   │       ├── schema.sql           # SQL Server Schema Definition Script
│   │   │       └── seed.sql             # Default Admin, Products & Demo Data
│   │   │
│   │   └── webapp/
│   │       ├── assets/                  # CSS Stylesheet & Static Assets
│   │       ├── WEB-INF/
│   │       │   ├── web.xml              # Web Deployment Descriptor
│   │       │   └── views/               # Protected JSP Views
│   │       │       ├── admin/           # Dashboard, Products, Orders, Reports, Audit
│   │       │       ├── auth/            # Login, Register, Password
│   │       │       ├── cart/            # Cart View
│   │       │       ├── customer/        # Profile, Address Book
│   │       │       ├── error/           # 403, 404, 500 Pages
│   │       │       ├── order/           # Checkout, Confirmation, Order History
│   │       │       ├── payment/         # Gateway & Callbacks
│   │       │       ├── product/         # Catalog Search & Product Details
│   │       │       └── wishlist/        # Wishlist View
│   │       └── index.jsp                # Storefront Landing Page
│   │
│   └── test/java/com/example/ecommerce/ # 52 JUnit 5 + Mockito Unit Tests
│
└── pom.xml                              # Maven Configuration
```

---

## ⚡ Quick Start Guide

### 1. Database Setup
Create and seed the SQL Server database:
```bash
sqlcmd -S localhost -U sa -P YourStrongPassword123! -i src/main/resources/db/schema.sql
sqlcmd -S localhost -U sa -P YourStrongPassword123! -i src/main/resources/db/seed.sql
```

### 2. Build & Test
```bash
mvn clean test
mvn package
```

### 3. Deploy to Tomcat 11
Copy `target/ecommerce-web.war` to Tomcat's `webapps/` folder as `ROOT.war` and start Tomcat:
```bash
%CATALINA_HOME%\bin\startup.bat
```

### 4. Access the Application
- **Storefront**: `http://localhost:8080/`
- **Product Catalog**: `http://localhost:8080/products`
- **System Health Diagnostic**: `http://localhost:8080/health`
- **Admin Console**: `http://localhost:8080/admin/dashboard`

---

## 🔑 Default Accounts

| Role | Email | Password |
|---|---|---|
| **System Admin** | `admin@ecommerce.com` | `Admin@123` |
| **Demo Customer** | `customer@ecommerce.com` | `Admin@123` |

---

## 📚 In-Depth Documentation

For thorough explanations of design decisions, SQL optimization, and architecture rationale, consult the [`docs/`](file:///docs/) directory:
- [Architecture & MVC Flow](file:///docs/architecture.md)
- [Database Design & Schema](file:///docs/database-design.md)
- [Module Catalog](file:///docs/modules.md)
- [URL Routes & Endpoints](file:///docs/api-routes.md)
- [Security Architecture](file:///docs/security.md)
- [Business Rules & Concurrency](file:///docs/business-rules.md)
- [Deployment Guide](file:///docs/deployment.md)
