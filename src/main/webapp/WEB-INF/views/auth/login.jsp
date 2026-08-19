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
            <title>ShopKart Sign-In | India's Premier Online Store</title>
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
                    max-width: 400px;
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
                    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
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

                .demo-credentials-box {
                    margin-top: 1.25rem;
                    padding: 0.85rem;
                    background: #f1f5f9;
                    border-radius: var(--radius-sm);
                    border: 1px dashed #cbd5e1;
                    font-size: 0.8rem;
                }

                .demo-chips-row {
                    display: flex;
                    gap: 0.5rem;
                    margin-top: 0.5rem;
                }

                .demo-chip-btn {
                    flex: 1;
                    padding: 0.35rem 0.5rem;
                    font-size: 0.75rem;
                    font-weight: 700;
                    background: #ffffff;
                    border: 1px solid #94a3b8;
                    border-radius: var(--radius-sm);
                    cursor: pointer;
                    transition: var(--transition-fast);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 0.3rem;
                }

                .demo-chip-btn:hover {
                    background: var(--primary-light);
                    border-color: var(--primary);
                    color: var(--primary);
                }

                .auth-divider {
                    display: flex;
                    align-items: center;
                    margin: 1.5rem 0 1rem;
                    color: var(--text-muted);
                    font-size: 0.8rem;
                    width: 100%;
                    max-width: 400px;
                }

                .auth-divider::before,
                .auth-divider::after {
                    content: '';
                    flex: 1;
                    border-bottom: 1px solid #d5d9d9;
                }

                .auth-divider span {
                    padding: 0 0.75rem;
                    white-space: nowrap;
                }

                .btn-create-account {
                    width: 100%;
                    max-width: 400px;
                    background: #ffffff;
                    border: 1px solid #d5d9d9;
                    border-radius: var(--radius-sm);
                    padding: 0.65rem;
                    font-size: 0.9rem;
                    font-weight: 600;
                    color: #111827;
                    text-align: center;
                    display: block;
                    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
                    transition: var(--transition-fast);
                    box-sizing: border-box;
                }

                .btn-create-account:hover {
                    background: #f7fafa;
                    border-color: #a88734;
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

                .auth-alert-warning {
                    background-color: #fef9c3;
                    color: #854d0e;
                    border: 1px solid #fde047;
                }

                .auth-alert-success {
                    background-color: #dcfce7;
                    color: #15803d;
                    border: 1px solid #86efac;
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
                <a href="${pageContext.request.contextPath}/" class="auth-brand-logo"
                    style="text-decoration: none; display: inline-block; margin-bottom: 1.5rem;" title="ShopKart">
                    <img src="${pageContext.request.contextPath}/assets/images/logo-dark.svg" alt="ShopKart"
                        style="height: 48px; width: auto; display: block;">
                </a>

                <!-- Main Card -->
                <div class="auth-card">
                    <h1 class="auth-card-title">Sign in</h1>

                    <!-- Alerts -->
                    <c:if test="${not empty error}">
                        <div class="auth-alert auth-alert-error">
                            <span>⚠️</span> <span>
                                <c:out value="${error}" />
                            </span>
                        </div>
                    </c:if>

                    <c:if test="${not empty warning}">
                        <div class="auth-alert auth-alert-warning">
                            <span>⚠️</span> <span>
                                <c:out value="${warning}" />
                            </span>
                        </div>
                    </c:if>

                    <c:if test="${param.logout == 'true'}">
                        <div class="auth-alert auth-alert-success">
                            <span>✓</span> <span>You have been signed out safely.</span>
                        </div>
                    </c:if>

                    <c:if test="${param.reset == 'success'}">
                        <div class="auth-alert auth-alert-success">
                            <span>✓</span> <span>Your password has been reset successfully. Please sign in with your new password.</span>
                        </div>
                    </c:if>

                    <!-- Login Form -->
                    <form action="${pageContext.request.contextPath}/login" method="POST" autocomplete="on">
                        <div class="auth-form-group">
                            <label for="email" class="auth-form-label">Email or mobile phone number</label>
                            <input type="email" id="email" name="email" class="auth-form-input"
                                value="<c:out value='${email}' />" required placeholder="you@example.com" autofocus>
                        </div>

                        <div class="auth-form-group">
                            <div style="display: flex; justify-content: space-between; align-items: baseline;">
                                <label for="password" class="auth-form-label">Password</label>
                                <a href="${pageContext.request.contextPath}/forgot-password"
                                   style="font-size: 0.75rem; color: var(--primary); text-decoration: none;"
                                   id="forgotPasswordLink">Forgot password?</a>
                            </div>
                            <input type="password" id="password" name="password" class="auth-form-input" required
                                placeholder="••••••••">
                        </div>

                        <button type="submit" class="btn-auth-submit">
                            Sign In
                        </button>
                    </form>

                    <div class="auth-terms-note">
                        By continuing, you agree to ShopKart's <a href="#">Conditions of Use</a> and <a href="#">Privacy
                            Notice</a>.
                    </div>

                    <!-- Demo Credentials 1-Click Fillers -->
                    <div class="demo-credentials-box">
                        <div style="font-weight: 700; color: #475569;">⚡ 1-Click Quick Demo Login:</div>
                        <div class="demo-chips-row">
                            <button type="button" class="demo-chip-btn"
                                onclick="fillCredentials('customer@ecommerce.com', 'Admin@123')">
                                👤 Customer
                            </button>
                            <button type="button" class="demo-chip-btn"
                                onclick="fillCredentials('admin@ecommerce.com', 'Admin@123')">
                                ⚙️ Admin
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Divider -->
                <div class="auth-divider">
                    <span>New to ShopKart?</span>
                </div>

                <!-- Create Account CTA -->
                <a href="${pageContext.request.contextPath}/register" class="btn-create-account">
                    Create your ShopKart account
                </a>

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

            <script>
                function fillCredentials(email, pass) {
                    document.getElementById('email').value = email;
                    document.getElementById('password').value = pass;
                }
            </script>
        </body>

        </html>