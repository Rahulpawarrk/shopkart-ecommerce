<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <c:if test="${empty featuredProducts and empty catalogError}">
                <c:redirect
                    url="/home${not empty pageContext.request.queryString ? '?'.concat(pageContext.request.queryString) : ''}" />
            </c:if>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <!-- Favicon -->
                <link rel="icon" type="image/svg+xml"
                    href="${pageContext.request.contextPath}/assets/images/favicon.svg">
                <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.svg">
                <link rel="apple-touch-icon" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="contextPath" content="${pageContext.request.contextPath}">
                <title>ShopKart | India's Premier Online Shopping Destination</title>
                <meta name="description"
                    content="ShopKart India - Discover top deals on smartphones, laptops, electronics, audio, and fashion. Free express delivery, verified reviews, and secure checkout.">
                <meta name="keywords"
                    content="ShopKart, online shopping India, electronics, smartphones, laptops, headphones, deals, fashion, discounts">
                <meta name="robots" content="index, follow">
                <meta name="theme-color" content="#2563eb">

                <!-- OpenGraph Social Sharing -->
                <meta property="og:title" content="ShopKart | India's Premier Online Shopping Destination">
                <meta property="og:description"
                    content="Discover top deals on smartphones, laptops, audio & fashion with instant express delivery.">
                <meta property="og:type" content="website">
                <meta property="og:url" content="https://shopkart-ecommerce-1m2n.onrender.com/">
                <meta property="og:image" content="${pageContext.request.contextPath}/assets/images/logo.svg">

                <!-- Schema.org JSON-LD -->
                <script type="application/ld+json">
    {
      "@context": "https://schema.org",
      "@type": "WebSite",
      "name": "ShopKart",
      "url": "https://shopkart-ecommerce-1m2n.onrender.com/",
      "potentialAction": {
        "@type": "SearchAction",
        "target": "https://shopkart-ecommerce-1m2n.onrender.com/products?q={search_term_string}",
        "query-input": "required name=search_term_string"
      }
    }
    </script>

                <link rel="preload" as="image"
                    href="https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=75"
                    fetchpriority="high">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=5.2">
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link
                    href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap"
                    rel="stylesheet">
            </head>

            <body>
                <!-- 0. SINGLE-LINE REGISTRATION SUCCESS & WELCOME STRIP -->
                <c:if test="${param.registered == 'true' || param.welcome == 'true'}">
                    <div id="registrationSuccessBanner"
                        style="background: linear-gradient(90deg, #059669 0%, #10b981 100%); color: #ffffff; padding: 0.55rem 1rem; font-size: 0.88rem; font-weight: 700; display: flex; align-items: center; justify-content: center; position: relative; z-index: 1200; box-shadow: 0 2px 8px rgba(0,0,0,0.12);">
                        <div style="display: flex; align-items: center; gap: 0.5rem; text-align: center;">
                            <span>🎉</span>
                            <span><strong>Registration Successful!</strong> Welcome to ShopKart<c:if
                                    test="${not empty sessionScope.currentUser.firstName}">,
                                    ${sessionScope.currentUser.firstName}</c:if>! Your account is verified. Enjoy your
                                shopping!</span>
                        </div>
                        <button type="button" id="registrationSuccessBannerClose"
                            class="registration-success-banner-close" title="Dismiss"
                            aria-label="Dismiss registration success message">
                            ✕
                        </button>
                    </div>
                </c:if>

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
                            <a href="${pageContext.request.contextPath}/admin/products" class="admin-bar-btn">📦
                                Catalog</a>
                            <a href="${pageContext.request.contextPath}/admin/orders" class="admin-bar-btn">🛒
                                Orders</a>
                            <a href="${pageContext.request.contextPath}/admin/reports/sales" class="admin-bar-btn">📊
                                Sales</a>
                        </div>
                    </div>
                </c:if>

                <!-- 1. TOP ANNOUNCEMENT TICKER -->
                <header class="top-ticker">
                    <div class="ticker-text">
                        <a href="${pageContext.request.contextPath}/products?deals=true"
                            style="color: inherit; text-decoration: none; display: inline-flex; align-items: center; gap: 0.5rem;">
                            <span class="ticker-badge">⚡ ShopKart Mega Sale</span>
                            <span>Grand Festive Carnival: Up to 50% Off Top Brands + Extra 20% Off with Code
                                <strong>SAVE20</strong> &bull; Explore Deals &rarr;</span>
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
                <nav class="main-header" aria-label="Main Navigation">
                    <div class="brand-group">
                        <a href="${pageContext.request.contextPath}/" class="brand-logo"
                            title="ShopKart - Premier Online Shopping">
                            <img src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="ShopKart"
                                class="logo-img">
                        </a>
                        <div class="delivery-locator" id="headerDeliveryTrigger" onclick="openPinCodeModal()"
                            title="Change delivery location" role="button" tabindex="0"
                            aria-label="Delivery Location Bengaluru 560100">
                            <span class="loc-icon" aria-hidden="true">📍</span>
                            <div class="loc-text">
                                <span class="sub">Deliver to</span>
                                <span class="main" id="headerPincodeText">Bengaluru 560100</span>
                            </div>
                        </div>
                    </div>

                    <!-- Global Search Bar with Live Autocomplete -->
                    <div class="header-search-wrapper">
                        <form action="${pageContext.request.contextPath}/products" method="GET"
                            class="header-search-form" id="headerSearchForm" role="search">
                            <select name="categoryId" class="category-select" id="searchCategorySelect"
                                aria-label="Product Category">
                                <option value="">All Categories</option>
                                <c:forEach var="cat" items="${categoryTree}">
                                    <option value="${cat.categoryId}">${cat.categoryName}</option>
                                </c:forEach>
                            </select>
                            <input type="text" name="keyword" class="search-input" id="globalSearchInput"
                                placeholder="Search for products, brands and tech essentials..." autocomplete="off"
                                aria-label="Search products, brands and essentials">
                            <button type="submit" class="search-button" aria-label="Search">🔍</button>
                        </form>

                        <!-- Autocomplete Dropdown Preview -->
                        <div class="search-autocomplete-dropdown" id="searchAutocompleteDropdown"></div>
                    </div>

                    <!-- Header Actions -->
                    <div class="header-actions">
                        <!-- User Account Menu with Hover Dropdown -->
                        <div class="user-account-menu" id="userAccountMenu">
                            <c:choose>
                                <c:when test="${not empty sessionScope.currentUser}">
                                    <div class="user-nav-btn" tabindex="0" role="button" aria-label="User Account Menu">
                                        <span class="user-avatar-icon" aria-hidden="true">👤</span>
                                        <span class="user-nav-name">${sessionScope.currentUser.fullName}</span>
                                        <span class="arrow-down" aria-hidden="true">▾</span>
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
                                        title="Sign In to ShopKart" aria-label="Sign In">
                                        <span class="user-avatar-icon" aria-hidden="true">👤</span>
                                        <span style="font-weight:600;">Sign In</span>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <a href="${pageContext.request.contextPath}/wishlist" class="header-action-inline"
                            title="Wishlist" aria-label="View Wishlist">
                            <span class="badge-icon" aria-hidden="true">❤️</span>
                            <span>Wishlist</span>
                        </a>

                        <a href="${pageContext.request.contextPath}/cart" class="header-action-inline" title="Cart"
                            aria-label="View Cart">
                            <div class="cart-icon-wrapper">
                                <span class="badge-icon" aria-hidden="true">🛒</span>
                                <span class="badge-count" id="headerCartBadge">${not empty sessionScope.cart ?
                                    sessionScope.cart.totalQuantity : 0}</span>
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
                        <a href="${pageContext.request.contextPath}/products?category=1">💻 Laptops & Computers</a>
                        <a href="${pageContext.request.contextPath}/products?category=2">📱 Smartphones & Tablets</a>
                        <a href="${pageContext.request.contextPath}/products?category=3">🎧 Audio & Headphones</a>
                        <a href="${pageContext.request.contextPath}/products?category=4">⌚ Smartwatches</a>
                        <a href="${pageContext.request.contextPath}/products?category=5">👕 Men's Fashion</a>
                        <a href="${pageContext.request.contextPath}/products?category=6">👗 Women's Fashion</a>
                        <a href="${pageContext.request.contextPath}/products?category=8">🏠 Home & Kitchen</a>
                        <a href="${pageContext.request.contextPath}/products?deals=true" class="hot-deal">🔥 Flash
                            Deals</a>
                        <a href="${pageContext.request.contextPath}/products?sortBy=price&sortDirection=ASC">🏷️ Under
                            ₹50,000</a>
                    </div>
                </div>

                <!-- 4. AUTO-SLIDING HERO CAROUSEL -->
                <section class="hero-carousel-container" aria-label="Featured Promotions">
                    <div class="hero-slider-track" id="heroSliderTrack">
                        <!-- Slide 1: Laptops -->
                        <div class="hero-slide"
                            style="background-image: url('https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=75');">
                            <div class="hero-slide-overlay"></div>
                            <div class="hero-slide-content">
                                <span class="hero-tag">⚡ Next-Gen Performance</span>
                                <h1 class="hero-title">Apple M3 Max & OLED Workstations</h1>
                                <p class="hero-description">Supercharge your workflow with unprecedented processing
                                    power, liquid retina XDR displays, and up to 22 hours of battery life.</p>
                                <a href="${pageContext.request.contextPath}/products?category=1"
                                    class="hero-cta-btn">Shop Pro Laptops →</a>
                            </div>
                        </div>
                        <!-- Slide 2: Audio -->
                        <div class="hero-slide"
                            style="background-image: url('https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=75');">
                            <div class="hero-slide-overlay"></div>
                            <div class="hero-slide-content">
                                <span class="hero-tag">🎧 Studio Soundfest</span>
                                <h2 class="hero-title">Industry Leading Noise Cancellation</h2>
                                <p class="hero-description">Immerse yourself in lossless spatial audio with Sony
                                    WH-1000XM5 and Apple AirPods Pro. Starting at ₹19,900.</p>
                                <a href="${pageContext.request.contextPath}/products?category=3"
                                    class="hero-cta-btn">Explore Audio Gear →</a>
                            </div>
                        </div>
                        <!-- Slide 3: Fashion -->
                        <div class="hero-slide"
                            style="background-image: url('https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1200&q=75');">
                            <div class="hero-slide-overlay"></div>
                            <div class="hero-slide-content">
                                <span class="hero-tag">✨ Premium Couture</span>
                                <h2 class="hero-title">Designer Autumn & Winter Apparel</h2>
                                <p class="hero-description">Discover the latest arrivals from Ralph Lauren, AllSaints,
                                    and Zimmermann with up to 40% limited-time markdown.</p>
                                <a href="${pageContext.request.contextPath}/products?category=5"
                                    class="hero-cta-btn">Discover Collection →</a>
                            </div>
                        </div>
                    </div>

                    <!-- Controls -->
                    <button class="carousel-nav-btn prev" id="carouselPrevBtn" aria-label="Previous Slide">❮</button>
                    <button class="carousel-nav-btn next" id="carouselNextBtn" aria-label="Next Slide">❯</button>

                    <div class="carousel-indicators">
                        <span class="indicator-dot active" aria-label="Slide 1"></span>
                        <span class="indicator-dot" aria-label="Slide 2"></span>
                        <span class="indicator-dot" aria-label="Slide 3"></span>
                    </div>
                </section>

                <!-- 5. TRUST & VALUE STRIP -->
                <div class="trust-badges-strip">
                    <div class="trust-badge-item">
                        <div class="badge-icon-box">🚚</div>
                        <div class="badge-details">
                            <h4>Free Express Shipping</h4>
                            <p>On all prepaid orders over ₹499</p>
                        </div>
                    </div>
                    <div class="trust-badge-item">
                        <div class="badge-icon-box">💵</div>
                        <div class="badge-details">
                            <h4>Cash on Delivery (COD)</h4>
                            <p>Pay upon delivery across 25,000+ PINs</p>
                        </div>
                    </div>
                    <div class="trust-badge-item">
                        <div class="badge-icon-box">🔄</div>
                        <div class="badge-details">
                            <h4>7-Day Easy Returns</h4>
                            <p>Instant doorstep pickup & refund</p>
                        </div>
                    </div>
                    <div class="trust-badge-item">
                        <div class="badge-icon-box">🛡️</div>
                        <div class="badge-details">
                            <h4>100% Genuine Products</h4>
                            <p>Authorized brand warranty included</p>
                        </div>
                    </div>
                </div>

                <!-- 6. FLIPKART/MEESHO DEAL OF THE DAY (WITH LIVE COUNTDOWN TIMER) -->
                <section class="deal-of-day-section">
                    <div class="deal-header">
                        <div class="deal-header-left">
                            <h2 class="deal-title">🔥 Deal of the Day</h2>
                            <div class="countdown-box">
                                <span>Ends in:</span>
                                <div class="countdown-digits">
                                    <span class="countdown-unit" id="dealHours">04</span> :
                                    <span class="countdown-unit" id="dealMins">42</span> :
                                    <span class="countdown-unit" id="dealSecs">35</span>
                                </div>
                            </div>
                        </div>
                        <div class="deal-header-right">
                            <a href="${pageContext.request.contextPath}/products?deals=true">View All Deals →</a>
                        </div>
                    </div>

                    <div class="product-slider-wrapper">
                        <button type="button" class="slider-arrow-btn prev"
                            onclick="scrollProductSlider('dealSliderTrack', -1)" aria-label="Previous Deals">❮</button>
                        <div class="product-slider-track" id="dealSliderTrack">
                            <c:forEach var="deal" items="${hotDeals}">
                                <div class="product-card">
                                    <button class="card-wishlist-btn"
                                        onclick="quickAddToWishlist('${deal.productId}', event)"
                                        title="Save to Wishlist">❤️</button>
                                    <c:if test="${deal.discountPercentage > 0}">
                                        <span class="card-discount-badge">
                                            <fmt:formatNumber value="${deal.discountPercentage}"
                                                maxFractionDigits="0" />% OFF
                                        </span>
                                    </c:if>

                                    <a href="${pageContext.request.contextPath}/product?id=${deal.productId}">
                                        <div class="product-img-container">
                                            <img src="${not empty deal.primaryImageUrl ? deal.primaryImageUrl : 'https://placehold.co/300x300?text=ShopKart'}"
                                                alt="${deal.productName}" loading="lazy">
                                        </div>
                                        <div class="product-brand">${deal.brand}</div>
                                        <div class="product-title">${deal.productName}</div>
                                    </a>

                                    <div class="rating-row">
                                        <span class="rating-badge">★ 4.8</span>
                                        <span class="rating-count">(128)</span>
                                    </div>

                                    <div class="price-row">
                                        <span class="current-price">₹
                                            <fmt:formatNumber value="${deal.effectivePrice}" pattern="#,##0" />
                                        </span>
                                        <c:if test="${deal.discountPercentage > 0}">
                                            <span class="mrp-price">₹
                                                <fmt:formatNumber value="${deal.price}" pattern="#,##0" />
                                            </span>
                                        </c:if>
                                    </div>

                                    <div class="delivery-tag">FREE Delivery by <strong>Tomorrow</strong></div>

                                    <div class="card-action-group">
                                        <c:choose>
                                            <c:when test="${sessionScope.currentUser.admin}">
                                                <a href="${pageContext.request.contextPath}/admin/products/edit?id=${deal.productId}"
                                                    style="display: flex; align-items: center; justify-content: center; gap: 0.35rem; width: 100%; padding: 0.5rem; font-size: 0.8rem; font-weight: 700; background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; border-radius: 8px; text-decoration: none;">
                                                    ✏️ Edit in Admin Console
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <button class="card-add-cart-btn"
                                                    onclick="quickAddToCart('${deal.productId}', 1, event)"
                                                    title="Add to Cart">
                                                    🛒 Add Cart
                                                </button>
                                                <button class="card-buy-now-btn"
                                                    onclick="quickBuyNow('${deal.productId}', 1, event)"
                                                    title="Buy Now">
                                                    ⚡ Buy
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <button type="button" class="slider-arrow-btn next"
                            onclick="scrollProductSlider('dealSliderTrack', 1)" aria-label="Next Deals">❯</button>
                    </div>
                </section>

                <!-- 7. AMAZON 4-QUAD DEPARTMENT GRID SHOWCASES -->
                <section class="quad-showcase-container">
                    <!-- Box 1: Workstations & Laptops -->
                    <div class="quad-box">
                        <h3 class="quad-title">Upgrade Your Workspace</h3>
                        <div class="quad-items-grid">
                            <c:forEach var="p" items="${techQuad}">
                                <a href="${pageContext.request.contextPath}/product?id=${p.productId}"
                                    class="quad-mini-item">
                                    <div class="img-wrapper">
                                        <img src="${p.primaryImageUrl}" alt="${p.productName}" loading="lazy">
                                    </div>
                                    <div class="mini-caption">${p.productName}</div>
                                    <div class="mini-price">₹
                                        <fmt:formatNumber value="${p.effectivePrice}" minFractionDigits="0"
                                            maxFractionDigits="0" />
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                        <a href="${pageContext.request.contextPath}/products?category=1" class="quad-footer-link">See
                            all laptops & workstations →</a>
                    </div>

                    <!-- Box 2: Audio & Wearables -->
                    <div class="quad-box">
                        <h3 class="quad-title">Audio & Smart Wearables</h3>
                        <div class="quad-items-grid">
                            <c:forEach var="p" items="${audioQuad}">
                                <a href="${pageContext.request.contextPath}/product?id=${p.productId}"
                                    class="quad-mini-item">
                                    <div class="img-wrapper">
                                        <img src="${p.primaryImageUrl}" alt="${p.productName}" loading="lazy">
                                    </div>
                                    <div class="mini-caption">${p.productName}</div>
                                    <div class="mini-price">₹
                                        <fmt:formatNumber value="${p.effectivePrice}" minFractionDigits="0"
                                            maxFractionDigits="0" />
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                        <a href="${pageContext.request.contextPath}/products?category=3"
                            class="quad-footer-link">Explore headphones & smartwatches →</a>
                    </div>

                    <!-- Box 3: Fashion & Apparel -->
                    <div class="quad-box">
                        <h3 class="quad-title">Trending Styles & Fashion</h3>
                        <div class="quad-items-grid">
                            <c:forEach var="p" items="${fashionQuad}">
                                <a href="${pageContext.request.contextPath}/product?id=${p.productId}"
                                    class="quad-mini-item">
                                    <div class="img-wrapper">
                                        <img src="${p.primaryImageUrl}" alt="${p.productName}" loading="lazy">
                                    </div>
                                    <div class="mini-caption">${p.productName}</div>
                                    <div class="mini-price">₹
                                        <fmt:formatNumber value="${p.effectivePrice}" minFractionDigits="0"
                                            maxFractionDigits="0" />
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                        <a href="${pageContext.request.contextPath}/products?category=5" class="quad-footer-link">Browse
                            apparel & footwear →</a>
                    </div>

                    <!-- Box 4: Home & Kitchen -->
                    <div class="quad-box">
                        <h3 class="quad-title">Home & Kitchen Essentials</h3>
                        <div class="quad-items-grid">
                            <c:forEach var="p" items="${lifestyleQuad}">
                                <a href="${pageContext.request.contextPath}/product?id=${p.productId}"
                                    class="quad-mini-item">
                                    <div class="img-wrapper">
                                        <img src="${p.primaryImageUrl}" alt="${p.productName}" loading="lazy">
                                    </div>
                                    <div class="mini-caption">${p.productName}</div>
                                    <div class="mini-price">₹
                                        <fmt:formatNumber value="${p.effectivePrice}" minFractionDigits="0"
                                            maxFractionDigits="0" />
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                        <a href="${pageContext.request.contextPath}/products?category=8" class="quad-footer-link">Shop
                            home appliances →</a>
                    </div>
                </section>

                <!-- 8. ALIBABA GLOBAL TRADE ASSURANCE STRIP -->
                <section class="alibaba-trade-bar">
                    <div class="alibaba-trade-grid">
                        <div class="alibaba-trade-item">
                            <div class="alibaba-trade-icon">🏭</div>
                            <div class="alibaba-trade-text">
                                <h4>Factory Direct Pricing</h4>
                                <p>Save up to 45% with verified OEM sourcing</p>
                            </div>
                        </div>
                        <div class="alibaba-trade-item">
                            <div class="alibaba-trade-icon">🛡️</div>
                            <div class="alibaba-trade-text">
                                <h4>ShopKart Trade Assurance</h4>
                                <p>100% Payment escrow & doorstep protection</p>
                            </div>
                        </div>
                        <div class="alibaba-trade-item">
                            <div class="alibaba-trade-icon">📦</div>
                            <div class="alibaba-trade-text">
                                <h4>Wholesale & Bulk Tiers</h4>
                                <p>Tiered volume discounts with MOQ from 1 unit</p>
                            </div>
                        </div>
                        <div class="alibaba-trade-item">
                            <div class="alibaba-trade-icon">⚡</div>
                            <div class="alibaba-trade-text">
                                <h4>Fast Express Logistics</h4>
                                <p>Doorstep dispatch across 28,000+ PIN codes</p>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- 9. ALIBABA SOURCE BY FACTORY & VOLUME DISCOUNTS SHOWCASE -->
                <section class="alibaba-factory-section">
                    <div class="alibaba-section-heading">
                        <div>
                            <h2>🌐 Global Sourcing & Factory Direct Hub</h2>
                            <p style="color: var(--text-muted); font-size: 0.9rem; margin-top: 0.25rem;">
                                Source top-tier electronics, mobile hardware & studio audio direct from certified
                                suppliers.
                            </p>
                        </div>
                        <a href="${pageContext.request.contextPath}/products"
                            style="color: var(--primary); font-weight: 700; font-size: 0.9rem; text-decoration: none;">
                            View All Factory Sources →
                        </a>
                    </div>

                    <div class="alibaba-factory-grid">
                        <!-- Factory Card 1 -->
                        <div class="alibaba-factory-card">
                            <span class="alibaba-factory-badge">OEM VERIFIED</span>
                            <img src="https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=600&q=80"
                                alt="Ultrabooks Factory" class="alibaba-factory-thumb">
                            <h3
                                style="font-size: 1.05rem; font-weight: 800; color: var(--text-primary); margin-bottom: 0.35rem;">
                                High-Performance Laptops</h3>
                            <p style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.5rem;">Direct from
                                Shenzhen OEM Assembly Lines</p>
                            <div class="alibaba-tier-pricing">
                                <span>MOQ: <strong>1 Unit</strong></span>
                                <span>Tier Price: <strong>₹48,999</strong></span>
                            </div>
                            <div
                                style="font-size: 0.75rem; color: var(--success); font-weight: 700; margin-bottom: 0.75rem;">
                                ⚡ Buy 3+ get extra 12% bulk discount
                            </div>
                            <a href="${pageContext.request.contextPath}/products?category=1" class="order-btn-primary"
                                style="margin-top: auto;">Source Computers</a>
                        </div>

                        <!-- Factory Card 2 -->
                        <div class="alibaba-factory-card">
                            <span class="alibaba-factory-badge">FACTORY DIRECT</span>
                            <img src="https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=600&q=80"
                                alt="Smartphones Factory" class="alibaba-factory-thumb">
                            <h3
                                style="font-size: 1.05rem; font-weight: 800; color: var(--text-primary); margin-bottom: 0.35rem;">
                                5G Flagship Smartphones</h3>
                            <p style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.5rem;">Grade-A
                                AMOLED & Fast Snapdragon Chipsets</p>
                            <div class="alibaba-tier-pricing">
                                <span>MOQ: <strong>1 Unit</strong></span>
                                <span>Tier Price: <strong>₹24,499</strong></span>
                            </div>
                            <div
                                style="font-size: 0.75rem; color: var(--success); font-weight: 700; margin-bottom: 0.75rem;">
                                ⚡ Ready to dispatch in 24h
                            </div>
                            <a href="${pageContext.request.contextPath}/products?category=2" class="order-btn-primary"
                                style="margin-top: auto;">Source Smartphones</a>
                        </div>

                        <!-- Factory Card 3 -->
                        <div class="alibaba-factory-card">
                            <span class="alibaba-factory-badge">ISO 9001 CERTIFIED</span>
                            <img src="https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80"
                                alt="Audio Factory" class="alibaba-factory-thumb">
                            <h3
                                style="font-size: 1.05rem; font-weight: 800; color: var(--text-primary); margin-bottom: 0.35rem;">
                                Studio ANC Headphones</h3>
                            <p style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.5rem;">Lossless
                                Hi-Res Audio & Active Noise Cancelling</p>
                            <div class="alibaba-tier-pricing">
                                <span>MOQ: <strong>1 Unit</strong></span>
                                <span>Tier Price: <strong>₹3,299</strong></span>
                            </div>
                            <div
                                style="font-size: 0.75rem; color: var(--success); font-weight: 700; margin-bottom: 0.75rem;">
                                ⚡ Free custom engraving on 5+ pcs
                            </div>
                            <a href="${pageContext.request.contextPath}/products?category=3" class="order-btn-primary"
                                style="margin-top: auto;">Source Audio</a>
                        </div>

                        <!-- Factory Card 4 -->
                        <div class="alibaba-factory-card">
                            <span class="alibaba-factory-badge">TOP RATED SUPPLIER</span>
                            <img src="https://images.unsplash.com/photo-1579586337278-3befd40fd17a?auto=format&fit=crop&w=600&q=80"
                                alt="Smart Wearables Factory" class="alibaba-factory-thumb">
                            <h3
                                style="font-size: 1.05rem; font-weight: 800; color: var(--text-primary); margin-bottom: 0.35rem;">
                                Smartwatches & Fit Bands</h3>
                            <p style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.5rem;">IP68
                                Waterproof, SpO2 & Heart Rate Trackers</p>
                            <div class="alibaba-tier-pricing">
                                <span>MOQ: <strong>1 Unit</strong></span>
                                <span>Tier Price: <strong>₹1,899</strong></span>
                            </div>
                            <div
                                style="font-size: 0.75rem; color: var(--success); font-weight: 700; margin-bottom: 0.75rem;">
                                ⚡ 1-Year Comprehensive Replacement
                            </div>
                            <a href="${pageContext.request.contextPath}/products?category=4" class="order-btn-primary"
                                style="margin-top: auto;">Source Wearables</a>
                        </div>
                    </div>
                </section>

                <!-- 10. BEST SELLERS HORIZONTAL CAROUSEL -->
                <section class="deal-of-day-section" style="margin-top: 0;">
                    <div class="deal-header">
                        <h2 class="deal-title">👑 Best Sellers & Customer Favorites</h2>
                        <div class="deal-header-right">
                            <a href="${pageContext.request.contextPath}/products">Explore All Products →</a>
                        </div>
                    </div>

                    <div class="product-slider-wrapper">
                        <button type="button" class="slider-arrow-btn prev"
                            onclick="scrollProductSlider('bestSellersSliderTrack', -1)"
                            aria-label="Previous Best Sellers">❮</button>
                        <div class="product-slider-track" id="bestSellersSliderTrack">
                            <c:forEach var="item" items="${bestSellers}">
                                <div class="product-card">
                                    <button class="card-wishlist-btn"
                                        onclick="quickAddToWishlist('${item.productId}', event)"
                                        title="Save to Wishlist">❤️</button>
                                    <c:if test="${item.discountPercentage > 0}">
                                        <span class="card-discount-badge">
                                            <fmt:formatNumber value="${item.discountPercentage}"
                                                maxFractionDigits="0" />% OFF
                                        </span>
                                    </c:if>

                                    <a href="${pageContext.request.contextPath}/product?id=${item.productId}">
                                        <div class="product-img-container">
                                            <img src="${not empty item.primaryImageUrl ? item.primaryImageUrl : 'https://placehold.co/300x300?text=ShopKart'}"
                                                alt="${item.productName}" loading="lazy">
                                        </div>
                                        <div class="product-brand">${item.brand}</div>
                                        <div class="product-title">${item.productName}</div>
                                    </a>

                                    <div class="rating-row">
                                        <span class="rating-badge">★ 4.9</span>
                                        <span class="rating-count">(254)</span>
                                    </div>

                                    <div class="price-row">
                                        <span class="current-price">₹
                                            <fmt:formatNumber value="${item.effectivePrice}" pattern="#,##0" />
                                        </span>
                                        <c:if test="${item.discountPercentage > 0}">
                                            <span class="mrp-price">₹
                                                <fmt:formatNumber value="${item.price}" pattern="#,##0" />
                                            </span>
                                        </c:if>
                                    </div>

                                    <div class="delivery-tag">FREE Delivery by <strong>Tomorrow</strong></div>

                                    <div class="card-action-group">
                                        <c:choose>
                                            <c:when test="${sessionScope.currentUser.admin}">
                                                <a href="${pageContext.request.contextPath}/admin/products/edit?id=${item.productId}"
                                                    style="display: flex; align-items: center; justify-content: center; gap: 0.35rem; width: 100%; padding: 0.5rem; font-size: 0.8rem; font-weight: 700; background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; border-radius: 8px; text-decoration: none;">
                                                    ✏️ Edit in Admin Console
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <button class="card-add-cart-btn"
                                                    onclick="quickAddToCart('${item.productId}', 1, event)"
                                                    title="Add to Cart">
                                                    🛒 Add Cart
                                                </button>
                                                <button class="card-buy-now-btn"
                                                    onclick="quickBuyNow('${item.productId}', 1, event)"
                                                    title="Buy Now">
                                                    ⚡ Buy Now
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <button type="button" class="slider-arrow-btn next"
                            onclick="scrollProductSlider('bestSellersSliderTrack', 1)"
                            aria-label="Next Best Sellers">❯</button>
                    </div>
                </section>

                <!-- FOOTER -->
                <footer
                    style="background-color: var(--amazon-dark); color: #cbd5e1; margin-top: auto; padding: 3rem 2rem 2rem; border-top: 1px solid var(--amazon-subnav);">
                    <div
                        style="max-width: 1400px; margin: 0 auto; display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 2rem; margin-bottom: 2rem;">
                        <div>
                            <h4 style="color: #ffffff; margin-bottom: 1rem; font-size: 1rem; font-weight: 800;">Get to
                                Know Us</h4>
                            <ul
                                style="list-style: none; display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.85rem;">
                                <li><a href="#">About ShopKart</a></li>
                                <li><a href="#">Careers</a></li>
                                <li><a href="#">Press Releases</a></li>
                                <li><a href="#">ShopKart Science</a></li>
                            </ul>
                        </div>
                        <div>
                            <h4 style="color: #ffffff; margin-bottom: 1rem; font-size: 1rem; font-weight: 800;">Connect
                                With Us</h4>
                            <ul
                                style="list-style: none; display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.85rem;">
                                <li><a href="#">Facebook</a></li>
                                <li><a href="#">Twitter / X</a></li>
                                <li><a href="#">Instagram</a></li>
                            </ul>
                        </div>
                        <div>
                            <h4 style="color: #ffffff; margin-bottom: 1rem; font-size: 1rem; font-weight: 800;">Make
                                Money with Us</h4>
                            <ul
                                style="list-style: none; display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.85rem;">
                                <li><a href="#">Sell on ShopKart</a></li>
                                <li><a href="#">Supply to ShopKart</a></li>
                                <li><a href="#">Affiliate Marketing</a></li>
                                <li><a href="#">Fulfillment by ShopKart</a></li>
                            </ul>
                        </div>
                        <div>
                            <h4 style="color: #ffffff; margin-bottom: 1rem; font-size: 1rem; font-weight: 800;">Let Us
                                Help You</h4>
                            <ul
                                style="list-style: none; display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.85rem;">
                                <li><a href="${pageContext.request.contextPath}/profile">Your Account</a></li>
                                <li><a href="${pageContext.request.contextPath}/orders">Returns & Replacements</a></li>
                                <li><a href="${pageContext.request.contextPath}/addresses">Saved Addresses</a></li>
                                <c:if test="${sessionScope.currentUser.admin}">
                                    <li><a href="${pageContext.request.contextPath}/health" style="color: #38bdf8;">🩺
                                            System Health</a></li>
                                </c:if>
                            </ul>
                        </div>
                    </div>
                    <div
                        style="text-align: center; border-top: 1px solid var(--amazon-subnav); padding-top: 1.5rem; font-size: 0.82rem; color: #94a3b8; display: flex; flex-direction: column; align-items: center; gap: 0.5rem;">
                        <div>&copy; 2026 ShopKart Inc. All rights reserved. &bull; Trade Assurance &bull; 100% Purchase
                            Protection</div>
                        <div
                            style="padding-top: 0.5rem; border-top: 1px solid rgba(255,255,255,0.08); width: 100%; max-width: 500px; color: #cbd5e1; font-size: 0.85rem;">
                            Designed, Developed &amp; Managed by <strong
                                style="color: #38bdf8; font-weight: 800; letter-spacing: 0.02em;">Rahul Pawar</strong>
                        </div>
                    </div>
                </footer>

                <!-- Order Placed Celebratory Success Modal Popup -->
                <c:if test="${not empty justPlacedOrder}">
                    <style>
                        .modal-backdrop {
                            position: fixed !important;
                            top: 0 !important;
                            left: 0 !important;
                            right: 0 !important;
                            bottom: 0 !important;
                            width: 100vw !important;
                            height: 100vh !important;
                            background: rgba(15, 23, 42, 0.65) !important;
                            backdrop-filter: blur(8px) !important;
                            -webkit-backdrop-filter: blur(8px) !important;
                            z-index: 99999 !important;
                            display: flex !important;
                            align-items: flex-start !important;
                            justify-content: center !important;
                            padding: 3.5rem 1rem 2rem !important;
                            overflow-y: auto !important;
                            box-sizing: border-box !important;
                            animation: orderModalFadeIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
                        }

                        .order-success-modal-card {
                            background: #ffffff !important;
                            border-radius: 24px !important;
                            max-width: 580px !important;
                            width: 100% !important;
                            padding: 2.5rem 2rem 2rem !important;
                            box-shadow: 0 25px 60px -15px rgba(0, 0, 0, 0.35), 0 0 0 1px rgba(226, 232, 240, 0.8) !important;
                            position: relative !important;
                            text-align: center !important;
                            box-sizing: border-box !important;
                            animation: orderModalSlideDown 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
                            margin: 0 auto !important;
                        }

                        @keyframes orderModalFadeIn {
                            from {
                                opacity: 0;
                            }

                            to {
                                opacity: 1;
                            }
                        }

                        @keyframes orderModalSlideDown {
                            from {
                                opacity: 0;
                                transform: translateY(-40px) scale(0.95);
                            }

                            to {
                                opacity: 1;
                                transform: translateY(0) scale(1);
                            }
                        }

                        .order-modal-close-btn {
                            position: absolute;
                            top: 1.25rem;
                            right: 1.25rem;
                            width: 36px;
                            height: 36px;
                            border-radius: 50%;
                            border: none;
                            background: #f1f5f9;
                            color: #64748b;
                            font-size: 1.1rem;
                            font-weight: 700;
                            cursor: pointer;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            transition: all 0.2s ease;
                            z-index: 10;
                        }

                        .order-modal-close-btn:hover {
                            background: #fee2e2;
                            color: #ef4444;
                            transform: rotate(90deg) scale(1.1);
                        }

                        .order-success-badge-icon {
                            width: 72px;
                            height: 72px;
                            margin: 0 auto 1.25rem;
                            border-radius: 50%;
                            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                            color: #ffffff;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 2.2rem;
                            font-weight: 900;
                            box-shadow: 0 10px 25px -5px rgba(16, 185, 129, 0.45);
                            animation: badgePop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
                        }

                        @keyframes badgePop {
                            0% {
                                transform: scale(0);
                            }

                            80% {
                                transform: scale(1.15);
                            }

                            100% {
                                transform: scale(1);
                            }
                        }

                        .checkmark-pulse {
                            display: inline-block;
                        }

                        .order-modal-title {
                            font-size: 1.65rem;
                            font-weight: 900;
                            color: #0f172a;
                            margin: 0 0 0.5rem;
                            letter-spacing: -0.02em;
                        }

                        .order-modal-subtitle {
                            font-size: 0.95rem;
                            color: #64748b;
                            line-height: 1.5;
                            margin: 0 0 1.25rem;
                        }

                        .order-number-pill {
                            display: inline-flex;
                            align-items: center;
                            gap: 0.75rem;
                            background: #f8fafc;
                            border: 1.5px dashed #cbd5e1;
                            padding: 0.5rem 1.25rem;
                            border-radius: 9999px;
                            font-weight: 700;
                            font-family: monospace;
                            font-size: 0.95rem;
                            color: #1e293b;
                            margin-bottom: 1.75rem;
                        }

                        .copy-order-btn {
                            background: #e2e8f0;
                            border: none;
                            padding: 0.25rem 0.65rem;
                            border-radius: 6px;
                            font-size: 0.75rem;
                            font-weight: 700;
                            color: #334155;
                            cursor: pointer;
                            transition: all 0.2s ease;
                        }

                        .copy-order-btn:hover {
                            background: #cbd5e1;
                            transform: scale(1.05);
                        }

                        .order-modal-details-grid {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 0.85rem;
                            margin-bottom: 1.75rem;
                            text-align: left;
                        }

                        @media (max-width: 500px) {
                            .order-modal-details-grid {
                                grid-template-columns: 1fr;
                            }
                        }

                        .order-detail-tile {
                            background: #f8fafc;
                            border: 1px solid #e2e8f0;
                            border-radius: 12px;
                            padding: 0.85rem 1rem;
                            display: flex;
                            align-items: center;
                            gap: 0.75rem;
                            transition: all 0.2s ease;
                        }

                        .order-detail-tile:hover {
                            border-color: #cbd5e1;
                            background: #f1f5f9;
                        }

                        .order-detail-tile .tile-icon {
                            font-size: 1.4rem;
                            flex-shrink: 0;
                        }

                        .order-detail-tile .tile-content {
                            display: flex;
                            flex-direction: column;
                            min-width: 0;
                        }

                        .order-detail-tile .tile-label {
                            font-size: 0.72rem;
                            color: #64748b;
                            font-weight: 700;
                            text-transform: uppercase;
                            letter-spacing: 0.03em;
                        }

                        .order-detail-tile .tile-value {
                            font-size: 0.92rem;
                            color: #0f172a;
                            font-weight: 800;
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                        }

                        .order-modal-actions {
                            display: flex;
                            gap: 0.75rem;
                            justify-content: center;
                            flex-wrap: wrap;
                        }

                        .order-modal-actions .btn-primary-action {
                            background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
                            color: #ffffff;
                            text-decoration: none;
                            padding: 0.85rem 1.25rem;
                            border-radius: 12px;
                            font-weight: 800;
                            font-size: 0.92rem;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            gap: 0.5rem;
                            box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35);
                            transition: all 0.2s ease;
                        }

                        .order-modal-actions .btn-primary-action:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 6px 20px rgba(37, 99, 235, 0.45);
                        }

                        .order-modal-actions .btn-secondary-action {
                            background: #f1f5f9;
                            color: #1e293b;
                            text-decoration: none;
                            padding: 0.85rem 1.15rem;
                            border-radius: 12px;
                            font-weight: 800;
                            font-size: 0.92rem;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            gap: 0.5rem;
                            transition: all 0.2s ease;
                        }

                        .order-modal-actions .btn-secondary-action:hover {
                            background: #e2e8f0;
                            transform: translateY(-2px);
                        }

                        .order-modal-actions .btn-outline-action {
                            background: transparent;
                            color: #475569;
                            border: 1.5px solid #cbd5e1;
                            padding: 0.85rem 1.15rem;
                            border-radius: 12px;
                            font-weight: 800;
                            font-size: 0.92rem;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            gap: 0.5rem;
                            cursor: pointer;
                            transition: all 0.2s ease;
                        }

                        .order-modal-actions .btn-outline-action:hover {
                            border-color: #94a3b8;
                            background: #f8fafc;
                            transform: translateY(-2px);
                        }

                        .confetti-bubble {
                            position: absolute;
                            font-size: 1.6rem;
                            pointer-events: none;
                            animation: confettiFloat 3s ease-in-out infinite;
                        }

                        .confetti-bubble.c1 {
                            top: -12px;
                            left: 10%;
                            animation-delay: 0s;
                        }

                        .confetti-bubble.c2 {
                            top: 20px;
                            right: 8%;
                            animation-delay: 0.7s;
                        }

                        .confetti-bubble.c3 {
                            bottom: 15px;
                            left: 6%;
                            animation-delay: 1.4s;
                        }

                        .confetti-bubble.c4 {
                            bottom: -10px;
                            right: 12%;
                            animation-delay: 2.1s;
                        }

                        @keyframes confettiFloat {

                            0%,
                            100% {
                                transform: translateY(0) rotate(0deg);
                            }

                            50% {
                                transform: translateY(-10px) rotate(12deg);
                            }
                        }
                    </style>

                    <div class="modal-backdrop open" id="orderSuccessModal" onclick="handleOrderModalBackdrop(event)">
                        <div class="order-success-modal-card" role="dialog" aria-labelledby="orderSuccessTitle"
                            aria-modal="true">
                            <!-- Floating Confetti / Sparkle Decor -->
                            <div class="confetti-bubble c1">🎉</div>
                            <div class="confetti-bubble c2">✨</div>
                            <div class="confetti-bubble c3">🚀</div>
                            <div class="confetti-bubble c4">⭐</div>

                            <button type="button" class="order-modal-close-btn" onclick="closeOrderSuccessModal()"
                                aria-label="Close modal">✕</button>

                            <div class="order-modal-hero">
                                <div class="order-success-badge-icon">
                                    <span class="checkmark-pulse">✓</span>
                                </div>
                                <h2 class="order-modal-title" id="orderSuccessTitle">Order Placed Successfully!</h2>
                                <p class="order-modal-subtitle">
                                    Thank you, <strong>${justPlacedOrder.shippingFullName}</strong>! We've received your
                                    order and are dispatching it with express delivery.
                                </p>
                                <div class="order-number-pill">
                                    <span>Order #${justPlacedOrder.orderNumber}</span>
                                    <button type="button" class="copy-order-btn"
                                        data-copy-val="${justPlacedOrder.orderNumber}" title="Copy Order Number">📋
                                        Copy</button>
                                </div>
                            </div>

                            <div class="order-modal-details-grid">
                                <div class="order-detail-tile">
                                    <div class="tile-icon">💰</div>
                                    <div class="tile-content">
                                        <span class="tile-label">Total Amount</span>
                                        <span class="tile-value text-gradient-primary">₹
                                            <fmt:formatNumber value="${justPlacedOrder.totalAmount}"
                                                pattern="#,##0.00" />
                                        </span>
                                    </div>
                                </div>

                                <div class="order-detail-tile">
                                    <div class="tile-icon">💳</div>
                                    <div class="tile-content">
                                        <span class="tile-label">Payment Method</span>
                                        <span class="tile-value">
                                            <c:choose>
                                                <c:when test="${justPlacedOrder.paymentMethod eq 'COD'}">💵 Cash on
                                                    Delivery</c:when>
                                                <c:when test="${justPlacedOrder.paymentMethod eq 'UPI'}">📱 UPI Instant
                                                </c:when>
                                                <c:when test="${justPlacedOrder.paymentMethod eq 'CREDIT_CARD'}">💳 Card
                                                    Payment</c:when>
                                                <c:otherwise>${justPlacedOrder.paymentMethod}</c:otherwise>
                                            </c:choose>
                                            <c:if test="${justPlacedOrder.paymentStatus eq 'PAID'}">
                                                <span class="badge-status badge-success"
                                                    style="font-size: 0.65rem; padding: 0.15rem 0.4rem; margin-left: 0.3rem;">PAID</span>
                                            </c:if>
                                        </span>
                                    </div>
                                </div>

                                <div class="order-detail-tile">
                                    <div class="tile-icon">📍</div>
                                    <div class="tile-content">
                                        <span class="tile-label">Delivering To</span>
                                        <span class="tile-value truncate-1"
                                            title="${justPlacedOrder.shippingFullName}, ${justPlacedOrder.shippingCity}">
                                            ${justPlacedOrder.shippingCity}, ${justPlacedOrder.shippingPostalCode}
                                        </span>
                                    </div>
                                </div>

                                <div class="order-detail-tile">
                                    <div class="tile-icon">🚚</div>
                                    <div class="tile-content">
                                        <span class="tile-label">Estimated Delivery</span>
                                        <span class="tile-value" style="color: var(--success); font-weight: 800;">2-4
                                            Business Days</span>
                                    </div>
                                </div>
                            </div>

                            <div class="order-modal-actions">
                                <a href="${pageContext.request.contextPath}/order/confirmation"
                                    class="btn-primary-action" style="flex: 1.2;">
                                    <span>🧾</span>
                                    <span>View Full Receipt</span>
                                </a>
                                <a href="${pageContext.request.contextPath}/orders" class="btn-secondary-action"
                                    style="flex: 1;">
                                    <span>📦</span>
                                    <span>Track Orders</span>
                                </a>
                                <button type="button" class="btn-outline-action" onclick="closeOrderSuccessModal()"
                                    style="flex: 1;">
                                    <span>🛍️</span>
                                    <span>Keep Shopping</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </c:if>

                <!-- Toast Notification Container -->
                <div id="toastContainer" class="toast-container"></div>

                <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
            </body>

            </html>