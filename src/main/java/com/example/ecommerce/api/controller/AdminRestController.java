package com.example.ecommerce.api.controller;

import com.example.ecommerce.admin.model.DashboardStats;
import com.example.ecommerce.admin.model.SalesReportItem;
import com.example.ecommerce.admin.service.DashboardService;
import com.example.ecommerce.api.dto.*;
import com.example.ecommerce.audit.model.AuditLog;
import com.example.ecommerce.audit.service.AuditLogService;
import com.example.ecommerce.auth.model.User;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.auth.service.AuthService;
import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.model.DiscountType;
import com.example.ecommerce.coupon.service.CouponService;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.inventory.model.Inventory;
import com.example.ecommerce.inventory.service.InventoryService;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderReturn;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.order.service.OrderReturnService;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.model.ProductImage;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API for Administrative Operations:
 * 1. Executive Dashboard KPIs & Sales Reports
 * 2. Product Catalog Management (CRUD, Status Toggle, Image mappings)
 * 3. Category Tree Management
 * 4. Order Lifecycle & Logistics Tracking Management
 * 5. Inventory Stock Adjustments & Transaction History
 * 6. Promotional Coupon Management
 * 7. Customer Returns Processing & Refunds
 * 8. User Management & Audit Logs
 *
 * Security: Protected by RoleFilter & server-side role validation (Requires ADMIN role).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final DashboardService dashboardService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final CouponService couponService;
    private final OrderReturnService orderReturnService;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final com.example.ecommerce.payment.service.PaymentService paymentService;

    @Autowired
    public AdminRestController(DashboardService dashboardService,
                               ProductService productService,
                               CategoryService categoryService,
                               OrderService orderService,
                               InventoryService inventoryService,
                               CouponService couponService,
                               OrderReturnService orderReturnService,
                               AuthService authService,
                               AuditLogService auditLogService,
                               com.example.ecommerce.payment.service.PaymentService paymentService) {
        this.dashboardService = dashboardService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.couponService = couponService;
        this.orderReturnService = orderReturnService;
        this.authService = authService;
        this.auditLogService = auditLogService;
        this.paymentService = paymentService;
    }

    private UserSession getAuthenticatedAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            throw new com.example.ecommerce.exception.UnauthorizedException("Please log in to access administrative operations.");
        }
        if (!user.isAdmin()) {
            throw new com.example.ecommerce.exception.AccessDeniedException("Access denied: Administrative privileges required.");
        }
        return user;
    }

    // =========================================================================
    // 1. DASHBOARD & KPI ANALYTICS
    // =========================================================================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDto>> getDashboardStats(HttpServletRequest request) {
        getAuthenticatedAdmin(request);

        DashboardStats kpis = dashboardService.getDashboardKPIs();
        AdminDashboardStatsDto dto = AdminDashboardStatsDto.from(kpis);

        // Recent 5 orders
        Pagination<Order> recentOrders = orderService.getAllOrders(null, null, 1, 5);
        dto.setRecentOrders(recentOrders.getItems().stream().map(OrderDto::fromEntity).collect(Collectors.toList()));

        // Top 5 selling products
        dto.setTopSellingProducts(dashboardService.getTopSellingProducts(5));

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesReport(
            @RequestParam(required = false) Integer year,
            HttpServletRequest request
    ) {
        getAuthenticatedAdmin(request);

        List<SalesReportItem> monthly = dashboardService.getMonthlySalesReport(year);
        List<SalesReportItem> byCategory = dashboardService.getCategoryRevenueReport();

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "monthlySales", monthly,
                "categoryRevenue", byCategory
        )));
    }

    // =========================================================================
    // 2. PRODUCT MANAGEMENT (CRUD)
    // =========================================================================

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<CatalogPageResponse<ProductDto>>> getProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "15") int pageSize,
            HttpServletRequest request
    ) {
        getAuthenticatedAdmin(request);

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setKeyword(q);
        criteria.setStatus(status); // Can be null to fetch all statuses for admin
        criteria.setPage(Math.max(1, page));
        criteria.setPageSize(Math.max(1, Math.min(100, pageSize)));

        Pagination<Product> rawPagination = productService.searchCatalog(criteria);
        List<ProductDto> dtos = rawPagination.getItems().stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(CatalogPageResponse.from(
                new Pagination<>(dtos, rawPagination.getCurrentPage(), rawPagination.getPageSize(), rawPagination.getTotalItems())
        )));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody AdminProductRequest req,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);

        Product p = new Product();
        p.setCategoryId(req.getCategoryId());
        p.setSku(req.getSku().trim().toUpperCase());
        p.setProductName(req.getProductName().trim());
        p.setSlug(req.getSlug() != null && !req.getSlug().trim().isEmpty() ? req.getSlug().trim().toLowerCase() :
                req.getProductName().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        p.setDescription(req.getDescription());
        p.setBrand(req.getBrand());
        p.setPrice(req.getPrice());
        p.setDiscountPercentage(req.getDiscountPercentage() != null ? req.getDiscountPercentage() : BigDecimal.ZERO);
        p.setTaxPercentage(req.getTaxPercentage() != null ? req.getTaxPercentage() : BigDecimal.ZERO);
        p.setWeightKg(req.getWeightKg() != null ? req.getWeightKg() : BigDecimal.ZERO);
        p.setStatus(req.getStatus() != null ? req.getStatus() : "ACTIVE");

        List<ProductImage> images = new ArrayList<>();
        if (req.getImageUrls() != null) {
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                images.add(new ProductImage(req.getImageUrls().get(i), p.getProductName(), i, i == 0));
            }
        }

        int initialStock = req.getStockQuantity() != null ? req.getStockQuantity() : 0;
        Product created = productService.createProduct(p, images, initialStock, 5);

        auditLogService.logAction(admin.getUserId(), "CREATE_PRODUCT", "Product", created.getProductId(), null, created.getProductName(), request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product created successfully", ProductDto.fromEntity(created)));
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable int productId,
            @Valid @RequestBody AdminProductRequest req,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);

        Product existing = productService.getProductById(productId);
        if (existing == null) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        existing.setCategoryId(req.getCategoryId());
        existing.setSku(req.getSku().trim().toUpperCase());
        existing.setProductName(req.getProductName().trim());
        if (req.getSlug() != null && !req.getSlug().trim().isEmpty()) {
            existing.setSlug(req.getSlug().trim().toLowerCase());
        }
        existing.setDescription(req.getDescription());
        existing.setBrand(req.getBrand());
        existing.setPrice(req.getPrice());
        existing.setDiscountPercentage(req.getDiscountPercentage() != null ? req.getDiscountPercentage() : BigDecimal.ZERO);
        existing.setTaxPercentage(req.getTaxPercentage() != null ? req.getTaxPercentage() : BigDecimal.ZERO);
        existing.setWeightKg(req.getWeightKg() != null ? req.getWeightKg() : BigDecimal.ZERO);
        existing.setStatus(req.getStatus() != null ? req.getStatus() : "ACTIVE");

        List<ProductImage> images = new ArrayList<>();
        if (req.getImageUrls() != null) {
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                images.add(new ProductImage(req.getImageUrls().get(i), existing.getProductName(), i, i == 0));
            }
        }

        productService.updateProduct(existing, images);
        auditLogService.logAction(admin.getUserId(), "UPDATE_PRODUCT", "Product", productId, null, existing.getProductName(), request.getRemoteAddr());

        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", ProductDto.fromEntity(existing)));
    }

    @PutMapping("/products/{productId}/status")
    public ResponseEntity<ApiResponse<Void>> toggleProductStatus(
            @PathVariable int productId,
            @RequestParam(required = false) String status,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);
        Product existing = productService.getProductById(productId);
        if (existing == null) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        String newStatus = status != null ? status.trim().toUpperCase() : ("ACTIVE".equalsIgnoreCase(existing.getStatus()) ? "INACTIVE" : "ACTIVE");
        if ("ACTIVE".equalsIgnoreCase(newStatus)) {
            productService.activateProduct(productId);
        } else {
            productService.deactivateProduct(productId);
        }

        auditLogService.logAction(admin.getUserId(), "TOGGLE_PRODUCT_STATUS", "Product", productId, existing.getStatus(), newStatus, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Status changed to " + newStatus, null));
    }

    // =========================================================================
    // 3. CATEGORY MANAGEMENT
    // =========================================================================

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories(HttpServletRequest request) {
        getAuthenticatedAdmin(request);
        List<Category> categories = categoryService.getAllCategories(false);
        return ResponseEntity.ok(ApiResponse.ok(categories.stream().map(CategoryDto::fromEntity).collect(Collectors.toList())));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody AdminCategoryRequest req,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);

        Category cat = new Category();
        cat.setCategoryName(req.getCategoryName().trim());
        cat.setSlug(req.getSlug() != null && !req.getSlug().trim().isEmpty() ? req.getSlug().trim().toLowerCase() :
                req.getCategoryName().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        cat.setDescription(req.getDescription());
        cat.setParentCategoryId(req.getParentCategoryId());
        cat.setActive(req.isActive());

        Category created = categoryService.createCategory(cat);
        auditLogService.logAction(admin.getUserId(), "CREATE_CATEGORY", "Category", created.getCategoryId(), null, created.getCategoryName(), request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Category created successfully", CategoryDto.fromEntity(created)));
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable int categoryId,
            @Valid @RequestBody AdminCategoryRequest req,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);

        Category cat = categoryService.getCategoryById(categoryId);
        if (cat == null) {
            throw new ResourceNotFoundException("Category not found: " + categoryId);
        }

        cat.setCategoryName(req.getCategoryName().trim());
        if (req.getSlug() != null && !req.getSlug().trim().isEmpty()) {
            cat.setSlug(req.getSlug().trim().toLowerCase());
        }
        cat.setDescription(req.getDescription());
        cat.setParentCategoryId(req.getParentCategoryId());
        cat.setActive(req.isActive());

        categoryService.updateCategory(cat);
        auditLogService.logAction(admin.getUserId(), "UPDATE_CATEGORY", "Category", categoryId, null, cat.getCategoryName(), request.getRemoteAddr());

        return ResponseEntity.ok(ApiResponse.ok("Category updated successfully", CategoryDto.fromEntity(cat)));
    }

    // =========================================================================
    // 4. ORDER MANAGEMENT
    // =========================================================================

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<CatalogPageResponse<OrderDto>>> getOrders(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "15") int pageSize,
            HttpServletRequest request
    ) {
        getAuthenticatedAdmin(request);

        OrderStatus orderStatus = null;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            try {
                orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        Pagination<Order> rawPagination = orderService.getAllOrders(q, orderStatus, safePage, safePageSize);
        List<OrderDto> dtos = rawPagination.getItems().stream().map(OrderDto::fromEntity).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(CatalogPageResponse.from(
                new Pagination<>(dtos, rawPagination.getCurrentPage(), rawPagination.getPageSize(), rawPagination.getTotalItems())
        )));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable int orderId, HttpServletRequest request) {
        getAuthenticatedAdmin(request);

        Order order = orderService.getAdminOrderById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }

        return ResponseEntity.ok(ApiResponse.ok(OrderDto.fromEntity(order)));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable int orderId,
            @Valid @RequestBody AdminOrderStatusUpdateRequest req,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);

        OrderStatus newStatus = OrderStatus.valueOf(req.getStatus().trim().toUpperCase());
        orderService.updateOrderStatusByAdmin(
                orderId,
                newStatus,
                req.getNotes(),
                admin.getUserId(),
                req.getCourierPartner(),
                req.getTrackingNumber(),
                null
        );

        Order updated = orderService.getAdminOrderById(orderId);
        auditLogService.logAction(admin.getUserId(), "UPDATE_ORDER_STATUS", "Order", orderId, null, newStatus.name(), request.getRemoteAddr());

        return ResponseEntity.ok(ApiResponse.ok("Order updated successfully", OrderDto.fromEntity(updated)));
    }

    // =========================================================================
    // 5. INVENTORY MANAGEMENT
    // =========================================================================

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<CatalogPageResponse<Inventory>>> getInventory(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            HttpServletRequest request
    ) {
        getAuthenticatedAdmin(request);
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        Pagination<Inventory> pagination = inventoryService.getInventoryList(q, filter, safePage, safePageSize);
        return ResponseEntity.ok(ApiResponse.ok(CatalogPageResponse.from(pagination)));
    }

    @PostMapping("/inventory/adjust")
    public ResponseEntity<ApiResponse<Void>> adjustStock(
            @Valid @RequestBody AdminStockAdjustmentRequest req,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);

        inventoryService.restockProduct(
                req.getProductId(),
                req.getQuantity(),
                "MANUAL_ADJUSTMENT",
                admin.getUserId(),
                req.getNotes() != null ? req.getNotes() : "Admin stock adjustment",
                admin.getUserId()
        );

        auditLogService.logAction(admin.getUserId(), "STOCK_ADJUSTMENT", "Product", req.getProductId(), null, String.valueOf(req.getQuantity()), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Stock updated successfully", null));
    }

    // =========================================================================
    // 6. PROMOTIONAL COUPONS
    // =========================================================================

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<CatalogPageResponse<Coupon>>> getCoupons(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            HttpServletRequest request
    ) {
        getAuthenticatedAdmin(request);
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        Pagination<Coupon> pagination = couponService.getAllCoupons(q, safePage, safePageSize);
        return ResponseEntity.ok(ApiResponse.ok(CatalogPageResponse.from(pagination)));
    }

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(
            @Valid @RequestBody AdminCouponRequest req,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);

        Coupon coupon = new Coupon();
        coupon.setCode(req.getCode().trim().toUpperCase());
        coupon.setDiscountType(DiscountType.valueOf(req.getDiscountType().trim().toUpperCase()));
        coupon.setDiscountValue(req.getDiscountValue());
        coupon.setMinSpend(req.getMinOrderAmount() != null ? req.getMinOrderAmount() : BigDecimal.ZERO);
        coupon.setMaxDiscount(req.getMaxDiscountAmount());
        coupon.setUsageLimit(req.getUsageLimit());
        coupon.setStartDate(req.getStartDate());
        coupon.setEndDate(req.getEndDate());
        coupon.setActive(req.isActive());

        int createdId = couponService.createCoupon(coupon);
        coupon.setCouponId(createdId);
        auditLogService.logAction(admin.getUserId(), "CREATE_COUPON", "Coupon", createdId, null, coupon.getCode(), request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Coupon created successfully", coupon));
    }

    @DeleteMapping("/coupons/{couponId}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable int couponId, HttpServletRequest request) {
        UserSession admin = getAuthenticatedAdmin(request);
        couponService.deleteCoupon(couponId);
        auditLogService.logAction(admin.getUserId(), "DELETE_COUPON", "Coupon", couponId, null, null, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Coupon deleted successfully", null));
    }

    // =========================================================================
    // 7. RETURNS PROCESSING
    // =========================================================================

    @GetMapping("/returns")
    public ResponseEntity<ApiResponse<CatalogPageResponse<OrderReturn>>> getReturns(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "15") int pageSize,
            HttpServletRequest request
    ) {
        getAuthenticatedAdmin(request);
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        Pagination<OrderReturn> rawPagination = orderReturnService.getAllReturns(null, status, safePage, safePageSize);
        return ResponseEntity.ok(ApiResponse.ok(CatalogPageResponse.from(rawPagination)));
    }

    @PutMapping("/returns/{returnId}/status")
    public ResponseEntity<ApiResponse<Void>> updateReturnStatus(
            @PathVariable int returnId,
            @Valid @RequestBody AdminReturnStatusUpdateRequest req,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);

        String status = req.getStatus().trim().toUpperCase();
        BigDecimal refundAmt = (req.getRefundAmount() != null) ? req.getRefundAmount() : BigDecimal.ZERO;
        orderReturnService.updateReturnStatus(returnId, status, req.getAdminNotes(), refundAmt);

        auditLogService.logAction(admin.getUserId(), "UPDATE_RETURN_STATUS", "OrderReturn", returnId, null, status, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Return status updated to " + status, null));
    }

    // =========================================================================
    // 8. USER MANAGEMENT & AUDIT LOGS
    // =========================================================================

    @GetMapping({"/users", "/admins"})
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsers(HttpServletRequest request) {
        getAuthenticatedAdmin(request);
        List<User> admins = authService.getAllAdmins();
        List<UserDto> dtos = admins.stream()
                .map(u -> UserDto.fromSession(UserSession.fromUser(u)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @PostMapping({"/users", "/admins"})
    public ResponseEntity<ApiResponse<UserDto>> createAdminUser(
            @Valid @RequestBody RegisterRequest req,
            HttpServletRequest request
    ) {
        UserSession currentAdmin = getAuthenticatedAdmin(request);

        User newAdmin = authService.registerAdmin(
                req.getEmail().trim().toLowerCase(),
                req.getPassword(),
                req.getConfirmPassword(),
                req.getFirstName().trim(),
                req.getLastName().trim(),
                req.getPhone() != null ? req.getPhone().trim() : "",
                currentAdmin.getUserId()
        );

        auditLogService.logAction(
                currentAdmin.getUserId(),
                "CREATE_ADMIN",
                "User",
                newAdmin.getUserId(),
                null,
                "Created admin: " + newAdmin.getEmail(),
                request.getRemoteAddr()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Administrator created successfully", UserDto.fromSession(UserSession.fromUser(newAdmin))));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs(
            @RequestParam(required = false, defaultValue = "50") int limit,
            HttpServletRequest request
    ) {
        getAuthenticatedAdmin(request);
        Pagination<AuditLog> pagination = auditLogService.getAuditLogs(null, null, null, 1, Math.min(100, limit));
        return ResponseEntity.ok(ApiResponse.ok(pagination.getItems()));
    }

    // =========================================================================
    // 9. PAYMENT RECONCILIATION
    // =========================================================================

    @GetMapping("/reconciliation")
    public ResponseEntity<ApiResponse<CatalogPageResponse<com.example.ecommerce.payment.model.PaymentReconciliation>>> getReconciliationList(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            HttpServletRequest request
    ) {
        getAuthenticatedAdmin(request);
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        Pagination<com.example.ecommerce.payment.model.PaymentReconciliation> pagination = paymentService.getReconciliationList(q, status, safePage, safePageSize);
        return ResponseEntity.ok(ApiResponse.ok(CatalogPageResponse.from(pagination)));
    }

    @GetMapping("/reconciliation/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReconciliationStats(HttpServletRequest request) {
        getAuthenticatedAdmin(request);
        Map<String, Object> stats = paymentService.getReconciliationStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/reconciliation/{id}")
    public ResponseEntity<ApiResponse<com.example.ecommerce.payment.model.PaymentReconciliation>> getReconciliationById(
            @PathVariable int id,
            HttpServletRequest request
    ) {
        getAuthenticatedAdmin(request);
        com.example.ecommerce.payment.model.PaymentReconciliation recon = paymentService.getReconciliationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation record not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(recon));
    }

    @PutMapping("/reconciliation/{id}")
    public ResponseEntity<ApiResponse<Void>> updateReconciliationResolution(
            @PathVariable int id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        UserSession admin = getAuthenticatedAdmin(request);
        String status = body != null ? body.get("reconciliationStatus") : null;
        if (status == null || status.trim().isEmpty()) {
            status = body != null ? body.get("status") : null;
        }
        String adminNotes = body != null ? body.get("adminNotes") : null;

        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Reconciliation status is required.");
        }

        boolean updated = paymentService.updateReconciliationResolution(id, status.trim().toUpperCase(), adminNotes, admin.getUserId());
        if (!updated) {
            throw new ResourceNotFoundException("Reconciliation record not found: " + id);
        }

        auditLogService.logAction(
                admin.getUserId(),
                "UPDATE_RECONCILIATION",
                "PaymentReconciliation",
                id,
                null,
                "Updated status to " + status + (adminNotes != null ? " with notes: " + adminNotes : ""),
                request.getRemoteAddr()
        );

        return ResponseEntity.ok(ApiResponse.ok("Reconciliation resolution updated successfully", null));
    }
}
