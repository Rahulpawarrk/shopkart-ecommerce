package com.example.ecommerce.filter;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Filter that ensures the user's active persistent database cart is loaded into the HTTP session
 * so that the header cart badge count accurately reflects all items previously added by the user.
 */
@WebFilter(filterName = "SessionCartFilter", urlPatterns = {"/*"})
public class SessionCartFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SessionCartFilter.class);
    private CartService cartService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.cartService = new CartService();
        logger.info("SessionCartFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getServletPath();

        // Skip static asset requests for performance
        if (path != null && (path.startsWith("/assets") || path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".svg") || path.endsWith(".jpg"))) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            UserSession user = (UserSession) session.getAttribute("currentUser");
            if (user != null && !user.isAdmin() && session.getAttribute("cart") == null) {
                try {
                    Cart cart = cartService.getCart(user.getUserId());
                    session.setAttribute("cart", cart);
                    logger.debug("Session cart initialized for user {} with {} items", user.getUserId(), cart.getTotalQuantity());
                } catch (Exception e) {
                    logger.warn("Could not load cart for user {}: {}", user.getUserId(), e.getMessage());
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("SessionCartFilter destroyed.");
    }
}
