<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="contextPath" content="${pageContext.request.contextPath}">
                <title>
                    <c:out value="${product.productName}" /> | ShopKart
                </title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link
                    href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap"
                    rel="stylesheet">
                <style>
                    /* ==============================================================================
                       PREMIUM CUSTOMER REVIEWS & RATINGS COMPONENT (PDP)
                       ============================================================================== */
                    .pdp-reviews-wrapper {
                        max-width: 1500px;
                        margin: 2.5rem auto 4rem;
                        padding: 0 2rem;
                        box-sizing: border-box;
                    }

                    .reviews-container-card {
                        background: #ffffff;
                        border-radius: 16px;
                        padding: 2.5rem;
                        border: 1.5px solid #e2e8f0;
                        box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01);
                    }

                    .reviews-header-bar {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        border-bottom: 2px solid #f1f5f9;
                        padding-bottom: 1.25rem;
                        margin-bottom: 2rem;
                        flex-wrap: wrap;
                        gap: 1rem;
                    }

                    .reviews-header-title {
                        display: flex;
                        align-items: center;
                        gap: 0.75rem;
                        flex-wrap: wrap;
                    }

                    .reviews-header-title h2 {
                        font-size: 1.5rem;
                        font-weight: 900;
                        margin: 0;
                        color: #0f172a;
                        letter-spacing: -0.02em;
                    }

                    .reviews-count-badge {
                        background: #eff6ff;
                        color: #2563eb;
                        font-size: 0.82rem;
                        font-weight: 800;
                        padding: 0.25rem 0.75rem;
                        border-radius: 9999px;
                        border: 1px solid #dbeafe;
                    }

                    .btn-write-review-hero {
                        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
                        color: #ffffff !important;
                        padding: 0.65rem 1.35rem;
                        border-radius: 8px;
                        font-weight: 800;
                        font-size: 0.9rem;
                        text-decoration: none;
                        display: inline-flex;
                        align-items: center;
                        gap: 0.45rem;
                        box-shadow: 0 4px 12px rgba(217, 119, 6, 0.3);
                        transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
                    }

                    .btn-write-review-hero:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 6px 16px rgba(217, 119, 6, 0.4);
                    }

                    .pdp-reviews-grid {
                        display: grid;
                        grid-template-columns: 360px 1fr;
                        gap: 3rem;
                        align-items: start;
                    }

                    /* Left Sidebar: Rating Overview Card */
                    .rating-overview-card {
                        background: #f8fafc;
                        border: 1.5px solid #e2e8f0;
                        border-radius: 14px;
                        padding: 1.75rem;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.02);
                    }

                    .rating-score-large {
                        font-size: 3.5rem;
                        font-weight: 900;
                        color: #0f172a;
                        line-height: 1;
                        letter-spacing: -0.03em;
                    }

                    .rating-score-max {
                        font-size: 1.25rem;
                        color: #64748b;
                        font-weight: 700;
                        margin-left: 0.35rem;
                    }

                    .rating-stars-visual {
                        color: #f59e0b;
                        font-size: 1.4rem;
                        letter-spacing: 0.08em;
                        margin: 0.4rem 0 0.2rem;
                    }

                    .rating-total-label {
                        font-size: 0.85rem;
                        color: #64748b;
                        font-weight: 600;
                        margin-bottom: 1.5rem;
                    }

                    .star-breakdown-row {
                        display: flex;
                        align-items: center;
                        gap: 0.75rem;
                        font-size: 0.85rem;
                        margin-bottom: 0.7rem;
                    }

                    .star-breakdown-label {
                        width: 42px;
                        font-weight: 700;
                        color: #334155;
                    }

                    .star-progress-track {
                        flex: 1;
                        height: 10px;
                        background: #e2e8f0;
                        border-radius: 9999px;
                        overflow: hidden;
                    }

                    .star-progress-fill {
                        height: 100%;
                        border-radius: 9999px;
                        transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
                    }

                    .fill-5star { background: linear-gradient(90deg, #10b981, #059669); }
                    .fill-4star { background: linear-gradient(90deg, #22c55e, #16a34a); }
                    .fill-3star { background: linear-gradient(90deg, #f59e0b, #d97706); }
                    .fill-2star { background: linear-gradient(90deg, #f97316, #ea580c); }
                    .fill-1star { background: linear-gradient(90deg, #ef4444, #dc2626); }

                    .star-breakdown-pct {
                        width: 40px;
                        text-align: right;
                        font-weight: 700;
                        color: #475569;
                    }

                    .star-breakdown-count {
                        width: 32px;
                        text-align: right;
                        font-size: 0.78rem;
                        color: #94a3b8;
                    }

                    /* User's existing review card */
                    .user-existing-review-card {
                        background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
                        border: 1.5px solid #bfdbfe;
                        border-radius: 12px;
                        padding: 1.25rem;
                        margin-top: 1.5rem;
                        box-shadow: 0 4px 12px rgba(37, 99, 235, 0.08);
                    }

                    /* Review Items in Right Column */
                    .customer-review-card {
                        background: #ffffff;
                        border: 1.5px solid #e2e8f0;
                        border-radius: 14px;
                        padding: 1.5rem 1.75rem;
                        margin-bottom: 1.25rem;
                        box-shadow: 0 2px 8px -2px rgba(0,0,0,0.04);
                        transition: all 0.2s ease;
                    }

                    .customer-review-card:hover {
                        border-color: #cbd5e1;
                        box-shadow: 0 6px 16px -4px rgba(0,0,0,0.07);
                    }

                    .review-author-row {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        margin-bottom: 0.6rem;
                        flex-wrap: wrap;
                        gap: 0.5rem;
                    }

                    .review-author-info {
                        display: flex;
                        align-items: center;
                        gap: 0.75rem;
                    }

                    .review-avatar-circle {
                        width: 38px;
                        height: 38px;
                        border-radius: 50%;
                        background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
                        color: #0284c7;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-weight: 800;
                        font-size: 0.95rem;
                        border: 1.5px solid #7dd3fc;
                    }

                    .review-author-name {
                        font-weight: 800;
                        font-size: 0.98rem;
                        color: #0f172a;
                    }

                    .verified-buyer-chip {
                        background: #dcfce7;
                        color: #15803d;
                        font-size: 0.75rem;
                        font-weight: 800;
                        padding: 0.15rem 0.55rem;
                        border-radius: 9999px;
                        display: inline-flex;
                        align-items: center;
                        gap: 0.25rem;
                        border: 1px solid #bbf7d0;
                    }

                    .review-date-label {
                        font-size: 0.8rem;
                        color: #94a3b8;
                        font-weight: 600;
                    }

                    .review-content-title {
                        font-size: 1.05rem;
                        font-weight: 800;
                        color: #0f172a;
                        margin: 0 0 0.5rem;
                        display: flex;
                        align-items: center;
                        gap: 0.5rem;
                    }

                    .review-content-text {
                        font-size: 0.94rem;
                        color: #334155;
                        line-height: 1.65;
                        margin: 0 0 0.85rem;
                    }

                    .review-photos-grid {
                        display: flex;
                        gap: 0.75rem;
                        flex-wrap: wrap;
                        margin-top: 0.75rem;
                    }

                    .review-photo-thumb {
                        width: 85px;
                        height: 85px;
                        border-radius: 10px;
                        overflow: hidden;
                        border: 1.5px solid #e2e8f0;
                        cursor: pointer;
                        box-shadow: 0 2px 6px rgba(0,0,0,0.06);
                        transition: transform 0.2s ease, box-shadow 0.2s ease;
                    }

                    .review-photo-thumb:hover {
                        transform: scale(1.05);
                        box-shadow: 0 6px 14px rgba(0,0,0,0.12);
                        border-color: #3b82f6;
                    }

                    .review-photo-thumb img {
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                    }

                    /* Review Form Card */
                    .pdp-write-review-card {
                        background: #ffffff;
                        border: 2px solid #3b82f6;
                        border-radius: 16px;
                        padding: 2rem;
                        margin-top: 2rem;
                        box-shadow: 0 8px 20px -4px rgba(37, 99, 235, 0.1);
                    }

                    @media (max-width: 992px) {
                        .pdp-reviews-grid {
                            grid-template-columns: 1fr;
                            gap: 2rem;
                        }
                    }
                </style>
            </head>

            <body>

                <!-- 0. ADMIN STOREFRONT NOTIFICATION BAR -->
                <c:if test="${sessionScope.currentUser.admin}">
                    <div class="admin-storefront-bar">
                        <div class="admin-bar-left">
                            <span class="admin-crown-badge">👑 ADMIN MODE</span>
                            <span>ShopKart Control Center &bull; Logged in as
                                <strong>${sessionScope.currentUser.fullName}</strong></span>
                        </div>
                        <div class="admin-bar-actions">
                            <a href="${pageContext.request.contextPath}/admin/dashboard"
                                class="admin-bar-btn admin-bar-btn-primary">⚙️ Admin Console</a>
                            <a href="${pageContext.request.contextPath}/admin/products/edit?id=${product.productId}"
                                class="admin-bar-btn">✏️ Edit This Product</a>
                            <a href="${pageContext.request.contextPath}/admin/products" class="admin-bar-btn">📦
                                Catalog</a>
                            <a href="${pageContext.request.contextPath}/admin/orders" class="admin-bar-btn">🛒
                                Orders</a>
                        </div>
                    </div>
                </c:if>

                <!-- TOP ANNOUNCEMENT TICKER -->
                <header class="top-ticker">
                    <div class="ticker-text">
                        <a href="${pageContext.request.contextPath}/products?deals=true" style="color: inherit; text-decoration: none; display: inline-flex; align-items: center; gap: 0.5rem;">
                            <span class="ticker-badge">🔥 Flash Deals</span>
                            <span>⚡ Use Coupon <strong>SAVE20</strong> at Checkout for 20% Instant Discount &bull; Explore Deals &rarr;</span>
                        </a>
                    </div>
                    <div class="ticker-links">
                        <c:if test="${sessionScope.currentUser.admin}">
                            <a href="${pageContext.request.contextPath}/admin/dashboard"
                                style="color: #fbbf24; font-weight: 700;">⚙️ Admin Console</a>
                            <a href="${pageContext.request.contextPath}/health"
                                style="color: #38bdf8; font-weight: 700;">🩺 System Health</a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/products?brand=Apple">Brand Store</a>
                    </div>
                </header>

                <!-- 2. MAIN HEADER -->
                <nav class="main-header">
                    <div class="brand-group">
                        <a href="${pageContext.request.contextPath}/" class="brand-logo"
                            title="ShopKart - Premier Online Shopping">
                            <img src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="ShopKart"
                                class="logo-img">
                        </a>

                        <!-- Delivery Locator -->
                        <div class="delivery-locator" id="headerDeliveryTrigger" onclick="openPinCodeModal()"
                            title="Delivery Location">
                            <span class="loc-icon">📍</span>
                            <div class="loc-text">
                                <span class="sub">Deliver to</span>
                                <span class="main" id="headerPincodeText">Bengaluru 560100</span>
                            </div>
                        </div>
                    </div>

                    <!-- Global Search Bar with Live Autocomplete -->
                    <div class="header-search-wrapper">
                        <form action="${pageContext.request.contextPath}/products" method="GET"
                            class="header-search-form" id="headerSearchForm">
                            <select name="categoryId" class="category-select" id="searchCategorySelect">
                                <option value="">All Categories</option>
                            </select>
                            <input type="text" name="keyword" class="search-input" id="globalSearchInput"
                                placeholder="Search for products, brands and tech essentials..." autocomplete="off">
                            <button type="submit" class="search-button" aria-label="Search">🔍</button>
                        </form>
                        <div class="search-autocomplete-dropdown" id="searchAutocompleteDropdown"></div>
                    </div>

                    <!-- Header Actions -->
                    <div class="header-actions">
                        <!-- User Account Menu with Hover Dropdown -->
                        <div class="user-account-menu" id="userAccountMenu">
                            <c:choose>
                                <c:when test="${not empty sessionScope.currentUser}">
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
                                                <div
                                                    style="font-size: 0.7rem; color: #fbbf24; font-weight: 800; margin-top: 0.2rem;">
                                                    👑 ADMINISTRATOR</div>
                                            </c:if>
                                        </div>
                                        <c:choose>
                                            <c:when test="${sessionScope.currentUser.admin}">
                                                <a href="${pageContext.request.contextPath}/admin/dashboard"
                                                    style="color:var(--amazon-orange); font-weight:800;">⚙️ Admin
                                                    Control Panel</a>
                                                <a href="${pageContext.request.contextPath}/admin/products">📦 Manage
                                                    Products</a>
                                                <a href="${pageContext.request.contextPath}/admin/orders">🛒 Manage All
                                                    Orders</a>
                                                <a href="${pageContext.request.contextPath}/admin/reports/sales">📈
                                                    Sales & Revenue</a>
                                                <a href="${pageContext.request.contextPath}/admin/profile">👑 Admin
                                                    Profile</a>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/profile">👤 My Profile</a>
                                                <a href="${pageContext.request.contextPath}/orders">📦 My Orders</a>
                                                <a href="${pageContext.request.contextPath}/addresses">📍 Saved
                                                    Addresses</a>
                                                <a href="${pageContext.request.contextPath}/change-password">🔒 Change
                                                    Password</a>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="dropdown-divider"></div>
                                        <a href="${pageContext.request.contextPath}/logout"
                                            style="color:var(--danger); font-weight: 600;">🚪 Sign Out</a>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/login" class="user-nav-btn"
                                        title="Sign In to ShopKart">
                                        <span class="user-avatar-icon">👤</span>
                                        <span style="font-weight:600;">Sign In</span>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <a href="${pageContext.request.contextPath}/wishlist" class="header-action-inline"
                            title="Wishlist">
                            <span class="badge-icon">❤️</span>
                            <span>Wishlist</span>
                        </a>

                        <a href="${pageContext.request.contextPath}/cart" class="header-action-inline" title="Cart">
                            <div class="cart-icon-wrapper">
                                <span class="badge-icon">🛒</span>
                                <span class="badge-count" id="headerCartBadge">${not empty sessionScope.cart ?
                                    sessionScope.cart.totalQuantity : 0}</span>
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
                        <a href="${pageContext.request.contextPath}/products?category=1">💻 Laptops & Computers</a>
                        <a href="${pageContext.request.contextPath}/products?category=2">📱 Smartphones & Tablets</a>
                        <a href="${pageContext.request.contextPath}/products?category=3">🎧 Audio & Headphones</a>
                        <a href="${pageContext.request.contextPath}/products?category=4">⌚ Smartwatches</a>
                        <a href="${pageContext.request.contextPath}/products?category=5">👕 Men's Fashion</a>
                        <a href="${pageContext.request.contextPath}/products?category=6">👗 Women's Fashion</a>
                        <a href="${pageContext.request.contextPath}/products?category=8">🏠 Home & Kitchen</a>
                        <a href="${pageContext.request.contextPath}/products?deals=true" class="hot-deal">🔥 Flash Deals</a>
                        <a href="${pageContext.request.contextPath}/products?sortBy=price&sortDirection=ASC">🏷️ Under ₹50,000</a>
                    </div>
                </div>

                <!-- BREADCRUMB -->
                <div
                    style="max-width: 1500px; margin: 1rem auto 0; padding: 0 2rem; font-size: 0.8rem; color: var(--text-muted);">
                    <a href="${pageContext.request.contextPath}/" style="color: var(--primary);">Home</a> &gt;
                    <a href="${pageContext.request.contextPath}/products?category=${product.categoryId}"
                        style="color: var(--primary);">${not empty product.category ? product.category.categoryName :
                        'Catalog'}</a> &gt;
                    <span>${product.brand}</span> &gt;
                    <span>${product.productName}</span>
                </div>

                <!-- MAIN PRODUCT DETAIL CONTAINER (Amazon 3-Column Layout) -->
                <main class="pdp-container">

                    <!-- COLUMN 1: IMAGE GALLERY & ZOOM -->
                    <div class="pdp-gallery-column">
                        <div class="pdp-thumbnails">
                            <div class="pdp-thumb-item active" data-full-img="${product.primaryImageUrl}">
                                <img src="${product.primaryImageUrl}" alt="Thumbnail 1">
                            </div>
                            <c:forEach var="img" items="${product.images}">
                                <div class="pdp-thumb-item" data-full-img="${img.imageUrl}">
                                    <img src="${img.imageUrl}" alt="${img.altText}">
                                </div>
                            </c:forEach>
                        </div>

                        <div class="pdp-main-image-wrapper" id="pdpMainImgWrapper">
                            <img id="pdpMainImg"
                                src="${not empty product.primaryImageUrl ? product.primaryImageUrl : 'https://placehold.co/500x500?text=TechZone'}"
                                alt="${product.productName}">
                        </div>
                    </div>

                    <!-- COLUMN 2: PRODUCT DETAILS & OFFERS -->
                    <div class="pdp-details-column">
                        <div>
                            <a href="${pageContext.request.contextPath}/products?brand=${product.brand}"
                                class="pdp-brand-tag">Visit the ${product.brand} Store</a>
                            <h1 class="pdp-title">${product.productName}</h1>
                        </div>

                        <div class="pdp-rating-row">
                            <c:choose>
                                <c:when test="${ratingSummary.totalReviews > 0}">
                                    <span class="rating-badge">★ <fmt:formatNumber value="${ratingSummary.averageRating}" minFractionDigits="1" maxFractionDigits="1" /></span>
                                    <a href="#customerReviewsSection" style="color: var(--primary); font-weight: 700; text-decoration: none;">
                                        ${ratingSummary.totalReviews} ${ratingSummary.totalReviews == 1 ? 'rating & review' : 'ratings & reviews'}
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <span class="rating-badge" style="background: #e2e8f0; color: #475569;">★ New</span>
                                    <span style="color: var(--text-muted); font-size: 0.85rem;">No customer reviews yet</span>
                                </c:otherwise>
                            </c:choose>
                            <span>•</span>
                            <span style="color: var(--text-muted);">SKU: <c:out value="${product.sku}" /></span>
                        </div>

                        <!-- Price Breakdown -->
                        <div class="pdp-price-box">
                            <c:if test="${product.discountPercentage > 0}">
                                <div class="pdp-deal-header-row">
                                    <span class="pdp-deal-badge">🔥 Limited Time Deal</span>
                                    <span class="pdp-deal-timer-chip">⏳ Sale Ends Tonight</span>
                                </div>
                            </c:if>
                            <div class="pdp-price-main">
                                <c:if test="${product.discountPercentage > 0}">
                                    <span class="pdp-discount-pill">-<fmt:formatNumber value="${product.discountPercentage}" maxFractionDigits="0" />%</span>
                                </c:if>
                                <span class="pdp-price-currency">₹</span><span class="price-val"><fmt:formatNumber value="${product.effectivePrice}" pattern="#,##0" /></span>
                            </div>
                            <c:if test="${product.discountPercentage > 0}">
                                <div class="pdp-mrp-row">
                                    <span class="mrp-label">M.R.P.:</span>
                                    <span class="mrp-val">₹<fmt:formatNumber value="${product.price}" pattern="#,##0" /></span>
                                    <span class="pdp-savings-tag">You Save: <strong>₹<fmt:formatNumber value="${product.discountAmount}" pattern="#,##0" /></strong></span>
                                </div>
                            </c:if>
                            <div class="pdp-price-tax-note">
                                <span class="pdp-tax-badge">✓ Inclusive of all taxes</span>
                                <span class="pdp-tax-bullet">•</span>
                                <span>Free Express Delivery on this order</span>
                            </div>
                        </div>

                        <!-- Special Bank Offers Card -->
                        <div class="bank-offers-container">
                            <div class="bank-offer-header">
                                <span>💳</span> <strong>Available Bank Offers & Promotions</strong>
                            </div>
                            <div class="bank-offer-item">
                                • <strong>Instant Discount:</strong> 10% Instant Discount up to ₹1,500 on ICICI & HDFC
                                Credit Cards.
                            </div>
                            <div class="bank-offer-item">
                                • <strong>Special Coupon:</strong> Apply code <strong>SAVE20</strong> at cart for
                                additional 20% off.
                            </div>
                            <div class="bank-offer-item">
                                • <strong>No Cost EMI:</strong> Available on major credit cards starting ₹4,150/month.
                            </div>
                        </div>

                        <!-- Delivery Pincode Checker -->
                        <div class="pincode-checker-box">
                            <strong style="font-size: 0.9rem;">📍 Check Delivery & Availability</strong>
                            <div class="pincode-form">
                                <input type="text" id="pincodeInput" class="pincode-input"
                                    placeholder="Enter 6-digit delivery PIN code" maxlength="6">
                                <button type="button" id="checkPincodeBtn" class="pincode-btn">Check</button>
                            </div>
                            <div id="pincodeResult" class="pincode-result"></div>
                        </div>

                        <!-- Highlights / Description -->
                        <div class="pdp-highlights">
                            <h4>About this item</h4>
                            <p style="color: var(--text-secondary); line-height: 1.6; margin-bottom: 1rem;">
                                ${product.description}
                            </p>
                            <ul>
                                <li>100% Original Brand Product with Manufacturer Warranty</li>
                                <li>7-Day Replacement Guarantee & Doorstep Returns</li>
                                <li>Cash on Delivery and Instant UPI Payments Supported</li>
                                <li>24x7 Customer Priority Support & Assisted Setup</li>
                            </ul>
                        </div>
                    </div>

                    <!-- COLUMN 3: AMAZON-STYLE BUY BOX -->
                    <div class="pdp-buy-box">
                        <div>
                            <div style="font-size: 0.82rem; font-weight: 700; color: var(--text-muted); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.05em;">Total Price:</div>
                            <div class="buy-box-price"><span class="buy-box-currency">₹</span><span class="buy-box-val"><fmt:formatNumber value="${product.effectivePrice}" pattern="#,##0" /></span></div>
                        </div>

                        <div class="buy-box-stock">
                            <c:choose>
                                <c:when test="${product.stockQuantity > 0}">
                                    ✓ In Stock (${product.stockQuantity} available)
                                </c:when>
                                <c:otherwise>
                                    <span class="buy-box-stock out-of-stock">⚠️ Currently Out of Stock</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="buy-box-seller-info">
                            Ships from <strong>TechZone Express</strong><br>
                            Sold by <strong>TechZone Official Retail</strong> (4.9 ★)
                        </div>

                        <!-- Quantity Stepper -->
                        <div class="buy-box-qty-row">
                            <span>Quantity:</span>
                            <div class="qty-stepper">
                                <button type="button" class="qty-btn qty-minus">-</button>
                                <input type="text" id="pdpQtyInput" class="qty-input" value="1" readonly>
                                <button type="button" class="qty-btn qty-plus">+</button>
                            </div>
                        </div>

                        <div class="buy-box-actions" style="display: flex; flex-direction: column; gap: 0.75rem; width: 100%;">
                            <button type="button" class="btn-add-to-cart" style="width: 100%; box-sizing: border-box;"
                                onclick="quickAddToCart('${product.productId}', document.getElementById('pdpQtyInput').value, event)">
                                🛒 Add to Cart
                            </button>

                            <button type="button" class="btn-buy-now" style="width: 100%; box-sizing: border-box;"
                                onclick="quickBuyNow('${product.productId}', document.getElementById('pdpQtyInput').value, event)">
                            ⚡ Buy Now
                            </button>

                            <button type="button" class="hero-cta-btn"
                                style="width: 100%; box-sizing: border-box; background: transparent; color: var(--primary); border: 1px solid var(--border-color); box-shadow: none; padding: 0.85rem; justify-content: center; font-weight: 700;"
                                onclick="quickAddToWishlist('${product.productId}', event)">
                                ❤️ Add to Wishlist
                            </button>
                        </div>

                        <div class="buy-box-secure" style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; color: #64748b; margin-top: 0.5rem;">
                            <span style="font-size: 1.1rem;">🔒</span>
                            <span>Secure Transaction &bull; 100% Purchase Protection</span>
                        </div>
                    </div>
                </main>

                <!-- ==============================================================================
                     VERIFIED CUSTOMER REVIEWS & RATINGS (ALIGNED BELOW PRODUCT IMAGE & DETAILS)
                     ============================================================================== -->
                <section id="reviews" class="pdp-reviews-wrapper">
                    <div class="reviews-container-card">
                        
                        <c:if test="${param.reviewSubmitted eq 'true'}">
                            <div style="background: #ecfdf5; border: 1.5px solid #10b981; border-radius: 12px; padding: 1.1rem 1.5rem; margin-bottom: 2rem; display: flex; align-items: center; gap: 0.9rem; color: #065f46; font-weight: 700; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.1);">
                                <span style="font-size: 1.6rem;">🎉</span>
                                <div>
                                    Thank you! Your verified product review and photos have been submitted and published successfully.
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${not empty param.reviewError}">
                            <div style="background: #fef2f2; border: 1.5px solid #ef4444; border-radius: 12px; padding: 1.1rem 1.5rem; margin-bottom: 2rem; display: flex; align-items: center; gap: 0.9rem; color: #991b1b; font-weight: 700; box-shadow: 0 4px 12px rgba(239, 68, 68, 0.1);">
                                <span style="font-size: 1.6rem;">⚠️</span>
                                <div>
                                    <c:out value="${param.reviewError}" />
                                </div>
                            </div>
                        </c:if>

                        <!-- Header Bar -->
                        <div class="reviews-header-bar">
                            <div class="reviews-header-title">
                                <h2>Customer Reviews &amp; Ratings</h2>
                                <span class="reviews-count-badge">
                                    <c:choose>
                                        <c:when test="${not empty ratingSummary && ratingSummary.totalReviews > 0}">
                                            ${ratingSummary.totalReviews} ${ratingSummary.totalReviews == 1 ? 'Verified Rating' : 'Verified Ratings'}
                                        </c:when>
                                        <c:otherwise>0 Verified Ratings</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <c:if test="${canReview}">
                                <a href="#writeReviewForm" class="btn-write-review-hero">
                                    <span>${not empty existingReview ? '✏️' : '⭐'}</span> ${not empty existingReview ? 'Edit Your Review' : 'Write a Product Review'}
                                </a>
                            </c:if>
                        </div>

                        <!-- 2-Column Reviews Layout -->
                        <div id="customerReviewsSection" class="pdp-reviews-grid">
                            
                            <!-- LEFT COLUMN: Ratings Overview & Breakdown -->
                            <div class="reviews-summary-column">
                                <div class="rating-overview-card">
                                    <div style="display: flex; align-items: baseline; margin-bottom: 0.25rem;">
                                        <span class="rating-score-large">
                                            <c:choose>
                                                <c:when test="${not empty ratingSummary && ratingSummary.totalReviews > 0}">
                                                    <fmt:formatNumber value="${ratingSummary.averageRating}" minFractionDigits="1" maxFractionDigits="1" />
                                                </c:when>
                                                <c:otherwise>0.0</c:otherwise>
                                            </c:choose>
                                        </span>
                                        <span class="rating-score-max">/ 5.0</span>
                                    </div>

                                    <div class="rating-stars-visual">
                                        <c:choose>
                                            <c:when test="${not empty ratingSummary && ratingSummary.totalReviews > 0}">
                                                ${ratingSummary.starsDisplay}
                                            </c:when>
                                            <c:otherwise>☆☆☆☆☆</c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="rating-total-label">
                                        <c:choose>
                                            <c:when test="${not empty ratingSummary && ratingSummary.totalReviews > 0}">
                                                Based on <strong>${ratingSummary.totalReviews}</strong> verified buyer rating(s)
                                            </c:when>
                                            <c:otherwise>No ratings yet. Be the first verified buyer to review!</c:otherwise>
                                        </c:choose>
                                    </div>

                                    <!-- Star Distribution Breakdown Bars -->
                                    <div style="border-top: 1.5px solid #e2e8f0; padding-top: 1.25rem;">
                                        <!-- 5 Stars -->
                                        <div class="star-breakdown-row">
                                            <span class="star-breakdown-label">5 ★</span>
                                            <div class="star-progress-track">
                                                <div class="star-progress-fill fill-5star" style="width: ${not empty ratingSummary ? ratingSummary.fiveStarPercentage : 0}%;"></div>
                                            </div>
                                            <span class="star-breakdown-pct">${not empty ratingSummary ? ratingSummary.fiveStarPercentage : 0}%</span>
                                            <span class="star-breakdown-count">(${not empty ratingSummary ? ratingSummary.fiveStarCount : 0})</span>
                                        </div>
                                        <!-- 4 Stars -->
                                        <div class="star-breakdown-row">
                                            <span class="star-breakdown-label">4 ★</span>
                                            <div class="star-progress-track">
                                                <div class="star-progress-fill fill-4star" style="width: ${not empty ratingSummary ? ratingSummary.fourStarPercentage : 0}%;"></div>
                                            </div>
                                            <span class="star-breakdown-pct">${not empty ratingSummary ? ratingSummary.fourStarPercentage : 0}%</span>
                                            <span class="star-breakdown-count">(${not empty ratingSummary ? ratingSummary.fourStarCount : 0})</span>
                                        </div>
                                        <!-- 3 Stars -->
                                        <div class="star-breakdown-row">
                                            <span class="star-breakdown-label">3 ★</span>
                                            <div class="star-progress-track">
                                                <div class="star-progress-fill fill-3star" style="width: ${not empty ratingSummary ? ratingSummary.threeStarPercentage : 0}%;"></div>
                                            </div>
                                            <span class="star-breakdown-pct">${not empty ratingSummary ? ratingSummary.threeStarPercentage : 0}%</span>
                                            <span class="star-breakdown-count">(${not empty ratingSummary ? ratingSummary.threeStarCount : 0})</span>
                                        </div>
                                        <!-- 2 Stars -->
                                        <div class="star-breakdown-row">
                                            <span class="star-breakdown-label">2 ★</span>
                                            <div class="star-progress-track">
                                                <div class="star-progress-fill fill-2star" style="width: ${not empty ratingSummary ? ratingSummary.twoStarPercentage : 0}%;"></div>
                                            </div>
                                            <span class="star-breakdown-pct">${not empty ratingSummary ? ratingSummary.twoStarPercentage : 0}%</span>
                                            <span class="star-breakdown-count">(${not empty ratingSummary ? ratingSummary.twoStarCount : 0})</span>
                                        </div>
                                        <!-- 1 Star -->
                                        <div class="star-breakdown-row">
                                            <span class="star-breakdown-label">1 ★</span>
                                            <div class="star-progress-track">
                                                <div class="star-progress-fill fill-1star" style="width: ${not empty ratingSummary ? ratingSummary.oneStarPercentage : 0}%;"></div>
                                            </div>
                                            <span class="star-breakdown-pct">${not empty ratingSummary ? ratingSummary.oneStarPercentage : 0}%</span>
                                            <span class="star-breakdown-count">(${not empty ratingSummary ? ratingSummary.oneStarCount : 0})</span>
                                        </div>
                                    </div>
                                </div>

                                <!-- Current User's Existing Review Card -->
                                <c:if test="${not empty existingReview}">
                                    <div class="user-existing-review-card">
                                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                                            <span style="font-size: 0.85rem; font-weight: 800; color: #1e40af; display: flex; align-items: center; gap: 0.35rem;">
                                                <span>⭐</span> Your Review
                                            </span>
                                            <span style="color: #f59e0b; font-size: 0.95rem; font-weight: 800;">
                                                <c:forEach begin="1" end="${existingReview.rating}">★</c:forEach><c:forEach begin="${existingReview.rating + 1}" end="5">☆</c:forEach>
                                            </span>
                                        </div>
                                        <div style="font-size: 0.92rem; font-weight: 800; color: #0f172a; margin-bottom: 0.35rem;">
                                            <c:out value="${existingReview.title}" />
                                        </div>
                                        <div style="font-size: 0.84rem; color: #334155; line-height: 1.5; margin-bottom: 0.75rem;">
                                            <c:out value="${existingReview.comment}" />
                                        </div>
                                        <c:if test="${not empty existingReview.imageUrl || not empty existingReview.imageUrls}">
                                            <div style="display: flex; gap: 0.4rem; margin-bottom: 0.75rem; flex-wrap: wrap;">
                                                <c:choose>
                                                    <c:when test="${not empty existingReview.imageUrls}">
                                                        <c:forEach var="img" items="${existingReview.imageUrls}">
                                                            <img src="${img}" alt="Your review photo" style="width: 44px; height: 44px; object-fit: cover; border-radius: 6px; border: 1px solid #bfdbfe; cursor: pointer;" onclick="openReviewLightbox('${img}', '<c:out value="${existingReview.title}" />')">
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:when test="${not empty existingReview.imageUrl}">
                                                        <img src="${existingReview.imageUrl}" alt="Your review photo" style="width: 44px; height: 44px; object-fit: cover; border-radius: 6px; border: 1px solid #bfdbfe; cursor: pointer;" onclick="openReviewLightbox('${existingReview.imageUrl}', '<c:out value="${existingReview.title}" />')">
                                                    </c:when>
                                                </c:choose>
                                            </div>
                                        </c:if>
                                        <a href="#writeReviewForm" style="display: inline-flex; align-items: center; gap: 0.3rem; font-size: 0.82rem; font-weight: 800; color: #2563eb; text-decoration: none;">
                                            ✏️ Edit / Update Your Review &rarr;
                                        </a>
                                    </div>
                                </c:if>

                                <!-- Verified Buyer Guarantee Banner -->
                                <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem; margin-top: 1.25rem; font-size: 0.82rem; color: #475569; line-height: 1.6;">
                                    <div style="font-weight: 800; color: #0f172a; margin-bottom: 0.35rem; display: flex; align-items: center; gap: 0.4rem;">
                                        <span>🛡️</span> Verified Purchase Guarantee
                                    </div>
                                    Reviews with the <span style="color:#15803d; font-weight:700;">✓ Verified Delivered Buyer</span> badge are submitted exclusively by verified customers after successful order delivery.
                                </div>
                            </div>

                            <!-- RIGHT COLUMN: Customer Reviews List & Submission Form -->
                            <div class="reviews-content-column">

                                <!-- Review Cards -->
                                <c:choose>
                                    <c:when test="${not empty reviews && not empty reviews.items}">
                                        <div style="margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center;">
                                            <span style="font-size: 0.95rem; font-weight: 800; color: #0f172a;">
                                                Top Customer Reviews (${reviews.totalItems})
                                            </span>
                                            <span style="font-size: 0.82rem; color: #64748b;">
                                                Most relevant verified feedback
                                            </span>
                                        </div>

                                        <c:forEach var="rev" items="${reviews.items}">
                                            <div class="customer-review-card">
                                                
                                                <!-- Reviewer Header -->
                                                <div class="review-author-row">
                                                    <div class="review-author-info">
                                                        <div class="review-avatar-circle">
                                                            ${not empty rev.customerName ? rev.customerName.substring(0, 1).toUpperCase() : 'U'}
                                                        </div>
                                                        <div>
                                                            <div class="review-author-name">
                                                                ${not empty rev.customerName ? rev.customerName : 'Verified Customer'}
                                                            </div>
                                                            <c:if test="${rev.verifiedPurchase}">
                                                                <span class="verified-buyer-chip">
                                                                    <span>✓</span> Verified Delivered Buyer
                                                                </span>
                                                            </c:if>
                                                        </div>
                                                    </div>
                                                    <div class="review-date-label">
                                                        ${rev.formattedCreatedAt}
                                                        <c:if test="${rev.edited}">
                                                            <span style="color: #64748b; font-style: italic; font-size: 0.75rem; margin-left: 0.25rem;">(Edited)</span>
                                                        </c:if>
                                                    </div>
                                                </div>

                                                <!-- Rating & Title -->
                                                <div style="display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.5rem; flex-wrap: wrap;">
                                                    <span style="color: #f59e0b; font-size: 1.1rem; letter-spacing: 0.05em;">
                                                        <c:forEach begin="1" end="${rev.rating}">★</c:forEach><c:forEach begin="${rev.rating + 1}" end="5">☆</c:forEach>
                                                    </span>
                                                    <h3 class="review-content-title" style="margin: 0;">
                                                        <c:out value="${rev.title}" />
                                                    </h3>
                                                </div>

                                                <!-- Review Body -->
                                                <div class="review-content-text">
                                                    <c:out value="${rev.comment}" />
                                                </div>

                                                <!-- Review Photos Gallery -->
                                                <c:if test="${not empty rev.imageUrl || not empty rev.imageUrls}">
                                                    <div class="review-photos-grid">
                                                        <c:choose>
                                                            <c:when test="${not empty rev.imageUrls}">
                                                                <c:forEach var="img" items="${rev.imageUrls}">
                                                                    <div class="review-photo-thumb" onclick="openReviewLightbox('${img}', '<c:out value="${rev.title}" />')">
                                                                        <img src="${img}" alt="Customer photo for ${product.productName}">
                                                                    </div>
                                                                </c:forEach>
                                                            </c:when>
                                                            <c:when test="${not empty rev.imageUrl}">
                                                                <div class="review-photo-thumb" onclick="openReviewLightbox('${rev.imageUrl}', '<c:out value="${rev.title}" />')">
                                                                    <img src="${rev.imageUrl}" alt="Customer photo for ${product.productName}">
                                                                </div>
                                                            </c:when>
                                                        </c:choose>
                                                    </div>
                                                </c:if>
                                            </div>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <div style="text-align: center; padding: 3.5rem 1.5rem; background: #f8fafc; border: 1.5px dashed #cbd5e1; border-radius: 16px; margin-bottom: 2rem;">
                                            <div style="font-size: 3rem; margin-bottom: 0.75rem;">💬</div>
                                            <h3 style="font-size: 1.25rem; font-weight: 800; color: #0f172a; margin: 0 0 0.4rem;">No Customer Reviews Yet</h3>
                                            <p style="color: #64748b; font-size: 0.92rem; max-width: 460px; margin: 0 auto 1.25rem;">
                                                Be the first verified customer to share your thoughts and photos about the ${product.productName}!
                                            </p>
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <!-- Write / Edit Review Form for Verified Buyers -->
                                <c:if test="${canReview}">
                                    <div id="writeReviewForm" class="pdp-write-review-card">
                                        <div style="border-bottom: 1.5px solid #e2e8f0; padding-bottom: 1rem; margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center;">
                                            <div>
                                                <h3 style="font-size: 1.25rem; font-weight: 900; color: #0f172a; margin: 0 0 0.25rem; display: flex; align-items: center; gap: 0.5rem;">
                                                    <span>${not empty existingReview ? '✏️' : '✍️'}</span> ${not empty existingReview ? 'Edit Your Verified Review' : 'Write a Verified Product Review'}
                                                </h3>
                                                <p style="font-size: 0.85rem; color: #64748b; margin: 0;">
                                                    ${not empty existingReview ? 'You have already reviewed this product. Updating this form will edit your existing review.' : 'Share your authentic experience to help other buyers make informed choices.'}
                                                </p>
                                            </div>
                                            <span class="verified-buyer-chip" style="font-size: 0.8rem; padding: 0.3rem 0.75rem;">
                                                <span>✓</span> Verified Delivery
                                            </span>
                                        </div>

                                        <form action="${pageContext.request.contextPath}/review" method="POST" enctype="multipart/form-data">
                                            <input type="hidden" name="productId" value="${product.productId}">
                                            <c:if test="${not empty deliveredOrderId}">
                                                <input type="hidden" name="orderId" value="${deliveredOrderId}">
                                            </c:if>

                                            <!-- Star Rating -->
                                            <div style="margin-bottom: 1.5rem;">
                                                <label style="display: block; font-size: 0.88rem; font-weight: 800; color: #334155; margin-bottom: 0.4rem;">
                                                    Overall Rating <span style="color: #ef4444;">*</span>
                                                </label>
                                                <div style="display: flex; align-items: center; gap: 0.5rem;">
                                                    <input type="hidden" name="rating" id="productPageRatingScore" value="${not empty existingReview ? existingReview.rating : 5}">
                                                    <div style="display: flex; gap: 0.35rem; font-size: 2rem; cursor: pointer;">
                                                        <c:set var="curRating" value="${not empty existingReview ? existingReview.rating : 5}" />
                                                        <span id="pdpStar_1" onclick="setPdpRating(1)" style="color: ${curRating >= 1 ? '#f59e0b' : '#cbd5e1'}; transition: transform 0.1s;" onmouseover="this.style.transform='scale(1.2)'" onmouseout="this.style.transform='scale(1)'">★</span>
                                                        <span id="pdpStar_2" onclick="setPdpRating(2)" style="color: ${curRating >= 2 ? '#f59e0b' : '#cbd5e1'}; transition: transform 0.1s;" onmouseover="this.style.transform='scale(1.2)'" onmouseout="this.style.transform='scale(1)'">★</span>
                                                        <span id="pdpStar_3" onclick="setPdpRating(3)" style="color: ${curRating >= 3 ? '#f59e0b' : '#cbd5e1'}; transition: transform 0.1s;" onmouseover="this.style.transform='scale(1.2)'" onmouseout="this.style.transform='scale(1)'">★</span>
                                                        <span id="pdpStar_4" onclick="setPdpRating(4)" style="color: ${curRating >= 4 ? '#f59e0b' : '#cbd5e1'}; transition: transform 0.1s;" onmouseover="this.style.transform='scale(1.2)'" onmouseout="this.style.transform='scale(1)'">★</span>
                                                        <span id="pdpStar_5" onclick="setPdpRating(5)" style="color: ${curRating >= 5 ? '#f59e0b' : '#cbd5e1'}; transition: transform 0.1s;" onmouseover="this.style.transform='scale(1.2)'" onmouseout="this.style.transform='scale(1)'">★</span>
                                                    </div>
                                                    <span id="pdpRatingDesc" style="font-size: 0.92rem; font-weight: 800; color: #0f172a; margin-left: 0.5rem;">
                                                        ${curRating} / 5 Stars
                                                    </span>
                                                </div>
                                            </div>

                                            <!-- Headline -->
                                            <div style="margin-bottom: 1.25rem;">
                                                <label for="pdpReviewTitle" style="display: block; font-size: 0.88rem; font-weight: 800; color: #334155; margin-bottom: 0.35rem;">
                                                    Review Headline <span style="color: #ef4444;">*</span>
                                                </label>
                                                <input type="text" 
                                                       id="pdpReviewTitle" 
                                                       name="title" 
                                                       required 
                                                       maxlength="150"
                                                       value="<c:out value='${existingReview.title}' />"
                                                       placeholder="e.g. Outstanding performance and super fast delivery!" 
                                                       style="width: 100%; box-sizing: border-box; padding: 0.8rem 1rem; border: 1.5px solid #cbd5e1; border-radius: 8px; font-size: 0.95rem; font-weight: 500; outline: none; transition: border-color 0.2s;"
                                                       onfocus="this.style.borderColor='#2563eb'" onblur="this.style.borderColor='#cbd5e1'">
                                            </div>

                                            <!-- Text Area -->
                                            <div style="margin-bottom: 1.25rem;">
                                                <label for="pdpReviewComment" style="display: block; font-size: 0.88rem; font-weight: 800; color: #334155; margin-bottom: 0.35rem;">
                                                    Detailed Review &amp; Experience <span style="color: #ef4444;">*</span>
                                                </label>
                                                <textarea id="pdpReviewComment" 
                                                          name="comment" 
                                                          required 
                                                          rows="4" 
                                                          maxlength="2000"
                                                          placeholder="What did you like or dislike? How was the build quality, display, battery, or fit?" 
                                                          style="width: 100%; box-sizing: border-box; padding: 0.8rem 1rem; border: 1.5px solid #cbd5e1; border-radius: 8px; font-size: 0.95rem; font-weight: 500; outline: none; resize: vertical; line-height: 1.6; transition: border-color 0.2s;"
                                                          onfocus="this.style.borderColor='#2563eb'" onblur="this.style.borderColor='#cbd5e1'"><c:out value='${existingReview.comment}' /></textarea>
                                            </div>

                                            <!-- Photo Upload -->
                                            <div style="margin-bottom: 1.75rem;">
                                                <label style="display: block; font-size: 0.88rem; font-weight: 800; color: #334155; margin-bottom: 0.35rem;">
                                                    📸 Add Product Photos (Unboxing / Product in Hand)
                                                </label>
                                                <div style="border: 2px dashed #94a3b8; background: #f8fafc; border-radius: 12px; padding: 1.4rem; text-align: center; cursor: pointer; transition: all 0.2s;"
                                                     onclick="document.getElementById('pdpReviewImageInput').click();"
                                                     onmouseover="this.style.borderColor='#2563eb'; this.style.background='#eff6ff';"
                                                     onmouseout="this.style.borderColor='#94a3b8'; this.style.background='#f8fafc';">
                                                    <div style="font-size: 2rem; margin-bottom: 0.35rem;">📷</div>
                                                    <div style="font-size: 0.92rem; font-weight: 800; color: #1e293b;">Click to upload product photos</div>
                                                    <div style="font-size: 0.78rem; color: #64748b; margin-top: 0.25rem;">Supports JPG, PNG, WebP up to 10MB</div>
                                                    <input type="file" 
                                                           id="pdpReviewImageInput" 
                                                           name="reviewImage" 
                                                           accept="image/*" 
                                                           onchange="previewPdpReviewImage(this)" 
                                                           style="display: none;">
                                                </div>
                                                <div id="pdpImagePreviewBox" style="display: none; align-items: center; gap: 0.75rem; margin-top: 0.75rem; padding: 0.75rem; background: #f1f5f9; border-radius: 10px; border: 1px solid #e2e8f0;">
                                                    <img id="pdpImagePreview" src="" alt="Preview" style="width: 55px; height: 55px; object-fit: cover; border-radius: 8px; border: 1.5px solid #cbd5e1;">
                                                    <span id="pdpImageName" style="font-size: 0.85rem; color: #166534; font-weight: 700;"></span>
                                                </div>
                                            </div>

                                            <button type="submit" style="background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: #ffffff; border: none; padding: 0.85rem 1.85rem; border-radius: 8px; font-weight: 800; font-size: 0.95rem; cursor: pointer; display: inline-flex; align-items: center; gap: 0.5rem; box-shadow: 0 4px 14px rgba(37, 99, 235, 0.3); transition: transform 0.15s, box-shadow 0.15s;"
                                                    onmouseover="this.style.transform='translateY(-2px)'; this.style.boxShadow='0 6px 18px rgba(37, 99, 235, 0.4)';"
                                                    onmouseout="this.style.transform='none'; this.style.boxShadow='0 4px 14px rgba(37, 99, 235, 0.3)';">
                                                <span>✍️</span> ${not empty existingReview ? 'Update Verified Review' : 'Submit Verified Review'}
                                            </button>
                                        </form>
                                    </div>
                                </c:if>

                            </div>
                        </div>
                    </div>
                </section>

                <!-- Review Image Lightbox Zoom Modal -->
                <div id="reviewLightboxModal" style="display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.85); backdrop-filter: blur(5px); -webkit-backdrop-filter: blur(5px); z-index: 100000; align-items: center; justify-content: center; padding: 1.5rem;" onclick="closeReviewLightbox()">
                    <div style="position: relative; max-width: 800px; max-height: 85vh; text-align: center;" onclick="event.stopPropagation();">
                        <button type="button" onclick="closeReviewLightbox()" style="position: absolute; top: -40px; right: 0; background: none; border: none; color: #ffffff; font-size: 2rem; cursor: pointer;">✕</button>
                        <img id="reviewLightboxImg" src="" alt="Customer Photo Zoom" style="max-width: 100%; max-height: 80vh; border-radius: 12px; box-shadow: 0 20px 40px rgba(0,0,0,0.5);">
                        <div id="reviewLightboxCaption" style="color: #cbd5e1; font-size: 0.95rem; font-weight: 700; margin-top: 0.75rem;"></div>
                    </div>
                </div>

                <script>
                    function openReviewLightbox(src, caption) {
                        const modal = document.getElementById('reviewLightboxModal');
                        document.getElementById('reviewLightboxImg').src = src;
                        document.getElementById('reviewLightboxCaption').innerText = caption || 'Customer Product Photo';
                        modal.style.display = 'flex';
                        document.body.style.overflow = 'hidden';
                    }

                    function closeReviewLightbox() {
                        const modal = document.getElementById('reviewLightboxModal');
                        modal.style.display = 'none';
                        document.body.style.overflow = '';
                    }

                    function setPdpRating(score) {
                        document.getElementById('productPageRatingScore').value = score;
                        document.getElementById('pdpRatingDesc').innerText = score + ' / 5 Stars';
                        for (let i = 1; i <= 5; i++) {
                            const star = document.getElementById('pdpStar_' + i);
                            if (star) {
                                star.style.color = (i <= score) ? '#f59e0b' : '#cbd5e1';
                            }
                        }
                    }

                    function previewPdpReviewImage(input) {
                        const box = document.getElementById('pdpImagePreviewBox');
                        const img = document.getElementById('pdpImagePreview');
                        const label = document.getElementById('pdpImageName');
                        if (input.files && input.files[0]) {
                            const file = input.files[0];
                            const reader = new FileReader();
                            reader.onload = function(e) {
                                img.src = e.target.result;
                                label.innerText = '✓ ' + file.name;
                                box.style.display = 'flex';
                            };
                            reader.readAsDataURL(file);
                        } else {
                            box.style.display = 'none';
                        }
                    }
                </script>

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