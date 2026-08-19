<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Order #${order.orderNumber} | ShopKart Console</title>
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
            <li><a href="${pageContext.request.contextPath}/admin/orders">← Back to Orders</a></li>
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
                    <li><a href="${pageContext.request.contextPath}/admin/orders" class="active"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reconciliation"><span>⚖️</span> Failed Payments &amp; Reconciliation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons & Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                <c:if test="${not empty param.error}">
                    <div style="background: #fee2e2; border-left: 4px solid #ef4444; color: #991b1b; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; font-weight: 600;">
                        ⚠️ <c:out value="${param.error}" />
                    </div>
                </c:if>

                <c:if test="${param.updated == 'true'}">
                    <div style="background: #dcfce7; border-left: 4px solid #10b981; color: #15803d; padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; font-weight: 600;">
                        ✓ Order fulfillment status updated successfully and recorded to audit trail!
                    </div>
                </c:if>

                <div class="card" style="padding: 1.75rem; margin-bottom: 1.5rem;">
                    
                    <!-- Header -->
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; padding-bottom: 1.25rem; margin-bottom: 1.5rem;">
                        <div>
                            <div style="font-size: 0.75rem; color: #64748b; font-weight: 700; text-transform: uppercase;">ORDER MANAGEMENT</div>
                            <h1 style="font-size: 1.4rem; font-weight: 800; color: #0f172a; margin: 0.2rem 0;">
                                Order #<c:out value="${order.orderNumber}" />
                            </h1>
                            <span style="font-size: 0.85rem; color: #64748b;">
                                Customer: <strong style="color: #1e293b;"><c:out value="${order.customerName}" /></strong> (<c:out value="${order.customerEmail}" />) &bull; Placed: <strong><c:choose><c:when test="${not empty order.formattedCreatedAt}">${order.formattedCreatedAt}</c:when><c:otherwise>${order.createdAt}</c:otherwise></c:choose></strong>
                            </span>
                        </div>
                        <div>
                            <span class="badge ${order.orderStatus == 'DELIVERED' ? 'badge-success' : (order.orderStatus == 'CANCELLED' ? 'badge-danger' : 'badge-primary')}" style="font-size: 0.95rem; padding: 0.4rem 0.85rem;">
                                <c:out value="${order.orderStatus}" />
                            </span>
                        </div>
                    </div>

                    <!-- Destination & Payment Snapshot -->
                    <div class="grid grid-cols-3" style="margin-bottom: 1.5rem; gap: 1.25rem;">
                        <div style="background: #f8fafc; padding: 1.25rem; border-radius: var(--radius-sm); border: 1px solid #e2e8f0;">
                            <strong style="color: #0f172a; display: block; margin-bottom: 0.5rem; font-size: 0.9rem;">📍 Delivery Destination:</strong>
                            <div style="color: #475569; font-size: 0.875rem; line-height: 1.5;">
                                <c:out value="${order.formattedShippingAddress}" />
                            </div>
                        </div>

                        <div style="background: #f8fafc; padding: 1.25rem; border-radius: var(--radius-sm); border: 1px solid #e2e8f0;">
                            <strong style="color: #0f172a; display: block; margin-bottom: 0.5rem; font-size: 0.9rem;">💳 Payment Method:</strong>
                            <div style="color: #475569; font-size: 0.875rem; line-height: 1.5;">
                                Method: <strong><c:out value="${order.paymentMethod}" /></strong><br>
                                <c:choose>
                                    <c:when test="${order.paymentMethod == 'COD'}">
                                        <c:if test="${order.paymentStatus == 'PAID' || order.orderStatus == 'DELIVERED'}">
                                            Payment Status: <span class="badge badge-success" style="font-size: 0.7rem;">PAID</span><br>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        Payment Status: <span class="badge ${order.paymentStatus == 'PAID' ? 'badge-success' : 'badge-warning'}" style="font-size: 0.7rem;"><c:out value="${order.paymentStatus}" /></span><br>
                                    </c:otherwise>
                                </c:choose>
                                Total Order Amount: <strong style="color: #0f172a; font-size: 1rem;">₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="0" /></strong>
                            </div>
                        </div>

                        <div style="background: #f8fafc; padding: 1.25rem; border-radius: var(--radius-sm); border: 1px solid #e2e8f0;">
                            <strong style="color: #0f172a; display: block; margin-bottom: 0.5rem; font-size: 0.9rem;">🚚 Logistics & Tracking:</strong>
                            <div style="color: #475569; font-size: 0.875rem; line-height: 1.5;">
                                Courier: <strong><c:out value="${not empty order.courierPartner ? order.courierPartner : 'Not Assigned'}" /></strong><br>
                                AWB/Tracking: <c:choose><c:when test="${not empty order.trackingNumber}"><code style="background:#e2e8f0; padding:0.15rem 0.35rem; border-radius:4px; font-weight:700;"><c:out value="${order.trackingNumber}" /></code></c:when><c:otherwise><span style="color:#94a3b8;">Pending Dispatch</span></c:otherwise></c:choose><br>
                                <c:if test="${not empty order.deliveryAgentPhone}">
                                    Agent Phone: <strong><c:out value="${order.deliveryAgentPhone}" /></strong><br>
                                </c:if>
                                <c:if test="${not empty order.deliveredAt}">
                                    Delivered At: <strong style="color: #10b981;"><c:choose><c:when test="${not empty order.formattedDeliveredAt}">${order.formattedDeliveredAt}</c:when><c:otherwise>${order.deliveredAt}</c:otherwise></c:choose></strong>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <!-- Items Table -->
                    <h2 style="font-size: 1.1rem; font-weight: 800; margin-bottom: 0.75rem; color: #0f172a;">Ordered Products & Quantities</h2>
                    <div class="table-responsive" style="margin-bottom: 1.5rem;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>SKU</th>
                                    <th>Product Name</th>
                                    <th>Unit Price</th>
                                    <th>Quantity</th>
                                    <th style="text-align: right;">Price</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${order.items}">
                                    <tr>
                                        <td><code style="background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; color: #475569; font-weight: 700;"><c:out value="${item.sku}" /></code></td>
                                        <td><strong><c:out value="${item.productName}" /></strong></td>
                                        <td>₹<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="0" /></td>
                                        <td><strong>${item.quantity}</strong></td>
                                        <td style="text-align: right;"><strong>₹<fmt:formatNumber value="${item.lineTotal}" minFractionDigits="0" /></strong></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- Enhanced Status & Logistics Update Form with Strict Sequential Fulfillment -->
                    <div style="background: #eef2ff; border: 1px solid #c7d2fe; border-radius: var(--radius-md); padding: 1.5rem; margin-bottom: 1.5rem;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; flex-wrap: wrap; gap: 0.5rem;">
                            <h2 style="font-size: 1.05rem; font-weight: 800; color: #3730a3; margin: 0;">Update Order Fulfillment & Logistics</h2>
                            <span style="font-size: 0.78rem; font-weight: 700; color: #4338ca; background: #e0e7ff; padding: 0.25rem 0.65rem; border-radius: 9999px;">
                                Sequential Lifecycle Enforced
                            </span>
                        </div>
                        <p style="font-size: 0.8rem; color: #4f46e5; margin-bottom: 1rem;">
                            Follow the required step-by-step order processing path:
                            <strong>Processing (1)</strong> &rarr; 
                            <strong>Dispatched (2)</strong> &rarr; 
                            <strong>In Transit (3)</strong> &rarr; 
                            <strong>Out for Delivery (4)</strong> &rarr; 
                            <strong>Delivered (5)</strong>.
                        </p>
                        
                        <c:choose>
                            <c:when test="${order.orderStatus == 'CANCELLED' || order.orderStatus == 'RETURNED'}">
                                <div style="background: #ffffff; border: 1px dashed #cbd5e1; padding: 1.25rem; border-radius: 8px; text-align: center; color: #64748b; font-weight: 700;">
                                    🔒 Order #${order.orderNumber} is in terminal status <strong>[${order.orderStatus}]</strong>. Inventory and payment lifecycle finalized; no further status transitions permitted.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/admin/orders/status" method="POST">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="orderId" value="${order.orderId}">
                                    <c:set var="curr" value="${order.orderStatus}" />
                                    
                                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; margin-bottom: 1rem;">
                                        <div>
                                            <label class="form-label" style="font-size: 0.82rem; color: #312e81; font-weight: 700;">Fulfillment Status *</label>
                                            <select name="newStatus" class="form-select" style="background: #ffffff; font-weight: 600;" id="statusSelect">
                                                <!-- PROCESSING (Stage 1) -->
                                                <option value="PROCESSING" 
                                                    <c:if test="${curr == 'PROCESSING'}">selected</c:if>
                                                    <c:if test="${curr != 'CONFIRMED' && curr != 'PENDING' && curr != 'PROCESSING'}">disabled</c:if>>
                                                    📦 1. PROCESSING (Dispatch in Progress) <c:if test="${curr != 'CONFIRMED' && curr != 'PENDING' && curr != 'PROCESSING'}">[Already Passed]</c:if>
                                                </option>

                                                <!-- DISPATCHED (Stage 2) -->
                                                <option value="DISPATCHED" 
                                                    <c:if test="${curr == 'DISPATCHED'}">selected</c:if>
                                                    <c:if test="${curr != 'PROCESSING' && curr != 'DISPATCHED'}">disabled</c:if>>
                                                    🚚 2. DISPATCHED (Handed to Courier) <c:if test="${curr == 'CONFIRMED' || curr == 'PENDING'}">[Requires Processing First]</c:if><c:if test="${curr == 'IN_TRANSIT' || curr == 'SHIPPED' || curr == 'OUT_FOR_DELIVERY' || curr == 'DELIVERED'}">[Already Dispatched]</c:if>
                                                </option>

                                                <!-- IN_TRANSIT (Stage 3) -->
                                                <option value="IN_TRANSIT" 
                                                    <c:if test="${curr == 'IN_TRANSIT' || curr == 'SHIPPED'}">selected</c:if>
                                                    <c:if test="${curr != 'DISPATCHED' && curr != 'IN_TRANSIT' && curr != 'SHIPPED'}">disabled</c:if>>
                                                    ✈️ 3. IN_TRANSIT (Hub Movement) <c:if test="${curr == 'CONFIRMED' || curr == 'PENDING' || curr == 'PROCESSING'}">[Requires Dispatch First]</c:if><c:if test="${curr == 'OUT_FOR_DELIVERY' || curr == 'DELIVERED'}">[Already In Transit]</c:if>
                                                </option>

                                                <!-- OUT_FOR_DELIVERY (Stage 4) -->
                                                <option value="OUT_FOR_DELIVERY" 
                                                    <c:if test="${curr == 'OUT_FOR_DELIVERY'}">selected</c:if>
                                                    <c:if test="${curr != 'IN_TRANSIT' && curr != 'SHIPPED' && curr != 'OUT_FOR_DELIVERY'}">disabled</c:if>>
                                                    🛵 4. OUT_FOR_DELIVERY (Doorstep Agent) <c:if test="${curr == 'CONFIRMED' || curr == 'PENDING' || curr == 'PROCESSING' || curr == 'DISPATCHED'}">[Requires In Transit First]</c:if><c:if test="${curr == 'DELIVERED'}">[Already Delivered]</c:if>
                                                </option>

                                                <!-- DELIVERED (Stage 5) -->
                                                <option value="DELIVERED" 
                                                    <c:if test="${curr == 'DELIVERED'}">selected</c:if>
                                                    <c:if test="${curr != 'OUT_FOR_DELIVERY' && curr != 'DELIVERED'}">disabled</c:if>>
                                                    ✅ 5. DELIVERED (Fulfilled & Paid) <c:if test="${curr != 'OUT_FOR_DELIVERY' && curr != 'DELIVERED'}">[Requires Out For Delivery First]</c:if>
                                                </option>

                                                <!-- CANCELLED -->
                                                <option value="CANCELLED" 
                                                    <c:if test="${curr == 'CANCELLED'}">selected</c:if>
                                                    <c:if test="${curr == 'DELIVERED'}">disabled</c:if>>
                                                    ✕ CANCELLED (Restores Product Stock) <c:if test="${curr == 'DELIVERED'}">[Cannot Cancel Delivered Order]</c:if>
                                                </option>

                                                <!-- RETURN_REQUESTED -->
                                                <option value="RETURN_REQUESTED" 
                                                    <c:if test="${curr == 'RETURN_REQUESTED'}">selected</c:if>
                                                    <c:if test="${curr != 'DELIVERED' && curr != 'RETURN_REQUESTED'}">disabled</c:if>>
                                                    ↩️ RETURN REQUESTED <c:if test="${curr != 'DELIVERED' && curr != 'RETURN_REQUESTED'}">[Only for Delivered Orders]</c:if>
                                                </option>

                                                <!-- RETURNED -->
                                                <option value="RETURNED" 
                                                    <c:if test="${curr == 'RETURNED'}">selected</c:if>
                                                    <c:if test="${curr != 'RETURN_REQUESTED' && curr != 'DELIVERED' && curr != 'RETURNED'}">disabled</c:if>>
                                                    🔄 RETURNED (Restores Product Stock) <c:if test="${curr != 'RETURN_REQUESTED' && curr != 'DELIVERED' && curr != 'RETURNED'}">[Only for Delivered/Return Orders]</c:if>
                                                </option>
                                            </select>
                                        </div>

                                        <div>
                                            <label class="form-label" style="font-size: 0.82rem; color: #312e81; font-weight: 700;">Courier Partner</label>
                                            <input type="text" name="courierPartner" value="<c:out value='${order.courierPartner}' />" placeholder="e.g. BlueDart Express, Delhivery, DTDC" 
                                                   list="courierList" class="form-input" style="background: #ffffff;">
                                            <datalist id="courierList">
                                                <option value="BlueDart Express">
                                                <option value="Delhivery Logistics">
                                                <option value="DTDC Express">
                                                <option value="Shiprocket">
                                                <option value="Ecom Express">
                                                <option value="Shadowfax">
                                                <option value="FedEx India">
                                                <option value="India Post Speed Post">
                                                <option value="ShopKart Prime Express">
                                            </datalist>
                                        </div>

                                        <div>
                                            <label class="form-label" style="font-size: 0.82rem; color: #312e81; font-weight: 700;">AWB / Tracking Number</label>
                                            <input type="text" name="trackingNumber" value="<c:out value='${order.trackingNumber}' />" placeholder="e.g. BD-89240182" 
                                                   class="form-input" style="background: #ffffff;">
                                        </div>

                                        <div>
                                            <label class="form-label" style="font-size: 0.82rem; color: #312e81; font-weight: 700;">Delivery Agent Phone</label>
                                            <input type="text" name="deliveryAgentPhone" value="<c:out value='${order.deliveryAgentPhone}' />" placeholder="e.g. +91-9876543210" 
                                                   class="form-input" style="background: #ffffff;">
                                        </div>
                                    </div>

                                    <div style="display: flex; gap: 1rem; align-items: flex-end;">
                                        <div style="flex: 1;">
                                            <label class="form-label" style="font-size: 0.82rem; color: #312e81; font-weight: 700;">Milestone Remarks / Audit Note *</label>
                                            <input type="text" name="remarks" placeholder="e.g. Handed over package to BlueDart courier hub" required 
                                                   class="form-input" style="background: #ffffff;">
                                        </div>

                                        <button type="submit" class="btn btn-primary" style="padding: 0.65rem 1.5rem; white-space: nowrap;">
                                            🚀 Update Next Stage
                                        </button>
                                    </div>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Audit Timeline -->
                    <h2 style="font-size: 1.1rem; font-weight: 800; margin-bottom: 0.75rem; color: #0f172a;">Audit Lifecycle History & Logistics Trail</h2>
                    <div style="border-left: 2px solid #6366f1; padding-left: 1.25rem; margin-left: 0.5rem;">
                        <c:forEach var="h" items="${order.statusHistory}">
                            <div style="margin-bottom: 1.25rem; position: relative;">
                                <div style="font-size: 0.95rem; font-weight: 700; color: #0f172a;">
                                    <span class="badge ${h.newStatus == 'DELIVERED' ? 'badge-success' : (h.newStatus == 'CANCELLED' ? 'badge-danger' : 'badge-info')}">
                                        <c:out value="${h.newStatus}" />
                                    </span>
                                </div>
                                <div style="font-size: 0.85rem; color: #475569; margin-top: 0.25rem;">
                                    <c:out value="${h.remarks}" /><br>
                                    <small style="color: #94a3b8;">Updated by: <strong style="color: #64748b;"><c:out value="${h.changedByName}" /></strong> on <c:choose><c:when test="${not empty h.formattedCreatedAt}">${h.formattedCreatedAt}</c:when><c:otherwise>${h.createdAt}</c:otherwise></c:choose></small>
                                </div>
                            </div>
                        </c:forEach>
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
