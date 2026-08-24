<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    response.setStatus(404);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <!-- Favicon -->
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.svg">
    <link rel="apple-touch-icon" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="robots" content="noindex, follow">
    <title>404 - Page Not Found | ShopKart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
    <header class="main-header">
        <div class="brand-group">
            <a href="${pageContext.request.contextPath}/" class="brand-logo" title="ShopKart">
                <img src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="ShopKart" class="logo-img">
            </a>
        </div>
    </header>

    <main class="container" style="text-align: center; padding: 4rem 1rem; max-width: 800px; margin: 0 auto;">
        <div class="card" style="padding: 3.5rem 2rem; border-radius: 16px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05);">
            <div style="font-size: 4.5rem; line-height: 1; margin-bottom: 1rem;">🔍</div>
            <h1 style="font-size: 2.2rem; font-weight: 900; color: #0f172a; margin-bottom: 0.5rem;">404 - Page Not Found</h1>
            <p style="color: #64748b; font-size: 1.05rem; margin-bottom: 2rem; max-width: 500px; margin-left: auto; margin-right: auto;">
                The page you're looking for doesn't exist or has been moved. Try searching for products below or exploring top categories.
            </p>

            <!-- Search Form -->
            <form action="${pageContext.request.contextPath}/products" method="GET" style="display: flex; max-width: 500px; margin: 0 auto 2.5rem; gap: 0.5rem;">
                <input type="text" name="q" placeholder="Search smartphones, laptops, electronics..." style="flex: 1; padding: 0.85rem 1.25rem; border: 1.5px solid #cbd5e1; border-radius: 10px; font-size: 0.95rem;" required>
                <button type="submit" style="background: #2563eb; color: #ffffff; border: none; padding: 0.85rem 1.5rem; border-radius: 10px; font-weight: 700; cursor: pointer;">Search</button>
            </form>

            <div style="margin-bottom: 2rem;">
                <h3 style="font-size: 0.95rem; text-transform: uppercase; letter-spacing: 0.05em; color: #94a3b8; font-weight: 700; margin-bottom: 1rem;">Popular Departments</h3>
                <div style="display: flex; flex-wrap: wrap; justify-content: center; gap: 0.75rem;">
                    <a href="${pageContext.request.contextPath}/category/laptops-computers" style="padding: 0.5rem 1rem; background: #f1f5f9; border-radius: 20px; text-decoration: none; color: #1e293b; font-weight: 600; font-size: 0.88rem;">💻 Laptops & Computers</a>
                    <a href="${pageContext.request.contextPath}/category/smartphones-tablets" style="padding: 0.5rem 1rem; background: #f1f5f9; border-radius: 20px; text-decoration: none; color: #1e293b; font-weight: 600; font-size: 0.88rem;">📱 Smartphones</a>
                    <a href="${pageContext.request.contextPath}/category/audio-wearables" style="padding: 0.5rem 1rem; background: #f1f5f9; border-radius: 20px; text-decoration: none; color: #1e293b; font-weight: 600; font-size: 0.88rem;">🎧 Audio & Wearables</a>
                    <a href="${pageContext.request.contextPath}/category/fashion" style="padding: 0.5rem 1rem; background: #f1f5f9; border-radius: 20px; text-decoration: none; color: #1e293b; font-weight: 600; font-size: 0.88rem;">👗 Fashion & Apparel</a>
                </div>
            </div>

            <div>
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary" style="padding: 0.75rem 1.75rem; font-weight: 700; border-radius: 8px; text-decoration: none; display: inline-block;">Return to Homepage</a>
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
