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
    <title>Administrator Management &amp; Registration | ShopKart Console</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        .admin-register-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: var(--radius-md);
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
        }
        .form-grid-3 {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 1rem;
        }
        .form-group-sm {
            margin-bottom: 0.75rem;
        }
        .form-group-sm label {
            display: block;
            font-size: 0.82rem;
            font-weight: 700;
            color: #334155;
            margin-bottom: 0.35rem;
        }
        .form-group-sm input {
            width: 100%;
            padding: 0.6rem 0.85rem;
            border: 1.5px solid #cbd5e1;
            border-radius: 6px;
            font-size: 0.88rem;
            font-family: inherit;
            outline: none;
            box-sizing: border-box;
            transition: all 0.2s ease;
        }
        .form-group-sm input:focus {
            border-color: #2563eb;
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
        }
        .admin-role-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.25rem;
            background: #fef3c7;
            border: 1px solid #fde047;
            color: #92400e;
            padding: 0.2rem 0.55rem;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 800;
            white-space: nowrap;
        }
        .admin-avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
            color: #f59e0b;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-weight: 800;
            font-size: 0.95rem;
            border: 1.5px solid #cbd5e1;
            flex-shrink: 0;
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
                    <li><a href="${pageContext.request.contextPath}/admin/admins" class="active"><span>👑</span> Admin Management</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/reports/sales"><span>📈</span> Sales &amp; Revenue</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/products"><span>📦</span> Product Catalog</a></li>
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
                <!-- Page Title Header -->
                <div style="background: #ffffff; padding: 1.25rem 1.5rem; border-radius: var(--radius-md); border: 1px solid #e2e8f0; margin-bottom: 1.25rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <h1 style="font-size: 1.5rem; font-weight: 800; color: #0f172a; margin: 0 0 0.25rem; display: flex; align-items: center; gap: 0.5rem;">
                            <span>👑</span> Administrator Management
                        </h1>
                        <p style="color: #64748b; font-size: 0.875rem; margin: 0;">
                            Register new system administrators and manage internal operator accounts.
                        </p>
                    </div>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <span class="badge badge-info" style="font-size: 0.88rem; padding: 0.4rem 0.85rem;">
                            <strong>${adminCount}</strong> Active Administrators
                        </span>
                    </div>
                </div>

                <!-- Flash Success Notification -->
                <c:if test="${not empty sessionScope.adminSuccessMessage}">
                    <div style="background: #f0fdf4; border-left: 4px solid var(--success, #16a34a); padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; color: #166534; font-size: 0.9rem; font-weight: 700; display: flex; align-items: center; gap: 0.5rem;">
                        <span>✓</span>
                        <span><c:out value="${sessionScope.adminSuccessMessage}" /></span>
                    </div>
                    <c:remove var="adminSuccessMessage" scope="session" />
                </c:if>

                <!-- Flash Error Notification -->
                <c:if test="${not empty error}">
                    <div style="background: #fef2f2; border-left: 4px solid var(--danger, #ef4444); padding: 0.85rem 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem; color: #991b1b; font-size: 0.9rem; font-weight: 700; display: flex; align-items: center; gap: 0.5rem;">
                        <span>⚠️</span>
                        <span><c:out value="${error}" /></span>
                    </div>
                </c:if>

                <!-- 1. Registered Administrators Table (Full Width) -->
                <div class="card" style="padding: 0; overflow: hidden; margin-bottom: 1.5rem;">
                    <div style="padding: 1rem 1.25rem; background: #f8fafc; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
                        <span style="font-weight: 800; color: #0f172a; font-size: 0.95rem;">
                            Registered Administrators
                        </span>
                        <span style="font-size: 0.8rem; color: #64748b;">
                            Role: ADMIN (Console Only)
                        </span>
                    </div>

                    <div class="table-responsive" style="border: none;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th style="min-width: 180px;">Admin User</th>
                                    <th style="min-width: 200px;">Email Address</th>
                                    <th style="min-width: 130px;">Mobile Phone</th>
                                    <th style="min-width: 160px;">Role / Status</th>
                                    <th style="min-width: 120px;">Joined Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty adminList}">
                                        <tr>
                                            <td colspan="5" style="text-align: center; padding: 2.5rem; color: #94a3b8;">
                                                No administrator accounts found.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="admin" items="${adminList}">
                                            <tr>
                                                <td>
                                                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                                                        <div class="admin-avatar">
                                                            <c:out value="${admin.firstName != null ? admin.firstName.substring(0, 1).toUpperCase() : 'A'}" />
                                                        </div>
                                                        <div>
                                                            <strong style="color: #0f172a; font-size: 0.92rem;">
                                                                <c:out value="${admin.firstName}" /> <c:out value="${admin.lastName}" />
                                                            </strong>
                                                            <div style="font-size: 0.75rem; color: #64748b;">ID: #${admin.userId}</div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <span style="font-weight: 600; color: #1e293b; font-size: 0.88rem;">
                                                        <c:out value="${admin.email}" />
                                                    </span>
                                                </td>
                                                <td>
                                                    <span style="color: #475569; font-size: 0.85rem;">
                                                        <c:out value="${not empty admin.phone ? admin.phone : '—'}" />
                                                    </span>
                                                </td>
                                                <td>
                                                    <div style="display: inline-flex; align-items: center; gap: 0.4rem; flex-wrap: nowrap;">
                                                        <span class="admin-role-badge">
                                                            <span>👑</span> ADMIN
                                                        </span>
                                                        <span class="badge badge-success">
                                                            <c:out value="${admin.status}" />
                                                        </span>
                                                    </div>
                                                </td>
                                                <td>
                                                    <small style="color: #64748b; font-weight: 600;">
                                                        <fmt:parseDate value="${admin.createdAt}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDate" type="both" />
                                                        <fmt:formatDate value="${parsedDate}" pattern="dd MMM yyyy" />
                                                    </small>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- 2. Register New Admin Form (Structured Card) -->
                <div class="admin-register-card">
                    <div style="border-bottom: 1px solid #e2e8f0; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                        <h2 style="font-size: 1.15rem; font-weight: 800; color: #0f172a; margin: 0 0 0.25rem; display: flex; align-items: center; gap: 0.4rem;">
                            <span>+</span> Register New Administrator
                        </h2>
                        <p style="font-size: 0.82rem; color: #64748b; margin: 0;">
                            Create an authorized administrator profile with full console management access.
                        </p>
                    </div>

                    <form action="${pageContext.request.contextPath}/admin/admins" method="POST">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <div class="form-grid-3">
                            <div class="form-group-sm">
                                <label for="firstName">First Name *</label>
                                <input type="text" id="firstName" name="firstName" required placeholder="e.g. John" value="<c:out value='${firstName}' />">
                            </div>
                            <div class="form-group-sm">
                                <label for="lastName">Last Name *</label>
                                <input type="text" id="lastName" name="lastName" required placeholder="e.g. Doe" value="<c:out value='${lastName}' />">
                            </div>
                            <div class="form-group-sm">
                                <label for="email">Admin Work Email *</label>
                                <input type="email" id="email" name="email" required placeholder="admin@shopkart.com" value="<c:out value='${email}' />">
                            </div>
                        </div>

                        <div class="form-grid-3" style="margin-top: 0.5rem;">
                            <div class="form-group-sm">
                                <label for="phone">Mobile Phone (10 Digits)</label>
                                <input type="tel" id="phone" name="phone" placeholder="9876543210" maxlength="10" pattern="[0-9]{10}" value="<c:out value='${phone}' />">
                            </div>
                            <div class="form-group-sm">
                                <label for="password">Password * (Min 8 Characters)</label>
                                <input type="password" id="password" name="password" required minlength="8" placeholder="••••••••">
                            </div>
                            <div class="form-group-sm">
                                <label for="confirmPassword">Confirm Password *</label>
                                <input type="password" id="confirmPassword" name="confirmPassword" required minlength="8" placeholder="••••••••">
                            </div>
                        </div>

                        <!-- Security Notice Callout -->
                        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0.85rem 1rem; margin: 1rem 0 1.25rem; font-size: 0.8rem; color: #475569; line-height: 1.45;">
                            <strong style="color: #0f172a; display: flex; align-items: center; gap: 0.35rem; margin-bottom: 0.25rem;">
                                <span>🔒</span> Role &amp; Access Policy:
                            </strong>
                            Registered accounts are strictly granted <strong>ADMIN</strong> console permissions. They manage catalog, orders, and reports but cannot perform customer storefront actions (such as cart, checkout, or address management).
                        </div>

                        <div style="display: flex; justify-content: flex-end;">
                            <button type="submit" class="btn btn-primary" style="padding: 0.75rem 2rem; font-weight: 800; font-size: 0.92rem; display: inline-flex; align-items: center; gap: 0.4rem;">
                                <span>👑</span> Create Administrator Account
                            </button>
                        </div>
                    </form>
                </div>

            </section>
        </div>
    </main>

    <!-- FOOTER -->
    <footer style="background-color: var(--amazon-dark, #0f172a); color: #cbd5e1; margin-top: auto; padding: 2rem; border-top: 1px solid var(--amazon-subnav, #1e293b); text-align: center;">
        <div style="max-width: 1200px; margin: 0 auto; font-size: 0.82rem; color: #94a3b8;">
            &copy; 2026 ShopKart Inc. &bull; Enterprise Console &bull; Confidential &amp; Proprietary
        </div>
    </footer>

</body>
</html>
