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
    <title>${isEdit ? 'Edit Coupon' : 'Create Coupon'} | ShopKart Console</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body style="background-color: #f8fafc;">

    <!-- TOP ADMIN NAVBAR -->
    <header class="admin-navbar">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="brand">
            <img src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="ShopKart" style="height: 36px; width: auto;">
            <span class="admin-brand-pill">ADMIN CONSOLE</span>
        </a>
        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/admin/coupons">← Back to Coupons</a></li>
            <li><a href="${pageContext.request.contextPath}/" target="_blank">🌐 Open Storefront ↗</a></li>
            <li><span style="font-weight: 700; color: #e2e8f0; font-size: 0.85rem;">👑 Admin: <c:out value="${sessionScope.currentUser.fullName}" /></span></li>
            <li><a href="${pageContext.request.contextPath}/logout" style="color: #f87171; font-weight: 700;">🚪 Sign Out</a></li>
        </ul>
    </header>

    <main class="admin-container">
        <div class="admin-layout">
            
            <!-- Left Sidebar Navigation -->
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
                    <li><a href="${pageContext.request.contextPath}/admin/coupons" class="active"><span>🏷️</span> Coupons &amp; Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                <div class="card" style="max-width: 760px; margin: 0 auto; padding: 2rem;">
                    <div style="border-bottom: 1px solid #e2e8f0; padding-bottom: 1rem; margin-bottom: 1.5rem;">
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">
                            ${isEdit ? 'Edit Coupon Campaign' : 'Create Promotional Coupon'}
                        </h1>
                        <p style="color: #64748b; font-size: 0.875rem;">
                            Set code triggers, percentage / flat reductions, order threshold limits, and global caps.
                        </p>
                    </div>

                    <c:if test="${not empty errors}">
                        <div style="background: #fee2e2; border-left: 4px solid #ef4444; color: #b91c1c; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; font-weight: 600;">
                            <ul style="margin-left: 1.25rem; margin-bottom: 0;">
                                <c:forEach var="err" items="${errors}">
                                    <li><c:out value="${err}" /></li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/admin/coupons/${isEdit ? 'edit' : 'add'}" method="POST">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <c:if test="${isEdit}">
                            <input type="hidden" name="couponId" value="${coupon.couponId}">
                        </c:if>

                        <div style="display: grid; grid-template-columns: 1fr 1.5fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                            <div class="form-group" style="margin-bottom: 0;">
                                <label class="form-label">Promo Code *</label>
                                <input type="text" name="code" required class="form-input" value="<c:out value='${coupon.code}' />" 
                                       placeholder="e.g. SAVE20" style="text-transform: uppercase; font-family: monospace; font-weight: 800; letter-spacing: 1px;">
                            </div>
                            <div class="form-group" style="margin-bottom: 0;">
                                <label class="form-label">Description / Campaign Purpose</label>
                                <input type="text" name="description" class="form-input" value="<c:out value='${coupon.description}' />" 
                                       placeholder="e.g. 20% festive discount across catalog">
                            </div>
                        </div>

                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                            <div class="form-group" style="margin-bottom: 0;">
                                <label class="form-label">Discount Type *</label>
                                <select name="discountType" class="form-select">
                                    <option value="PERCENTAGE" ${coupon.discountType == 'PERCENTAGE' ? 'selected' : ''}>Percentage Discount (%)</option>
                                    <option value="FIXED_AMOUNT" ${coupon.discountType == 'FIXED_AMOUNT' ? 'selected' : ''}>Fixed Amount Discount (₹)</option>
                                </select>
                            </div>
                            <div class="form-group" style="margin-bottom: 0;">
                                <label class="form-label">Discount Value *</label>
                                <input type="number" step="0.01" min="0.01" name="discountValue" required class="form-input" value="${coupon.discountValue}" placeholder="e.g. 20.00 or 500.00">
                            </div>
                        </div>

                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                            <div class="form-group" style="margin-bottom: 0;">
                                <label class="form-label">Minimum Order Spend (₹)</label>
                                <input type="number" step="0.01" min="0" name="minSpend" class="form-input" value="${coupon.minSpend}" placeholder="499.00">
                            </div>
                            <div class="form-group" style="margin-bottom: 0;">
                                <label class="form-label">Maximum Discount Cap (₹) <small style="color:#64748b;">(For % promos)</small></label>
                                <input type="number" step="0.01" min="0" name="maxDiscount" class="form-input" value="${coupon.maxDiscount}" placeholder="Leave blank for no cap">
                            </div>
                        </div>

                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                            <div class="form-group" style="margin-bottom: 0;">
                                <label class="form-label">Total Global Usage Limit</label>
                                <input type="number" min="1" name="usageLimit" class="form-input" value="${coupon.usageLimit}" placeholder="Leave blank for unlimited uses">
                            </div>
                            <div style="display: flex; align-items: center; gap: 0.5rem; margin-top: 1.75rem;">
                                <input type="checkbox" id="active" name="active" value="true" ${coupon.active ? 'checked' : ''} style="width: 1.2rem; height: 1.2rem; cursor: pointer;">
                                <label for="active" style="font-weight: 700; color: #0f172a; cursor: pointer;">Enable Coupon for Customer Checkout</label>
                            </div>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary" style="padding: 0.65rem 1.75rem;">
                                ${isEdit ? '✓ Update Coupon' : '✓ Save & Publish Coupon'}
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/coupons" class="btn btn-secondary">
                                Cancel
                            </a>
                        </div>
                    </form>
                </div>
            </section>

        </div>
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
