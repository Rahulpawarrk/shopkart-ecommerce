<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Customer Review Moderation | ShopKart Console</title>
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
                    <li><a href="${pageContext.request.contextPath}/admin/payments"><span>💳</span> Payment History</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reconciliation"><span>⚖️</span> Failed Payments &amp; Reconciliation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reviews" class="active"><span>⭐</span> Review Moderation</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/coupons"><span>🏷️</span> Coupons & Offers</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/admins"><span>👑</span> Admin Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/audit-logs"><span>🛡️</span> Security Audit Logs</a></li>
                </ul>
            </aside>

            <!-- Main Content Area -->
            <section>
                <div style="background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0; margin-bottom: 1.25rem;">
                    <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.25rem;">Customer Review Moderation</h1>
                    <p style="color: #64748b; font-size: 0.875rem;">Moderate verified buyer feedback, approve authentic ratings, and prevent spam.</p>
                </div>

                <!-- Search -->
                <div class="card" style="padding: 1rem 1.25rem; margin-bottom: 1.25rem;">
                    <form action="${pageContext.request.contextPath}/admin/reviews" method="GET" style="display: flex; gap: 1rem;">
                        <input type="text" name="q" placeholder="Search by Product Name, Review Title, Customer..." value="<c:out value='${keyword}' />" 
                               class="form-input" style="flex: 1;">
                        <button type="submit" class="btn btn-primary">Search</button>
                        <a href="${pageContext.request.contextPath}/admin/reviews" class="btn btn-secondary">Reset</a>
                    </form>
                </div>

                <!-- Reviews Table -->
                <div class="card" style="padding: 0; overflow: hidden;">
                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Customer</th>
                                    <th>Rating</th>
                                    <th>Review Content</th>
                                    <th>Verified</th>
                                    <th>Visibility</th>
                                    <th style="text-align: right;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty pagination.items}">
                                        <tr>
                                            <td colspan="7" style="text-align: center; padding: 2.5rem; color: #94a3b8;">
                                                No customer reviews matching query.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="rev" items="${pagination.items}">
                                            <tr>
                                                <td>
                                                    <strong style="color: #0f172a;"><c:out value="${rev.productName}" /></strong>
                                                </td>
                                                <td>
                                                    <strong style="color: #334155;"><c:out value="${rev.customerName}" /></strong><br>
                                                    <small style="color: #64748b;"><c:out value="${rev.customerEmail}" /></small>
                                                </td>
                                                <td>
                                                    <span style="color: #f59e0b; font-weight: 800; font-size: 0.95rem;">★ ${rev.rating}.0</span>
                                                </td>
                                                <td style="max-width: 280px;">
                                                    <strong style="color: #1e293b;"><c:out value="${rev.title}" /></strong><br>
                                                    <small style="color: #475569; line-height: 1.4;"><c:out value="${rev.comment}" /></small>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${rev.verifiedPurchase}">
                                                            <span class="badge badge-success">✓ VERIFIED BUYER</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background: #f1f5f9; color: #64748b;">UNVERIFIED</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${rev.approved}">
                                                            <span class="badge badge-success">PUBLIC (Visible)</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-danger">HIDDEN</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 0.35rem;">
                                                        <form action="${pageContext.request.contextPath}/admin/reviews/approve" method="POST">
                                                            <input type="hidden" name="reviewId" value="${rev.reviewId}">
                                                            <input type="hidden" name="approved" value="${!rev.approved}">
                                                            <button type="submit" class="btn btn-secondary btn-sm">
                                                                ${rev.approved ? 'Hide' : 'Approve'}
                                                            </button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/admin/reviews/delete" method="POST" onsubmit="return confirm('Delete review permanently?');">
                                                            <input type="hidden" name="reviewId" value="${rev.reviewId}">
                                                            <button type="submit" class="btn btn-danger btn-sm">
                                                                Delete
                                                            </button>
                                                        </form>
                                                    </div>
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
