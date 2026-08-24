<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <!-- Favicon -->
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.svg">
    <link rel="apple-touch-icon" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="robots" content="noindex, nofollow">
    <title>500 - Server Error | ShopKart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/" class="brand">
            <span>🛒</span> Enterprise E-Commerce
        </a>
    </header>

    <main class="container" style="text-align: center; padding-top: 4rem;">
        <div class="card" style="max-width: 650px; margin: 0 auto; padding: 3rem;">
            <div style="font-size: 4rem; color: var(--danger-color); margin-bottom: 1rem;">⚠️</div>
            <h1 style="font-size: 2rem; margin-bottom: 0.5rem;">500 - Internal Server Error</h1>
            <p style="color: var(--text-secondary); margin-bottom: 2rem;">
                An unexpected condition was encountered on the server. The incident has been safely logged internally for our engineering team. Sensitive details and stack traces are suppressed for security.
            </p>
            <div>
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Homepage</a>
                <c:if test="${sessionScope.currentUser.admin}">
                    <a href="${pageContext.request.contextPath}/health" class="btn btn-secondary" style="margin-left: 0.5rem;">Check Status</a>
                </c:if>
            </div>
        </div>
    </main>

    <footer style="text-align: center; padding: 2rem; color: #64748b; font-size: 0.85rem; border-top: 1px solid #e2e8f0; margin-top: auto; background: #ffffff;">
        <div style="margin-bottom: 0.35rem; color: #334155; font-weight: 600;">
            &copy; 2026 ShopKart Inc. &bull; All rights reserved.
        </div>
        <div style="font-size: 0.82rem; color: #64748b;">
            Designed, Developed &amp; Managed by <strong style="color: #2563eb; font-weight: 800;">Rahul Pawar</strong>
        </div>
    </footer>
</body>
</html>
