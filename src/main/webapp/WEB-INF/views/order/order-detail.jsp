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
                <title>Order #${order.orderNumber} Details & Tracking | ShopKart India</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=4.0">
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link
                    href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap"
                    rel="stylesheet">

                <!-- Self-Contained Order Tracking Styles to Guarantee Flawless Rendering -->
                <style>
                    /* Base Container */
                    .order-tracking-hero {
                        background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%) !important;
                        border-radius: 16px !important;
                        padding: 2rem 2.25rem !important;
                        color: #ffffff !important;
                        margin-bottom: 2rem !important;
                        box-shadow: 0 10px 30px -10px rgba(15, 23, 42, 0.4) !important;
                        position: relative !important;
                        overflow: hidden !important;
                        border: 1px solid rgba(255, 255, 255, 0.1) !important;
                    }

                    .order-tracking-topbar {
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: flex-start !important;
                        flex-wrap: wrap !important;
                        gap: 1.25rem !important;
                        border-bottom: 1px solid rgba(255, 255, 255, 0.15) !important;
                        padding-bottom: 1.5rem !important;
                        margin-bottom: 1.5rem !important;
                    }

                    .order-tracking-meta-title {
                        font-size: 0.75rem !important;
                        font-weight: 700 !important;
                        text-transform: uppercase !important;
                        letter-spacing: 0.08em !important;
                        color: #94a3b8 !important;
                        margin-bottom: 0.35rem !important;
                    }

                    .order-tracking-heading {
                        font-size: 1.75rem !important;
                        font-weight: 900 !important;
                        color: #ffffff !important;
                        letter-spacing: -0.02em !important;
                        margin: 0 0 0.4rem 0 !important;
                        display: flex !important;
                        align-items: center !important;
                        gap: 0.75rem !important;
                        flex-wrap: wrap !important;
                    }

                    .order-tracking-date-info {
                        font-size: 0.88rem !important;
                        color: #cbd5e1 !important;
                    }

                    .order-eta-badge {
                        display: inline-flex !important;
                        align-items: center !important;
                        gap: 0.5rem !important;
                        background: rgba(16, 185, 129, 0.2) !important;
                        border: 1px solid rgba(16, 185, 129, 0.4) !important;
                        color: #34d399 !important;
                        padding: 0.45rem 1rem !important;
                        border-radius: 9999px !important;
                        font-weight: 700 !important;
                        font-size: 0.88rem !important;
                    }

                    /* Order Cards */
                    .order-card-wrapper {
                        background: #ffffff !important;
                        border: 1px solid #e2e8f0 !important;
                        border-radius: 14px !important;
                        overflow: hidden !important;
                        box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.05) !important;
                        margin-bottom: 2rem !important;
                    }

                    .order-card-top {
                        background: #f8fafc !important;
                        padding: 1.2rem 1.5rem !important;
                        border-bottom: 1px solid #e2e8f0 !important;
                        display: flex !important;
                        align-items: center !important;
                        justify-content: space-between !important;
                        flex-wrap: wrap !important;
                        gap: 1rem !important;
                    }

                    .order-card-content {
                        padding: 1.75rem !important;
                    }

                    /* 5-Stage Timeline Stepper */
                    .order-timeline-stepper {
                        display: flex !important;
                        flex-direction: row !important;
                        justify-content: space-between !important;
                        align-items: flex-start !important;
                        position: relative !important;
                        margin: 2.5rem 0 1.5rem !important;
                        padding: 0 1rem !important;
                    }

                    .order-timeline-stepper::before {
                        content: '' !important;
                        position: absolute !important;
                        top: 24px !important;
                        left: 5% !important;
                        right: 5% !important;
                        height: 4px !important;
                        background: #e2e8f0 !important;
                        z-index: 1 !important;
                    }

                    .timeline-step {
                        position: relative !important;
                        z-index: 2 !important;
                        text-align: center !important;
                        flex: 1 !important;
                        display: flex !important;
                        flex-direction: column !important;
                        align-items: center !important;
                    }

                    .timeline-step-icon {
                        width: 50px !important;
                        height: 50px !important;
                        border-radius: 50% !important;
                        background: #ffffff !important;
                        border: 3px solid #cbd5e1 !important;
                        color: #64748b !important;
                        display: flex !important;
                        align-items: center !important;
                        justify-content: center !important;
                        font-size: 1.35rem !important;
                        font-weight: 800 !important;
                        margin-bottom: 0.75rem !important;
                        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06) !important;
                        transition: all 0.3s ease !important;
                    }

                    .timeline-step-title {
                        font-size: 0.92rem !important;
                        font-weight: 700 !important;
                        color: #64748b !important;
                        margin-bottom: 0.2rem !important;
                    }

                    .timeline-step-subtitle {
                        font-size: 0.78rem !important;
                        color: #94a3b8 !important;
                        max-width: 140px !important;
                        line-height: 1.35 !important;
                    }

                    /* Completed Step */
                    .timeline-step.completed .timeline-step-icon {
                        background: #10b981 !important;
                        border-color: #10b981 !important;
                        color: #ffffff !important;
                        box-shadow: 0 6px 16px -2px rgba(16, 185, 129, 0.4) !important;
                    }

                    .timeline-step.completed .timeline-step-title {
                        color: #0f172a !important;
                        font-weight: 800 !important;
                    }

                    /* Active In-Progress Step */
                    .timeline-step.active .timeline-step-icon {
                        background: #ffffff !important;
                        border-color: #f59e0b !important;
                        color: #f59e0b !important;
                        box-shadow: 0 0 0 6px rgba(245, 158, 11, 0.2), 0 8px 20px -4px rgba(245, 158, 11, 0.4) !important;
                    }

                    .timeline-step.active .timeline-step-title {
                        color: #d97706 !important;
                        font-weight: 900 !important;
                    }

                    /* Cancelled Step */
                    .timeline-step.cancelled .timeline-step-icon {
                        background: #ef4444 !important;
                        border-color: #ef4444 !important;
                        color: #ffffff !important;
                    }

                    .timeline-step.cancelled .timeline-step-title {
                        color: #ef4444 !important;
                    }

                    /* Logistics Live Card */
                    .logistics-live-card {
                        background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%) !important;
                        border: 1px solid #bbf7d0 !important;
                        border-radius: 12px !important;
                        padding: 1.25rem 1.5rem !important;
                        margin: 1.75rem 0 !important;
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: center !important;
                        flex-wrap: wrap !important;
                        gap: 1.25rem !important;
                        box-shadow: 0 4px 12px -2px rgba(16, 185, 129, 0.1) !important;
                    }

                    .logistics-info-group {
                        display: flex !important;
                        align-items: center !important;
                        gap: 1.1rem !important;
                    }

                    .logistics-truck-icon {
                        width: 52px !important;
                        height: 52px !important;
                        background: #ffffff !important;
                        border-radius: 50% !important;
                        display: flex !important;
                        align-items: center !important;
                        justify-content: center !important;
                        font-size: 1.75rem !important;
                        box-shadow: 0 4px 10px rgba(16, 185, 129, 0.2) !important;
                        flex-shrink: 0 !important;
                    }

                    .logistics-carrier-name {
                        font-size: 1.05rem !important;
                        font-weight: 800 !important;
                        color: #14532d !important;
                        margin-bottom: 0.25rem !important;
                    }

                    .logistics-awb-row {
                        font-size: 0.88rem !important;
                        color: #166534 !important;
                        display: flex !important;
                        align-items: center !important;
                        gap: 0.6rem !important;
                        flex-wrap: wrap !important;
                    }

                    .awb-code-chip {
                        background: #ffffff !important;
                        border: 1px dashed #86efac !important;
                        padding: 0.2rem 0.6rem !important;
                        border-radius: 6px !important;
                        font-family: monospace !important;
                        font-weight: 800 !important;
                        color: #0f172a !important;
                    }

                    /* 3-Column Info Grid */
                    .order-info-tri-grid {
                        display: grid !important;
                        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)) !important;
                        gap: 1.25rem !important;
                        margin-top: 1.75rem !important;
                    }

                    .order-info-tile {
                        background: #f8fafc !important;
                        border: 1px solid #e2e8f0 !important;
                        border-radius: 10px !important;
                        padding: 1.35rem !important;
                    }

                    .order-info-tile-header {
                        display: flex !important;
                        align-items: center !important;
                        gap: 0.6rem !important;
                        font-size: 0.92rem !important;
                        font-weight: 800 !important;
                        color: #0f172a !important;
                        margin-bottom: 0.85rem !important;
                        padding-bottom: 0.6rem !important;
                        border-bottom: 1px solid #e2e8f0 !important;
                    }

                    /* Activity Feed */
                    .activity-feed-wrapper {
                        margin-top: 2rem !important;
                        background: #ffffff !important;
                        border: 1px solid #e2e8f0 !important;
                        border-radius: 12px !important;
                        padding: 1.5rem !important;
                    }

                    .activity-feed-heading {
                        font-size: 1.05rem !important;
                        font-weight: 800 !important;
                        color: #0f172a !important;
                        margin: 0 0 1.25rem 0 !important;
                        display: flex !important;
                        align-items: center !important;
                        gap: 0.5rem !important;
                    }

                    .activity-feed-list {
                        position: relative !important;
                        padding-left: 2rem !important;
                    }

                    .activity-feed-list::before {
                        content: '' !important;
                        position: absolute !important;
                        top: 6px !important;
                        bottom: 6px !important;
                        left: 8px !important;
                        width: 2px !important;
                        background: #cbd5e1 !important;
                    }

                    .activity-feed-item {
                        position: relative !important;
                        margin-bottom: 1.35rem !important;
                    }

                    .activity-feed-item:last-child {
                        margin-bottom: 0 !important;
                    }

                    .activity-feed-dot {
                        position: absolute !important;
                        left: -2rem !important;
                        top: 2px !important;
                        width: 18px !important;
                        height: 18px !important;
                        border-radius: 50% !important;
                        background: #ffffff !important;
                        border: 3px solid #3b82f6 !important;
                        z-index: 2 !important;
                    }

                    .activity-feed-item.latest .activity-feed-dot {
                        background: #3b82f6 !important;
                        border-color: #93c5fd !important;
                        box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.25) !important;
                    }

                    .activity-feed-status-badge {
                        display: inline-block !important;
                        font-size: 0.72rem !important;
                        font-weight: 800 !important;
                        text-transform: uppercase !important;
                        letter-spacing: 0.04em !important;
                        padding: 0.2rem 0.55rem !important;
                        border-radius: 4px !important;
                        margin-bottom: 0.25rem !important;
                    }

                    .activity-feed-remarks {
                        font-size: 0.88rem !important;
                        color: #334155 !important;
                        font-weight: 600 !important;
                        line-height: 1.45 !important;
                    }

                    .activity-feed-timestamp {
                        font-size: 0.75rem !important;
                        color: #94a3b8 !important;
                        margin-top: 0.15rem !important;
                    }

                    /* Items in Order */
                    .order-product-item {
                        display: flex !important;
                        align-items: center !important;
                        padding: 1.25rem 0 !important;
                        border-bottom: 1px solid #f1f5f9 !important;
                        gap: 1.25rem !important;
                        flex-wrap: wrap !important;
                    }

                    .order-product-item:last-child {
                        border-bottom: none !important;
                        padding-bottom: 0 !important;
                    }

                    .order-product-thumb {
                        width: 70px !important;
                        height: 70px !important;
                        border-radius: 8px !important;
                        border: 1px solid #e2e8f0 !important;
                        display: flex !important;
                        align-items: center !important;
                        justify-content: center !important;
                        background: #f8fafc !important;
                        font-size: 1.8rem !important;
                        flex-shrink: 0 !important;
                    }

                    .order-product-details {
                        flex: 1 !important;
                        min-width: 220px !important;
                    }

                    .item-name {
                        font-size: 1rem !important;
                        font-weight: 800 !important;
                        color: #0f172a !important;
                        text-decoration: none !important;
                        display: block !important;
                        margin-bottom: 0.35rem !important;
                    }

                    .item-name:hover {
                        color: #2563eb !important;
                    }

                    .order-product-actions {
                        display: flex !important;
                        gap: 0.6rem !important;
                        align-items: center !important;
                        flex-wrap: wrap !important;
                    }

                    .order-btn-primary {
                        background: #f59e0b !important;
                        color: #0f172a !important;
                        border: none !important;
                        padding: 0.55rem 1.1rem !important;
                        border-radius: 8px !important;
                        font-weight: 800 !important;
                        font-size: 0.85rem !important;
                        text-decoration: none !important;
                        display: inline-flex !important;
                        align-items: center !important;
                        gap: 0.4rem !important;
                        cursor: pointer !important;
                    }

                    .order-btn-secondary {
                        background: #ffffff !important;
                        color: #334155 !important;
                        border: 1px solid #cbd5e1 !important;
                        padding: 0.55rem 1.1rem !important;
                        border-radius: 8px !important;
                        font-weight: 700 !important;
                        font-size: 0.85rem !important;
                        text-decoration: none !important;
                        display: inline-flex !important;
                        align-items: center !important;
                        gap: 0.4rem !important;
                        cursor: pointer !important;
                    }

                    .copy-order-btn {
                        background: #ffffff !important;
                        border: 1px solid #cbd5e1 !important;
                        border-radius: 6px !important;
                        font-size: 0.78rem !important;
                        padding: 0.35rem 0.75rem !important;
                        cursor: pointer !important;
                        font-weight: 700 !important;
                        display: inline-flex !important;
                        align-items: center !important;
                        gap: 0.3rem !important;
                        color: #334155 !important;
                    }

                    /* Pill Badges */
                    .pill-badge {
                        display: inline-flex !important;
                        align-items: center !important;
                        gap: 0.35rem !important;
                        font-size: 0.8rem !important;
                        font-weight: 800 !important;
                        padding: 0.35rem 0.8rem !important;
                        border-radius: 9999px !important;
                        text-transform: uppercase !important;
                        letter-spacing: 0.04em !important;
                    }

                    .badge-success {
                        background: #10b981 !important;
                        color: #ffffff !important;
                    }

                    .badge-info {
                        background: #3b82f6 !important;
                        color: #ffffff !important;
                    }

                    .badge-warning {
                        background: #f59e0b !important;
                        color: #ffffff !important;
                    }

                    .badge-danger {
                        background: #ef4444 !important;
                        color: #ffffff !important;
                    }

                    @media (max-width: 768px) {
                        .order-timeline-stepper {
                            flex-direction: column !important;
                            align-items: flex-start !important;
                            padding-left: 2.5rem !important;
                        }

                        .order-timeline-stepper::before {
                            top: 24px !important;
                            bottom: 24px !important;
                            left: 20px !important;
                            width: 3px !important;
                            height: auto !important;
                            right: auto !important;
                        }

                        .timeline-step {
                            flex-direction: row !important;
                            align-items: center !important;
                            gap: 1rem !important;
                            margin-bottom: 1.5rem !important;
                            text-align: left !important;
                        }

                        .timeline-step-icon {
                            margin-bottom: 0 !important;
                            margin-left: -2.5rem !important;
                        }

                        .timeline-step-subtitle {
                            max-width: 100% !important;
                        }
                    }

                    /* 4-Column In-Line Unified Order Snapshot Layout */
                    .order-details-hero-card {
                        background: #ffffff !important;
                        border: 1px solid #e2e8f0 !important;
                        border-radius: 14px !important;
                        margin-top: 1.75rem !important;
                        overflow: hidden !important;
                        box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.05), 0 0 1px rgba(0, 0, 0, 0.08) !important;
                    }

                    .order-details-hero-header {
                        background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%) !important;
                        color: #ffffff !important;
                        padding: 1rem 1.5rem !important;
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: center !important;
                        flex-wrap: wrap !important;
                        gap: 0.75rem !important;
                    }

                    .order-details-hero-grid {
                        display: grid !important;
                        grid-template-columns: 1fr 1fr 1.05fr 1.35fr !important;
                        width: 100% !important;
                    }

                    @media (max-width: 992px) {
                        .order-details-hero-grid {
                            grid-template-columns: 1fr 1fr !important;
                        }
                    }

                    @media (max-width: 600px) {
                        .order-details-hero-grid {
                            grid-template-columns: 1fr !important;
                        }
                    }

                    .order-details-col {
                        padding: 1.35rem 1.35rem !important;
                        display: flex !important;
                        flex-direction: column !important;
                        border-right: 1px solid #f1f5f9 !important;
                        background: #ffffff !important;
                    }

                    .order-details-col:last-child {
                        border-right: none !important;
                        background: #fbfcfe !important;
                    }

                    @media (max-width: 992px) {
                        .order-details-col:nth-child(2) {
                            border-right: none !important;
                        }

                        .order-details-col {
                            border-bottom: 1px solid #f1f5f9 !important;
                        }
                    }

                    .order-col-title {
                        height: 38px !important;
                        font-size: 0.84rem !important;
                        font-weight: 800 !important;
                        color: #0f172a !important;
                        margin-bottom: 0.85rem !important;
                        padding-bottom: 0.5rem !important;
                        border-bottom: 2px solid #e2e8f0 !important;
                        display: flex !important;
                        align-items: center !important;
                        gap: 0.45rem !important;
                        letter-spacing: 0.02em !important;
                        text-transform: uppercase !important;
                        white-space: nowrap !important;
                        box-sizing: border-box !important;
                    }

                    .btn-cod-paynow {
                        display: inline-flex !important;
                        align-items: center !important;
                        gap: 0.35rem !important;
                        background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%) !important;
                        color: #ffffff !important;
                        padding: 0.35rem 0.85rem !important;
                        border-radius: 6px !important;
                        font-weight: 800 !important;
                        font-size: 0.78rem !important;
                        text-decoration: none !important;
                        box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25) !important;
                        transition: all 0.2s ease !important;
                        border: none !important;
                        cursor: pointer !important;
                    }

                    .btn-cod-paynow:hover {
                        background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%) !important;
                        box-shadow: 0 4px 12px rgba(37, 99, 235, 0.4) !important;
                        transform: translateY(-1px) !important;
                        color: #ffffff !important;
                    }
                </style>
            </head>

            <body
                style="background: #f8fafc; font-family: 'Plus Jakarta Sans', sans-serif; color: #1e293b; margin: 0; padding: 0;">

                <!-- 1. TOP TICKER STRIP -->
                <header class="top-ticker">
                    <div class="ticker-text">
                        <span class="ticker-badge">⚡ Order Tracking</span>
                        <span>Live Dispatch & Fulfillment Tracking | 100% Purchase Protection</span>
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
                            title="Change Delivery Location">
                            <span class="loc-icon">📍</span>
                            <div class="loc-text">
                                <span class="sub">Deliver to</span>
                                <span class="main" id="headerPincodeText">Bengaluru 560100</span>
                            </div>
                        </div>
                    </div>

                    <div class="header-search-wrapper">
                        <form action="${pageContext.request.contextPath}/products" method="GET"
                            class="header-search-form">
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
                                <span class="badge-count" id="headerCartBadge">${not empty sessionScope.cart ? sessionScope.cart.totalQuantity : 0}</span>
                            </div>
                            <span>Cart</span>
                        </a>
                    </div>
                </nav>

                <!-- 3. SUB-NAVBAR -->
                <div class="sub-navbar">
                    <a href="${pageContext.request.contextPath}/orders" class="all-categories-btn">
                        <span>&larr;</span> <strong>Back to All Orders</strong>
                    </a>
                    <div class="nav-links-strip">
                        <a href="${pageContext.request.contextPath}/products?category=1">💻 Laptops</a>
                        <a href="${pageContext.request.contextPath}/products?category=2">📱 Mobiles</a>
                        <a href="${pageContext.request.contextPath}/products?category=3">🎧 Audio</a>
                        <a href="${pageContext.request.contextPath}/products?category=5">👕 Fashion</a>
                    </div>
                </div>

                <!-- MAIN ORDER DETAIL VIEW -->
                <main style="max-width: 1040px; margin: 2rem auto 4rem; padding: 0 1.5rem;">

                    <c:if test="${param.cancelled == 'true'}">
                        <div
                            style="background: #fee2e2; border-left: 4px solid var(--danger); padding: 1rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #991b1b; font-weight: 600;">
                            ✓ Order #${order.orderNumber} has been successfully cancelled and payment/inventory
                            restored.
                        </div>
                    </c:if>

                    <c:if test="${param.returnSubmitted == 'true'}">
                        <div
                            style="background: #dcfce7; border-left: 4px solid #16a34a; padding: 1rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #14532d; font-weight: 700; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.5rem;">
                            <div>
                                ✓ Your return / replacement request (Ref: <strong>
                                    <c:out value="${param.returnNum}" />
                                </strong>) has been submitted successfully!
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${not empty param.returnError}">
                        <div
                            style="background: #fee2e2; border-left: 4px solid #dc2626; padding: 1rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #991b1b; font-weight: 700;">
                            ✕ Return Error:
                            <c:out value="${param.returnError}" />
                        </div>
                    </c:if>

                    <c:choose>
                        <c:when test="${order.orderStatus == 'DELIVERED'}">
                            <!-- =========================================================================
                     1. DELIVERED ORDER HERO (ONLY ORDER DETAILS & DELIVERED BANNER)
                     ========================================================================= -->
                            <div class="order-delivered-hero"
                                style="background: linear-gradient(135deg, #064e3b 0%, #065f46 100%); border-radius: 16px; padding: 2rem 2.25rem; color: #ffffff; margin-bottom: 2rem; box-shadow: 0 10px 30px -10px rgba(6, 78, 59, 0.4); position: relative; overflow: hidden; border: 1px solid rgba(52, 211, 153, 0.3);">
                                <div
                                    style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1.25rem; border-bottom: 1px solid rgba(255, 255, 255, 0.18); padding-bottom: 1.5rem; margin-bottom: 1.5rem;">
                                    <div>
                                        <div
                                            style="display: inline-flex; align-items: center; gap: 0.4rem; font-size: 0.75rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; color: #a7f3d0; margin-bottom: 0.4rem; background: rgba(0,0,0,0.2); padding: 0.2rem 0.65rem; border-radius: 9999px;">
                                            ✓ 100% Fulfilled & Verified
                                        </div>
                                        <div
                                            style="font-size: 1.75rem; font-weight: 900; color: #ffffff; letter-spacing: -0.02em; margin: 0 0 0.4rem 0; display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                                            Order #${order.orderNumber}
                                            <button type="button" class="copy-order-btn"
                                                data-copy-val="${order.orderNumber}"
                                                style="background: rgba(255,255,255,0.18); border: 1px solid rgba(255,255,255,0.3); color: #ffffff; padding: 0.35rem 0.75rem; font-size: 0.8rem; border-radius: 6px; cursor: pointer; font-weight: 700; display: inline-flex; align-items: center; gap: 0.3rem;">
                                                📋 Copy #
                                            </button>
                                        </div>
                                        <div style="font-size: 0.92rem; color: #d1fae5;">
                                            Placed on <strong>
                                                <c:choose>
                                                    <c:when test="${not empty order.formattedCreatedAt}">
                                                        ${order.formattedCreatedAt}</c:when>
                                                    <c:otherwise>${order.createdAt}</c:otherwise>
                                                </c:choose>
                                            </strong> &bull; Order ID: <code>#${order.orderId}</code>
                                        </div>
                                    </div>

                                    <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                                        <button type="button" onclick="window.print()" class="order-btn-secondary"
                                            style="background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.3); color: #ffffff; font-size: 0.85rem; padding: 0.55rem 1.1rem; border-radius: 8px; font-weight: 700; cursor: pointer;">
                                            🖨️ Download / Print Invoice
                                        </button>

                                        <c:choose>
                                            <c:when test="${not empty orderReturn}">
                                                <span class="pill-badge ${orderReturn.statusBadgeClass}"
                                                    style="font-size: 0.88rem; padding: 0.55rem 1.15rem; border-radius: 8px; font-weight: 800; display: inline-flex; align-items: center; gap: 0.35rem;">
                                                    🔄 Return
                                                    <c:out value="${orderReturn.returnStatus}" />
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="button" onclick="openOrderReturnModal()"
                                                    style="background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); color: #ffffff; border: none; font-size: 0.88rem; padding: 0.55rem 1.25rem; border-radius: 8px; font-weight: 800; cursor: pointer; display: inline-flex; align-items: center; gap: 0.4rem; box-shadow: 0 4px 12px rgba(217, 119, 6, 0.35);">
                                                    🔄 Return / Replace Order
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <!-- Verified Delivered on Banner -->
                                <div
                                    style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                                    <div
                                        style="display: inline-flex; align-items: center; gap: 0.6rem; background: rgba(16, 185, 129, 0.25); border: 1px solid rgba(52, 211, 153, 0.5); color: #ffffff; padding: 0.6rem 1.25rem; border-radius: 12px; font-weight: 800; font-size: 1.05rem;">
                                        <span style="font-size: 1.3rem;">✓</span>
                                        <span>Delivered on <strong>
                                                <c:out value="${order.formattedDeliveredDate}" />
                                            </strong></span>
                                    </div>

                                    <div style="font-size: 0.95rem; color: #d1fae5;">
                                        Total Paid: <strong style="color: #ffffff; font-size: 1.25rem;">₹
                                            <fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2" />
                                        </strong> (${order.paymentMethod})
                                    </div>
                                </div>
                            </div>

                            <!-- Active Return Details Card (if customer previously requested a return) -->
                            <c:if test="${not empty orderReturn}">
                                <div class="order-card-wrapper"
                                    style="background: #ffffff; border: 1.5px solid #cbd5e1; border-radius: 14px; overflow: hidden; margin-bottom: 2rem; box-shadow: var(--shadow-sm);">
                                    <div class="order-card-top"
                                        style="background: #f8fafc; padding: 1.1rem 1.5rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.75rem;">
                                        <div style="display: flex; align-items: center; gap: 0.6rem;">
                                            <span style="font-size: 1.2rem;">🔄</span>
                                            <h2
                                                style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin: 0;">
                                                Return & Replacement Status (Ref:
                                                <c:out value="${orderReturn.returnNumber}" />)
                                            </h2>
                                        </div>
                                        <span class="pill-badge ${orderReturn.statusBadgeClass}"
                                            style="font-size: 0.82rem; font-weight: 800; padding: 0.35rem 0.85rem;">
                                            <c:out value="${orderReturn.returnStatus}" />
                                        </span>
                                    </div>
                                    <div class="order-card-content" style="padding: 1.5rem;">
                                        <div
                                            style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.25rem; font-size: 0.88rem;">
                                            <div>
                                                <span
                                                    style="color: #64748b; font-weight: 700; font-size: 0.78rem; text-transform: uppercase;">Return
                                                    Reason:</span>
                                                <div style="font-weight: 800; color: #0f172a; margin-top: 0.2rem;">
                                                    <c:out value="${orderReturn.returnReason}" />
                                                </div>
                                            </div>
                                            <div>
                                                <span
                                                    style="color: #64748b; font-weight: 700; font-size: 0.78rem; text-transform: uppercase;">Resolution
                                                    Preference:</span>
                                                <div style="font-weight: 800; color: #2563eb; margin-top: 0.2rem;">
                                                    <c:choose>
                                                        <c:when test="${orderReturn.resolutionType == 'REFUND'}">💳 Full
                                                            Refund to Source</c:when>
                                                        <c:when test="${orderReturn.resolutionType == 'REPLACEMENT'}">🔄
                                                            Brand New Replacement</c:when>
                                                        <c:otherwise>
                                                            <c:out value="${orderReturn.resolutionType}" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                            <div>
                                                <span
                                                    style="color: #64748b; font-weight: 700; font-size: 0.78rem; text-transform: uppercase;">Requested
                                                    On:</span>
                                                <div style="font-weight: 800; color: #0f172a; margin-top: 0.2rem;">
                                                    <c:out value="${orderReturn.formattedCreatedAt}" />
                                                </div>
                                            </div>
                                        </div>
                                        <c:if test="${not empty orderReturn.comments}">
                                            <div
                                                style="margin-top: 1rem; padding-top: 0.85rem; border-top: 1px solid #f1f5f9; font-size: 0.85rem; color: #475569;">
                                                <strong>Customer Notes:</strong>
                                                <c:out value="${orderReturn.comments}" />
                                            </div>
                                        </c:if>
                                        <c:if test="${not empty orderReturn.adminNotes}">
                                            <div
                                                style="margin-top: 0.75rem; background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 0.75rem 1rem; font-size: 0.85rem; color: #1e40af;">
                                                <strong>Support Team Note:</strong>
                                                <c:out value="${orderReturn.adminNotes}" />
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Consolidated In-Line Details Hero Card: Shipping, Billing, Payment & Summary -->
                            <div class="order-details-hero-card" style="margin-bottom: 2rem;">
                                <div class="order-details-hero-header">
                                    <div
                                        style="font-size: 0.95rem; font-weight: 800; display: flex; align-items: center; gap: 0.6rem;">
                                        <span>📑</span> Order Details & Payment Summary
                                    </div>
                                    <div style="display: flex; align-items: center; gap: 0.75rem; font-size: 0.82rem;">
                                        <span
                                            style="background: rgba(255, 255, 255, 0.15); padding: 0.25rem 0.65rem; border-radius: 6px; font-weight: 700; font-family: monospace;">
                                            Order #
                                            <c:out value="${order.orderNumber}" />
                                        </span>
                                        <span style="color: #cbd5e1; font-weight: 500;">
                                            Placed on: <c:choose>
                                                <c:when test="${not empty order.formattedCreatedAt}">
                                                    ${order.formattedCreatedAt}</c:when>
                                                <c:otherwise>${order.createdAt}</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                </div>

                                <div class="order-details-hero-grid">

                                    <!-- 1. Shipping Address -->
                                    <div class="order-details-col">
                                        <div class="order-col-title">
                                            <span>📍</span> SHIPPING ADDRESS
                                        </div>
                                        <div
                                            style="font-weight: 800; color: #0f172a; font-size: 0.92rem; margin-bottom: 0.25rem;">
                                            <c:out value="${order.shippingFullName}" />
                                        </div>
                                        <div style="color: #475569; font-size: 0.83rem; line-height: 1.55; flex: 1;">
                                            <c:out value="${order.shippingAddressLine1}" />
                                            <c:if test="${not empty order.shippingAddressLine2}">,
                                                <c:out value="${order.shippingAddressLine2}" />
                                            </c:if><br>
                                            <c:out value="${order.shippingCity}" />,
                                            <c:out value="${order.shippingState}" /> - <strong>
                                                <c:out value="${order.shippingPostalCode}" />
                                            </strong><br>
                                            <span style="color: #64748b;">Phone:</span> <strong>
                                                <c:out value="${order.shippingPhone}" />
                                            </strong>
                                        </div>
                                    </div>

                                    <!-- 2. Billing Address -->
                                    <div class="order-details-col">
                                        <div class="order-col-title">
                                            <span>🏢</span> BILLING ADDRESS
                                        </div>
                                        <c:choose>
                                            <c:when test="${not empty order.billingAddressSnapshot}">
                                                <div
                                                    style="color: #475569; font-size: 0.83rem; line-height: 1.55; flex: 1;">
                                                    <c:out value="${order.billingAddressSnapshot}" />
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div
                                                    style="font-weight: 800; color: #0f172a; font-size: 0.92rem; margin-bottom: 0.25rem;">
                                                    <c:out value="${order.shippingFullName}" />
                                                </div>
                                                <div
                                                    style="color: #475569; font-size: 0.83rem; line-height: 1.55; flex: 1;">
                                                    <c:out value="${order.shippingAddressLine1}" />
                                                    <c:if test="${not empty order.shippingAddressLine2}">,
                                                        <c:out value="${order.shippingAddressLine2}" />
                                                    </c:if><br>
                                                    <c:out value="${order.shippingCity}" />,
                                                    <c:out value="${order.shippingState}" /> - <strong>
                                                        <c:out value="${order.shippingPostalCode}" />
                                                    </strong><br>
                                                    <span
                                                        style="display: inline-block; margin-top: 0.4rem; font-size: 0.72rem; background: #e0f2fe; color: #0369a1; padding: 0.15rem 0.5rem; border-radius: 4px; font-weight: 700;">
                                                        ✓ Same as Shipping Address
                                                    </span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <!-- 3. Payment Information -->
                                    <div class="order-details-col">
                                        <div class="order-col-title">
                                            <span>💳</span> PAYMENT INFO
                                        </div>
                                        <div
                                            style="color: #475569; font-size: 0.83rem; line-height: 1.6; flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
                                            <div>
                                                <div style="margin-bottom: 0.35rem;">
                                                    Payment Method: <strong style="color: #0f172a;">
                                                        <c:choose>
                                                            <c:when test="${order.paymentMethod == 'COD'}">COD</c:when>
                                                            <c:otherwise>
                                                                <c:out value="${order.paymentMethod}" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </strong>
                                                </div>
                                                <div
                                                    style="margin-bottom: 0.4rem; display: flex; align-items: center; gap: 0.4rem;">
                                                    <span>Payment Status:</span>
                                                    <strong style="color: #10b981;">Paid</strong>
                                                </div>
                                            </div>
                                            <div style="font-size: 0.76rem; color: #64748b; margin-top: 0.4rem;">
                                                Security: <strong style="color: #10b981;">🛡️ 256-Bit SSL</strong>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 4. Order Summary -->
                                    <div class="order-details-col">
                                        <div class="order-col-title">
                                            <span>🧾</span> ORDER SUMMARY
                                        </div>
                                        <div
                                            style="font-size: 0.83rem; color: #475569; line-height: 1.65; flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
                                            <div>
                                                <div style="display: flex; justify-content: space-between;">
                                                    <span>Items Subtotal:</span>
                                                    <span style="font-weight: 600; color: #1e293b;">₹
                                                        <fmt:formatNumber value="${order.subtotal}"
                                                            minFractionDigits="2" />
                                                    </span>
                                                </div>
                                                <c:if test="${order.discountAmount > 0}">
                                                    <div
                                                        style="display: flex; justify-content: space-between; color: #10b981; font-weight: 700;">
                                                        <span>Applied Discount:</span>
                                                        <span>-₹
                                                            <fmt:formatNumber value="${order.discountAmount}"
                                                                minFractionDigits="2" />
                                                        </span>
                                                    </div>
                                                </c:if>
                                                <div style="display: flex; justify-content: space-between;">
                                                    <span>Shipping:</span>
                                                    <span>
                                                        <c:choose>
                                                            <c:when test="${order.shippingAmount == 0}"><strong
                                                                    style="color: #10b981;">FREE</strong></c:when>
                                                            <c:otherwise>₹
                                                                <fmt:formatNumber value="${order.shippingAmount}"
                                                                    minFractionDigits="2" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                </div>
                                                <div style="display: flex; justify-content: space-between;">
                                                    <span>GST (Included):</span>
                                                    <span>₹
                                                        <fmt:formatNumber value="${order.taxAmount}"
                                                            minFractionDigits="2" />
                                                    </span>
                                                </div>
                                            </div>
                                            <div
                                                style="display: flex; justify-content: space-between; align-items: center; font-weight: 900; color: #0f172a; font-size: 0.92rem; border-top: 2px solid #e2e8f0; padding-top: 0.45rem; margin-top: 0.45rem; flex-wrap: nowrap;">
                                                <span>Total Paid:</span>
                                                <span
                                                    style="color: #2563eb; font-size: 1.05rem; white-space: nowrap; margin-left: 0.4rem;">₹
                                                    <fmt:formatNumber value="${order.totalAmount}"
                                                        minFractionDigits="2" />
                                                </span>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                        </c:when>

                        <c:otherwise>
                            <!-- =========================================================================
                     2. IN-PROGRESS / CANCELLED ORDER TRACKING HERO & STEPPER
                     ========================================================================= -->
                            <div class="order-tracking-hero"
                                style="background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%); border-radius: 16px; padding: 2rem 2.25rem; color: #ffffff; margin-bottom: 2rem; box-shadow: 0 10px 30px -10px rgba(15, 23, 42, 0.4); position: relative; overflow: hidden; border: 1px solid rgba(255, 255, 255, 0.1);">
                                <div class="order-tracking-topbar"
                                    style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1.25rem; border-bottom: 1px solid rgba(255, 255, 255, 0.15); padding-bottom: 1.5rem; margin-bottom: 1.5rem;">
                                    <div>
                                        <div class="order-tracking-meta-title"
                                            style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; color: #94a3b8; margin-bottom: 0.35rem;">
                                            Verified Customer Order &bull; 256-Bit Encrypted
                                        </div>
                                        <div class="order-tracking-heading"
                                            style="font-size: 1.75rem; font-weight: 900; color: #ffffff; letter-spacing: -0.02em; margin: 0 0 0.4rem 0; display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                                            Order #${order.orderNumber}
                                            <button type="button" class="copy-order-btn"
                                                onclick="copyOrderNumber('${order.orderNumber}', event)"
                                                style="background: rgba(255,255,255,0.18); border: 1px solid rgba(255,255,255,0.3); color: #ffffff; padding: 0.35rem 0.75rem; font-size: 0.8rem; border-radius: 6px; cursor: pointer; font-weight: 700; display: inline-flex; align-items: center; gap: 0.3rem;">
                                                📋 Copy #
                                            </button>
                                        </div>
                                        <div class="order-tracking-date-info"
                                            style="font-size: 0.88rem; color: #cbd5e1;">
                                            Placed on <strong>
                                                <c:choose>
                                                    <c:when test="${not empty order.formattedCreatedAt}">
                                                        ${order.formattedCreatedAt}</c:when>
                                                    <c:otherwise>${order.createdAt}</c:otherwise>
                                                </c:choose>
                                            </strong> &bull; Order Ref: <code>${order.orderNumber}</code>
                                        </div>
                                    </div>

                                    <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                                        <button type="button" onclick="window.print()" class="order-btn-secondary"
                                            style="background: rgba(255,255,255,0.12); border: 1px solid rgba(255,255,255,0.25); color: #ffffff; font-size: 0.85rem; padding: 0.55rem 1.1rem; border-radius: 8px; font-weight: 700; cursor: pointer;">
                                            🖨️ Download / Print Invoice
                                        </button>
                                        <c:choose>
                                            <c:when test="${order.orderStatus == 'CANCELLED'}">
                                                <span class="pill-badge badge-danger"
                                                    style="font-size: 0.9rem; padding: 0.5rem 1.1rem; background: #ef4444; color: #ffffff; border-radius: 9999px; font-weight: 800;">
                                                    ✕ CANCELLED
                                                </span>
                                            </c:when>
                                            <c:when test="${order.orderStatus == 'OUT_FOR_DELIVERY'}">
                                                <span class="pill-badge badge-warning"
                                                    style="font-size: 0.9rem; padding: 0.5rem 1.1rem; background: #f59e0b; color: #ffffff; border-radius: 9999px; font-weight: 800;">
                                                    🛵 OUT FOR DELIVERY
                                                </span>
                                            </c:when>
                                            <c:when
                                                test="${order.orderStatus == 'DISPATCHED' || order.orderStatus == 'IN_TRANSIT' || order.orderStatus == 'SHIPPED'}">
                                                <span class="pill-badge badge-info"
                                                    style="font-size: 0.9rem; padding: 0.5rem 1.1rem; background: #3b82f6; color: #ffffff; border-radius: 9999px; font-weight: 800;">
                                                    🚚 IN TRANSIT
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="pill-badge badge-warning"
                                                    style="font-size: 0.9rem; padding: 0.5rem 1.1rem; background: #eab308; color: #000000; border-radius: 9999px; font-weight: 800;">
                                                    ⏳
                                                    <c:out value="${order.orderStatus}" />
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <!-- Delivery ETA Pill -->
                                <div
                                    style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                                    <div class="order-eta-badge"
                                        style="display: inline-flex; align-items: center; gap: 0.5rem; background: rgba(16, 185, 129, 0.2); border: 1px solid rgba(16, 185, 129, 0.4); color: #34d399; padding: 0.45rem 1rem; border-radius: 9999px; font-weight: 700; font-size: 0.88rem;">
                                        <span style="font-size: 1.1rem;">⚡</span>
                                        <span>
                                            <c:choose>
                                                <c:when test="${order.orderStatus == 'CANCELLED'}">
                                                    This order was cancelled and is no longer in transit
                                                </c:when>
                                                <c:otherwise>
                                                    Estimated Express Delivery: <strong>Within 2-4 Business
                                                        Days</strong>
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>

                                    <div style="font-size: 0.88rem; color: #cbd5e1;">
                                        Total: <strong style="color: #ffffff; font-size: 1.15rem;">₹
                                            <fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2" />
                                        </strong> (${order.paymentMethod})
                                    </div>
                                </div>
                            </div>

                            <!-- 2. Live 5-Stage Visual Tracking Stepper Card -->
                            <div class="order-card-wrapper"
                                style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 14px; overflow: hidden; box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.05); margin-bottom: 2rem;">
                                <div class="order-card-top"
                                    style="background: #f8fafc; padding: 1.2rem 1.5rem; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem;">
                                    <h2 style="font-size: 1.1rem; font-weight: 800; color: #0f172a; margin: 0;">
                                        📍 Live Shipment & Milestone Tracking
                                    </h2>
                                    <span style="font-size: 0.85rem; color: #64748b;">
                                        Real-time updates directly from fulfillment network
                                    </span>
                                </div>

                                <div class="order-card-content" style="padding: 1.75rem;">
                                    <!-- 5-Stage Stepper -->
                                    <div class="order-timeline-stepper"
                                        style="display: flex; flex-direction: row; justify-content: space-between; align-items: flex-start; position: relative; margin: 2.5rem 0 1.5rem; padding: 0 1rem;">

                                        <!-- Dynamic Progress Fill Bar -->
                                        <div
                                            style="position: absolute; top: 24px; left: 10%; height: 4px; background: #10b981; z-index: 1; transition: width 0.4s ease; width: '${order.orderStatus == 'CANCELLED' ? '0%' : (order.milestoneStep <= 1 ? '0%' : (order.milestoneStep == 2 ? '20%' : (order.milestoneStep == 3 ? '40%' : (order.milestoneStep == 4 ? '60%' : '80%'))))}';">
                                        </div>

                                        <!-- Step 1: Confirmed -->
                                        <div class="timeline-step ${order.orderStatus != 'CANCELLED' ? 'completed' : 'cancelled'}"
                                            style="position: relative; z-index: 2; text-align: center; flex: 1; display: flex; flex-direction: column; align-items: center;">
                                            <div class="timeline-step-icon"
                                                style="width: 50px; height: 50px; border-radius: 50%; ${order.orderStatus != 'CANCELLED' ? 'background: #10b981; border: 3px solid #10b981; color: #ffffff;' : 'background: #ef4444; border: 3px solid #ef4444; color: #ffffff;'} display: flex; align-items: center; justify-content: center; font-size: 1.35rem; font-weight: 800; margin-bottom: 0.75rem; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);">
                                                ${order.orderStatus == 'CANCELLED' ? '✕' : '✓'}
                                            </div>
                                            <div class="timeline-step-title"
                                                style="font-size: 0.92rem; font-weight: 800; color: #0f172a; margin-bottom: 0.2rem;">
                                                Order Confirmed</div>
                                            <div class="timeline-step-subtitle"
                                                style="font-size: 0.78rem; color: #94a3b8; max-width: 140px; line-height: 1.35;">
                                                <c:choose>
                                                    <c:when test="${not empty order.formattedCreatedAt}">
                                                        ${order.formattedCreatedAt}</c:when>
                                                    <c:otherwise>${order.createdAt}</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>

                                        <!-- Step 2: Dispatch / Dispatched -->
                                        <div class="timeline-step ${order.orderStatus == 'CANCELLED' ? 'cancelled' : (order.milestoneStep > 2 ? 'completed' : (order.milestoneStep == 2 ? 'active' : ''))}"
                                            style="position: relative; z-index: 2; text-align: center; flex: 1; display: flex; flex-direction: column; align-items: center;">
                                            <div class="timeline-step-icon"
                                                style="width: 50px; height: 50px; border-radius: 50%; ${order.milestoneStep > 2 ? 'background: #10b981; border: 3px solid #10b981; color: #ffffff;' : (order.milestoneStep == 2 ? 'background: #ffffff; border: 3px solid #f59e0b; color: #f59e0b; box-shadow: 0 0 0 6px rgba(245, 158, 11, 0.2);' : 'background: #ffffff; border: 3px solid #cbd5e1; color: #64748b;')} display: flex; align-items: center; justify-content: center; font-size: 1.35rem; font-weight: 800; margin-bottom: 0.75rem; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);">
                                                ${order.orderStatus == 'CANCELLED' ? '✕' : (order.milestoneStep > 2 ? '✓' : '📦')}
                                            </div>
                                            <div class="timeline-step-title"
                                                style="font-size: 0.92rem; font-weight: ${order.milestoneStep > 2 ? '800' : (order.milestoneStep == 2 ? '900' : '700')}; color: ${order.milestoneStep > 2 ? '#0f172a' : (order.milestoneStep == 2 ? '#d97706' : '#64748b')}; margin-bottom: 0.2rem;">
                                                ${order.milestoneStep > 2 ? 'Dispatched' : 'Dispatch'}
                                            </div>
                                            <div class="timeline-step-subtitle"
                                                style="font-size: 0.78rem; color: #94a3b8; max-width: 140px; line-height: 1.35;">
                                                ${order.milestoneStep > 2 ? (not empty order.courierPartner ? order.courierPartner : 'Dispatched from Hub') : (order.milestoneStep == 2 ? 'Dispatch in Progress' : 'Awaiting Dispatch')}
                                            </div>
                                        </div>

                                        <!-- Step 3: In Transit -->
                                        <div class="timeline-step ${order.orderStatus == 'CANCELLED' ? '' : (order.milestoneStep > 3 ? 'completed' : (order.milestoneStep == 3 ? 'active' : ''))}"
                                            style="position: relative; z-index: 2; text-align: center; flex: 1; display: flex; flex-direction: column; align-items: center;">
                                            <div class="timeline-step-icon"
                                                style="width: 50px; height: 50px; border-radius: 50%; ${order.milestoneStep > 3 ? 'background: #10b981; border: 3px solid #10b981; color: #ffffff;' : (order.milestoneStep == 3 ? 'background: #ffffff; border: 3px solid #f59e0b; color: #f59e0b; box-shadow: 0 0 0 6px rgba(245, 158, 11, 0.2);' : 'background: #ffffff; border: 3px solid #cbd5e1; color: #64748b;')} display: flex; align-items: center; justify-content: center; font-size: 1.35rem; font-weight: 800; margin-bottom: 0.75rem; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);">
                                                ${order.milestoneStep > 3 ? '✓' : '🚚'}
                                            </div>
                                            <div class="timeline-step-title"
                                                style="font-size: 0.92rem; font-weight: ${order.milestoneStep > 3 ? '800' : (order.milestoneStep == 3 ? '900' : '700')}; color: ${order.milestoneStep > 3 ? '#0f172a' : (order.milestoneStep == 3 ? '#d97706' : '#64748b')}; margin-bottom: 0.2rem;">
                                                In Transit</div>
                                            <div class="timeline-step-subtitle"
                                                style="font-size: 0.78rem; color: #94a3b8; max-width: 140px; line-height: 1.35;">
                                                ${order.milestoneStep >= 3 ? (not empty order.courierPartner ? 'In Transit via '.concat(order.courierPartner) : 'Moving to Hub') : 'Awaiting Movement'}
                                            </div>
                                        </div>

                                        <!-- Step 4: Out for Delivery -->
                                        <div class="timeline-step ${order.orderStatus == 'CANCELLED' ? '' : (order.milestoneStep > 4 ? 'completed' : (order.milestoneStep == 4 ? 'active' : ''))}"
                                            style="position: relative; z-index: 2; text-align: center; flex: 1; display: flex; flex-direction: column; align-items: center;">
                                            <div class="timeline-step-icon"
                                                style="width: 50px; height: 50px; border-radius: 50%; ${order.milestoneStep > 4 ? 'background: #10b981; border: 3px solid #10b981; color: #ffffff;' : (order.milestoneStep == 4 ? 'background: #ffffff; border: 3px solid #f59e0b; color: #f59e0b; box-shadow: 0 0 0 6px rgba(245, 158, 11, 0.2);' : 'background: #ffffff; border: 3px solid #cbd5e1; color: #64748b;')} display: flex; align-items: center; justify-content: center; font-size: 1.35rem; font-weight: 800; margin-bottom: 0.75rem; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);">
                                                ${order.milestoneStep > 4 ? '✓' : '🛵'}
                                            </div>
                                            <div class="timeline-step-title"
                                                style="font-size: 0.92rem; font-weight: ${order.milestoneStep > 4 ? '800' : (order.milestoneStep == 4 ? '900' : '700')}; color: ${order.milestoneStep > 4 ? '#0f172a' : (order.milestoneStep == 4 ? '#d97706' : '#64748b')}; margin-bottom: 0.2rem;">
                                                Out for Delivery</div>
                                            <div class="timeline-step-subtitle"
                                                style="font-size: 0.78rem; color: #94a3b8; max-width: 140px; line-height: 1.35;">
                                                ${order.milestoneStep >= 4 ? 'Local Hub Agent' : 'Destination Hub'}
                                            </div>
                                        </div>

                                        <!-- Step 5: Delivered -->
                                        <div class="timeline-step"
                                            style="position: relative; z-index: 2; text-align: center; flex: 1; display: flex; flex-direction: column; align-items: center;">
                                            <div class="timeline-step-icon"
                                                style="width: 50px; height: 50px; border-radius: 50%; background: #ffffff; border: 3px solid #cbd5e1; color: #64748b; display: flex; align-items: center; justify-content: center; font-size: 1.35rem; font-weight: 800; margin-bottom: 0.75rem; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);">
                                                🎁
                                            </div>
                                            <div class="timeline-step-title"
                                                style="font-size: 0.92rem; font-weight: 700; color: #64748b; margin-bottom: 0.2rem;">
                                                Delivered</div>
                                            <div class="timeline-step-subtitle"
                                                style="font-size: 0.78rem; color: #94a3b8; max-width: 140px; line-height: 1.35;">
                                                Doorstep Handover
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Logistics & Courier Info Banner (if dispatched or later) -->
                                    <c:if test="${not empty order.courierPartner || not empty order.trackingNumber}">
                                        <div class="logistics-live-card"
                                            style="background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%); border: 1px solid #bbf7d0; border-radius: 12px; padding: 1.25rem 1.5rem; margin: 1.75rem 0; box-shadow: 0 4px 12px -2px rgba(16, 185, 129, 0.1);">
                                            <div
                                                style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1.25rem;">
                                                <div class="logistics-info-group"
                                                    style="display: flex; align-items: center; gap: 1.1rem;">
                                                    <div class="logistics-truck-icon"
                                                        style="width: 52px; height: 52px; background: #ffffff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.75rem; box-shadow: 0 4px 10px rgba(16, 185, 129, 0.2); flex-shrink: 0;">
                                                        🚚
                                                    </div>
                                                    <div>
                                                        <div class="logistics-carrier-name"
                                                            style="font-size: 1.05rem; font-weight: 800; color: #14532d; margin-bottom: 0.25rem; display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
                                                            <span>Dispatched via
                                                                <c:out value="${order.courierPartner}" />
                                                            </span>
                                                            <span
                                                                style="font-size: 0.72rem; background: #166534; color: #ffffff; padding: 0.15rem 0.55rem; border-radius: 9999px; font-weight: 800;">LIVE
                                                                API INTEGRATED</span>
                                                        </div>
                                                        <div class="logistics-awb-row"
                                                            style="font-size: 0.88rem; color: #166534; display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap;">
                                                            <span>Tracking AWB:</span>
                                                            <span class="awb-code-chip"
                                                                style="background: #ffffff; border: 1px dashed #86efac; padding: 0.2rem 0.6rem; border-radius: 6px; font-family: monospace; font-weight: 800; color: #0f172a;">
                                                                <c:out value="${order.trackingNumber}" />
                                                            </span>
                                                            <c:if test="${not empty order.deliveryAgentPhone}">
                                                                <span>&bull; Delivery Agent Contact: <strong>
                                                                        <c:out value="${order.deliveryAgentPhone}" />
                                                                    </strong></span>
                                                            </c:if>
                                                        </div>
                                                        <div id="carrierLiveStatusText"
                                                            style="font-size: 0.82rem; color: #15803d; margin-top: 0.35rem; font-weight: 600;">
                                                            📍 Real-time updates verified with
                                                            <c:out value="${order.courierPartner}" /> logistics network
                                                        </div>
                                                    </div>
                                                </div>

                                                <div
                                                    style="display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap;">
                                                    <c:if test="${not empty order.trackingNumber}">
                                                        <button type="button" class="copy-order-btn"
                                                            data-copy-val="${order.trackingNumber}"
                                                            style="padding: 0.5rem 0.85rem; font-size: 0.82rem; background: #ffffff; border: 1px solid #86efac; color: #166534; border-radius: 6px; font-weight: 700; cursor: pointer; display: inline-flex; align-items: center; gap: 0.3rem;">
                                                            📋 Copy AWB
                                                        </button>
                                                    </c:if>
                                                    <button type="button" id="syncLiveTrackingBtn"
                                                        data-action="syncLiveTracking" data-order-id="${order.orderId}" data-auto-sync="${not empty order.trackingNumber && order.orderStatus != 'DELIVERED'}"
                                                        style="padding: 0.5rem 1.1rem; font-size: 0.82rem; background: #166534; color: #ffffff; border: none; border-radius: 6px; font-weight: 800; cursor: pointer; display: inline-flex; align-items: center; gap: 0.4rem; box-shadow: 0 2px 6px rgba(22, 101, 52, 0.25);">
                                                        <span id="syncIcon">🔄</span> Sync Live Carrier Telemetry
                                                    </button>
                                                </div>
                                            </div>

                                            <!-- Live Scans Container (Loaded dynamically or via Sync) -->
                                            <div id="liveScansContainer" style="display: none;"></div>
                                        </div>
                                    </c:if>

                                    <!-- Consolidated In-Line Details Hero Card: Shipping, Billing, Payment & Summary -->
                                    <div class="order-details-hero-card">

                                        <div class="order-details-hero-header">
                                            <div
                                                style="font-size: 0.95rem; font-weight: 800; display: flex; align-items: center; gap: 0.6rem;">
                                                <span>📑</span> Order, Address & Financial Breakdown
                                            </div>
                                            <div
                                                style="display: flex; align-items: center; gap: 0.75rem; font-size: 0.82rem;">
                                                <span
                                                    style="background: rgba(255, 255, 255, 0.15); padding: 0.25rem 0.65rem; border-radius: 6px; font-weight: 700; font-family: monospace;">
                                                    Order #
                                                    <c:out value="${order.orderNumber}" />
                                                </span>
                                                <span style="color: #cbd5e1; font-weight: 500;">
                                                    Placed on: <c:choose>
                                                        <c:when test="${not empty order.formattedCreatedAt}">
                                                            ${order.formattedCreatedAt}</c:when>
                                                        <c:otherwise>${order.createdAt}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                        </div>

                                        <div class="order-details-hero-grid">

                                            <!-- 1. Shipping Address -->
                                            <div class="order-details-col">
                                                <div class="order-col-title">
                                                    <span>📍</span> SHIPPING ADDRESS
                                                </div>
                                                <div
                                                    style="font-weight: 800; color: #0f172a; font-size: 0.92rem; margin-bottom: 0.25rem;">
                                                    <c:out value="${order.shippingFullName}" />
                                                </div>
                                                <div
                                                    style="color: #475569; font-size: 0.83rem; line-height: 1.55; flex: 1;">
                                                    <c:out value="${order.shippingAddressLine1}" />
                                                    <c:if test="${not empty order.shippingAddressLine2}">,
                                                        <c:out value="${order.shippingAddressLine2}" />
                                                    </c:if><br>
                                                    <c:out value="${order.shippingCity}" />,
                                                    <c:out value="${order.shippingState}" /> - <strong>
                                                        <c:out value="${order.shippingPostalCode}" />
                                                    </strong><br>
                                                    <span style="color: #64748b;">Phone:</span> <strong>
                                                        <c:out value="${order.shippingPhone}" />
                                                    </strong>
                                                </div>
                                            </div>

                                            <!-- 2. Billing Address -->
                                            <div class="order-details-col">
                                                <div class="order-col-title">
                                                    <span>🏢</span> BILLING ADDRESS
                                                </div>
                                                <c:choose>
                                                    <c:when test="${not empty order.billingAddressSnapshot}">
                                                        <div
                                                            style="color: #475569; font-size: 0.83rem; line-height: 1.55; flex: 1;">
                                                            <c:out value="${order.billingAddressSnapshot}" />
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div
                                                            style="font-weight: 800; color: #0f172a; font-size: 0.92rem; margin-bottom: 0.25rem;">
                                                            <c:out value="${order.shippingFullName}" />
                                                        </div>
                                                        <div
                                                            style="color: #475569; font-size: 0.83rem; line-height: 1.55; flex: 1;">
                                                            <c:out value="${order.shippingAddressLine1}" />
                                                            <c:if test="${not empty order.shippingAddressLine2}">,
                                                                <c:out value="${order.shippingAddressLine2}" />
                                                            </c:if><br>
                                                            <c:out value="${order.shippingCity}" />,
                                                            <c:out value="${order.shippingState}" /> - <strong>
                                                                <c:out value="${order.shippingPostalCode}" />
                                                            </strong><br>
                                                            <span
                                                                style="display: inline-block; margin-top: 0.4rem; font-size: 0.72rem; background: #e0f2fe; color: #0369a1; padding: 0.15rem 0.5rem; border-radius: 4px; font-weight: 700;">
                                                                ✓ Same as Shipping Address
                                                            </span>
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <!-- 3. Payment Information -->
                                            <div class="order-details-col">
                                                <div class="order-col-title">
                                                    <span>💳</span> PAYMENT INFO
                                                </div>
                                                <div
                                                    style="color: #475569; font-size: 0.83rem; line-height: 1.6; flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
                                                    <div>
                                                        <div style="margin-bottom: 0.35rem;">
                                                            Payment Method: <strong style="color: #0f172a;">
                                                                <c:choose>
                                                                    <c:when test="${order.paymentMethod == 'COD'}">COD
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <c:out value="${order.paymentMethod}" />
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </strong>
                                                        </div>
                                                        <div
                                                            style="margin-bottom: 0.4rem; display: flex; align-items: center; gap: 0.4rem;">
                                                            <span>Payment Status:</span>
                                                            <c:choose>
                                                                <c:when test="${order.paymentStatus == 'PAID'}">
                                                                    <strong style="color: #10b981;">Paid</strong>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <strong style="color: #dc2626;">Unpaid</strong>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                        <c:if test="${order.paymentStatus != 'PAID'}">
                                                            <div style="margin-top: 0.45rem; margin-bottom: 0.45rem;">
                                                                <form action="${pageContext.request.contextPath}/payment/gateway" method="POST" style="margin: 0; display: inline;">
                                                                    <input type="hidden" name="orderId" value="${order.orderId}">
                                                                    <button type="submit" class="btn-cod-paynow" style="border: none; cursor: pointer;" title="Complete online payment now via UPI, Cards, Net Banking">
                                                                        ⚡ Pay Now ➔
                                                                    </button>
                                                                </form>
                                                            </div>
                                                        </c:if>
                                                    </div>
                                                    <div
                                                        style="font-size: 0.76rem; color: #64748b; margin-top: 0.4rem;">
                                                        Security: <strong style="color: #10b981;">🛡️ 256-Bit
                                                            SSL</strong>
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- 4. Order Summary -->
                                            <div class="order-details-col">
                                                <div class="order-col-title">
                                                    <span>🧾</span> ORDER SUMMARY
                                                </div>
                                                <div
                                                    style="font-size: 0.83rem; color: #475569; line-height: 1.65; flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
                                                    <div>
                                                        <div style="display: flex; justify-content: space-between;">
                                                            <span>Items Subtotal:</span>
                                                            <span style="font-weight: 600; color: #1e293b;">₹
                                                                <fmt:formatNumber value="${order.subtotal}"
                                                                    minFractionDigits="2" />
                                                            </span>
                                                        </div>
                                                        <c:if test="${order.discountAmount > 0}">
                                                            <div
                                                                style="display: flex; justify-content: space-between; color: #10b981; font-weight: 700;">
                                                                <span>Applied Discount:</span>
                                                                <span>-₹
                                                                    <fmt:formatNumber value="${order.discountAmount}"
                                                                        minFractionDigits="2" />
                                                                </span>
                                                            </div>
                                                        </c:if>
                                                        <div style="display: flex; justify-content: space-between;">
                                                            <span>Shipping:</span>
                                                            <span>
                                                                <c:choose>
                                                                    <c:when test="${order.shippingAmount == 0}"><strong
                                                                            style="color: #10b981;">FREE</strong>
                                                                    </c:when>
                                                                    <c:otherwise>₹
                                                                        <fmt:formatNumber
                                                                            value="${order.shippingAmount}"
                                                                            minFractionDigits="2" />
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                        </div>
                                                        <div style="display: flex; justify-content: space-between;">
                                                            <span>GST (Included):</span>
                                                            <span>₹
                                                                <fmt:formatNumber value="${order.taxAmount}"
                                                                    minFractionDigits="2" />
                                                            </span>
                                                        </div>
                                                    </div>
                                                    <div
                                                        style="display: flex; justify-content: space-between; align-items: center; font-weight: 900; color: #0f172a; font-size: 0.92rem; border-top: 2px solid #e2e8f0; padding-top: 0.45rem; margin-top: 0.45rem; flex-wrap: nowrap;">
                                                        <span>
                                                            <c:choose>
                                                                <c:when test="${order.paymentStatus == 'PAID'}">
                                                                    Total Paid:
                                                                </c:when>
                                                                <c:when test="${order.paymentMethod == 'COD'}">
                                                                    Total Amount To Paid:
                                                                </c:when>
                                                                <c:otherwise>
                                                                    Total Paid:
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </span>
                                                        <span
                                                            style="color: #2563eb; font-size: 1.05rem; white-space: nowrap; margin-left: 0.4rem;">₹
                                                            <fmt:formatNumber value="${order.totalAmount}"
                                                                minFractionDigits="2" />
                                                        </span>
                                                    </div>
                                                </div>
                                            </div>

                                        </div>
                                    </div>

                                    <!-- 4. Detailed Milestone Activity Log -->
                                    <c:if test="${not empty order.statusHistory}">
                                        <div class="activity-feed-wrapper"
                                            style="margin-top: 2rem; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.5rem;">
                                            <h3 class="activity-feed-heading"
                                                style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin: 0 0 1.25rem 0; display: flex; align-items: center; gap: 0.5rem;">
                                                <span>📜</span> Milestone Updates & Activity Log
                                            </h3>
                                            <div class="activity-feed-list"
                                                style="position: relative; padding-left: 2rem;">
                                                <c:forEach var="h" items="${order.statusHistory}" varStatus="loop">
                                                    <div class="activity-feed-item ${loop.first ? 'latest' : ''}"
                                                        style="position: relative; margin-bottom: 1.35rem;">
                                                        <div class="activity-feed-dot"
                                                            style="position: absolute; left: -2rem; top: 2px; width: 18px; height: 18px; border-radius: 50%; background: ${loop.first ? '#3b82f6' : '#ffffff'}; border: 3px solid ${loop.first ? '#93c5fd' : '#3b82f6'}; z-index: 2;">
                                                        </div>
                                                        <div>
                                                            <span
                                                                class="activity-feed-status-badge ${h.newStatus == 'DELIVERED' ? 'badge-success' : (h.newStatus == 'CANCELLED' ? 'badge-danger' : 'badge-info')}"
                                                                style="display: inline-block; font-size: 0.72rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.04em; padding: 0.2rem 0.55rem; border-radius: 4px; margin-bottom: 0.25rem; background: ${h.newStatus == 'DELIVERED' ? '#10b981' : (h.newStatus == 'CANCELLED' ? '#ef4444' : '#3b82f6')}; color: #ffffff;">
                                                                <c:out value="${h.newStatus}" />
                                                            </span>
                                                            <div class="activity-feed-remarks"
                                                                style="font-size: 0.88rem; color: #334155; font-weight: 600; line-height: 1.45;">
                                                                <c:out value="${h.remarks}" />
                                                            </div>
                                                            <div class="activity-feed-timestamp"
                                                                style="font-size: 0.75rem; color: #94a3b8; margin-top: 0.15rem;">
                                                                Recorded on <c:choose>
                                                                    <c:when test="${not empty h.formattedCreatedAt}">
                                                                        ${h.formattedCreatedAt}</c:when>
                                                                    <c:otherwise>${h.createdAt}</c:otherwise>
                                                                </c:choose> &bull; Verified by Fulfillment Logistics
                                                            </div>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <!-- 3. Ordered Items Card -->
                    <div class="order-card-wrapper" style="margin-bottom: 2rem;">
                        <div class="order-card-top"
                            style="display: flex; justify-content: space-between; align-items: center;">
                            <h3 style="font-size: 1.1rem; font-weight: 800; margin: 0; color: var(--text-primary);">
                                Item(s)
                            </h3>
                            <span style="font-size: 0.82rem; color: var(--text-muted); font-weight: 600;">
                                All items verified for standard dispatch
                            </span>
                        </div>

                        <div class="order-card-content" style="padding: 0 !important;">
                            <div style="display: flex; flex-direction: column;">
                                <c:forEach var="item" items="${order.items}" varStatus="itemStatus">
                                    <div
                                        style="display: flex; align-items: center; justify-content: space-between; padding: 1.25rem 1.75rem; ${!itemStatus.last ? 'border-bottom: 1px solid #f1f5f9;' : ''} transition: background 0.2s ease; flex-wrap: wrap; gap: 1rem;">

                                        <div
                                            style="display: flex; align-items: center; gap: 1.25rem; flex: 1; min-width: 260px;">
                                            <div
                                                style="width: 52px; height: 52px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.6rem; background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%); border: 1px solid #e2e8f0; flex-shrink: 0;">
                                                📦
                                            </div>
                                            <div style="min-width: 0;">
                                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}"
                                                    style="font-size: 1.02rem; font-weight: 800; color: #0f172a; text-decoration: none; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.35; margin-bottom: 0.35rem;">
                                                    <c:out value="${item.productName}" />
                                                </a>
                                                <div
                                                    style="display: flex; align-items: center; gap: 0.6rem; font-size: 0.82rem; color: var(--text-muted); flex-wrap: wrap;">
                                                    <span
                                                        style="background: #f1f5f9; color: #475569; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 700; font-family: monospace; font-size: 0.75rem;">
                                                        SKU:
                                                        <c:out value="${item.sku}" />
                                                    </span>
                                                    <span>&bull;</span>
                                                    <span>Unit Price: <strong>₹
                                                            <fmt:formatNumber value="${item.unitPrice}"
                                                                minFractionDigits="2" />
                                                        </strong></span>
                                                    <span>&bull;</span>
                                                    <span>Qty: <strong
                                                            style="color: #0f172a;">${item.quantity}</strong></span>
                                                </div>
                                            </div>
                                        </div>

                                        <div
                                            style="text-align: right; min-width: 150px; display: flex; flex-direction: column; align-items: flex-end; justify-content: center;">
                                            <div
                                                style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase; font-weight: 700; letter-spacing: 0.04em;">
                                                Price</div>
                                            <div
                                                style="font-size: 1.15rem; font-weight: 900; color: #0f172a; margin-top: 0.1rem;">
                                                ₹
                                                <fmt:formatNumber value="${item.lineTotal}" minFractionDigits="2" />
                                            </div>
                                            <c:if test="${order.orderStatus == 'DELIVERED'}">
                                                <c:set var="userRev" value="${userReviews[item.productId]}" />
                                                <c:choose>
                                                    <c:when test="${not empty userRev}">
                                                        <button type="button" class="btn-write-review"
                                                            onclick="openProductReviewModal(${item.productId}, '<c:out value="
                                                            ${item.productName}" />', '
                                                        <c:out value="${item.sku}" />', ${userRev.rating}, '
                                                        <c:out value="${userRev.title}" />', '
                                                        <c:out value="${userRev.comment}" />', '${userRev.imageUrl}')"
                                                        style="margin-top: 0.6rem; background: linear-gradient(135deg,
                                                        #2563eb 0%, #1d4ed8 100%); color: #ffffff; border: none;
                                                        padding: 0.4rem 0.85rem; border-radius: 6px; font-size: 0.8rem;
                                                        font-weight: 800; cursor: pointer; display: inline-flex;
                                                        align-items: center; gap: 0.35rem; box-shadow: 0 2px 5px
                                                        rgba(37, 99, 235, 0.25); transition: all 0.2s ease;">
                                                        <span>✏️</span> Edit Review (★ ${userRev.rating})
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button type="button" class="btn-write-review"
                                                            onclick="openProductReviewModal(${item.productId}, '<c:out value="
                                                            ${item.productName}" />', '
                                                        <c:out value="${item.sku}" />', 5, '', '', '')"
                                                        style="margin-top: 0.6rem; background: linear-gradient(135deg,
                                                        #f59e0b 0%, #d97706 100%); color: #ffffff; border: none;
                                                        padding: 0.4rem 0.85rem; border-radius: 6px; font-size: 0.8rem;
                                                        font-weight: 800; cursor: pointer; display: inline-flex;
                                                        align-items: center; gap: 0.35rem; box-shadow: 0 2px 5px
                                                        rgba(217, 119, 6, 0.25); transition: all 0.2s ease;">
                                                        <span>⭐</span> Write Review
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:if>
                                        </div>

                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>

                    <!-- 4. Order Actions Bottom Bar -->
                    <div
                        style="display: flex; justify-content: space-between; align-items: center; margin-top: 1.5rem; flex-wrap: wrap; gap: 1rem;">
                        <a href="${pageContext.request.contextPath}/orders"
                            style="color: var(--primary); font-weight: 700; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem;">
                            &larr; Back to Order History
                        </a>

                        <c:if test="${order.orderStatus == 'PENDING' || order.orderStatus == 'CONFIRMED'}">
                            <form action="${pageContext.request.contextPath}/order/cancel" method="POST"
                                onsubmit="return confirm('Are you sure you want to cancel Order #${order.orderNumber}? This cannot be undone.');">
                                <input type="hidden" name="orderId" value="${order.orderNumber}">
                                <button type="submit"
                                    style="background: #fee2e2; color: #b91c1c; border: 1px solid #f87171; padding: 0.6rem 1.25rem; border-radius: var(--radius-md); font-weight: 700; cursor: pointer; transition: var(--transition-fast);">
                                    ✕ Cancel This Order
                                </button>
                            </form>
                        </c:if>
                    </div>

                </main>

                <!-- FOOTER -->
                <footer class="main-footer"
                    style="background: #0f172a; color: #cbd5e1; padding: 3rem 1.5rem 1.5rem; margin-top: 4rem;">
                    <div class="footer-grid"
                        style="max-width: 1440px; margin: 0 auto; display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 2rem; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 2rem;">
                        <div>
                            <h4 style="color: #ffffff; margin-bottom: 1rem;">ShopKart India</h4>
                            <p style="font-size: 0.85rem; line-height: 1.6; color: #94a3b8;">
                                India's leading retail & wholesale e-commerce destination with doorstep fast delivery
                                and verified OEM manufacturing sources.
                            </p>
                        </div>
                        <div>
                            <h4 style="color: #ffffff; margin-bottom: 1rem;">Customer Care</h4>
                            <ul style="list-style: none; padding: 0; font-size: 0.85rem; line-height: 2;">
                                <li><a href="${pageContext.request.contextPath}/orders"
                                        style="color: #94a3b8; text-decoration: none;">Track Orders</a></li>
                                <li><a href="${pageContext.request.contextPath}/addresses"
                                        style="color: #94a3b8; text-decoration: none;">Saved Addresses</a></li>
                                <c:if test="${sessionScope.currentUser.admin}">
                                    <li><a href="${pageContext.request.contextPath}/health"
                                            style="color: #38bdf8; text-decoration: none;">🩺 System Status</a></li>
                                </c:if>
                            </ul>
                        </div>
                        <div>
                            <h4 style="color: #ffffff; margin-bottom: 1rem;">Shop Departments</h4>
                            <ul style="list-style: none; padding: 0; font-size: 0.85rem; line-height: 2;">
                                <li><a href="${pageContext.request.contextPath}/products?category=1"
                                        style="color: #94a3b8; text-decoration: none;">Laptops & Ultrabooks</a></li>
                                <li><a href="${pageContext.request.contextPath}/products?category=2"
                                        style="color: #94a3b8; text-decoration: none;">Smartphones & 5G</a></li>
                                <li><a href="${pageContext.request.contextPath}/products?category=3"
                                        style="color: #94a3b8; text-decoration: none;">Headphones & Audio</a></li>
                            </ul>
                        </div>
                    </div>
                    <div
                        style="text-align: center; font-size: 0.82rem; color: #94a3b8; margin-top: 1.5rem; display: flex; flex-direction: column; align-items: center; gap: 0.5rem;">
                        <div>&copy; 2026 ShopKart Inc. All rights reserved. &bull; Trade Assurance &bull; 100% Purchase
                            Protection</div>
                        <div
                            style="padding-top: 0.5rem; border-top: 1px solid rgba(255,255,255,0.08); width: 100%; max-width: 500px; color: #cbd5e1; font-size: 0.85rem;">
                            Designed, Developed &amp; Managed by <strong
                                style="color: #38bdf8; font-weight: 800; letter-spacing: 0.02em;">Rahul Pawar</strong>
                        </div>
                    </div>
                </footer>
                <!-- ==============================================================================
         PAYMENT SUCCESSFUL MODAL POPUP (POST-PLACEMENT COD / ONLINE PAYMENT SUCCESS)
         ============================================================================== -->
                <c:if test="${param.paymentSuccess eq 'true'}">
                    <style>
                        .pay-modal-backdrop {
                            position: fixed;
                            top: 0;
                            left: 0;
                            right: 0;
                            bottom: 0;
                            background: rgba(15, 23, 42, 0.75);
                            backdrop-filter: blur(8px);
                            -webkit-backdrop-filter: blur(8px);
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            z-index: 99999;
                            padding: 1.25rem;
                            opacity: 1;
                            animation: payModalFadeIn 0.3s ease forwards;
                        }

                        @keyframes payModalFadeIn {
                            from {
                                opacity: 0;
                            }

                            to {
                                opacity: 1;
                            }
                        }

                        .pay-modal-card {
                            background: #ffffff;
                            border-radius: 24px;
                            max-width: 520px;
                            width: 100%;
                            padding: 2.25rem 2rem 2rem;
                            box-shadow: 0 25px 60px -15px rgba(0, 0, 0, 0.35), 0 0 0 1px rgba(255, 255, 255, 0.1);
                            position: relative;
                            text-align: center;
                            overflow: hidden;
                            animation: payModalSlideUp 0.35s cubic-bezier(0.16, 1, 0.3, 1) forwards;
                        }

                        @keyframes payModalSlideUp {
                            from {
                                opacity: 0;
                                transform: scale(0.92) translateY(20px);
                            }

                            to {
                                opacity: 1;
                                transform: scale(1) translateY(0);
                            }
                        }

                        .pay-modal-close-btn {
                            position: absolute;
                            top: 16px;
                            right: 16px;
                            width: 36px;
                            height: 36px;
                            border-radius: 50%;
                            background: #f1f5f9;
                            border: none;
                            font-size: 1.1rem;
                            color: #64748b;
                            cursor: pointer;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            transition: all 0.2s ease;
                            z-index: 10;
                        }

                        .pay-modal-close-btn:hover {
                            background: #e2e8f0;
                            color: #0f172a;
                            transform: rotate(90deg);
                        }

                        .pay-success-badge-icon {
                            width: 72px;
                            height: 72px;
                            border-radius: 50%;
                            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                            color: #ffffff;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 2.5rem;
                            margin: 0 auto 1.25rem;
                            box-shadow: 0 10px 25px -5px rgba(16, 185, 129, 0.4);
                            animation: checkmarkPulse 2s infinite ease-in-out;
                        }

                        @keyframes checkmarkPulse {

                            0%,
                            100% {
                                transform: scale(1);
                                box-shadow: 0 10px 25px -5px rgba(16, 185, 129, 0.4);
                            }

                            50% {
                                transform: scale(1.06);
                                box-shadow: 0 15px 30px -3px rgba(16, 185, 129, 0.55);
                            }
                        }

                        .pay-modal-title {
                            font-size: 1.75rem;
                            font-weight: 900;
                            color: #0f172a;
                            margin: 0 0 0.4rem;
                            letter-spacing: -0.02em;
                        }

                        .pay-modal-subtitle {
                            font-size: 0.95rem;
                            color: #64748b;
                            line-height: 1.5;
                            margin: 0 auto 1.25rem;
                            max-width: 420px;
                        }

                        .pay-txn-pill {
                            display: inline-flex;
                            align-items: center;
                            gap: 0.6rem;
                            background: #f8fafc;
                            border: 1.5px dashed #cbd5e1;
                            padding: 0.45rem 1rem;
                            border-radius: 9999px;
                            font-size: 0.85rem;
                            font-family: monospace;
                            color: #1e293b;
                            margin-bottom: 1.4rem;
                        }

                        .pay-modal-details-grid {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 0.75rem;
                            margin-bottom: 1.25rem;
                            text-align: left;
                        }

                        @media (max-width: 480px) {
                            .pay-modal-details-grid {
                                grid-template-columns: 1fr;
                            }
                        }

                        .pay-detail-tile {
                            background: #f8fafc;
                            border: 1px solid #e2e8f0;
                            border-radius: 12px;
                            padding: 0.75rem 0.9rem;
                            display: flex;
                            align-items: center;
                            gap: 0.65rem;
                        }

                        .pay-detail-tile .tile-icon {
                            font-size: 1.3rem;
                            flex-shrink: 0;
                        }

                        .pay-detail-tile .tile-content {
                            display: flex;
                            flex-direction: column;
                            min-width: 0;
                        }

                        .pay-detail-tile .tile-label {
                            font-size: 0.7rem;
                            color: #64748b;
                            font-weight: 700;
                            text-transform: uppercase;
                            letter-spacing: 0.03em;
                        }

                        .pay-detail-tile .tile-value {
                            font-size: 0.92rem;
                            color: #0f172a;
                            font-weight: 800;
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                        }

                        .pay-receipt-notice-box {
                            background: #f0fdf4;
                            border: 1.5px solid #86efac;
                            border-radius: 12px;
                            padding: 0.9rem 1.1rem;
                            margin-bottom: 1.5rem;
                            text-align: left;
                            display: flex;
                            gap: 0.75rem;
                            align-items: flex-start;
                        }

                        .pay-modal-actions {
                            display: flex;
                            gap: 0.75rem;
                            justify-content: center;
                            flex-wrap: wrap;
                        }

                        .pay-modal-actions .btn-pay-close {
                            background: #0f172a;
                            color: #ffffff;
                            border: none;
                            padding: 0.85rem 1.75rem;
                            border-radius: 12px;
                            font-weight: 800;
                            font-size: 0.95rem;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            gap: 0.5rem;
                            cursor: pointer;
                            flex: 1.2;
                            min-width: 140px;
                            box-shadow: 0 4px 14px rgba(15, 23, 42, 0.25);
                            transition: all 0.2s ease;
                        }

                        .pay-modal-actions .btn-pay-close:hover {
                            background: #1e293b;
                            transform: translateY(-2px);
                            box-shadow: 0 6px 20px rgba(15, 23, 42, 0.35);
                        }

                        .pay-modal-actions .btn-pay-receipt {
                            background: #f8fafc;
                            color: #0f172a;
                            border: 1.5px solid #cbd5e1;
                            padding: 0.85rem 1.25rem;
                            border-radius: 12px;
                            font-weight: 800;
                            font-size: 0.95rem;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            gap: 0.5rem;
                            cursor: pointer;
                            flex: 1;
                            min-width: 140px;
                            transition: all 0.2s ease;
                        }

                        .pay-modal-actions .btn-pay-receipt:hover {
                            background: #e2e8f0;
                            border-color: #94a3b8;
                            transform: translateY(-2px);
                        }
                    </style>

                    <div class="pay-modal-backdrop" id="paymentSuccessModal"
                        onclick="handlePaymentModalBackdrop(event)">
                        <div class="pay-modal-card" role="dialog" aria-labelledby="paymentSuccessTitle"
                            aria-modal="true">
                            <!-- Floating Confetti Decor -->
                            <div class="confetti-bubble c1"
                                style="position: absolute; top: -10px; left: 8%; font-size: 1.5rem; animation: confettiFloat 3s infinite;">
                                🎉</div>
                            <div class="confetti-bubble c2"
                                style="position: absolute; top: 18px; right: 8%; font-size: 1.5rem; animation: confettiFloat 3s infinite 0.7s;">
                                ✨</div>
                            <div class="confetti-bubble c3"
                                style="position: absolute; bottom: 15px; left: 6%; font-size: 1.5rem; animation: confettiFloat 3s infinite 1.4s;">
                                💳</div>
                            <div class="confetti-bubble c4"
                                style="position: absolute; bottom: -8px; right: 10%; font-size: 1.5rem; animation: confettiFloat 3s infinite 2.1s;">
                                ⭐</div>

                            <!-- Close Button (Top Corner) -->
                            <button type="button" class="pay-modal-close-btn" onclick="closePaymentSuccessModal()"
                                aria-label="Close modal">✕</button>

                            <!-- Hero Section -->
                            <div class="pay-success-badge-icon">
                                <span>✓</span>
                            </div>
                            <h2 class="pay-modal-title" id="paymentSuccessTitle">Payment Successful!</h2>
                            <p class="pay-modal-subtitle">
                                Thank you, <strong>${sessionScope.currentUser.fullName}</strong>! Your payment has been
                                received and verified successfully.
                            </p>

                            <!-- Transaction Reference Pill -->
                            <div class="pay-txn-pill">
                                <span>Txn: <strong>
                                        <c:choose>
                                            <c:when test="${not empty param.txnRef}">
                                                <c:out value="${param.txnRef}" />
                                            </c:when>
                                            <c:otherwise>TXN-${order.orderId}-PAID</c:otherwise>
                                        </c:choose>
                                    </strong></span>
                                <button type="button" class="copy-order-btn"
                                    data-copy-val="<c:out value='${not empty param.txnRef ? param.txnRef : order.orderNumber}' />"
                                    title="Copy Transaction ID">📋 Copy</button>
                            </div>

                            <!-- Transaction Details Grid -->
                            <div class="pay-modal-details-grid">
                                <div class="pay-detail-tile">
                                    <div class="tile-icon">💰</div>
                                    <div class="tile-content">
                                        <span class="tile-label">Amount Paid</span>
                                        <span class="tile-value" style="color: #059669; font-size: 1.05rem;">
                                            ₹
                                            <fmt:formatNumber value="${order.totalAmount}" pattern="#,##0.00" />
                                        </span>
                                    </div>
                                </div>

                                <div class="pay-detail-tile">
                                    <div class="tile-icon">💳</div>
                                    <div class="tile-content">
                                        <span class="tile-label">Payment Status</span>
                                        <span class="tile-value">
                                            <span class="badge-status badge-success"
                                                style="font-size: 0.72rem; padding: 0.2rem 0.5rem; background: #10b981; color: #ffffff; border-radius: 9999px; font-weight: 800;">
                                                PAID ✓
                                            </span>
                                        </span>
                                    </div>
                                </div>

                                <div class="pay-detail-tile">
                                    <div class="tile-icon">📦</div>
                                    <div class="tile-content">
                                        <span class="tile-label">Order Number</span>
                                        <span class="tile-value"
                                            title="#${order.orderNumber}">#${order.orderNumber}</span>
                                    </div>
                                </div>

                                <div class="pay-detail-tile">
                                    <div class="tile-icon">🚚</div>
                                    <div class="tile-content">
                                        <span class="tile-label">Order Status</span>
                                        <span class="tile-value" style="color: #0284c7; font-weight: 800;">
                                            <c:choose>
                                                <c:when test="${order.orderStatus == 'DELIVERED'}">Delivered</c:when>
                                                <c:when test="${order.orderStatus == 'OUT_FOR_DELIVERY'}">Out for
                                                    Delivery</c:when>
                                                <c:when
                                                    test="${order.orderStatus == 'DISPATCHED' || order.orderStatus == 'IN_TRANSIT'}">
                                                    In Transit</c:when>
                                                <c:otherwise>In Processing</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                </div>
                            </div>

                            <!-- Receipt Download Info Message Box -->
                            <div class="pay-receipt-notice-box">
                                <span style="font-size: 1.4rem; line-height: 1; flex-shrink: 0;">🧾</span>
                                <div style="font-size: 0.85rem; color: #166534; line-height: 1.5;">
                                    <strong
                                        style="display: block; margin-bottom: 0.15rem; color: #14532d; font-size: 0.88rem;">Official
                                        Payment Receipt Available</strong>
                                    If you want to download your official payment receipt &amp; tax invoice, you can
                                    download or print it directly from this <strong>Order Tracking</strong> page using
                                    the <strong>🖨️ Download / Print Invoice</strong> button.
                                </div>
                            </div>

                            <!-- Action Buttons -->
                            <div class="pay-modal-actions">
                                <button type="button" class="btn-pay-close" onclick="closePaymentSuccessModal()">
                                    <span>✓</span>
                                    <span>Close</span>
                                </button>
                                <button type="button" class="btn-pay-receipt"
                                    onclick="closePaymentSuccessModal(); window.print();">
                                    <span>🖨️</span>
                                    <span>Download Receipt</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </c:if>

                </main>

                <!-- Toast Notification Container -->
                <div id="toastContainer" class="toast-container"></div>

                <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
                <script nonce="${cspNonce}">
                    async function syncLiveCarrierTracking(orderId) {
                        const btn = document.getElementById('syncLiveTrackingBtn');
                        const icon = document.getElementById('syncIcon');
                        const statusText = document.getElementById('carrierLiveStatusText');
                        const scansContainer = document.getElementById('liveScansContainer');

                        if (btn) {
                            btn.disabled = true;
                            btn.style.opacity = '0.75';
                        }
                        if (icon) {
                            icon.innerText = '⏳';
                        }

                        try {
                            const response = await fetch('${pageContext.request.contextPath}/order/live-tracking?orderId=' + orderId);
                            const data = await response.json();

                            if (data.success) {
                                showToast('📡 Real-time telemetry synchronized with ' + (data.courierName || 'Carrier Network') + '!', 'success');

                                if (statusText && data.remarks) {
                                    statusText.innerHTML = '📍 <strong>' + data.remarks + '</strong>' + (data.currentLocation ? ' &bull; ' + data.currentLocation : '');
                                }

                                if (scansContainer && data.scans && data.scans.length > 0) {
                                    let html = '<div class="carrier-scan-feed" style="margin-top: 1.25rem; border-top: 1px dashed #86efac; padding-top: 1.25rem;">';
                                    html += '<div style="font-size: 0.92rem; font-weight: 800; color: #14532d; margin-bottom: 0.85rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">';
                                    html += '  <div style="display: flex; align-items: center; gap: 0.5rem;"><span>📡</span> Live Checkpoint Scans from ' + (data.courierName || 'Logistics Network') + '</div>';
                                    if (data.carrierTrackingUrl && data.carrierTrackingUrl !== '#') {
                                        html += '  <a href="' + data.carrierTrackingUrl + '" target="_blank" rel="noopener noreferrer" style="font-size: 0.78rem; color: #166534; font-weight: 800; text-decoration: underline;">🔗 View on ' + (data.courierName || 'Carrier') + ' Portal &rarr;</a>';
                                    }
                                    html += '</div>';
                                    html += '<div class="scan-timeline-list" style="display: flex; flex-direction: column; gap: 0.6rem;">';

                                    data.scans.forEach((scan, idx) => {
                                        const isLatest = (idx === data.scans.length - 1);
                                        html += '<div style="background: #ffffff; border: 1.5px solid ' + (isLatest ? '#22c55e' : '#e2e8f0') + '; border-radius: 8px; padding: 0.75rem 1rem; display: flex; justify-content: space-between; align-items: center; gap: 0.75rem; flex-wrap: wrap; box-shadow: ' + (isLatest ? '0 2px 8px rgba(34, 197, 94, 0.15)' : 'none') + ';">';
                                        html += '  <div style="display: flex; align-items: center; gap: 0.75rem;">';
                                        html += '    <span style="font-size: 1.1rem;">' + (isLatest ? '🟢' : '⚪') + '</span>';
                                        html += '    <div>';
                                        html += '      <div style="font-size: 0.88rem; font-weight: 800; color: #0f172a;">' + scan.activity + (isLatest ? ' <span style="font-size: 0.68rem; background: #dcfce7; color: #15803d; padding: 0.1rem 0.4rem; border-radius: 4px; font-weight: 800; margin-left: 0.35rem;">LATEST SCAN</span>' : '') + '</div>';
                                        html += '      <div style="font-size: 0.78rem; color: #64748b; margin-top: 0.1rem;">📍 Facility: <strong>' + scan.location + '</strong></div>';
                                        html += '    </div>';
                                        html += '  </div>';
                                        html += '  <div style="font-size: 0.78rem; color: #475569; font-family: monospace; font-weight: 700; background: #f8fafc; padding: 0.25rem 0.6rem; border-radius: 6px; border: 1px solid #e2e8f0;">' + scan.time + '</div>';
                                        html += '</div>';
                                    });

                                    html += '</div></div>';
                                    scansContainer.innerHTML = html;
                                    scansContainer.style.display = 'block';
                                }
                            } else {
                                showToast('Live tracking status: ' + (data.message || 'Carrier server busy'), 'warning');
                            }
                        } catch (e) {
                            showToast('Error syncing carrier telemetry: ' + e.message, 'danger');
                        } finally {
                            if (btn) {
                                btn.disabled = false;
                                btn.style.opacity = '1';
                            }
                            if (icon) {
                                icon.innerText = '🔄';
                            }
                        }
                    }

                    // Auto-load live carrier telemetry if tracking number exists (only for in-transit orders)
                    document.addEventListener('DOMContentLoaded', function () {
                        const syncBtn = document.getElementById('syncLiveTrackingBtn');
                        if (syncBtn && syncBtn.getAttribute('data-auto-sync') === 'true') {
                            const orderId = syncBtn.getAttribute('data-order-id');
                            if (orderId) {
                                syncLiveCarrierTracking(orderId);
                            }
                        }

                        const urlParams = new URLSearchParams(window.location.search);
                        if (urlParams.get('reviewSubmitted') === 'true') {
                            showToast('🎉 Thank you! Your product review & photos have been submitted successfully.', 'success');
                        }
                        if (urlParams.has('reviewError')) {
                            showToast('Review Error: ' + urlParams.get('reviewError'), 'danger');
                        }
                        if (urlParams.get('returnSubmitted') === 'true') {
                            const returnRef = urlParams.get('returnNum');
                            showToast('✓ Return request' + (returnRef ? ' (Ref: ' + returnRef + ')' : '') + ' submitted successfully!', 'success');
                        }
                        if (urlParams.has('returnError')) {
                            showToast('Return Error: ' + urlParams.get('returnError'), 'danger');
                        }
                    });

                    // CSP-compliant delegation for Sync Live Tracking button
                    document.addEventListener('click', function(e) {
                        const btn = e.target.closest('[data-action="syncLiveTracking"]');
                        if (btn) {
                            const orderId = btn.getAttribute('data-order-id');
                            if (orderId) syncLiveCarrierTracking(orderId);
                        }
                    });

                    // Return Modal Logic
                    function openOrderReturnModal() {
                        clearReturnImagePreview();
                        const modal = document.getElementById('orderReturnModalBackdrop');
                        if (modal) {
                            modal.style.display = 'flex';
                            document.body.style.overflow = 'hidden';
                        }
                    }

                    function closeOrderReturnModal() {
                        const modal = document.getElementById('orderReturnModalBackdrop');
                        if (modal) {
                            modal.style.display = 'none';
                            document.body.style.overflow = '';
                        }
                    }

                    function previewReturnImage(input) {
                        const previewContainer = document.getElementById('returnImagePreviewBox');
                        const previewImg = document.getElementById('returnImagePreview');
                        const fileNameLabel = document.getElementById('returnImageFileName');

                        if (input.files && input.files[0]) {
                            const file = input.files[0];
                            if (file.size > 10 * 1024 * 1024) {
                                alert('Image size exceeds 10MB limit. Please choose a smaller photo.');
                                input.value = '';
                                clearReturnImagePreview();
                                return;
                            }
                            const reader = new FileReader();
                            reader.onload = function (e) {
                                previewImg.src = e.target.result;
                                fileNameLabel.innerText = file.name + ' (' + (file.size / 1024).toFixed(1) + ' KB)';
                                previewContainer.style.display = 'flex';
                            };
                            reader.readAsDataURL(file);
                        } else {
                            clearReturnImagePreview();
                        }
                    }

                    function clearReturnImagePreview() {
                        const input = document.getElementById('returnImageInput');
                        if (input) input.value = '';
                        const previewContainer = document.getElementById('returnImagePreviewBox');
                        if (previewContainer) previewContainer.style.display = 'none';
                        const previewImg = document.getElementById('returnImagePreview');
                        if (previewImg) previewImg.src = '';
                    }

                    // Review Modal Logic
                    let currentRatingScore = 5;

                    function openProductReviewModal(productId, productName, sku, rating, title, comment, imageUrl) {
                        document.getElementById('reviewModalProductId').value = productId;
                        document.getElementById('reviewModalProductName').innerText = productName;
                        document.getElementById('reviewModalSku').innerText = sku ? 'SKU: ' + sku : '';

                        const isEdit = (title && title.trim().length > 0) || (comment && comment.trim().length > 0);

                        document.getElementById('reviewModalHeaderText').innerText = isEdit ? '✏️ Edit Product Review' : 'Rate & Review Product';
                        document.getElementById('reviewModalSubmitBtn').innerHTML = isEdit ? '<span>✍️</span> Update Verified Review' : '<span>✍️</span> Submit Verified Review';

                        setModalRating(rating ? parseInt(rating) : 5);
                        document.getElementById('reviewTitleInput').value = title || '';
                        document.getElementById('reviewCommentInput').value = comment || '';

                        clearReviewImagePreview();
                        if (imageUrl && imageUrl.trim().length > 0) {
                            const previewContainer = document.getElementById('reviewImagePreviewBox');
                            const previewImg = document.getElementById('reviewImagePreview');
                            const fileNameLabel = document.getElementById('reviewImageFileName');
                            previewImg.src = imageUrl;
                            fileNameLabel.innerText = 'Current attached photo (uploading new photo will replace it)';
                            previewContainer.style.display = 'flex';
                        }

                        const modal = document.getElementById('productReviewModalBackdrop');
                        modal.style.display = 'flex';
                        document.body.style.overflow = 'hidden';
                    }

                    function closeProductReviewModal() {
                        const modal = document.getElementById('productReviewModalBackdrop');
                        modal.style.display = 'none';
                        document.body.style.overflow = '';
                    }

                    function setModalRating(score) {
                        currentRatingScore = score;
                        document.getElementById('reviewModalRatingValue').value = score;

                        const starDescs = {
                            1: '★☆☆☆☆ (1/5 - Poor)',
                            2: '★★☆☆☆ (2/5 - Fair)',
                            3: '★★★☆☆ (3/5 - Average)',
                            4: '★★★★☆ (4/5 - Good)',
                            5: '★★★★★ (5/5 - Excellent)'
                        };
                        document.getElementById('modalRatingLabel').innerText = starDescs[score] || '';

                        for (let i = 1; i <= 5; i++) {
                            const starEl = document.getElementById('starBtn_' + i);
                            if (starEl) {
                                starEl.style.color = (i <= score) ? '#f59e0b' : '#cbd5e1';
                                starEl.style.transform = (i === score) ? 'scale(1.2)' : 'scale(1)';
                            }
                        }
                    }

                    function previewReviewImage(input) {
                        const previewContainer = document.getElementById('reviewImagePreviewBox');
                        const previewImg = document.getElementById('reviewImagePreview');
                        const fileNameLabel = document.getElementById('reviewImageFileName');

                        if (input.files && input.files[0]) {
                            const file = input.files[0];
                            if (file.size > 10 * 1024 * 1024) {
                                alert('Image size exceeds 10MB limit. Please choose a smaller photo.');
                                input.value = '';
                                clearReviewImagePreview();
                                return;
                            }
                            const reader = new FileReader();
                            reader.onload = function (e) {
                                previewImg.src = e.target.result;
                                fileNameLabel.innerText = file.name + ' (' + (file.size / 1024).toFixed(1) + ' KB)';
                                previewContainer.style.display = 'flex';
                            };
                            reader.readAsDataURL(file);
                        } else {
                            clearReviewImagePreview();
                        }
                    }

                    function clearReviewImagePreview() {
                        const input = document.getElementById('reviewImageInput');
                        if (input) input.value = '';
                        const previewContainer = document.getElementById('reviewImagePreviewBox');
                        if (previewContainer) previewContainer.style.display = 'none';
                        const previewImg = document.getElementById('reviewImagePreview');
                        if (previewImg) previewImg.src = '';
                    }
                </script>

                <!-- ==============================================================================
         ORDER RETURN & REPLACEMENT MODAL FOR DELIVERED ORDERS
         ============================================================================== -->
                <div id="orderReturnModalBackdrop"
                    style="display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.75); backdrop-filter: blur(6px); -webkit-backdrop-filter: blur(6px); align-items: center; justify-content: center; z-index: 100000; padding: 1.5rem; animation: payModalFadeIn 0.25s ease forwards;">
                    <div
                        style="background: #ffffff; border-radius: 20px; max-width: 580px; width: 100%; max-height: 90vh; overflow-y: auto; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.35); position: relative; animation: payModalSlideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards; border: 1px solid #e2e8f0;">

                        <!-- Modal Header -->
                        <div
                            style="padding: 1.5rem 1.75rem 1.25rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: flex-start; background: #f8fafc; border-top-left-radius: 20px; border-top-right-radius: 20px;">
                            <div>
                                <div
                                    style="display: inline-flex; align-items: center; gap: 0.35rem; font-size: 0.75rem; font-weight: 800; color: #1e40af; background: #dbeafe; padding: 0.2rem 0.6rem; border-radius: 9999px; margin-bottom: 0.4rem;">
                                    🔄 7-Day Hassle-Free Returns & Replacements
                                </div>
                                <h3
                                    style="font-size: 1.25rem; font-weight: 900; color: #0f172a; margin: 0; line-height: 1.3;">
                                    Return / Replace Order #${order.orderNumber}
                                </h3>
                                <div style="font-size: 0.8rem; color: #64748b; margin-top: 0.2rem;">
                                    Delivered on <strong>
                                        <c:out value="${order.formattedDeliveredDate}" />
                                    </strong>
                                </div>
                            </div>
                            <button type="button" onclick="closeOrderReturnModal()"
                                style="background: #f1f5f9; border: none; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; color: #64748b; cursor: pointer; transition: all 0.2s ease;">✕</button>
                        </div>

                        <!-- Modal Form Body -->
                        <form action="${pageContext.request.contextPath}/order/return" method="POST"
                            enctype="multipart/form-data" style="padding: 1.5rem 1.75rem;">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="orderId" value="${order.orderId}">

                            <!-- 1. Return Reason -->
                            <div style="margin-bottom: 1.25rem;">
                                <label for="returnReasonSelect"
                                    style="display: block; font-size: 0.88rem; font-weight: 800; color: #1e293b; margin-bottom: 0.4rem;">
                                    Reason for Return / Replacement <span style="color: #ef4444;">*</span>
                                </label>
                                <select id="returnReasonSelect" name="returnReason" required
                                    style="width: 100%; box-sizing: border-box; padding: 0.75rem 1rem; border: 1.5px solid #cbd5e1; border-radius: 8px; font-size: 0.92rem; font-weight: 600; outline: none; background: #f8fafc;">
                                    <option value="">-- Please select a reason --</option>
                                    <option value="Defective / Damaged product received">⚠️ Defective / Damaged product
                                        received</option>
                                    <option value="Wrong item delivered">📦 Wrong item delivered</option>
                                    <option value="Product does not match website description">🔍 Product does not match
                                        website description</option>
                                    <option value="Quality not as expected">⭐ Quality not as expected</option>
                                    <option value="Missing parts or accessories">🧩 Missing parts or accessories
                                    </option>
                                    <option value="Changed mind / No longer needed">💭 Changed mind / No longer needed
                                    </option>
                                    <option value="Other reason">📝 Other reason</option>
                                </select>
                            </div>

                            <!-- 2. Resolution Type Preference -->
                            <div style="margin-bottom: 1.25rem;">
                                <label
                                    style="display: block; font-size: 0.88rem; font-weight: 800; color: #1e293b; margin-bottom: 0.5rem;">
                                    Preferred Resolution <span style="color: #ef4444;">*</span>
                                </label>
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
                                    <label
                                        style="display: flex; align-items: center; gap: 0.6rem; padding: 0.75rem; border: 1.5px solid #cbd5e1; border-radius: 8px; cursor: pointer; background: #ffffff;">
                                        <input type="radio" name="resolutionType" value="REFUND" checked
                                            style="accent-color: #2563eb;">
                                        <div>
                                            <div style="font-weight: 800; font-size: 0.88rem; color: #0f172a;">💳 Refund
                                            </div>
                                            <div style="font-size: 0.75rem; color: #64748b;">Credit back to source
                                                payment</div>
                                        </div>
                                    </label>
                                    <label
                                        style="display: flex; align-items: center; gap: 0.6rem; padding: 0.75rem; border: 1.5px solid #cbd5e1; border-radius: 8px; cursor: pointer; background: #ffffff;">
                                        <input type="radio" name="resolutionType" value="REPLACEMENT"
                                            style="accent-color: #2563eb;">
                                        <div>
                                            <div style="font-weight: 800; font-size: 0.88rem; color: #0f172a;">🔄
                                                Replacement</div>
                                            <div style="font-size: 0.75rem; color: #64748b;">Free courier replacement
                                                unit</div>
                                        </div>
                                    </label>
                                </div>
                            </div>

                            <!-- 3. Comments / Issue Description -->
                            <div style="margin-bottom: 1.25rem;">
                                <label for="returnCommentsInput"
                                    style="display: block; font-size: 0.88rem; font-weight: 800; color: #1e293b; margin-bottom: 0.4rem;">
                                    Detailed Comments / Feedback
                                </label>
                                <textarea id="returnCommentsInput" name="comments" rows="3" maxlength="1000"
                                    placeholder="Please share any specific details about the issue with the product or packaging..."
                                    style="width: 100%; box-sizing: border-box; padding: 0.75rem 1rem; border: 1.5px solid #cbd5e1; border-radius: 8px; font-size: 0.92rem; font-weight: 500; outline: none; resize: vertical; line-height: 1.5;"></textarea>
                            </div>

                            <!-- 4. Damaged Product Photo Upload -->
                            <div style="margin-bottom: 1.5rem;">
                                <label
                                    style="display: block; font-size: 0.88rem; font-weight: 800; color: #1e293b; margin-bottom: 0.4rem;">
                                    📸 Upload Photo of Product / Defect (Optional)
                                </label>

                                <div style="border: 2px dashed #94a3b8; background: #f8fafc; border-radius: 10px; padding: 1.1rem; text-align: center; cursor: pointer;"
                                    onclick="document.getElementById('returnImageInput').click();">
                                    <div style="font-size: 1.8rem; margin-bottom: 0.25rem;">📷</div>
                                    <div style="font-size: 0.85rem; font-weight: 700; color: #1e293b;">Click to upload
                                        proof photo</div>
                                    <div style="font-size: 0.72rem; color: #64748b; margin-top: 0.15rem;">Supports JPG,
                                        PNG, WebP up to 10MB</div>
                                    <input type="file" id="returnImageInput" name="returnImage" accept="image/*"
                                        style="display: none;" onchange="previewReturnImage(this)">
                                </div>

                                <!-- Image Preview Area -->
                                <div id="returnImagePreviewBox"
                                    style="display: none; align-items: center; gap: 1rem; margin-top: 0.85rem; padding: 0.75rem; background: #f1f5f9; border-radius: 8px; border: 1px solid #e2e8f0;">
                                    <img id="returnImagePreview" src="" alt="Return Preview"
                                        style="width: 64px; height: 64px; object-fit: cover; border-radius: 6px; border: 1px solid #cbd5e1;">
                                    <div style="flex: 1; min-width: 0;">
                                        <div id="returnImageFileName"
                                            style="font-size: 0.82rem; font-weight: 700; color: #0f172a; word-break: break-all;">
                                        </div>
                                        <div
                                            style="font-size: 0.72rem; color: #10b981; font-weight: 600; margin-top: 0.15rem;">
                                            ✓ Ready to upload with request</div>
                                    </div>
                                    <button type="button" onclick="clearReturnImagePreview()"
                                        style="background: #fee2e2; border: 1px solid #fca5a5; color: #b91c1c; border-radius: 6px; padding: 0.35rem 0.65rem; font-size: 0.78rem; font-weight: 700; cursor: pointer;">Remove</button>
                                </div>
                            </div>

                            <!-- Modal Actions -->
                            <div
                                style="display: flex; justify-content: flex-end; gap: 0.75rem; border-top: 1px solid #e2e8f0; padding-top: 1.25rem;">
                                <button type="button" onclick="closeOrderReturnModal()"
                                    style="background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; padding: 0.65rem 1.25rem; border-radius: 8px; font-weight: 700; cursor: pointer;">
                                    Cancel
                                </button>
                                <button type="submit"
                                    style="background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: #ffffff; border: none; padding: 0.65rem 1.5rem; border-radius: 8px; font-weight: 800; cursor: pointer; box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3); display: flex; align-items: center; gap: 0.4rem;">
                                    <span>✓</span> Submit Return Request
                                </button>
                            </div>

                        </form>
                    </div>
                </div>

                <!-- ==============================================================================
         PRODUCT REVIEW MODAL WITH IMAGE UPLOAD FOR DELIVERED ITEMS
         ============================================================================== -->
                <div id="productReviewModalBackdrop"
                    style="display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.75); backdrop-filter: blur(6px); -webkit-backdrop-filter: blur(6px); align-items: center; justify-content: center; z-index: 100000; padding: 1.5rem; animation: payModalFadeIn 0.25s ease forwards;">
                    <div
                        style="background: #ffffff; border-radius: 20px; max-width: 580px; width: 100%; max-height: 90vh; overflow-y: auto; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.35); position: relative; animation: payModalSlideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards; border: 1px solid #e2e8f0;">

                        <!-- Modal Header -->
                        <div
                            style="padding: 1.5rem 1.75rem 1.25rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: flex-start; background: #f8fafc; border-top-left-radius: 20px; border-top-right-radius: 20px;">
                            <div>
                                <div
                                    style="display: inline-flex; align-items: center; gap: 0.35rem; font-size: 0.75rem; font-weight: 800; color: #15803d; background: #dcfce7; padding: 0.2rem 0.6rem; border-radius: 9999px; margin-bottom: 0.4rem;">
                                    ✓ Verified Purchase &bull; Delivered Order #${order.orderNumber}
                                </div>
                                <h3 style="font-size: 1.25rem; font-weight: 900; color: #0f172a; margin: 0; line-height: 1.3;"
                                    id="reviewModalHeaderText">
                                    Rate & Review Product
                                </h3>
                                <div style="font-size: 0.88rem; font-weight: 700; color: #2563eb; margin-top: 0.2rem;"
                                    id="reviewModalProductName"></div>
                                <div style="font-size: 0.8rem; color: #64748b; margin-top: 0.1rem; font-family: monospace;"
                                    id="reviewModalSku"></div>
                            </div>
                            <button type="button" onclick="closeProductReviewModal()"
                                style="background: #f1f5f9; border: none; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; color: #64748b; cursor: pointer; transition: all 0.2s ease;">✕</button>
                        </div>

                        <!-- Modal Form Body -->
                        <form action="${pageContext.request.contextPath}/order/review" method="POST"
                            enctype="multipart/form-data" style="padding: 1.5rem 1.75rem;">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <input type="hidden" name="productId" id="reviewModalProductId" value="">
                            <input type="hidden" name="rating" id="reviewModalRatingValue" value="5">

                            <!-- 1. Star Rating Selector -->
                            <div style="margin-bottom: 1.35rem;">
                                <label
                                    style="display: block; font-size: 0.88rem; font-weight: 800; color: #1e293b; margin-bottom: 0.5rem;">
                                    Overall Product Rating <span style="color: #ef4444;">*</span>
                                </label>
                                <div style="display: flex; align-items: center; gap: 0.5rem;">
                                    <div style="display: flex; gap: 0.35rem;">
                                        <c:forEach var="i" begin="1" end="5">
                                            <button type="button" id="starBtn_${i}" onclick="setModalRating(${i})"
                                                style="background: none; border: none; font-size: 2rem; color: #f59e0b; cursor: pointer; padding: 0; line-height: 1; transition: transform 0.15s ease;">
                                                ★
                                            </button>
                                        </c:forEach>
                                    </div>
                                    <span id="modalRatingLabel"
                                        style="font-size: 0.88rem; font-weight: 700; color: #475569; margin-left: 0.5rem;">
                                        ★★★★★ (5/5 - Excellent)
                                    </span>
                                </div>
                            </div>

                            <!-- 2. Review Headline -->
                            <div style="margin-bottom: 1.25rem;">
                                <label for="reviewTitleInput"
                                    style="display: block; font-size: 0.88rem; font-weight: 800; color: #1e293b; margin-bottom: 0.4rem;">
                                    Review Headline / Title <span style="color: #ef4444;">*</span>
                                </label>
                                <input type="text" id="reviewTitleInput" name="title" required maxlength="150"
                                    placeholder="e.g. Excellent build quality, highly recommended!"
                                    style="width: 100%; box-sizing: border-box; padding: 0.75rem 1rem; border: 1.5px solid #cbd5e1; border-radius: 8px; font-size: 0.92rem; font-weight: 500; outline: none; transition: border-color 0.2s;">
                            </div>

                            <!-- 3. Review Comments / Feedback -->
                            <div style="margin-bottom: 1.25rem;">
                                <label for="reviewCommentInput"
                                    style="display: block; font-size: 0.88rem; font-weight: 800; color: #1e293b; margin-bottom: 0.4rem;">
                                    Detailed Feedback & Experience <span style="color: #ef4444;">*</span>
                                </label>
                                <textarea id="reviewCommentInput" name="comment" required rows="4" maxlength="2000"
                                    placeholder="Describe what you liked, product performance, packaging, and unboxing experience..."
                                    style="width: 100%; box-sizing: border-box; padding: 0.75rem 1rem; border: 1.5px solid #cbd5e1; border-radius: 8px; font-size: 0.92rem; font-weight: 500; outline: none; resize: vertical; line-height: 1.5; transition: border-color 0.2s;"></textarea>
                            </div>

                            <!-- 4. Photo / Image Upload -->
                            <div style="margin-bottom: 1.5rem;">
                                <label
                                    style="display: block; font-size: 0.88rem; font-weight: 800; color: #1e293b; margin-bottom: 0.4rem;">
                                    📸 Upload Product Photos (Unboxing & Product in use)
                                </label>

                                <div style="border: 2px dashed #94a3b8; background: #f8fafc; border-radius: 10px; padding: 1.25rem; text-align: center; cursor: pointer; transition: all 0.2s ease;"
                                    onclick="document.getElementById('reviewImageInput').click();">
                                    <div style="font-size: 2rem; margin-bottom: 0.35rem;">📷</div>
                                    <div style="font-size: 0.88rem; font-weight: 700; color: #1e293b;">Click to upload
                                        product photo</div>
                                    <div style="font-size: 0.75rem; color: #64748b; margin-top: 0.2rem;">Supports JPG,
                                        PNG, WebP up to 10MB</div>
                                    <input type="file" id="reviewImageInput" name="reviewImage" accept="image/*"
                                        style="display: none;" onchange="previewReviewImage(this)">
                                </div>

                                <!-- Image Preview Area -->
                                <div id="reviewImagePreviewBox"
                                    style="display: none; align-items: center; gap: 1rem; margin-top: 0.85rem; padding: 0.75rem; background: #f1f5f9; border-radius: 8px; border: 1px solid #e2e8f0;">
                                    <img id="reviewImagePreview" src="" alt="Review Preview"
                                        style="width: 64px; height: 64px; object-fit: cover; border-radius: 6px; border: 1px solid #cbd5e1;">
                                    <div style="flex: 1; min-width: 0;">
                                        <div id="reviewImageFileName"
                                            style="font-size: 0.82rem; font-weight: 700; color: #0f172a; word-break: break-all;">
                                        </div>
                                        <div
                                            style="font-size: 0.72rem; color: #10b981; font-weight: 600; margin-top: 0.15rem;">
                                            ✓ Ready to upload with review</div>
                                    </div>
                                    <button type="button" onclick="clearReviewImagePreview()"
                                        style="background: #fee2e2; border: 1px solid #fca5a5; color: #b91c1c; border-radius: 6px; padding: 0.35rem 0.65rem; font-size: 0.78rem; font-weight: 700; cursor: pointer;">Remove</button>
                                </div>
                            </div>

                            <!-- Modal Actions -->
                            <div
                                style="display: flex; justify-content: flex-end; gap: 0.75rem; border-top: 1px solid #e2e8f0; padding-top: 1.25rem;">
                                <button type="button" onclick="closeProductReviewModal()"
                                    style="background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; padding: 0.65rem 1.25rem; border-radius: 8px; font-weight: 700; cursor: pointer;">
                                    Cancel
                                </button>
                                <button type="submit" id="reviewModalSubmitBtn"
                                    style="background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: #ffffff; border: none; padding: 0.65rem 1.5rem; border-radius: 8px; font-weight: 800; cursor: pointer; box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3); display: flex; align-items: center; gap: 0.4rem;">
                                    <span>✍️</span> Submit Verified Review
                                </button>
                            </div>

                        </form>
                    </div>
                </div>
            </body>

            </html>