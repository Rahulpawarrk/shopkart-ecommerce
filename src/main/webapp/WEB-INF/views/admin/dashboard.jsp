<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Executive Admin Dashboard | ShopKart Console</title>
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
            <li><span style="font-weight: 700; color: #e2e8f0; font-size: 0.85rem;">👑 Admin: <c:out value="${adminName != null ? adminName : sessionScope.currentUser.fullName}" /></span></li>
            <li><a href="${pageContext.request.contextPath}/logout" style="color: #f87171; font-weight: 700;">🚪 Sign Out</a></li>
        </ul>
    </header>

    <main class="admin-container">
        <div class="admin-layout">
            
            <!-- Left Navigation Sidebar -->
            <aside class="admin-sidebar-card">
                <div class="admin-nav-header">MAIN NAVIGATION</div>
                <ul class="admin-nav">
                    <li><a href="${pageContext.request.contextPath}/admin/dashboard" class="active"><span>📊</span> Dashboard Overview</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reports/sales"><span>📈</span> Sales & Revenue</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/products"><span>📦</span> Product Catalog</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/categories"><span>📁</span> Categories</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/inventory"><span>🏭</span> Inventory & Stock</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/orders"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/returns"><span>🔄</span> Return Requests</a></li>
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
                
                <c:if test="${not empty param.error}">
                    <div style="background: #fffbeb; border: 1.5px solid #f59e0b; border-radius: var(--radius-md); padding: 1rem 1.5rem; margin-bottom: 1.5rem; display: flex; align-items: center; gap: 0.85rem; color: #92400e; font-size: 0.9rem; font-weight: 600; box-shadow: 0 4px 12px rgba(245, 158, 11, 0.1);">
                        <span style="font-size: 1.4rem;">🔒</span>
                        <div>
                            <strong style="color: #78350f;">Role Restriction Notice:</strong>
                            <c:choose>
                                <c:when test="${param.error == 'admin_cannot_shop' || param.error == 'admin_restricted'}">
                                    Administrator accounts are restricted to the Admin Console and cannot add items to cart, checkout, or place customer orders.
                                </c:when>
                                <c:when test="${param.error == 'admin_cannot_manage_addresses'}">
                                    Customer delivery addresses cannot be added by admin accounts. Admins manage store logistics via the Admin Console.
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${param.error}" />
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:if>

                <!-- Welcome & Actions Banner -->
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0; box-shadow: 0 2px 8px rgba(0,0,0,0.03);">
                    <div>
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">
                            Executive Store Metrics
                        </h1>
                        <p style="color: #64748b; font-size: 0.875rem;">
                            Live store metrics, revenue performance, and inventory health across ShopKart.
                        </p>
                    </div>
                    <div style="display: flex; gap: 0.75rem;">
                        <a href="${pageContext.request.contextPath}/admin/products/new" class="btn btn-primary btn-sm">
                            + Add New Product
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/reports/sales" class="btn btn-secondary btn-sm">
                            📊 Full Sales Report &rarr;
                        </a>
                    </div>
                </div>

                <!-- Primary KPI Metrics Grid -->
                <div class="grid grid-cols-4" style="margin-bottom: 1.5rem;">
                    
                    <!-- KPI 1: Revenue -->
                    <div class="stat-card" style="border-top: 4px solid #10b981;">
                        <div class="stat-label">Total Gross Revenue</div>
                        <div class="stat-value" style="color: #10b981;">
                            ₹<fmt:formatNumber value="${stats.totalRevenue}" minFractionDigits="0" maxFractionDigits="0" />
                        </div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600;">
                            Today: <strong style="color: #0f172a;">₹<fmt:formatNumber value="${stats.todayRevenue}" minFractionDigits="0" /></strong>
                        </div>
                    </div>

                    <!-- KPI 2: Orders -->
                    <div class="stat-card" style="border-top: 4px solid #6366f1;">
                        <div class="stat-label">Total Orders Placed</div>
                        <div class="stat-value" style="color: #6366f1;">${stats.totalOrders}</div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600;">
                            Today: <strong style="color: #0f172a;">${stats.todayOrders} new orders</strong>
                        </div>
                    </div>

                    <!-- KPI 3: Products -->
                    <div class="stat-card" style="border-top: 4px solid #3b82f6;">
                        <div class="stat-label">Active Catalog Products</div>
                        <div class="stat-value" style="color: #3b82f6;">${stats.totalProducts}</div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600;">Across active departments</div>
                    </div>

                    <!-- KPI 4: Customers -->
                    <div class="stat-card" style="border-top: 4px solid #f59e0b;">
                        <div class="stat-label">Registered Customers</div>
                        <div class="stat-value" style="color: #f59e0b;">${stats.totalCustomers}</div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600;">Verified buyer accounts</div>
                    </div>
                </div>

                <!-- Inventory & Fulfillment Alerts -->
                <c:if test="${stats.lowStockProducts > 0 || stats.pendingOrders > 0}">
                    <div class="grid grid-cols-2" style="gap: 1rem; margin-bottom: 1.5rem;">
                        <c:if test="${stats.lowStockProducts > 0}">
                            <div class="card" style="background-color: #fffbeb; border: 1px solid #fde68a; padding: 1rem 1.25rem; display: flex; justify-content: space-between; align-items: center; margin-bottom: 0;">
                                <div style="display: flex; align-items: center; gap: 0.75rem;">
                                    <span style="font-size: 1.5rem;">⚠️</span>
                                    <div>
                                        <strong style="color: #92400e; font-size: 0.9rem;">Low Stock Warning:</strong>
                                        <div style="color: #78350f; font-size: 0.8rem;">${stats.lowStockProducts} products have &le; 5 units remaining in warehouse.</div>
                                    </div>
                                </div>
                                <a href="${pageContext.request.contextPath}/admin/inventory" class="btn btn-secondary btn-sm" style="background: #ffffff;">Restock Now</a>
                            </div>
                        </c:if>
                        <c:if test="${stats.pendingOrders > 0}">
                            <div class="card" style="background-color: #eef2ff; border: 1px solid #c7d2fe; padding: 1rem 1.25rem; display: flex; justify-content: space-between; align-items: center; margin-bottom: 0;">
                                <div style="display: flex; align-items: center; gap: 0.75rem;">
                                    <span style="font-size: 1.5rem;">📦</span>
                                    <div>
                                        <strong style="color: #3730a3; font-size: 0.9rem;">Pending Orders:</strong>
                                        <div style="color: #312e81; font-size: 0.8rem;">${stats.pendingOrders} orders require shipping dispatch and packaging.</div>
                                    </div>
                                </div>
                                <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-primary btn-sm">Process Shipments</a>
                            </div>
                        </c:if>
                    </div>
                </c:if>

                <!-- Dual Tables: Recent Orders & Top Products -->
                <div class="grid grid-cols-2" style="gap: 1.5rem;">
                    
                    <!-- Recent Orders -->
                    <div class="card" style="padding: 0; overflow: hidden; margin-bottom: 0;">
                        <div style="padding: 1.15rem 1.25rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; background: #ffffff;">
                            <h2 style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin: 0;">Recent Orders</h2>
                            <a href="${pageContext.request.contextPath}/admin/orders" style="font-size: 0.8rem; color: #6366f1; text-decoration: none; font-weight: 700;">View All Orders →</a>
                        </div>
                        <div class="table-responsive" style="border: none;">
                            <table class="admin-table">
                                <thead>
                                    <tr>
                                        <th>Order #</th>
                                        <th>Customer</th>
                                        <th>Amount</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty recentOrders}">
                                            <tr><td colspan="4" style="text-align: center; padding: 2rem; color: #94a3b8;">No recent orders recorded yet.</td></tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="o" items="${recentOrders}">
                                                <tr>
                                                    <td>
                                                        <a href="${pageContext.request.contextPath}/admin/orders/detail?id=${o.orderId}" style="color: #4f46e5; font-weight: 700; text-decoration: none;">
                                                            <c:out value="${o.orderNumber}" />
                                                        </a>
                                                    </td>
                                                    <td><strong style="color: #334155;"><c:out value="${o.customerName}" /></strong></td>
                                                    <td><strong>₹<fmt:formatNumber value="${o.totalAmount}" minFractionDigits="0" /></strong></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${o.orderStatus == 'DELIVERED'}">
                                                                <span class="badge badge-success">✓ Delivered</span>
                                                            </c:when>
                                                            <c:when test="${o.orderStatus == 'SHIPPED'}">
                                                                <span class="badge badge-info">🚚 Shipped</span>
                                                            </c:when>
                                                            <c:when test="${o.orderStatus == 'CONFIRMED' || o.orderStatus == 'PROCESSING'}">
                                                                <span class="badge badge-warning">⏳ Processing</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge badge-primary"><c:out value="${o.orderStatus}" /></span>
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

                    <!-- Top Selling Products -->
                    <div class="card" style="padding: 0; overflow: hidden; margin-bottom: 0;">
                        <div style="padding: 1.15rem 1.25rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; background: #ffffff;">
                            <h2 style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin: 0;">Top Selling Products</h2>
                            <a href="${pageContext.request.contextPath}/admin/products" style="font-size: 0.8rem; color: #6366f1; text-decoration: none; font-weight: 700;">Catalog →</a>
                        </div>
                        <div class="table-responsive" style="border: none;">
                            <table class="admin-table">
                                <thead>
                                    <tr>
                                        <th>Product</th>
                                        <th>Units</th>
                                        <th>Revenue</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty topProducts}">
                                            <tr><td colspan="3" style="text-align: center; padding: 2rem; color: #94a3b8;">No sales recorded yet.</td></tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="tp" items="${topProducts}">
                                                <tr>
                                                    <td>
                                                        <strong style="color: #1e293b;"><c:out value="${tp.productName}" /></strong><br>
                                                        <small style="color: #64748b; font-family: monospace;">SKU: <c:out value="${tp.sku}" /></small>
                                                    </td>
                                                    <td><strong>${tp.unitsSold}</strong></td>
                                                    <td style="color: #10b981; font-weight: 800;">
                                                        ₹<fmt:formatNumber value="${tp.totalRevenue}" minFractionDigits="0" />
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
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
