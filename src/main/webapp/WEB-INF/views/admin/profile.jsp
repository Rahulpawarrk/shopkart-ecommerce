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
    <title>Administrator Profile | ShopKart Console</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body style="background-color: #f8fafc;">

    <!-- TOP ADMIN PORTAL NAVBAR -->
    <header class="admin-navbar">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="brand">
            <img src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="ShopKart" style="height: 36px; width: auto;">
            <span class="admin-brand-pill">ADMIN CONSOLE</span>
        </a>
        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/" target="_blank" style="background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15);">🌐 Open Storefront ↗</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/profile" style="font-weight: 700; color: #fbbf24; font-size: 0.85rem; text-decoration: none;">👑 Admin: <c:out value="${sessionScope.currentUser.fullName}" /></a></li>
            <li><a href="${pageContext.request.contextPath}/logout" style="color: #f87171; font-weight: 700;">🚪 Sign Out</a></li>
        </ul>
    </header>

    <main class="admin-container">
        <div class="admin-layout">
            
            <!-- Left Navigation Sidebar -->
            <aside class="admin-sidebar-card">
                <div class="admin-nav-header">MAIN NAVIGATION</div>
                <ul class="admin-nav">
                    <li><a href="${pageContext.request.contextPath}/admin/dashboard"><span>📊</span> Dashboard Overview</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/admins"><span>👑</span> Admin Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reports/sales"><span>📈</span> Sales &amp; Revenue</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/products"><span>📦</span> Product Catalog</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/categories"><span>📁</span> Categories</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/inventory"><span>🏭</span> Inventory &amp; Stock</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/orders"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/returns"><span>🔄</span> Return Requests</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reconciliation"><span>⚖️</span> Failed Payments &amp; Reconciliation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons &amp; Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                    <li style="margin-top: 1rem; border-top: 1px solid #e2e8f0; padding-top: 0.75rem;">
                        <a href="${pageContext.request.contextPath}/admin/profile" class="active"><span>👤</span> Admin Account Profile</a>
                    </li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                
                <!-- Administrator Header Banner -->
                <div style="background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%); color: #ffffff; padding: 2rem; border-radius: var(--radius-md); margin-bottom: 1.5rem; border: 1px solid #334155; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                    <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                        <div style="display: flex; align-items: center; gap: 1.25rem;">
                            <div style="width: 64px; height: 64px; border-radius: 50%; background: linear-gradient(135deg, #f59e0b, #d97706); display: flex; align-items: center; justify-content: center; font-size: 2rem; box-shadow: 0 4px 10px rgba(245, 158, 11, 0.4);">
                                👑
                            </div>
                            <div>
                                <div style="display: flex; align-items: center; gap: 0.6rem;">
                                    <h1 style="font-size: 1.5rem; font-weight: 800; color: #ffffff; margin: 0;">
                                        <c:out value="${user.fullName}" />
                                    </h1>
                                    <span style="background: #f59e0b; color: #0f172a; font-weight: 800; font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 20px; text-transform: uppercase;">
                                        SUPER ADMIN
                                    </span>
                                </div>
                                <div style="color: #94a3b8; font-size: 0.875rem; margin-top: 0.25rem;">
                                    <c:out value="${user.email}" /> &bull; User ID: #${user.userId}
                                </div>
                            </div>
                        </div>
                        <div style="display: flex; gap: 0.75rem;">
                            <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary" style="background: rgba(255,255,255,0.1); border-color: rgba(255,255,255,0.2); color: #ffffff; font-weight: 700;">
                                📊 Dashboard
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/audit-logs" class="btn btn-primary" style="background: #3b82f6; border-color: #2563eb; font-weight: 700;">
                                🛡️ Audit Logs
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Notifications -->
                <c:if test="${not empty error}">
                    <div style="background: #fee2e2; border-left: 4px solid var(--danger); padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; color: #991b1b; font-size: 0.9rem; font-weight: 600;">
                        ⚠️ <c:out value="${error}" />
                    </div>
                </c:if>
                <c:if test="${not empty successMessage}">
                    <div style="background: #dcfce7; border-left: 4px solid var(--success); padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; color: #166534; font-size: 0.9rem; font-weight: 600;">
                        ✓ <c:out value="${successMessage}" />
                    </div>
                </c:if>

                <div style="display: grid; grid-template-columns: 2fr 1.2fr; gap: 1.5rem;">
                    
                    <!-- Left: Profile Details Edit Form -->
                    <div class="card" style="padding: 2rem;">
                        <h2 style="font-size: 1.25rem; font-weight: 800; color: #0f172a; margin-bottom: 0.35rem;">
                            Account Information
                        </h2>
                        <p style="color: #64748b; font-size: 0.85rem; margin-bottom: 1.5rem;">
                            Update administrative contact details and identity credentials.
                        </p>

                        <form action="${pageContext.request.contextPath}/profile" method="POST">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.25rem;">
                                <div>
                                    <label class="form-label">First Name *</label>
                                    <input type="text" name="firstName" class="form-input" 
                                           pattern="[a-zA-Z\s.'-]{2,50}" maxlength="50" minlength="2"
                                           title="Please enter a valid first name (letters and spaces only, 2-50 characters, no numbers)"
                                           value="<c:out value='${user.firstName}' />" required>
                                </div>
                                <div>
                                    <label class="form-label">Last Name *</label>
                                    <input type="text" name="lastName" class="form-input" 
                                           pattern="[a-zA-Z\s.'-]{1,50}" maxlength="50" minlength="1"
                                           title="Please enter a valid last name (letters and spaces only, 1-50 characters, no numbers)"
                                           value="<c:out value='${user.lastName}' />" required>
                                </div>
                            </div>

                            <div style="margin-bottom: 1.25rem;">
                                <label class="form-label">Email Address (System Login)</label>
                                <input type="email" class="form-input" value="<c:out value='${user.email}' />" disabled style="background: #f1f5f9; cursor: not-allowed;">
                                <small style="color: #64748b; font-size: 0.75rem;">Login email identifier cannot be modified directly.</small>
                            </div>

                            <div style="margin-bottom: 1.5rem;">
                                <label class="form-label">Contact Phone Number</label>
                                <input type="tel" name="phone" class="form-input" 
                                       value="<c:out value='${user.phone}' />" placeholder="e.g. 9876543210 (10-digit mobile)"
                                       pattern="[6-9][0-9]{9}" maxlength="10" minlength="10"
                                       title="Please enter a valid 10-digit Indian mobile number (e.g. 9876543210)">
                            </div>

                            <button type="submit" class="btn btn-primary" style="font-weight: 800; padding: 0.65rem 1.5rem;">
                                Save
                            </button>
                        </form>
                    </div>

                    <!-- Right: Privileges & Security Snapshot -->
                    <div>
                        <div class="card" style="padding: 1.5rem; margin-bottom: 1.5rem;">
                            <h3 style="font-size: 1.1rem; font-weight: 800; color: #0f172a; margin-bottom: 1rem; display: flex; align-items: center; gap: 0.5rem;">
                                🛡️ Admin Privileges
                            </h3>
                            <ul style="list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 0.75rem; font-size: 0.85rem;">
                                <li style="display: flex; align-items: center; gap: 0.5rem; color: #166534;">
                                    <span>✅</span> <strong>Catalog Management:</strong> Create, update & delete products
                                </li>
                                <li style="display: flex; align-items: center; gap: 0.5rem; color: #166534;">
                                    <span>✅</span> <strong>Order Control:</strong> Process fulfillment, statuses & refunds
                                </li>
                                <li style="display: flex; align-items: center; gap: 0.5rem; color: #166534;">
                                    <span>✅</span> <strong>Financial Analytics:</strong> View executive revenue & sales reports
                                </li>
                                <li style="display: flex; align-items: center; gap: 0.5rem; color: #166534;">
                                    <span>✅</span> <strong>Promotions:</strong> Manage coupons & marketing discounts
                                </li>
                                <li style="display: flex; align-items: center; gap: 0.5rem; color: #166534;">
                                    <span>✅</span> <strong>Security Audit:</strong> Complete traceability audit logging
                                </li>
                            </ul>
                        </div>

                        <div class="card" style="padding: 1.5rem; background: #eff6ff; border-color: #bfdbfe;">
                            <h3 style="font-size: 1rem; font-weight: 800; color: #1e3a8a; margin-bottom: 0.5rem;">
                                💡 Admin Workspace
                            </h3>
                            <p style="font-size: 0.8rem; color: #1e40af; margin-bottom: 1rem; line-height: 1.5;">
                                When signed in as Administrator, your session maintains active console control across all administrative modules and endpoints.
                            </p>
                            <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-primary" style="width: 100%; text-align: center; justify-content: center; font-size: 0.85rem;">
                                Go to Main Dashboard &rarr;
                            </a>
                        </div>
                    </div>
                </div>

            </section>
    </main>

    <!-- ADMIN FOOTER -->
    <footer style="text-align: center; padding: 2rem; color: #64748b; font-size: 0.85rem; border-top: 1px solid #e2e8f0; margin-top: 4rem; background: #ffffff;">
        <div style="margin-bottom: 0.35rem; color: #334155; font-weight: 600;">
            &copy; 2026 ShopKart Enterprise E-Commerce Console &bull; All rights reserved.
        </div>
        <div style="font-size: 0.82rem; color: #64748b;">
            Designed, Developed &amp; Managed by <strong style="color: #2563eb; font-weight: 800;">Rahul Pawar</strong>
        </div>
    </footer>

</body>
</html>
