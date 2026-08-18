package com.example.ecommerce.product.service;

import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.product.dao.ProductDAO;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.util.Pagination;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests with Mockito")
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should return paginated search results from DAO")
    void testSearchCatalog() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setKeyword("Laptop");

        Product p = new Product();
        p.setProductId(1);
        p.setProductName("ProBook Ultra");
        p.setPrice(new BigDecimal("75000.00"));

        Pagination<Product> mockPagination = new Pagination<>(List.of(p), 1, 12, 1);
        when(productDAO.searchProducts(criteria)).thenReturn(mockPagination);

        Pagination<Product> result = productService.searchCatalog(criteria);

        assertNotNull(result);
        assertEquals(1, result.getTotalItems());
        assertEquals("ProBook Ultra", result.getItems().get(0).getProductName());
    }

    @Test
    @DisplayName("Should correctly calculate discount amount and discounted price")
    void testProductDiscountCalculations() {
        Product p = new Product();
        p.setPrice(new BigDecimal("10000.00"));
        p.setDiscountPercentage(new BigDecimal("15.00"));

        assertEquals(new BigDecimal("1500.00"), p.getDiscountAmount());
        assertEquals(new BigDecimal("8500.00"), p.getDiscountedPrice());
    }

    @Test
    @DisplayName("Should reject product creation with duplicate SKU")
    void testCreateProductDuplicateSku() {
        Product p = new Product();
        p.setProductName("Test Phone");
        p.setSku("PROD-DUP-001");
        p.setCategoryId(2);
        p.setPrice(new BigDecimal("25000.00"));

        when(productDAO.existsBySku("PROD-DUP-001", null)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> 
            productService.createProduct(p, null, 10, 2)
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should reject product creation with negative price")
    void testCreateProductNegativePrice() {
        Product p = new Product();
        p.setProductName("Invalid Item");
        p.setSku("PROD-INV-002");
        p.setCategoryId(1);
        p.setPrice(new BigDecimal("-10.00"));

        assertThrows(ValidationException.class, () -> 
            productService.createProduct(p, null, 10, 2)
        );
    }

    @Test
    @DisplayName("Should update status to INACTIVE on deactivation")
    void testDeactivateProduct() {
        productService.deactivateProduct(5);
        verify(productDAO, times(1)).updateStatus(5, "INACTIVE");
    }

    @Test
    @DisplayName("Should retrieve distinct brands by category from DAO")
    void testGetDistinctBrands() {
        when(productDAO.findDistinctBrandsByCategory(2)).thenReturn(List.of("Apple", "Dell", "ASUS"));
        List<String> brands = productService.getDistinctBrands(2);
        assertNotNull(brands);
        assertEquals(3, brands.size());
        assertTrue(brands.contains("Apple"));
        verify(productDAO).findDistinctBrandsByCategory(2);
    }

    @Test
    @DisplayName("Should retrieve category brands map from DAO")
    void testGetCategoryBrandsMap() {
        java.util.Map<Integer, List<String>> mockMap = java.util.Map.of(2, List.of("Apple", "Dell"));
        when(productDAO.findCategoryBrandsMap()).thenReturn(mockMap);
        java.util.Map<Integer, List<String>> result = productService.getCategoryBrandsMap();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productDAO).findCategoryBrandsMap();
    }
}
