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
    <meta name="robots" content="noindex, nofollow">
    <meta name="contextPath" content="${pageContext.request.contextPath}">
    <title>Saved Addresses | ShopKart India</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>

    <!-- 1. TOP TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <span class="ticker-badge">Addresses</span>
            <span>Manage multiple doorstep delivery destinations and default shipping addresses</span>
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

    <!-- MAIN ADDRESSES LAYOUT -->
    <main class="orders-page-layout">
        
        <!-- Left Sidebar -->
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
                <li><a href="${pageContext.request.contextPath}/addresses" class="profile-nav-link active">📍 Saved Addresses</a></li>
                <li><a href="${pageContext.request.contextPath}/change-password" class="profile-nav-link">🔒 Change Password</a></li>
            </ul>
        </aside>

        <!-- Right Addresses Grid -->
        <section>
            <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 1.5rem;">
                <div>
                    <h1 style="font-size: 1.5rem; font-weight: 800; color: var(--text-primary); margin-bottom: 0.25rem;">Your Addresses</h1>
                    <p style="color: var(--text-muted); font-size: 0.9rem;">Manage saved delivery destinations for quick 1-click checkout.</p>
                </div>
                <a href="${pageContext.request.contextPath}/addresses/add" class="hero-cta-btn" style="padding: 0.5rem 1.25rem; font-size: 0.85rem;">
                    + Add New Address
                </a>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1.25rem;">
                
                <!-- Add Address Box -->
                <a href="${pageContext.request.contextPath}/addresses/add" style="display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 200px; border: 2px dashed #cbd5e1; border-radius: var(--radius-md); text-decoration: none; color: var(--text-secondary); transition: var(--transition-fast); background: #ffffff;">
                    <div style="font-size: 2.5rem; color: var(--amazon-orange);">+</div>
                    <div style="font-weight: 800; font-size: 1.1rem; color: var(--text-primary); margin-top: 0.5rem;">Add Address</div>
                </a>

                <!-- Existing Addresses -->
                <c:forEach var="addr" items="${addresses}">
                    <div class="order-card-wrapper" style="padding: 1.25rem; display: flex; flex-direction: column; justify-content: space-between; margin-bottom: 0; min-height: 200px;">
                        <div>
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                                <strong style="font-size: 1rem; color: var(--text-primary);"><c:out value="${addr.fullName}" /></strong>
                                <c:if test="${addr.defaultAddress}">
                                    <span style="background: #fef3c7; color: #92400e; font-size: 0.7rem; font-weight: 800; padding: 0.15rem 0.5rem; border-radius: 4px; border: 1px solid #fde68a;">
                                        DEFAULT
                                    </span>
                                </c:if>
                            </div>
                            <div style="color: var(--text-secondary); font-size: 0.85rem; line-height: 1.5;">
                                <c:out value="${addr.addressLine1}" /><c:if test="${not empty addr.addressLine2}">, <c:out value="${addr.addressLine2}" /></c:if><br>
                                <c:out value="${addr.city}" />, <c:out value="${addr.state}" /> - <c:out value="${addr.postalCode}" /><br>
                                Phone: <c:out value="${addr.phone}" />
                            </div>
                        </div>

                        <div style="display: flex; gap: 0.5rem; margin-top: 1.25rem; border-top: 1px solid var(--border-color); padding-top: 0.75rem; font-size: 0.8rem; align-items: center;">
                            <a href="${pageContext.request.contextPath}/addresses/edit?id=${addr.addressId}" style="color: var(--primary); font-weight: 700; text-decoration: none;">Edit</a>
                            <span style="color: var(--border-color);">|</span>
                            <form action="${pageContext.request.contextPath}/addresses/delete" method="POST"
                                  style="display:inline; margin:0; padding:0;"
                                  onsubmit="return confirm('Remove this address?');">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="addressId" value="${addr.addressId}">
                                <button type="submit" style="background:none; border:none; padding:0; color: var(--danger); font-weight: 700; font-size: 0.8rem; cursor:pointer;">Remove</button>
                            </form>
                            <c:if test="${not addr.defaultAddress}">
                                <span style="color: var(--border-color);">|</span>
                                <form action="${pageContext.request.contextPath}/addresses/default" method="POST"
                                      style="display:inline; margin:0; padding:0;"
                                      onsubmit="return confirm('Set as default delivery address?');">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="addressId" value="${addr.addressId}">
                                    <button type="submit" style="background:none; border:none; padding:0; color: var(--text-muted); font-size: 0.8rem; cursor:pointer;">Set as Default</button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </section>
    </main>

    <!-- FOOTER -->
    <footer class="main-footer" style="background: #0f172a; color: #cbd5e1; padding: 2.5rem 1.5rem 2rem; margin-top: 4rem; text-align: center;">
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

    <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
</body>
</html>
