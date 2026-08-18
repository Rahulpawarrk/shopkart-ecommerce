# Security Architecture & Defensive Engineering

## 1. Threat Mitigation Matrix

| Security Threat | Vulnerability Mechanism | Implemented Defense Mechanism |
|---|---|---|
| **SQL Injection (SQLi)** | Concatenating dynamic strings into SQL queries. | 100% parameterized queries using JDBC `PreparedStatement` with typed parameter setters. String concatenation in SQL statements is strictly prohibited. |
| **Cross-Site Scripting (XSS)** | Echoing unsanitized user input into HTML DOM. | Context-aware output encoding via JSTL `<c:out value="${...}" />` and HTML attribute sanitization across all JSP views. |
| **Broken Authentication** | Weak hashing or plaintext passwords in DB. | Passwords hashed using **BCrypt** with salt work factor of 10 (`jbcrypt`). Plaintext passwords are never logged, persisted, or returned in models. |
| **Broken Access Control** | Unauthorized access to admin or customer resources. | Declarative Servlet Filter chain (`AuthFilter` + `RoleFilter`). Server-side ownership verification on all order/address mutations. |
| **Session Hijacking / Fixation** | Insecure session tokens or session reuse after auth. | `HttpSession.invalidate()` on logout and session rotation upon login. Secure cookie flags: `HttpOnly=true`, `SameSite=Lax`, and `Secure` (for HTTPS). |
| **Negative Stock Exploits** | Concurrent race conditions when stock is depleted. | Pessimistic SQL Server row locking (`WITH (UPDLOCK, ROWLOCK)`) and database `CHECK (quantity >= 0)` constraints. |
| **Price Tampering** | Submitting manipulated prices via form fields. | Never trusting client prices or discounts. All item prices, taxes, and discounts are freshly fetched from database during order calculation. |

---

## 2. Password Security with BCrypt

Passwords are never stored in plaintext. Password hashing is centralized in `PasswordUtil.java`:

```java
public static String hashPassword(String plainPassword) {
    if (plainPassword == null || plainPassword.trim().isEmpty()) {
        throw new ValidationException("Password cannot be empty");
    }
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
}

public static boolean checkPassword(String plainPassword, String hashedPassword) {
    if (plainPassword == null || hashedPassword == null) {
        return false;
    }
    return BCrypt.checkpw(plainPassword, hashedPassword);
}
```

---

## 3. Session Configuration (`web.xml`)

```xml
<session-config>
    <session-timeout>30</session-timeout> <!-- 30 Minutes Inactivity Expiry -->
    <cookie-config>
        <http-only>true</http-only>     <!-- Prevents JavaScript document.cookie access -->
        <secure>false</secure>          <!-- Set to true in Production with TLS/HTTPS -->
        <same-site>Lax</same-site>      <!-- Protects against Cross-Site Request Forgery -->
    </cookie-config>
    <tracking-mode>COOKIE</tracking-mode>
</session-config>
```

---

## 4. Role-Based Access Control (RBAC) Architecture

- **`AuthFilter`**: Intercepts private routes (`/profile`, `/orders`, `/cart`, `/checkout`, `/addresses`, `/admin/*`). Verifies `session.getAttribute("currentUser") != null`.
- **`RoleFilter`**: Intercepts `/admin/*`. Verifies `((UserSession) session.getAttribute("currentUser")).isAdmin()`. If not authorized, returns **HTTP 403 Forbidden** and renders `403.jsp`.
- **Data-Level Ownership Check**: Even when authenticated as a customer, services verify that the requested order/address ID belongs to `userSession.getUserId()`. If a user attempts to fetch another customer's order, a `SecurityAlert` is logged and `AccessDeniedException` is thrown.
