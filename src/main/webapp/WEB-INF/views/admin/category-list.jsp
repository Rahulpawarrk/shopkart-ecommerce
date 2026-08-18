<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Category Taxonomy Management | ShopKart Console</title>
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
                    <li><a href="${pageContext.request.contextPath}/admin/categories" class="active"><span>📁</span> Categories</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/inventory"><span>🏭</span> Inventory & Stock</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/orders"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons & Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0;">
                    <div>
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">Category Taxonomy Management</h1>
                        <p style="color: #64748b; font-size: 0.875rem;">Structure department trees, parent categories, and navigation URL slugs.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/categories/add" class="btn btn-primary">
                        + Add New Category
                    </a>
                </div>

                <c:if test="${param.saved == 'true'}">
                    <div style="background: #dcfce7; border-left: 4px solid #10b981; color: #15803d; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; font-weight: 600;">
                        ✓ Category structure saved and synchronized successfully!
                    </div>
                </c:if>

                <div class="card" style="padding: 0; overflow: hidden;">
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Category Name</th>
                                    <th>Parent Classification</th>
                                    <th>URL Slug</th>
                                    <th>Status</th>
                                    <th style="text-align: right;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${categories}">
                                    <tr>
                                        <td><strong>#${c.categoryId}</strong></td>
                                        <td>
                                            <strong style="color: #0f172a;"><c:out value="${c.categoryName}" /></strong>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty c.parentCategoryName}">
                                                    <span class="badge badge-info"><c:out value="${c.parentCategoryName}" /></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #94a3b8; font-size: 0.85rem; font-weight: 600;">[Top-Level Department]</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><code style="background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; color: #475569; font-weight: 700;"><c:out value="${c.slug}" /></code></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${c.active}">
                                                    <span class="badge badge-success">ACTIVE</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-danger">INACTIVE</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right;">
                                            <a href="${pageContext.request.contextPath}/admin/categories/edit?id=${c.categoryId}" class="btn btn-secondary btn-sm">
                                                ✏️ Edit
                                            </a>
                                            <form action="${pageContext.request.contextPath}/admin/categories/status" method="POST" style="display: inline-block; margin-left: 0.25rem;">
                                                <input type="hidden" name="id" value="${c.categoryId}">
                                                <input type="hidden" name="active" value="${!c.active}">
                                                <button type="submit" class="btn btn-secondary btn-sm" style="cursor: pointer;">
                                                    ${c.active ? 'Deactivate' : 'Activate'}
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
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
