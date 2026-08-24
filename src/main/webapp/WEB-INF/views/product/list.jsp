<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/views/common/seo-head.jsp" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>

    <!-- 0. ADMIN STOREFRONT NOTIFICATION BAR -->
    <c:if test="${sessionScope.currentUser.admin}">
        <div class="admin-storefront-bar">
            <div class="admin-bar-left">
                <span class="admin-crown-badge">👑 ADMIN MODE</span>
                <span>ShopKart Control Center &bull; Logged in as <strong><c:out value="${sessionScope.currentUser.fullName}"/></strong></span>
            </div>
            <div class="admin-bar-actions">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="admin-bar-btn admin-bar-btn-primary">⚙️ Admin Console</a>
                <a href="${pageContext.request.contextPath}/admin/products" class="admin-bar-btn">📦 Catalog</a>
                <a href="${pageContext.request.contextPath}/admin/orders" class="admin-bar-btn">🛒 Orders</a>
                <a href="${pageContext.request.contextPath}/admin/reports/sales" class="admin-bar-btn">📊 Sales</a>
            </div>
        </div>
    </c:if>

    <!-- TOP ANNOUNCEMENT TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <a href="${pageContext.request.contextPath}/products?deals=true" style="color: inherit; text-decoration: none; display: inline-flex; align-items: center; gap: 0.5rem;">
                <span class="ticker-badge">Flash Sale</span>
                <span>⚡ Extra 20% Off Electronics & Fashion with Coupon <strong>SAVE20</strong> | Free Delivery on ₹499+ &bull; Explore Deals &rarr;</span>
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

            <!-- Delivery Locator -->
            <div class="delivery-locator" id="headerDeliveryTrigger" onclick="openPinCodeModal()" title="Delivery Location">
                <span class="loc-icon">📍</span>
                <div class="loc-text">
                    <span class="sub">Deliver to</span>
                    <span class="main" id="headerPincodeText">Bengaluru 560100</span>
                </div>
            </div>
        </div>

        <!-- Global Search Bar with Live Autocomplete -->
        <div class="header-search-wrapper">
            <form action="${pageContext.request.contextPath}/products" method="GET" class="header-search-form" id="headerSearchForm">
                <select name="categoryId" class="category-select" id="searchCategorySelect">
                    <option value="">All Categories</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.categoryId}" ${criteria.categoryId == cat.categoryId ? 'selected' : ''}>${cat.categoryName}</option>
                    </c:forEach>
                </select>
                <input type="text" name="keyword" class="search-input" id="globalSearchInput" placeholder="Search for products, brands and tech essentials..." value="<c:out value='${criteria.keyword}'/>" autocomplete="off">
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
                            <span class="user-nav-name"><c:out value="${sessionScope.currentUser.fullName}"/></span>
                            <span class="arrow-down">▾</span>
                        </div>
                        <div class="account-dropdown">
                            <div class="dropdown-header">
                                <div class="user-name">Hello, <c:out value="${sessionScope.currentUser.fullName}"/></div>
                                <div class="user-email"><c:out value="${sessionScope.currentUser.email}"/></div>
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

    <!-- SECONDARY SUB-NAVBAR -->
    <div class="sub-navbar">
        <a href="${pageContext.request.contextPath}/products" class="all-categories-btn">
            <span>☰</span> <strong>All Departments</strong>
        </a>
        <div class="nav-links-strip">
            <a href="${pageContext.request.contextPath}/category/1">💻 Laptops & Computers</a>
            <a href="${pageContext.request.contextPath}/category/2">📱 Smartphones & Tablets</a>
            <a href="${pageContext.request.contextPath}/category/3">🎧 Audio & Headphones</a>
            <a href="${pageContext.request.contextPath}/category/4">⌚ Smartwatches</a>
            <a href="${pageContext.request.contextPath}/category/5">👕 Men's Fashion</a>
            <a href="${pageContext.request.contextPath}/category/6">👗 Women's Fashion</a>
            <a href="${pageContext.request.contextPath}/category/8">🏠 Home & Kitchen</a>
            <a href="${pageContext.request.contextPath}/products?deals=true" class="hot-deal ${criteria.dealsOnly or not empty criteria.minDiscount ? 'active' : ''}">🔥 Flash Deals</a>
            <a href="${pageContext.request.contextPath}/products?sortBy=price&sortDirection=ASC">🏷️ Under ₹50,000</a>
        </div>
    </div>

    <!-- MAIN CATALOG LAYOUT -->
    <main class="catalog-page-layout">
        
        <!-- FILTER SIDEBAR (Flipkart / Amazon standard) -->
        <aside class="filter-sidebar">
            <div class="sidebar-header">
                <h3>Filters</h3>
                <a href="${pageContext.request.contextPath}/products" class="clear-filters-btn">Clear All</a>
            </div>

            <form action="${pageContext.request.contextPath}/products" method="GET" id="catalogFilterForm">
                <input type="hidden" name="q" value="<c:out value='${criteria.keyword}'/>">
                <input type="hidden" name="sort" value="${sortParam}">

                <!-- 1. Deals & Discounts Filter Group -->
                <div class="filter-group">
                    <div class="filter-group-title">
                        <span>⚡ Deals &amp; Discounts</span>
                        <c:if test="${criteria.dealsOnly or not empty criteria.minDiscount}">
                            <a href="javascript:void(0)" onclick="clearDealFilters()" style="font-size: 0.72rem; color: var(--primary); text-decoration: underline;">Reset</a>
                        </c:if>
                    </div>
                    <div class="filter-options-list">
                        <label class="filter-checkbox-label">
                            <input type="radio" name="discountOption" value="" ${empty criteria.minDiscount and not criteria.dealsOnly ? 'checked' : ''} onchange="handleDiscountOptionSelection('')">
                            <span>All Products</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="discountOption" value="deals_all" ${criteria.dealsOnly and empty criteria.minDiscount ? 'checked' : ''} onchange="handleDiscountOptionSelection('deals_all')">
                            <span><strong style="color: #dc2626;">🔥 All Hot Deals</strong></span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="discountOption" value="50" ${criteria.hasMinDiscount('50') ? 'checked' : ''} onchange="handleDiscountOptionSelection('50')">
                            <span>50% Off or more</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="discountOption" value="40" ${criteria.hasMinDiscount('40') ? 'checked' : ''} onchange="handleDiscountOptionSelection('40')">
                            <span>40% Off or more</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="discountOption" value="30" ${criteria.hasMinDiscount('30') ? 'checked' : ''} onchange="handleDiscountOptionSelection('30')">
                            <span>30% Off or more</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="discountOption" value="20" ${criteria.hasMinDiscount('20') ? 'checked' : ''} onchange="handleDiscountOptionSelection('20')">
                            <span>20% Off or more</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="discountOption" value="10" ${criteria.hasMinDiscount('10') ? 'checked' : ''} onchange="handleDiscountOptionSelection('10')">
                            <span>10% Off or more</span>
                        </label>
                    </div>
                    <input type="hidden" name="minDiscount" id="formMinDiscount" value="${criteria.minDiscount}">
                    <input type="hidden" name="deals" id="formDeals" value="${criteria.dealsOnly ? 'true' : ''}">
                </div>

                <!-- 2. Categories Filter -->
                <div class="filter-group">
                    <div class="filter-group-title">
                        <span>Categories</span>
                        <c:if test="${not empty criteria.categoryId}">
                            <a href="javascript:void(0)" onclick="selectCategory('')" style="font-size: 0.72rem; color: var(--primary); text-decoration: underline;">Reset</a>
                        </c:if>
                    </div>
                    <div class="filter-options-list" style="max-height: 240px; overflow-y: auto; padding-right: 4px;">
                        <label class="filter-checkbox-label">
                            <input type="radio" name="category" value="" ${empty criteria.categoryId ? 'checked' : ''} onchange="handleCategorySelection('')">
                            <span><strong>All Categories</strong></span>
                        </label>
                        <c:forEach var="cat" items="${categories}">
                            <label class="filter-checkbox-label" style="${cat.parentCategoryId != null and cat.parentCategoryId > 0 ? 'padding-left: 1.25rem; font-size: 0.82rem;' : 'font-weight: 600;'}">
                                <input type="radio" name="category" value="${cat.categoryId}" ${criteria.categoryId == cat.categoryId ? 'checked' : ''} onchange="handleCategorySelection('${cat.categoryId}')">
                                <span>${cat.parentCategoryId != null and cat.parentCategoryId > 0 ? '↳ ' : ''}${cat.categoryName}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>

                <!-- 3. Featured Brands Filter (Dynamically Shows Brands by Category) -->
                <div class="filter-group">
                    <div class="filter-group-title">
                        <span>Featured Brands</span>
                        <span id="brandsCountBadge" style="font-size: 0.72rem; color: var(--text-muted); font-weight: 600;">
                            ${not empty availableBrands ? availableBrands.size() : 0} available
                        </span>
                    </div>
                    <div class="filter-options-list" id="brandsOptionsList" style="max-height: 220px; overflow-y: auto; padding-right: 4px;">
                        <c:forEach var="b" items="${availableBrands}">
                            <label class="filter-checkbox-label brand-checkbox-row">
                                <input type="checkbox" name="brand" value="${b}" ${criteria.hasBrand(b) ? 'checked' : ''} onchange="submitCatalogForm()">
                                <span>${b}</span>
                            </label>
                        </c:forEach>
                        <c:if test="${empty availableBrands}">
                            <div style="font-size: 0.8rem; color: var(--text-muted); padding: 0.5rem 0;">
                                No specific brands found for this selection.
                            </div>
                        </c:if>
                    </div>
                </div>

                <!-- 4. Price Range Filter (Multiple Range Selection) -->
                <div class="filter-group">
                    <div class="filter-group-title">
                        <span>Price Range (₹)</span>
                        <span style="font-size: 0.72rem; color: var(--text-muted); font-weight: 600;">Multi-select</span>
                    </div>
                    <div class="filter-options-list">
                        <label class="filter-checkbox-label">
                            <input type="checkbox" name="priceRange" value="below_15k" ${criteria.hasPriceRange('below_15k') ? 'checked' : ''} onchange="submitCatalogForm()">
                            <span>Below ₹15,000</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="checkbox" name="priceRange" value="15k_30k" ${criteria.hasPriceRange('15k_30k') ? 'checked' : ''} onchange="submitCatalogForm()">
                            <span>₹15,000 - ₹30,000</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="checkbox" name="priceRange" value="30k_40k" ${criteria.hasPriceRange('30k_40k') ? 'checked' : ''} onchange="submitCatalogForm()">
                            <span>₹30,000 - ₹40,000</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="checkbox" name="priceRange" value="40k_50k" ${criteria.hasPriceRange('40k_50k') ? 'checked' : ''} onchange="submitCatalogForm()">
                            <span>₹40,000 - ₹50,000</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="checkbox" name="priceRange" value="50k_1lakh" ${criteria.hasPriceRange('50k_1lakh') ? 'checked' : ''} onchange="submitCatalogForm()">
                            <span>₹50,000 - ₹1,00,000</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="checkbox" name="priceRange" value="above_1lakh" ${criteria.hasPriceRange('above_1lakh') ? 'checked' : ''} onchange="submitCatalogForm()">
                            <span>₹1,00,000 &amp; Above</span>
                        </label>
                    </div>

                    <!-- Custom Price Row -->
                    <div style="font-size: 0.76rem; font-weight: 700; color: var(--text-muted); margin: 0.85rem 0 0.35rem; text-transform: uppercase;">
                        Custom Range:
                    </div>
                    <div class="price-inputs-row">
                        <input type="number" name="minPrice" placeholder="Min" value="${criteria.minPrice}">
                        <span>-</span>
                        <input type="number" name="maxPrice" placeholder="Max" value="${criteria.maxPrice}">
                        <button type="submit" class="price-apply-btn">Go</button>
                    </div>
                </div>

                <!-- Customer Rating Filter -->
                <div class="filter-group">
                    <div class="filter-group-title">
                        <span>Customer Ratings</span>
                    </div>
                    <div class="filter-options-list">
                        <label class="filter-checkbox-label">
                            <input type="checkbox" checked disabled>
                            <span>★★★★☆ 4★ &amp; above</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="checkbox" checked disabled>
                            <span>★★★☆☆ 3★ &amp; above</span>
                        </label>
                    </div>
                </div>

                <!-- Availability -->
                <div class="filter-group">
                    <div class="filter-group-title">
                        <span>Availability</span>
                    </div>
                    <div class="filter-options-list">
                        <label class="filter-checkbox-label">
                            <input type="checkbox" checked disabled>
                            <span>Include In Stock Only</span>
                        </label>
                    </div>
                </div>

                <div style="margin-top: 1.25rem;">
                    <button type="submit" class="btn-primary-action" style="width: 100%; padding: 0.75rem; border-radius: 8px; font-weight: 800; cursor: pointer; border: none; background: #febd69; color: #0f172a; box-shadow: var(--shadow-sm); transition: var(--transition-fast);">
                        Apply Filters
                    </button>
                </div>
            </form>
        </aside>

        <!-- CATALOG MAIN CONTENT -->
        <div class="catalog-content">
            
            <!-- Active Deals Promotional Banner -->
            <c:if test="${criteria.dealsOnly or not empty criteria.minDiscount}">
                <div style="background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%); border-radius: 12px; padding: 1.25rem 1.5rem; margin-bottom: 1.5rem; color: #ffffff; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 15px -3px rgba(15, 23, 42, 0.25); border: 1px solid rgba(255,255,255,0.1); flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <div style="font-size: 0.75rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; color: #fbbf24; display: flex; align-items: center; gap: 0.4rem; margin-bottom: 0.25rem;">
                            <span>⚡ SPECIAL PROMOTION</span>
                            <span style="background: #dc2626; color: #fff; padding: 0.15rem 0.45rem; border-radius: 4px; font-size: 0.7rem;">LIMITED TIME</span>
                        </div>
                        <h3 style="font-size: 1.35rem; font-weight: 900; margin: 0 0 0.25rem 0; color: #ffffff; letter-spacing: -0.01em;">🔥 Active Deals &amp; Flash Discounts</h3>
                        <p style="font-size: 0.88rem; color: #cbd5e1; margin: 0;">
                            Showing items with special price drops ${not empty criteria.minDiscount ? ('(' += criteria.minDiscount += '% OFF or higher)') : '(Up to 50% OFF)'}
                        </p>
                    </div>
                    <a href="${pageContext.request.contextPath}/products" style="background: #ffffff; color: #0f172a; padding: 0.55rem 1.15rem; border-radius: 8px; font-weight: 800; font-size: 0.85rem; text-decoration: none; box-shadow: 0 2px 4px rgba(0,0,0,0.1); transition: var(--transition-fast);">
                        View All Products &rarr;
                    </a>
                </div>
            </c:if>

            <!-- Toolbar -->
            <div class="catalog-toolbar">
                <div class="results-count">
                    Showing <strong>${pagination.totalItems}</strong> results
                    <c:if test="${not empty criteria.keyword}">
                        for "<strong><c:out value='${criteria.keyword}'/></strong>"
                    </c:if>
                    <c:if test="${not empty criteria.categoryId}">
                        <c:forEach var="c" items="${categories}">
                            <c:if test="${c.categoryId == criteria.categoryId}">
                                in <strong>${c.categoryName}</strong>
                            </c:if>
                        </c:forEach>
                    </c:if>
                    <c:if test="${criteria.dealsOnly or not empty criteria.minDiscount}">
                        <span style="display: inline-flex; align-items: center; gap: 0.3rem; margin-left: 0.5rem; background: #fee2e2; color: #dc2626; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.78rem; font-weight: 800;">
                            🔥 Deals Applied
                        </span>
                    </c:if>
                </div>

                <div class="toolbar-controls">
                    <form action="${pageContext.request.contextPath}/products" method="GET" style="display: inline-flex; align-items: center; gap: 0.5rem;" id="toolbarSortForm">
                        <input type="hidden" name="q" value="<c:out value='${criteria.keyword}'/>">
                        <input type="hidden" name="category" value="${criteria.categoryId}">
                        <input type="hidden" name="minDiscount" value="${criteria.minDiscount}">
                        <input type="hidden" name="deals" value="${criteria.dealsOnly ? 'true' : ''}">
                        <c:forEach var="b" items="${criteria.brands}">
                            <input type="hidden" name="brand" value="${b}">
                        </c:forEach>
                        <c:forEach var="pr" items="${criteria.priceRanges}">
                            <input type="hidden" name="priceRange" value="${pr}">
                        </c:forEach>
                        <input type="hidden" name="minPrice" value="${criteria.minPrice}">
                        <input type="hidden" name="maxPrice" value="${criteria.maxPrice}">
                        
                        <label style="font-size: 0.85rem; color: var(--text-secondary); font-weight: 600;">Sort By:</label>
                        <select name="sort" class="sort-select" onchange="this.form.submit()">
                            <option value="created_at" ${sortParam == 'created_at' or sortParam == 'newest' ? 'selected' : ''}>Featured / Newest</option>
                            <option value="discount_desc" ${sortParam == 'discount_desc' or sortParam == 'discount' or sortParam == 'deals' ? 'selected' : ''}>🔥 Highest Discount (Flash Deals)</option>
                            <option value="price_asc" ${sortParam == 'price_asc' or sortParam == 'price_low' ? 'selected' : ''}>Price: Low to High</option>
                            <option value="price_desc" ${sortParam == 'price_desc' or sortParam == 'price_high' ? 'selected' : ''}>Price: High to Low</option>
                            <option value="name_asc" ${sortParam == 'name_asc' or sortParam == 'product_name' ? 'selected' : ''}>Product Name</option>
                        </select>
                    </form>

                    <div class="view-toggle-group">
                        <button type="button" class="view-toggle-btn active" id="gridBtn" title="Grid View">⊞</button>
                        <button type="button" class="view-toggle-btn" id="listBtn" title="List View">☰</button>
                    </div>
                </div>
            </div>

            <!-- Products Grid -->
            <div class="catalog-products-grid" id="catalogProductsGrid">
                <c:forEach var="product" items="${pagination.items}">
                    <div class="product-card">
                        <button class="card-wishlist-btn" onclick="quickAddToWishlist(${product.productId}, event)" title="Save to Wishlist">❤️</button>
                        <c:if test="${product.discountPercentage > 0}">
                            <span class="card-discount-badge"><fmt:formatNumber value="${product.discountPercentage}" maxFractionDigits="0"/>% OFF</span>
                        </c:if>

                        <a href="${pageContext.request.contextPath}/product/${not empty product.slug ? product.slug : product.productId}">
                            <div class="product-img-container">
                                <img src="${not empty product.primaryImageUrl ? product.primaryImageUrl : 'https://placehold.co/300x300?text=ShopKart'}" alt="<c:out value='${product.brand}'/> <c:out value='${product.productName}'/> - ShopKart" loading="lazy">
                            </div>
                            <div class="product-brand"><c:out value="${product.brand}"/></div>
                            <div class="product-title"><c:out value="${product.productName}"/></div>
                        </a>

                        <div class="rating-row">
                            <c:choose>
                                <c:when test="${product.reviewCount > 0}">
                                    <span class="rating-badge">★ <fmt:formatNumber value="${product.averageRating}" minFractionDigits="1" maxFractionDigits="1"/></span>
                                    <span class="rating-count">(${product.reviewCount})</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="rating-badge" style="background: #e2e8f0; color: #475569;">★ New</span>
                                    <span class="rating-count" style="color: #94a3b8; font-size: 0.75rem;">(0 reviews)</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="price-row">
                            <span class="current-price">₹<fmt:formatNumber value="${product.effectivePrice}" pattern="#,##0"/></span>
                            <c:if test="${product.discountPercentage > 0}">
                                <span class="mrp-price">₹<fmt:formatNumber value="${product.price}" pattern="#,##0"/></span>
                            </c:if>
                        </div>

                        <div class="delivery-tag">FREE Delivery by <strong>Tomorrow</strong></div>

                        <div class="card-action-group">
                            <c:choose>
                                <c:when test="${sessionScope.currentUser.admin}">
                                    <a href="${pageContext.request.contextPath}/admin/products/edit?id=${product.productId}" 
                                       style="display: flex; align-items: center; justify-content: center; gap: 0.35rem; width: 100%; padding: 0.5rem; font-size: 0.8rem; font-weight: 700; background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; border-radius: 8px; text-decoration: none;">
                                        ✏️ Edit in Admin Console
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <button class="card-add-cart-btn" onclick="quickAddToCart(${product.productId}, 1, event)" title="Add to Cart">
                                        🛒 Add Cart
                                    </button>
                                    <button class="card-buy-now-btn" onclick="quickBuyNow(${product.productId}, 1, event)" title="Buy Now">
                                        ⚡ Buy Now
                                    </button>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <!-- Pagination Bar if multiple pages -->
            <c:if test="${pagination.totalPages > 1}">
                <div style="display: flex; justify-content: center; gap: 0.5rem; margin-top: 2rem; align-items: center;">
                    <c:if test="${pagination.currentPage > 1}">
                        <a href="${pageContext.request.contextPath}/products?page=${pagination.currentPage - 1}&category=${criteria.categoryId}&q=<c:out value='${criteria.keyword}'/>&sort=${sortParam}&minPrice=${criteria.minPrice}&maxPrice=${criteria.maxPrice}&minDiscount=${criteria.minDiscount}&deals=${criteria.dealsOnly ? 'true' : ''}" 
                           class="order-btn-secondary" style="padding: 0.5rem 1rem;">&laquo; Prev</a>
                    </c:if>
                    
                    <span style="font-size: 0.9rem; font-weight: 700; color: var(--text-secondary); margin: 0 0.5rem;">
                        Page ${pagination.currentPage} of ${pagination.totalPages}
                    </span>

                    <c:if test="${pagination.currentPage < pagination.totalPages}">
                        <a href="${pageContext.request.contextPath}/products?page=${pagination.currentPage + 1}&category=${criteria.categoryId}&q=<c:out value='${criteria.keyword}'/>&sort=${sortParam}&minPrice=${criteria.minPrice}&maxPrice=${criteria.maxPrice}&minDiscount=${criteria.minDiscount}&deals=${criteria.dealsOnly ? 'true' : ''}" 
                           class="order-btn-secondary" style="padding: 0.5rem 1rem;">Next &raquo;</a>
                    </c:if>
                </div>
            </c:if>

            <c:if test="${empty pagination.items}">
                <div style="background: #ffffff; padding: 4rem 2rem; text-align: center; border-radius: var(--radius-md); border: 1px solid var(--border-color);">
                    <div style="font-size: 3rem; margin-bottom: 1rem;">🔍</div>
                    <h2 style="font-size: 1.5rem; margin-bottom: 0.5rem;">No matching products found</h2>
                    <p style="color: var(--text-muted); margin-bottom: 1.5rem;">Try adjusting your price range, selecting different brand filters, or clearing your deals filter.</p>
                    <a href="${pageContext.request.contextPath}/products" class="hero-cta-btn">View All Products</a>
                </div>
            </c:if>
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

    <script nonce="${cspNonce}">
        <!-- categoryBrandsMap is populated with server-sanitized JSON -->
        const categoryBrandsMap = ${categoryBrandsJson != null ? categoryBrandsJson : '{}'};
        
        function handleCategorySelection(catId) {
            // Update brand options in the sidebar live
            updateBrandsListUI(catId);
            // Submit form to reload products matching category
            document.getElementById('catalogFilterForm').submit();
        }

        function selectCategory(catId) {
            const form = document.getElementById('catalogFilterForm');
            const catInputs = form.querySelectorAll('input[name="category"]');
            catInputs.forEach(input => {
                input.checked = (input.value === catId);
            });
            handleCategorySelection(catId);
        }

        function handleDiscountOptionSelection(val) {
            const minDiscInput = document.getElementById('formMinDiscount');
            const dealsInput = document.getElementById('formDeals');
            if (val === 'deals_all') {
                minDiscInput.value = '';
                dealsInput.value = 'true';
            } else if (val && val !== '') {
                minDiscInput.value = val;
                dealsInput.value = 'true';
            } else {
                minDiscInput.value = '';
                dealsInput.value = '';
            }
            submitCatalogForm();
        }

        function clearDealFilters() {
            document.getElementById('formMinDiscount').value = '';
            document.getElementById('formDeals').value = '';
            const discInputs = document.querySelectorAll('input[name="discountOption"]');
            discInputs.forEach(inp => {
                inp.checked = (inp.value === '');
            });
            submitCatalogForm();
        }

        function updateBrandsListUI(catId) {
            const brandsContainer = document.getElementById('brandsOptionsList');
            const countBadge = document.getElementById('brandsCountBadge');
            if (!brandsContainer) return;

            const key = catId && catId !== '' ? String(catId) : 'all';
            const brands = categoryBrandsMap[key] || categoryBrandsMap['all'] || [];

            if (countBadge) {
                countBadge.textContent = brands.length + ' available';
            }

            if (brands.length === 0) {
                brandsContainer.innerHTML = '<div style="font-size: 0.8rem; color: var(--text-muted); padding: 0.5rem 0;">No specific brands for this selection.</div>';
                return;
            }

            let html = '';
            brands.forEach(b => {
                html += '<label class="filter-checkbox-label brand-checkbox-row">' +
                        '<input type="checkbox" name="brand" value="' + b + '" onchange="submitCatalogForm()">' +
                        '<span>' + b + '</span>' +
                        '</label>';
            });
            brandsContainer.innerHTML = html;
        }

        function submitCatalogForm() {
            document.getElementById('catalogFilterForm').submit();
        }
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
</body>
</html>
