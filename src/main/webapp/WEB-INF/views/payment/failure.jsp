<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Failed | ShopKart India</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body style="background: #f8fafc;">

    <!-- 1. TOP ANNOUNCEMENT TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <span class="ticker-badge">⚠️ Payment Notice</span>
            <span>Online payment was not completed. Your order has not been placed.</span>
        </div>
        <div class="ticker-links">
            <a href="${pageContext.request.contextPath}/orders">My Orders</a>
            <a href="${pageContext.request.contextPath}/cart">Back to Cart</a>
        </div>
    </header>

    <!-- 2. MAIN HEADER -->
    <nav class="main-header">
        <div class="brand-group">
            <a href="${pageContext.request.contextPath}/" class="brand-logo" title="ShopKart - Premier Online Shopping">
                <img src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="ShopKart" class="logo-img">
            </a>
        </div>

        <div class="header-search-wrapper">
            <form action="${pageContext.request.contextPath}/products" method="GET" class="header-search-form">
                <input type="text" name="keyword" class="search-input" placeholder="Search for products..." autocomplete="off">
                <button type="submit" class="search-button">🔍</button>
            </form>
        </div>

        <div class="header-actions">
            <a href="${pageContext.request.contextPath}/orders" class="header-action-inline" title="My Orders">
                <span class="badge-icon">📦</span>
                <span>My Orders</span>
            </a>
            <a href="${pageContext.request.contextPath}/cart" class="header-action-inline" title="Cart">
                <div class="cart-icon-wrapper">
                    <span class="badge-icon">🛒</span>
                </div>
                <span>Cart</span>
            </a>
        </div>
    </nav>

    <main class="container" style="max-width: 620px; margin: 3rem auto 4rem; padding: 0 1rem;">
        
        <div class="order-card-wrapper" style="text-align: center; border-top: 5px solid #dc2626; padding: 3rem 2rem; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.06); background: #ffffff;">
            <div style="font-size: 3.5rem; margin-bottom: 1rem; color: #ef4444;">❌</div>
            <h1 style="font-size: 1.75rem; font-weight: 900; color: #0f172a; margin-bottom: 0.5rem;">
                Payment Failed &mdash; Order Not Placed
            </h1>
            <p style="color: var(--text-muted); margin-bottom: 1.5rem; font-size: 0.95rem; line-height: 1.5;">
                We were unable to process your payment for Order Reference <strong><c:out value="${order.orderNumber}" /></strong>. Your order has not been placed.
            </p>

            <c:if test="${not empty error}">
                <div style="background-color: #fee2e2; color: #991b1b; padding: 0.85rem 1.25rem; border-radius: 8px; margin-bottom: 1.5rem; border: 1px solid #fca5a5; font-size: 0.9rem; font-weight: 600; text-align: left;">
                    ⚠️ Reason: <c:out value="${error}" />
                </div>
            </c:if>

            <div style="background: #f8fafc; border: 1px solid var(--border-color); border-radius: 12px; padding: 1.25rem; margin-bottom: 2rem; text-align: left; font-size: 0.95rem;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
                    <span style="color: var(--text-muted);">Order Reference:</span>
                    <strong><c:out value="${order.orderNumber}" /></strong>
                </div>
                <div style="display: flex; justify-content: space-between; border-top: 1px dashed #e2e8f0; padding-top: 0.5rem;">
                    <span style="color: var(--text-muted);">Amount Due:</span>
                    <strong style="color: #0f172a; font-size: 1.15rem; font-weight: 900;">₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2" /></strong>
                </div>
            </div>

            <div style="display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/payment/gateway?orderId=${order.orderId}&paymentFailed=true" class="hero-cta-btn" style="padding: 0.85rem 1.75rem; text-decoration: none; border-radius: 10px; font-weight: 800;">
                    🔄 Retry Payment Now
                </a>
                <a href="${pageContext.request.contextPath}/orders" class="order-btn-outline" style="padding: 0.85rem 1.5rem; text-decoration: none; border-radius: 10px; font-weight: 700;">
                    View Orders
                </a>
            </div>
        </div>

    </main>

    <!-- FOOTER -->
    <footer style="background-color: var(--amazon-dark, #0f172a); color: #cbd5e1; margin-top: auto; padding: 2.5rem 2rem 2rem; border-top: 1px solid var(--amazon-subnav, #1e293b); text-align: center;">
        <div style="max-width: 1200px; margin: 0 auto; display: flex; flex-direction: column; align-items: center; gap: 0.6rem;">
            <div style="font-size: 0.9rem; font-weight: 700; color: #f8fafc; display: flex; align-items: center; gap: 0.4rem;">
                <span>🛒</span> <span>ShopKart Enterprise E-Commerce Platform</span>
            </div>
            <div style="font-size: 0.82rem; color: #94a3b8;">
                &copy; 2026 ShopKart Inc. All rights reserved. &bull; 100% Purchase Protection &bull; Safe &amp; Secure Delivery
            </div>
            <div style="margin-top: 0.4rem; padding-top: 0.75rem; border-top: 1px solid rgba(255, 255, 255, 0.08); width: 100%; max-width: 600px; font-size: 0.85rem; color: #cbd5e1;">
                Designed, Developed &amp; Managed by <strong style="color: #38bdf8; font-weight: 800; letter-spacing: 0.02em;">Rahul Pawar</strong>
            </div>
        </div>
    </footer>

</body>
</html>
