<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="contextPath" content="${pageContext.request.contextPath}">
    <title>Customer Profile | ShopKart India</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&family=Outfit:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        .profile-hero-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            background: #e0f2fe;
            color: #0369a1;
            font-size: 0.75rem;
            font-weight: 800;
            padding: 0.25rem 0.65rem;
            border-radius: var(--radius-full);
            border: 1px solid #bae6fd;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .profile-field-hint {
            font-size: 0.78rem;
            color: var(--text-muted);
            margin-top: 0.35rem;
            display: flex;
            align-items: center;
            gap: 0.35rem;
        }
        .profile-security-card {
            background: #f8fafc;
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            padding: 1.25rem 1.5rem;
            margin-top: 2rem;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1.25rem;
        }
        .security-item {
            display: flex;
            align-items: flex-start;
            gap: 0.75rem;
        }
        .security-icon {
            font-size: 1.4rem;
            line-height: 1;
        }
        .security-label {
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
            color: var(--text-muted);
            letter-spacing: 0.5px;
        }
        .security-val {
            font-size: 0.9rem;
            font-weight: 700;
            color: var(--text-primary);
            margin-top: 0.15rem;
        }
    </style>
</head>
<body>

    <!-- 0. ADMIN STOREFRONT NOTIFICATION BAR -->
    <c:if test="${sessionScope.currentUser.admin}">
        <div class="admin-storefront-bar">
            <div class="admin-bar-left">
                <span class="admin-crown-badge">👑 ADMIN MODE</span>
                <span>ShopKart Control Center &bull; Logged in as <strong>${sessionScope.currentUser.fullName}</strong></span>
            </div>
            <div class="admin-bar-actions">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="admin-bar-btn admin-bar-btn-primary">⚙️ Admin Console</a>
                <a href="${pageContext.request.contextPath}/admin/products" class="admin-bar-btn">📦 Catalog</a>
                <a href="${pageContext.request.contextPath}/admin/orders" class="admin-bar-btn">🛒 Orders</a>
                <a href="${pageContext.request.contextPath}/admin/reports/sales" class="admin-bar-btn">📊 Sales</a>
            </div>
        </div>
    </c:if>

    <!-- 1. TOP TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <span class="ticker-badge">Account</span>
            <span>Manage your profile, security settings, and saved addresses</span>
        </div>
        <div class="ticker-links">
            <c:if test="${sessionScope.currentUser.admin}">
                <a href="${pageContext.request.contextPath}/admin/dashboard" style="color: #fbbf24; font-weight: 700;">⚙️ Admin Console</a>
                <a href="${pageContext.request.contextPath}/health" style="color: #38bdf8; font-weight: 700;">🩺 System Health</a>
            </c:if>
            <a href="${pageContext.request.contextPath}/orders">My Orders</a>
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

    <!-- MAIN PROFILE LAYOUT -->
    <main class="profile-page-layout">
        
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
                <li><a href="${pageContext.request.contextPath}/profile" class="profile-nav-link active">👤 Personal Info</a></li>
                <li><a href="${pageContext.request.contextPath}/addresses" class="profile-nav-link">📍 Saved Addresses</a></li>
                <li><a href="${pageContext.request.contextPath}/change-password" class="profile-nav-link">🔒 Change Password</a></li>
            </ul>

            <div style="margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid var(--border-color); font-size: 0.78rem; color: var(--text-muted); display: flex; align-items: center; gap: 0.4rem;">
                <span>🔒</span> 256-Bit SSL Secured Account
            </div>
        </aside>

        <!-- Right Profile Form Card -->
        <section>
            <c:if test="${sessionScope.currentUser.admin}">
                <div class="order-card-wrapper" style="padding: 1.5rem; background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%); color: #ffffff; margin-bottom: 1.5rem; border: 1px solid #6366f1;">
                    <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                        <div>
                            <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.35rem;">
                                <span class="admin-crown-badge">👑 ADMINISTRATOR ACCOUNT</span>
                                <span style="font-size: 0.8rem; color: #a5b4fc; font-weight: 600;">Full System Privileges</span>
                            </div>
                            <p style="color: #e0e7ff; font-size: 0.875rem; margin: 0;">
                                You are signed in with administrative access. You can manage store catalog, orders, transactions, users, and financial reports.
                            </p>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-primary" style="background: #fbbf24; color: #1e1b4b; border-color: #f59e0b; font-weight: 800; padding: 0.6rem 1.25rem;">
                            🛡️ Open Admin Console &rarr;
                        </a>
                    </div>
                </div>
            </c:if>

            <div class="profile-card">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem; border-bottom: 1px solid var(--border-color); padding-bottom: 1.25rem;">
                    <div>
                        <h1 style="font-size: 1.6rem; font-weight: 900; color: var(--text-primary); margin: 0 0 0.35rem 0;">
                            Personal Information
                        </h1>
                        <p style="color: var(--text-muted); font-size: 0.9rem; margin: 0;">
                            Manage your personal details, name, and contact information.
                        </p>
                    </div>
                    <div>
                        <span class="profile-hero-badge">
                            ✓ Active Member
                        </span>
                    </div>
                </div>

                <!-- Alert Messages -->
                <c:if test="${not empty error}">
                    <div style="background: #fef2f2; border-left: 4px solid var(--danger); padding: 0.9rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #991b1b; font-size: 0.9rem; font-weight: 600; display: flex; align-items: center; gap: 0.5rem;">
                        <span>⚠️</span> <span><c:out value="${error}" /></span>
                    </div>
                </c:if>
                <c:if test="${not empty successMessage || not empty success}">
                    <div style="background: #f0fdf4; border-left: 4px solid var(--success); padding: 0.9rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #166534; font-size: 0.9rem; font-weight: 600; display: flex; align-items: center; gap: 0.5rem;">
                        <span>✓</span> <span><c:out value="${not empty successMessage ? successMessage : success}" /></span>
                    </div>
                </c:if>

                <!-- 1. READ-ONLY PROFILE VIEW (Default State) -->
                <div id="profileViewMode" style="${not empty error ? 'display: none;' : 'display: block;'}">
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem; background: #f8fafc; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.5rem; margin-bottom: 1.5rem;">
                        <div style="min-width: 0;">
                            <div style="font-size: 0.78rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 0.25rem;">
                                First Name
                            </div>
                            <div style="font-size: 1.05rem; font-weight: 700; color: var(--text-primary); word-break: break-word;">
                                <c:out value="${user.firstName}" />
                            </div>
                        </div>

                        <div style="min-width: 0;">
                            <div style="font-size: 0.78rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 0.25rem;">
                                Last Name
                            </div>
                            <div style="font-size: 1.05rem; font-weight: 700; color: var(--text-primary); word-break: break-word;">
                                <c:out value="${user.lastName}" />
                            </div>
                        </div>

                        <div style="min-width: 0;">
                            <div style="font-size: 0.78rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 0.25rem;">
                                Email Address
                            </div>
                            <div style="font-size: 1.05rem; font-weight: 700; color: var(--text-primary); display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
                                <span style="word-break: break-all;"><c:out value="${user.email}" /></span>
                                <span style="font-size: 0.7rem; background: #dcfce7; color: #166534; font-weight: 800; padding: 0.15rem 0.5rem; border-radius: 4px; flex-shrink: 0; display: inline-flex; align-items: center; gap: 0.2rem;">
                                    ✓ Verified
                                </span>
                            </div>
                        </div>

                        <div style="min-width: 0;">
                            <div style="font-size: 0.78rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 0.25rem;">
                                Mobile Number
                            </div>
                            <div style="font-size: 1.05rem; font-weight: 700; color: var(--text-primary); word-break: break-word;">
                                <c:choose>
                                    <c:when test="${not empty user.phone}">
                                        <c:choose>
                                            <c:when test="${user.phone.startsWith('+91')}">
                                                <c:out value="${user.phone}" />
                                            </c:when>
                                            <c:otherwise>
                                                +91 <c:out value="${user.phone}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: var(--text-muted); font-weight: 400; font-style: italic;">Not provided</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- Edit Trigger Button -->
                    <div style="display: flex; gap: 1rem; align-items: center; padding-top: 1rem; border-top: 1px solid var(--border-color);">
                        <button type="button" id="editProfileBtn" onclick="toggleEditMode(true)" class="hero-cta-btn" style="padding: 0.75rem 2rem; border-radius: var(--radius-sm); border: none; cursor: pointer; font-size: 0.95rem;">
                            ✏️ Edit
                        </button>
                    </div>
                </div>

                <!-- 2. EDIT PROFILE FORM (Toggled State) -->
                <div id="profileEditMode" style="${not empty error ? 'display: block;' : 'display: none;'}">
                    <form action="${pageContext.request.contextPath}/profile" method="POST">
                        <div class="form-grid-2">
                            <div>
                                <label class="form-label" for="firstName">First Name *</label>
                                <input type="text" id="firstName" name="firstName" class="auth-form-input" value="<c:out value='${user.firstName}' />" required placeholder="Enter first name">
                            </div>
                            <div>
                                <label class="form-label" for="lastName">Last Name *</label>
                                <input type="text" id="lastName" name="lastName" class="auth-form-input" value="<c:out value='${user.lastName}' />" required placeholder="Enter last name">
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="email">Email Address</label>
                            <input type="email" id="email" class="auth-form-input" value="<c:out value='${user.email}' />" disabled readonly>
                            <div class="profile-field-hint">
                                <span>🔒</span> Email identifier is linked to your account credentials and cannot be changed directly.
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="phone">Phone / Mobile Number</label>
                            <input type="tel" id="phone" name="phone" class="auth-form-input" value="<c:out value='${user.phone}' />" placeholder="e.g. 9876543210 (10-digit mobile)" pattern="[0-9]{10}" maxlength="10">
                            <div class="profile-field-hint">
                                <span>📱</span> Used for order SMS notifications and OTP verification.
                            </div>
                        </div>

                        <div style="display: flex; gap: 1rem; align-items: center; margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid var(--border-color);">
                            <button type="submit" class="hero-cta-btn" style="padding: 0.75rem 2.25rem; border-radius: var(--radius-sm); border: none; cursor: pointer; font-size: 0.95rem;">
                                Save
                            </button>
                            <button type="button" onclick="toggleEditMode(false)" class="order-btn-outline" style="padding: 0.75rem 1.5rem; font-size: 0.95rem; cursor: pointer; background: #ffffff;">
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Security & Account Snapshot Details -->
                <div class="profile-security-card">
                    <div class="security-item">
                        <div class="security-icon">🛡️</div>
                        <div>
                            <div class="security-label">Account Security</div>
                            <div class="security-val">Password Protected</div>
                        </div>
                    </div>
                    <div class="security-item">
                        <div class="security-icon">💳</div>
                        <div>
                            <div class="security-label">Checkout Mode</div>
                            <div class="security-val">1-Click Fast Checkout</div>
                        </div>
                    </div>
                    <div class="security-item">
                        <div class="security-icon">📦</div>
                        <div>
                            <div class="security-label">Customer ID</div>
                            <div class="security-val">#SK-${user.userId}</div>
                        </div>
                    </div>
                </div>
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
    <script>
        function toggleEditMode(isEdit) {
            const viewMode = document.getElementById('profileViewMode');
            const editMode = document.getElementById('profileEditMode');
            if (isEdit) {
                viewMode.style.display = 'none';
                editMode.style.display = 'block';
                const firstNameInput = document.getElementById('firstName');
                if (firstNameInput) firstNameInput.focus();
            } else {
                editMode.style.display = 'none';
                viewMode.style.display = 'block';
            }
        }
    </script>
</body>
</html>
