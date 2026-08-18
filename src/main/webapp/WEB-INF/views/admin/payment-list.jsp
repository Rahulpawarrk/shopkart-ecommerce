<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Financial Transactions & Gateways | ShopKart Console</title>
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
                    <li><a href="${pageContext.request.contextPath}/admin/reports/sales"><span>📈</span> Sales & Revenue</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/products"><span>📦</span> Product Catalog</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/categories"><span>📁</span> Categories</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/inventory"><span>🏭</span> Inventory & Stock</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/orders"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments" class="active"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reconciliation"><span>⚖️</span> Failed Payments &amp; Reconciliation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons & Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                <div style="background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0; margin-bottom: 1.25rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">Financial & Payment Gateway Transactions</h1>
                        <p style="color: #64748b; font-size: 0.875rem; margin: 0;">Audit gateway authorizations, UPI / Card settlements, and refund logs.</p>
                    </div>
                    <div>
                        <a href="${pageContext.request.contextPath}/admin/reconciliation" class="btn btn-secondary btn-sm" style="color: #dc2626; border-color: #fca5a5; font-weight: 700;">
                            ⚖️ Failed Payments &amp; Disputes &rarr;
                        </a>
                    </div>
                </div>

                <!-- Financial Metrics Grid -->
                <div class="grid grid-cols-4" style="margin-bottom: 1.25rem;">
                    <div class="stat-card" style="border-top: 4px solid #6366f1;">
                        <div class="stat-label">Total Transactions</div>
                        <div class="stat-value" style="color: #6366f1;">${stats.totalTransactions}</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #10b981;">
                        <div class="stat-label">Total Collected</div>
                        <div class="stat-value" style="color: #10b981;">
                            ₹<fmt:formatNumber value="${stats.totalCollected}" minFractionDigits="0" maxFractionDigits="0" />
                        </div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #3b82f6;">
                        <div class="stat-label">Successful Settlements</div>
                        <div class="stat-value" style="color: #3b82f6;">${stats.successfulTransactions}</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #ef4444;">
                        <div class="stat-label">Failed / Declined</div>
                        <div class="stat-value" style="color: #ef4444;">${stats.failedTransactions}</div>
                        <div style="margin-top: 0.35rem;">
                            <a href="${pageContext.request.contextPath}/admin/reconciliation" style="font-size: 0.75rem; color: #dc2626; font-weight: 700; text-decoration: underline;">Reconcile Disputes &rarr;</a>
                        </div>
                    </div>
                </div>

                <!-- Search -->
                <div class="card" style="padding: 1rem 1.25rem; margin-bottom: 1.25rem;">
                    <form action="${pageContext.request.contextPath}/admin/payments" method="GET" style="display: flex; gap: 1rem;">
                        <input type="text" name="q" placeholder="Search by Txn Reference ID, Order #, Customer Full Name..." value="<c:out value='${keyword}' />" 
                               class="form-input" style="flex: 1;">
                        <button type="submit" class="btn btn-primary">Search</button>
                        <a href="${pageContext.request.contextPath}/admin/payments" class="btn btn-secondary">Reset</a>
                    </form>
                </div>

                <!-- Payments Table -->
                <div class="card" style="padding: 0; overflow: hidden;">
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Timestamp</th>
                                    <th>Txn Reference ID</th>
                                    <th>Order #</th>
                                    <th>Customer</th>
                                    <th>Payment Method</th>
                                    <th>Settled Amount</th>
                                    <th style="text-align: right;">Gateway Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty pagination.items}">
                                        <tr>
                                            <td colspan="7" style="text-align: center; padding: 2.5rem; color: #94a3b8;">
                                                No payment transactions found matching search criteria.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="p" items="${pagination.items}">
                                            <tr>
                                                <td><small style="color: #64748b;">${p.createdAt}</small></td>
                                                <td><code style="background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; color: #475569; font-weight: 700;"><c:out value="${p.transactionReference}" /></code></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/admin/orders/detail?id=${p.orderId}" style="font-weight: 700; color: #4f46e5; text-decoration: none;">
                                                        <c:out value="${p.orderNumber}" /> ↗
                                                    </a>
                                                </td>
                                                <td><strong style="color: #1e293b;"><c:out value="${p.customerName}" /></strong></td>
                                                <td><span class="badge badge-info"><c:out value="${p.paymentMethod}" /></span></td>
                                                <td><strong style="color: #0f172a; font-size: 0.95rem;">₹<fmt:formatNumber value="${p.amount}" minFractionDigits="0" /></strong></td>
                                                <td style="text-align: right;">
                                                    <c:choose>
                                                        <c:when test="${p.paymentStatus == 'SUCCESS'}">
                                                            <span class="badge badge-success">✓ SUCCESS</span>
                                                        </c:when>
                                                        <c:when test="${p.paymentStatus == 'FAILED'}">
                                                            <span class="badge badge-danger">✕ FAILED</span>
                                                        </c:when>
                                                        <c:when test="${p.paymentStatus == 'REFUNDED'}">
                                                            <span class="badge badge-warning">REFUNDED</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-info"><c:out value="${p.paymentStatus}" /></span>
                                                        </c:otherwise>
                                                    </c:choose>
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
