#!/bin/bash
# ==============================================================================
# ShopKart Production Environment Variables (Linux / Docker Tomcat)
# Copy this file to your Tomcat bin folder as: <TOMCAT_HOME>/bin/setenv.sh
# and chmod +x <TOMCAT_HOME>/bin/setenv.sh
# ==============================================================================

# 1. Database Connection (SQL Server or PostgreSQL)
export DB_URL="jdbc:sqlserver://localhost:1433;databaseName=shopkart_db;encrypt=true;trustServerCertificate=true"
export DB_USER="sa"
export DB_PASSWORD="YourStrongPassword123!"

# 2. SMS Gateway API Key (Fast2SMS for OTPs & Password Resets)
# Get free key from: https://www.fast2sms.com -> Dev API
# If left empty, system automatically runs in SMS Simulation Mode (logs OTP to console)
export FAST2SMS_API_KEY="YOUR_FAST2SMS_API_KEY_HERE"

# 3. Carrier Logistics Webhook Secret
# Set any random secure string here and in your Carrier Dashboard (e.g. Delhivery, Shiprocket)
export LOGISTICS_WEBHOOK_SECRET="sec_wh_shopkart_prod_2026_x89a"

# 4. Payment Gateway (Razorpay)
export RAZORPAY_KEY_ID="rzp_live_your_actual_key_id"
export RAZORPAY_KEY_SECRET="your_actual_razorpay_secret"

# 5. HTTPS & SSL Enforcement
export ENFORCE_HTTPS="true"

echo "[ShopKart] Production Environment Variables loaded successfully."
