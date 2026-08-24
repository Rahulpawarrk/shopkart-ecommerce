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
    <title>Payment Status | ShopKart India</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        .failure-box {
            background: #ffffff;
            border-radius: 16px;
            border: 1px solid #fee2e2;
            border-top: 6px solid #ef4444;
            box-shadow: 0 16px 36px -8px rgba(239, 68, 68, 0.12);
            padding: 3rem 2rem;
            text-align: center;
        }
        .action-card {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 1.25rem;
            margin: 1.5rem 0;
            text-align: left;
        }
        .btn-retry {
            background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
            color: #ffffff;
            font-weight: 800;
            padding: 0.85rem 1.75rem;
            border: none;
            border-radius: 10px;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            box-shadow: 0 4px 12px rgba(2, 132, 199, 0.25);
            font-size: 0.95rem;
            transition: all 0.2s ease;
        }
        .btn-retry:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 16px rgba(2, 132, 199, 0.35);
        }
        .btn-cod {
            background: linear-gradient(135deg, #059669 0%, #047857 100%);
            color: #ffffff;
            font-weight: 800;
            padding: 0.85rem 1.75rem;
            border: none;
            border-radius: 10px;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            box-shadow: 0 4px 12px rgba(5, 150, 105, 0.25);
            font-size: 0.95rem;
            transition: all 0.2s ease;
        }
        .btn-cod:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 16px rgba(5, 150, 105, 0.35);
        }
    </style>
</head>
<body style="background: #f8fafc;">

    <!-- 1. TOP ANNOUNCEMENT TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <span class="ticker-badge">⚠️ Transaction Notice</span>
            <span>Online payment was not completed. Your items are safe and you can retry or switch payment mode.</span>
        </div>
        <div class="ticker-links">
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
                <input type="text" name="keyword" class="search-input" placeholder="Search products, brands..." autocomplete="off">
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

    <main class="container" style="max-width: 680px; margin: 3rem auto 5rem; padding: 0 1.25rem;">
        
        <div class="failure-box">
            <div style="width: 72px; height: 72px; border-radius: 50%; background: #fee2e2; color: #dc2626; font-size: 2.25rem; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem; box-shadow: 0 8px 16px rgba(220, 38, 38, 0.15);">
                ✕
            </div>
            <h1 style="font-size: 1.85rem; font-weight: 900; color: #0f172a; margin-bottom: 0.5rem; letter-spacing: -0.02em;">
                Payment Incomplete
            </h1>
            <p style="color: var(--text-muted); margin-bottom: 1.5rem; font-size: 1rem; line-height: 1.5; max-width: 520px; margin-left: auto; margin-right: auto;">
                We were unable to verify your payment with the gateway. No funds were captured for this attempt.
            </p>

            <c:if test="${not empty error}">
                <div style="background-color: #fef2f2; color: #991b1b; padding: 1rem 1.25rem; border-radius: 10px; margin-bottom: 1.5rem; border: 1px solid #fca5a5; font-size: 0.92rem; font-weight: 600; text-align: left; display: flex; gap: 0.6rem; align-items: flex-start;">
                    <span>⚠️</span>
                    <div>
                        <strong>Reason:</strong> <c:out value="${error}" />
                    </div>
                </div>
            </c:if>

            <div class="action-card">
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.6rem; font-size: 0.92rem;">
                    <span style="color: var(--text-muted);">Payment Channel:</span>
                    <strong style="color: #0f172a;"><c:out value="${not empty failureContext.paymentMethod ? failureContext.paymentMethod : order.paymentMethod}" default="ONLINE" /></strong>
                </div>
                <div style="display: flex; justify-content: space-between; border-top: 1px dashed #cbd5e1; padding-top: 0.6rem; font-size: 1.05rem;">
                    <span style="color: var(--text-muted); font-weight: 600;">Total Payable:</span>
                    <strong style="color: #2563eb; font-size: 1.3rem; font-weight: 900;">₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2" /></strong>
                </div>
            </div>

            <!-- Action Options Form (Clean POST actions, zero address bar leakage) -->
            <div style="display: flex; flex-direction: column; gap: 1rem; margin-top: 2rem;">
                
                <!-- Option 1: 1-Click Retry Payment -->
                <form action="${pageContext.request.contextPath}/payment/failure/action" method="POST" style="margin: 0;">
                    <input type="hidden" name="action" value="retry">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="btn-retry" style="width: 100%; justify-content: center;">
                        🔄 Retry Payment with Razorpay (UPI / Card / NetBanking)
                    </button>
                </form>

                <!-- Option 2: 1-Click Switch to Cash on Delivery -->
                <form action="${pageContext.request.contextPath}/payment/failure/action" method="POST" style="margin: 0;">
                    <input type="hidden" name="action" value="switch_cod">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="btn-cod" style="width: 100%; justify-content: center;">
                        💵 Switch to Cash on Delivery (COD) &amp; Confirm Order
                    </button>
                </form>

                <div style="display: flex; gap: 1rem; justify-content: center; margin-top: 0.5rem; flex-wrap: wrap;">
                    <a href="${pageContext.request.contextPath}/cart" class="order-btn-secondary" style="padding: 0.65rem 1.5rem; text-decoration: none; border-radius: 8px; font-weight: 700; font-size: 0.9rem;">
                        🛒 Return to Cart
                    </a>
                    <a href="${pageContext.request.contextPath}/orders" class="order-btn-secondary" style="padding: 0.65rem 1.5rem; text-decoration: none; border-radius: 8px; font-weight: 700; font-size: 0.9rem;">
                        📦 My Orders
                    </a>
                </div>
            </div>

            <!-- Support note -->
            <div style="margin-top: 2rem; padding-top: 1.25rem; border-top: 1px solid #f1f5f9; font-size: 0.82rem; color: #94a3b8; line-height: 1.5;">
                Need help with your payment? If your account was debited, your bank will automatically reverse the transaction within 24-48 business hours. For immediate assistance, contact <b>ShopKart 24x7 Priority Support</b>.
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
