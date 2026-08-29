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
    <title>Select Delivery Address | Step 1 of 3 | ShopKart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        .checkout-container {
            max-width: 860px;
            margin: 2rem auto 4rem;
            padding: 0 1.5rem;
        }
        .checkout-stepper {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 2rem;
            margin-bottom: 2.5rem;
            flex-wrap: wrap;
        }
        .step-item {
            display: flex;
            align-items: center;
            gap: 0.6rem;
        }
        .step-bubble {
            width: 34px;
            height: 34px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 800;
            font-size: 0.9rem;
            transition: all 0.2s ease;
        }
        .step-bubble.active {
            background: #2563eb;
            color: #ffffff;
            box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);
        }
        .step-bubble.inactive {
            background: #e2e8f0;
            color: #64748b;
        }
        .step-divider {
            width: 40px;
            height: 2px;
            background: #cbd5e1;
        }
        .address-card-option {
            border: 2px solid #e2e8f0;
            border-radius: 14px;
            padding: 1.25rem 1.5rem;
            margin-bottom: 1rem;
            cursor: pointer;
            display: flex;
            gap: 1.25rem;
            align-items: flex-start;
            background: #ffffff;
            transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
            position: relative;
        }
        .address-card-option:hover {
            border-color: #93c5fd;
            background: #f8faff;
            transform: translateY(-1px);
        }
        .address-card-option.selected {
            border-color: #2563eb;
            background: #eff6ff;
            box-shadow: 0 4px 15px rgba(37, 99, 235, 0.1);
        }
    </style>
</head>
<body style="background: #f8fafc; min-height: 100vh; display: flex; flex-direction: column;">

    <!-- 1. TOP ANNOUNCEMENT TICKER -->
    <header class="top-ticker">
        <div class="ticker-text">
            <a href="${pageContext.request.contextPath}/products?deals=true" style="color: inherit; text-decoration: none; display: inline-flex; align-items: center; gap: 0.5rem;">
                <span class="ticker-badge">⚡ Safe Checkout</span>
                <span>Grand Festive Carnival: Up to 50% Off Top Brands + Extra 20% Off with Code <strong>SAVE20</strong> &bull; Explore Deals &rarr;</span>
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
            <!-- Delivery Locator (Pin Code Trigger) -->
            <div class="delivery-locator" id="headerDeliveryTrigger" onclick="openPinCodeModal()" title="Change delivery location">
                <span class="loc-icon">📍</span>
                <div class="loc-text">
                    <span class="sub">Deliver to</span>
                    <span class="main" id="headerPincodeText">Satara 415312</span>
                </div>
            </div>
        </div>

        <!-- Global Search Bar with Live Autocomplete -->
        <div class="header-search-wrapper">
            <form action="${pageContext.request.contextPath}/products" method="GET" class="header-search-form" id="headerSearchForm">
                <input type="text" name="keyword" class="search-input" id="globalSearchInput" placeholder="Search for products, brands and tech essentials..." autocomplete="off">
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
                    <span class="badge-count" id="headerCartBadge">${not empty sessionScope.cart ? sessionScope.cart.totalQuantity : (not empty cart ? cart.totalQuantity : 0)}</span>
                </div>
                <span>Cart</span>
            </a>
        </div>
    </nav>

    <main class="checkout-container">

        <!-- Progress Stepper: Step 1 Active -->
        <div class="checkout-stepper">
            <div class="step-item">
                <div class="step-bubble active">1</div>
                <strong style="color: #0f172a; font-size: 0.95rem;">Delivery Address</strong>
            </div>
            <div class="step-divider"></div>
            <div class="step-item">
                <div class="step-bubble inactive">2</div>
                <span style="color: #64748b; font-size: 0.95rem; font-weight: 600;">Order Summary</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item">
                <div class="step-bubble inactive">3</div>
                <span style="color: #64748b; font-size: 0.95rem; font-weight: 600;">Payment</span>
            </div>
        </div>

        <c:set var="addressReturnUrl" value="/checkout/address" />

        <c:if test="${not empty error}">
            <div style="background: #fee2e2; border-left: 4px solid var(--danger); padding: 1rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #991b1b; font-weight: 700;">
                ⚠️ <c:out value="${error}" />
            </div>
        </c:if>

        <!-- MAIN ADDRESS SELECTION CARD -->
        <div style="background: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; padding: 2rem; box-shadow: 0 4px 20px rgba(0,0,0,0.04);">
            
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.75rem; border-bottom: 1px solid #f1f5f9; padding-bottom: 1rem; flex-wrap: wrap; gap: 0.75rem;">
                <div>
                    <h1 style="font-size: 1.35rem; font-weight: 900; color: #0f172a; margin: 0 0 0.25rem 0;">
                        📍 Select Delivery Address
                    </h1>
                    <p style="color: #64748b; font-size: 0.88rem; margin: 0;">
                        Choose where you want your order delivered or add a new address.
                    </p>
                </div>
                <a href="${pageContext.request.contextPath}/addresses/add?returnUrl=${addressReturnUrl}" class="order-btn-secondary" style="font-size: 0.85rem; padding: 0.55rem 1.1rem; border-radius: 8px; font-weight: 800; background: #f8fafc; border: 1.5px solid #cbd5e1; color: #0f172a; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem;">
                    <span>➕ Add New Address</span>
                </a>
            </div>

            <form id="addressSelectionForm" action="${pageContext.request.contextPath}/checkout/address" method="POST">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <c:if test="${isDirectBuy}">
                    <input type="hidden" name="buyNowProductId" value="${directBuyProductId}">
                    <input type="hidden" name="quantity" value="${directBuyQuantity}">
                </c:if>

                <c:choose>
                    <c:when test="${empty addresses}">
                        <div style="text-align: center; padding: 3.5rem 1.5rem; border: 2px dashed #cbd5e1; border-radius: 14px; background: #f8fafc; margin: 1.5rem 0;">
                            <div style="font-size: 3rem; margin-bottom: 0.75rem;">📍</div>
                            <h3 style="font-size: 1.2rem; font-weight: 800; color: #0f172a; margin-bottom: 0.35rem;">No saved address found</h3>
                            <p style="color: #64748b; margin-bottom: 1.5rem; font-size: 0.92rem;">
                                You need to add a delivery address before you can proceed to the order summary.
                            </p>
                            <a href="${pageContext.request.contextPath}/addresses/add?returnUrl=${addressReturnUrl}" class="hero-cta-btn" style="padding: 0.75rem 1.75rem; font-size: 0.95rem; border-radius: 8px; text-decoration: none;">
                                ➕ Add Delivery Address Now
                            </a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="address-options-list" style="margin-bottom: 2rem;">
                            <c:forEach var="addr" items="${addresses}" varStatus="status">
                                <label class="address-card-option ${ (selectedAddressId == addr.addressId or (empty selectedAddressId and addr.defaultAddress) or (empty selectedAddressId and status.first)) ? 'selected' : '' }" onclick="selectAddressCard(this)">
                                    <input type="radio" name="addressId" value="${addr.addressId}" ${ (selectedAddressId == addr.addressId or (empty selectedAddressId and addr.defaultAddress) or (empty selectedAddressId and status.first)) ? 'checked' : '' } required style="margin-top: 0.35rem; accent-color: #2563eb; width: 20px; height: 20px; cursor: pointer;">
                                    
                                    <div style="flex: 1; min-width: 0; font-size: 0.92rem; color: #334155; line-height: 1.55;">
                                        <div style="display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.4rem; flex-wrap: wrap;">
                                            <strong style="font-size: 1.05rem; color: #0f172a;"><c:out value="${addr.fullName}" /></strong>
                                            <span class="pill-badge badge-info" style="font-size: 0.72rem; padding: 0.2rem 0.55rem; border-radius: 6px; font-weight: 800;"><c:out value="${addr.addressType}" /></span>
                                            <c:if test="${addr.defaultAddress}">
                                                <span class="pill-badge badge-success" style="font-size: 0.72rem; padding: 0.2rem 0.55rem; border-radius: 6px; font-weight: 800;">DEFAULT</span>
                                            </c:if>
                                        </div>

                                        <div style="color: #475569; margin-bottom: 0.35rem;">
                                            <c:out value="${addr.addressLine1}" /><c:if test="${not empty addr.addressLine2}">, <c:out value="${addr.addressLine2}" /></c:if><br>
                                            <c:out value="${addr.city}" />, <c:out value="${addr.state}" /> &mdash; <strong><c:out value="${addr.postalCode}" /></strong>
                                        </div>

                                        <div style="font-size: 0.85rem; color: #64748b;">
                                            📞 Phone: <strong style="color: #0f172a;"><c:out value="${addr.phone}" /></strong>
                                        </div>
                                    </div>

                                    <div>
                                        <a href="${pageContext.request.contextPath}/addresses/edit?id=${addr.addressId}&returnUrl=${addressReturnUrl}" style="font-size: 0.82rem; font-weight: 700; color: #2563eb; text-decoration: none; border: 1px solid #bfdbfe; padding: 0.3rem 0.65rem; border-radius: 6px; background: #ffffff;">
                                            ✏️ Edit
                                        </a>
                                    </div>
                                </label>
                            </c:forEach>
                        </div>

                        <!-- Action Submit Button -->
                        <div style="display: flex; justify-content: flex-end; align-items: center; border-top: 1px solid #f1f5f9; padding-top: 1.5rem;">
                            <button type="submit" class="hero-cta-btn" style="padding: 0.85rem 2.25rem; font-size: 1rem; font-weight: 800; border-radius: 10px; cursor: pointer; border: none; background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: #ffffff; box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35); display: inline-flex; align-items: center; gap: 0.5rem;">
                                <span>Save &amp; Proceed &rarr;</span>
                            </button>
                        </div>
                    </c:otherwise>
                </c:choose>
            </form>
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
        function selectAddressCard(card) {
            document.querySelectorAll('.address-card-option').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            const radio = card.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
        }
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
</body>
</html>
