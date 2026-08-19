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
    <title>Secure Payment | ShopKart</title>
    <meta name="description" content="Complete your secure ShopKart payment with UPI, Google Pay, PhonePe, Paytm, QR, or Cards.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- Razorpay Standard Checkout SDK -->
    <script src="https://checkout.razorpay.com/v1/checkout.js"></script>

    <style>
        *, *::before, *::after { box-sizing: border-box; }

        body {
            background: #f1f5f9;
            font-family: 'Outfit', 'Inter', -apple-system, sans-serif;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.5rem 1rem;
        }

        .gw-wrapper {
            width: 100%;
            max-width: 520px;
        }

        /* ── Header bar ── */
        .gw-header {
            background: #0f172a;
            color: #fff;
            border-radius: 16px 16px 0 0;
            padding: 1.25rem 1.5rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .gw-brand { font-size: 1.2rem; font-weight: 900; color: #f59e0b; letter-spacing: -0.5px; }
        .gw-brand small { display: block; font-size: 0.72rem; color: #94a3b8; font-weight: 500; margin-top: 2px; }
        .gw-amount { text-align: right; }
        .gw-amount span { display: block; font-size: 0.72rem; color: #94a3b8; font-weight: 600; }
        .gw-amount strong { font-size: 1.6rem; font-weight: 900; color: #38bdf8; }

        /* ── Card body ── */
        .gw-card {
            background: #ffffff;
            border-radius: 0 0 16px 16px;
            border: 1px solid #e2e8f0;
            border-top: none;
            box-shadow: 0 16px 36px -8px rgba(0,0,0,0.1);
            overflow: hidden;
        }

        /* ── Order summary pill ── */
        .gw-summary {
            background: #f8fafc;
            border-bottom: 1px solid #e2e8f0;
            padding: 0.85rem 1.5rem;
            font-size: 0.82rem;
            color: #64748b;
            display: flex;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 0.75rem;
        }
        .gw-summary span { display: flex; flex-direction: column; }
        .gw-summary span b { color: #0f172a; font-size: 0.88rem; font-weight: 700; }

        /* ── Launch Razorpay Real Checkout CTA ── */
        .rzp-quick-launch-box {
            padding: 1.25rem 1.5rem 0.5rem;
        }
        .rzp-launch-btn {
            width: 100%;
            padding: 0.95rem 1.25rem;
            background: linear-gradient(135deg, #0284c7, #0369a1);
            border: none;
            border-radius: 12px;
            font-size: 1rem;
            font-weight: 800;
            color: #ffffff;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.6rem;
            box-shadow: 0 4px 14px rgba(2, 132, 199, 0.35);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .rzp-launch-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(2, 132, 199, 0.45);
        }

        .rzp-divider-or {
            display: flex;
            align-items: center;
            text-align: center;
            margin: 1.25rem 1.5rem;
            color: #94a3b8;
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .rzp-divider-or::before, .rzp-divider-or::after {
            content: '';
            flex: 1;
            border-bottom: 1px solid #e2e8f0;
        }
        .rzp-divider-or span { padding: 0 0.75rem; }

        /* ── Tabs ── */
        .gw-tabs {
            display: flex;
            border-top: 1px solid #e2e8f0;
            border-bottom: 1px solid #e2e8f0;
            background: #fafafa;
        }
        .gw-tab {
            flex: 1;
            padding: 0.85rem 0.35rem;
            text-align: center;
            font-size: 0.78rem;
            font-weight: 700;
            color: #64748b;
            cursor: pointer;
            border-bottom: 3px solid transparent;
            transition: all 0.18s ease;
            user-select: none;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 4px;
        }
        .gw-tab .tab-icon { font-size: 1.25rem; }
        .gw-tab:hover { color: #0f172a; background: #f1f5f9; }
        .gw-tab.active {
            color: #0f172a;
            border-bottom-color: #f59e0b;
            background: #ffffff;
        }

        /* ── Tab panels ── */
        .gw-panel { display: none; padding: 1.5rem; }
        .gw-panel.active { display: block; }

        /* ── UPI Apps Grid ── */
        .upi-app-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 0.75rem;
            margin-bottom: 1.25rem;
        }
        .upi-app-btn {
            background: #ffffff;
            border: 1.5px solid #e2e8f0;
            border-radius: 10px;
            padding: 0.85rem 0.5rem;
            text-align: center;
            cursor: pointer;
            transition: all 0.18s ease;
            font-size: 0.78rem;
            font-weight: 700;
            color: #334155;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 0.35rem;
        }
        .upi-app-btn:hover {
            border-color: #f59e0b;
            background: #fffbeb;
            transform: translateY(-2px);
            box-shadow: 0 4px 10px rgba(245, 158, 11, 0.15);
        }
        .upi-app-btn.selected {
            border-color: #f59e0b;
            background: #fffbeb;
            color: #b45309;
            box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.2);
        }
        .upi-app-icon {
            font-size: 1.6rem;
        }

        /* ── Inputs ── */
        .gw-input-group { margin-bottom: 1.15rem; }
        .gw-label {
            display: block;
            font-size: 0.78rem;
            font-weight: 700;
            color: #334155;
            text-transform: uppercase;
            letter-spacing: 0.04em;
            margin-bottom: 0.35rem;
        }
        .gw-input {
            width: 100%;
            padding: 0.75rem 0.9rem;
            border: 1.5px solid #cbd5e1;
            border-radius: 8px;
            font-size: 0.92rem;
            font-family: inherit;
            color: #0f172a;
            background: #fff;
            outline: none;
            transition: border-color 0.18s ease;
        }
        .gw-input:focus {
            border-color: #f59e0b;
            box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15);
        }

        /* ── Quick VPA pills ── */
        .vpa-pills {
            display: flex;
            gap: 0.45rem;
            flex-wrap: wrap;
            margin-top: 0.4rem;
        }
        .vpa-pill {
            font-size: 0.72rem;
            font-weight: 700;
            padding: 0.2rem 0.55rem;
            border-radius: 9999px;
            background: #f1f5f9;
            color: #475569;
            cursor: pointer;
            border: 1px solid #e2e8f0;
            transition: all 0.15s ease;
        }
        .vpa-pill:hover { background: #e0f2fe; color: #0284c7; border-color: #7dd3fc; }

        /* ── Dynamic QR Code Box ── */
        .qr-box {
            text-align: center;
            padding: 1.25rem;
            background: #fafafa;
            border: 2px dashed #cbd5e1;
            border-radius: 12px;
            margin-bottom: 1.25rem;
        }
        .qr-image-wrapper {
            display: inline-block;
            background: #ffffff;
            padding: 0.75rem;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.06);
            margin: 0.5rem 0;
        }
        .qr-image-wrapper img {
            display: block;
            width: 160px;
            height: 160px;
        }

        /* ── Action buttons ── */
        .gw-pay-btn {
            width: 100%;
            padding: 0.9rem;
            background: linear-gradient(135deg, #f59e0b, #d97706);
            border: none;
            border-radius: 10px;
            font-size: 1rem;
            font-weight: 800;
            color: #fff;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            box-shadow: 0 4px 12px rgba(245,158,11,0.35);
            transition: all 0.2s ease;
        }
        .gw-pay-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(245,158,11,0.4); }

        .gw-fail-link {
            display: block;
            text-align: center;
            margin-top: 0.85rem;
            font-size: 0.78rem;
            color: #94a3b8;
            cursor: pointer;
            text-decoration: none;
        }
        .gw-fail-link:hover { color: #ef4444; }

        /* ── Sandbox badge ── */
        .sandbox-badge {
            background: #fef3c7;
            color: #92400e;
            font-size: 0.68rem;
            font-weight: 800;
            padding: 0.15rem 0.5rem;
            border-radius: 4px;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-left: 0.4rem;
            vertical-align: middle;
        }

        /* ── Test Mode Guidance Notice ── */
        .sandbox-guide-notice {
            background: #eff6ff;
            border-left: 4px solid #3b82f6;
            padding: 0.75rem 1rem;
            font-size: 0.78rem;
            color: #1e40af;
            border-radius: 6px;
            margin-bottom: 1.25rem;
            line-height: 1.45;
        }

        /* ── Security badges ── */
        .gw-security {
            border-top: 1px solid #f1f5f9;
            padding: 0.85rem 1.5rem;
            display: flex;
            justify-content: center;
            gap: 1.25rem;
            font-size: 0.72rem;
            color: #94a3b8;
            flex-wrap: wrap;
            background: #fafafa;
        }
        .gw-security span { display: flex; align-items: center; gap: 3px; }

        /* ── UPI AUTHORIZATION MODAL ── */
        .upi-modal-backdrop {
            position: fixed;
            inset: 0;
            background: rgba(15, 23, 42, 0.75);
            backdrop-filter: blur(4px);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
            padding: 1.5rem;
        }
        .upi-modal-card {
            background: #ffffff;
            border-radius: 18px;
            width: 100%;
            max-width: 440px;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
            overflow: hidden;
            animation: modalPop 0.25s cubic-bezier(0.16, 1, 0.3, 1);
        }
        @keyframes modalPop {
            0% { transform: scale(0.92); opacity: 0; }
            100% { transform: scale(1); opacity: 1; }
        }
        .upi-modal-header {
            background: #0f172a;
            color: #ffffff;
            padding: 1.5rem;
            text-align: center;
        }
        .pulse-phone-icon {
            font-size: 2.5rem;
            display: inline-block;
            position: relative;
            margin-bottom: 0.5rem;
            animation: pulseGlow 1.5s infinite;
        }
        @keyframes pulseGlow {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.08); }
        }
        .upi-notif-dot {
            position: absolute;
            top: 2px;
            right: -2px;
            width: 12px;
            height: 12px;
            background: #ef4444;
            border: 2px solid #0f172a;
            border-radius: 50%;
        }
        .upi-modal-body {
            padding: 1.5rem;
        }
        .upi-req-amount-box {
            text-align: center;
            background: #f0f9ff;
            border: 1px solid #bae6fd;
            border-radius: 12px;
            padding: 1rem;
            margin-bottom: 1.25rem;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .upi-step-box {
            background: #f8fafc;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            padding: 0.85rem 1rem;
            margin-bottom: 1.25rem;
        }
    </style>
</head>
<body>

<div class="gw-wrapper">

    <%-- Hidden Form for Processing Callback & Signature Verification --%>
    <form action="${pageContext.request.contextPath}/payment/callback"
          method="POST" id="paymentForm" style="display:none;">
        <input type="hidden" name="orderId"              value="${order.orderId}">
        <input type="hidden" name="transactionReference" id="txnRefField" value="${payment.transactionReference}">
        <input type="hidden" name="status"               id="statusField" value="SUCCESS">
        <input type="hidden" name="reason"               id="reasonField" value="">
        <input type="hidden" name="razorpay_payment_id"  id="rzpPaymentIdField" value="">
        <input type="hidden" name="razorpay_order_id"    id="rzpOrderIdField" value="${razorpayOrderId}">
        <input type="hidden" name="razorpay_signature"   id="rzpSignatureField" value="">
    </form>

    <%-- Header --%>
    <div class="gw-header">
        <div class="gw-brand">
            🛒 Razorpay Gateway
            <small>UPI &bull; VPA &bull; QR &bull; Cards <span class="sandbox-badge">Sandbox</span></small>
        </div>
        <div class="gw-amount">
            <span>Amount Due</span>
            <strong>₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/></strong>
        </div>
    </div>

    <div class="gw-card">

        <%-- Order summary --%>
        <div class="gw-summary">
            <span>Order Reference<b>#${order.orderNumber}</b></span>
            <span>Customer<b><c:out value="${sessionScope.currentUser.fullName}"/></b></span>
            <span>Items<b><c:out value="${order.totalItems}" default="1"/> Items</b></span>
        </div>

        <%-- PAYMENT FAILED ALERT / RETRY BANNER --%>
        <c:if test="${param.paymentFailed eq 'true' || not empty param.error || not empty error}">
            <div style="background: #fef2f2; border: 1.5px solid #f87171; border-radius: 12px; margin: 1.25rem 1.5rem 0.5rem; padding: 1rem 1.25rem; text-align: left; display: flex; gap: 0.85rem; align-items: flex-start; box-shadow: 0 4px 14px rgba(239, 68, 68, 0.12);">
                <span style="font-size: 1.6rem; line-height: 1; flex-shrink: 0;">❌</span>
                <div style="flex: 1;">
                    <strong style="color: #991b1b; font-size: 0.98rem; display: block; margin-bottom: 0.25rem;">Payment Failed &bull; Order Not Placed</strong>
                    <div style="font-size: 0.84rem; color: #b91c1c; line-height: 1.45;">
                        <c:choose>
                            <c:when test="${not empty param.error}"><c:out value="${param.error}" /></c:when>
                            <c:when test="${not empty error}"><c:out value="${error}" /></c:when>
                            <c:otherwise>Your online transaction was declined or cancelled. Your order has not been placed yet.</c:otherwise>
                        </c:choose>
                    </div>
                    <div style="font-size: 0.78rem; color: #7f1d1d; font-weight: 700; margin-top: 0.5rem; border-top: 1px dashed #fca5a5; padding-top: 0.4rem;">
                        🔄 Please choose a payment method below to retry your payment.
                    </div>
                </div>
            </div>
        </c:if>

        <%-- 1. REAL-TIME RAZORPAY STANDARD CHECKOUT MODAL LAUNCHER --%>
        <div class="rzp-quick-launch-box">
            <button type="button" class="rzp-launch-btn" id="launchRazorpayBtn" onclick="launchRazorpayCheckout()">
                <span>⚡ Open Razorpay Test Popup</span>
                <span style="font-size: 0.8rem; opacity: 0.9;">(UPI Intent, QR &amp; Cards) →</span>
            </button>
            <div style="margin-top: 0.75rem; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 0.65rem 0.85rem; font-size: 0.75rem; color: #166534; line-height: 1.45;">
                ✨ <b>Test Popup Credentials:</b><br>
                • <b>UPI / QR:</b> Enter <code>success@razorpay</code> or scan the test QR code.<br>
                • <b>Cards:</b> Number <code>4111 2222 3333 4444</code>, Expiry <code>12/28</code>, CVV <code>123</code>, OTP <code>123456</code>.<br>
                • <b>Server Verification:</b> Automatically verified with cryptographic <b>HMAC-SHA256</b> signature upon success.
            </div>
        </div>

        <div class="rzp-divider-or">
            <span>or use simulated sandbox channels</span>
        </div>

        <%-- 2. IN-APP SIMULATED TABS --%>
        <div class="gw-tabs">
            <div class="gw-tab active" onclick="switchTab('upi')">
                <span class="tab-icon">📱</span>
                <span>UPI Apps</span>
            </div>
            <div class="gw-tab" onclick="switchTab('vpa')">
                <span class="tab-icon">🏷️</span>
                <span>UPI ID / VPA</span>
            </div>
            <div class="gw-tab" onclick="switchTab('qr')">
                <span class="tab-icon">🔳</span>
                <span>Dynamic QR</span>
            </div>
            <div class="gw-tab" onclick="switchTab('card')">
                <span class="tab-icon">💳</span>
                <span>Cards</span>
            </div>
            <div class="gw-tab" onclick="switchTab('netbanking')">
                <span class="tab-icon">🏦</span>
                <span>Net Banking</span>
            </div>
        </div>

        <%-- PANEL 1: UPI APPS --%>
        <div class="gw-panel active" id="panel-upi">
            <div class="sandbox-guide-notice">
                💡 <b>UPI Intent Mode:</b> Select your preferred UPI application to send a collect request and authorize payment with your UPI PIN.
            </div>

            <div class="upi-app-grid">
                <div class="upi-app-btn selected" onclick="selectUpiApp(this, 'Google Pay')">
                    <span class="upi-app-icon">🟢</span>
                    <span>Google Pay</span>
                </div>
                <div class="upi-app-btn" onclick="selectUpiApp(this, 'PhonePe')">
                    <span class="upi-app-icon">🟣</span>
                    <span>PhonePe</span>
                </div>
                <div class="upi-app-btn" onclick="selectUpiApp(this, 'Paytm')">
                    <span class="upi-app-icon">🔵</span>
                    <span>Paytm UPI</span>
                </div>
                <div class="upi-app-btn" onclick="selectUpiApp(this, 'BHIM')">
                    <span class="upi-app-icon">🇮🇳</span>
                    <span>BHIM UPI</span>
                </div>
                <div class="upi-app-btn" onclick="selectUpiApp(this, 'CRED')">
                    <span class="upi-app-icon">💳</span>
                    <span>CRED UPI</span>
                </div>
                <div class="upi-app-btn" onclick="selectUpiApp(this, 'Amazon Pay')">
                    <span class="upi-app-icon">🟠</span>
                    <span>Amazon Pay</span>
                </div>
            </div>

            <button type="button" class="gw-pay-btn" id="upiPayBtn" onclick="openUpiWaitingModal(selectedUpiAppGlobal)">
                <span>🔒 Pay ₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/> via <span id="selectedUpiName">Google Pay</span></span>
            </button>
            <a class="gw-fail-link" onclick="submitSimulatedFailure('UPI transaction cancelled by user on mobile app')">
                Simulate Payment Decline / Cancel
            </a>
        </div>

        <%-- PANEL 2: UPI VPA COLLECT --%>
        <div class="gw-panel" id="panel-vpa">
            <div class="sandbox-guide-notice">
                💡 <b>UPI Collect Mode:</b> Enter your Virtual Payment Address (VPA) or use test VPAs below.
            </div>

            <div class="gw-input-group">
                <label class="gw-label" for="vpaInput">Enter UPI ID / VPA</label>
                <input type="text" class="gw-input" id="vpaInput" placeholder="username@okhdfcbank" value="success@razorpay">
                <div class="vpa-pills">
                    <span class="vpa-pill" onclick="setVpa('success@razorpay')">✓ success@razorpay</span>
                    <span class="vpa-pill" onclick="setVpa('failure@razorpay')">✕ failure@razorpay</span>
                    <span class="vpa-pill" onclick="setVpa('<c:out value="${not empty sessionScope.currentUser.phone ? sessionScope.currentUser.phone : order.shippingPhone}" default="9876543210"/>@paytm')">📱 <c:out value="${not empty sessionScope.currentUser.phone ? sessionScope.currentUser.phone : order.shippingPhone}" default="9876543210"/>@paytm</span>
                </div>
            </div>

            <button type="button" class="gw-pay-btn" onclick="submitVpaPayment()">
                <span>🔒 Request ₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/> via UPI VPA</span>
            </button>
            <a class="gw-fail-link" onclick="submitSimulatedFailure('UPI Collect request timed out or was rejected')">
                Simulate VPA Rejection
            </a>
        </div>

        <%-- PANEL 3: DYNAMIC QR CODE --%>
        <div class="gw-panel" id="panel-qr">
            <div class="sandbox-guide-notice">
                💡 <b>Dynamic UPI QR:</b> Scan this dynamic QR code with any UPI app (GPay, PhonePe, Paytm) to pay.
            </div>

            <div class="qr-box">
                <div style="font-size: 0.78rem; font-weight: 700; color: #475569;">
                    Scan with Any UPI App
                </div>
                <div class="qr-image-wrapper">
                    <img src="https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=upi://pay?pa=shopkart.sandbox@hdfcbank%26pn=ShopKart%26am=${order.totalAmount}%26cu=INR%26tn=Order-${order.orderNumber}" alt="Dynamic UPI QR Code">
                </div>
                <div style="font-size: 0.75rem; color: #64748b;">
                    Amount: <b style="color: #0f172a;">₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/></b> &bull; Expires in <span id="qrTimer" style="color: #0284c7; font-weight: 700;">05:00</span>
                </div>
            </div>

            <button type="button" class="gw-pay-btn" onclick="openUpiWaitingModal('Dynamic UPI QR Scan')">
                <span>📱 Simulate QR Scan &amp; Confirm Payment</span>
            </button>
        </div>

        <%-- PANEL 4: CARDS --%>
        <div class="gw-panel" id="panel-card">
            <div class="gw-input-group">
                <label class="gw-label">Card Number</label>
                <input type="text" class="gw-input" value="4111 2222 3333 4444" maxlength="19">
            </div>
            <div style="display: flex; gap: 0.75rem;">
                <div class="gw-input-group" style="flex: 1;">
                    <label class="gw-label">Valid Thru</label>
                    <input type="text" class="gw-input" value="12/28" placeholder="MM/YY" maxlength="5">
                </div>
                <div class="gw-input-group" style="flex: 1;">
                    <label class="gw-label">CVV</label>
                    <input type="password" class="gw-input" value="123" placeholder="CVV" maxlength="4">
                </div>
            </div>
            <button type="button" class="gw-pay-btn" onclick="openUpiWaitingModal('Credit / Debit Card (3D Secure)')">
                <span>🔒 Pay ₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/> via Card</span>
            </button>
            <a class="gw-fail-link" onclick="submitSimulatedFailure('Card declined: Insufficient funds or invalid OTP')">
                Simulate Card Decline
            </a>
        </div>

        <%-- PANEL 5: NETBANKING --%>
        <div class="gw-panel" id="panel-netbanking">
            <div class="gw-input-group">
                <label class="gw-label">Select Popular Bank</label>
                <select class="gw-input" id="bankSelect">
                    <option value="HDFC">HDFC Bank</option>
                    <option value="SBI">State Bank of India</option>
                    <option value="ICICI">ICICI Bank</option>
                    <option value="AXIS">Axis Bank</option>
                    <option value="KOTAK">Kotak Mahindra Bank</option>
                </select>
            </div>
            <button type="button" class="gw-pay-btn" onclick="openUpiWaitingModal('Net Banking (' + document.getElementById('bankSelect').value + ')')">
                <span>🔒 Proceed to Net Banking</span>
            </button>
            <a class="gw-fail-link" onclick="submitSimulatedFailure('Bank server unavailable or user aborted NetBanking')">
                Simulate Bank Abort
            </a>
        </div>

        <%-- Security Footer --%>
        <div class="gw-security">
            <span>🔒 256-Bit SSL Encrypted</span>
            <span>🛡️ PCI-DSS Level 1 Certified</span>
            <span>⚡ 100% Purchase Protection</span>
        </div>
    </div>

    <!-- GATEWAY FOOTER -->
    <footer style="text-align: center; margin-top: 1.5rem; font-size: 0.8rem; color: #64748b; line-height: 1.6;">
        <div>&copy; 2026 ShopKart Inc. All rights reserved. &bull; Razorpay Payment Gateway</div>
        <div style="margin-top: 0.25rem;">
            Designed, Developed &amp; Managed by <strong style="color: #0284c7; font-weight: 800;">Rahul Pawar</strong>
        </div>
    </footer>

</div>

<!-- UPI App Collect Authorization Modal -->
<div id="upiWaitingModal" class="upi-modal-backdrop" style="display: none;">
    <div class="upi-modal-card">
        <div class="upi-modal-header">
            <div class="pulse-phone-icon">📱<span class="upi-notif-dot"></span></div>
            <h3 style="font-size: 1.15rem; font-weight: 800; color: #ffffff; margin: 0 0 0.35rem;">
                Approve Payment Request
            </h3>
            <p style="font-size: 0.85rem; color: #94a3b8; margin: 0;">
                Collect request sent from <b>ShopKart India</b>
            </p>
        </div>

        <div class="upi-modal-body">
            <div class="upi-req-amount-box">
                <span style="font-size: 0.78rem; color: #64748b; font-weight: 600;">Amount to Authorize</span>
                <strong style="font-size: 1.6rem; color: #0284c7; font-weight: 900;">
                    ₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/>
                </strong>
                <span style="font-size: 0.78rem; color: #475569; margin-top: 0.35rem;">
                    Target App / VPA: <b id="modalTargetVpa" style="color: #0f172a;">Google Pay</b>
                </span>
            </div>

            <div class="upi-step-box">
                <div style="font-weight: 700; color: #334155; font-size: 0.82rem; margin-bottom: 0.35rem;">
                    📱 Steps on your Phone:
                </div>
                <ol style="margin: 0; padding-left: 1.2rem; font-size: 0.8rem; color: #64748b; line-height: 1.6;">
                    <li>Open your <b>UPI App</b> (Google Pay, PhonePe, Paytm, BHIM, CRED).</li>
                    <li>Check notifications for the payment request from <b>ShopKart</b>.</li>
                    <li>Enter your secret <b>UPI PIN</b> to confirm and authorize the payment.</li>
                </ol>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; background: #fffbeb; border: 1px solid #fef3c7; padding: 0.6rem 0.85rem; border-radius: 8px; font-size: 0.78rem; color: #92400e; margin-bottom: 1.25rem;">
                <span>⏱️ Request expires in:</span>
                <span id="upiModalTimer" style="font-weight: 800; font-size: 0.9rem; color: #b45309;">04:59</span>
            </div>

            <!-- Simulator Action Controls -->
            <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 1rem; margin-bottom: 1rem;">
                <div style="font-size: 0.72rem; font-weight: 800; color: #0284c7; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 0.6rem;">
                    🧪 Sandbox Simulator Actions:
                </div>
                <button type="button" class="gw-pay-btn" id="modalApproveBtn" onclick="confirmUpiAuthorizationSuccess()" style="padding: 0.8rem; font-size: 0.95rem; margin-bottom: 0.6rem;">
                    <span>✓ Simulate UPI PIN Entry &amp; Approve</span>
                </button>
                <button type="button" class="order-btn-outline" onclick="confirmUpiAuthorizationDecline()" style="width: 100%; padding: 0.6rem; font-size: 0.85rem; color: #ef4444; border-color: #fca5a5; background: #ffffff; cursor: pointer;">
                    <span>✕ Simulate Decline / Reject in UPI App</span>
                </button>
            </div>

            <div style="text-align: center;">
                <a onclick="closeUpiModal()" style="font-size: 0.8rem; color: #64748b; cursor: pointer; text-decoration: underline;">
                    Cancel Request &amp; Change Payment Method
                </a>
            </div>
        </div>
    </div>
</div>

<script>
    let selectedUpiAppGlobal = 'Google Pay';

    // Tab switching
    function switchTab(name) {
        document.querySelectorAll('.gw-tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.gw-panel').forEach(p => p.classList.remove('active'));

        const tabIdx = ['upi', 'vpa', 'qr', 'card', 'netbanking'].indexOf(name);
        if (tabIdx >= 0) {
            document.querySelectorAll('.gw-tab')[tabIdx].classList.add('active');
            document.getElementById('panel-' + name).classList.add('active');
        }
    }

    // UPI App Selection
    function selectUpiApp(el, name) {
        document.querySelectorAll('.upi-app-btn').forEach(b => b.classList.remove('selected'));
        el.classList.add('selected');
        selectedUpiAppGlobal = name;
        document.getElementById('selectedUpiName').textContent = name;
    }

    // Set VPA from test chip
    function setVpa(val) {
        document.getElementById('vpaInput').value = val;
    }

    // VPA Submit -> Opens Interactive Authorization Modal
    function submitVpaPayment() {
        const vpa = document.getElementById('vpaInput').value.trim();
        if (vpa.toLowerCase().includes('fail')) {
            submitSimulatedFailure('VPA Payment Failed for ' + vpa);
        } else {
            openUpiWaitingModal('UPI ID (' + vpa + ')');
        }
    }

    // Opens UPI Waiting Modal
    let modalTimerInterval = null;
    function openUpiWaitingModal(targetDesc) {
        document.getElementById('modalTargetVpa').textContent = targetDesc || selectedUpiAppGlobal;
        document.getElementById('upiWaitingModal').style.display = 'flex';
        
        let seconds = 299;
        const timerEl = document.getElementById('upiModalTimer');
        if (modalTimerInterval) clearInterval(modalTimerInterval);
        modalTimerInterval = setInterval(() => {
            seconds--;
            if (seconds <= 0) {
                clearInterval(modalTimerInterval);
                timerEl.textContent = 'Expired';
            } else {
                const m = String(Math.floor(seconds / 60)).padStart(2, '0');
                const s = String(seconds % 60).padStart(2, '0');
                timerEl.textContent = m + ':' + s;
            }
        }, 1000);
    }

    function closeUpiModal() {
        if (modalTimerInterval) clearInterval(modalTimerInterval);
        document.getElementById('upiWaitingModal').style.display = 'none';
    }

    function confirmUpiAuthorizationSuccess() {
        const approveBtn = document.getElementById('modalApproveBtn');
        if (approveBtn) {
            approveBtn.disabled = true;
            approveBtn.innerHTML = '<span>⏳ Verifying UPI PIN with Bank...</span>';
        }
        setTimeout(() => {
            submitSimulatedPayment(document.getElementById('modalTargetVpa').textContent);
        }, 1200);
    }

    function confirmUpiAuthorizationDecline() {
        closeUpiModal();
        submitSimulatedFailure('UPI collect request was rejected or declined by user.');
    }

    // Real-Time Razorpay Standard Checkout Modal
    function launchRazorpayCheckout() {
        const keyId = "${razorpayKeyId}";
        const amountPaise = ${razorpayAmountInPaise};
        const rzpOrderId = "${razorpayOrderId}";

        const options = {
            "key": keyId,
            "amount": amountPaise,
            "currency": "INR",
            "name": "ShopKart India",
            "description": "Order #${order.orderNumber}",
            "image": "${pageContext.request.contextPath}/assets/images/logo-dark.svg",
            "order_id": rzpOrderId,
            "prefill": {
                "name": "<c:out value='${sessionScope.currentUser.fullName}' default='Customer'/>",
                "email": "<c:out value='${sessionScope.currentUser.email}' default='customer@example.com'/>",
                "contact": "<c:out value='${not empty order.shippingPhone ? order.shippingPhone : sessionScope.currentUser.phone}' default='9876543210'/>"
            },
            "notes": {
                "ecommerce_order_id": "${order.orderId}",
                "order_number": "${order.orderNumber}"
            },
            "theme": {
                "color": "#f59e0b"
            },
            "handler": function (response) {
                // Cryptographic Response from Razorpay
                document.getElementById('rzpPaymentIdField').value = response.razorpay_payment_id;
                document.getElementById('rzpOrderIdField').value = response.razorpay_order_id;
                document.getElementById('rzpSignatureField').value = response.razorpay_signature;
                document.getElementById('txnRefField').value = response.razorpay_payment_id;
                document.getElementById('statusField').value = 'SUCCESS';
                document.getElementById('paymentForm').submit();
            },
            "modal": {
                "ondismiss": function() {
                    console.log('Razorpay checkout modal dismissed by user.');
                }
            }
        };

        try {
            const rzp = new Razorpay(options);
            rzp.on('payment.failed', function (response){
                document.getElementById('statusField').value = 'FAILED';
                document.getElementById('reasonField').value = response.error.description || 'Payment Failed';
                document.getElementById('paymentForm').submit();
            });
            rzp.open();
        } catch (e) {
            console.warn('Razorpay SDK initialization notice:', e);
            openUpiWaitingModal('Razorpay Simulated UPI Checkout');
        }
    }

    // Simulated Submission helpers
    function submitSimulatedPayment(method) {
        document.getElementById('statusField').value = 'SUCCESS';
        document.getElementById('reasonField').value = '';
        const simTxn = 'pay_sim_' + Math.random().toString(36).substring(2, 10).toUpperCase();
        document.getElementById('txnRefField').value = simTxn;
        document.getElementById('rzpPaymentIdField').value = simTxn;
        document.getElementById('paymentForm').submit();
    }

    function submitSimulatedFailure(reason) {
        document.getElementById('statusField').value = 'FAILED';
        document.getElementById('reasonField').value = reason;
        document.getElementById('paymentForm').submit();
    }

    // Countdown timer for QR code
    let qrSeconds = 300;
    const qrTimerEl = document.getElementById('qrTimer');
    if (qrTimerEl) {
        const interval = setInterval(() => {
            qrSeconds--;
            if (qrSeconds <= 0) {
                clearInterval(interval);
                qrTimerEl.textContent = 'Expired';
            } else {
                const m = String(Math.floor(qrSeconds / 60)).padStart(2, '0');
                const s = String(qrSeconds % 60).padStart(2, '0');
                qrTimerEl.textContent = m + ':' + s;
            }
        }, 1000);
    }
</script>

</body>
</html>
