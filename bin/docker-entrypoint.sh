#!/bin/sh
set -e

# Disable default shutdown port
sed -i 's/port="8005"/port="-1"/g' conf/server.xml

# Configure dynamic HTTP port binding for Render / Cloud
PORT_TO_USE="${PORT:-8080}"
sed -i "s/<Connector port=\"8080\"/<Connector port=\"$PORT_TO_USE\" address=\"0.0.0.0\"/g" conf/server.xml

# Performance Optimization: Skip scanning 80+ Spring/Hibernate/Driver JARs for TLDs
echo "tomcat.util.scan.StandardJarScanFilter.jarsToSkip=*.jar" >> conf/catalina.properties
echo "tomcat.util.scan.StandardJarScanFilter.jarsToScan=jakarta.servlet.jsp.jstl*.jar" >> conf/catalina.properties

# JVM optimization flags: Timezone IST, non-blocking entropy (/dev/./urandom), container memory ergonomics, IPv4 preference
export CATALINA_OPTS="-Duser.timezone=Asia/Kolkata -Djava.net.preferIPv4Stack=true -Djava.security.egd=file:/dev/./urandom -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 $CATALINA_OPTS"

echo "================================================================"
echo "Starting Apache Tomcat 11 for ShopKart E-Commerce Platform"
echo "Listening on port: $PORT_TO_USE (0.0.0.0)"
echo "Timezone: Asia/Kolkata (IST)"
echo "Optimizations: Pre-exploded ROOT, TLD scan filter enabled, fast entropy"
echo "================================================================"

exec catalina.sh run
