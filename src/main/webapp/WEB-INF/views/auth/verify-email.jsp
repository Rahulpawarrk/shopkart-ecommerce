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
    <title>Dual Verification (Email &amp; Mobile) | ShopKart</title>
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
            max-width: 490px;
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

        .verification-destinations {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 0.75rem;
            margin: 1rem 0 1.25rem;
        }

        .dest-chip {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 0.65rem 0.75rem;
            font-size: 0.8rem;
        }

        .dest-chip-title {
            font-size: 0.72rem;
            font-weight: 700;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            display: flex;
            align-items: center;
            gap: 0.35rem;
            margin-bottom: 0.25rem;
        }

        .dest-chip-val {
            font-weight: 700;
            color: #0f172a;
            word-break: break-all;
            font-size: 0.85rem;
        }

        .otp-input-group {
            background: #fdfdfd;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 1rem;
            margin-bottom: 1.15rem;
            transition: var(--transition-fast);
        }

        .otp-input-group:focus-within {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .otp-group-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 0.5rem;
        }

        .otp-group-label {
            font-size: 0.85rem;
            font-weight: 700;
            color: #1e293b;
            display: flex;
            align-items: center;
            gap: 0.4rem;
        }

        .otp-resend-link {
            font-size: 0.75rem;
            font-weight: 700;
            color: #2563eb;
            text-decoration: none;
        }

        .otp-resend-link:hover {
            text-decoration: underline;
        }

        .otp-input-field {
            width: 100%;
            padding: 0.65rem;
            font-size: 1.4rem;
            font-weight: 900;
            letter-spacing: 8px;
            text-align: center;
            color: #0f172a;
            background: #ffffff;
            border: 1.5px solid #cbd5e1;
            border-radius: 6px;
            outline: none;
            transition: var(--transition-fast);
            font-family: monospace;
        }

        .otp-input-field:focus {
            border-color: #2563eb;
            box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
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
            <span class="step-pill active">🔐 2. Dual Verification (Email &amp; Mobile)</span>
            <span>→</span>
            <span class="step-pill">🛍️ 3. Shop</span>
        </div>

        <div class="auth-card">
            <h1 style="font-size: 1.45rem; font-weight: 800; color: #111827; margin: 0 0 0.4rem;">
                Verify Email &amp; Mobile Number
            </h1>
            <p style="color: #4b5563; font-size: 0.84rem; margin: 0; line-height: 1.45;">
                We've sent two 6-digit verification codes. Enter both codes below to activate your account.
            </p>

            <!-- Verification Targets Box -->
            <div class="verification-destinations">
                <div class="dest-chip">
                    <div class="dest-chip-title"><span>✉️</span> Email Code</div>
                    <div class="dest-chip-val"><c:out value="${pendingEmail}" /></div>
                </div>
                <div class="dest-chip">
                    <div class="dest-chip-title"><span>📱</span> Mobile SMS Code</div>
                    <div class="dest-chip-val">+91 <c:out value="${pendingPhone}" /></div>
                </div>
            </div>

            <!-- Error Notification -->
            <c:if test="${not empty error}">
                <div
                    style="background: #fef2f2; border-left: 4px solid var(--danger); padding: 0.75rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1.15rem; color: #991b1b; font-size: 0.85rem; font-weight: 600; display: flex; align-items: center; gap: 0.4rem;">
                    <span>⚠️</span>
                    <span>
                        <c:out value="${error}" />
                    </span>
                </div>
            </c:if>

            <!-- Success Notification -->
            <c:if test="${not empty successMessage}">
                <div
                    style="background: #f0fdf4; border-left: 4px solid var(--success); padding: 0.75rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1.15rem; color: #166534; font-size: 0.85rem; font-weight: 600; display: flex; align-items: center; gap: 0.4rem;">
                    <span>✓</span>
                    <span>
                        <c:out value="${successMessage}" />
                    </span>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/verify-email" method="POST" id="dualVerifyForm">
                <!-- 1. Email OTP Field -->
                <div class="otp-input-group">
                    <div class="otp-group-header">
                        <label for="emailOtp" class="otp-group-label">
                            <span>✉️</span> 1. Email Verification Code *
                        </label>
                        <a href="${pageContext.request.contextPath}/verify-email?resend=email" class="otp-resend-link">
                            🔄 Resend Email OTP
                        </a>
                    </div>
                    <input type="text" id="emailOtp" name="emailOtp" class="otp-input-field" maxlength="6"
                        pattern="[0-9]{6}" placeholder="------" autocomplete="one-time-code" autofocus required
                        value="<c:out value='${emailOtp}' />">
                </div>

                <!-- 2. Mobile SMS OTP Field -->
                <div class="otp-input-group">
                    <div class="otp-group-header">
                        <label for="mobileOtp" class="otp-group-label">
                            <span>📱</span> 2. Mobile SMS Verification Code *
                        </label>
                        <a href="${pageContext.request.contextPath}/verify-email?resend=mobile" class="otp-resend-link">
                            🔄 Resend SMS OTP
                        </a>
                    </div>
                    <input type="text" id="mobileOtp" name="mobileOtp" class="otp-input-field" maxlength="6"
                        pattern="[0-9]{6}" placeholder="------" autocomplete="one-time-code" required
                        value="<c:out value='${mobileOtp}' />">
                </div>

                <div style="font-size: 0.75rem; color: #64748b; margin: -0.25rem 0 1.25rem; text-align: center;">
                    ⏳ Both codes are valid for 10 minutes.
                </div>

                <button type="submit" id="verifySubmitBtn" class="hero-cta-btn"
                    style="width: 100%; text-align: center; justify-content: center; padding: 0.8rem; font-size: 1rem; margin-bottom: 1.25rem; font-weight: 700;">
                    Verify Both Codes &amp; Complete Registration →
                </button>
            </form>

            <div
                style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #e2e8f0; padding-top: 1rem; font-size: 0.82rem;">
                <div>
                    <a href="${pageContext.request.contextPath}/verify-email?resend=all" id="resendAllBtn"
                        style="color: #2563eb; font-weight: 700; text-decoration: none;">
                        🔄 Resend Both Codes
                    </a>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}/register"
                        style="color: #64748b; text-decoration: none;">
                        ✏️ Change Details
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
                    Designed, Developed &amp; Managed by <strong style="color: #2563eb; font-weight: 700;">Rahul
                        Pawar</strong>
                </div>
            </div>
        </footer>
    </div>

    <script>
        const emailOtpInput = document.getElementById('emailOtp');
        const mobileOtpInput = document.getElementById('mobileOtp');
        const verifyForm = document.getElementById('dualVerifyForm');
        const submitBtn = document.getElementById('verifySubmitBtn');

        [emailOtpInput, mobileOtpInput].forEach(input => {
            if (input) {
                input.addEventListener('input', function () {
                    this.value = this.value.replace(/[^0-9]/g, '').slice(0, 6);
                });
            }
        });

        if (verifyForm) {
            verifyForm.addEventListener('submit', function (e) {
                if (emailOtpInput && emailOtpInput.value.length !== 6) {
                    e.preventDefault();
                    emailOtpInput.focus();
                    return;
                }
                if (mobileOtpInput && mobileOtpInput.value.length !== 6) {
                    e.preventDefault();
                    mobileOtpInput.focus();
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