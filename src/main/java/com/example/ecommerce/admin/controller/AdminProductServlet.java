package com.example.ecommerce.admin.controller;

import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.model.ProductImage;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Administrative Product Management.
 * Routes: /admin/products, /admin/products/add, /admin/products/edit, /admin/products/status
 */
@WebServlet(name = "AdminProductServlet", urlPatterns = {
        "/admin/products", 
        "/admin/products/add", 
        "/admin/products/new",
        "/admin/products/create",
        "/admin/products/edit", 
        "/admin/products/status"
})
public class AdminProductServlet extends HttpServlet {

    private ProductService productService;
    private CategoryService categoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        switch (path) {
            case "/admin/products/new", "/admin/products/create", "/admin/products/add" -> showAddForm(request, response);
            case "/admin/products/edit" -> showEditForm(request, response);
            default -> listProducts(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        if ("/admin/products/add".equals(path) || "/admin/products/new".equals(path) || "/admin/products/create".equals(path)) {
            saveProduct(request, response, false);
        } else if ("/admin/products/edit".equals(path)) {
            saveProduct(request, response, true);
        } else if ("/admin/products/status".equals(path)) {
            toggleStatus(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/products");
        }
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setStatus(request.getParameter("status")); // can be null for all statuses
        criteria.setKeyword(request.getParameter("q"));
        
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                criteria.setPage(Integer.parseInt(pageParam.trim()));
            } catch (NumberFormatException ignored) {}
        }
        criteria.setPageSize(15);

        Pagination<Product> pagination = productService.searchCatalog(criteria);

        request.setAttribute("pagination", pagination);
        request.setAttribute("criteria", criteria);
        request.getRequestDispatcher("/WEB-INF/views/admin/product-list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Category> categories = categoryService.getAllCategories(true);
        request.setAttribute("categories", categories);
        request.setAttribute("isEdit", false);
        request.getRequestDispatcher("/WEB-INF/views/admin/product-form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Product product = productService.getProductById(id);
        List<Category> categories = categoryService.getAllCategories(true);

        request.setAttribute("product", product);
        request.setAttribute("categories", categories);
        request.setAttribute("isEdit", true);
        request.getRequestDispatcher("/WEB-INF/views/admin/product-form.jsp").forward(request, response);
    }

    private void saveProduct(HttpServletRequest request, HttpServletResponse response, boolean isEdit) 
            throws ServletException, IOException {
        
        Product product = new Product();
        if (isEdit) {
            product.setProductId(Integer.parseInt(request.getParameter("productId")));
        }

        product.setCategoryId(Integer.parseInt(request.getParameter("categoryId")));
        product.setSku(request.getParameter("sku"));
        product.setProductName(request.getParameter("productName"));
        product.setSlug(request.getParameter("slug"));
        product.setDescription(request.getParameter("description"));
        product.setBrand(request.getParameter("brand"));
        product.setPrice(new BigDecimal(request.getParameter("price")));
        
        String discount = request.getParameter("discountPercentage");
        product.setDiscountPercentage(discount != null && !discount.trim().isEmpty() ? new BigDecimal(discount) : BigDecimal.ZERO);
        
        String tax = request.getParameter("taxPercentage");
        product.setTaxPercentage(tax != null && !tax.trim().isEmpty() ? new BigDecimal(tax) : BigDecimal.ZERO);

        String weight = request.getParameter("weightKg");
        product.setWeightKg(weight != null && !weight.trim().isEmpty() ? new BigDecimal(weight) : BigDecimal.ZERO);

        product.setStatus(request.getParameter("status"));

        // Parse Primary & Secondary Image URLs
        List<ProductImage> images = new ArrayList<>();
        String primaryImgUrl = request.getParameter("primaryImageUrl");
        if (primaryImgUrl != null && !primaryImgUrl.trim().isEmpty()) {
            images.add(new ProductImage(primaryImgUrl.trim(), product.getProductName(), 1, true));
        }

        String extraImg1 = request.getParameter("extraImageUrl1");
        if (extraImg1 != null && !extraImg1.trim().isEmpty()) {
            images.add(new ProductImage(extraImg1.trim(), product.getProductName(), 2, false));
        }

        try {
            if (isEdit) {
                productService.updateProduct(product, images);
            } else {
                int initialStock = Integer.parseInt(request.getParameter("initialStock"));
                int lowStock = Integer.parseInt(request.getParameter("lowStockThreshold"));
                productService.createProduct(product, images, initialStock, lowStock);
            }
            response.sendRedirect(request.getContextPath() + "/admin/products?saved=true");
        } catch (ValidationException ve) {
            request.setAttribute("error", ve.getMessage());
            request.setAttribute("product", product);
            request.setAttribute("categories", categoryService.getAllCategories(true));
            request.setAttribute("isEdit", isEdit);
            request.getRequestDispatcher("/WEB-INF/views/admin/product-form.jsp").forward(request, response);
        }
    }

    private void toggleStatus(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String status = request.getParameter("status");
        if ("ACTIVE".equalsIgnoreCase(status)) {
            productService.activateProduct(id);
        } else {
            productService.deactivateProduct(id);
        }
        response.sendRedirect(request.getContextPath() + "/admin/products?statusUpdated=true");
    }
}
