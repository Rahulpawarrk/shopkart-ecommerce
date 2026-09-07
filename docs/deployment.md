# Deployment & Environment Setup Guide

## 1. Prerequisites

- **Operating System**: Windows 10/11, Linux, or macOS
- **JDK**: Java 21 LTS or Java 25 (Set `JAVA_HOME`)
- **Web Server**: Apache Tomcat 11.0.x (Jakarta EE 11 compatible)
- **Database**: Microsoft SQL Server 2019 / 2022 / Azure SQL Database
- **Build Tool**: Apache Maven 3.9+

---

## 2. Microsoft SQL Server Setup

1. Open **SQL Server Management Studio (SSMS)** or `sqlcmd`.
2. Execute the schema generation script:
   ```bash
   sqlcmd -S localhost -U  -P  -i src/main/resources/db/schema.sql
   ```
3. Execute the seed data script:
   ```bash
   sqlcmd -S localhost -U  -P  -i src/main/resources/db/seed.sql
   ```
4. Verify database creation:
   ```sql
   USE ecommerce_db;
   SELECT COUNT(*) FROM dbo.products;
   SELECT COUNT(*) FROM dbo.users;
   ```

---

## 3. Database Connection Configuration

Open `src/main/resources/db.properties` and adjust your database connection credentials:

```properties
db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
db.url=jdbc:sqlserver://localhost:1433;databaseName=ecommerce_db;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=true;

hikaricp.poolName=EcommerceHikariPool
hikaricp.maximumPoolSize=20
hikaricp.minimumIdle=5
```

---

## 4. Building the Application (WAR Package)

Run the Maven clean package command in the root project directory:

```bash
mvn clean package
```

This compiles all Java source files, executes the 52 unit tests, and packages the application as a standalone WAR file located at:
```
target/ecommerce-web.war
```

---

## 5. Deploying to Apache Tomcat 11

### Manual Deployment:
1. Copy `target/ecommerce-web.war` to the Tomcat `webapps/` folder:
   ```bash
   copy target\ecommerce-web.war %CATALINA_HOME%\webapps\ROOT.war
   ```
   *(Renaming to `ROOT.war` serves the app directly at `http://localhost:8080/`)*.
2. Start Tomcat:
   ```bash
   %CATALINA_HOME%\bin\startup.bat
   ```
3. Open your browser and navigate to:
   - Storefront: `http://localhost:8080/`
   - Health Diagnostic: `http://localhost:8080/health`
   - Admin Console: `http://localhost:8080/admin/dashboard`

---

## 6. Default Seed Credentials

| Role | Email | Default Password | Access Scope |
|---|---|---|---|
| **System Administrator** | `admin@ecommerce.com` | `Admin@123` | Full Admin Console (`/admin/*`) & Storefront |
| **Test Customer** | `customer@ecommerce.com` | `Admin@123` | Customer Storefront, Cart, Checkout, Profile |
