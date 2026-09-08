package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.ApiResponse;
import com.example.ecommerce.api.dto.CategoryDto;
import com.example.ecommerce.api.dto.HomeShowcaseResponse;
import com.example.ecommerce.api.dto.ProductDto;
import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for homepage showcase, featured collections, departments, and flash deals.
 */
@RestController
@RequestMapping("/api/home")
public class HomeRestController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public HomeRestController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/showcase")
    public ResponseEntity<ApiResponse<HomeShowcaseResponse>> getShowcase() {
        HomeShowcaseResponse response = new HomeShowcaseResponse();

        // 1. Category Tree
        List<Category> categoryTree = categoryService.getCategoryTree(true);
        response.setCategories(categoryTree.stream().map(CategoryDto::fromEntity).collect(Collectors.toList()));

        // 2. Catalog slice for sections
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setStatus("ACTIVE");
        criteria.setPage(1);
        criteria.setPageSize(40);
        Pagination<Product> catalog = productService.searchCatalog(criteria);
        List<Product> allProducts = catalog.getItems();

        // 3. Featured Products
        response.setFeaturedProducts(allProducts.stream()
                .limit(8)
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList()));

        // 4. Hot Flash Deals
        response.setHotDeals(allProducts.stream()
                .filter(p -> p.getDiscountPercentage() != null && p.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getDiscountPercentage().compareTo(a.getDiscountPercentage()))
                .limit(6)
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList()));

        // 5. Tech Quad
        response.setTechQuad(allProducts.stream()
                .filter(p -> {
                    String name = p.getProductName().toLowerCase();
                    String brand = p.getBrand() != null ? p.getBrand().toLowerCase() : "";
                    return name.contains("macbook") || name.contains("xps") || name.contains("rog") ||
                            name.contains("galaxy") || name.contains("iphone") || brand.contains("apple") ||
                            brand.contains("dell") || brand.contains("asus") || brand.contains("samsung");
                })
                .limit(4)
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList()));

        // 6. Audio Quad
        response.setAudioQuad(allProducts.stream()
                .filter(p -> {
                    String name = p.getProductName().toLowerCase();
                    return name.contains("sony") || name.contains("airpods") || name.contains("watch") ||
                            name.contains("headphone") || name.contains("audio");
                })
                .limit(4)
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList()));

        // 7. Fashion Quad
        response.setFashionQuad(allProducts.stream()
                .filter(p -> {
                    String name = p.getProductName().toLowerCase();
                    return name.contains("shirt") || name.contains("dress") || name.contains("jacket") ||
                            name.contains("leather") || name.contains("oxford") || name.contains("zimmermann") ||
                            name.contains("ralph");
                })
                .limit(4)
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList()));

        // 8. Lifestyle Quad
        response.setLifestyleQuad(allProducts.stream()
                .filter(p -> {
                    String name = p.getProductName().toLowerCase();
                    return name.contains("espresso") || name.contains("airfryer") || name.contains("nike") ||
                            name.contains("shoe") || name.contains("kitchen") || name.contains("coffee");
                })
                .limit(4)
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList()));

        // 9. Best Sellers
        List<Product> bestSellers = allProducts.stream().skip(2).limit(6).collect(Collectors.toList());
        response.setBestSellers((bestSellers.isEmpty() ? allProducts.stream().limit(6).collect(Collectors.toList()) : bestSellers)
                .stream().map(ProductDto::fromEntity).collect(Collectors.toList()));

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
