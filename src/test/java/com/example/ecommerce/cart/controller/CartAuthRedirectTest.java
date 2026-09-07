package com.example.ecommerce.cart.controller;

import com.example.ecommerce.filter.AuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unauthenticated Add To Cart Auth Flow Tests")
class CartAuthRedirectTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("AuthFilter captures productId and quantity, sets pendingCartProductId, and redirects to /auth/login")
    void testAuthFilterCapturesAddToCartParams() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestURI()).thenReturn("/cart/add");
        when(request.getQueryString()).thenReturn("productId=42&quantity=2");
        when(request.getParameter("buyNowProductId")).thenReturn(null);
        when(request.getParameter("productId")).thenReturn("42");
        when(request.getParameter("quantity")).thenReturn("2");
        when(request.getContextPath()).thenReturn("");

        AuthFilter filter = new AuthFilter();
        filter.doFilter(request, response, filterChain);

        verify(session).setAttribute(eq("pendingCartProductId"), eq(42));
        verify(session).setAttribute(eq("pendingCartQuantity"), eq(2));
        verify(session).setAttribute(eq("authMessage"), eq("Please sign in to add items to your cart."));
        verify(session).setAttribute(eq("redirectAfterLogin"), eq("/cart?added=true"));
        verify(response).sendRedirect("/auth/login");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("CartServlet handleAdd captures productId without login and redirects to /auth/login")
    void testCartServletHandleAddWithoutLogin() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getServletPath()).thenReturn("/cart/add");
        when(request.getParameter("productId")).thenReturn("88");
        when(request.getParameter("quantity")).thenReturn("3");
        when(request.getContextPath()).thenReturn("");
        when(request.getHeader("X-Requested-With")).thenReturn(null);
        when(request.getParameter("ajax")).thenReturn(null);

        CartServlet servlet = new CartServlet();
        servlet.doPost(request, response);

        verify(session).setAttribute(eq("pendingCartProductId"), eq(88));
        verify(session).setAttribute(eq("pendingCartQuantity"), eq(3));
        verify(session).setAttribute(eq("authMessage"), eq("Please sign in to add items to your cart."));
        verify(session).setAttribute(eq("redirectAfterLogin"), eq("/cart?added=true"));
        verify(response).sendRedirect("/auth/login");
    }

    @Test
    @DisplayName("CartServlet handleAdd with AJAX returns 401 JSON redirect without login")
    void testCartServletHandleAddAjaxWithoutLogin() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getServletPath()).thenReturn("/cart/add");
        when(request.getParameter("productId")).thenReturn("12");
        when(request.getParameter("quantity")).thenReturn("1");
        when(request.getContextPath()).thenReturn("");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        CartServlet servlet = new CartServlet();
        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(session).setAttribute(eq("pendingCartProductId"), eq(12));
        verify(session).setAttribute(eq("pendingCartQuantity"), eq(1));
        assertTrue(out.toString().contains("/auth/login"));
    }
}
