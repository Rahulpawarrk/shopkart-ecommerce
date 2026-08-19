<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Confirmed | ShopKart India</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body style="background: #f8fafc;">

    <!-- 1. TOP ANNOUNCEMENT TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <a href="${pageContext.request.contextPath}/products?deals=true" style="color: inherit; text-decoration: none; display: inline-flex; align-items: center; gap: 0.5rem;">
                <span class="ticker-badge">⚡ Safe Checkout</span>
                <span>Grand Festive Carnival: Up to 50% Off Top Brands + Extra 20% Off with Code <strong>SAVE20</strong> &bull; Explore Deals &rarr;</span>
            </a>
        </div>
        <div class="ticker-links">
            <c:if test="${sessionScope.currentUser.admin}">
                <a href="${pageContext.request.contextPath}/admin/dashboard" style="color: #fbbf24; font-weight: 700;">⚙️ Admin Console</a>
                <a href="${pageContext.request.contextPath}/health" style="color: #38bdf8; font-weight: 700;">🩺 System Health</a>
            </c:if>
            <a href="${pageContext.request.contextPath}/products?brand=Apple">Brand Store</a>
        </div>
    </header>

    <!-- 2. MAIN HEADER -->
    <nav class="main-header">
        <div class="brand-group">
            <a href="${pageContext.request.contextPath}/" class="brand-logo" title="ShopKart - Premier Online Shopping">
                <img src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="ShopKart" class="logo-img">
            </a>
            <!-- Delivery Locator (Pin Code Trigger) -->
            <div class="delivery-locator" id="headerDeliveryTrigger" onclick="openPinCodeModal()" title="Change delivery location">
                <span class="loc-icon">📍</span>
                <div class="loc-text">
                    <span class="sub">Deliver to</span>
                    <span class="main" id="headerPincodeText">Satara 415312</span>
                </div>
            </div>
        </div>

        <!-- Global Search Bar with Live Autocomplete -->
        <div class="header-search-wrapper">
            <form action="${pageContext.request.contextPath}/products" method="GET" class="header-search-form" id="headerSearchForm">
                <input type="text" name="keyword" class="search-input" id="globalSearchInput" placeholder="Search for products, brands and tech essentials..." autocomplete="off">
                <button type="submit" class="search-button" aria-label="Search">🔍</button>
            </form>
            <!-- Autocomplete Dropdown Preview -->
            <div class="search-autocomplete-dropdown" id="searchAutocompleteDropdown"></div>
        </div>

        <!-- STOREFRONT HEADER / NAVBAR USER SECTION -->
        <ul class="nav-links">
            <c:choose>
                <%-- 1. When an ADMIN is logged in: Show Admin Console Badge (Hide Cart) --%>
                <c:when test="${not empty sessionScope.currentUser && sessionScope.currentUser.isAdmin()}">
                    <li>
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-warning" style="font-weight: 800; font-size: 0.85rem; padding: 0.4rem 0.85rem; border-radius: 6px;">
                            👑 Admin Console ↗
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/logout" style="color: #ef4444; font-weight: 700;">Sign Out</a>
                    </li>
                </c:when>
                <%-- 2. When a CUSTOMER is logged in: Show Cart, Wishlist, My Orders --%>
                <c:when test="${not empty sessionScope.currentUser}">
                    <li><a href="${pageContext.request.contextPath}/orders">📦 My Orders</a></li>
                    <li><a href="${pageContext.request.contextPath}/wishlist">❤️ Wishlist</a></li>
                    <li>
                        <a href="${pageContext.request.contextPath}/cart" class="cart-btn">
                            🛒 Cart <c:if test="${not empty cart && cart.totalQuantity > 0}">(${cart.totalQuantity})</c:if>
                        </a>
                    </li>
                    <li><a href="${pageContext.request.contextPath}/logout" style="color: #ef4444; font-weight: 700;">Sign Out</a></li>
                </c:when>
                <%-- 3. GUEST: Show Sign In --%>
                <c:otherwise>
                    <li><a href="${pageContext.request.contextPath}/auth/login" class="btn btn-primary btn-sm">Sign In</a></li>
                    <li><a href="${pageContext.request.contextPath}/register">Register</a></li>
                </c:otherwise>
            </c:choose>
        </ul>
    </nav>

    <main class="container" style="max-width: 860px; margin: 3rem auto 5rem; padding: 0 1.5rem;">
        
        <!-- Success Hero Card -->
        <div class="order-card-wrapper" style="text-align: center; border-top: 6px solid #10b981; padding: 3.5rem 2rem; margin-bottom: 2rem; box-shadow: 0 10px 30px -5px rgba(16, 185, 129, 0.15);">
            <div style="width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: #ffffff; font-size: 2.75rem; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.5rem; box-shadow: 0 10px 20px rgba(16, 185, 129, 0.35); animation: pulseGlow 2s infinite;">
                ✓
            </div>
            <h1 style="font-size: 2rem; font-weight: 900; color: #0f172a; margin-bottom: 0.5rem; letter-spacing: -0.02em;">
                Order Placed Successfully!
            </h1>
            <p style="color: var(--text-muted); font-size: 1.05rem; margin-bottom: 1.75rem; max-width: 520px; margin-left: auto; margin-right: auto; line-height: 1.6;">
                Thank you for shopping with ShopKart India! Your order has been placed and is currently being prepared by our fulfillment team.
            </p>
            <div style="background: #f8fafc; border: 1px dashed #94a3b8; border-radius: var(--radius-md); padding: 1.25rem 2.5rem; display: inline-block;">
                <span style="color: #64748b; font-size: 0.8rem; text-transform: uppercase; font-weight: 800; letter-spacing: 0.05em;">Order Reference Number</span><br>
                <strong style="font-size: 1.6rem; color: #2563eb; letter-spacing: 1px; font-weight: 900;">
                    <c:out value="${order.orderNumber}" />
                </strong>
            </div>
        </div>

        <!-- Receipt Card -->
        <div class="order-card-wrapper" style="padding: 2.5rem; margin-bottom: 2rem;">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #e2e8f0; padding-bottom: 1.25rem; margin-bottom: 1.75rem; flex-wrap: wrap; gap: 0.75rem;">
                <div>
                    <h2 style="font-size: 1.35rem; font-weight: 900; color: #0f172a; margin: 0;">
                        🧾 Official Order Summary
                    </h2>
                    <span style="font-size: 0.85rem; color: var(--text-muted);">Estimated Delivery: 2-3 Days via Express Shipping</span>
                </div>
                <span class="pill-badge badge-success" style="font-size: 0.88rem; padding: 0.45rem 1rem;">
                    ✓ <c:out value="${order.orderStatus}" />
                </span>
            </div>

            <!-- Shipping Destination -->
            <div style="margin-bottom: 1.75rem; background: #f8fafc; padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0;">
                <strong style="color: #0f172a; font-size: 1rem; display: block; margin-bottom: 0.5rem;">📍 Delivery Destination:</strong>
                <div style="color: #334155; font-size: 0.95rem; line-height: 1.6; font-weight: 600;">
                    <c:out value="${order.shippingFullName}" /><br>
                    <span style="font-weight: 400; color: var(--text-secondary);">
                        <c:out value="${order.shippingAddressLine1}" /><c:if test="${not empty order.shippingAddressLine2}">, <c:out value="${order.shippingAddressLine2}" /></c:if><br>
                        <c:out value="${order.shippingCity}" />, <c:out value="${order.shippingState}" /> - <c:out value="${order.shippingPostalCode}" /> (Phone: <c:out value="${order.shippingPhone}" />)
                    </span>
                </div>
                <div style="margin-top: 1rem; font-size: 0.88rem; color: var(--text-muted); border-top: 1px dashed #cbd5e1; padding-top: 0.75rem; display: flex; justify-content: space-between; flex-wrap: wrap;">
                    <span>Payment Method: <strong style="color: #0f172a;"><c:out value="${order.paymentMethod}" /></strong></span>
                    <span>Payment Status: <strong style="color: #10b981;"><c:out value="${order.paymentStatus}" /></strong></span>
                </div>
            </div>

            <!-- Items Table -->
            <table class="data-table" style="margin-bottom: 1.75rem; width: 100%;">
                <thead>
                    <tr>
                        <th style="padding: 0.85rem;">Item Description</th>
                        <th style="padding: 0.85rem;">Unit Price</th>
                        <th style="padding: 0.85rem; text-align: center;">Qty</th>
                        <th style="padding: 0.85rem; text-align: right;">Price</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${order.items}">
                        <tr>
                            <td style="padding: 0.85rem;">
                                <strong style="color: #0f172a;"><c:out value="${item.productName}" /></strong><br>
                                <small style="color: var(--text-muted);">SKU: <code><c:out value="${item.sku}" /></code></small>
                            </td>
                            <td style="padding: 0.85rem;">₹<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" /></td>
                            <td style="padding: 0.85rem; text-align: center;">${item.quantity}</td>
                            <td style="padding: 0.85rem; text-align: right;"><strong style="color: #0f172a;">₹<fmt:formatNumber value="${item.lineTotal}" minFractionDigits="2" /></strong></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <!-- Financial Totals -->
            <div style="max-width: 360px; margin-left: auto; font-size: 0.95rem; background: #f8fafc; border: 1px solid #e2e8f0; padding: 1.25rem; border-radius: var(--radius-md);">
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.45rem; color: var(--text-secondary);">
                    <span>Items Subtotal:</span>
                    <span>₹<fmt:formatNumber value="${order.subtotal}" minFractionDigits="2" /></span>
                </div>
                <c:if test="${order.discountAmount > 0}">
                    <div style="display: flex; justify-content: space-between; margin-bottom: 0.45rem; color: var(--success); font-weight: 700;">
                        <span>Applied Savings / Discount:</span>
                        <span>-₹<fmt:formatNumber value="${order.discountAmount}" minFractionDigits="2" /></span>
                    </div>
                </c:if>
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.45rem; color: var(--text-secondary);">
                    <span>GST (Included):</span>
                    <span>₹<fmt:formatNumber value="${order.taxAmount}" minFractionDigits="2" /></span>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.45rem; color: var(--text-secondary);">
                    <span>Delivery Charge:</span>
                    <span><c:choose><c:when test="${order.shippingAmount == 0}"><strong style="color: var(--success);">FREE</strong></c:when><c:otherwise>₹<fmt:formatNumber value="${order.shippingAmount}" minFractionDigits="2" /></c:otherwise></c:choose></span>
                </div>
                <div style="display: flex; justify-content: space-between; border-top: 2px solid #cbd5e1; padding-top: 0.75rem; margin-top: 0.75rem; font-weight: 900; font-size: 1.3rem; color: #0f172a;">
                    <span>Grand Total:</span>
                    <span style="color: #2563eb;">₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2" /></span>
                </div>
            </div>

            <!-- Action Buttons -->
            <div style="margin-top: 2.5rem; display: flex; gap: 1.25rem; justify-content: center; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/order?id=${order.orderId}" class="order-btn-primary" style="padding: 0.85rem 2rem; font-size: 1rem; border-radius: var(--radius-full);">
                    📦 Track Live Order
                </a>
                <a href="${pageContext.request.contextPath}/products" class="order-btn-secondary" style="padding: 0.85rem 2rem; font-size: 1rem; border-radius: var(--radius-full);">
                    🛍️ Continue Shopping
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
    <!-- Toast Notification Container -->
    <div id="toastContainer" class="toast-container"></div>

    <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
</body>
</html>
