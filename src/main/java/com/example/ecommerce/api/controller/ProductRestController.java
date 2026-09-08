package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.ApiResponse;
import com.example.ecommerce.api.dto.CatalogPageResponse;
import com.example.ecommerce.api.dto.ProductDto;
import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for catalog browsing, multi-faceted filtering, sorting, pagination, and product details.
 */
@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public ProductRestController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CatalogPageResponse<ProductDto>>> listProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minDiscount,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "16") int pageSize
    ) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setStatus("ACTIVE");

        String kw = (q != null && !q.trim().isEmpty()) ? q.trim() : (keyword != null ? keyword.trim() : null);
        criteria.setKeyword(kw);

        if (categoryId != null && categoryId > 0) {
            criteria.setCategoryId(categoryId);
        } else if (category != null && !category.trim().isEmpty()) {
            try {
                int catId = Integer.parseInt(category.trim());
                criteria.setCategoryId(catId);
            } catch (NumberFormatException e) {
                try {
                    Category cat = categoryService.getCategoryBySlug(category.trim());
                    if (cat != null) {
                        criteria.setCategoryId(cat.getCategoryId());
                    }
                } catch (Exception ignored) {}
            }
        }

        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setBrand(brand);
        criteria.setMinDiscount(minDiscount);
        criteria.setSortBy(sort);
        criteria.setPage(Math.max(1, page));
        criteria.setPageSize(Math.max(1, Math.min(60, pageSize)));

        Pagination<Product> rawPagination = productService.searchCatalog(criteria);

        List<ProductDto> dtoList = rawPagination.getItems().stream()
                .filter(p -> inStockOnly == null || !inStockOnly || p.isInStock())
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());

        Pagination<ProductDto> dtoPagination = new Pagination<>(
                dtoList,
                rawPagination.getCurrentPage(),
                rawPagination.getPageSize(),
                rawPagination.getTotalItems()
        );

        return ResponseEntity.ok(ApiResponse.ok(CatalogPageResponse.from(dtoPagination)));
    }

    @GetMapping("/{slugOrId}")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable String slugOrId) {
        Product product = null;
        if (slugOrId.matches("\\d+")) {
            try {
                product = productService.getProductById(Integer.parseInt(slugOrId));
            } catch (Exception ignored) {}
        }
        if (product == null) {
            try {
                product = productService.getProductBySlug(slugOrId);
            } catch (Exception ignored) {}
        }

        if (product == null) {
            throw new ResourceNotFoundException("Product not found: " + slugOrId);
        }

        return ResponseEntity.ok(ApiResponse.ok(ProductDto.fromEntity(product)));
    }

    @GetMapping("/{productId}/related")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getRelatedProducts(
            @PathVariable int productId,
            @RequestParam(required = false, defaultValue = "4") int limit
    ) {
        Product current = productService.getProductById(productId);
        if (current == null) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setStatus("ACTIVE");
        criteria.setCategoryId(current.getCategoryId());
        criteria.setPage(1);
        criteria.setPageSize(limit + 1);

        Pagination<Product> result = productService.searchCatalog(criteria);
        List<ProductDto> related = result.getItems().stream()
                .filter(p -> p.getProductId() != productId)
                .limit(limit)
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(related));
    }

    @GetMapping("/search/suggestions")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getSearchSuggestions(
            @RequestParam String q,
            @RequestParam(required = false) Integer categoryId
    ) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok(List.of()));
        }

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setStatus("ACTIVE");
        criteria.setKeyword(q.trim());
        if (categoryId != null && categoryId > 0) {
            criteria.setCategoryId(categoryId);
        }
        criteria.setPage(1);
        criteria.setPageSize(6);

        Pagination<Product> result = productService.searchCatalog(criteria);
        List<ProductDto> suggestions = result.getItems().stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(suggestions));
    }
}
