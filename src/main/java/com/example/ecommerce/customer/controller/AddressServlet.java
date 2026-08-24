package com.example.ecommerce.customer.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.customer.model.Address;
import com.example.ecommerce.customer.service.AddressService;
import com.example.ecommerce.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import com.example.ecommerce.util.ServletUtils;

/**
 * Controller for managing Customer Addresses.
 * Protected by AuthFilter.
 */
@WebServlet(name = "AddressServlet", urlPatterns = {
        "/addresses",
        "/addresses/add",
        "/addresses/edit",
        "/addresses/delete",
        "/addresses/default"
})
public class AddressServlet extends HttpServlet {

    private AddressService addressService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.addressService = new AddressService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user != null && user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=admin_cannot_manage_addresses");
            return;
        }

        String path = request.getServletPath();

        switch (path) {
            case "/addresses/add" -> showAddForm(request, response);
            case "/addresses/edit" -> showEditForm(request, response);
            default -> listAddresses(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user != null && user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=admin_cannot_manage_addresses");
            return;
        }

        String path = request.getServletPath();

        switch (path) {
            case "/addresses/add" -> saveAddress(request, response, false);
            case "/addresses/edit" -> saveAddress(request, response, true);
            case "/addresses/delete" -> deleteAddress(request, response);
            case "/addresses/default" -> setDefaultAddress(request, response);
            default -> response.sendRedirect(request.getContextPath() + "/addresses");
        }
    }

    private void listAddresses(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        List<Address> addresses = addressService.getUserAddresses(user.getUserId());
        request.setAttribute("addresses", addresses);

        request.getRequestDispatcher("/WEB-INF/views/customer/address-list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setAttribute("isEdit", false);
        String returnUrl = request.getParameter("returnUrl");
        if (returnUrl != null && !returnUrl.trim().isEmpty()) {
            request.setAttribute("returnUrl", returnUrl.trim());
        }
        request.getRequestDispatcher("/WEB-INF/views/customer/address-form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        try {
            int addressId = ServletUtils.parseIntParam(request, "id", -1);
            if (addressId <= 0) {
                response.sendRedirect(request.getContextPath() + "/addresses?error=invalid_id");
                return;
            }
            Address address = addressService.getAddressById(addressId, user.getUserId());
            request.setAttribute("address", address);
            request.setAttribute("isEdit", true);
            String returnUrl = request.getParameter("returnUrl");
            if (returnUrl != null && !returnUrl.trim().isEmpty()) {
                request.setAttribute("returnUrl", returnUrl.trim());
            }
            request.getRequestDispatcher("/WEB-INF/views/customer/address-form.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/addresses?error=unauthorized");
        }
    }

    private void saveAddress(HttpServletRequest request, HttpServletResponse response, boolean isEdit) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        String returnUrl = request.getParameter("returnUrl");

        Address address = new Address();
        if (isEdit) {
            int addressId = ServletUtils.parseIntParam(request, "addressId", -1);
            if (addressId <= 0) {
                response.sendRedirect(request.getContextPath() + "/addresses?error=invalid_id");
                return;
            }
            address.setAddressId(addressId);
        }
        address.setUserId(user.getUserId());
        address.setFullName(request.getParameter("fullName"));
        address.setPhone(request.getParameter("phone"));
        address.setAddressType(request.getParameter("addressType"));
        address.setAddressLine1(request.getParameter("addressLine1"));
        address.setAddressLine2(request.getParameter("addressLine2"));
        address.setCity(request.getParameter("city"));
        address.setState(request.getParameter("state"));
        address.setPostalCode(request.getParameter("postalCode"));
        address.setCountry(request.getParameter("country"));
        address.setDefaultAddress("on".equalsIgnoreCase(request.getParameter("isDefault")) || "true".equalsIgnoreCase(request.getParameter("isDefault")));

        try {
            if (isEdit) {
                addressService.updateAddress(address);
            } else {
                addressService.saveAddress(address);
            }
            
            if (returnUrl != null && isSafeRedirect(request, returnUrl)) {
                String target = returnUrl.startsWith(request.getContextPath())
                        ? returnUrl
                        : (returnUrl.startsWith("/") ? request.getContextPath() + returnUrl : request.getContextPath() + "/" + returnUrl);
                response.sendRedirect(target);
            } else {
                response.sendRedirect(request.getContextPath() + "/addresses?saved=true");
            }
        } catch (ValidationException ve) {
            request.setAttribute("error", ve.getMessage());
            request.setAttribute("address", address);
            request.setAttribute("isEdit", isEdit);
            if (returnUrl != null && !returnUrl.trim().isEmpty()) {
                request.setAttribute("returnUrl", returnUrl.trim());
            }
            request.getRequestDispatcher("/WEB-INF/views/customer/address-form.jsp").forward(request, response);
        }
    }

    private void deleteAddress(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        try {
            int addressId = ServletUtils.parseIntParam(request, "addressId", -1);
            if (addressId <= 0) {
                response.sendRedirect(request.getContextPath() + "/addresses?error=invalid_id");
                return;
            }
            addressService.deleteAddress(addressId, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/addresses?deleted=true");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/addresses?error=" + e.getMessage());
        }
    }

    private void setDefaultAddress(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return;
        }

        try {
            int addressId = ServletUtils.parseIntParam(request, "addressId", -1);
            if (addressId <= 0) {
                response.sendRedirect(request.getContextPath() + "/addresses?error=invalid_id");
                return;
            }
            addressService.setDefaultAddress(addressId, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/addresses?defaultUpdated=true");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/addresses?error=" + e.getMessage());
        }
    }

    private boolean isSafeRedirect(HttpServletRequest request, String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        String contextPath = request.getContextPath();
        return (url.startsWith(contextPath + "/") || url.startsWith("/"))
                && !url.startsWith("//")
                && !url.contains("://")
                && !url.contains("\n")
                && !url.contains("\r");
    }
}
