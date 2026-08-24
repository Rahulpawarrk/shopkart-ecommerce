package com.example.ecommerce.seo;

import com.example.ecommerce.seo.controller.RobotsServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RobotsServlet Unit Tests")
class RobotsServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    @DisplayName("Should write valid robots.txt with allowed public and blocked private routes and Sitemap directive")
    void testDoGetRobotsTxt() throws ServletException, IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        RobotsServlet servlet = new RobotsServlet();
        servlet.doGet(request, response);

        verify(response).setContentType("text/plain; charset=UTF-8");
        verify(response).setHeader("Cache-Control", "public, max-age=86400");

        String output = stringWriter.toString();
        assertTrue(output.contains("User-agent: *"));
        assertTrue(output.contains("Allow: /"));
        assertTrue(output.contains("Allow: /product/"));
        assertTrue(output.contains("Allow: /category/"));
        assertTrue(output.contains("Disallow: /admin/"));
        assertTrue(output.contains("Disallow: /cart"));
        assertTrue(output.contains("Disallow: /checkout"));
        assertTrue(output.contains("Disallow: /profile"));
        assertTrue(output.contains("Disallow: /orders"));
        assertTrue(output.contains("Sitemap: https://shopkart-ecommerce-1m2n.onrender.com/sitemap.xml"));
    }
}
