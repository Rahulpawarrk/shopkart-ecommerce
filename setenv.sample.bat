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
REM Set any random secure string here and in your Carrier Dashboard (e.g. Delhivery, Shiprocket)
set "LOGISTICS_WEBHOOK_SECRET=sec_wh_shopkart_prod_2026_x89a"

REM 4. Payment Gateway (Razorpay)
set "RAZORPAY_KEY_ID=rzp_live_your_actual_key_id"
set "RAZORPAY_KEY_SECRET=your_actual_razorpay_secret"

REM 5. HTTPS & SSL Enforcement
set "ENFORCE_HTTPS=true"

echo [ShopKart] Production Environment Variables loaded successfully.
