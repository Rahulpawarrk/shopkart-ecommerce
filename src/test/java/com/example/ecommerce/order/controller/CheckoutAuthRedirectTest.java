package com.example.ecommerce.order.controller;

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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unauthenticated Buy Now & Checkout Auth Flow Tests")
class CheckoutAuthRedirectTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("AuthFilter should capture buyNowProductId, store in session, and set redirectAfterLogin")
    void testAuthFilterCapturesBuyNowParams() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestURI()).thenReturn("/checkout");
        when(request.getQueryString()).thenReturn("buyNowProductId=15&quantity=2");
        when(request.getParameter("buyNowProductId")).thenReturn("15");
        when(request.getParameter("quantity")).thenReturn("2");
        when(request.getContextPath()).thenReturn("");

        AuthFilter filter = new AuthFilter();
        filter.doFilter(request, response, filterChain);

        verify(session).setAttribute(eq("directBuyProductId"), eq(15));
        verify(session).setAttribute(eq("directBuyQuantity"), eq(2));
        verify(session).setAttribute(eq("redirectAfterLogin"), eq("/checkout?buyNowProductId=15&quantity=2"));
        verify(response).sendRedirect("/auth/login");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("CheckoutServlet doPost captures buyNowProductId without login and redirects to /auth/login")
    void testCheckoutServletDoPostCapturesBuyNowWithoutLogin() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getParameter("buyNowProductId")).thenReturn("99");
        when(request.getParameter("quantity")).thenReturn("1");
        when(request.getContextPath()).thenReturn("");

        CheckoutServlet servlet = new CheckoutServlet();
        servlet.doPost(request, response);

        verify(session).setAttribute(eq("directBuyProductId"), eq(99));
        verify(session).setAttribute(eq("directBuyQuantity"), eq(1));
        verify(session).setAttribute(eq("redirectAfterLogin"), eq("/checkout?buyNowProductId=99&quantity=1"));
        verify(response).sendRedirect("/auth/login");
    }

    @Test
    @DisplayName("CheckoutServlet doGet captures buyNowProductId without login and redirects to /auth/login")
    void testCheckoutServletDoGetCapturesBuyNowWithoutLogin() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getParameter("buyNowProductId")).thenReturn("77");
        when(request.getParameter("quantity")).thenReturn("3");
        when(request.getContextPath()).thenReturn("");

        CheckoutServlet servlet = new CheckoutServlet();
        servlet.doGet(request, response);

        verify(session).setAttribute(eq("directBuyProductId"), eq(77));
        verify(session).setAttribute(eq("directBuyQuantity"), eq(3));
        verify(session).setAttribute(eq("redirectAfterLogin"), eq("/checkout?buyNowProductId=77&quantity=3"));
        verify(response).sendRedirect("/auth/login");
    }

    @Test
    @DisplayName("CheckoutServlet doPost on /checkout/address saves addressId and redirects to /checkout/summary even in direct buy")
    void testCheckoutServletDoPostAddressStepDirectBuySavesAddressAndRedirectsToSummary() throws ServletException, IOException {
        com.example.ecommerce.auth.model.UserSession user = new com.example.ecommerce.auth.model.UserSession(
                1, "test@example.com", "Rahul", "Pawar", java.util.Set.of("CUSTOMER"));

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(user);
        when(request.getServletPath()).thenReturn("/checkout/address");
        when(request.getParameter("addressId")).thenReturn("5");
        when(request.getParameter("buyNowProductId")).thenReturn("15");
        when(request.getParameter("quantity")).thenReturn("2");
        when(request.getContextPath()).thenReturn("");

        CheckoutServlet servlet = new CheckoutServlet();
        servlet.doPost(request, response);

        verify(session).setAttribute(eq("directBuyProductId"), eq(15));
        verify(session).setAttribute(eq("directBuyQuantity"), eq(2));
        verify(session).setAttribute(eq("checkoutAddressId"), eq(5));
        verify(response).sendRedirect("/checkout/summary");
        verify(response, never()).sendRedirect("/checkout/address");
    }

    @Test
    @DisplayName("CheckoutServlet doPost on /checkout/summary saves notes and redirects to /checkout/payment")
    void testCheckoutServletDoPostSummaryStepSavesNotesAndRedirectsToPayment() throws ServletException, IOException {
        com.example.ecommerce.auth.model.UserSession user = new com.example.ecommerce.auth.model.UserSession(
                1, "test@example.com", "Rahul", "Pawar", java.util.Set.of("CUSTOMER"));

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(user);
        when(request.getServletPath()).thenReturn("/checkout/summary");
        when(request.getParameter("buyNowProductId")).thenReturn(null);
        when(request.getParameter("notes")).thenReturn("Please ring the doorbell");
        when(request.getContextPath()).thenReturn("");

        CheckoutServlet servlet = new CheckoutServlet();
        servlet.doPost(request, response);

        verify(session).setAttribute(eq("checkoutNotes"), eq("Please ring the doorbell"));
        verify(response).sendRedirect("/checkout/payment");
    }
}
