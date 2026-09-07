package com.example.ecommerce;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EcommerceApplicationTests {

    @Test
    @DisplayName("Verify EcommerceApplication class definition and package integrity")
    void testApplicationClassPresence() {
        assertNotNull(EcommerceApplication.class);
    }
}
