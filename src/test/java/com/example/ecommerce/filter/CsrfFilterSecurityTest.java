package com.example.ecommerce.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CsrfFilterSecurityTest {

    private CsrfFilter csrfFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        csrfFilter = new CsrfFilter();
    }

    @Test
    @DisplayName("GET request should pass without requiring CSRF token")
    void testGetRequestPassesWithoutToken() throws ServletException, IOException {
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("CSRF_TOKEN")).thenReturn("valid-token-123");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/products");
        when(request.getMethod()).thenReturn("GET");

        csrfFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("POST request with X-Requested-With header but NO valid CSRF token must be BLOCKED with 403")
    void testPostWithAjaxHeaderOnlyIsBlocked() throws ServletException, IOException {
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("CSRF_TOKEN")).thenReturn("server-session-csrf-token");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/orders/checkout");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        csrfFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(sw.toString().contains("CSRF_FORBIDDEN"));
    }

    @Test
    @DisplayName("POST request with valid X-CSRF-TOKEN header should PASS")
    void testPostWithValidCsrfTokenPasses() throws ServletException, IOException {
        String token = "server-session-csrf-token";
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("CSRF_TOKEN")).thenReturn(token);
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/orders/checkout");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-CSRF-Token")).thenReturn(token);

        csrfFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
