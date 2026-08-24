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
    <title>${isEdit ? 'Edit Product' : 'Add New Product'} | ShopKart Console</title>
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
                    <li><a href="${pageContext.request.contextPath}/admin/products" class="active"><span>📦</span> Product Catalog</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/categories"><span>📁</span> Categories</a></li>
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
                <div class="card" style="max-width: 860px; margin: 0 auto; padding: 2rem;">
                    <div style="border-bottom: 1px solid #e2e8f0; padding-bottom: 1rem; margin-bottom: 1.5rem;">
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">
                            ${isEdit ? 'Edit Product Details' : 'Onboard New Product'}
                        </h1>
                        <p style="color: #64748b; font-size: 0.875rem;">
                            Define pricing, department categorization, inventory buffer levels, and listing status.
                        </p>
                    </div>

                    <c:if test="${not empty error}">
                        <div style="background: #fee2e2; border-left: 4px solid #ef4444; color: #b91c1c; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem; font-weight: 600;">
                            ⚠️ <c:out value="${error}" />
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}${isEdit ? '/admin/products/edit' : '/admin/products/add'}" method="POST">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <c:if test="${isEdit}">
                            <input type="hidden" name="productId" value="${product.productId}">
                        </c:if>

                        <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                            <div class="form-group" style="margin-bottom: 0;">
                                <label for="productName" class="form-label">Product Title *</label>
                                <input type="text" id="productName" name="productName" class="form-input" 
                                       minlength="3" maxlength="255"
                                       title="Product title must be between 3 and 255 characters"
                                       value="<c:out value='${product.productName}' />" required placeholder="e.g. Dell XPS 15 OLED Laptop">
                            </div>
                            <div class="form-group" style="margin-bottom: 0;">
                                <label for="sku" class="form-label">SKU Code * (Unique)</label>
                                <input type="text" id="sku" name="sku" class="form-input" 
                                       pattern="[A-Za-z0-9-_]{3,50}" maxlength="50" minlength="3"
                                       title="SKU must be 3-50 alphanumeric characters or hyphens"
                                       value="<c:out value='${product.sku}' />" required placeholder="e.g. DELL-XPS-15-01">
                            </div>
                        </div>

                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                            <div class="form-group" style="margin-bottom: 0;">
                                <label for="categoryId" class="form-label">Category Department *</label>
                                <select id="categoryId" name="categoryId" class="form-select" required>
                                    <option value="">-- Select Category --</option>
                                    <c:forEach var="c" items="${categories}">
                                        <option value="${c.categoryId}" <c:if test="${product.categoryId == c.categoryId}">selected</c:if>>
                                            <c:out value="${c.categoryName}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="form-group" style="margin-bottom: 0;">
                                <label for="brand" class="form-label">Brand Name</label>
                                <input type="text" id="brand" name="brand" class="form-input" 
                                       value="<c:out value='${product.brand}' />" placeholder="e.g. Dell">
                            </div>
                        </div>

                        <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                            <div class="form-group" style="margin-bottom: 0;">
                                <label for="price" class="form-label">Base Retail Price (₹) *</label>
                                <input type="number" step="0.01" id="price" name="price" class="form-input" 
                                       value="${product.price}" required placeholder="49999.00">
                            </div>
                            <div class="form-group" style="margin-bottom: 0;">
                                <label for="discountPercentage" class="form-label">Discount (%)</label>
                                <input type="number" step="0.01" min="0" max="100" id="discountPercentage" name="discountPercentage" class="form-input" 
                                       value="${product.discountPercentage}" placeholder="10.00">
                            </div>
                            <div class="form-group" style="margin-bottom: 0;">
                                <label for="taxPercentage" class="form-label">Tax / GST (%)</label>
                                <input type="number" step="0.01" min="0" id="taxPercentage" name="taxPercentage" class="form-input" 
                                       value="${product.taxPercentage}" placeholder="18.00">
                            </div>
                        </div>

                        <c:if test="${not isEdit}">
                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; background: #f8fafc; padding: 1.25rem; border-radius: var(--radius-sm); border: 1px solid #e2e8f0; margin-bottom: 1.25rem;">
                                <div class="form-group" style="margin-bottom: 0;">
                                    <label for="initialStock" class="form-label">Initial Opening Stock Units *</label>
                                    <input type="number" min="0" id="initialStock" name="initialStock" class="form-input" value="50" required>
                                </div>
                                <div class="form-group" style="margin-bottom: 0;">
                                    <label for="lowStockThreshold" class="form-label">Low Stock Alert Level *</label>
                                    <input type="number" min="1" id="lowStockThreshold" name="lowStockThreshold" class="form-input" value="5" required>
                                </div>
                            </div>
                        </c:if>

                        <div class="form-group">
                            <label for="primaryImageUrl" class="form-label">Primary Image URL</label>
                            <input type="text" id="primaryImageUrl" name="primaryImageUrl" class="form-input" 
                                   value="<c:out value='${product.primaryImageUrl}' />" placeholder="https://placehold.co/600x600?text=Product">
                        </div>

                        <div class="form-group">
                            <label for="description" class="form-label">Product Detailed Description</label>
                            <textarea id="description" name="description" class="form-input" style="min-height: 110px; resize: vertical;" placeholder="Comprehensive product specifications, features, and warranty details..."><c:out value="${product.description}" /></textarea>
                        </div>

                        <div class="form-group">
                            <label for="status" class="form-label">Listing Status</label>
                            <select id="status" name="status" class="form-select">
                                <option value="ACTIVE" <c:if test="${empty product or product.status == 'ACTIVE'}">selected</c:if>>ACTIVE (Purchasable in Storefront)</option>
                                <option value="INACTIVE" <c:if test="${product.status == 'INACTIVE'}">selected</c:if>>INACTIVE (Hidden from Customers)</option>
                                <option value="ARCHIVED" <c:if test="${product.status == 'ARCHIVED'}">selected</c:if>>ARCHIVED</option>
                            </select>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary" style="padding: 0.65rem 1.75rem;">
                                ${isEdit ? '✓ Update Product' : '✓ Save & Onboard Product'}
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-secondary">
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
