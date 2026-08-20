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
    <title>Verify Your Email | ShopKart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link
        href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap"
        rel="stylesheet">
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

        .auth-brand-logo {
            font-size: 1.85rem;
            font-weight: 900;
            color: var(--amazon-dark);
            text-decoration: none;
            margin-bottom: 1.5rem;
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            letter-spacing: -0.5px;
        }

        .auth-brand-logo span {
            color: var(--amazon-orange);
        }

        .auth-card {
            width: 100%;
            max-width: 440px;
            background: #ffffff;
            border: 1px solid #d5d9d9;
            border-radius: var(--radius-md);
            padding: 2.25rem 2rem;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
        }

        .step-progress-bar {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            margin-bottom: 1.5rem;
            font-size: 0.75rem;
            font-weight: 700;
            color: var(--text-muted);
        }

        .step-pill {
            padding: 0.25rem 0.65rem;
            border-radius: 9999px;
            background: #f1f5f9;
        }

        .step-pill.completed {
            background: #dcfce7;
            color: #166534;
        }

        .step-pill.active {
            background: #e0f2fe;
            color: #0369a1;
            border: 1px solid #7dd3fc;
        }

        .email-display-chip {
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            background: #eff6ff;
            border: 1px solid #bfdbfe;
            color: #1d4ed8;
            padding: 0.45rem 0.85rem;
            border-radius: 6px;
            font-weight: 700;
            font-size: 0.88rem;
            margin: 0.6rem 0 1.25rem;
            word-break: break-all;
        }

        .otp-input-field {
            width: 100%;
            padding: 0.85rem;
            font-size: 1.5rem;
            font-weight: 900;
            letter-spacing: 10px;
            text-align: center;
            color: #0f172a;
            background: #ffffff;
            border: 2px solid #cbd5e1;
            border-radius: var(--radius-md);
            outline: none;
            transition: var(--transition-fast);
            font-family: monospace;
            box-sizing: border-box;
        }

        .otp-input-field:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(40, 116, 240, 0.15);
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
            margin-top: 1.25rem;
            margin-bottom: 1.25rem;
            transition: var(--transition-fast);
        }

        .btn-auth-submit:hover {
            background: linear-gradient(180deg, #f5d378 0%, #eeb933 100%);
        }

        .btn-auth-submit:disabled {
            opacity: 0.7;
            cursor: not-allowed;
        }

        .auth-footer {
            margin-top: 2rem;
            text-align: center;
            font-size: 0.75rem;
            color: #64748b;
        }

        .auth-footer-links {
            display: flex;
            justify-content: center;
            gap: 1.25rem;
            margin-bottom: 0.75rem;
        }

        .auth-footer-links a {
            color: #2563eb;
            text-decoration: none;
        }

        .auth-footer-links a:hover {
            text-decoration: underline;
        }
    </style>
</head>

<body>

    <div class="auth-page-wrapper">
        <!-- Logo -->
        <a href="${pageContext.request.contextPath}/" class="auth-brand-logo">
            🛒 Shop<span>Kart</span>
        </a>

        <!-- Step Indicator -->
        <div class="step-progress-bar">
            <span class="step-pill completed">✓ 1. Account Details</span>
            <span>→</span>
            <span class="step-pill active">✉️ 2. Email Verification</span>
            <span>→</span>
            <span class="step-pill">🛍️ 3. Shop</span>
        </div>

        <div class="auth-card">
            <h1 style="font-size: 1.45rem; font-weight: 800; color: #111827; margin: 0 0 0.4rem;">
                Verify Your Email Address
            </h1>
            <p style="color: #4b5563; font-size: 0.85rem; margin: 0; line-height: 1.45;">
                We've sent a 6-digit verification code to:
            </p>

            <div class="email-display-chip">
                <span>✉️</span>
                <c:out value="${pendingEmail}" />
            </div>

            <!-- Error Notification -->
            <c:if test="${not empty error}">
                <div
                    style="background: #fef2f2; border-left: 4px solid var(--danger); padding: 0.75rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; color: #991b1b; font-size: 0.85rem; font-weight: 600; display: flex; align-items: center; gap: 0.4rem;">
                    <span>⚠️</span>
                    <span>
                        <c:out value="${error}" />
                    </span>
                </div>
            </c:if>

            <!-- Success Notification -->
            <c:if test="${not empty successMessage}">
                <div
                    style="background: #f0fdf4; border-left: 4px solid var(--success); padding: 0.75rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; color: #166534; font-size: 0.85rem; font-weight: 600; display: flex; align-items: center; gap: 0.4rem;">
                    <span>✓</span>
                    <span>
                        <c:out value="${successMessage}" />
                    </span>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/verify-email" method="POST" id="verifyEmailForm">
                <div>
                    <label for="otp"
                        style="display: block; font-size: 0.85rem; font-weight: 700; color: #111827; margin-bottom: 0.5rem;">
                        Enter 6-Digit Verification Code *
                    </label>
                    <input type="text" id="otp" name="otp" class="otp-input-field" maxlength="6"
                        pattern="[0-9]{6}" placeholder="------" autocomplete="one-time-code" autofocus required>
                    <div
                        style="font-size: 0.75rem; color: #64748b; margin-top: 0.4rem; display: flex; justify-content: space-between;">
                        <span>Code expires in 10 minutes</span>
                    </div>
                </div>

                <button type="submit" id="verifySubmitBtn" class="btn-auth-submit">
                    Verify &amp; Create Account →
                </button>
            </form>

            <div
                style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #e2e8f0; padding-top: 1rem; font-size: 0.82rem;">
                <div>
                    <a href="${pageContext.request.contextPath}/verify-email?resend=true" id="resendBtn"
                        style="color: #2563eb; font-weight: 700; text-decoration: none;">
                        🔄 Resend Code
                    </a>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}/register"
                        style="color: #64748b; text-decoration: none;">
                        ✏️ Change Email
                    </a>
                </div>
            </div>
        </div>

        <!-- Footer -->
        <footer class="auth-footer" style="text-align: center; margin-top: 2rem;">
            <div class="auth-footer-links">
                <a href="#">Conditions of Use</a>
                <a href="#">Privacy Notice</a>
                <a href="#">Help Center</a>
            </div>
            <div style="font-size: 0.8rem; color: #94a3b8; margin-top: 0.6rem; line-height: 1.5;">
                <div>&copy; 2026 ShopKart Inc. All rights reserved. &bull; 100% Purchase Protection</div>
                <div style="margin-top: 0.35rem; color: #64748b;">
                    Designed, Developed &amp; Managed by <strong style="color: #2563eb; font-weight: 700;">Rahul Pawar</strong>
                </div>
            </div>
        </footer>
    </div>

    <script nonce="${cspNonce}">
        const otpInput = document.getElementById('otp');
        const verifyForm = document.getElementById('verifyEmailForm');
        const submitBtn = document.getElementById('verifySubmitBtn');

        if (otpInput) {
            otpInput.addEventListener('input', function () {
                this.value = this.value.replace(/[^0-9]/g, '').slice(0, 6);
            });
        }

        if (verifyForm) {
            verifyForm.addEventListener('submit', function (e) {
                if (otpInput && otpInput.value.length !== 6) {
                    e.preventDefault();
                    otpInput.focus();
                    return;
                }
                if (submitBtn) {
                    submitBtn.disabled = true;
                    submitBtn.style.opacity = '0.75';
                    submitBtn.innerHTML = '⏳ Verifying & Creating Account...';
                }
            });
        }
    </script>
</body>

</html>