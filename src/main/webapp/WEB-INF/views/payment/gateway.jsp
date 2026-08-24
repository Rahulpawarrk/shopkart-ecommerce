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
    <title>Secure Payment | ShopKart India</title>
    <meta name="description" content="Complete your secure payment with Razorpay. Supports UPI (Google Pay, PhonePe, Paytm, BHIM, CRED), Credit/Debit Cards, NetBanking, and Wallets.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- Official Razorpay Standard Checkout SDK -->
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
            margin: 0;
        }

        .gw-wrapper {
            width: 100%;
            max-width: 520px;
        }

        /* ── Header bar ── */
        .gw-header {
            background: #0f172a;
            color: #fff;
            border-radius: 20px 20px 0 0;
            padding: 1.5rem 1.75rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 4px 12px rgba(15, 23, 42, 0.15);
        }
        .gw-brand { 
            font-size: 1.25rem; 
            font-weight: 900; 
            color: #f59e0b; 
            letter-spacing: -0.5px; 
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        .gw-brand small { 
            display: block; 
            font-size: 0.75rem; 
            color: #94a3b8; 
            font-weight: 600; 
            margin-top: 3px; 
        }
        .gw-amount { text-align: right; }
        .gw-amount span { display: block; font-size: 0.72rem; color: #94a3b8; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
        .gw-amount strong { font-size: 1.75rem; font-weight: 900; color: #38bdf8; }

        /* ── Card body ── */
        .gw-card {
            background: #ffffff;
            border-radius: 0 0 20px 20px;
            border: 1px solid #e2e8f0;
            border-top: none;
            box-shadow: 0 20px 40px -12px rgba(0,0,0,0.12);
            overflow: hidden;
            padding-bottom: 1.5rem;
        }

        /* ── Order summary pill ── */
        .gw-summary {
            background: #f8fafc;
            border-bottom: 1px solid #e2e8f0;
            padding: 1rem 1.75rem;
            font-size: 0.85rem;
            color: #64748b;
            display: flex;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 0.75rem;
        }
        .gw-summary span { display: flex; flex-direction: column; }
        .gw-summary span b { color: #0f172a; font-size: 0.95rem; font-weight: 800; }

        /* ── Live payment box ── */
        .rzp-action-box {
            padding: 1.75rem 1.75rem 1rem;
            text-align: center;
        }

        .rzp-launch-btn {
            width: 100%;
            padding: 1.1rem 1.5rem;
            background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
            border: none;
            border-radius: 14px;
            font-size: 1.05rem;
            font-weight: 800;
            color: #ffffff;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.6rem;
            box-shadow: 0 6px 20px rgba(2, 132, 199, 0.35);
            transition: all 0.2s ease;
        }
        .rzp-launch-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(2, 132, 199, 0.45);
            background: linear-gradient(135deg, #0369a1 0%, #075985 100%);
        }

        .live-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
            background: #10b981;
            color: #ffffff;
            font-size: 0.68rem;
            font-weight: 800;
            padding: 0.2rem 0.55rem;
            border-radius: 9999px;
            letter-spacing: 0.05em;
            text-transform: uppercase;
        }

        /* Supported UPI Apps & Cards Showcase */
        .payment-methods-showcase {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 1.25rem 1rem;
            margin: 1.5rem 1.75rem 1rem;
            text-align: center;
        }
        .methods-title {
            font-size: 0.78rem;
            font-weight: 800;
            color: #475569;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 0.85rem;
        }
        .upi-icons-row {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 0.85rem;
            flex-wrap: wrap;
        }
        .upi-pill {
            background: #ffffff;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            padding: 0.35rem 0.65rem;
            font-size: 0.75rem;
            font-weight: 700;
            color: #334155;
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
            box-shadow: 0 1px 3px rgba(0,0,0,0.04);
        }

        /* ── Security Trust Footer ── */
        .gw-trust-bar {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 1.25rem;
            padding: 1rem 1.5rem 0;
            font-size: 0.75rem;
            color: #64748b;
            font-weight: 600;
            border-top: 1px solid #f1f5f9;
            margin-top: 1rem;
            flex-wrap: wrap;
        }
        .trust-item {
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
        }

        .secondary-actions {
            display: flex;
            justify-content: center;
            gap: 1rem;
            margin-top: 1.25rem;
            padding: 0 1.75rem;
            flex-wrap: wrap;
        }
        .btn-link {
            color: #64748b;
            font-size: 0.85rem;
            font-weight: 700;
            text-decoration: none;
            transition: color 0.15s ease;
        }
        .btn-link:hover {
            color: #0f172a;
            text-decoration: underline;
        }
    </style>
</head>
<body>

<div class="gw-wrapper">

    <%-- Hidden Form for Processing Secure Razorpay Callback & Cryptographic Verification --%>
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
        <div>
            <div class="gw-brand">
                <span>🛒 ShopKart</span>
                <c:choose>
                    <c:when test="${isRazorpayLive}">
                        <span class="live-badge">✓ Live Gateway</span>
                    </c:when>
                    <c:otherwise>
                        <span class="live-badge" style="background:#f59e0b;">Test Mode</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <small>Razorpay Enterprise Payment Gateway</small>
        </div>
        <div class="gw-amount">
            <span>Payable Amount</span>
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
            <div style="background: #fef2f2; border: 1.5px solid #f87171; border-radius: 12px; margin: 1.25rem 1.75rem 0; padding: 1rem 1.25rem; text-align: left; display: flex; gap: 0.85rem; align-items: flex-start; box-shadow: 0 4px 14px rgba(239, 68, 68, 0.12);">
                <span style="font-size: 1.6rem; line-height: 1; flex-shrink: 0;">❌</span>
                <div style="flex: 1;">
                    <strong style="color: #991b1b; font-size: 0.98rem; display: block; margin-bottom: 0.25rem;">Payment Incomplete</strong>
                    <div style="font-size: 0.84rem; color: #b91c1c; line-height: 1.45;">
                        <c:choose>
                            <c:when test="${not empty param.error}"><c:out value="${param.error}" /></c:when>
                            <c:when test="${not empty error}"><c:out value="${error}" /></c:when>
                            <c:otherwise>Your online transaction was cancelled or declined. Please click below to try again.</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </c:if>

        <%-- 1. REAL-TIME RAZORPAY STANDARD CHECKOUT LAUNCHER --%>
        <div class="rzp-action-box">
            <button type="button" class="rzp-launch-btn" id="launchRazorpayBtn" onclick="launchRazorpayCheckout()">
                <span>🔒 Pay ₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/> with Razorpay Secure</span>
            </button>
            <div style="margin-top: 0.85rem; font-size: 0.82rem; color: #64748b;">
                Click above to open the secure Razorpay payment window
            </div>
        </div>

        <%-- Supported Payment Channels Showcase --%>
        <div class="payment-methods-showcase">
            <div class="methods-title">Supported Live Payment Methods</div>
            <div class="upi-icons-row">
                <span class="upi-pill"><span>🟢</span> Google Pay</span>
                <span class="upi-pill"><span>🟣</span> PhonePe</span>
                <span class="upi-pill"><span>🔵</span> Paytm UPI</span>
                <span class="upi-pill"><span>🇮🇳</span> BHIM UPI</span>
                <span class="upi-pill"><span>💳</span> CRED UPI</span>
                <span class="upi-pill"><span>📱</span> Dynamic QR</span>
                <span class="upi-pill"><span>💳</span> Debit / Credit Cards</span>
                <span class="upi-pill"><span>🏦</span> Net Banking (50+ Banks)</span>
            </div>
        </div>

        <!-- Secondary Actions (Switch to COD or return to cart) -->
        <div class="secondary-actions">
            <form action="${pageContext.request.contextPath}/payment/failure/action" method="POST" style="margin:0; display:inline;">
                <input type="hidden" name="action" value="switch_cod">
                <input type="hidden" name="orderId" value="${order.orderId}">
                <button type="submit" style="background:none; border:none; color:#059669; font-weight:800; font-size:0.85rem; cursor:pointer; text-decoration:underline;">
                    💵 Switch to Cash on Delivery (COD)
                </button>
            </form>
            <span style="color:#cbd5e1;">&bull;</span>
            <a href="${pageContext.request.contextPath}/cart" class="btn-link">
                🛒 Cancel &amp; Back to Cart
            </a>
        </div>

        <!-- Trust & Security Badges -->
        <div class="gw-trust-bar">
            <div class="trust-item"><span>🔒</span> 256-Bit SSL Encrypted</div>
            <div class="trust-item"><span>🛡️</span> PCI-DSS Level 1 Certified</div>
            <div class="trust-item"><span>⚡</span> 100% Purchase Protection</div>
        </div>

    </div>

</div>

<script nonce="${cspNonce}">
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
                "color": "#0284c7"
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
                    console.log('Razorpay checkout popup dismissed by user.');
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
            console.error('Razorpay SDK initialization error:', e);
            alert('Unable to open Razorpay payment popup. Please ensure popups are allowed or click the payment button again.');
        }
    }

    // Auto-launch Razorpay Checkout popup upon page load
    window.addEventListener('DOMContentLoaded', () => {
        // Auto launch with a small delay for smooth DOM render
        setTimeout(() => {
            launchRazorpayCheckout();
        }, 500);
    });
</script>

</body>
</html>
