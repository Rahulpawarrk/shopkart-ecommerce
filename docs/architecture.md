# ShopKart — System Architecture & Design

## 1. Architectural Overview

ShopKart is architected as a **Decoupled Modern E-Commerce Platform** comprising a **React Single-Page Application (SPA)** frontend and a **Spring Boot REST API** backend powered by **Hibernate / Spring Data JPA** and **PostgreSQL**.

```
[ React SPA Client (TypeScript + Vite + Redux) ]
                       │
                       ▼  (HTTP REST over /api/** with credentials: true)
         [ Cross-Origin & Security Layer ]
    (WebMvcConfig CORS + AuthFilter + RoleFilter)
                       │
                       ▼
       [ Spring REST Controllers (@RestController) ]
            (Jakarta Bean Validation DTOs)
                       │
                       ▼
       [ Business Service Layer (@Service) ]
 (Transaction Management, Concurrency Locks, Pricing & Discounts)
                       │
                       ▼
       [ Data Access Layer (Spring Data JPA / DAOs) ]
                       │
                       ▼
          [ HikariCP Connection Pool ]
                       │
                       ▼
         [ PostgreSQL Database (ecommerce_db) ]
```

---

## 2. Layer Responsibilities & Strict Boundaries

| Layer | Technology | Responsibilities | Strict Prohibitions |
|---|---|---|---|
| **Client SPA** | React 19, TypeScript, Vite, Tailwind CSS, Redux Toolkit | Rendering UI views, client routing, user input validation, global UI state (auth, cart, wishlist, notifications). | ❌ Never store private secrets (`RAZORPAY_KEY_SECRET`), no direct DB calls. |
| **REST API Layer** | Spring Boot `@RestController` | HTTP endpoint exposure, JSON serialization, parameter sanitization, Jakarta Bean Validation (`@Valid`). | ❌ No business calculations or direct SQL queries. |
| **Security & Filters** | `AuthFilter`, `RoleFilter`, `WebMvcConfig` | CORS header evaluation, preflight `OPTIONS` handling, role authorization (`ROLE_ADMIN`), structured 401/403 responses. | ❌ No business logic or state modification. |
| **Business Service Layer** | Spring `@Service` POJOs | ACID transaction management (`@Transactional`), authoritative inventory reservation, order placement, Razorpay HMAC-SHA256 signature verification. | ❌ No UI dependencies, no raw HTTP response writing. |
| **Data Access Layer** | Spring Data JPA / JDBC DAOs | Query execution, PostgreSQL connection acquisition via HikariCP, entity mapping. | ❌ No business rule validation. |
| **Database** | PostgreSQL 16+ | ACID transactional state, foreign key integrity, row-level locks on stock movements. | — |

---

## 3. End-to-End Order Placement & Payment Flow

```mermaid
sequenceDiagram
    autonumber
    actor Customer as User
    participant React as React SPA (CheckoutPage)
    participant API as Spring Boot (/api/orders & /api/payments)
    participant Razorpay as Razorpay Gateway
    participant DB as PostgreSQL

    Customer->>React: Selects address, clicks "Pay with Razorpay"
    React->>API: POST /api/orders/checkout {addressId, paymentMethod: 'RAZORPAY'}
    API->>DB: Atomically reserve stock, save Order (status: PENDING)
    API-->>React: 200 OK (Order ID, Order Number)
    React->>API: POST /api/payments/initiate/{orderId}
    API->>Razorpay: Create Order (amountInPaise, currency: INR)
    Razorpay-->>API: razorpay_order_id
    API-->>React: 200 OK {razorpayOrderId, keyId, amount}
    React->>Customer: Opens Razorpay Checkout Modal
    Customer->>Razorpay: Authorizes Payment (Card / UPI / NetBanking)
    Razorpay-->>React: payment_id, order_id, signature
    React->>API: POST /api/payments/verify {orderId, razorpayPaymentId, razorpaySignature}
    API->>API: Verify HMAC-SHA256 signature with RAZORPAY_KEY_SECRET
    API->>DB: Update Payment (PAID) & Order (CONFIRMED), finalize stock
    API-->>React: 200 OK {success: true}
    React->>Customer: Redirects to /order-confirmation/:id
```
