package com.example.ecommerce.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Spring MVC Controller for root site endpoint ("/").
 * 
 * Forwards requests to HomeServlet ("/home") to populate catalog showcases,
 * flash deals, quads, and categories, which then renders index.jsp.
 */
@Controller
public class HomeController {

    @GetMapping(value = {
            "/",
            "/home",
            "/products",
            "/products/**",
            "/cart",
            "/checkout",
            "/login",
            "/register",
            "/forgot-password",
            "/reset-password",
            "/wishlist",
            "/order-confirmation/**",
            "/orders",
            "/orders/**",
            "/profile",
            "/addresses",
            "/admin",
            "/admin/**"
    })
    public String home() {
        return "forward:/index.html";
    }
}
