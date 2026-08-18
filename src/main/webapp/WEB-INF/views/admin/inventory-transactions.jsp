<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inventory Movement Audit Ledger | ShopKart Console</title>
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
            <li><a href="${pageContext.request.contextPath}/admin/inventory">← Back to Inventory</a></li>
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
                    <li><a href="${pageContext.request.contextPath}/admin/inventory" class="active"><span>🏭</span> Inventory & Stock</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/orders"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons & Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                <div class="card" style="border-left: 4px solid #6366f1; margin-bottom: 1.25rem; padding: 1.25rem 1.5rem;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h1 style="font-size: 1.35rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">
                                Stock Audit Ledger: <c:out value="${inventory.productName}" />
                            </h1>
                            <p style="color: #64748b; font-size: 0.875rem; margin: 0;">
                                SKU: <code style="background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; color: #475569; font-weight: 700;"><c:out value="${inventory.sku}" /></code> &bull; Current Live Balance: <strong style="color: #0f172a;">${inventory.quantity} units</strong>
                            </p>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/inventory" class="btn btn-secondary">
                            &larr; Back to Inventory
                        </a>
                    </div>
                </div>

                <!-- Transactions Table -->
                <div class="card" style="padding: 0; overflow: hidden;">
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Timestamp</th>
                                    <th>Transaction Type</th>
                                    <th>Prev Stock</th>
                                    <th>Change</th>
                                    <th>New Balance</th>
                                    <th>Reference & Remarks</th>
                                    <th style="text-align: right;">Initiated By</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty transactions.items}">
                                        <tr>
                                            <td colspan="7" style="text-align: center; padding: 2.5rem; color: #94a3b8;">
                                                No stock movement transactions recorded yet for this product.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="t" items="${transactions.items}">
                                            <tr>
                                                <td><small style="color: #64748b;">${t.createdAt}</small></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${t.transactionType == 'PURCHASE'}">
                                                            <span class="badge badge-success">PURCHASE (Restock)</span>
                                                        </c:when>
                                                        <c:when test="${t.transactionType == 'SALE'}">
                                                            <span class="badge badge-danger">SALE (Order)</span>
                                                        </c:when>
                                                        <c:when test="${t.transactionType == 'RETURN'}">
                                                            <span class="badge badge-info">RETURN (Restocked)</span>
                                                        </c:when>
                                                        <c:when test="${t.transactionType == 'CANCELLATION'}">
                                                            <span class="badge badge-info">CANCELLATION</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-warning">ADJUSTMENT</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>${t.previousStock}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${t.quantityChanged > 0}">
                                                            <strong style="color: #10b981; font-weight: 800;">+${t.quantityChanged}</strong>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <strong style="color: #ef4444; font-weight: 800;">${t.quantityChanged}</strong>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td><strong style="color: #0f172a;">${t.newStock}</strong></td>
                                                <td>
                                                    <strong style="color: #1e293b;"><c:out value="${t.referenceType}" /></strong><br>
                                                    <small style="color: #64748b;"><c:out value="${t.remarks}" /></small>
                                                </td>
                                                <td style="text-align: right;"><strong style="color: #475569;"><c:out value="${t.createdByName}" /></strong></td>
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
