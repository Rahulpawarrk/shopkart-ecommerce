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
    <title>Returns & Replacements Moderation | ShopKart Console</title>
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
                    <li><a href="${pageContext.request.contextPath}/admin/returns" class="active"><span>🔄</span> Return Requests</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
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
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">Order Returns &amp; Replacements</h1>
                        <p style="color: #64748b; font-size: 0.875rem; margin: 0;">Review customer return requests, inspect defective item photos, schedule pickups, and issue refunds.</p>
                    </div>
                </div>

                <c:if test="${param.updated == 'true'}">
                    <div style="background: #dcfce7; border-left: 4px solid #16a34a; padding: 0.9rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; color: #14532d; font-weight: 700;">
                        ✓ Return request status and resolution updated successfully!
                    </div>
                </c:if>

                <!-- Filter & Search Toolbar -->
                <div class="card" style="padding: 1rem 1.25rem; margin-bottom: 1.25rem;">
                    <form action="${pageContext.request.contextPath}/admin/returns" method="GET" style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: center;">
                        <div style="flex: 1; min-width: 200px;">
                            <input type="text" name="q" value="<c:out value="${keyword}" />" placeholder="Search by Return #, Order #, or Customer Email..." class="form-control" style="padding: 0.5rem 0.85rem; border-radius: 8px; border: 1px solid #cbd5e1; width: 100%; box-sizing: border-box;">
                        </div>
                        <div>
                            <select name="status" class="form-control" style="padding: 0.5rem 0.85rem; border-radius: 8px; border: 1px solid #cbd5e1; font-weight: 600;">
                                <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                                <option value="REQUESTED" ${selectedStatus == 'REQUESTED' ? 'selected' : ''}>⏳ Requested</option>
                                <option value="APPROVED" ${selectedStatus == 'APPROVED' ? 'selected' : ''}>✓ Approved</option>
                                <option value="PICKUP_SCHEDULED" ${selectedStatus == 'PICKUP_SCHEDULED' ? 'selected' : ''}>🚚 Pickup Scheduled</option>
                                <option value="ITEM_RECEIVED" ${selectedStatus == 'ITEM_RECEIVED' ? 'selected' : ''}>📦 Item Received</option>
                                <option value="REFUNDED" ${selectedStatus == 'REFUNDED' ? 'selected' : ''}>💳 Refunded</option>
                                <option value="REPLACED" ${selectedStatus == 'REPLACED' ? 'selected' : ''}>🔄 Replaced</option>
                                <option value="REJECTED" ${selectedStatus == 'REJECTED' ? 'selected' : ''}>✕ Rejected</option>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-primary" style="padding: 0.5rem 1.25rem; border-radius: 8px; font-weight: 700;">Filter</button>
                        <c:if test="${not empty keyword || (not empty selectedStatus && selectedStatus != 'ALL')}">
                            <a href="${pageContext.request.contextPath}/admin/returns" style="color: #64748b; font-size: 0.85rem; text-decoration: none; font-weight: 700;">Clear</a>
                        </c:if>
                    </form>
                </div>

                <!-- Returns List Table -->
                <div class="card" style="padding: 0; overflow: hidden;">
                    <table class="table" style="margin: 0; width: 100%; border-collapse: collapse;">
                        <thead>
                            <tr style="background: #f8fafc; border-bottom: 2px solid #e2e8f0;">
                                <th style="padding: 0.85rem 1rem; text-align: left; font-size: 0.78rem; text-transform: uppercase; color: #475569;">Return # / Date</th>
                                <th style="padding: 0.85rem 1rem; text-align: left; font-size: 0.78rem; text-transform: uppercase; color: #475569;">Order # / Customer</th>
                                <th style="padding: 0.85rem 1rem; text-align: left; font-size: 0.78rem; text-transform: uppercase; color: #475569;">Reason &amp; Resolution</th>
                                <th style="padding: 0.85rem 1rem; text-align: left; font-size: 0.78rem; text-transform: uppercase; color: #475569;">Status</th>
                                <th style="padding: 0.85rem 1rem; text-align: left; font-size: 0.78rem; text-transform: uppercase; color: #475569;">Customer Proof</th>
                                <th style="padding: 0.85rem 1rem; text-align: right; font-size: 0.78rem; text-transform: uppercase; color: #475569;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty returns.items}">
                                    <tr>
                                        <td colspan="6" style="padding: 3rem; text-align: center; color: #64748b;">
                                            <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🔄</div>
                                            <p style="font-weight: 700; margin: 0;">No return requests found.</p>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="ret" items="${returns.items}">
                                        <tr style="border-bottom: 1px solid #f1f5f9;">
                                            <td style="padding: 1rem;">
                                                <strong style="color: #0f172a; font-family: monospace; font-size: 0.9rem;"><c:out value="${ret.returnNumber}" /></strong>
                                                <div style="font-size: 0.75rem; color: #64748b; margin-top: 0.2rem;"><c:out value="${ret.formattedCreatedAt}" /></div>
                                            </td>
                                            <td style="padding: 1rem;">
                                                <a href="${pageContext.request.contextPath}/admin/orders?search=${ret.orderNumber}" style="color: #2563eb; font-weight: 800; text-decoration: none;">
                                                    Order #<c:out value="${ret.orderNumber}" />
                                                </a>
                                                <div style="font-size: 0.82rem; color: #334155; margin-top: 0.2rem;"><c:out value="${ret.customerName}" /></div>
                                                <div style="font-size: 0.75rem; color: #64748b;"><c:out value="${ret.customerEmail}" /></div>
                                            </td>
                                            <td style="padding: 1rem;">
                                                <div style="font-weight: 700; color: #0f172a; font-size: 0.88rem;"><c:out value="${ret.returnReason}" /></div>
                                                <div style="font-size: 0.8rem; margin-top: 0.25rem;">
                                                    <span style="font-weight: 700; color: #2563eb;">Resolution:</span>
                                                    <c:choose>
                                                        <c:when test="${ret.resolutionType == 'REFUND'}"><span class="badge" style="background: #eff6ff; color: #1d4ed8;">💳 Refund</span></c:when>
                                                        <c:when test="${ret.resolutionType == 'REPLACEMENT'}"><span class="badge" style="background: #f0fdf4; color: #166534;">🔄 Replacement</span></c:when>
                                                        <c:otherwise><c:out value="${ret.resolutionType}" /></c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <c:if test="${not empty ret.comments}">
                                                    <div style="font-size: 0.75rem; color: #64748b; margin-top: 0.35rem; font-style: italic; max-width: 250px;">
                                                        "<c:out value="${ret.comments}" />"
                                                    </div>
                                                </c:if>
                                            </td>
                                            <td style="padding: 1rem;">
                                                <span class="pill-badge ${ret.statusBadgeClass}" style="font-size: 0.78rem; font-weight: 800;">
                                                    <c:out value="${ret.returnStatus}" />
                                                </span>
                                                <c:if test="${not empty ret.adminNotes}">
                                                    <div style="font-size: 0.72rem; color: #475569; margin-top: 0.25rem;">Note: <c:out value="${ret.adminNotes}" /></div>
                                                </c:if>
                                            </td>
                                            <td style="padding: 1rem;">
                                                <c:choose>
                                                    <c:when test="${not empty ret.imageUrl}">
                                                        <a href="${ret.imageUrl}" target="_blank">
                                                            <img src="${ret.imageUrl}" alt="Proof" style="width: 48px; height: 48px; object-fit: cover; border-radius: 6px; border: 1px solid #cbd5e1; cursor: pointer;">
                                                        </a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="color: #94a3b8; font-size: 0.8rem;">No photo</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="padding: 1rem; text-align: right;">
                                                <button type="button" 
                                                        onclick="openAdminReturnModal(${ret.returnId}, '${ret.returnNumber}', '${ret.returnStatus}', '${ret.resolutionType}', '<c:out value="${ret.adminNotes}" />')"
                                                        class="btn btn-sm btn-outline-primary" 
                                                        style="font-size: 0.8rem; padding: 0.35rem 0.75rem; font-weight: 700; border-radius: 6px; cursor: pointer;">
                                                    ⚙️ Update Status
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </section>
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

    <!-- Admin Return Moderation Modal -->
    <div id="adminReturnModal" style="display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.6); align-items: center; justify-content: center; z-index: 9999; padding: 1.5rem;">
        <div style="background: #ffffff; border-radius: 14px; max-width: 480px; width: 100%; box-shadow: 0 20px 40px rgba(0,0,0,0.25); border: 1px solid #e2e8f0; overflow: hidden;">
            <div style="padding: 1.25rem 1.5rem; background: #f8fafc; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center;">
                <h3 style="font-size: 1.15rem; font-weight: 800; color: #0f172a; margin: 0;" id="adminModalTitle">Update Return Status</h3>
                <button type="button" onclick="closeAdminReturnModal()" style="background: none; border: none; font-size: 1.2rem; cursor: pointer; color: #64748b;">✕</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/returns" method="POST" style="padding: 1.5rem;">
                <input type="hidden" name="returnId" id="adminModalReturnId" value="">
                
                <div style="margin-bottom: 1.25rem;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 700; color: #1e293b; margin-bottom: 0.4rem;">Change Return Status</label>
                    <select name="returnStatus" id="adminModalStatusSelect" required style="width: 100%; padding: 0.65rem 0.85rem; border-radius: 8px; border: 1.5px solid #cbd5e1; font-weight: 600; outline: none;">
                        <option value="REQUESTED">⏳ REQUESTED (Under Review)</option>
                        <option value="APPROVED">✓ APPROVED (Return Approved)</option>
                        <option value="PICKUP_SCHEDULED">🚚 PICKUP_SCHEDULED (Courier Assigned)</option>
                        <option value="ITEM_RECEIVED">📦 ITEM_RECEIVED (Warehouse Inspected)</option>
                        <option value="REFUNDED">💳 REFUNDED (Refund Processed)</option>
                        <option value="REPLACED">🔄 REPLACED (Replacement Shipped)</option>
                        <option value="REJECTED">✕ REJECTED (Return Declined)</option>
                    </select>
                </div>

                <div style="margin-bottom: 1.25rem;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 700; color: #1e293b; margin-bottom: 0.4rem;">Admin / Customer Note</label>
                    <textarea name="adminNotes" id="adminModalNotes" rows="3" placeholder="e.g. Pickup scheduled for tomorrow with BlueDart AWB #..." style="width: 100%; padding: 0.65rem 0.85rem; border-radius: 8px; border: 1.5px solid #cbd5e1; font-size: 0.88rem; box-sizing: border-box;"></textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 0.75rem; border-top: 1px solid #e2e8f0; padding-top: 1.25rem;">
                    <button type="button" onclick="closeAdminReturnModal()" class="btn btn-secondary" style="padding: 0.5rem 1rem; border-radius: 6px; font-weight: 700;">Cancel</button>
                    <button type="submit" class="btn btn-primary" style="padding: 0.5rem 1.25rem; border-radius: 6px; font-weight: 800;">Save Status</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openAdminReturnModal(returnId, returnNumber, status, resolution, notes) {
            document.getElementById('adminModalReturnId').value = returnId;
            document.getElementById('adminModalTitle').innerText = 'Update Return #' + returnNumber;
            document.getElementById('adminModalStatusSelect').value = status || 'REQUESTED';
            document.getElementById('adminModalNotes').value = notes || '';
            const modal = document.getElementById('adminReturnModal');
            modal.style.display = 'flex';
        }

        function closeAdminReturnModal() {
            document.getElementById('adminReturnModal').style.display = 'none';
        }
    </script>
</body>
</html>
