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
    <title>Sales & Revenue Analytics | ShopKart Console</title>
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
                    <li><a href="${pageContext.request.contextPath}/admin/reports/sales" class="active"><span>📈</span> Sales & Revenue</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/products"><span>📦</span> Product Catalog</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/categories"><span>📁</span> Categories</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/inventory"><span>🏭</span> Inventory & Stock</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/orders"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reconciliation"><span>⚖️</span> Failed Payments &amp; Reconciliation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons & Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/admins"><span>👑</span> Admin Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0;">
                    <div>
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">Financial Sales & Revenue Analytics</h1>
                        <p style="color: #64748b; font-size: 0.875rem;">Yearly revenue curves, monthly rollups, and category contributions.</p>
                    </div>
                    
                    <form action="${pageContext.request.contextPath}/admin/reports/sales" method="GET" style="display: flex; gap: 0.5rem; align-items: center;">
                        <label for="year" style="font-size: 0.85rem; font-weight: 700; color: #334155;">Fiscal Year:</label>
                        <select name="year" id="year" onchange="this.form.submit()" class="form-select" style="padding: 0.45rem 1rem; width: auto; font-weight: 700;">
                            <option value="2026" ${selectedYear == 2026 ? 'selected' : ''}>2026 Fiscal Year</option>
                            <option value="2025" ${selectedYear == 2025 ? 'selected' : ''}>2025 Fiscal Year</option>
                            <option value="2024" ${selectedYear == 2024 ? 'selected' : ''}>2024 Fiscal Year</option>
                        </select>
                    </form>
                </div>

                <!-- Financial Sales KPI Metrics Grid -->
                <div class="grid grid-cols-4" style="margin-bottom: 1.5rem;">
                    <div class="stat-card" style="border-top: 4px solid #10b981; background: #ffffff;">
                        <div class="stat-label">Total Settled Revenue (${selectedYear})</div>
                        <div class="stat-value" style="color: #10b981; font-size: 1.75rem;">
                            ₹<fmt:formatNumber value="${totalSettledRevenue}" minFractionDigits="0" maxFractionDigits="0" />
                        </div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600; margin-top: 0.25rem;">Gross revenue from delivered orders</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #6366f1; background: #ffffff;">
                        <div class="stat-label">Net Product Sales (Pre-Tax)</div>
                        <div class="stat-value" style="color: #6366f1; font-size: 1.75rem;">
                            ₹<fmt:formatNumber value="${totalNetSales}" minFractionDigits="0" maxFractionDigits="0" />
                        </div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600; margin-top: 0.25rem;">Product price after discount</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #3b82f6; background: #ffffff;">
                        <div class="stat-label">Total Taxes Collected (GST)</div>
                        <div class="stat-value" style="color: #3b82f6; font-size: 1.75rem;">
                            ₹<fmt:formatNumber value="${totalTaxes}" minFractionDigits="0" maxFractionDigits="0" />
                        </div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600; margin-top: 0.25rem;">Applicable item taxes</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #f59e0b; background: #ffffff;">
                        <div class="stat-label">Fulfilled Orders &amp; AOV</div>
                        <div class="stat-value" style="color: #f59e0b; font-size: 1.75rem;">
                            ${totalOrdersCount} <span style="font-size: 0.95rem; font-weight: 600; color: #64748b;">(${totalUnitsSold} units)</span>
                        </div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600; margin-top: 0.25rem;">
                            Avg: <strong>₹<fmt:formatNumber value="${avgOrderValue}" minFractionDigits="0" maxFractionDigits="0" /></strong> / order
                        </div>
                    </div>
                </div>

                <!-- Monthly Revenue Breakdown -->
                <div class="card" style="padding: 0; margin-bottom: 1.5rem; overflow: hidden;">
                    <div style="padding: 1.25rem 1.5rem; border-bottom: 1px solid #e2e8f0; background: #ffffff;">
                        <h2 style="font-size: 1.15rem; font-weight: 800; color: #0f172a; margin: 0;">📅 Monthly Revenue Performance (${selectedYear})</h2>
                    </div>
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Month</th>
                                    <th>Completed Orders</th>
                                    <th style="text-align: right;">Net Sales (Pre-Tax)</th>
                                    <th style="text-align: right;">Taxes (GST)</th>
                                    <th style="text-align: right;">Total Settled Revenue</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty monthlyReport}">
                                        <tr><td colspan="5" style="text-align: center; padding: 2.5rem; color: #94a3b8;">No sales records found for ${selectedYear}.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="m" items="${monthlyReport}">
                                            <tr>
                                                <td><strong style="color: #0f172a;"><c:out value="${m.label}" /></strong></td>
                                                <td><strong style="color: #475569;">${m.orderCount}</strong> <small style="color: #94a3b8;">fulfilled orders</small></td>
                                                <td style="text-align: right; color: #6366f1; font-weight: 700;">
                                                    ₹<fmt:formatNumber value="${m.netSales}" minFractionDigits="0" maxFractionDigits="0" />
                                                </td>
                                                <td style="text-align: right; color: #3b82f6; font-weight: 700;">
                                                    ₹<fmt:formatNumber value="${m.taxAmount}" minFractionDigits="0" maxFractionDigits="0" />
                                                </td>
                                                <td style="text-align: right; color: #10b981; font-weight: 800; font-size: 1.05rem;">
                                                    ₹<fmt:formatNumber value="${m.totalSales}" minFractionDigits="0" maxFractionDigits="0" />
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Category Revenue Share -->
                <div class="card" style="padding: 0; overflow: hidden;">
                    <div style="padding: 1.25rem 1.5rem; border-bottom: 1px solid #e2e8f0; background: #ffffff;">
                        <h2 style="font-size: 1.15rem; font-weight: 800; color: #0f172a; margin: 0;">🏷️ Revenue Contribution by Category</h2>
                    </div>
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Category Taxonomy</th>
                                    <th>Orders Involved</th>
                                    <th>Units Sold</th>
                                    <th style="text-align: right;">Net Sales (Pre-Tax)</th>
                                    <th style="text-align: right;">Taxes (GST)</th>
                                    <th style="text-align: right;">Total Gross Revenue</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty categoryReport}">
                                        <tr><td colspan="6" style="text-align: center; padding: 2.5rem; color: #94a3b8;">No category sales data recorded.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="c" items="${categoryReport}">
                                            <tr>
                                                <td><strong style="color: #0f172a;"><c:out value="${c.label}" /></strong></td>
                                                <td>${c.orderCount}</td>
                                                <td><strong>${c.unitsSold}</strong> <small style="color: #94a3b8;">units</small></td>
                                                <td style="text-align: right; color: #6366f1; font-weight: 700;">
                                                    ₹<fmt:formatNumber value="${c.netSales}" minFractionDigits="0" maxFractionDigits="0" />
                                                </td>
                                                <td style="text-align: right; color: #3b82f6; font-weight: 700;">
                                                    ₹<fmt:formatNumber value="${c.taxAmount}" minFractionDigits="0" maxFractionDigits="0" />
                                                </td>
                                                <td style="text-align: right; color: #10b981; font-weight: 800; font-size: 1rem;">
                                                    ₹<fmt:formatNumber value="${c.totalSales}" minFractionDigits="0" maxFractionDigits="0" />
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
