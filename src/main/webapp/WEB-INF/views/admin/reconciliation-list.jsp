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
    <title>Failed Payments & Reconciliation | ShopKart Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        .recon-badge {
            display: inline-block;
            padding: 0.3rem 0.65rem;
            border-radius: 9999px;
            font-size: 0.72rem;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }
        .recon-badge.PENDING { background: #fef2f2; color: #dc2626; border: 1px solid #fca5a5; }
        .recon-badge.VERIFIED_DEBITED { background: #fffbeb; color: #d97706; border: 1px solid #fde68a; }
        .recon-badge.REFUND_INITIATED { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
        .recon-badge.REFUND_COMPLETED { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
        .recon-badge.MANUALLY_CREDITED { background: #f5f3ff; color: #7c3aed; border: 1px solid #ddd6fe; }
        .recon-badge.NOT_DEBITED { background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; }
        .recon-badge.RESOLVED { background: #f0fdf4; color: #15803d; border: 1px solid #86efac; }

        /* Modal Overlay */
        .modal-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(15, 23, 42, 0.65);
            backdrop-filter: blur(4px);
            z-index: 1000;
            align-items: center;
            justify-content: center;
            padding: 1.5rem;
        }
        .modal-overlay.active { display: flex; }
        .modal-card {
            background: #ffffff;
            border-radius: 16px;
            max-width: 680px;
            width: 100%;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 20px 40px rgba(0,0,0,0.25);
            border: 1px solid #e2e8f0;
            animation: modalFadeIn 0.25s ease-out;
        }
        @keyframes modalFadeIn {
            from { opacity: 0; transform: scale(0.96) translateY(10px); }
            to { opacity: 1; transform: scale(1) translateY(0); }
        }
    </style>
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
                    <li><a href="${pageContext.request.contextPath}/admin/inventory"><span>🏭</span> Inventory &amp; Stock</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/orders"><span>🛒</span> Order Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/returns"><span>🔄</span> Return Requests</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reconciliation" class="active"><span>⚖️</span> Failed Payments &amp; Reconciliation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons &amp; Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                
                <!-- Notification message -->
                <c:if test="${not empty successMessage}">
                    <div style="background: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; padding: 0.9rem 1.25rem; border-radius: 10px; margin-bottom: 1.25rem; font-weight: 700; display: flex; align-items: center; gap: 0.5rem;">
                        <span>✅</span>
                        <span><c:out value="${successMessage}" /></span>
                    </div>
                </c:if>

                <!-- Page Header Banner -->
                <div style="background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0; margin-bottom: 1.25rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">Failed Payments &amp; Reconciliation Ledger</h1>
                        <p style="color: #64748b; font-size: 0.875rem; margin: 0;">Audit failed transactions, investigate customer bank debit disputes, and log refund/settlement resolutions.</p>
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        <a href="${pageContext.request.contextPath}/admin/payments" class="btn btn-secondary btn-sm">💳 View Settled Payments</a>
                    </div>
                </div>

                <!-- Financial & Disputed KPI Grid -->
                <div class="grid grid-cols-4" style="margin-bottom: 1.25rem; gap: 1rem;">
                    <div class="stat-card" style="border-top: 4px solid #ef4444;">
                        <div class="stat-label">Total Failed Records</div>
                        <div class="stat-value" style="color: #ef4444;">${stats.totalFailed}</div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600;">Recorded in failure ledger</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #f59e0b;">
                        <div class="stat-label">Pending Queries / Audit</div>
                        <div class="stat-value" style="color: #d97706;">${stats.pendingAudit}</div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600;">Requires admin action</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #3b82f6;">
                        <div class="stat-label">Verified Debited Claims</div>
                        <div class="stat-value" style="color: #2563eb;">${stats.verifiedDebited}</div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600;">Customer bank deducted</div>
                    </div>
                    <div class="stat-card" style="border-top: 4px solid #6366f1;">
                        <div class="stat-label">Total Disputed Value</div>
                        <div class="stat-value" style="color: #4f46e5;">
                            ₹<fmt:formatNumber value="${stats.totalDisputedAmount}" minFractionDigits="0" maxFractionDigits="0" />
                        </div>
                        <div style="font-size: 0.75rem; color: #64748b; font-weight: 600;">Cumulative failed amount</div>
                    </div>
                </div>

                <!-- Filter & Search Toolbar -->
                <div class="card" style="padding: 1rem 1.25rem; margin-bottom: 1.25rem;">
                    <form action="${pageContext.request.contextPath}/admin/reconciliation" method="GET" style="display: flex; gap: 0.85rem; align-items: center; flex-wrap: wrap;">
                        <div style="flex: 1; min-width: 260px;">
                            <input type="text" name="q" placeholder="Search by Txn Ref, Order #, Customer Name, Email, Phone..." value="<c:out value='${keyword}' />" 
                                   class="form-input" style="width: 100%;">
                        </div>
                        <div style="min-width: 180px;">
                            <select name="status" class="form-input" style="width: 100%; cursor: pointer;" onchange="this.form.submit()">
                                <option value="ALL" ${currentStatus eq 'ALL' ? 'selected' : ''}>Status: All Statuses</option>
                                <option value="PENDING" ${currentStatus eq 'PENDING' ? 'selected' : ''}>⏳ Pending Investigation</option>
                                <option value="VERIFIED_DEBITED" ${currentStatus eq 'VERIFIED_DEBITED' ? 'selected' : ''}>⚠️ Verified Debited</option>
                                <option value="REFUND_INITIATED" ${currentStatus eq 'REFUND_INITIATED' ? 'selected' : ''}>🔄 Refund Initiated</option>
                                <option value="REFUND_COMPLETED" ${currentStatus eq 'REFUND_COMPLETED' ? 'selected' : ''}>✅ Refund Completed</option>
                                <option value="MANUALLY_CREDITED" ${currentStatus eq 'MANUALLY_CREDITED' ? 'selected' : ''}>📦 Manually Credited</option>
                                <option value="NOT_DEBITED" ${currentStatus eq 'NOT_DEBITED' ? 'selected' : ''}>🚫 Confirmed Not Debited</option>
                                <option value="RESOLVED" ${currentStatus eq 'RESOLVED' ? 'selected' : ''}>🎉 Case Resolved</option>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-primary">Filter</button>
                        <a href="${pageContext.request.contextPath}/admin/reconciliation" class="btn btn-secondary">Reset</a>
                    </form>
                </div>

                <!-- Table Card -->
                <div class="card" style="padding: 0; overflow: hidden; border-radius: 12px;">
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>ID / Timestamp</th>
                                    <th>Txn Reference ID</th>
                                    <th>Order Reference</th>
                                    <th>Customer Details</th>
                                    <th>Amount</th>
                                    <th>Failure / Gateway Reason</th>
                                    <th>Reconciliation Status</th>
                                    <th style="text-align: center;">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty pagination.items}">
                                        <c:forEach var="item" items="${pagination.items}">
                                            <tr>
                                                <td>
                                                    <div style="font-weight: 800; color: #0f172a;">#REC-${item.reconciliationId}</div>
                                                    <div style="font-size: 0.75rem; color: #64748b;">${item.formattedCreatedAt}</div>
                                                </td>
                                                <td>
                                                    <span style="font-family: monospace; font-weight: 700; color: #334155; font-size: 0.82rem; background: #f1f5f9; padding: 0.15rem 0.45rem; border-radius: 4px;">
                                                        <c:out value="${item.transactionReference}" />
                                                    </span>
                                                    <c:if test="${not empty item.gatewayOrderId}">
                                                        <div style="font-size: 0.72rem; color: #64748b; margin-top: 0.2rem;">
                                                            RZP: <c:out value="${item.gatewayOrderId}" />
                                                        </div>
                                                    </c:if>
                                                    <div style="font-size: 0.72rem; color: #94a3b8; margin-top: 0.15rem;">
                                                        Method: <strong><c:out value="${item.paymentMethod}" /></strong>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div style="font-weight: 800; color: #0f172a; font-family: monospace; font-size: 0.88rem;">
                                                        #<c:out value="${item.orderNumber}" />
                                                    </div>
                                                </td>
                                                <td>
                                                    <div style="font-weight: 700; color: #0f172a;"><c:out value="${item.customerName}" /></div>
                                                    <div style="font-size: 0.75rem; color: #64748b;"><c:out value="${item.customerEmail}" /></div>
                                                    <c:if test="${not empty item.customerPhone}">
                                                        <div style="font-size: 0.75rem; color: #64748b;">📞 <c:out value="${item.customerPhone}" /></div>
                                                    </c:if>
                                                </td>
                                                <td>
                                                    <strong style="color: #0f172a; font-size: 1rem;">
                                                        ₹<fmt:formatNumber value="${item.amount}" minFractionDigits="2" />
                                                    </strong>
                                                </td>
                                                <td>
                                                    <div style="font-size: 0.82rem; color: #b91c1c; font-weight: 600; max-width: 220px; line-height: 1.35;">
                                                        <c:out value="${item.failureReason}" />
                                                    </div>
                                                </td>
                                                <td>
                                                    <span class="recon-badge ${item.reconciliationStatus}">
                                                        <c:out value="${item.reconciliationStatus}" />
                                                    </span>
                                                    <c:if test="${not empty item.resolverName}">
                                                        <div style="font-size: 0.7rem; color: #64748b; margin-top: 0.25rem;">
                                                            By: <c:out value="${item.resolverName}" />
                                                        </div>
                                                    </c:if>
                                                </td>
                                                <td style="text-align: center;">
                                                    <button type="button" class="btn btn-secondary btn-sm" 
                                                            onclick="openReconcileModal(${item.reconciliationId}, '${item.orderNumber}', '${item.customerName}', '${item.customerEmail}', '${item.customerPhone}', '${item.transactionReference}', '${item.gatewayOrderId}', '${item.paymentMethod}', '${item.amount}', '${item.failureReason}', '${item.reconciliationStatus}', '${item.adminNotes}', '${item.resolverName}', '${item.formattedResolvedAt}')"
                                                            style="white-space: nowrap; font-weight: 700;">
                                                        🔍 Inspect &amp; Resolve
                                                    </button>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="8" style="text-align: center; padding: 3rem; color: #64748b;">
                                                <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🎉</div>
                                                <div style="font-size: 1.1rem; font-weight: 800; color: #0f172a;">No Failed Transactions Found</div>
                                                <p style="margin-top: 0.25rem; font-size: 0.875rem;">No failed payments match the selected status filter or search keyword.</p>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${pagination.totalPages > 1}">
                        <div style="padding: 1rem 1.5rem; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #e2e8f0; background: #fafafa;">
                            <div style="font-size: 0.85rem; color: #64748b;">
                                Showing Page <strong>${pagination.currentPage}</strong> of <strong>${pagination.totalPages}</strong> (${pagination.totalItems} total records)
                            </div>
                            <div style="display: flex; gap: 0.5rem;">
                                <c:if test="${pagination.hasPreviousPage}">
                                    <a href="${pageContext.request.contextPath}/admin/reconciliation?page=${pagination.currentPage - 1}&q=<c:out value='${keyword}'/>&status=<c:out value='${currentStatus}'/>" class="btn btn-secondary btn-sm">&larr; Previous</a>
                                </c:if>
                                <c:if test="${pagination.hasNextPage}">
                                    <a href="${pageContext.request.contextPath}/admin/reconciliation?page=${pagination.currentPage + 1}&q=<c:out value='${keyword}'/>&status=<c:out value='${currentStatus}'/>" class="btn btn-secondary btn-sm">Next &rarr;</a>
                                </c:if>
                            </div>
                        </div>
                    </c:if>
                </div>

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

    <!-- RECONCILIATION DETAIL & RESOLUTION MODAL -->
    <div id="reconcileModal" class="modal-overlay">
        <div class="modal-card">
            <div style="padding: 1.5rem 1.75rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h3 style="font-size: 1.25rem; font-weight: 800; color: #0f172a; margin: 0;">Dispute &amp; Payment Reconciliation</h3>
                    <span id="modalRecId" style="font-size: 0.8rem; color: #64748b; font-family: monospace;"></span>
                </div>
                <button type="button" onclick="closeReconcileModal()" style="background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #64748b;">&times;</button>
            </div>

            <div style="padding: 1.5rem 1.75rem;">
                
                <!-- Summary Details Grid -->
                <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem; margin-bottom: 1.5rem; display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; font-size: 0.88rem;">
                    <div>
                        <span style="color: #64748b; font-size: 0.75rem; font-weight: 700; text-transform: uppercase;">Order Number:</span>
                        <div id="modalOrderNo" style="font-weight: 800; color: #0284c7; font-size: 1rem;"></div>
                    </div>
                    <div>
                        <span style="color: #64748b; font-size: 0.75rem; font-weight: 700; text-transform: uppercase;">Disputed Amount:</span>
                        <div id="modalAmount" style="font-weight: 900; color: #0f172a; font-size: 1.1rem;"></div>
                    </div>
                    <div>
                        <span style="color: #64748b; font-size: 0.75rem; font-weight: 700; text-transform: uppercase;">Customer Information:</span>
                        <div id="modalCustomer" style="font-weight: 700; color: #0f172a;"></div>
                    </div>
                    <div>
                        <span style="color: #64748b; font-size: 0.75rem; font-weight: 700; text-transform: uppercase;">Transaction Reference:</span>
                        <div id="modalTxnRef" style="font-family: monospace; font-weight: 700; color: #334155;"></div>
                    </div>
                    <div style="grid-column: 1 / -1;">
                        <span style="color: #64748b; font-size: 0.75rem; font-weight: 700; text-transform: uppercase;">Failure / Gateway Reason:</span>
                        <div id="modalFailureReason" style="color: #b91c1c; font-weight: 600; background: #fee2e2; padding: 0.5rem 0.75rem; border-radius: 6px; margin-top: 0.25rem;"></div>
                    </div>
                </div>

                <!-- Status Update Form -->
                <form action="${pageContext.request.contextPath}/admin/reconciliation/status" method="POST">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <input type="hidden" id="modalFormRecId" name="reconciliationId">

                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-size: 0.8rem; font-weight: 800; color: #334155; text-transform: uppercase; margin-bottom: 0.4rem;">
                            Reconciliation Action / Resolution Status <span style="color: #ef4444;">*</span>
                        </label>
                        <select id="modalStatusSelect" name="reconciliationStatus" class="form-input" style="width: 100%; font-weight: 700;" required>
                            <option value="PENDING">⏳ PENDING (Awaiting Bank Confirmation / User Support Query)</option>
                            <option value="VERIFIED_DEBITED">⚠️ VERIFIED_DEBITED (Money deducted from customer bank, requires refund/credit)</option>
                            <option value="REFUND_INITIATED">🔄 REFUND_INITIATED (Bank refund requested through Payment Gateway)</option>
                            <option value="REFUND_COMPLETED">✅ REFUND_COMPLETED (Refund successfully credited back to customer)</option>
                            <option value="MANUALLY_CREDITED">📦 MANUALLY_CREDITED (Verified payment receipt &amp; manually processed order)</option>
                            <option value="NOT_DEBITED">🚫 NOT_DEBITED (Confirmed transaction failed prior to bank deduction)</option>
                            <option value="RESOLVED">🎉 RESOLVED (Case fully closed)</option>
                        </select>
                    </div>

                    <div style="margin-bottom: 1.5rem;">
                        <label style="display: block; font-size: 0.8rem; font-weight: 800; color: #334155; text-transform: uppercase; margin-bottom: 0.4rem;">
                            Admin Audit &amp; Resolution Notes
                        </label>
                        <textarea id="modalAdminNotes" name="adminNotes" class="form-input" rows="4" style="width: 100%; font-family: inherit; line-height: 1.45;" placeholder="Enter customer resolution details, bank UTR reference numbers, or gateway refund IDs..."></textarea>
                    </div>

                    <div id="modalResolverAudit" style="font-size: 0.78rem; color: #64748b; margin-bottom: 1.25rem; display: none;">
                        Last resolved by: <strong id="modalResolverName"></strong> on <span id="modalResolvedDate"></span>
                    </div>

                    <div style="display: flex; gap: 0.75rem; justify-content: flex-end;">
                        <button type="button" onclick="closeReconcileModal()" class="btn btn-secondary">Cancel</button>
                        <button type="submit" class="btn btn-primary" style="font-weight: 800;">💾 Save Resolution</button>
                    </div>
                </form>

            </div>
        </div>
    </div>

    <!-- Scripts -->
    <script nonce="${cspNonce}">
        function openReconcileModal(recId, orderNo, custName, custEmail, custPhone, txnRef, gwOrderId, method, amount, failureReason, status, notes, resolverName, resolvedAt) {
            document.getElementById('modalRecId').textContent = 'Audit Record #REC-' + recId;
            document.getElementById('modalFormRecId').value = recId;
            document.getElementById('modalOrderNo').textContent = '#' + orderNo;
            document.getElementById('modalAmount').textContent = '₹' + parseFloat(amount).toFixed(2);
            document.getElementById('modalCustomer').innerHTML = custName + '<br><small style="color: #64748b;">' + custEmail + (custPhone ? ' &bull; ' + custPhone : '') + '</small>';
            document.getElementById('modalTxnRef').innerHTML = txnRef + (gwOrderId ? '<br><small style="color: #64748b;">RZP Order: ' + gwOrderId + '</small>' : '');
            document.getElementById('modalFailureReason').textContent = failureReason || 'Transaction declined / user cancelled';
            document.getElementById('modalStatusSelect').value = status;
            document.getElementById('modalAdminNotes').value = notes && notes !== 'null' ? notes : '';

            const auditDiv = document.getElementById('modalResolverAudit');
            if (resolverName && resolverName !== 'null') {
                document.getElementById('modalResolverName').textContent = resolverName;
                document.getElementById('modalResolvedDate').textContent = resolvedAt;
                auditDiv.style.display = 'block';
            } else {
                auditDiv.style.display = 'none';
            }

            document.getElementById('reconcileModal').classList.add('active');
        }

        function closeReconcileModal() {
            document.getElementById('reconcileModal').classList.remove('active');
        }

        window.onclick = function(event) {
            const modal = document.getElementById('reconcileModal');
            if (event.target === modal) {
                closeReconcileModal();
            }
        };
    </script>

</body>
</html>
