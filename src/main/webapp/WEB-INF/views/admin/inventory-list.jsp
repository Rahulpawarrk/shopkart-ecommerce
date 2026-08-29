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
    <title>Warehouse Inventory & Stock Control | ShopKart Console</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style nonce="${cspNonce}">
        .modal {
            display: none;
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(15, 23, 42, 0.6);
            backdrop-filter: blur(4px);
            align-items: center;
            justify-content: center;
            z-index: 2000;
        }
        .modal.open {
            display: flex;
        }
        .modal-content {
            background: #ffffff;
            padding: 2rem;
            border-radius: var(--radius-md);
            max-width: 480px;
            width: 90%;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            border: 1px solid #e2e8f0;
        }
    </style>
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
                    <li><a href="${pageContext.request.contextPath}/admin/inventory" class="active"><span>🏭</span> Inventory &amp; Stock</a></li>
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
                <div style="background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0; margin-bottom: 1.25rem;">
                    <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">Warehouse Inventory & Stock Control</h1>
                    <p style="color: #64748b; font-size: 0.875rem;">Real-time stock balances, PO batch replenishment, and ledger movement tracking.</p>
                </div>

                <!-- Warehouse Stat Cards -->
                <div class="grid grid-cols-4" style="margin-bottom: 1.25rem;">
                    <div class="stat-card" style="border-top: 4px solid #6366f1;">
                        <div class="stat-label">Total SKUs Tracked</div>
                        <div class="stat-value" style="color: #6366f1;">${stats.totalItems}</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #10b981;">
                        <div class="stat-label">Total Warehouse Units</div>
                        <div class="stat-value" style="color: #10b981;">${stats.totalUnits}</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #f59e0b;">
                        <div class="stat-label">Low Stock Alerts</div>
                        <div class="stat-value" style="color: #f59e0b;">${stats.lowStock}</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #ef4444;">
                        <div class="stat-label">Out of Stock</div>
                        <div class="stat-value" style="color: #ef4444;">${stats.outOfStock}</div>
                    </div>
                </div>

                <c:if test="${param.restocked == 'true'}">
                    <div style="background: #dcfce7; border-left: 4px solid #10b981; color: #15803d; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; font-weight: 600;">
                        ✓ Stock successfully restocked and logged to audit ledger!
                    </div>
                </c:if>

                <c:if test="${param.adjusted == 'true'}">
                    <div style="background: #dcfce7; border-left: 4px solid #10b981; color: #15803d; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; font-weight: 600;">
                        ✓ Inventory count adjusted and audit record created!
                    </div>
                </c:if>

                <!-- Filter Tabs -->
                <div class="tabs">
                    <a href="${pageContext.request.contextPath}/admin/inventory" class="tab-item ${empty filter || filter == 'all' ? 'active' : ''}">All Inventory</a>
                    <a href="${pageContext.request.contextPath}/admin/inventory?filter=low_stock" class="tab-item ${filter == 'low_stock' ? 'active' : ''}">Low Stock (${stats.lowStock})</a>
                    <a href="${pageContext.request.contextPath}/admin/inventory?filter=out_of_stock" class="tab-item ${filter == 'out_of_stock' ? 'active' : ''}">Out of Stock (${stats.outOfStock})</a>
                    <a href="${pageContext.request.contextPath}/admin/inventory?filter=in_stock" class="tab-item ${filter == 'in_stock' ? 'active' : ''}">Adequate Stock (${stats.inStock})</a>
                </div>

                <!-- Search -->
                <div class="card" style="padding: 1rem 1.25rem; margin-bottom: 1.25rem;">
                    <form action="${pageContext.request.contextPath}/admin/inventory" method="GET" style="display: flex; gap: 1rem;">
                        <input type="hidden" name="filter" value="${filter}">
                        <input type="text" name="q" placeholder="Search by SKU, Product Name, Brand..." value="<c:out value='${keyword}' />" 
                               class="form-input" style="flex: 1;">
                        <button type="submit" class="btn btn-primary">Search</button>
                        <a href="${pageContext.request.contextPath}/admin/inventory" class="btn btn-secondary">Reset</a>
                    </form>
                </div>

                <!-- Inventory Table -->
                <div class="card" style="padding: 0; overflow: hidden;">
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>SKU</th>
                                    <th>Product Name</th>
                                    <th>Category</th>
                                    <th>Current Quantity</th>
                                    <th>Alert Level</th>
                                    <th>Status</th>
                                    <th style="text-align: right;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${pagination.items}">
                                    <tr>
                                        <td><code style="background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; color: #475569; font-weight: 700;"><c:out value="${item.sku}" /></code></td>
                                        <td>
                                            <strong style="color: #0f172a;"><c:out value="${item.productName}" /></strong>
                                        </td>
                                        <td><span class="badge badge-info"><c:out value="${item.categoryName}" /></span></td>
                                        <td>
                                            <strong style="font-size: 1.05rem; color: #0f172a;">${item.quantity}</strong> <small style="color: #64748b;">units</small>
                                        </td>
                                        <td>
                                            <small style="color: #64748b; font-weight: 600;">Threshold: ${item.lowStockThreshold}</small>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.outOfStock}">
                                                    <span class="badge badge-danger">OUT OF STOCK</span>
                                                </c:when>
                                                <c:when test="${item.lowStock}">
                                                    <span class="badge badge-warning">LOW STOCK</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-success">HEALTHY</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right;">
                                            <button type="button"
                                                    class="btn btn-primary btn-sm js-restock-btn"
                                                    data-product-id="${item.productId}"
                                                    data-product-name="<c:out value="${item.productName}" />"
                                                    data-quantity="${item.quantity}">
                                                + Restock
                                            </button>
                                            <button type="button"
                                                    class="btn btn-secondary btn-sm js-adjust-btn"
                                                    style="margin-left: 0.25rem;"
                                                    data-product-id="${item.productId}"
                                                    data-product-name="<c:out value="${item.productName}" />"
                                                    data-quantity="${item.quantity}">
                                                Adjust
                                            </button>
                                            <a href="${pageContext.request.contextPath}/admin/inventory/transactions?productId=${item.productId}" 
                                               class="btn btn-secondary btn-sm" style="margin-left: 0.25rem;">
                                                Ledger ↗
                                            </a>
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

    <!-- Restock Modal Dialog -->
    <div id="restockModal" class="modal">
        <div class="modal-content">
            <h2 id="restockTitle" style="font-size: 1.25rem; font-weight: 800; color: #0f172a; margin-bottom: 1.25rem;">Restock Product Inventory</h2>
            <form action="${pageContext.request.contextPath}/admin/inventory/restock" method="POST">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" id="restockProductId" name="productId">
                
                <div class="form-group">
                    <label class="form-label">Units to Add *</label>
                    <input type="number" name="quantity" min="1" required class="form-input" placeholder="e.g. 50">
                </div>

                <div class="form-group">
                    <label class="form-label">Supplier PO Reference</label>
                    <input type="text" name="reference" class="form-input" placeholder="e.g. PO-2026-089">
                </div>

                <div class="form-group">
                    <label class="form-label">Audit Remarks</label>
                    <input type="text" name="remarks" class="form-input" placeholder="Supplier shipment received at warehouse">
                </div>

                <div style="display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 1.5rem;">
                    <button type="button" class="btn btn-secondary js-modal-cancel">Cancel</button>
                    <button type="submit" class="btn btn-primary">Confirm Restock</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Adjust Modal Dialog -->
    <div id="adjustModal" class="modal">
        <div class="modal-content">
            <h2 id="adjustTitle" style="font-size: 1.25rem; font-weight: 800; color: #0f172a; margin-bottom: 1.25rem;">Adjust Stock Count</h2>
            <form action="${pageContext.request.contextPath}/admin/inventory/adjust" method="POST">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" id="adjustProductId" name="productId">
                
                <div class="form-group">
                    <label class="form-label">New Exact Physical Count *</label>
                    <input type="number" id="adjustNewQty" name="newQuantity" min="0" required class="form-input">
                </div>

                <div class="form-group">
                    <label class="form-label">Mandatory Reason / Audit Note *</label>
                    <input type="text" name="remarks" required class="form-input" placeholder="e.g. Physical warehouse audit correction">
                </div>

                <div style="display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 1.5rem;">
                    <button type="button" class="btn btn-secondary js-modal-cancel">Cancel</button>
                    <button type="submit" class="btn btn-primary">Apply Count Correction</button>
                </div>
            </form>
        </div>
    </div>

    <script nonce="${cspNonce}">
        function openRestockModal(productId, productName, currentQty) {
            document.getElementById('restockProductId').value = productId;
            document.getElementById('restockTitle').innerText = 'Restock: ' + productName + ' (Current: ' + currentQty + ')';
            document.getElementById('restockModal').classList.add('open');
        }

        function openAdjustModal(productId, productName, currentQty) {
            document.getElementById('adjustProductId').value = productId;
            document.getElementById('adjustNewQty').value = currentQty;
            document.getElementById('adjustTitle').innerText = 'Adjust Count: ' + productName;
            document.getElementById('adjustModal').classList.add('open');
        }

        function closeModals() {
            document.getElementById('restockModal').classList.remove('open');
            document.getElementById('adjustModal').classList.remove('open');
        }

        // CSP-compliant event delegation — replaces inline onclick="" handlers,
        // which are blocked by CSP even when the <script> tag itself carries a nonce.
        document.addEventListener('click', function (event) {
            const restockBtn = event.target.closest('.js-restock-btn');
            if (restockBtn) {
                openRestockModal(
                    restockBtn.getAttribute('data-product-id'),
                    restockBtn.getAttribute('data-product-name'),
                    restockBtn.getAttribute('data-quantity')
                );
                return;
            }

            const adjustBtn = event.target.closest('.js-adjust-btn');
            if (adjustBtn) {
                openAdjustModal(
                    adjustBtn.getAttribute('data-product-id'),
                    adjustBtn.getAttribute('data-product-name'),
                    adjustBtn.getAttribute('data-quantity')
                );
                return;
            }

            if (event.target.closest('.js-modal-cancel')) {
                closeModals();
                return;
            }

            // Click on the dark overlay (outside modal-content) also closes it
            if (event.target.classList.contains('modal') && event.target.classList.contains('open')) {
                closeModals();
            }
        });
    </script>

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
