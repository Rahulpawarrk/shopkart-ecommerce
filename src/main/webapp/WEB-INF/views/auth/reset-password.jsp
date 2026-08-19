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
    <title>Reset Password | ShopKart India</title>
    <meta name="description" content="Set a new secure password for your ShopKart account.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        .auth-page-wrapper {
            min-height: 100vh;
            background: #f8fafc;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 2rem 1rem;
        }
        .auth-card {
            width: 100%;
            max-width: 420px;
            background: #fff;
            border: 1px solid #d5d9d9;
            border-radius: var(--radius-md);
            padding: 2rem;
            box-shadow: 0 4px 16px rgba(0,0,0,0.05);
        }
        .auth-card-title {
            font-size: 1.5rem;
            font-weight: 800;
            color: #111827;
            margin-bottom: 0.5rem;
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
        .btn-auth-submit:hover { background: linear-gradient(180deg, #f5d378 0%, #eeb933 100%); }
        .btn-auth-submit:disabled { opacity: 0.6; cursor: not-allowed; }
        .alert-error {
            background: #fee2e2;
            border: 1px solid #fca5a5;
            color: #b91c1c;
            border-radius: var(--radius-sm);
            padding: 0.75rem 1rem;
            font-size: 0.85rem;
            margin-bottom: 1.25rem;
        }
        .alert-invalid {
            background: #fef9c3;
            border: 1px solid #fde047;
            color: #854d0e;
            border-radius: var(--radius-sm);
            padding: 1rem 1.25rem;
            font-size: 0.9rem;
            margin-bottom: 1.25rem;
            line-height: 1.5;
        }
        .password-strength-bar {
            height: 4px;
            border-radius: 2px;
            transition: all 0.3s ease;
            margin-top: 6px;
        }
        .strength-hint {
            font-size: 0.75rem;
            margin-top: 4px;
        }
        .form-group { margin-bottom: 1.25rem; }
        .pwd-wrapper { position: relative; }
        .pwd-toggle {
            position: absolute;
            right: 0.75rem;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            cursor: pointer;
            font-size: 1rem;
            color: #6b7280;
            padding: 0;
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
    </style>
</head>
<body>
    <div class="auth-page-wrapper">
        <!-- Logo -->
        <a href="${pageContext.request.contextPath}/" style="text-decoration:none; margin-bottom:1.5rem; display:inline-block;">
            <img src="${pageContext.request.contextPath}/assets/images/logo-dark.svg" alt="ShopKart" style="height:48px; width:auto; display:block;">
        </a>

        <div class="auth-card">
            <h1 class="auth-card-title">Reset Your Password</h1>
            <p style="color:#6b7280; font-size:0.875rem; margin:0 0 1.25rem;">
                Enter the 6-digit verification code sent to your email or mobile, then choose a secure new password.
            </p>

            <c:if test="${not empty error}">
                <div class="alert-error">⚠️ <c:out value="${error}"/></div>
            </c:if>

            <form action="${pageContext.request.contextPath}/reset-password" method="POST" id="resetForm" novalidate>
                <c:if test="${not empty token}">
                    <input type="hidden" name="token" value="<c:out value='${token}'/>">
                </c:if>

                <!-- Identifier Field (Email / Mobile) -->
                <div class="form-group">
                    <label for="identifier" class="auth-form-label">Registered Email or Mobile Number *</label>
                    <input type="text" id="identifier" name="identifier" class="auth-form-input"
                           required placeholder="you@example.com or 10-digit mobile"
                           value="<c:out value='${identifier}'/>"
                           <c:if test="${not empty identifier}">readonly style="background-color:#f1f5f9; cursor:not-allowed;"</c:if>>
                </div>

                <!-- 6-Digit OTP Field -->
                <div class="form-group">
                    <label for="otpCode" class="auth-form-label">6-Digit Verification OTP Code *</label>
                    <input type="text" id="otpCode" name="otpCode"
                           class="auth-form-input" required maxlength="6" pattern="[0-9]{6}"
                           placeholder="123456" autocomplete="one-time-code" autofocus
                           value="<c:out value='${otpCode}'/>"
                           style="font-size: 1.3rem; letter-spacing: 6px; text-align: center; font-weight: 800; font-family: monospace;">
                </div>

                <!-- New Password -->
                <div class="form-group">
                    <label for="newPassword" class="auth-form-label">New Password * (Min 8 Characters)</label>
                    <div class="pwd-wrapper">
                        <input type="password" id="newPassword" name="newPassword"
                               class="auth-form-input" required minlength="8"
                               placeholder="Enter new password" autocomplete="new-password"
                               oninput="checkStrength(this.value)">
                        <button type="button" class="pwd-toggle" onclick="togglePwd('newPassword', this)" title="Show/hide">👁</button>
                    </div>
                    <div id="strengthBar" class="password-strength-bar" style="background:#e5e7eb;"></div>
                    <div id="strengthHint" class="strength-hint" style="color:#9ca3af;"></div>
                </div>

                <!-- Confirm Password -->
                <div class="form-group">
                    <label for="confirmPassword" class="auth-form-label">Confirm New Password *</label>
                    <div class="pwd-wrapper">
                        <input type="password" id="confirmPassword" name="confirmPassword"
                               class="auth-form-input" required minlength="8"
                               placeholder="Confirm new password" autocomplete="new-password"
                               oninput="checkMatch()">
                        <button type="button" class="pwd-toggle" onclick="togglePwd('confirmPassword', this)" title="Show/hide">👁</button>
                    </div>
                    <div id="matchHint" class="strength-hint"></div>
                </div>

                <button type="submit" class="btn-auth-submit" id="resetBtn">
                    🔒 Verify Code &amp; Update Password
                </button>
            </form>

            <a href="${pageContext.request.contextPath}/login" class="back-link">← Back to Sign In</a>
        </div>

        <footer style="margin-top: 2rem; font-size: 0.8rem; color: #9ca3af; text-align: center; line-height: 1.6;">
            <div>&copy; 2026 ShopKart Inc. &bull; All rights reserved.</div>
            <div style="margin-top: 0.25rem; color: #6b7280;">
                Designed, Developed &amp; Managed by <strong style="color: #2563eb; font-weight: 700;">Rahul Pawar</strong>
            </div>
        </footer>
    </div>

    <script>
        function togglePwd(fieldId, btn) {
            const field = document.getElementById(fieldId);
            if (field.type === 'password') {
                field.type = 'text';
                btn.textContent = '🙈';
            } else {
                field.type = 'password';
                btn.textContent = '👁';
            }
        }

        function checkStrength(value) {
            const bar  = document.getElementById('strengthBar');
            const hint = document.getElementById('strengthHint');
            let score = 0;
            if (value.length >= 8)  score++;
            if (value.length >= 12) score++;
            if (/[A-Z]/.test(value)) score++;
            if (/[0-9]/.test(value)) score++;
            if (/[^A-Za-z0-9]/.test(value)) score++;

            const levels = [
                { color: '#ef4444', label: 'Very weak',  width: '20%' },
                { color: '#f97316', label: 'Weak',       width: '40%' },
                { color: '#eab308', label: 'Fair',       width: '60%' },
                { color: '#22c55e', label: 'Strong',     width: '80%' },
                { color: '#16a34a', label: 'Very strong',width: '100%'},
            ];
            const lvl = levels[Math.max(0, score - 1)] || levels[0];
            bar.style.background = value.length > 0 ? lvl.color : '#e5e7eb';
            bar.style.width = value.length > 0 ? lvl.width : '0';
            hint.textContent = value.length > 0 ? lvl.label : '';
            hint.style.color  = lvl.color;
            checkMatch();
        }

        function checkMatch() {
            const pw  = document.getElementById('newPassword').value;
            const cpw = document.getElementById('confirmPassword').value;
            const hint = document.getElementById('matchHint');
            if (cpw.length === 0) { hint.textContent = ''; return; }
            if (pw === cpw) {
                hint.textContent = '✓ Passwords match';
                hint.style.color = '#16a34a';
            } else {
                hint.textContent = '✗ Passwords do not match';
                hint.style.color = '#ef4444';
            }
        }

        // Prevent double-submit
        document.getElementById('resetForm')?.addEventListener('submit', function(e) {
            const pw  = document.getElementById('newPassword')?.value;
            const cpw = document.getElementById('confirmPassword')?.value;
            if (pw !== cpw) {
                e.preventDefault();
                alert('Passwords do not match. Please check and try again.');
                return;
            }
            const btn = document.getElementById('resetBtn');
            if (btn) { btn.disabled = true; btn.textContent = 'Updating…'; }
        });
    </script>
</body>
</html>
