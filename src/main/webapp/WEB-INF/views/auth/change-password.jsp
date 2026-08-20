<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <meta name="contextPath" content="${pageContext.request.contextPath}">
    <title>Change Password | ShopKart India</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>

    <!-- 1. TOP TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <span class="ticker-badge">Account Security</span>
            <span>Protect your account credentials and maintain strong security protocols</span>
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
                <div class="user-nav-btn" tabindex="0" role="button">
                    <span class="user-avatar-icon">👤</span>
                    <span class="user-nav-name">${sessionScope.currentUser.fullName}</span>
                    <span class="arrow-down">▾</span>
                </div>
                <div class="account-dropdown">
                    <div class="dropdown-header">
                        <div class="user-name">Hello, ${sessionScope.currentUser.fullName}</div>
                        <div class="user-email">${sessionScope.currentUser.email}</div>
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

    <!-- 3. SUB-NAVBAR -->
    <div class="sub-navbar">
        <a href="${pageContext.request.contextPath}/products" class="all-categories-btn">
            <span>☰</span> <strong>All Departments</strong>
        </a>
        <div class="nav-links-strip">
            <a href="${pageContext.request.contextPath}/products?category=1">💻 Laptops</a>
            <a href="${pageContext.request.contextPath}/products?category=2">📱 Mobiles</a>
            <a href="${pageContext.request.contextPath}/products?category=3">🎧 Audio</a>
            <a href="${pageContext.request.contextPath}/products?category=5">👕 Fashion</a>
        </div>
    </div>

    <!-- MAIN CHANGE PASSWORD LAYOUT -->
    <main class="orders-page-layout">
        
        <!-- Left Sidebar Navigation -->
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
                <li><a href="${pageContext.request.contextPath}/wishlist" class="profile-nav-link">❤️ My Wishlist</a></li>
                <li><a href="${pageContext.request.contextPath}/profile" class="profile-nav-link">👤 Personal Info</a></li>
                <li><a href="${pageContext.request.contextPath}/addresses" class="profile-nav-link">📍 Saved Addresses</a></li>
                <li><a href="${pageContext.request.contextPath}/change-password" class="profile-nav-link active">🔒 Change Password</a></li>
            </ul>
        </aside>

        <!-- Right Change Password Card -->
        <section>
            <div class="order-card-wrapper" style="padding: 2rem; max-width: 680px;">
                <div style="border-bottom: 1px solid var(--border-color); padding-bottom: 1rem; margin-bottom: 1.5rem;">
                    <h1 style="font-size: 1.5rem; font-weight: 800; color: var(--text-primary); margin: 0 0 0.25rem;">
                        Security & Password Settings
                    </h1>
                    <p style="color: var(--text-muted); font-size: 0.875rem; margin: 0;">
                        Ensure your account uses a strong, unique password with at least 8 characters.
                    </p>
                </div>

                <c:if test="${not empty successMessage}">
                    <div style="background: #dcfce7; border-left: 4px solid var(--success); padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #166534; font-size: 0.9rem; font-weight: 600;">
                        ✓ <c:out value="${successMessage}" />
                    </div>
                </c:if>

                <c:if test="${not empty error}">
                    <div style="background: #fee2e2; border-left: 4px solid var(--danger); padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #991b1b; font-size: 0.9rem; font-weight: 600;">
                        ⚠️ <c:out value="${error}" />
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/change-password" method="POST">
                    <div style="margin-bottom: 1.25rem;">
                        <label for="currentPassword" class="form-label" style="font-weight: 700; font-size: 0.85rem; margin-bottom: 0.35rem; display: block;">
                            Current Account Password *
                        </label>
                        <input type="password" id="currentPassword" name="currentPassword" class="auth-form-input" required placeholder="••••••••">
                    </div>

                    <div style="margin-bottom: 1.25rem;">
                        <label for="newPassword" class="form-label" style="font-weight: 700; font-size: 0.85rem; margin-bottom: 0.35rem; display: block;">
                            New Password * (Min 8 characters)
                        </label>
                        <input type="password" id="newPassword" name="newPassword" class="auth-form-input" minlength="8" required placeholder="••••••••">
                    </div>

                    <div style="margin-bottom: 1.5rem;">
                        <label for="confirmNewPassword" class="form-label" style="font-weight: 700; font-size: 0.85rem; margin-bottom: 0.35rem; display: block;">
                            Confirm New Password *
                        </label>
                        <input type="password" id="confirmNewPassword" name="confirmNewPassword" class="auth-form-input" minlength="8" required placeholder="••••••••">
                    </div>

                    <div style="display: flex; gap: 1rem; align-items: center;">
                        <button type="submit" class="hero-cta-btn" style="padding: 0.75rem 2rem; font-size: 0.95rem; border: none; cursor: pointer;">
                            🔒 Update Password
                        </button>
                        <a href="${pageContext.request.contextPath}/profile" class="order-btn-outline" style="padding: 0.7rem 1.5rem; font-size: 0.95rem; text-decoration: none;">
                            Cancel
                        </a>
                    </div>
                </form>
            </div>
        </section>
    </main>

    <!-- FOOTER -->
    <footer style="background-color: var(--amazon-dark); color: #cbd5e1; margin-top: auto; padding: 3rem 2rem 2rem; border-top: 1px solid var(--amazon-subnav);">
        <div style="max-width: 1400px; margin: 0 auto; display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 2rem; margin-bottom: 2rem;">
            <div>
                <h4 style="color: #ffffff; margin-bottom: 1rem; font-size: 1rem; font-weight: 800;">Get to Know Us</h4>
                <ul style="list-style: none; display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.85rem;">
                    <li><a href="#">About ShopKart</a></li>
                    <li><a href="#">Careers</a></li>
                    <li><a href="#">Press Releases</a></li>
                </ul>
            </div>
            <div>
                <h4 style="color: #ffffff; margin-bottom: 1rem; font-size: 1rem; font-weight: 800;">Connect With Us</h4>
                <ul style="list-style: none; display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.85rem;">
                    <li><a href="#">Facebook</a></li>
                    <li><a href="#">Twitter / X</a></li>
                    <li><a href="#">Instagram</a></li>
                </ul>
            </div>
            <div>
                <h4 style="color: #ffffff; margin-bottom: 1rem; font-size: 1rem; font-weight: 800;">Make Money with Us</h4>
                <ul style="list-style: none; display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.85rem;">
                    <li><a href="#">Sell on ShopKart</a></li>
                    <li><a href="#">Affiliate Marketing</a></li>
                </ul>
            </div>
            <div>
                <h4 style="color: #ffffff; margin-bottom: 1rem; font-size: 1rem; font-weight: 800;">Let Us Help You</h4>
                <ul style="list-style: none; display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.85rem;">
                    <li><a href="${pageContext.request.contextPath}/profile">Your Account</a></li>
                    <li><a href="${pageContext.request.contextPath}/orders">Returns & Replacements</a></li>
                    <li><a href="${pageContext.request.contextPath}/addresses">Saved Addresses</a></li>
                    <c:if test="${sessionScope.currentUser.admin}">
                        <li><a href="${pageContext.request.contextPath}/health" style="color: #38bdf8;">🩺 System Health</a></li>
                    </c:if>
                </ul>
            </div>
        </div>
        <div style="text-align: center; border-top: 1px solid var(--amazon-subnav); padding-top: 1.5rem; font-size: 0.82rem; color: #94a3b8; display: flex; flex-direction: column; align-items: center; gap: 0.5rem;">
            <div>&copy; 2026 ShopKart Inc. All rights reserved. &bull; Trade Assurance &bull; 100% Purchase Protection.</div>
            <div style="padding-top: 0.5rem; border-top: 1px solid rgba(255,255,255,0.08); width: 100%; max-width: 500px; color: #cbd5e1; font-size: 0.85rem;">
                Designed, Developed &amp; Managed by <strong style="color: #38bdf8; font-weight: 800; letter-spacing: 0.02em;">Rahul Pawar</strong>
            </div>
        </div>
    </footer>

    <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
</body>
</html>
