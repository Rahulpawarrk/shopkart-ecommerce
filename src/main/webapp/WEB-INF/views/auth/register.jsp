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
    <title>Create Your TechZone Account | India's Premier Online Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
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
            padding: 2rem;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
        }

        .auth-card-title {
            font-size: 1.6rem;
            font-weight: 800;
            color: #111827;
            margin-bottom: 1.25rem;
        }

        .form-row {
            display: flex;
            gap: 0.75rem;
        }

        .form-row .auth-form-group {
            flex: 1;
        }

        .auth-form-group {
            margin-bottom: 1.15rem;
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
            background-color: #ffffff;
            box-sizing: border-box;
        }

        .auth-form-input:focus {
            border-color: var(--amazon-orange);
            box-shadow: 0 0 0 3px rgba(250, 137, 0, 0.25);
        }

        .btn-auth-submit {
            width: 100%;
            background: linear-gradient(180deg, var(--amazon-yellow) 0%, #f0c14b 100%);
            border: 1px solid #a88734;
            border-radius: var(--radius-sm);
            padding: 0.7rem;
            font-size: 0.95rem;
            font-weight: 700;
            color: #111827;
            cursor: pointer;
            box-shadow: 0 1px 2px rgba(0,0,0,0.1);
            transition: var(--transition-fast);
            margin-top: 0.5rem;
        }

        .btn-auth-submit:hover {
            background: linear-gradient(180deg, #f5d378 0%, #eeb933 100%);
            border-color: #846a29;
        }

        .auth-terms-note {
            font-size: 0.75rem;
            color: var(--text-secondary);
            margin-top: 1.25rem;
            line-height: 1.4;
        }

        .auth-terms-note a {
            color: var(--primary);
            text-decoration: underline;
        }

        .auth-alert {
            padding: 0.75rem 1rem;
            border-radius: var(--radius-sm);
            font-size: 0.85rem;
            margin-bottom: 1.25rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .auth-alert-error {
            background-color: #fee2e2;
            color: #b91c1c;
            border: 1px solid #fca5a5;
        }

        .auth-already-account {
            margin-top: 1.5rem;
            padding-top: 1.25rem;
            border-top: 1px solid #e2e8f0;
            font-size: 0.85rem;
            color: var(--text-secondary);
        }

        .auth-already-account a {
            color: var(--primary);
            font-weight: 700;
            text-decoration: none;
        }

        .auth-already-account a:hover {
            text-decoration: underline;
        }

        .auth-footer {
            margin-top: 2rem;
            text-align: center;
            font-size: 0.75rem;
            color: var(--text-muted);
            border-top: 1px solid #e2e8f0;
            padding-top: 1.5rem;
            width: 100%;
            max-width: 500px;
        }

        .auth-footer-links {
            display: flex;
            justify-content: center;
            gap: 1.5rem;
            margin-bottom: 0.75rem;
        }

        .auth-footer-links a {
            color: var(--primary);
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
        <a href="${pageContext.request.contextPath}/" class="auth-brand-logo" style="text-decoration: none; display: inline-block; margin-bottom: 1.5rem;" title="ShopKart">
            <img src="${pageContext.request.contextPath}/assets/images/logo-dark.svg" alt="ShopKart" style="height: 48px; width: auto; display: block;">
        </a>

        <!-- Step Indicator -->
        <div style="display: flex; align-items: center; justify-content: center; gap: 0.5rem; margin-bottom: 1.25rem; font-size: 0.75rem; font-weight: 700; color: var(--text-muted);">
            <span style="padding: 0.25rem 0.65rem; border-radius: 9999px; background: #e0f2fe; color: #0369a1; border: 1px solid #7dd3fc;">1. Account Details</span>
            <span>→</span>
            <span style="padding: 0.25rem 0.65rem; border-radius: 9999px; background: #f1f5f9;">✉️ 2. Email Verification</span>
            <span>→</span>
            <span style="padding: 0.25rem 0.65rem; border-radius: 9999px; background: #f1f5f9;">🛍️ 3. Shop</span>
        </div>

        <!-- Main Card -->
        <div class="auth-card">
            <h1 class="auth-card-title">Create Account</h1>

            <!-- Errors -->
            <c:if test="${not empty error}">
                <div class="auth-alert auth-alert-error">
                    <span>⚠️</span> <span><c:out value="${error}" /></span>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/register" method="POST">
                <div class="form-row">
                    <div class="auth-form-group">
                        <label for="firstName" class="auth-form-label">First Name *</label>
                        <input type="text" id="firstName" name="firstName" class="auth-form-input" 
                               value="<c:out value='${firstName}' />" required placeholder="First Name" autofocus>
                    </div>
                    <div class="auth-form-group">
                        <label for="lastName" class="auth-form-label">Last Name *</label>
                        <input type="text" id="lastName" name="lastName" class="auth-form-input" 
                               value="<c:out value='${lastName}' />" required placeholder="Last Name">
                    </div>
                </div>

                <div class="auth-form-group">
                    <label for="email" class="auth-form-label">Email Address *</label>
                    <input type="email" id="email" name="email" class="auth-form-input" 
                           value="<c:out value='${email}' />" required placeholder="you@example.com">
                </div>

                <div class="auth-form-group">
                    <label for="phone" class="auth-form-label">Mobile Number *</label>
                    <input type="tel" id="phone" name="phone" class="auth-form-input" 
                           value="<c:out value='${phone}' />" required pattern="[0-9]{10}" maxlength="10" 
                           placeholder="10-digit mobile number (e.g. 9876543210)"
                           title="Please enter a valid 10-digit mobile number">
                </div>

                <div class="form-row">
                    <div class="auth-form-group">
                        <label for="password" class="auth-form-label">Password * (min. 8 chars)</label>
                        <input type="password" id="password" name="password" class="auth-form-input" 
                               minlength="8" required placeholder="At least 8 characters">
                    </div>
                    <div class="auth-form-group">
                        <label for="confirmPassword" class="auth-form-label">Confirm Password *</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" class="auth-form-input" 
                               minlength="8" required placeholder="Re-enter password">
                    </div>
                </div>

                <button type="submit" class="btn-auth-submit">
                    Continue to Email Verification →
                </button>
            </form>

            <div class="auth-terms-note">
                By creating an account, you agree to ShopKart's <a href="#">Conditions of Use</a> and <a href="#">Privacy Notice</a>.
            </div>

            <div class="auth-already-account">
                Already have an account? <a href="${pageContext.request.contextPath}/login">Sign In →</a>
            </div>
        </div>

        <!-- Footer -->
        <footer class="auth-footer" style="text-align: center; margin-top: 2rem;">
            <div class="auth-footer-links">
                <a href="#">Conditions of Use</a>
                <a href="#">Privacy Notice</a>
                <c:if test="${sessionScope.currentUser.admin}">
                    <a href="${pageContext.request.contextPath}/health">System Health</a>
                </c:if>
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

</body>
</html>
