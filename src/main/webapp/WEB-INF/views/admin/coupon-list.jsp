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
    <title>Coupons & Promotional Engine | ShopKart Console</title>
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
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0;">
                    <div>
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">Coupons & Promotional Engine</h1>
                        <p style="color: #64748b; font-size: 0.875rem;">Create percentage / flat voucher campaigns, minimum spend rules, and max discount caps.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/coupons/add" class="btn btn-primary">
                        + Create New Coupon
                    </a>
                </div>

                <!-- Stats Cards -->
                <div class="grid grid-cols-3" style="margin-bottom: 1.25rem;">
                    <div class="stat-card" style="border-top: 4px solid #6366f1;">
                        <div class="stat-label">Total Promo Codes</div>
                        <div class="stat-value" style="color: #6366f1;">${stats.totalCoupons}</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #10b981;">
                        <div class="stat-label">Active Campaigns</div>
                        <div class="stat-value" style="color: #10b981;">${stats.activeCoupons}</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #f59e0b;">
                        <div class="stat-label">Total Redemptions</div>
                        <div class="stat-value" style="color: #f59e0b;">${stats.totalRedemptions}</div>
                    </div>
                </div>

                <!-- Search -->
                <div class="card" style="padding: 1rem 1.25rem; margin-bottom: 1.25rem;">
                    <form action="${pageContext.request.contextPath}/admin/coupons" method="GET" style="display: flex; gap: 1rem;">
                        <input type="text" name="q" placeholder="Search by Coupon Code, Title or Campaign Description..." value="<c:out value='${keyword}' />" 
                               class="form-input" style="flex: 1;">
                        <button type="submit" class="btn btn-primary">Search</button>
                        <a href="${pageContext.request.contextPath}/admin/coupons" class="btn btn-secondary">Reset</a>
                    </form>
                </div>

                <!-- Coupons Table -->
                <div class="card" style="padding: 0; overflow: hidden;">
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Promo Code</th>
                                    <th>Discount Type</th>
                                    <th>Min Spend</th>
                                    <th>Max Cap</th>
                                    <th>Redemptions</th>
                                    <th>Status</th>
                                    <th style="text-align: right;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty pagination.items}">
                                        <tr>
                                            <td colspan="7" style="text-align: center; padding: 2.5rem; color: #94a3b8;">
                                                No promo coupons found matching query.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="c" items="${pagination.items}">
                                            <tr>
                                                <td>
                                                    <span style="font-family: monospace; font-weight: 800; font-size: 0.95rem; color: #4338ca; background: #e0e7ff; padding: 0.25rem 0.6rem; border-radius: 4px; border: 1px solid #c7d2fe;">
                                                        <c:out value="${c.code}" />
                                                    </span>
                                                    <div style="font-size: 0.8rem; color: #64748b; margin-top: 0.25rem;"><c:out value="${c.description}" /></div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${c.discountType == 'PERCENTAGE'}">
                                                            <strong style="color: #10b981; font-weight: 800;"><fmt:formatNumber value="${c.discountValue}" maxFractionDigits="0" />% OFF</strong>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <strong style="color: #10b981; font-weight: 800;">₹<fmt:formatNumber value="${c.discountValue}" minFractionDigits="0" /> Flat</strong>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>₹<fmt:formatNumber value="${c.minSpend}" minFractionDigits="0" /></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty c.maxDiscount}">
                                                            ₹<fmt:formatNumber value="${c.maxDiscount}" minFractionDigits="0" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span style="color: #94a3b8;">No Cap</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <strong>${c.usedCount}</strong>
                                                    <c:if test="${not empty c.usageLimit}">
                                                        <small style="color: #64748b;">/ ${c.usageLimit}</small>
                                                    </c:if>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${c.validNow}">
                                                            <span class="badge badge-success">ACTIVE</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-danger">INACTIVE / EXPIRED</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 0.35rem;">
                                                        <a href="${pageContext.request.contextPath}/admin/coupons/edit?id=${c.couponId}" class="btn btn-secondary btn-sm">✏️ Edit</a>
                                                        <form action="${pageContext.request.contextPath}/admin/coupons/toggle" method="POST">
                                                            <input type="hidden" name="_csrf" value="${csrfToken}">
                                                            <input type="hidden" name="couponId" value="${c.couponId}">
                                                            <input type="hidden" name="active" value="${!c.active}">
                                                            <button type="submit" class="btn btn-secondary btn-sm" style="cursor: pointer;">
                                                                ${c.active ? 'Disable' : 'Enable'}
                                                            </button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
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
