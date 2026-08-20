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
    <title>My Orders | ShopKart India</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=4.0">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>

    <!-- 1. TOP TICKER STRIP -->
    <header class="top-ticker">
        <div class="ticker-text">
            <span class="ticker-badge">⚡ ShopKart Fast</span>
            <span>Free Express Delivery on all orders over ₹499 | 100% Verified Sourcing</span>
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
            <div class="delivery-locator" id="headerDeliveryTrigger" onclick="openPinCodeModal()" title="Change Delivery Location">
                <span class="loc-icon">📍</span>
                <div class="loc-text">
                    <span class="sub">Deliver to</span>
                    <span class="main" id="headerPincodeText">Bengaluru 560100</span>
                </div>
            </div>
        </div>

        <!-- Global Search Bar -->
        <div class="header-search-wrapper">
            <form action="${pageContext.request.contextPath}/products" method="GET" class="header-search-form">
                <input type="text" name="keyword" class="search-input" id="globalSearchInput" placeholder="Search for products, brands and tech essentials..." autocomplete="off">
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

    <!-- 3. SECONDARY SUB-NAVBAR -->
    <div class="sub-navbar">
        <a href="${pageContext.request.contextPath}/products" class="all-categories-btn">
            <span>☰</span> <strong>All Departments</strong>
        </a>
        <div class="nav-links-strip">
            <a href="${pageContext.request.contextPath}/products?category=1">💻 Laptops & PCs</a>
            <a href="${pageContext.request.contextPath}/products?category=2">📱 Mobiles & Tablets</a>
            <a href="${pageContext.request.contextPath}/products?category=3">🎧 Audio & Studio</a>
            <a href="${pageContext.request.contextPath}/products?category=5">👕 Fashion & Wearables</a>
            <a href="${pageContext.request.contextPath}/products?category=8">🏠 Home & Kitchen</a>
        </div>
    </div>

    <!-- MAIN ORDERS LAYOUT -->
    <main class="orders-page-layout">
        
        <!-- Left Customer Navigation Sidebar -->
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
                <li><a href="${pageContext.request.contextPath}/orders" class="profile-nav-link active">📦 My Orders</a></li>
                <li><a href="${pageContext.request.contextPath}/wishlist" class="profile-nav-link">❤️ My Wishlist</a></li>
                <li><a href="${pageContext.request.contextPath}/profile" class="profile-nav-link">👤 Personal Info</a></li>
                <li><a href="${pageContext.request.contextPath}/addresses" class="profile-nav-link">📍 Saved Addresses</a></li>
                <li><a href="${pageContext.request.contextPath}/change-password" class="profile-nav-link">🔒 Change Password</a></li>
            </ul>
        </aside>

        <!-- Right Orders Content -->
        <section>
            <div style="margin-bottom: 1.5rem;">
                <h1 style="font-size: 1.6rem; font-weight: 900; color: var(--text-primary); margin-bottom: 0.25rem;">Your Orders</h1>
                <p style="color: var(--text-muted); font-size: 0.9rem;">Track live shipments, download tax invoices, and manage past purchases.</p>
            </div>

            <!-- Order Filter & Sort Toolbar -->
            <div class="orders-toolbar" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem; background: #ffffff; padding: 0.85rem 1.25rem; border-radius: 12px; border: 1px solid var(--border-color); box-shadow: var(--shadow-sm);">
                <!-- Order Filter Tabs -->
                <div class="order-filter-bar" id="orderFilterBar" style="display: flex; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0;">
                    <button type="button" class="order-filter-tab ${selectedStatus == 'ALL' || empty selectedStatus ? 'active' : ''}" onclick="filterOrders('ALL', this)">All Orders</button>
                    <button type="button" class="order-filter-tab ${selectedStatus == 'PROCESSING' || selectedStatus == 'IN_PROGRESS' ? 'active' : ''}" onclick="filterOrders('PROCESSING', this)">⏳ In Progress</button>
                    <button type="button" class="order-filter-tab ${selectedStatus == 'DISPATCHED' ? 'active' : ''}" onclick="filterOrders('DISPATCHED', this)">🚚 Dispatched</button>
                    <button type="button" class="order-filter-tab ${selectedStatus == 'IN_TRANSIT' || selectedStatus == 'SHIPPED' ? 'active' : ''}" onclick="filterOrders('IN_TRANSIT', this)">✈️ In Transit</button>
                    <button type="button" class="order-filter-tab ${selectedStatus == 'DELIVERED' ? 'active' : ''}" onclick="filterOrders('DELIVERED', this)">✓ Delivered</button>
                    <button type="button" class="order-filter-tab ${selectedStatus == 'CANCELLED' ? 'active' : ''}" onclick="filterOrders('CANCELLED', this)">✕ Cancelled</button>
                </div>

                <!-- Sort Order Dropdown & Quick Search -->
                <div class="order-sort-wrapper" style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                    <div style="display: flex; align-items: center; gap: 0.4rem; background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 8px; padding: 0.35rem 0.75rem;">
                        <span style="font-size: 0.82rem; font-weight: 700; color: #475569;">Sort by:</span>
                        <select id="orderSortSelect" onchange="handleOrderSortChange(this.value)" style="border: none; background: transparent; font-size: 0.85rem; font-weight: 700; color: #0f172a; outline: none; cursor: pointer;">
                            <option value="newest" ${selectedSort == 'newest' || empty selectedSort ? 'selected' : ''}>📅 Date: Newest First</option>
                            <option value="oldest" ${selectedSort == 'oldest' ? 'selected' : ''}>📅 Date: Oldest First</option>
                            <option value="price_high" ${selectedSort == 'price_high' ? 'selected' : ''}>💰 Price: High to Low</option>
                            <option value="price_low" ${selectedSort == 'price_low' ? 'selected' : ''}>💰 Price: Low to High</option>
                            <option value="order_num" ${selectedSort == 'order_num' ? 'selected' : ''}>🔢 Order Number</option>
                        </select>
                    </div>

                    <div style="position: relative;">
                        <input type="text" id="orderSearchInput" oninput="handleOrderSearch(this.value)" placeholder="Search orders..." style="padding: 0.4rem 0.75rem 0.4rem 2rem; border-radius: 8px; border: 1px solid #cbd5e1; font-size: 0.85rem; outline: none; background: #f8fafc; width: 160px;">
                        <span style="position: absolute; left: 0.6rem; top: 50%; transform: translateY(-50%); font-size: 0.8rem; pointer-events: none; color: #94a3b8;">🔍</span>
                    </div>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty pagination.items}">
                    <!-- Empty Orders State -->
                    <div class="order-card-wrapper" style="text-align: center; padding: 4rem 2rem; border-style: dashed; border-width: 2px;">
                        <div style="font-size: 4rem; margin-bottom: 1rem; animation: floatAnim 3s ease-in-out infinite;">📦</div>
                        <h2 style="font-size: 1.5rem; font-weight: 900; color: #0f172a; margin-bottom: 0.5rem;">No orders placed yet</h2>
                        <p style="color: var(--text-muted); margin-bottom: 1.75rem; max-width: 420px; margin-left: auto; margin-right: auto; line-height: 1.6;">
                            Looks like you haven't placed an order yet. Explore our curated collections and tech essentials with superfast doorstep delivery!
                        </p>
                        <a href="${pageContext.request.contextPath}/products?deals=true" class="hero-cta-btn" style="padding: 0.75rem 2rem; font-size: 1rem; border-radius: var(--radius-full);">
                            Explore Trending Deals →
                        </a>
                    </div>
                </c:when>

                <c:otherwise>
                    <!-- List of Order Cards Container -->
                    <div id="ordersCardsContainer">
                        <c:forEach var="order" items="${pagination.items}">
                            <div class="order-card-wrapper order-record-card" 
                                 data-status="${order.orderStatus}"
                                 data-timestamp="${order.createdAtEpochMillis}"
                                 data-amount="${order.totalAmount}"
                                 data-order-number="${order.orderNumber}"
                                 data-search="${order.orderNumber} ${order.shippingFullName} ${order.shippingCity} ${order.shippingPostalCode}">
                                <!-- Card Header -->
                                <div class="order-card-top">
                                    <div class="order-meta-col">
                                        <span class="meta-label">Order Placed</span>
                                        <span class="meta-val"><c:choose><c:when test="${not empty order.formattedCreatedAt}">${order.formattedCreatedAt}</c:when><c:otherwise>${order.createdAt}</c:otherwise></c:choose></span>
                                    </div>

                                    <div class="order-meta-col">
                                        <span class="meta-label">Total Amount</span>
                                        <span class="meta-val" style="color: #2563eb; font-size: 1.05rem;">₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2" /></span>
                                    </div>

                                    <div class="order-meta-col">
                                        <span class="meta-label">Ship To</span>
                                        <span class="meta-val" title="Delivery Address">📍 <c:out value="${order.shippingFullName}" /></span>
                                    </div>

                                    <div class="order-meta-col" style="margin-left: auto; text-align: right;">
                                        <span class="meta-label">Order # <c:out value="${order.orderNumber}" /></span>
                                        <a href="${pageContext.request.contextPath}/order?id=${order.orderNumber}" style="color: #2563eb; font-weight: 800; text-decoration: none; font-size: 0.9rem;">
                                            View Order & Invoice ➔
                                        </a>
                                    </div>
                                </div>

                                <!-- Card Body -->
                                <div class="order-card-content">
                                    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.25rem; flex-wrap: wrap; gap: 0.75rem;">
                                        <div>
                                            <c:choose>
                                                <c:when test="${order.orderStatus == 'DELIVERED'}">
                                                    <span class="pill-badge badge-success" style="background: #dcfce7; color: #15803d; border: 1px solid #86efac; font-weight: 800;">
                                                        ✓ Delivered on <c:out value="${order.formattedDeliveredDate}" />
                                                    </span>
                                                </c:when>
                                                <c:when test="${order.orderStatus == 'OUT_FOR_DELIVERY'}">
                                                    <span class="pill-badge badge-warning" style="background:#fef3c7; color:#92400e; border:1px solid #fde68a;">
                                                        🛵 Out for Delivery
                                                    </span>
                                                </c:when>
                                                <c:when test="${order.orderStatus == 'IN_TRANSIT' || order.orderStatus == 'SHIPPED'}">
                                                    <span class="pill-badge badge-info">
                                                        ✈️ In Transit
                                                    </span>
                                                </c:when>
                                                <c:when test="${order.orderStatus == 'DISPATCHED'}">
                                                    <span class="pill-badge badge-info" style="background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe;">
                                                        🚚 Dispatched <c:if test="${not empty order.courierPartner}">(${order.courierPartner})</c:if>
                                                    </span>
                                                </c:when>
                                                <c:when test="${order.orderStatus == 'PROCESSING'}">
                                                    <span class="pill-badge badge-warning">
                                                        📦 Dispatch in Progress
                                                    </span>
                                                </c:when>
                                                <c:when test="${order.orderStatus == 'CANCELLED'}">
                                                    <span class="pill-badge badge-danger">
                                                        ✕ Cancelled
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="pill-badge badge-info">
                                                        🛒 Order Confirmed
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                            <div style="font-size: 0.82rem; color: var(--text-muted); margin-top: 0.35rem;">
                                                Delivering to: <strong><c:out value="${order.shippingCity}" />, <c:out value="${order.shippingState}" /> - <c:out value="${order.shippingPostalCode}" /></strong>
                                            </div>
                                        </div>

                                        <div style="display: flex; gap: 0.65rem; align-items: center; flex-wrap: wrap;">
                                            <c:choose>
                                                <c:when test="${order.paymentMethod == 'COD'}">
                                                    <span class="pill-badge" style="background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; font-weight: 700;">
                                                        💵 Cash on Delivery<c:if test="${order.paymentStatus == 'PAID' || order.orderStatus == 'DELIVERED'}"> <span style="color: #15803d; font-weight: 800; margin-left: 0.25rem;">(PAID)</span></c:if>
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="pill-badge" style="background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; font-weight: 700;">
                                                        💳 <c:out value="${order.paymentMethod}" /> (<c:out value="${order.paymentStatus}" />)
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                            <c:choose>
                                                <c:when test="${order.orderStatus == 'DELIVERED'}">
                                                    <a href="${pageContext.request.contextPath}/order?id=${order.orderNumber}" class="order-btn-primary" style="display: inline-flex; align-items: center; gap: 0.4rem; font-weight: 800; font-size: 0.82rem; padding: 0.42rem 0.95rem; text-decoration: none; border-radius: 8px; background: linear-gradient(135deg, #059669 0%, #047857 100%); color: #ffffff; box-shadow: 0 2px 6px rgba(5, 150, 105, 0.25);">
                                                        📦 View Order
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageContext.request.contextPath}/order?id=${order.orderNumber}" class="order-btn-primary" style="display: inline-flex; align-items: center; gap: 0.4rem; font-weight: 800; font-size: 0.82rem; padding: 0.42rem 0.95rem; text-decoration: none; border-radius: 8px; background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: #ffffff; box-shadow: 0 2px 6px rgba(37, 99, 235, 0.25);">
                                                        🚚 Track Order
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <!-- Order Items Rows -->
                                    <c:forEach var="item" items="${order.items}">
                                        <div class="order-product-item">
                                            <div class="order-product-thumb" style="display: flex; align-items: center; justify-content: center; font-size: 1.75rem; background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);">
                                                📦
                                            </div>
                                            
                                            <div class="order-product-details">
                                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}" class="item-name">
                                                    <c:out value="${item.productName}" />
                                                </a>
                                                <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.25rem;">
                                                    SKU: <code><c:out value="${item.sku}" /></code> | Qty: <strong>${item.quantity}</strong> &times; ₹<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" />
                                                </div>
                                                <div style="font-size: 0.9rem; font-weight: 800; color: #0f172a;">
                                                    Item Total: ₹<fmt:formatNumber value="${item.lineTotal}" minFractionDigits="2" />
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- Dynamic Filter/Search Empty State -->
                    <div id="filterEmptyState" class="order-card-wrapper" style="display: none; text-align: center; padding: 3rem 2rem; border-style: dashed; border-width: 2px;">
                        <div style="font-size: 3rem; margin-bottom: 0.75rem;">🔍</div>
                        <h3 style="font-size: 1.25rem; font-weight: 800; color: #0f172a; margin-bottom: 0.35rem;">No matching orders found</h3>
                        <p style="color: var(--text-muted); font-size: 0.88rem; margin-bottom: 1.25rem;">
                            No orders match your selected status tab or search filter.
                        </p>
                        <button type="button" class="order-filter-tab active" onclick="resetOrderFilters()" style="cursor: pointer; padding: 0.5rem 1.25rem; border-radius: 8px;">
                            Reset Filters
                        </button>
                    </div>

                    <!-- Pagination Controls -->
                    <c:if test="${pagination.totalPages > 1}">
                        <div class="pagination" style="display: flex; justify-content: center; gap: 0.5rem; margin-top: 2rem;">
                            <c:forEach begin="1" end="${pagination.totalPages}" var="p">
                                <a href="${pageContext.request.contextPath}/orders?page=${p}&status=${selectedStatus}&sort=${selectedSort}" 
                                   class="page-link ${p == pagination.currentPage ? 'active' : ''}"
                                   style="padding: 0.5rem 0.95rem; border: 1px solid var(--border-color); border-radius: var(--radius-md); text-decoration: none; font-weight: 700; color: ${p == pagination.currentPage ? '#ffffff' : 'var(--text-primary)'}; background: ${p == pagination.currentPage ? '#2563eb' : '#ffffff'};">
                                   ${p}
                                </a>
                            </c:forEach>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>

            <script nonce="${cspNonce}">
                let currentFilter = '${selectedStatus}';
                let currentSort = '${selectedSort}';
                let currentSearch = '';

                function filterOrders(status, btn) {
                    currentFilter = status;
                    document.querySelectorAll('.order-filter-tab').forEach(t => t.classList.remove('active'));
                    if (btn) btn.classList.add('active');
                    applyOrderFiltersAndSort();
                }

                function handleOrderSortChange(sortVal) {
                    currentSort = sortVal;
                    applyOrderFiltersAndSort();
                }

                function handleOrderSearch(query) {
                    currentSearch = (query || '').trim().toLowerCase();
                    applyOrderFiltersAndSort();
                }

                function resetOrderFilters() {
                    currentFilter = 'ALL';
                    currentSort = 'newest';
                    currentSearch = '';
                    const searchInput = document.getElementById('orderSearchInput');
                    if (searchInput) searchInput.value = '';
                    const sortSelect = document.getElementById('orderSortSelect');
                    if (sortSelect) sortSelect.value = 'newest';
                    const allBtn = document.querySelector('.order-filter-tab');
                    if (allBtn) {
                        document.querySelectorAll('.order-filter-tab').forEach(t => t.classList.remove('active'));
                        allBtn.classList.add('active');
                    }
                    applyOrderFiltersAndSort();
                }

                function applyOrderFiltersAndSort() {
                    const container = document.getElementById('ordersCardsContainer');
                    if (!container) return;
                    
                    const cards = Array.from(container.querySelectorAll('.order-record-card'));
                    let visibleCount = 0;

                    // 1. Filter cards by status & search
                    cards.forEach(card => {
                        const cardStatus = (card.getAttribute('data-status') || '').toUpperCase();
                        const searchText = (card.getAttribute('data-search') || '').toLowerCase();
                        const cardContentText = card.textContent.toLowerCase();

                        let statusMatch = false;
                        if (currentFilter === 'ALL' || !currentFilter) {
                            statusMatch = true;
                        } else if (currentFilter === 'PROCESSING' || currentFilter === 'IN_PROGRESS') {
                            statusMatch = (cardStatus === 'PROCESSING' || cardStatus === 'PENDING' || cardStatus === 'CONFIRMED');
                        } else if (currentFilter === 'DISPATCHED') {
                            statusMatch = (cardStatus === 'DISPATCHED');
                        } else if (currentFilter === 'IN_TRANSIT' || currentFilter === 'SHIPPED') {
                            statusMatch = (cardStatus === 'SHIPPED' || cardStatus === 'IN_TRANSIT' || cardStatus === 'OUT_FOR_DELIVERY');
                        } else if (currentFilter === 'DELIVERED') {
                            statusMatch = (cardStatus === 'DELIVERED');
                        } else if (currentFilter === 'CANCELLED') {
                            statusMatch = (cardStatus === 'CANCELLED' || cardStatus === 'RETURNED' || cardStatus === 'RETURN_REQUESTED');
                        } else {
                            statusMatch = (cardStatus === currentFilter);
                        }

                        let searchMatch = true;
                        if (currentSearch) {
                            searchMatch = searchText.includes(currentSearch) || cardContentText.includes(currentSearch);
                        }

                        const visible = statusMatch && searchMatch;
                        card.style.display = visible ? 'block' : 'none';
                        if (visible) visibleCount++;
                    });

                    // 2. Sort visible cards
                    cards.sort((a, b) => {
                        const timeA = parseInt(a.getAttribute('data-timestamp') || '0', 10);
                        const timeB = parseInt(b.getAttribute('data-timestamp') || '0', 10);
                        const amountA = parseFloat(a.getAttribute('data-amount') || '0');
                        const amountB = parseFloat(b.getAttribute('data-amount') || '0');
                        const numA = a.getAttribute('data-order-number') || '';
                        const numB = b.getAttribute('data-order-number') || '';

                        if (currentSort === 'oldest') {
                            return timeA - timeB;
                        } else if (currentSort === 'price_high') {
                            return amountB - amountA;
                        } else if (currentSort === 'price_low') {
                            return amountA - amountB;
                        } else if (currentSort === 'order_num') {
                            return numB.localeCompare(numA);
                        } else {
                            // newest
                            return timeB - timeA;
                        }
                    });

                    cards.forEach(card => container.appendChild(card));

                    // 3. Toggle empty state
                    const emptyBox = document.getElementById('filterEmptyState');
                    if (emptyBox) {
                        emptyBox.style.display = (visibleCount === 0 && cards.length > 0) ? 'block' : 'none';
                    }
                }

                // Initialize on page load
                document.addEventListener('DOMContentLoaded', () => {
                    applyOrderFiltersAndSort();
                });
            </script>

        </section>
    </main>

    <!-- FOOTER -->
    <footer class="main-footer" style="background: #0f172a; color: #cbd5e1; padding: 3rem 1.5rem 1.5rem; margin-top: 4rem;">
        <div class="footer-grid" style="max-width: 1440px; margin: 0 auto; display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 2rem; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 2rem;">
            <div>
                <h4 style="color: #ffffff; margin-bottom: 1rem;">ShopKart India</h4>
                <p style="font-size: 0.85rem; line-height: 1.6; color: #94a3b8;">
                    India's leading retail & wholesale e-commerce destination with doorstep fast delivery and verified OEM manufacturing sources.
                </p>
            </div>
            <div>
                <h4 style="color: #ffffff; margin-bottom: 1rem;">Customer Care</h4>
                <ul style="list-style: none; padding: 0; font-size: 0.85rem; line-height: 2;">
                    <li><a href="${pageContext.request.contextPath}/orders" style="color: #94a3b8; text-decoration: none;">Track Orders</a></li>
                    <li><a href="${pageContext.request.contextPath}/addresses" style="color: #94a3b8; text-decoration: none;">Saved Addresses</a></li>
                    <c:if test="${sessionScope.currentUser.admin}">
                        <li><a href="${pageContext.request.contextPath}/health" style="color: #38bdf8; text-decoration: none;">🩺 System Status</a></li>
                    </c:if>
                </ul>
            </div>
            <div>
                <h4 style="color: #ffffff; margin-bottom: 1rem;">Shop Departments</h4>
                <ul style="list-style: none; padding: 0; font-size: 0.85rem; line-height: 2;">
                    <li><a href="${pageContext.request.contextPath}/products?category=1" style="color: #94a3b8; text-decoration: none;">Laptops & Ultrabooks</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=2" style="color: #94a3b8; text-decoration: none;">Smartphones & 5G</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=3" style="color: #94a3b8; text-decoration: none;">Headphones & Audio</a></li>
                </ul>
            </div>
        </div>
        <div style="text-align: center; font-size: 0.82rem; color: #94a3b8; margin-top: 1.5rem; display: flex; flex-direction: column; align-items: center; gap: 0.5rem;">
            <div>&copy; 2026 ShopKart Inc. All rights reserved. &bull; Trade Assurance &bull; 100% Purchase Protection</div>
            <div style="padding-top: 0.5rem; border-top: 1px solid rgba(255,255,255,0.08); width: 100%; max-width: 500px; color: #cbd5e1; font-size: 0.85rem;">
                Designed, Developed &amp; Managed by <strong style="color: #38bdf8; font-weight: 800; letter-spacing: 0.02em;">Rahul Pawar</strong>
            </div>
        </div>
    </footer>

    <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
</body>
</html>
