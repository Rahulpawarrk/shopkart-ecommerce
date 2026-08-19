<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Customer Order Management | ShopKart Console</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link
                    href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap"
                    rel="stylesheet">
            </head>

            <body style="background-color: #f8fafc;">

                <!-- TOP ADMIN NAVBAR -->
                <header class="admin-navbar">
                    <a href="${pageContext.request.contextPath}/admin/dashboard" class="brand">
                        <img src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="ShopKart"
                            style="height: 36px; width: auto;">
                        <span class="admin-brand-pill">ADMIN CONSOLE</span>
                    </a>
                    <ul class="nav-links">
                        <li><a href="${pageContext.request.contextPath}/" target="_blank">🌐 Open Storefront ↗</a></li>
                        <li><span style="font-weight: 700; color: #e2e8f0; font-size: 0.85rem;">👑 Admin:
                                <c:out value="${sessionScope.currentUser.fullName}" />
                            </span></li>
                        <li><a href="${pageContext.request.contextPath}/logout"
                                style="color: #f87171; font-weight: 700;">🚪 Sign Out</a></li>
                    </ul>
                </header>

                <main class="admin-container">
                    <div class="admin-layout">

                        <!-- Left Sidebar Navigation -->
                        <aside class="admin-sidebar-card">
                            <div class="admin-nav-header">MAIN NAVIGATION</div>
                            <ul class="admin-nav">
                                <li><a href="${pageContext.request.contextPath}/admin/dashboard"><span>📊</span>
                                        Dashboard Overview</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/reports/sales"><span>📈</span>
                                        Sales & Revenue</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/products"><span>📦</span> Product
                                        Catalog</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/categories"><span>📁</span>
                                        Categories</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/inventory"><span>🏭</span>
                                        Inventory & Stock</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/orders"
                                        class="active"><span>🛒</span> Order Management</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/returns"><span>🔄</span> Return
                                        Requests</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment
                                        History</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/reconciliation"><span>⚖️</span>
                                        Failed Payments &amp; Reconciliation</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review
                                        Moderation</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons
                                        & Offers</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/admins"><span>👑</span>
                                        Admin Management</a></li>
                                <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span>
                                        Security Audit Logs</a></li>
                            </ul>
                        </aside>

                        <!-- Main Content Area -->
                        <section>
                            <div
                                style="background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0; margin-bottom: 1.25rem;">
                                <h1
                                    style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">
                                    Customer Order Fulfillment</h1>
                                <p style="color: #64748b; font-size: 0.875rem;">Track shipments, dispatch orders, verify
                                    invoice breakdowns, and process logistics.</p>
                            </div>

                            <!-- Order Status Metrics Grid -->
                            <div class="grid grid-cols-4" style="margin-bottom: 1.25rem;">
                                <div class="stat-card" style="border-top: 4px solid #6366f1;">
                                    <div class="stat-label">Total Orders</div>
                                    <div class="stat-value" style="color: #6366f1;">
                                        <c:out value="${not empty stats.totalOrders ? stats.totalOrders : 0}" />
                                    </div>
                                </div>
                                <div class="stat-card" style="border-top: 4px solid #f59e0b;">
                                    <div class="stat-label">Processing / Pending</div>
                                    <div class="stat-value" style="color: #f59e0b;">
                                        <c:out
                                            value="${(not empty stats.processingOrders ? stats.processingOrders : 0) + (not empty stats.pendingOrders ? stats.pendingOrders : 0)}" />
                                    </div>
                                </div>
                                <div class="stat-card" style="border-top: 4px solid #3b82f6;">
                                    <div class="stat-label">In Transit (Shipped)</div>
                                    <div class="stat-value" style="color: #3b82f6;">
                                        <c:out value="${not empty stats.inTransitOrders ? stats.inTransitOrders : 0}" />
                                    </div>
                                </div>
                                <div class="stat-card" style="border-top: 4px solid #10b981;">
                                    <div class="stat-label">Delivered Orders</div>
                                    <div class="stat-value" style="color: #10b981;">
                                        <c:out value="${not empty stats.deliveredOrders ? stats.deliveredOrders : 0}" />
                                    </div>
                                </div>
                            </div>

                            <!-- Clean Filter Tabs (No empty parentheses) -->
                            <div class="tabs" style="flex-wrap: wrap;">
                                <a href="${pageContext.request.contextPath}/admin/orders"
                                    class="tab-item ${empty status || status == 'ALL' ? 'active' : ''}">
                                    All Orders <c:if test="${not empty stats.totalOrders && stats.totalOrders > 0}">
                                        (${stats.totalOrders})</c:if>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/orders?status=PROCESSING"
                                    class="tab-item ${status == 'PROCESSING' ? 'active' : ''}">
                                    📦 Processing <c:if
                                        test="${not empty stats.processingOrders && stats.processingOrders > 0}">
                                        (${stats.processingOrders})</c:if>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/orders?status=DISPATCHED"
                                    class="tab-item ${status == 'DISPATCHED' ? 'active' : ''}">
                                    🚚 Dispatched <c:if
                                        test="${not empty stats.dispatchedOrders && stats.dispatchedOrders > 0}">
                                        (${stats.dispatchedOrders})</c:if>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/orders?status=IN_TRANSIT"
                                    class="tab-item ${status == 'IN_TRANSIT' || status == 'SHIPPED' ? 'active' : ''}">
                                    ✈️ In Transit <c:if
                                        test="${not empty stats.inTransitOrders && stats.inTransitOrders > 0}">
                                        (${stats.inTransitOrders})</c:if>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/orders?status=OUT_FOR_DELIVERY"
                                    class="tab-item ${status == 'OUT_FOR_DELIVERY' ? 'active' : ''}">
                                    🛵 Out for Delivery <c:if
                                        test="${not empty stats.outForDeliveryOrders && stats.outForDeliveryOrders > 0}">
                                        (${stats.outForDeliveryOrders})</c:if>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/orders?status=DELIVERED"
                                    class="tab-item ${status == 'DELIVERED' ? 'active' : ''}">
                                    ✅ Delivered <c:if
                                        test="${not empty stats.deliveredOrders && stats.deliveredOrders > 0}">
                                        (${stats.deliveredOrders})</c:if>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/orders?status=CANCELLED"
                                    class="tab-item ${status == 'CANCELLED' ? 'active' : ''}">
                                    ✕ Cancelled <c:if
                                        test="${not empty stats.cancelledOrders && stats.cancelledOrders > 0}">
                                        (${stats.cancelledOrders})</c:if>
                                </a>
                            </div>

                            <!-- Search Form -->
                            <div class="card" style="padding: 1rem 1.25rem; margin-bottom: 1.25rem;">
                                <form action="${pageContext.request.contextPath}/admin/orders" method="GET"
                                    style="display: flex; gap: 1rem;">
                                    <input type="hidden" name="status" value="${status}">
                                    <input type="text" name="q"
                                        placeholder="Search by Order #, Customer Full Name, Email..."
                                        value="<c:out value='${keyword}' />" class="form-input" style="flex: 1;">
                                    <button type="submit" class="btn btn-primary">Search</button>
                                    <a href="${pageContext.request.contextPath}/admin/orders"
                                        class="btn btn-secondary">Reset</a>
                                </form>
                            </div>

                            <!-- Orders Table Card -->
                            <div class="card" style="padding: 0; overflow: hidden;">
                                <div class="table-responsive" style="border: none;">
                                    <table class="admin-table">
                                        <thead>
                                            <tr>
                                                <th>Order #</th>
                                                <th>Customer</th>
                                                <th>Date</th>
                                                <th>Payment Method</th>
                                                <th>Total Amount</th>
                                                <th>Status</th>
                                                <th style="text-align: right;">Action</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${empty pagination.items}">
                                                    <tr>
                                                        <td colspan="7"
                                                            style="text-align: center; padding: 2.5rem; color: #94a3b8;">
                                                            No customer orders matching the specified filter criteria.
                                                        </td>
                                                    </tr>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach var="order" items="${pagination.items}">
                                                        <tr>
                                                            <td><code
                                                                    style="background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; color: #475569; font-weight: 700;"><c:out value="${order.orderNumber}" /></code>
                                                            </td>
                                                            <td>
                                                                <strong style="color: #0f172a;">
                                                                    <c:out value="${order.customerName}" />
                                                                </strong><br>
                                                                <small style="color: #64748b;">
                                                                    <c:out value="${order.customerEmail}" />
                                                                </small>
                                                            </td>
                                                            <td><small
                                                                    style="color: #64748b;">${order.createdAt}</small>
                                                            </td>
                                                            <td>
                                                                <span style="font-weight: 700; color: #334155;">
                                                                    <c:out value="${order.paymentMethod}" />
                                                                </span>
                                                                <c:choose>
                                                                    <c:when test="${order.paymentMethod == 'COD'}">
                                                                        <c:if
                                                                            test="${order.paymentStatus == 'PAID' || order.orderStatus == 'DELIVERED'}">
                                                                            <br><small class="badge badge-success"
                                                                                style="font-size: 0.65rem;">PAID</small>
                                                                        </c:if>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <br><small
                                                                            class="badge ${order.paymentStatus == 'PAID' ? 'badge-success' : 'badge-info'}"
                                                                            style="font-size: 0.65rem;">
                                                                            <c:out value="${order.paymentStatus}" />
                                                                        </small>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td><strong style="color: #0f172a; font-size: 0.95rem;">₹
                                                                    <fmt:formatNumber value="${order.totalAmount}"
                                                                        minFractionDigits="0" />
                                                                </strong></td>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${order.orderStatus == 'DELIVERED'}">
                                                                        <span class="badge badge-success">✓
                                                                            Delivered</span>
                                                                    </c:when>
                                                                    <c:when test="${order.orderStatus == 'CANCELLED'}">
                                                                        <span class="badge badge-danger">✕
                                                                            Cancelled</span>
                                                                    </c:when>
                                                                    <c:when test="${order.orderStatus == 'SHIPPED'}">
                                                                        <span class="badge badge-info">🚚 Shipped</span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="badge badge-warning">⏳
                                                                            <c:out value="${order.orderStatus}" />
                                                                        </span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td style="text-align: right;">
                                                                <a href="${pageContext.request.contextPath}/admin/orders/detail?id=${order.orderId}"
                                                                    class="btn btn-secondary btn-sm">
                                                                    Manage Details &rarr;
                                                                </a>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            <!-- Pagination -->
                            <c:if test="${pagination.totalPages > 1}">
                                <div style="display: flex; justify-content: center; gap: 0.5rem; margin-top: 1.5rem;">
                                    <c:forEach begin="1" end="${pagination.totalPages}" var="p">
                                        <a href="${pageContext.request.contextPath}/admin/orders?page=${p}&status=${status}&q=${keyword}"
                                            class="btn ${p == pagination.currentPage ? 'btn-primary' : 'btn-secondary'} btn-sm">${p}</a>
                                    </c:forEach>
                                </div>
                            </c:if>

                        </section>
                    </div>
                </main>

                <!-- ADMIN FOOTER -->
                <footer
                    style="text-align: center; padding: 2rem; color: #64748b; font-size: 0.85rem; border-top: 1px solid #e2e8f0; margin-top: 4rem; background: #ffffff;">
                    <div style="margin-bottom: 0.35rem; color: #334155; font-weight: 600;">
                        &copy; 2026 ShopKart Enterprise E-Commerce Console &bull; All rights reserved.
                    </div>
                    <div style="font-size: 0.82rem; color: #64748b;">
                        Designed, Developed &amp; Managed by <strong style="color: #2563eb; font-weight: 800;">Rahul
                            Pawar</strong>
                    </div>
                </footer>

            </body>

            </html>