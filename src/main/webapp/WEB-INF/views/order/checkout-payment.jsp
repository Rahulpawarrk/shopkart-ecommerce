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
    <title>Payment & Final Confirmation | Step 3 of 3 | ShopKart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        .checkout-layout {
            display: grid;
            grid-template-columns: 1fr 380px;
            gap: 2rem;
            margin-top: 1.5rem;
        }
        @media (max-width: 900px) {
            .checkout-layout {
                grid-template-columns: 1fr;
            }
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
        .step-bubble.completed {
            background: #10b981;
            color: #ffffff;
        }
        .step-bubble.active {
            background: #2563eb;
            color: #ffffff;
            box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);
        }
        .step-divider {
            width: 40px;
            height: 2px;
            background: #cbd5e1;
        }
        .payment-option-card {
            border: 2px solid #e2e8f0;
            border-radius: 14px;
            padding: 1.25rem 1.5rem;
            margin-bottom: 1rem;
            cursor: pointer;
            display: flex;
            gap: 1.25rem;
            align-items: center;
            background: #ffffff;
            transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
        }
        .payment-option-card:hover {
            border-color: #93c5fd;
            background: #f8faff;
            transform: translateY(-1px);
        }
        .payment-option-card.selected {
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

    <main class="container" style="max-width: 1200px; margin: 2rem auto 4rem; padding: 0 1.5rem;">

        <!-- Progress Stepper: Step 3 Active -->
        <div class="checkout-stepper">
            <a href="${pageContext.request.contextPath}/checkout/address${isDirectBuy ? '?buyNowProductId='.concat(directBuyProductId).concat('&quantity=').concat(directBuyQuantity).concat('&addressId=').concat(selectedAddress.addressId) : '?addressId='.concat(selectedAddress.addressId)}" class="step-item" style="text-decoration: none;">
                <div class="step-bubble completed">✓</div>
                <span style="color: #10b981; font-size: 0.95rem; font-weight: 700;">1. Delivery Address</span>
            </a>
            <div class="step-divider"></div>
            <a href="${pageContext.request.contextPath}/checkout/summary?addressId=${selectedAddress.addressId}${isDirectBuy ? '&buyNowProductId='.concat(directBuyProductId).concat('&quantity=').concat(directBuyQuantity) : ''}" class="step-item" style="text-decoration: none;">
                <div class="step-bubble completed">✓</div>
                <span style="color: #10b981; font-size: 0.95rem; font-weight: 700;">2. Order Summary</span>
            </a>
            <div class="step-divider"></div>
            <div class="step-item">
                <div class="step-bubble active">3</div>
                <strong style="color: #0f172a; font-size: 0.95rem;">Payment</strong>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div style="background: #fee2e2; border-left: 4px solid var(--danger); padding: 1rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #991b1b; font-weight: 700;">
                ⚠️ <c:out value="${error}" />
            </div>
        </c:if>

        <form id="finalOrderPlacementForm" action="${pageContext.request.contextPath}/checkout" method="POST">
            <input type="hidden" name="addressId" value="${selectedAddress.addressId}">
            <input type="hidden" name="notes" value="<c:out value="${notes}" />">
            <c:if test="${isDirectBuy}">
                <input type="hidden" name="buyNowProductId" value="${directBuyProductId}">
                <input type="hidden" name="quantity" value="${directBuyQuantity}">
            </c:if>

            <div class="checkout-layout">
                
                <!-- Left Column: Shipping Address & Select Payment Channel -->
                <div>
                    
                    <!-- 1. Shipping Address Card -->
                    <div style="background: #ffffff; border-radius: 14px; border: 1px solid #e2e8f0; padding: 1.5rem 1.75rem; margin-bottom: 1.5rem; box-shadow: 0 2px 10px rgba(0,0,0,0.02);">
                        <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1rem;">
                            <h2 style="font-size: 1.15rem; font-weight: 900; color: #0f172a; margin: 0; display: flex; align-items: center; gap: 0.5rem;">
                                <span>📍 Shipping Address</span>
                            </h2>
                            <a href="${pageContext.request.contextPath}/checkout/address${isDirectBuy ? '?buyNowProductId='.concat(directBuyProductId).concat('&quantity=').concat(directBuyQuantity).concat('&addressId=').concat(selectedAddress.addressId) : '?addressId='.concat(selectedAddress.addressId)}" style="font-size: 0.82rem; font-weight: 800; color: #2563eb; text-decoration: none; border: 1px solid #bfdbfe; padding: 0.35rem 0.75rem; border-radius: 6px; background: #eff6ff;">
                                ✏️ Change Address
                            </a>
                        </div>
                        
                        <div style="font-size: 0.95rem; color: #334155; line-height: 1.6;">
                            <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.3rem;">
                                <strong style="color: #0f172a; font-size: 1.05rem;"><c:out value="${selectedAddress.fullName}" /></strong>
                                <span class="pill-badge badge-info" style="font-size: 0.72rem; padding: 0.15rem 0.5rem;"><c:out value="${selectedAddress.addressType}" /></span>
                            </div>
                            <div>
                                <c:out value="${selectedAddress.addressLine1}" /><c:if test="${not empty selectedAddress.addressLine2}">, <c:out value="${selectedAddress.addressLine2}" /></c:if><br>
                                <c:out value="${selectedAddress.city}" />, <c:out value="${selectedAddress.state}" /> &mdash; <strong><c:out value="${selectedAddress.postalCode}" /></strong><br>
                                <span style="color: #64748b;">Phone:</span> <strong style="color: #0f172a;"><c:out value="${selectedAddress.phone}" /></strong>
                            </div>
                        </div>
                    </div>

                    <!-- 2. Select Payment Channel Card -->
                    <div style="background: #ffffff; border-radius: 14px; border: 1px solid #e2e8f0; padding: 1.75rem; box-shadow: 0 2px 10px rgba(0,0,0,0.02);">
                        <div style="margin-bottom: 1.5rem; border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem;">
                            <h2 style="font-size: 1.2rem; font-weight: 900; color: #0f172a; margin: 0 0 0.25rem 0;">
                                💳 Select Payment Channel
                            </h2>
                            <p style="color: #64748b; font-size: 0.88rem; margin: 0;">
                                Choose your preferred payment option below.
                            </p>
                        </div>

                        <div class="payment-options-list">
                            
                            <!-- COD -->
                            <label class="payment-option-card selected" onclick="selectPaymentCard(this)">
                                <input type="radio" name="paymentMethod" value="COD" checked required style="accent-color: #2563eb; width: 20px; height: 20px; cursor: pointer;">
                                <div style="font-size: 1.6rem; width: 40px; text-align: center;">💵</div>
                                <div style="flex: 1; min-width: 0;">
                                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                                        <strong style="font-size: 1.05rem; color: #0f172a;">Cash on Delivery (COD)</strong>
                                        <span class="pill-badge badge-success" style="font-size: 0.72rem; padding: 0.15rem 0.5rem; font-weight: 800;">POPULAR</span>
                                    </div>
                                    <div style="color: #64748b; font-size: 0.85rem; margin-top: 0.2rem;">
                                        Pay with cash or UPI scan when your order arrives at your doorstep.
                                    </div>
                                </div>
                            </label>

                            <!-- UPI -->
                            <label class="payment-option-card" onclick="selectPaymentCard(this)">
                                <input type="radio" name="paymentMethod" value="UPI" style="accent-color: #2563eb; width: 20px; height: 20px; cursor: pointer;">
                                <div style="font-size: 1.6rem; width: 40px; text-align: center;">⚡</div>
                                <div style="flex: 1; min-width: 0;">
                                    <strong style="font-size: 1.05rem; color: #0f172a;">UPI / QR Code Instant</strong>
                                    <div style="color: #64748b; font-size: 0.85rem; margin-top: 0.2rem;">
                                        Google Pay, PhonePe, Paytm, BHIM &amp; all UPI Apps.
                                    </div>
                                </div>
                            </label>

                            <!-- Credit / Debit Card -->
                            <label class="payment-option-card" onclick="selectPaymentCard(this)">
                                <input type="radio" name="paymentMethod" value="CREDIT_CARD" style="accent-color: #2563eb; width: 20px; height: 20px; cursor: pointer;">
                                <div style="font-size: 1.6rem; width: 40px; text-align: center;">💳</div>
                                <div style="flex: 1; min-width: 0;">
                                    <strong style="font-size: 1.05rem; color: #0f172a;">Credit / Debit Card</strong>
                                    <div style="color: #64748b; font-size: 0.85rem; margin-top: 0.2rem;">
                                        Visa, MasterCard, RuPay, Diners Club &amp; American Express.
                                    </div>
                                </div>
                            </label>

                            <!-- Internet Banking -->
                            <label class="payment-option-card" onclick="selectPaymentCard(this)">
                                <input type="radio" name="paymentMethod" value="NET_BANKING" style="accent-color: #2563eb; width: 20px; height: 20px; cursor: pointer;">
                                <div style="font-size: 1.6rem; width: 40px; text-align: center;">🏦</div>
                                <div style="flex: 1; min-width: 0;">
                                    <strong style="font-size: 1.05rem; color: #0f172a;">Net Banking</strong>
                                    <div style="color: #64748b; font-size: 0.85rem; margin-top: 0.2rem;">
                                        HDFC, SBI, ICICI, Axis, Kotak, and 50+ Indian Banks.
                                    </div>
                                </div>
                            </label>

                        </div>
                    </div>

                </div>

                <!-- Right Column: Payment Breakdown & Place Order -->
                <aside style="height: fit-content; position: sticky; top: 5rem;">
                    <div style="background: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; padding: 1.75rem; box-shadow: 0 4px 20px rgba(0,0,0,0.05);">
                        <h3 style="font-size: 1.2rem; font-weight: 900; margin: 0 0 1.25rem 0; color: #0f172a; border-bottom: 2px solid #e2e8f0; padding-bottom: 0.75rem;">
                            Payment Breakdown
                        </h3>

                        <div style="display: flex; justify-content: space-between; margin-bottom: 0.65rem; font-size: 0.92rem; color: #475569;">
                            <span>Items Subtotal (${cart.totalQuantity} items):</span>
                            <span style="font-weight: 700; color: #0f172a;">₹<fmt:formatNumber value="${cart.subtotal}" minFractionDigits="2" /></span>
                        </div>

                        <c:if test="${cart.totalDiscount > 0}">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 0.65rem; font-size: 0.92rem; color: #16a34a; font-weight: 800;">
                                <span>⚡ Applied Discount:</span>
                                <span>-₹<fmt:formatNumber value="${cart.totalDiscount}" minFractionDigits="2" /></span>
                            </div>
                        </c:if>

                        <c:if test="${cart.couponDiscount > 0}">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 0.65rem; font-size: 0.92rem; color: #16a34a; font-weight: 800;">
                                <span>🏷️ Coupon Discount (${cart.appliedCouponCode}):</span>
                                <span>-₹<fmt:formatNumber value="${cart.couponDiscount}" minFractionDigits="2" /></span>
                            </div>
                        </c:if>

                        <div style="display: flex; justify-content: space-between; margin-bottom: 0.65rem; font-size: 0.92rem; color: #475569;">
                            <span>Estimated Tax (GST):</span>
                            <span style="font-weight: 700; color: #0f172a;">₹<fmt:formatNumber value="${cart.estimatedTax}" minFractionDigits="2" /></span>
                        </div>

                        <div style="display: flex; justify-content: space-between; margin-bottom: 0.65rem; font-size: 0.92rem; color: #475569;">
                            <span>Delivery Fee:</span>
                            <span style="color: #16a34a; font-weight: 800;">FREE</span>
                        </div>

                        <div style="display: flex; justify-content: space-between; border-top: 2px solid #e2e8f0; padding-top: 1rem; margin-top: 1rem; font-size: 1.35rem; font-weight: 900; color: #0f172a;">
                            <span>Grand Total:</span>
                            <span style="color: #2563eb;">₹<fmt:formatNumber value="${cart.grandTotal}" minFractionDigits="2" /></span>
                        </div>

                        <c:if test="${cart.totalSavings > 0}">
                            <div style="margin-top: 1rem; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 0.65rem 0.85rem; font-size: 0.85rem; font-weight: 800; color: #166534; text-align: center;">
                                🎉 You are saving ₹<fmt:formatNumber value="${cart.totalSavings}" minFractionDigits="2" /> on this order!
                            </div>
                        </c:if>

                        <!-- Place Order Button -->
                        <div style="margin-top: 1.5rem;">
                            <button type="submit" id="btnPlaceOrder" class="hero-cta-btn" style="width: 100%; padding: 1.05rem; font-size: 1.1rem; font-weight: 900; border-radius: 12px; cursor: pointer; border: none; background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: #ffffff; box-shadow: 0 4px 18px rgba(37, 99, 235, 0.35); display: flex; align-items: center; justify-content: center; gap: 0.5rem;">
                                <span>Place Order</span>
                            </button>
                        </div>

                        <div style="margin-top: 1rem; text-align: center; font-size: 0.78rem; color: #64748b;">
                            🛡️ Safe &amp; Secure 256-Bit SSL Encrypted Checkout
                        </div>
                    </div>
                </aside>

            </div>
        </form>

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

    <script>
        function selectPaymentCard(card) {
            document.querySelectorAll('.payment-option-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            const radio = card.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
        }
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
</body>
</html>
