package com.example.ecommerce.admin.controller;

import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import com.example.ecommerce.util.ServletUtils;

/**
 * Controller for Administrative Category Management.
 * Handled routes: /admin/categories, /admin/categories/add, /admin/categories/edit, /admin/categories/status
 */
@WebServlet(name = "AdminCategoryServlet", urlPatterns = {
        "/admin/categories", 
        "/admin/categories/add", 
        "/admin/categories/new",
        "/admin/categories/create",
        "/admin/categories/edit", 
        "/admin/categories/status"
})
public class AdminCategoryServlet extends HttpServlet {

    private CategoryService categoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.categoryService = new CategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        switch (path) {
            case "/admin/categories/new", "/admin/categories/create", "/admin/categories/add" -> showAddForm(request, response);
            case "/admin/categories/edit" -> showEditForm(request, response);
            default -> listCategories(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        if ("/admin/categories/add".equals(path) || "/admin/categories/new".equals(path) || "/admin/categories/create".equals(path)) {
            saveCategory(request, response, false);
        } else if ("/admin/categories/edit".equals(path)) {
            saveCategory(request, response, true);
        } else if ("/admin/categories/status".equals(path)) {
            toggleStatus(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/categories");
        }
    }

    private void listCategories(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Category> categories = categoryService.getAllCategories(false);
        request.setAttribute("categories", categories);
        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Category> parentOptions = categoryService.getAllCategories(true);
        request.setAttribute("parentOptions", parentOptions);
        request.setAttribute("isEdit", false);
        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int id = ServletUtils.parseIntParam(request, "id", -1);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin/categories?error=invalid_id");
            return;
        }
        Category category = categoryService.getCategoryById(id);
        List<Category> parentOptions = categoryService.getAllCategories(true);

        request.setAttribute("category", category);
        request.setAttribute("parentOptions", parentOptions);
        request.setAttribute("isEdit", true);
        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private void saveCategory(HttpServletRequest request, HttpServletResponse response, boolean isEdit) 
            throws ServletException, IOException {
        
        Category category = new Category();
        if (isEdit) {
            int catId = ServletUtils.parseIntParam(request, "categoryId", -1);
            if (catId <= 0) {
                response.sendRedirect(request.getContextPath() + "/admin/categories?error=invalid_id");
                return;
            }
            category.setCategoryId(catId);
        }

        String parentIdParam = request.getParameter("parentCategoryId");
        if (parentIdParam != null && !parentIdParam.trim().isEmpty() && !"0".equals(parentIdParam)) {
            try {
                category.setParentCategoryId(Integer.parseInt(parentIdParam.trim()));
            } catch (NumberFormatException e) {
                // Ignore or handle
            }
        }

        category.setCategoryName(request.getParameter("categoryName"));
        category.setSlug(request.getParameter("slug"));
        category.setDescription(request.getParameter("description"));
        category.setActive("on".equalsIgnoreCase(request.getParameter("active")) || "true".equalsIgnoreCase(request.getParameter("active")));

        try {
            if (isEdit) {
                categoryService.updateCategory(category);
            } else {
                categoryService.createCategory(category);
            }
            response.sendRedirect(request.getContextPath() + "/admin/categories?saved=true");
        } catch (ValidationException ve) {
            request.setAttribute("error", ve.getMessage());
            request.setAttribute("category", category);
            request.setAttribute("parentOptions", categoryService.getAllCategories(true));
            request.setAttribute("isEdit", isEdit);
            request.getRequestDispatcher("/index.html").forward(request, response);
        }
    }

    private void toggleStatus(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        int id = ServletUtils.parseIntParam(request, "id", -1);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin/categories?error=invalid_id");
            return;
        }
        boolean active = Boolean.parseBoolean(request.getParameter("active"));
        categoryService.toggleStatus(id, active);
        response.sendRedirect(request.getContextPath() + "/admin/categories?statusUpdated=true");
    }
}

