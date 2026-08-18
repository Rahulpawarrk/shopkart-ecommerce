package com.example.ecommerce.health.model;

import java.time.LocalDateTime;

/**
 * Model representing runtime system health, memory usage, and database status.
 */
public class SystemHealth {

    private String appName;
    private String appVersion;
    private String javaVersion;
    private String servletVersion;
    private String osName;
    private LocalDateTime timestamp;

    // JVM Stats
    private long totalMemoryMb;
    private long freeMemoryMb;
    private long maxMemoryMb;
    private long usedMemoryMb;
    private int availableProcessors;

    // Database Status
    private boolean databaseConnected;
    private String databaseProductName;
    private String databaseProductVersion;
    private String databaseUrl;
    private String databaseErrorMessage;

    public SystemHealth() {
        this.timestamp = LocalDateTime.now();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getServletVersion() {
        return servletVersion;
    }

    public void setServletVersion(String servletVersion) {
        this.servletVersion = servletVersion;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getTotalMemoryMb() {
        return totalMemoryMb;
    }

    public void setTotalMemoryMb(long totalMemoryMb) {
        this.totalMemoryMb = totalMemoryMb;
    }

    public long getFreeMemoryMb() {
        return freeMemoryMb;
    }

    public void setFreeMemoryMb(long freeMemoryMb) {
        this.freeMemoryMb = freeMemoryMb;
    }

    public long getMaxMemoryMb() {
        return maxMemoryMb;
    }

    public void setMaxMemoryMb(long maxMemoryMb) {
        this.maxMemoryMb = maxMemoryMb;
    }

    public long getUsedMemoryMb() {
        return usedMemoryMb;
    }

    public void setUsedMemoryMb(long usedMemoryMb) {
        this.usedMemoryMb = usedMemoryMb;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public boolean isDatabaseConnected() {
        return databaseConnected;
    }

    public void setDatabaseConnected(boolean databaseConnected) {
        this.databaseConnected = databaseConnected;
    }

    public String getDatabaseProductName() {
        return databaseProductName;
    }

    public void setDatabaseProductName(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }

    public String getDatabaseProductVersion() {
        return databaseProductVersion;
    }

    public void setDatabaseProductVersion(String databaseProductVersion) {
        this.databaseProductVersion = databaseProductVersion;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseErrorMessage() {
        return databaseErrorMessage;
    }

    public void setDatabaseErrorMessage(String databaseErrorMessage) {
        this.databaseErrorMessage = databaseErrorMessage;
    }
}
