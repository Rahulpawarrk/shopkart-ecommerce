package com.example.ecommerce.order.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderReturn;
import com.example.ecommerce.order.service.OrderReturnService;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

/**
 * Controller handling Customer Order Returns, Replacements, and Return History.
 * Routes: /order/return, /returns
 * Protected by AuthFilter.
 */
@WebServlet(name = "OrderReturnServlet", urlPatterns = {"/order/return", "/returns"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2 MB
    maxFileSize = 1024 * 1024 * 10,       // 10 MB
    maxRequestSize = 1024 * 1024 * 30     // 30 MB
)
public class OrderReturnServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(OrderReturnServlet.class);
    private OrderReturnService orderReturnService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.orderReturnService = new OrderReturnService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String path = request.getServletPath();
        if ("/returns".equals(path)) {
            int page = 1;
            String pStr = request.getParameter("page");
            if (pStr != null && !pStr.trim().isEmpty()) {
                try { page = Integer.parseInt(pStr.trim()); } catch (NumberFormatException ignored) {}
            }
            Pagination<OrderReturn> returns = orderReturnService.getUserReturns(user.getUserId(), page, 10);
            request.setAttribute("returns", returns);
            request.getRequestDispatcher("/index.html").forward(request, response);
        } else {
            String orderIdStr = request.getParameter("orderId");
            if (orderIdStr != null && !orderIdStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/order?id=" + orderIdStr.trim());
            } else {
                response.sendRedirect(request.getContextPath() + "/orders");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderIdStr = request.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            orderIdStr = request.getParameter("id");
        }
        String returnReason = request.getParameter("returnReason");
        String resolutionType = request.getParameter("resolutionType");
        String comments = request.getParameter("comments");

        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/orders?error=invalid_order");
            return;
        }

        Order order;
        try {
            order = new OrderService().getOrderByIdOrNumber(orderIdStr.trim(), user.getUserId());
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/orders?error=invalid_order");
            return;
        }

        int orderId = order.getOrderId();

        // Process optional damaged/defective product photo upload
        String imageUrl = null;
        try {
            Part part = request.getPart("returnImage");
            if (part != null && part.getSize() > 0 && part.getSubmittedFileName() != null && !part.getSubmittedFileName().trim().isEmpty()) {
                
                // Magic byte validation — verify actual file content matches expected image type
                byte[] header = new byte[12];
                try (java.io.InputStream is = part.getInputStream()) {
                    is.read(header);
                }
                if (!isValidImageMagicBytes(header)) {
                    request.setAttribute("error", "Invalid image file. Only JPEG, PNG, WEBP, and GIF are accepted.");
                    response.sendRedirect(request.getContextPath() + "/order?id=" + order.getOrderNumber() + "&returnError=" + URLEncoder.encode("Invalid image file.", StandardCharsets.UTF_8));
                    return;
                }

                imageUrl = saveUploadedReturnImage(request, part, user.getUserId(), orderId);
            }
        } catch (Exception ex) {
            logger.warn("Could not process uploaded return image: {}", ex.getMessage());
        }

        try {
            OrderReturn orderReturn = orderReturnService.requestReturn(user.getUserId(), orderId, returnReason, resolutionType, comments, imageUrl);
            response.sendRedirect(request.getContextPath() + "/order?id=" + order.getOrderNumber() + "&returnSubmitted=true&returnNum=" + orderReturn.getReturnNumber());
        } catch (ValidationException ve) {
            String encoded = URLEncoder.encode(ve.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/order?id=" + order.getOrderNumber() + "&returnError=" + encoded);
        } catch (Exception e) {
            logger.error("Error submitting return request for order [{}]: {}", orderId, e.getMessage(), e);
            response.sendRedirect(request.getContextPath() + "/order?id=" + order.getOrderNumber() + "&returnError=Failed+to+submit+return+request");
        }
    }

    private String saveUploadedReturnImage(HttpServletRequest request, Part part, int userId, int orderId) {
        try {
            String submittedFileName = part.getSubmittedFileName();
            String extension = ".jpg";
            int dotIdx = submittedFileName.lastIndexOf('.');
            if (dotIdx > 0) {
                String ext = submittedFileName.substring(dotIdx).toLowerCase();
                if (ext.matches("\\.(jpg|jpeg|png|webp|gif)")) {
                    extension = ext;
                }
            }

            String fileName = "ret_u" + userId + "_ord" + orderId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + extension;
            
            String realPath = request.getServletContext().getRealPath("/uploads/returns");
            if (realPath != null) {
                File uploadDir = new File(realPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                File targetFile = new File(uploadDir, fileName);
                try (InputStream in = part.getInputStream()) {
                    Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                return request.getContextPath() + "/uploads/returns/" + fileName;
            }

            // Fallback to Base64 data URI
            try (InputStream in = part.getInputStream()) {
                byte[] bytes = in.readAllBytes();
                String mime = part.getContentType() != null ? part.getContentType() : "image/jpeg";
                return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            logger.error("Failed to save return image upload", e);
            return null;
        }
    }

    private boolean isValidImageMagicBytes(byte[] header) {
        if (header == null || header.length < 4) return false;
        // JPEG: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) return true;
        // PNG: 89 50 4E 47
        if ((header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50 && (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47) return true;
        // GIF: 47 49 46 38
        if ((header[0] & 0xFF) == 0x47 && (header[1] & 0xFF) == 0x49 && (header[2] & 0xFF) == 0x46 && (header[3] & 0xFF) == 0x38) return true;
        // WEBP: 52 49 46 46 ... 57 45 42 50
        if ((header[0] & 0xFF) == 0x52 && (header[1] & 0xFF) == 0x49 && (header[2] & 0xFF) == 0x46 && (header[3] & 0xFF) == 0x46) return true;
        return false;
    }
}

