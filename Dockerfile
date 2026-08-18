# ==============================================================================
# Multi-Stage Dockerfile for ShopKart E-Commerce Web Application (Tomcat 11 + JDK 21)
# Optimized for 1-Click Free Cloud Deployment on Render, Koyeb, Railway, or Docker
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

# Create dynamic entrypoint script to adapt Tomcat port to cloud platform $PORT (Render / Koyeb)
RUN printf '#!/bin/sh\n\
if [ -n "$PORT" ]; then\n\
  sed -i "s/port=\"8080\"/port=\"$PORT\"/g" conf/server.xml\n\
fi\n\
exec catalina.sh run\n' > /usr/local/tomcat/bin/docker-entrypoint.sh && \
chmod +x /usr/local/tomcat/bin/docker-entrypoint.sh

# Expose default HTTP port
EXPOSE 8080

# Start Tomcat via dynamic entrypoint
CMD ["/usr/local/tomcat/bin/docker-entrypoint.sh"]
