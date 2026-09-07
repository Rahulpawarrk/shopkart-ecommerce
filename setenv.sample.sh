#!/bin/bash
# ==============================================================================
# ShopKart Production Environment Variables (Linux / Docker Tomcat)
# Copy this file to your Tomcat bin folder as: <TOMCAT_HOME>/bin/setenv.sh
# and chmod +x <TOMCAT_HOME>/bin/setenv.sh
# ==============================================================================

# 1. Database Connection (SQL Server or PostgreSQL)
export DB_URL="jdbc:sqlserver://localhost:1433;databaseName=shopkart_db;encrypt=true;trustServerCertificate=true"
export DB_USER="sa"
export DB_PASSWORD="YOUR_DATABASE_PASSWORD_HERE"

# 2. SMS Gateway API Key (Fast2SMS for OTPs & Password Resets)
# Get free key from: https://www.fast2sms.com -> Dev API
# If left empty, system automatically runs in SMS Simulation Mode (logs OTP to console)
export FAST2SMS_API_KEY="YOUR_FAST2SMS_API_KEY_HERE"

# 3. Carrier Logistics Webhook Secret
# Set any random secure string here and configure the SAME value in your Carrier Dashboards
# (Shiprocket: Settings > Webhooks, Delhivery: Contact support, BlueDart: NetConnect portal)
export LOGISTICS_WEBHOOK_SECRET="YOUR_LOGISTICS_WEBHOOK_SECRET_HERE"

# 6. Shiprocket API Credentials (Multi-Carrier Aggregator)
# Get from: https://app.shiprocket.in -> Settings -> API -> Generate Token
# Option A: Pre-generated JWT Bearer Token (recommended for production)
export SHIPROCKET_AUTH_TOKEN="YOUR_SHIPROCKET_JWT_TOKEN_HERE"
# Option B: Email/Password for auto-login (used if AUTH_TOKEN is not set)
export SHIPROCKET_EMAIL="your_shiprocket_registered@email.com"
export SHIPROCKET_PASSWORD="your_shiprocket_password_here"

# 7. Delhivery API Credentials
# Get from: https://app.delhivery.com -> Tools & APIs -> API Integration
# Contact your Delhivery account manager for API key provisioning.
export DELHIVERY_API_KEY="YOUR_DELHIVERY_API_KEY_HERE"

# 8. BlueDart NetConnect API Credentials
# Get from: https://netconnect.bluedart.com -> Account -> API Integration
# Requires a BlueDart corporate account. Contact BlueDart sales for onboarding.
export BLUEDART_LOGIN_ID="YOUR_BLUEDART_LOGIN_ID"
export BLUEDART_LICENSE_KEY="YOUR_BLUEDART_LICENSE_KEY"
export BLUEDART_CUSTOMER_CODE="YOUR_BLUEDART_CUSTOMER_CODE"

# 4. Payment Gateway (Razorpay)
export RAZORPAY_KEY_ID="rzp_test_your_key_id"
export RAZORPAY_KEY_SECRET="your_razorpay_secret"

# 5. HTTPS & SSL Enforcement
export ENFORCE_HTTPS="true"

echo "[ShopKart] Production Environment Variables loaded successfully."
