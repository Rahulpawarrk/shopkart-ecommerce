package com.example.ecommerce.api.dto;

import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.model.ProductImage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductDto {
    private int productId;
    private int categoryId;
    private String categoryName;
    private String categorySlug;
    private String sku;
    private String productName;
    private String slug;
    private String description;
    private String brand;
    private BigDecimal price;
    private BigDecimal discountPercentage;
    private BigDecimal effectivePrice;
    private BigDecimal taxPercentage;
    private BigDecimal weightKg;
    private String status;
    private int stockQuantity;
    private boolean inStock;
    private String primaryImageUrl;
    private List<ProductImageDto> images = new ArrayList<>();

    public ProductDto() {}

    public static ProductDto fromEntity(Product p) {
        if (p == null) return null;
        ProductDto dto = new ProductDto();
        dto.setProductId(p.getProductId());
        dto.setCategoryId(p.getCategoryId());
        if (p.getCategory() != null) {
            dto.setCategoryName(p.getCategory().getCategoryName());
            dto.setCategorySlug(p.getCategory().getSlug());
        }
        dto.setSku(p.getSku());
        dto.setProductName(p.getProductName());
        dto.setSlug(p.getSlug());
        dto.setDescription(p.getDescription());
        dto.setBrand(p.getBrand());
        dto.setPrice(p.getPrice());
        dto.setDiscountPercentage(p.getDiscountPercentage());
        dto.setEffectivePrice(p.getEffectivePrice());
        dto.setTaxPercentage(p.getTaxPercentage());
        dto.setWeightKg(p.getWeightKg());
        dto.setStatus(p.getStatus());
        dto.setStockQuantity(p.getStockQuantity());
        dto.setInStock(p.isInStock());
        dto.setPrimaryImageUrl(p.getPrimaryImageUrl());

        if (p.getImages() != null) {
            for (ProductImage img : p.getImages()) {
                dto.getImages().add(new ProductImageDto(
                        img.getImageId(),
                        img.getImageUrl(),
                        img.isPrimary(),
                        img.getDisplayOrder(),
                        img.getAltText()
                ));
            }
        }
        return dto;
    }

    public static class ProductImageDto {
        private int imageId;
        private String imageUrl;
        private boolean primary;
        private int displayOrder;
        private String altText;

        public ProductImageDto() {}

        public ProductImageDto(int imageId, String imageUrl, boolean primary, int displayOrder, String altText) {
            this.imageId = imageId;
            this.imageUrl = imageUrl;
            this.primary = primary;
            this.displayOrder = displayOrder;
            this.altText = altText;
        }

        public int getImageId() {
            return imageId;
        }

        public void setImageId(int imageId) {
            this.imageId = imageId;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public int getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(int displayOrder) {
            this.displayOrder = displayOrder;
        }

        public String getAltText() {
            return altText;
        }

        public void setAltText(String altText) {
            this.altText = altText;
        }
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getEffectivePrice() {
        return effectivePrice;
    }

    public void setEffectivePrice(BigDecimal effectivePrice) {
        this.effectivePrice = effectivePrice;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public List<ProductImageDto> getImages() {
        return images;
    }

    public void setImages(List<ProductImageDto> images) {
        this.images = images;
    }
}
