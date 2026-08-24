package com.example.ecommerce.seo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Handles Google Search Console verification HTML file requests dynamically.
 * Mapped to /google*.html so ANY Google verification file is verified instantly.
 */
@WebServlet(name = "GoogleVerificationServlet", urlPatterns = {
    "/google33c7bac26ade82d4.html",
    "/google5c0320ea15284d10.html",
    "/googlepzfFJyk1bR6DXUw00hc_ffbD2amT6dgRCta6yGsyhKM.html"
})
public class GoogleVerificationServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String filename = uri.substring(uri.lastIndexOf('/') + 1);
        
        response.setContentType("text/html; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("google-site-verification: " + filename + "\n");
    }
}
