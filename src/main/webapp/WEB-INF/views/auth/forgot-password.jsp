<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <!-- Favicon -->
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.svg">
    <link rel="apple-touch-icon" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password | ShopKart India</title>
    <meta name="description" content="Reset your ShopKart account password securely via Email or Mobile.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        .auth-page-wrapper {
            min-height: 100vh;
            background-color: #f8fafc;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 2rem 1rem;
        }
        .auth-card {
            width: 100%;
            max-width: 440px;
            background: #ffffff;
            border: 1px solid #d5d9d9;
            border-radius: var(--radius-md);
            padding: 2rem;
            box-shadow: 0 4px 16px rgba(0,0,0,0.05);
        }
        .auth-card-title {
            font-size: 1.5rem;
            font-weight: 800;
            color: #111827;
            margin-bottom: 0.35rem;
        }
        .auth-subtitle {
            color: #6b7280;
            font-size: 0.875rem;
            margin: 0 0 1.25rem;
            line-height: 1.4;
        }

        /* ── Reset Method Tabs ── */
        .method-tabs {
            display: flex;
            background: #f1f5f9;
            border-radius: var(--radius-sm);
            padding: 4px;
            margin-bottom: 1.25rem;
            gap: 4px;
        }
        .method-tab {
            flex: 1;
            padding: 0.6rem 0.5rem;
            text-align: center;
            font-size: 0.85rem;
            font-weight: 700;
            color: #64748b;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.15s ease;
            user-select: none;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.35rem;
        }
        .method-tab:hover { color: #0f172a; }
        .method-tab.active {
            background: #ffffff;
            color: #0f172a;
            box-shadow: 0 2px 6px rgba(0,0,0,0.08);
        }

        .auth-form-label {
            display: block;
            font-size: 0.85rem;
            font-weight: 700;
            color: #111827;
            margin-bottom: 0.35rem;
        }
        .auth-form-input {
            width: 100%;
            padding: 0.65rem 0.85rem;
            border: 1px solid #888c8c;
            border-radius: var(--radius-sm);
            font-size: 0.95rem;
            font-family: inherit;
            outline: none;
            transition: var(--transition-fast);
            box-sizing: border-box;
        }
        .auth-form-input:focus {
            border-color: var(--amazon-orange);
            box-shadow: 0 0 0 3px rgba(250,137,0,0.2);
        }
        .btn-auth-submit {
            width: 100%;
            background: linear-gradient(180deg, var(--amazon-yellow) 0%, #f0c14b 100%);
            border: 1px solid #a88734;
            border-radius: var(--radius-sm);
            padding: 0.75rem;
            font-size: 0.95rem;
            font-weight: 700;
            color: #111827;
            cursor: pointer;
            margin-top: 1rem;
            transition: var(--transition-fast);
        }
        .btn-auth-submit:hover {
            background: linear-gradient(180deg, #f5d378 0%, #eeb933 100%);
        }
        .btn-auth-submit:disabled {
            opacity: 0.7;
            cursor: not-allowed;
        }
        .alert-success {
            background: #dcfce7;
            border: 1px solid #86efac;
            color: #15803d;
            border-radius: var(--radius-sm);
            padding: 0.85rem 1rem;
            font-size: 0.88rem;
            margin-bottom: 1.25rem;
            line-height: 1.5;
        }
        .alert-error {
            background: #fee2e2;
            border: 1px solid #fca5a5;
            color: #b91c1c;
            border-radius: var(--radius-sm);
            padding: 0.75rem 1rem;
            font-size: 0.85rem;
            margin-bottom: 1.25rem;
        }
        .back-link {
            display: block;
            text-align: center;
            margin-top: 1.25rem;
            font-size: 0.85rem;
            color: var(--primary);
            text-decoration: none;
        }
        .back-link:hover { text-decoration: underline; }
        .steps-hint {
            background: #f0f9ff;
            border: 1px solid #bae6fd;
            border-radius: var(--radius-sm);
            padding: 0.85rem 1rem;
            font-size: 0.8rem;
            color: #0369a1;
            margin-bottom: 1.25rem;
            line-height: 1.5;
        }
        .steps-hint ol {
            margin: 0.4rem 0 0 1rem;
            padding: 0;
        }
        .steps-hint li { margin-bottom: 0.2rem; }

        .otp-input-box {
            font-size: 1.4rem;
            letter-spacing: 8px;
            text-align: center;
            font-weight: 800;
            font-family: monospace;
            padding: 0.75rem;
            border: 2px solid #888c8c;
            border-radius: var(--radius-sm);
            width: 100%;
            box-sizing: border-box;
            outline: none;
            transition: var(--transition-fast);
        }
        .otp-input-box:focus {
            border-color: var(--amazon-orange);
            box-shadow: 0 0 0 3px rgba(250,137,0,0.25);
        }
        .resend-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 1rem;
            font-size: 0.82rem;
        }
        .resend-link {
            color: var(--primary);
            text-decoration: none;
            background: none;
            border: none;
            cursor: pointer;
            padding: 0;
            font-size: 0.82rem;
            font-family: inherit;
        }
        .resend-link:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="auth-page-wrapper">
        <!-- Logo -->
        <a href="${pageContext.request.contextPath}/" style="text-decoration:none; margin-bottom:1.5rem; display:inline-block;" title="ShopKart">
            <img src="${pageContext.request.contextPath}/assets/images/logo-dark.svg" alt="ShopKart" style="height:48px; width:auto; display:block;">
        </a>

        <div class="auth-card">
            <h1 class="auth-card-title">Forgot Password?</h1>

            <c:if test="${not empty error}">
                <div class="alert-error">⚠️ <c:out value="${error}"/></div>
            </c:if>

            <c:choose>
                <%-- State 2: OTP Generated on Same Page — Show OTP input field --%>
                <c:when test="${otpSent}">
                    <div class="alert-success">
                        <strong>✓ 6-Digit OTP Sent!</strong><br>
                        <c:choose>
                            <c:when test="${methodType == 'PHONE'}">
                                A 6-digit code has been dispatched to <strong>+91-<c:out value="${destination}"/></strong>.
                            </c:when>
                            <c:otherwise>
                                A 6-digit code has been sent to <strong><c:out value="${destination}"/></strong>.
                            </c:otherwise>
                        </c:choose>
                        Valid for <strong>10 minutes</strong>.
                    </div>

                    <form action="${pageContext.request.contextPath}/forgot-password" method="POST" id="otpVerifyForm">
                        <input type="hidden" name="action" value="verify_otp">
                        <input type="hidden" name="identifier" value="<c:out value='${rawIdentifier != null ? rawIdentifier : identifier}'/>">
                        <input type="hidden" name="destination" value="<c:out value='${destination}'/>">

                        <div style="margin-bottom: 1.25rem;">
                            <label for="otpCode" class="auth-form-label">Enter 6-Digit Verification OTP *</label>
                            <input type="text" id="otpCode" name="otpCode" class="otp-input-box"
                                   required maxlength="6" pattern="[0-9]{6}"
                                   placeholder="••••••" autocomplete="one-time-code" autofocus>
                        </div>

                        <button type="submit" class="btn-auth-submit" id="verifySubmitBtn">
                            Submit OTP &amp; Proceed →
                        </button>
                    </form>

                    <div class="resend-row">
                        <!-- Resend OTP Form -->
                        <form action="${pageContext.request.contextPath}/forgot-password" method="POST" style="display:inline;" id="resendForm">
                            <input type="hidden" name="action" value="send_otp">
                            <input type="hidden" name="identifier" value="<c:out value='${rawIdentifier != null ? rawIdentifier : identifier}'/>">
                            <button type="submit" class="resend-link" id="resendBtn">
                                🔄 Resend OTP
                            </button>
                        </form>

                        <a href="${pageContext.request.contextPath}/forgot-password" class="resend-link" style="color:#64748b;">
                            ← Change email/mobile
                        </a>
                    </div>
                </c:when>

                <%-- State 1: Select Email or Mobile and request OTP --%>
                <c:otherwise>
                    <p class="auth-subtitle">
                        Select and enter your registered email address or mobile number to receive a 6-digit OTP to reset your password.
                    </p>

                    <!-- Dual Method Tabs -->
                    <div class="method-tabs" role="tablist">
                        <div class="method-tab active" id="tabEmail" onclick="selectMethod('email')">
                            ✉️ Email Address
                        </div>
                        <div class="method-tab" id="tabPhone" onclick="selectMethod('phone')">
                            📱 Mobile Number
                        </div>
                    </div>

                    <div class="steps-hint" id="hintBox">
                        <strong>Reset via Email OTP:</strong>
                        <ol>
                            <li>Enter your registered email address</li>
                            <li>Receive a 6-digit OTP code on your email</li>
                            <li>Submit OTP &amp; set your new password</li>
                        </ol>
                    </div>

                    <form action="${pageContext.request.contextPath}/forgot-password" method="POST" id="forgotForm" novalidate>
                        <input type="hidden" name="action" value="send_otp">

                        <!-- Email Input Group -->
                        <div id="emailGroup" style="margin-bottom:1.25rem;">
                            <label for="emailInput" class="auth-form-label">Registered Email Address *</label>
                            <input type="email" id="emailInput" name="identifier" class="auth-form-input"
                                   placeholder="you@example.com" autofocus
                                   value="<c:out value='${identifier}'/>">
                        </div>

                        <!-- Mobile Input Group (hidden by default) -->
                        <div id="phoneGroup" style="margin-bottom:1.25rem; display:none;">
                            <label for="phoneInput" class="auth-form-label">Registered 10-Digit Mobile Number *</label>
                            <input type="tel" id="phoneInput" class="auth-form-input"
                                   placeholder="10-digit mobile (e.g. 9876543210)"
                                   maxlength="10" pattern="[0-9]{10}">
                        </div>

                        <button type="submit" class="btn-auth-submit" id="forgotSubmitBtn">
                            Send 6-Digit OTP Code →
                        </button>
                    </form>
                </c:otherwise>
            </c:choose>

            <a href="${pageContext.request.contextPath}/login" class="back-link">
                ← Back to Sign In
            </a>
        </div>

        <footer style="margin-top: 2rem; font-size: 0.8rem; color: #9ca3af; text-align: center; line-height: 1.6;">
            <div>&copy; 2026 ShopKart Inc. &bull; All rights reserved.</div>
            <div style="margin-top: 0.25rem; color: #6b7280;">
                Designed, Developed &amp; Managed by <strong style="color: #2563eb; font-weight: 700;">Rahul Pawar</strong>
            </div>
        </footer>
    </div>

    <script>
        let currentMethod = 'email';

        function selectMethod(method) {
            currentMethod = method;
            const tabEmail = document.getElementById('tabEmail');
            const tabPhone = document.getElementById('tabPhone');
            const emailGroup = document.getElementById('emailGroup');
            const phoneGroup = document.getElementById('phoneGroup');
            const emailInput = document.getElementById('emailInput');
            const phoneInput = document.getElementById('phoneInput');
            const hintBox = document.getElementById('hintBox');

            if (method === 'email') {
                tabEmail.classList.add('active');
                tabPhone.classList.remove('active');
                emailGroup.style.display = 'block';
                phoneGroup.style.display = 'none';
                emailInput.name = 'identifier';
                phoneInput.name = '';
                emailInput.focus();
                hintBox.innerHTML = '<strong>Reset via Email OTP:</strong>' +
                    '<ol>' +
                    '<li>Enter your registered email address</li>' +
                    '<li>Receive a 6-digit OTP code on your email</li>' +
                    '<li>Submit OTP &amp; set your new password</li>' +
                    '</ol>';
            } else {
                tabPhone.classList.add('active');
                tabEmail.classList.remove('active');
                phoneGroup.style.display = 'block';
                emailGroup.style.display = 'none';
                phoneInput.name = 'identifier';
                emailInput.name = '';
                phoneInput.focus();
                hintBox.innerHTML = '<strong>Reset via Mobile SMS OTP:</strong>' +
                    '<ol>' +
                    '<li>Enter your registered 10-digit mobile number</li>' +
                    '<li>Receive a 6-digit SMS OTP code on your phone</li>' +
                    '<li>Submit OTP &amp; set your new password</li>' +
                    '</ol>';
            }
        }

        // Form submission handling for initial send
        const form = document.getElementById('forgotForm');
        const btn  = document.getElementById('forgotSubmitBtn');
        if (form && btn) {
            form.addEventListener('submit', function(e) {
                const emailVal = document.getElementById('emailInput')?.value.trim();
                const phoneVal = document.getElementById('phoneInput')?.value.trim();

                if (currentMethod === 'email' && (!emailVal || !emailVal.includes('@'))) {
                    e.preventDefault();
                    alert('Please enter a valid email address.');
                    document.getElementById('emailInput')?.focus();
                    return;
                }
                if (currentMethod === 'phone' && (!phoneVal || phoneVal.length < 10)) {
                    e.preventDefault();
                    alert('Please enter a valid 10-digit mobile number.');
                    document.getElementById('phoneInput')?.focus();
                    return;
                }

                btn.disabled = true;
                btn.textContent = 'Sending OTP…';
            });
        }

        // Form submission handling for OTP verification
        const otpForm = document.getElementById('otpVerifyForm');
        const otpBtn  = document.getElementById('verifySubmitBtn');
        if (otpForm && otpBtn) {
            otpForm.addEventListener('submit', function(e) {
                const otpVal = document.getElementById('otpCode')?.value.trim();
                if (!otpVal || otpVal.length !== 6 || !/^\d{6}$/.test(otpVal)) {
                    e.preventDefault();
                    alert('Please enter a valid 6-digit OTP code.');
                    document.getElementById('otpCode')?.focus();
                    return;
                }
                otpBtn.disabled = true;
                otpBtn.textContent = 'Verifying OTP…';
            });
        }
    </script>
</body>
</html>
