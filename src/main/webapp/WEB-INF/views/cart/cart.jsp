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
    <meta name="contextPath" content="${pageContext.request.contextPath}">
    <title>Shopping Cart | ShopKart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>

    <!-- 0. ADMIN NOTIFICATION BAR -->
    <c:if test="${sessionScope.currentUser.admin}">
        <div class="admin-storefront-bar">
            <div class="admin-bar-left">
                <span class="admin-crown-badge">👑 ADMIN MODE</span>
                <span>ShopKart Control Center &bull; Logged in as <strong><c:out value="${sessionScope.currentUser.fullName}" /></strong></span>
            </div>
            <div class="admin-bar-actions">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="admin-bar-btn admin-bar-btn-primary">⚙️ Admin Console</a>
                <a href="${pageContext.request.contextPath}/admin/orders" class="admin-bar-btn">🛒 Orders</a>
            </div>
        </div>
    </c:if>

    <!-- TOP TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <span class="ticker-badge">Cart</span>
            <span>⚡ 100% Purchase Protection & Safe Doorstep Delivery</span>
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
            <div class="delivery-locator" id="headerDeliveryTrigger" onclick="openPinCodeModal()" title="Change Delivery Location">
                <span class="loc-icon">📍</span>
                <div class="loc-text">
                    <span class="sub">Deliver to</span>
                    <span class="main" id="headerPincodeText">Bengaluru 560100</span>
                </div>
            </div>
        </div>

        <div class="header-search-wrapper">
            <form action="${pageContext.request.contextPath}/products" method="GET" class="header-search-form">
                <input type="text" name="keyword" class="search-input" id="globalSearchInput" placeholder="Search for products, brands and more..." autocomplete="off">
                <button type="submit" class="search-button">🔍</button>
            </form>
            <div class="search-autocomplete-dropdown" id="searchAutocompleteDropdown"></div>
        </div>

        <div class="header-actions">
            <!-- User Account Menu with Hover Dropdown -->
            <div class="user-account-menu" id="userAccountMenu">
                <c:choose>
                    <c:when test="${not empty sessionScope.currentUser}">
                        <div class="user-nav-btn" tabindex="0" role="button">
                            <span class="user-avatar-icon">👤</span>
                            <span class="user-nav-name"><c:out value="${sessionScope.currentUser.fullName}" /></span>
                            <span class="arrow-down">▾</span>
                        </div>
                        <div class="account-dropdown">
                            <div class="dropdown-header">
                                <div class="user-name">Hello, <c:out value="${sessionScope.currentUser.fullName}" /></div>
                                <div class="user-email"><c:out value="${sessionScope.currentUser.email}" /></div>
                                <c:if test="${sessionScope.currentUser.admin}">
                                    <div style="font-size: 0.7rem; color: #fbbf24; font-weight: 800; margin-top: 0.2rem;">👑 ADMINISTRATOR</div>
                                </c:if>
                            </div>
                            <c:choose>
                                <c:when test="${sessionScope.currentUser.admin}">
                                    <a href="${pageContext.request.contextPath}/admin/dashboard" style="color:var(--amazon-orange); font-weight:800;">⚙️ Admin Control Panel</a>
                                    <a href="${pageContext.request.contextPath}/admin/products">📦 Manage Products</a>
                                    <a href="${pageContext.request.contextPath}/admin/orders">🛒 Manage All Orders</a>
                                    <a href="${pageContext.request.contextPath}/admin/reports/sales">📈 Sales & Revenue</a>
                                    <a href="${pageContext.request.contextPath}/admin/profile">👑 Admin Profile</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/profile">👤 My Profile</a>
                                    <a href="${pageContext.request.contextPath}/orders">📦 My Orders</a>
                                    <a href="${pageContext.request.contextPath}/addresses">📍 Saved Addresses</a>
                                    <a href="${pageContext.request.contextPath}/change-password">🔒 Change Password</a>
                                </c:otherwise>
                            </c:choose>
                            <div class="dropdown-divider"></div>
                            <a href="${pageContext.request.contextPath}/logout" style="color:var(--danger); font-weight: 600;">🚪 Sign Out</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login" class="user-nav-btn" title="Sign In to ShopKart">
                            <span class="user-avatar-icon">👤</span>
                            <span style="font-weight:600;">Sign In</span>
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>

            <a href="${pageContext.request.contextPath}/wishlist" class="header-action-inline" title="Wishlist">
                <span class="badge-icon">❤️</span>
                <span>Wishlist</span>
            </a>

            <a href="${pageContext.request.contextPath}/cart" class="header-action-inline" title="Cart">
                <div class="cart-icon-wrapper">
                    <span class="badge-icon">🛒</span>
                    <span class="badge-count" id="headerCartBadge">${cart.totalQuantity}</span>
                </div>
                <span>Cart</span>
            </a>
        </div>
    </nav>

    <!-- MAIN CART CONTAINER -->
    <main class="cart-page-layout">
        <c:choose>
            <c:when test="${empty cart or empty cart.items}">
                <!-- Empty Cart State -->
                <div class="empty-cart-container" style="grid-column: 1 / -1; text-align: center; padding: 4rem 1.5rem; background: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 4px 20px rgba(0,0,0,0.03); margin: 2rem auto; max-width: 600px;">
                    <div style="font-size: 3.5rem; margin-bottom: 1rem;">🛒</div>
                    <h2 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin: 0 0 0.5rem 0;">Nothing in your cart</h2>
                    <p style="color: #64748b; font-size: 0.95rem; margin: 0;">Your shopping cart is currently empty.</p>
                </div>
            </c:when>

            <c:otherwise>
                <!-- Cart Items Section -->
                <section class="cart-items-section">
                    <div class="cart-header-title" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.75rem;">
                        <div>
                            <h2 style="margin: 0; display: inline-block;">Shopping Cart</h2>
                            <span class="items-count" style="margin-left: 0.5rem;">${cart.totalQuantity} Items</span>
                        </div>
                        <div>
                            <a href="${pageContext.request.contextPath}/products" style="display: inline-flex; align-items: center; gap: 0.3rem; color: var(--primary); font-weight: 700; font-size: 0.85rem; text-decoration: none;">
                                <span>🛍️ Browse Catalog &rarr;</span>
                            </a>
                        </div>
                    </div>

                    <!-- Flash / Error messages -->
                    <c:if test="${not empty error}">
                        <div style="background:#fee2e2; color:#b91c1c; padding:0.75rem 1rem; border-radius:var(--radius-sm); margin-bottom:1rem; border:1px solid #fca5a5; font-size:0.9rem;">
                            ⚠️ <c:out value="${error}" />
                        </div>
                    </c:if>
                    <c:if test="${param.added == 'true'}">
                        <div style="background:#dcfce7; color:#15803d; padding:0.75rem 1rem; border-radius:var(--radius-sm); margin-bottom:1rem; border:1px solid #86efac; font-size:0.9rem;">
                            ✓ Products added to your cart successfully!
                        </div>
                    </c:if>
                    <c:if test="${param.couponApplied == 'true'}">
                        <div style="background:#dcfce7; color:#15803d; padding:0.75rem 1rem; border-radius:var(--radius-sm); margin-bottom:1rem; border:1px solid #86efac; font-size:0.9rem;">
                            ✓ Coupon applied successfully! Discount has been deducted from your order.
                        </div>
                    </c:if>
                    <c:if test="${param.couponRemoved == 'true'}">
                        <div style="background:#f1f5f9; color:#475569; padding:0.75rem 1rem; border-radius:var(--radius-sm); margin-bottom:1rem; border:1px solid #cbd5e1; font-size:0.9rem;">
                            ℹ️ Coupon removed from your cart.
                        </div>
                    </c:if>

                    <div class="cart-item-list">
                        <c:forEach var="item" items="${cart.items}">
                            <div class="cart-item-row">
                                <div class="cart-item-img">
                                    <img src="${not empty item.primaryImageUrl ? item.primaryImageUrl : 'https://placehold.co/120x120?text=ShopKart'}" alt="<c:out value='${item.productName}'/>">
                                </div>

                                <div class="cart-item-info">
                                    <h3 class="cart-item-title">
                                        <a href="${pageContext.request.contextPath}/product?id=${item.productId}"><c:out value="${item.productName}"/></a>
                                    </h3>
                                    <div class="cart-item-meta">
                                        <c:if test="${not empty item.sku}">
                                            <span>SKU: <c:out value="${item.sku}"/></span>
                                        </c:if>
                                        <span class="stock-status in-stock">✓ In Stock</span>
                                        <span>Eligible for FREE Shipping</span>
                                    </div>

                                    <!-- Interactive Stepper & Actions -->
                                    <div class="cart-item-controls">
                                        <div class="qty-stepper">
                                            <form action="${pageContext.request.contextPath}/cart/update" method="POST" style="display:inline;">
                                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                                <input type="hidden" name="productId" value="${item.productId}">
                                                <input type="hidden" name="quantity" value="${item.quantity - 1}">
                                                <button type="submit" class="qty-btn" ${item.quantity <= 1 ? 'disabled' : ''}>-</button>
                                            </form>
                                            <input type="number" class="qty-input" value="${item.quantity}" readonly>
                                            <form action="${pageContext.request.contextPath}/cart/update" method="POST" style="display:inline;">
                                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                                <input type="hidden" name="productId" value="${item.productId}">
                                                <input type="hidden" name="quantity" value="${item.quantity + 1}">
                                                <button type="submit" class="qty-btn">+</button>
                                            </form>
                                        </div>

                                        <button type="button" class="cart-action-btn" onclick="quickAddToWishlist(${item.productId}, event)">
                                            ❤️ Save for Later
                                        </button>

                                        <form action="${pageContext.request.contextPath}/cart/remove" method="POST" style="display:inline;">
                                            <input type="hidden" name="_csrf" value="${csrfToken}">
                                            <input type="hidden" name="productId" value="${item.productId}">
                                            <button type="submit" style="background:none; border:none; color:var(--danger); font-size:0.8rem; font-weight:600; cursor:pointer;">
                                                🗑️ Delete
                                            </button>
                                        </form>
                                    </div>
                                </div>

                                <div class="cart-item-price">
                                    ₹<fmt:formatNumber value="${item.lineSubtotal}" pattern="#,##0"/>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- Bottom Action Strip -->
                    <div style="display: flex; justify-content: flex-end; align-items: center; padding: 1rem 0; border-top: 1px dashed var(--border-color); margin-top: 1.5rem; flex-wrap: wrap; gap: 0.75rem;">
                        <a href="${pageContext.request.contextPath}/products" style="color: var(--text-secondary); font-size: 0.85rem; font-weight: 700; text-decoration: none;">
                            🛍️ Explore All Categories &rarr;
                        </a>
                    </div>

                    <!-- Frequently Added Together / Suggested Products Section -->
                    <c:if test="${not empty suggestedProducts}">
                        <div style="margin-top: 2rem; background: #ffffff; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem; box-shadow: var(--shadow-xs);">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                                <div>
                                    <h3 style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin: 0;">⚡ Frequently Added Together</h3>
                                    <span style="font-size: 0.78rem; color: var(--text-muted);">Quickly add popular tech &amp; lifestyle essentials</span>
                                </div>
                                <a href="${pageContext.request.contextPath}/" style="color: var(--primary); font-weight: 700; font-size: 0.82rem; text-decoration: none;">
                                    Explore Home &rarr;
                                </a>
                            </div>

                            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(190px, 1fr)); gap: 1rem;">
                                <c:forEach var="sug" items="${suggestedProducts}" begin="0" end="3">
                                    <div style="border: 1px solid var(--border-light); border-radius: 10px; padding: 0.85rem; display: flex; flex-direction: column; justify-content: space-between; background: #fafafa;">
                                        <div style="display: flex; gap: 0.75rem; align-items: center; margin-bottom: 0.5rem;">
                                            <img src="${not empty sug.primaryImageUrl ? sug.primaryImageUrl : 'https://placehold.co/60x60?text=Tech'}" alt="<c:out value='${sug.productName}'/>" style="width: 52px; height: 52px; object-fit: contain; border-radius: 6px; background: #fff; border: 1px solid #f1f5f9;">
                                            <div style="min-width: 0;">
                                                <div style="font-size: 0.72rem; color: var(--text-muted); font-weight: 700; text-transform: uppercase;"><c:out value="${sug.brand}"/></div>
                                                <a href="${pageContext.request.contextPath}/product?id=${sug.productId}" style="font-size: 0.82rem; font-weight: 700; color: #1e293b; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.25;">
                                                    <c:out value="${sug.productName}"/>
                                                </a>
                                            </div>
                                        </div>
                                        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: auto; padding-top: 0.5rem; border-top: 1px solid #f1f5f9;">
                                            <span style="font-weight: 800; font-size: 0.92rem; color: #0f172a;">₹<fmt:formatNumber value="${sug.effectivePrice}" pattern="#,##0"/></span>
                                            <button type="button" onclick="quickAddToCart(${sug.productId}, 1, event)" style="background: #febd69; border: none; border-radius: 6px; padding: 0.35rem 0.75rem; font-weight: 800; font-size: 0.78rem; cursor: pointer; color: #0f172a; transition: var(--transition-fast);">
                                                + Add Cart
                                            </button>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </section>

                <!-- Order Summary & Checkout -->
                <aside class="cart-summary-card">
                    <h3 class="summary-title">Order Summary</h3>

                    <!-- Price Breakdown Table -->
                    <div class="summary-rows">
                        <div class="summary-row">
                            <span>Subtotal (${cart.totalQuantity} items):</span>
                            <span>₹<fmt:formatNumber value="${cart.subtotal}" pattern="#,##0"/></span>
                        </div>

                        <c:if test="${cart.totalDiscount > 0}">
                            <div class="summary-row" style="color: var(--success); font-weight: 700;">
                                <span>⚡ Applied Discount:</span>
                                <span>-₹<fmt:formatNumber value="${cart.totalDiscount}" pattern="#,##0"/></span>
                            </div>
                        </c:if>

                        <c:if test="${cart.couponDiscount > 0}">
                            <div class="summary-row" style="color: var(--success); font-weight: 700;">
                                <span>🏷️ Coupon Discount (<c:out value="${cart.appliedCouponCode}"/>):</span>
                                <span>-₹<fmt:formatNumber value="${cart.couponDiscount}" pattern="#,##0"/></span>
                            </div>
                        </c:if>

                        <div class="summary-row">
                            <span>Estimated Tax (GST):</span>
                            <span>₹<fmt:formatNumber value="${cart.estimatedTax}" pattern="#,##0"/></span>
                        </div>

                        <div class="summary-row">
                            <span>Delivery Charges:</span>
                            <span style="color: var(--success); font-weight: 700;">FREE</span>
                        </div>

                        <div class="summary-row total-row">
                            <span>Grand Total:</span>
                            <span>₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0"/></span>
                        </div>

                        <c:if test="${cart.totalSavings > 0}">
                            <div class="summary-row savings-row">
                                <span>Your Total Savings:</span>
                                <span>₹<fmt:formatNumber value="${cart.totalSavings}" pattern="#,##0"/></span>
                            </div>
                        </c:if>
                    </div>

                    <div style="background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: var(--radius-sm); padding: 0.65rem 0.85rem; margin: 1rem 0; display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; color: #475569;">
                        <span>🏷️</span>
                        <span>Have a coupon or gift code? Apply it at <strong>Checkout</strong> after address &amp; payment selection.</span>
                    </div>

                    <!-- Proceed to Checkout -->
                    <a href="${pageContext.request.contextPath}/checkout" class="btn-checkout">
                        Proceed to Buy (${cart.totalQuantity} items) →
                    </a>

                    <div style="margin-top: 1.25rem; font-size: 0.75rem; color: var(--text-muted); text-align: center;">
                        🔒 100% Safe &amp; Secure Payments | Cash on Delivery Available
                    </div>
                </aside>
            </c:otherwise>
        </c:choose>
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
