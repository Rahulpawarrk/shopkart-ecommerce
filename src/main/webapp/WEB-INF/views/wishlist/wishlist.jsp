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
    <meta name="robots" content="noindex, nofollow">
    <meta name="contextPath" content="${pageContext.request.contextPath}">
    <title>My Wishlist | ShopKart India</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body style="background: #f8fafc;">

    <!-- TOP TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <span class="ticker-badge">Wishlist</span>
            <span>⚡ Save products and move them to cart with 1-click when prices drop!</span>
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
                    <span class="badge-count" id="headerCartBadge">${not empty sessionScope.cart ? sessionScope.cart.totalQuantity : 0}</span>
                </div>
                <span>Cart</span>
            </a>
        </div>
    </nav>

    <!-- SUB-NAVBAR -->
    <div class="sub-navbar">
        <a href="${pageContext.request.contextPath}/products" class="all-categories-btn">
            <span>☰</span> <strong>All Departments</strong>
        </a>
        <div class="nav-links-strip">
            <a href="${pageContext.request.contextPath}/category/1">💻 Laptops</a>
            <a href="${pageContext.request.contextPath}/category/2">📱 Mobiles</a>
            <a href="${pageContext.request.contextPath}/category/3">🎧 Audio</a>
            <a href="${pageContext.request.contextPath}/category/5">👕 Fashion</a>
            <a href="${pageContext.request.contextPath}/category/8">🏠 Kitchen</a>
        </div>
    </div>

    <!-- MAIN PROFILE / WISHLIST LAYOUT -->
    <main class="orders-page-layout">
        
        <!-- Left Customer Navigation -->
        <aside class="profile-sidebar">
            <div class="profile-user-card">
                <div class="profile-avatar-circle">
                    <c:choose>
                        <c:when test="${sessionScope.currentUser.admin}">👑</c:when>
                        <c:otherwise>👤</c:otherwise>
                    </c:choose>
                </div>
                <div style="min-width: 0;">
                    <div class="profile-user-greeting">Hello,</div>
                    <div class="profile-user-name"><c:out value="${sessionScope.currentUser.fullName}" /></div>
                    <div class="profile-user-email"><c:out value="${sessionScope.currentUser.email}" /></div>
                </div>
            </div>
            <ul class="profile-nav-list">
                <li><a href="${pageContext.request.contextPath}/orders" class="profile-nav-link">📦 My Orders</a></li>
                <li><a href="${pageContext.request.contextPath}/wishlist" class="profile-nav-link active">❤️ My Wishlist</a></li>
                <li><a href="${pageContext.request.contextPath}/profile" class="profile-nav-link">👤 Personal Info</a></li>
                <li><a href="${pageContext.request.contextPath}/addresses" class="profile-nav-link">📍 Saved Addresses</a></li>
                <li><a href="${pageContext.request.contextPath}/change-password" class="profile-nav-link">🔒 Change Password</a></li>
            </ul>
        </aside>

        <!-- Right Wishlist Content -->
        <section>
            <div style="background: #ffffff; border-radius: var(--radius-md); padding: 1.5rem; border: 1px solid var(--border-color); box-shadow: var(--shadow-sm); margin-bottom: 1.5rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-color); padding-bottom: 1rem; margin-bottom: 1.5rem;">
                    <h2 style="font-size: 1.35rem; font-weight: 800;">My Wishlist (${not empty wishlist.items ? wishlist.items.size() : 0} items)</h2>
                    <a href="${pageContext.request.contextPath}/products" style="color: var(--primary); font-weight: 700; font-size: 0.85rem;">+ Add More Items</a>
                </div>

                <c:choose>
                    <c:when test="${empty wishlist.items}">
                        <div style="text-align: center; padding: 3rem 1rem;">
                            <div style="font-size: 3.5rem; margin-bottom: 1rem;">❤️</div>
                            <h3 style="font-size: 1.35rem; margin-bottom: 0.5rem;">Your Wishlist is Empty</h3>
                            <p style="color: var(--text-muted); margin-bottom: 1.5rem;">Save products here that you'd like to buy later.</p>
                            <a href="${pageContext.request.contextPath}/products?deals=true" class="hero-cta-btn">Explore Deals →</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="catalog-products-grid">
                            <c:forEach var="item" items="${wishlist.items}">
                                <div class="product-card">
                                    <c:if test="${item.discountPercentage > 0}">
                                        <span class="card-discount-badge"><fmt:formatNumber value="${item.discountPercentage}" maxFractionDigits="0"/>% OFF</span>
                                    </c:if>

                                    <a href="${pageContext.request.contextPath}/product?id=${item.productId}">
                                        <div class="product-img-container">
                                            <img src="${item.primaryImageUrl}" alt="<c:out value='${item.productName}'/>">
                                        </div>
                                        <div class="product-brand"><c:out value="${item.brand}"/></div>
                                        <div class="product-title"><c:out value="${item.productName}"/></div>
                                    </a>

                                    <div class="price-row">
                                        <span class="current-price">₹<fmt:formatNumber value="${item.effectivePrice}" pattern="#,##0"/></span>
                                        <c:if test="${item.discountPercentage > 0}">
                                            <span class="mrp-price">₹<fmt:formatNumber value="${item.price}" pattern="#,##0"/></span>
                                        </c:if>
                                    </div>

                                    <div style="display: flex; gap: 0.4rem; margin-top: auto;">
                                        <!-- Move to Cart -->
                                        <form action="${pageContext.request.contextPath}/wishlist/move-to-cart" method="POST" style="flex:1; margin:0;">
                                            <input type="hidden" name="_csrf" value="${csrfToken}">
                                            <input type="hidden" name="productId" value="${item.productId}">
                                            <button type="submit" class="card-add-cart-btn" style="width:100%;" title="Move to Cart">
                                                🛒 Add Cart
                                            </button>
                                        </form>

                                        <!-- Buy Now -->
                                        <button type="button" class="card-buy-now-btn" style="flex:1;" onclick="quickBuyNow(${item.productId}, 1, event)" title="Buy Now">
                                            ⚡ Buy Now
                                        </button>

                                        <!-- Remove Item -->
                                        <form action="${pageContext.request.contextPath}/wishlist/remove" method="POST" style="margin:0;">
                                            <input type="hidden" name="_csrf" value="${csrfToken}">
                                            <input type="hidden" name="productId" value="${item.productId}">
                                            <button type="submit" style="background:#fee2e2; border:1px solid #fca5a5; color:var(--danger); border-radius:var(--radius-sm); padding:0.55rem 0.65rem; cursor:pointer; font-weight:700;" title="Remove from Wishlist">
                                                🗑️
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
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
