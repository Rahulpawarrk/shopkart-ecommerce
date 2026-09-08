# ShopKart — Deployment & Environment Setup Guide

## 1. Prerequisites

- **Operating System**: Windows 10/11, Linux, or macOS
- **JDK**: Java 21 LTS (`JAVA_HOME` configured)
- **Node.js**: Node 20 LTS or later with `npm`
- **Database**: PostgreSQL 15+ (Local or Cloud PostgreSQL like Neon/Render)
- **Build Tools**: Apache Maven 3.9+

---

## 2. PostgreSQL Database Setup

1. Connect to your PostgreSQL instance using `psql` or **pgAdmin**:
   ```bash
   psql -U postgres
   ```
2. Create the database:
   ```sql
   CREATE DATABASE ecommerce_db;
   ```
3. Run the schema and seed scripts:
   ```bash
   psql -U postgres -d ecommerce_db -f src/main/resources/db/schema.sql
   psql -U postgres -d ecommerce_db -f src/main/resources/db/seed.sql
   ```

---

## 3. Environment Configuration

Spring Boot reads externalized configuration from environment variables or `application.properties`:

| Variable | Description | Default |
|---|---|---|
| `PORT` | HTTP server port | `8080` |
| `DB_URL` | JDBC Connection URL | `jdbc:postgresql://localhost:5432/ecommerce_db` |
| `DB_USER` | PostgreSQL Username | `postgres` |
| `DB_PASSWORD` | PostgreSQL Password | *(blank)* |
| `RAZORPAY_KEY_ID` | Razorpay Public Key ID | `rzp_test_...` |
| `RAZORPAY_KEY_SECRET` | Razorpay Secret Key | `...` |

---

## 4. Local Development Execution

### Backend (Spring Boot)
```bash
# In the repository root
mvn clean test
mvn spring-boot:run
```
Backend will start on `http://localhost:8080`.

### Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
Frontend development server will start on `http://localhost:5173` with automatic API proxying to port `8080`.

---

## 5. Production Deployment on Render

### Architecture on Render
1. **Managed PostgreSQL**:
   - Create a **PostgreSQL** database on Render (Name: `ecommerce-db`).
   - Copy the Internal Database URL.
2. **Spring Boot Web Service**:
   - Create a **Web Service** on Render connected to this GitHub repo.
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -Xmx384m -jar target/ecommerce-web.war` (or Docker)
   - Add Environment Variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`.
3. **React Static Site**:
   - Create a **Static Site** on Render connected to this GitHub repo.
   - Root Directory: `frontend`
   - Build Command: `npm install && npm run build`
   - Publish Directory: `dist`
   - Add Rewrite Rule: Source `/*` -> Destination `/index.html` (for SPA routing).

---

## 6. Default Credentials

| Role | Email | Default Password | Access Scope |
|---|---|---|---|
| **System Administrator** | `admin@ecommerce.com` | `Admin@123` | Full Admin Console (`/admin/*`) & Storefront |
| **Test Customer** | `customer@ecommerce.com` | `Admin@123` | Customer Storefront, Cart, Checkout, Profile |
