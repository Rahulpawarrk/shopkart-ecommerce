# ==============================================================================
# Multi-Stage Dockerfile for ShopKart E-Commerce Platform (Spring Boot 3 + JDK 21)
# Optimized for 1-Click Production Cloud Deployment on Render, Koyeb, Railway, or Docker
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build the Spring Boot executable WAR artifact
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy dependency descriptors first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy full application source code
COPY src ./src

# Compile and package production Spring Boot executable WAR archive
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2: Production Container Runtime (Lightweight JRE 21)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Security Hardening: Create dedicated non-root application user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled Spring Boot executable WAR artifact
COPY --from=builder --chown=appuser:appgroup /app/target/ecommerce-web.war /app/ecommerce-web.war

# Expose default HTTP port
EXPOSE 8080

USER appuser

# Spring Boot production runtime flags: Enforces IST, container memory ergonomics, and port binding
ENTRYPOINT ["sh", "-c", "java -Duser.timezone=Asia/Kolkata -Dserver.port=${PORT:-8080} -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -jar /app/ecommerce-web.war"]
