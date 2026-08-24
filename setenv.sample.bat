@echo off
REM ==============================================================================
REM ShopKart Production Environment Variables (Windows Tomcat)
REM Copy this file to your Tomcat bin folder as: <TOMCAT_HOME>\bin\setenv.bat
REM ==============================================================================

REM 1. Database Connection (SQL Server or PostgreSQL)
set "DB_URL=jdbc:sqlserver://localhost:1433;databaseName=shopkart_db;encrypt=true;trustServerCertificate=true"
set "DB_USER=sa"
set "DB_PASSWORD=YourStrongPassword123!"

REM 2. SMS Gateway API Key (Fast2SMS for OTPs & Password Resets)
REM Get free key from: https://www.fast2sms.com -> Dev API
REM If left empty, system automatically runs in SMS Simulation Mode (logs OTP to console)
set "FAST2SMS_API_KEY=YOUR_FAST2SMS_API_KEY_HERE"

REM 3. Carrier Logistics Webhook Secret
REM Set any random secure string here and configure the SAME value in your Carrier Dashboards
REM (Shiprocket: Settings > Webhooks, Delhivery: Contact support, BlueDart: NetConnect portal)
set "LOGISTICS_WEBHOOK_SECRET=sec_wh_shopkart_prod_2026_x89a"

REM 6. Shiprocket API Credentials (Multi-Carrier Aggregator)
REM Get from: https://app.shiprocket.in -> Settings -> API -> Generate Token
REM Option A: Pre-generated JWT Bearer Token (recommended for production)
set "SHIPROCKET_AUTH_TOKEN=YOUR_SHIPROCKET_JWT_TOKEN_HERE"
REM Option B: Email/Password for auto-login (used if AUTH_TOKEN is not set)
set "SHIPROCKET_EMAIL=your_shiprocket_registered@email.com"
set "SHIPROCKET_PASSWORD=your_shiprocket_password_here"

REM 7. Delhivery API Credentials
REM Get from: https://app.delhivery.com -> Tools & APIs -> API Integration
REM Contact your Delhivery account manager for API key provisioning.
set "DELHIVERY_API_KEY=YOUR_DELHIVERY_API_KEY_HERE"

REM 8. BlueDart NetConnect API Credentials
REM Get from: https://netconnect.bluedart.com -> Account -> API Integration
REM Requires a BlueDart corporate account. Contact BlueDart sales for onboarding.
set "BLUEDART_LOGIN_ID=YOUR_BLUEDART_LOGIN_ID"
set "BLUEDART_LICENSE_KEY=YOUR_BLUEDART_LICENSE_KEY"
set "BLUEDART_CUSTOMER_CODE=YOUR_BLUEDART_CUSTOMER_CODE"

REM 4. Payment Gateway (Razorpay)
set "RAZORPAY_KEY_ID=rzp_live_your_actual_key_id"
set "RAZORPAY_KEY_SECRET=your_actual_razorpay_secret"

REM 5. HTTPS & SSL Enforcement
set "ENFORCE_HTTPS=true"

echo [ShopKart] Production Environment Variables loaded successfully.
