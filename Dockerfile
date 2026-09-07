# ==============================================================================
# Multi-Stage Dockerfile for ShopKart E-Commerce Platform (Tomcat 11 + JDK 21)
# Optimized for 1-Click Production Cloud Deployment on Render, Koyeb, Railway, or Docker
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build the Maven WAR artifact
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy dependency descriptors first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy full application source code
COPY src ./src

# Compile and package production WAR archive
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2: Production Runtime with Apache Tomcat 11
# ------------------------------------------------------------------------------
FROM tomcat:11.0-jdk21-temurin

WORKDIR /usr/local/tomcat

# Remove default sample webapps to maximize security and startup speed
RUN rm -rf webapps/* webapps.dist

# Copy the compiled WAR archive from builder stage directly as ROOT.war
COPY --from=builder /app/target/ecommerce-web.war webapps/ROOT.war

# Copy dynamic entrypoint script to adapt Tomcat port to cloud platform $PORT (Render / Koyeb)
COPY bin/docker-entrypoint.sh /usr/local/tomcat/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/tomcat/bin/docker-entrypoint.sh

# Security Hardening: Create tomcat user if not exists and ensure writable log directory
RUN (id -u tomcat >/dev/null 2>&1 || (groupadd -r tomcat && useradd -r -g tomcat -d /usr/local/tomcat -s /sbin/nologin tomcat)) && \
    mkdir -p /tmp/ecommerce-logs && \
    chown -R tomcat:tomcat /usr/local/tomcat /tmp/ecommerce-logs

# Expose default HTTP port
EXPOSE 8080

USER tomcat

# Start Tomcat via dynamic entrypoint
CMD ["/usr/local/tomcat/bin/docker-entrypoint.sh"]
