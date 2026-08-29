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
    <title>${isEdit ? 'Edit Category' : 'Create New Category'} | ShopKart Console</title>
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
            <li><a href="${pageContext.request.contextPath}/admin/categories">← Back to Categories</a></li>
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
                    <li><a href="${pageContext.request.contextPath}/admin/categories" class="active"><span>📁</span> Categories</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/inventory"><span>🏭</span> Inventory &amp; Stock</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/orders"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/returns"><span>🔄</span> Return Requests</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reconciliation"><span>⚖️</span> Failed Payments &amp; Reconciliation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons &amp; Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                <div class="card" style="max-width: 720px; margin: 0 auto; padding: 2rem;">
                    <div style="border-bottom: 1px solid #e2e8f0; padding-bottom: 1rem; margin-bottom: 1.5rem;">
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">
                            ${isEdit ? 'Edit Category' : 'Create New Category'}
                        </h1>
                        <p style="color: #64748b; font-size: 0.875rem;">
                            Configure department classification, hierarchical parent mapping, and SEO-friendly slug.
                        </p>
                    </div>

                    <c:if test="${not empty error}">
                        <div style="background: #fee2e2; border-left: 4px solid #ef4444; color: #b91c1c; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; font-weight: 600;">
                            ⚠️ <c:out value="${error}" />
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}${isEdit ? '/admin/categories/edit' : '/admin/categories/add'}" method="POST">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <c:if test="${isEdit}">
                            <input type="hidden" name="categoryId" value="${category.categoryId}">
                        </c:if>

                        <div class="form-group">
                            <label for="parentCategoryId" class="form-label">Parent Category Classification</label>
                            <select id="parentCategoryId" name="parentCategoryId" class="form-select">
                                <option value="0">-- None (Top Level Root Department) --</option>
                                <c:forEach var="p" items="${parentOptions}">
                                    <c:if test="${empty category or category.categoryId != p.categoryId}">
                                        <option value="${p.categoryId}" <c:if test="${category.parentCategoryId == p.categoryId}">selected</c:if>>
                                            <c:out value="${p.categoryName}" />
                                        </option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="categoryName" class="form-label">Category Name *</label>
                            <input type="text" id="categoryName" name="categoryName" class="form-input" 
                                   value="<c:out value='${category.categoryName}' />" required placeholder="e.g. Smart Wearables & IoT">
                        </div>

                        <div class="form-group">
                            <label for="slug" class="form-label">URL Slug (Auto-generated if left empty)</label>
                            <input type="text" id="slug" name="slug" class="form-input" 
                                   value="<c:out value='${category.slug}' />" placeholder="e.g. smart-wearables-iot">
                        </div>

                        <div class="form-group">
                            <label for="description" class="form-label">Department Description</label>
                            <textarea id="description" name="description" class="form-input" style="min-height: 90px; resize: vertical;" placeholder="Brief category description and product scope..."><c:out value="${category.description}" /></textarea>
                        </div>

                        <div class="form-group">
                            <label for="active" class="form-label">Active Visibility</label>
                            <select id="active" name="active" class="form-select">
                                <option value="true" <c:if test="${empty category or category.active}">selected</c:if>>ACTIVE (Visible in Storefront Nav)</option>
                                <option value="false" <c:if test="${not empty category and not category.active}">selected</c:if>>INACTIVE (Hidden)</option>
                            </select>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary" style="padding: 0.65rem 1.75rem;">
                                ${isEdit ? '✓ Update Category' : '✓ Create Category'}
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/categories" class="btn btn-secondary">
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
