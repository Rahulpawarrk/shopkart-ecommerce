# Enterprise E-Commerce Platform Architecture

## 1. Architectural Overview

This application is built with a **strict Multi-Tiered MVC (Model-View-Controller) Architecture**, incorporating dedicated **Service** and **Data Access Object (DAO)** layers. It runs on **Jakarta EE 11** on top of **Apache Tomcat 11**, persisting data to **Microsoft SQL Server** via standard **JDBC** and connection pooling with **HikariCP**.

```
[ Web Browser ]
      │
      ▼  (HTTP Request: GET / POST)
[ Servlet Filters ] (AuthFilter, RoleFilter, UTF-8 Encoding)
      │
      ▼
[ Jakarta Servlets (Controllers) ]
      │  (Delegates business logic, parameter extraction, view forwarding)
      ▼
[ Service Layer ]
      │  (Business rules, transactions, stock validations, coupon discounts)
      ▼
[ Data Access Object (DAO) Layer ]
      │  (PreparedStatements, SQL Server queries, ResultSet mapping)
      ▼
[ HikariCP Connection Pool / JDBC ]
      │
      ▼
[ Microsoft SQL Server (ecommerce_db) ]
```

---

## 2. Layer Responsibilities & Strict Boundaries

| Layer | Component | Core Responsibilities | Strict Prohibitions |
|---|---|---|---|
| **View** | JSP, JSTL, EL, HTML5, CSS3, JS | Displaying UI, form rendering, formatting currency/dates, submitting HTTP actions. | ❌ No direct SQL queries, No database connections, No business calculations. |
| **Controller** | Jakarta Servlets (`HttpServlet`) | Intercepting requests, parsing/sanitizing parameters, invoking services, binding model attributes, session management, forwarding/redirecting. | ❌ No direct SQL, No business calculations, No transaction management. |
| **Service** | POJO Business Services | Business rules, calculations (totals, discounts, taxes), multi-DAO orchestration, ACID transaction management (`commit`/`rollback`), concurrency safeguards. | ❌ No `HttpServletRequest`/`HttpServletResponse` dependencies, No UI rendering. |
| **Data Access** | DAO Classes | Executing SQL via `PreparedStatement`, handling `ResultSet` extraction, managing CRUD operations. | ❌ No business validation, No UI code, No session handling. |
| **Model** | POJO Domain Models & DTOs | Encapsulating relational state and data transfer payloads. | ❌ No database or business operations. |

---

## 3. Request & Response Lifecycle Flow

### Step-by-Step Flow for an Order Placement Request:
1. **Client Action**: Customer reviews cart and clicks "Place Order" (`POST /checkout/place-order`).
2. **Filter Interception**: `AuthFilter` verifies the `UserSession` exists in the active `HttpSession`. If not logged in, redirects to `/login`.
3. **Controller Handling**: `CheckoutServlet` reads address ID, payment method, customer notes, and CSRF token. It extracts the authenticated `userId`.
4. **Service Execution**: `OrderService.placeOrder()` begins a database transaction (`connection.setAutoCommit(false)`):
   - Validates user and active shipping address.
   - Fetches active cart items and computes authoritative server-side prices.
   - Verifies stock levels with SQL Server row-level locking (`WITH (UPDLOCK, ROWLOCK)`).
   - Validates and applies coupon discount if supplied.
   - Inserts order record into `dbo.orders`.
   - Inserts line items snapshot into `dbo.order_items`.
   - Decrements stock in `dbo.inventory` and writes audit log to `dbo.inventory_transactions`.
   - Generates initial payment record in `dbo.payments`.
   - Clears cart in `dbo.cart_items`.
   - Commits transaction (`connection.commit()`).
5. **Controller Response**: `CheckoutServlet` puts order confirmation into session/request and redirects to `/order/confirmation?orderId=...`.
6. **View Rendering**: `confirmation.jsp` renders order summary and tracking ID via JSTL `<c:out>` and `<fmt:formatNumber>`.

---

## 4. Why This Architecture?

1. **Testability**: Decoupled Services and DAOs can be unit-tested in isolation using **JUnit 5** and **Mockito** without booting Tomcat or maintaining live database state.
2. **Security**: Centralized Filter chain enforces role-based authorization before any controller code executes. PreparedStatements prevent SQL injection.
3. **Maintainability**: Clear separation of concerns means UI changes (JSP/CSS) never jeopardize business logic or data integrity.
4. **Resilience**: HikariCP handles database connection pooling, auto-reconnects, and prevents connection leaks through automatic timeout closures.
