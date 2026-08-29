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
    <title>Product Catalog Management | ShopKart Console</title>
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
                    <li><a href="${pageContext.request.contextPath}/admin/reports/sales"><span>📈</span> Sales & Revenue</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/products" class="active"><span>📦</span> Product Catalog</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/categories"><span>📁</span> Categories</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/inventory"><span>🏭</span> Inventory & Stock</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/orders"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reconciliation"><span>⚖️</span> Failed Payments &amp; Reconciliation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons & Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0;">
                    <div>
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">Product Catalog Management</h1>
                        <p style="color: #64748b; font-size: 0.875rem;">Manage active merchandise, SKU definitions, tier pricing, and live inventory status.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/products/add" class="btn btn-primary">
                        + Add New Product
                    </a>
                </div>

                <c:if test="${param.saved == 'true'}">
                    <div style="background: #dcfce7; border-left: 4px solid #10b981; color: #15803d; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; font-weight: 600;">
                        ✓ Product details saved and synchronized successfully!
                    </div>
                </c:if>

                <!-- Search & Filters Bar -->
                <div class="card" style="padding: 1rem 1.25rem; margin-bottom: 1.25rem;">
                    <form action="${pageContext.request.contextPath}/admin/products" method="GET" style="display: flex; gap: 1rem; flex-wrap: wrap;">
                        <input type="text" name="q" placeholder="Search by SKU, Product Name, Brand..." value="<c:out value='${criteria.keyword}' />" 
                               class="form-input" style="flex: 2;">
                        
                        <select name="status" class="form-select" style="flex: 1;">
                            <option value="">All Statuses</option>
                            <option value="ACTIVE" <c:if test="${criteria.status == 'ACTIVE'}">selected</c:if>>Active</option>
                            <option value="INACTIVE" <c:if test="${criteria.status == 'INACTIVE'}">selected</c:if>>Inactive</option>
                            <option value="ARCHIVED" <c:if test="${criteria.status == 'ARCHIVED'}">selected</c:if>>Archived</option>
                        </select>

                        <button type="submit" class="btn btn-primary">Search</button>
                        <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-secondary">Reset</a>
                    </form>
                </div>

                <!-- Products Data Table -->
                <div class="card" style="padding: 0; overflow: hidden;">
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>SKU</th>
                                    <th>Product Details</th>
                                    <th>Category</th>
                                    <th>Base Price</th>
                                    <th>Discount</th>
                                    <th>Stock</th>
                                    <th>Status</th>
                                    <th style="text-align: right;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty pagination.items}">
                                        <tr><td colspan="8" style="text-align: center; padding: 2rem; color: #94a3b8;">No products found matching criteria.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="p" items="${pagination.items}">
                                            <tr>
                                                <td><code style="background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; color: #475569; font-weight: 700;"><c:out value="${p.sku}" /></code></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/product?id=${p.productId}" target="_blank" style="color: #0f172a; font-weight: 700; text-decoration: none;">
                                                        <c:out value="${p.productName}" /> ↗
                                                    </a><br>
                                                    <small style="color: #64748b; font-weight: 600;"><c:out value="${p.brand}" /></small>
                                                </td>
                                                <td><span class="badge badge-info"><c:out value="${p.category.categoryName}" /></span></td>
                                                <td><strong>₹<fmt:formatNumber value="${p.price}" minFractionDigits="0" /></strong></td>
                                                <td>
                                                    <c:if test="${p.discountPercentage > 0}">
                                                        <span style="color: #16a34a; font-weight: 700;"><fmt:formatNumber value="${p.discountPercentage}" maxFractionDigits="0" />% OFF</span>
                                                    </c:if>
                                                    <c:if test="${empty p.discountPercentage || p.discountPercentage <= 0}">
                                                        <span style="color: #94a3b8;">—</span>
                                                    </c:if>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${p.stockQuantity <= 0}">
                                                            <span class="badge badge-danger">0 Out of Stock</span>
                                                        </c:when>
                                                        <c:when test="${p.lowStock}">
                                                            <span class="badge badge-warning">${p.stockQuantity} Low Stock</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-success">${p.stockQuantity} In Stock</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <span class="badge ${p.active ? 'badge-success' : 'badge-danger'}">
                                                        <c:out value="${p.status}" />
                                                    </span>
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${pageContext.request.contextPath}/admin/products/edit?id=${p.productId}" class="btn btn-secondary btn-sm">
                                                        ✏️ Edit
                                                    </a>
                                                    <form action="${pageContext.request.contextPath}/admin/products/status" method="POST" style="display: inline-block; margin-left: 0.25rem;">
                                                        <input type="hidden" name="_csrf" value="${csrfToken}">
                                                        <input type="hidden" name="id" value="${p.productId}">
                                                        <input type="hidden" name="status" value="${p.active ? 'INACTIVE' : 'ACTIVE'}">
                                                        <button type="submit" class="btn btn-secondary btn-sm" style="cursor: pointer;">
                                                            ${p.active ? 'Deactivate' : 'Activate'}
                                                        </button>
                                                    </form>
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
