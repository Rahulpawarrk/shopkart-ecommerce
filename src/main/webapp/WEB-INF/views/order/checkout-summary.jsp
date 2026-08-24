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
    <title>Order Summary & Coupons | Step 2 of 3 | ShopKart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
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
        .step-bubble.completed {
            background: #10b981;
            color: #ffffff;
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
        .checkout-layout {
            display: grid;
            grid-template-columns: 1.8fr 1.2fr;
            gap: 2rem;
        }
        @media (max-width: 900px) {
            .checkout-layout {
                grid-template-columns: 1fr;
            }
        }
        .order-item-card {
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 1rem 1.25rem;
            margin-bottom: 0.85rem;
            background: #ffffff;
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
        
        <c:set var="summaryReturnUrl" value="/checkout/summary" />

        <!-- Progress Stepper: Step 2 Active -->
        <div class="checkout-stepper">
            <a href="${pageContext.request.contextPath}/checkout/address" class="step-item" style="text-decoration: none;">
                <div class="step-bubble completed">✓</div>
                <span style="color: #10b981; font-size: 0.95rem; font-weight: 700;">1. Delivery Address</span>
            </a>
            <div class="step-divider"></div>
            <div class="step-item">
                <div class="step-bubble active">2</div>
                <strong style="color: #0f172a; font-size: 0.95rem;">Order Summary</strong>
            </div>
            <div class="step-divider"></div>
            <div class="step-item">
                <div class="step-bubble inactive">3</div>
                <span style="color: #64748b; font-size: 0.95rem; font-weight: 600;">Payment</span>
            </div>
        </div>

        <!-- Coupon Apply & Remove Forms -->
        <form id="couponApplyForm" action="${pageContext.request.contextPath}/coupon/apply" method="POST">
            <input type="hidden" name="returnUrl" value="${summaryReturnUrl}">
            <c:if test="${isDirectBuy}">
                <input type="hidden" name="buyNowProductId" value="${directBuyProductId}">
                <input type="hidden" name="quantity" value="${directBuyQuantity}">
            </c:if>
        </form>

        <form id="couponRemoveForm" action="${pageContext.request.contextPath}/coupon/remove" method="POST">
            <input type="hidden" name="returnUrl" value="${summaryReturnUrl}">
        </form>

        <!-- Flash & Notification Messages -->
        <c:if test="${not empty error}">
            <div style="background: #fee2e2; border-left: 4px solid var(--danger); padding: 1rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #991b1b; font-weight: 700;">
                ⚠️ <c:out value="${error}" />
            </div>
        </c:if>
        <c:if test="${param.couponApplied == 'true'}">
            <div style="background: #dcfce7; border-left: 4px solid var(--success); padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #15803d; font-weight: 700;">
                ✓ Coupon applied successfully! Discount has been deducted from your order total.
            </div>
        </c:if>
        <c:if test="${param.couponRemoved == 'true'}">
            <div style="background: #f1f5f9; border-left: 4px solid #94a3b8; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; color: #475569; font-weight: 600;">
                ℹ️ Coupon removed from your order.
            </div>
        </c:if>

        <div class="checkout-layout">
            
            <!-- Left Column: Details & Items -->
            <div>
                
                <!-- Selected Delivery Destination Review Box -->
                <div style="background: #ffffff; border-radius: 14px; border: 1px solid #e2e8f0; padding: 1.5rem 1.75rem; margin-bottom: 1.5rem; box-shadow: 0 2px 10px rgba(0,0,0,0.02);">
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1rem;">
                        <h2 style="font-size: 1.15rem; font-weight: 900; color: #0f172a; margin: 0; display: flex; align-items: center; gap: 0.5rem;">
                            <span>📍 Delivery Location</span>
                        </h2>
                        <a href="${pageContext.request.contextPath}/checkout/address" style="font-size: 0.85rem; font-weight: 800; color: #2563eb; text-decoration: none; border: 1px solid #bfdbfe; padding: 0.35rem 0.75rem; border-radius: 6px; background: #eff6ff;">
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

                <!-- Order Items Review Box -->
                <div style="background: #ffffff; border-radius: 14px; border: 1px solid #e2e8f0; padding: 1.75rem; margin-bottom: 1.5rem; box-shadow: 0 2px 10px rgba(0,0,0,0.02);">
                    <h2 style="font-size: 1.15rem; font-weight: 900; color: #0f172a; margin: 0 0 1rem 0; border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem;">
                        🛒 Item(s)
                    </h2>

                    <div class="order-items-list">
                        <c:forEach var="item" items="${cart.items}">
                            <div class="order-item-card" style="display: flex; align-items: center; gap: 1rem;">
                                <img src="${not empty item.primaryImageUrl ? item.primaryImageUrl : 'https://placehold.co/75x75?text=Product'}" alt="${item.productName}" style="width: 70px; height: 70px; object-fit: contain; border-radius: 8px; border: 1px solid #e2e8f0; background: #fff; flex-shrink: 0;">
                                
                                <div style="flex: 1; min-width: 0;">
                                    <h4 style="font-size: 0.95rem; font-weight: 800; color: #0f172a; margin: 0 0 0.25rem 0; line-height: 1.35;">
                                        <c:out value="${item.productName}" />
                                    </h4>
                                    <div style="font-size: 0.82rem; color: #64748b;">
                                        <c:if test="${not empty item.sku}"><span>SKU: ${item.sku}</span> &bull; </c:if>
                                        <span>Qty: <strong style="color: #0f172a;">${item.quantity}</strong></span>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <!-- Promotional Coupon Section -->
                <div style="background: #ffffff; border-radius: 14px; border: 1px solid #e2e8f0; padding: 1.75rem; margin-bottom: 1.5rem; box-shadow: 0 2px 10px rgba(0,0,0,0.02); ${not empty cart.appliedCouponCode ? 'border: 2px solid #86efac; background: #f0fdf4;' : ''}">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; flex-wrap: wrap; gap: 0.5rem;">
                        <h2 style="font-size: 1.15rem; font-weight: 900; color: #0f172a; margin: 0;">
                            🏷️ Apply Promotional Coupon
                        </h2>
                        <c:if test="${not empty cart.appliedCouponCode}">
                            <button type="submit" form="couponRemoveForm" class="btn btn-sm btn-danger" style="padding: 0.35rem 0.75rem; font-size: 0.78rem; font-weight: 700; border-radius: 6px; cursor: pointer; background: #ef4444; color: #fff; border: none;">
                                ✕ Remove Coupon
                            </button>
                        </c:if>
                    </div>

                    <c:choose>
                        <c:when test="${not empty cart.appliedCouponCode}">
                            <div style="margin-top: 0.75rem;">
                                <div style="display: flex; align-items: center; gap: 0.6rem;">
                                    <span style="font-size: 1.4rem;">🎉</span>
                                    <div>
                                        <strong style="font-size: 1.05rem; color: #15803d;">Coupon '${cart.appliedCouponCode}' Applied!</strong>
                                        <div style="font-size: 0.88rem; color: #166534; margin-top: 0.2rem;">
                                            You are saving <strong>₹<fmt:formatNumber value="${cart.couponDiscount}" minFractionDigits="2"/></strong> on this order!
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p style="color: #64748b; font-size: 0.88rem; margin-bottom: 1rem;">
                                Enter your promo code to get instant discounts on your order.
                            </p>

                            <c:if test="${not empty param.couponError}">
                                <div style="background: #fee2e2; border-left: 4px solid var(--danger); padding: 0.65rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1rem; color: #991b1b; font-size: 0.88rem; font-weight: 700;">
                                    ⚠️ <c:out value="${param.couponError}" />
                                </div>
                            </c:if>

                            <!-- Coupon Input Form Row -->
                            <div style="display: flex; gap: 0.75rem; align-items: center; max-width: 480px; margin-bottom: 1rem;">
                                <input type="text" name="couponCode" form="couponApplyForm" id="summaryCouponInput" placeholder="Enter coupon (e.g. SAVE20)" class="form-input" style="flex: 1; padding: 0.65rem 1rem; border: 1.5px solid #cbd5e1; border-radius: 8px; font-size: 0.92rem; text-transform: uppercase; font-weight: 700;" required autocomplete="off">
                                <button type="submit" form="couponApplyForm" class="hero-cta-btn" style="padding: 0.65rem 1.25rem; font-size: 0.88rem; border-radius: 8px; border: none; cursor: pointer; white-space: nowrap; font-weight: 800;">
                                    Apply
                                </button>
                            </div>

                            <!-- Available Coupon Chips -->
                            <div style="display: flex; gap: 0.6rem; flex-wrap: wrap; align-items: center;">
                                <span style="font-size: 0.78rem; font-weight: 700; color: #64748b;">Available Deals:</span>
                                <button type="button" class="coupon-chip" onclick="applySummaryCoupon('SAVE20')" style="background: #eff6ff; color: #1d4ed8; border: 1px dashed #93c5fd; padding: 0.35rem 0.75rem; border-radius: 6px; font-size: 0.78rem; font-weight: 800; cursor: pointer; transition: all 0.2s;">
                                    🏷️ SAVE20 (20% Off)
                                </button>
                                <button type="button" class="coupon-chip" onclick="applySummaryCoupon('FLAT300')" style="background: #eff6ff; color: #1d4ed8; border: 1px dashed #93c5fd; padding: 0.35rem 0.75rem; border-radius: 6px; font-size: 0.78rem; font-weight: 800; cursor: pointer; transition: all 0.2s;">
                                    🏷️ FLAT300 (₹300 Off)
                                </button>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Delivery Instructions Form Card -->
                <div style="background: #ffffff; border-radius: 14px; border: 1px solid #e2e8f0; padding: 1.5rem 1.75rem; box-shadow: 0 2px 10px rgba(0,0,0,0.02);">
                    <h2 style="font-size: 1.15rem; font-weight: 900; color: #0f172a; margin-bottom: 0.5rem;">
                        📝 Delivery Instructions (Optional)
                    </h2>
                    <textarea name="notes" id="deliveryNotesInput" rows="2" class="form-input" style="width: 100%; padding: 0.75rem 1rem; border: 1px solid #cbd5e1; border-radius: var(--radius-md); font-family: inherit; font-size: 0.9rem;" placeholder="e.g. Please leave parcel with security guard or call upon doorstep arrival"><c:out value="${sessionScope.checkoutNotes}" /></textarea>
                </div>

            </div>

            <!-- Right Column: Sticky Summary & Proceed to Payment -->
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

                    <!-- Proceed to Step 3: Payment Button -->
                    <form id="proceedToPaymentForm" action="${pageContext.request.contextPath}/checkout/summary" method="POST">
                        <input type="hidden" name="addressId" value="${selectedAddress.addressId}">
                        <c:if test="${isDirectBuy}">
                            <input type="hidden" name="buyNowProductId" value="${directBuyProductId}">
                            <input type="hidden" name="quantity" value="${directBuyQuantity}">
                        </c:if>
                        <input type="hidden" name="notes" id="hiddenNotesInput" value="">

                        <button type="button" onclick="submitToPayment()" class="hero-cta-btn" style="width: 100%; margin-top: 1.5rem; padding: 1rem; font-size: 1.05rem; font-weight: 900; border-radius: 10px; cursor: pointer; border: none; background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: #ffffff; box-shadow: 0 4px 15px rgba(37, 99, 235, 0.35); display: flex; align-items: center; justify-content: center; gap: 0.5rem;">
                            <span>Proceed to Payment &rarr;</span>
                        </button>
                    </form>

                    <div style="margin-top: 1rem; text-align: center; font-size: 0.78rem; color: #64748b;">
                        🛡️ Safe &amp; Secure Checkout &bull; 100% Purchase Protection
                    </div>
                </div>
            </aside>

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
        function applySummaryCoupon(code) {
            const input = document.getElementById('summaryCouponInput');
            if (input) {
                input.value = code;
                input.focus();
            }
        }

        function submitToPayment() {
            const notesEl = document.getElementById('deliveryNotesInput');
            if (notesEl) {
                document.getElementById('hiddenNotesInput').value = notesEl.value;
            }
            document.getElementById('proceedToPaymentForm').submit();
        }
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/ecommerce.js"></script>
</body>
</html>
