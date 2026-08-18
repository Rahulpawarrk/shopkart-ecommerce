<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
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
        .alert-success {
            background: #dcfce7;
            border: 1px solid #86efac;
            color: #15803d;
            border-radius: var(--radius-sm);
            padding: 1rem 1.25rem;
            font-size: 0.9rem;
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

        .simulation-box {
            background: #fffbeb;
            border: 1px dashed #f59e0b;
            border-radius: var(--radius-sm);
            padding: 0.85rem;
            margin-top: 1rem;
            font-size: 0.82rem;
            color: #92400e;
        }
        .simulation-box a {
            color: #b45309;
            font-weight: 700;
            text-decoration: underline;
            word-break: break-all;
        }
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

            <c:choose>
                <%-- State 2: Form submitted — show confirmation --%>
                <c:when test="${submitted}">
                    <c:choose>
                        <c:when test="${methodType == 'PHONE'}">
                            <div class="alert-success">
                                <strong>✓ 6-Digit SMS OTP Sent!</strong><br>
                                A verification code has been dispatched to <strong>+91-<c:out value="${destination}"/></strong>.
                                The OTP is valid for <strong>10 minutes</strong>.
                            </div>

                            <c:if test="${not empty otpCode}">
                                <div class="simulation-box">
                                    <strong>📱 SMS Received (Sandbox Preview):</strong><br>
                                    Your 6-Digit Verification Code is: <span style="font-size: 1.25rem; font-weight: 900; letter-spacing: 3px; color: #b45309;"><c:out value="${otpCode}"/></span>
                                </div>
                            </c:if>

                            <a href="${pageContext.request.contextPath}/reset-password?phone=<c:out value='${rawPhone}'/><c:if test='${not empty otpCode}'>&amp;otpCode=<c:out value='${otpCode}'/></c:if>" 
                               class="btn-auth-submit"
                               style="display:block; text-align:center; text-decoration:none; padding:0.75rem; margin-top:1.25rem;">
                                Enter OTP &amp; Reset Password →
                            </a>
                        </c:when>
                        <c:otherwise>
                            <div class="alert-success">
                                <strong>✓ Check your inbox!</strong><br>
                                If an account is registered with <strong><c:out value="${destination}"/></strong>,
                                we've sent a password reset link. The link expires in <strong>1 hour</strong>.
                            </div>

                            <c:if test="${not empty resetLink}">
                                <div class="simulation-box">
                                    <strong>⚡ Direct Reset Link (Preview):</strong><br>
                                    <a href="<c:out value='${resetLink}'/>">Click here to reset password →</a>
                                </div>
                            </c:if>

                            <a href="${pageContext.request.contextPath}/forgot-password" class="btn-auth-submit"
                               style="display:block; text-align:center; text-decoration:none; padding:0.75rem; margin-top:1.25rem;">
                                Request Another Reset
                            </a>
                        </c:otherwise>
                    </c:choose>
                </c:when>

                <%-- State 1: Show dual-method input form --%>
                <c:otherwise>
                    <p class="auth-subtitle">
                        Choose your preferred verification method to reset your ShopKart account password.
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
                        <strong>Reset via Email:</strong>
                        <ol>
                            <li>Enter your registered email address</li>
                            <li>Check your inbox for a secure reset link</li>
                            <li>Click the link &amp; set a new password</li>
                        </ol>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert-error">⚠️ <c:out value="${error}"/></div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/forgot-password" method="POST" id="forgotForm" novalidate>
                        
                        <!-- Email Input Group -->
                        <div id="emailGroup" style="margin-bottom:1.25rem;">
                            <label for="emailInput" class="auth-form-label">Registered Email Address *</label>
                            <input type="email" id="emailInput" name="identifier" class="auth-form-input"
                                   placeholder="you@example.com" autofocus
                                   value="<c:out value='${param.email}'/>">
                        </div>

                        <!-- Mobile Input Group (hidden by default) -->
                        <div id="phoneGroup" style="margin-bottom:1.25rem; display:none;">
                            <label for="phoneInput" class="auth-form-label">Registered 10-Digit Mobile Number *</label>
                            <input type="tel" id="phoneInput" class="auth-form-input"
                                   placeholder="10-digit mobile (e.g. 9876543210)"
                                   maxlength="10" pattern="[0-9]{10}">
                        </div>

                        <button type="submit" class="btn-auth-submit" id="forgotSubmitBtn">
                            Send Reset Link →
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
                hintBox.innerHTML = '<strong>Reset via Email:</strong>' +
                    '<ol>' +
                    '<li>Enter your registered email address</li>' +
                    '<li>Check your inbox for a secure reset link</li>' +
                    '<li>Click the link &amp; set a new password</li>' +
                    '</ol>';
            } else {
                tabPhone.classList.add('active');
                tabEmail.classList.remove('active');
                phoneGroup.style.display = 'block';
                emailGroup.style.display = 'none';
                phoneInput.name = 'identifier';
                emailInput.name = '';
                phoneInput.focus();
                hintBox.innerHTML = '<strong>Reset via Mobile Number:</strong>' +
                    '<ol>' +
                    '<li>Enter your registered 10-digit mobile number</li>' +
                    '<li>We will verify your account &amp; issue a reset link</li>' +
                    '<li>Set your new secure password immediately</li>' +
                    '</ol>';
            }
        }

        // Form submission handling
        const form = document.getElementById('forgotForm');
        const btn  = document.getElementById('forgotSubmitBtn');
        if (form && btn) {
            form.addEventListener('submit', function(e) {
                const emailVal = document.getElementById('emailInput').value.trim();
                const phoneVal = document.getElementById('phoneInput').value.trim();

                if (currentMethod === 'email' && (!emailVal || !emailVal.includes('@'))) {
                    e.preventDefault();
                    alert('Please enter a valid email address.');
                    document.getElementById('emailInput').focus();
                    return;
                }
                if (currentMethod === 'phone' && (!phoneVal || phoneVal.length < 10)) {
                    e.preventDefault();
                    alert('Please enter a valid 10-digit mobile number.');
                    document.getElementById('phoneInput').focus();
                    return;
                }

                btn.disabled = true;
                btn.textContent = 'Processing…';
            });
        }
    </script>
</body>
</html>
