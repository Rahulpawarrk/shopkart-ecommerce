package com.example.ecommerce.home.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeControllerTest {

    @Test
    @DisplayName("Root URL GET / forwards to /index.html")
    void testRootMapping() {
        HomeController controller = new HomeController();
        String target = controller.home();
        assertEquals("forward:/index.html", target);
    }
}
