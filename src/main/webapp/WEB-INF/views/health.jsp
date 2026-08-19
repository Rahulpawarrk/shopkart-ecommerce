<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <!-- Favicon -->
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.svg">
    <link rel="apple-touch-icon" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>System Health & Diagnostics | E-Commerce Platform</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>

    <!-- Header Navigation -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/" class="brand">
            <span>🛒</span> Enterprise E-Commerce
        </a>
        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/">Home</a></li>
            <li><a href="${pageContext.request.contextPath}/health" style="color: var(--primary-color);">Health Diagnostics</a></li>
            <li><a href="${pageContext.request.contextPath}/api/health" target="_blank">JSON Health API</a></li>
        </ul>
    </header>

    <main class="container">
        <!-- Header Title Banner -->
        <div class="card" style="border-left: 5px solid var(--primary-color);">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h1 style="font-size: 1.5rem; color: var(--text-primary);">System Health & Runtime Diagnostics</h1>
                    <p style="color: var(--text-secondary); margin-top: 0.25rem;">
                        Live status check across Web Container (Tomcat 11), JVM, and Microsoft SQL Server Database.
                    </p>
                </div>
                <div>
                    <c:choose>
                        <c:when test="${health.databaseConnected}">
                            <span class="badge badge-success">● SYSTEM HEALTHY</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-danger">● DEGRADED STATE</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- Metric Stat Cards -->
        <div class="grid grid-cols-4" style="margin-bottom: 1.5rem;">
            <div class="stat-card">
                <div class="stat-label">Database Status</div>
                <div class="stat-value" style="font-size: 1.25rem;">
                    <c:choose>
                        <c:when test="${health.databaseConnected}">
                            <span style="color: var(--success-color);">Connected ✓</span>
                        </c:when>
                        <c:otherwise>
                            <span style="color: var(--danger-color);">Disconnected ✗</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-label">Used Memory</div>
                <div class="stat-value">
                    ${health.usedMemoryMb} <span style="font-size: 0.9rem; font-weight: normal; color: var(--text-secondary);">MB</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-label">Free Memory</div>
                <div class="stat-value">
                    ${health.freeMemoryMb} <span style="font-size: 0.9rem; font-weight: normal; color: var(--text-secondary);">MB</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-label">Max Allocatable</div>
                <div class="stat-value">
                    ${health.maxMemoryMb} <span style="font-size: 0.9rem; font-weight: normal; color: var(--text-secondary);">MB</span>
                </div>
            </div>
        </div>

        <!-- Detailed Diagnostics Tables -->
        <div class="grid grid-cols-2">
            
            <!-- Database Health Card -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title">Microsoft SQL Server Diagnostics</h2>
                    <c:if test="${health.databaseConnected}">
                        <span class="badge badge-success">Active Connection</span>
                    </c:if>
                </div>

                <div class="table-responsive">
                    <table class="data-table">
                        <tbody>
                            <tr>
                                <th>Database Engine</th>
                                <td><c:out value="${health.databaseProductName != null ? health.databaseProductName : 'Microsoft SQL Server'}" /></td>
                            </tr>
                            <tr>
                                <th>Database Version</th>
                                <td><c:out value="${health.databaseProductVersion != null ? health.databaseProductVersion : 'N/A'}" /></td>
                            </tr>
                            <tr>
                                <th>Connection Pool</th>
                                <td>HikariCP (EcommerceHikariPool)</td>
                            </tr>
                            <tr>
                                <th>JDBC Target URL</th>
                                <td><code style="font-size: 0.8rem; word-break: break-all;"><c:out value="${health.databaseUrl != null ? health.databaseUrl : 'Configured via db.properties'}" /></code></td>
                            </tr>
                            <c:if test="${not empty health.databaseErrorMessage}">
                                <tr>
                                    <th>Error Message</th>
                                    <td style="color: var(--danger-color); font-weight: 500;"><c:out value="${health.databaseErrorMessage}" /></td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Server & Environment Card -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title">Container & Environment Info</h2>
                    <span class="badge badge-info">Runtime Info</span>
                </div>

                <div class="table-responsive">
                    <table class="data-table">
                        <tbody>
                            <tr>
                                <th>Application Name</th>
                                <td><strong><c:out value="${health.appName}" /></strong></td>
                            </tr>
                            <tr>
                                <th>Servlet Container</th>
                                <td><c:out value="${health.servletVersion}" /></td>
                            </tr>
                            <tr>
                                <th>Java Runtime</th>
                                <td>JDK <c:out value="${health.javaVersion}" /></td>
                            </tr>
                            <tr>
                                <th>Operating System</th>
                                <td><c:out value="${health.osName}" /></td>
                            </tr>
                            <tr>
                                <th>CPU Cores Available</th>
                                <td>${health.availableProcessors} Cores</td>
                            </tr>
                            <tr>
                                <th>Server Timestamp</th>
                                <td>${health.timestamp}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>

        <div style="margin-top: 1.5rem; text-align: center;">
            <a href="${pageContext.request.contextPath}/health" class="btn btn-primary">Refresh Diagnostics</a>
            <a href="${pageContext.request.contextPath}/" class="btn btn-secondary" style="margin-left: 0.5rem;">Return to Homepage</a>
        </div>
    </main>

    <footer style="text-align: center; padding: 2rem; color: #64748b; font-size: 0.85rem; border-top: 1px solid #e2e8f0; margin-top: 4rem; background: #ffffff;">
        <div style="margin-bottom: 0.35rem; color: #334155; font-weight: 600;">
            &copy; 2026 ShopKart Enterprise Platform Diagnostics &bull; All rights reserved.
        </div>
        <div style="font-size: 0.82rem; color: #64748b;">
            Designed, Developed &amp; Managed by <strong style="color: #2563eb; font-weight: 800;">Rahul Pawar</strong>
        </div>
    </footer>

</body>
</html>
