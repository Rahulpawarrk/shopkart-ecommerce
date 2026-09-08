package com.example.ecommerce.api.dto;

import java.util.ArrayList;
import java.util.List;

public class HomeShowcaseResponse {
    private List<CategoryDto> categories = new ArrayList<>();
    private List<ProductDto> featuredProducts = new ArrayList<>();
    private List<ProductDto> hotDeals = new ArrayList<>();
    private List<ProductDto> techQuad = new ArrayList<>();
    private List<ProductDto> audioQuad = new ArrayList<>();
    private List<ProductDto> fashionQuad = new ArrayList<>();
    private List<ProductDto> lifestyleQuad = new ArrayList<>();
    private List<ProductDto> bestSellers = new ArrayList<>();

    public HomeShowcaseResponse() {}

    public List<CategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDto> categories) {
        this.categories = categories;
    }

    public List<ProductDto> getFeaturedProducts() {
        return featuredProducts;
    }

    public void setFeaturedProducts(List<ProductDto> featuredProducts) {
        this.featuredProducts = featuredProducts;
    }

    public List<ProductDto> getHotDeals() {
        return hotDeals;
    }

    public void setHotDeals(List<ProductDto> hotDeals) {
        this.hotDeals = hotDeals;
    }

    public List<ProductDto> getTechQuad() {
        return techQuad;
    }

    public void setTechQuad(List<ProductDto> techQuad) {
        this.techQuad = techQuad;
    }

    public List<ProductDto> getAudioQuad() {
        return audioQuad;
    }

    public void setAudioQuad(List<ProductDto> audioQuad) {
        this.audioQuad = audioQuad;
    }

    public List<ProductDto> getFashionQuad() {
        return fashionQuad;
    }

    public void setFashionQuad(List<ProductDto> fashionQuad) {
        this.fashionQuad = fashionQuad;
    }

    public List<ProductDto> getLifestyleQuad() {
        return lifestyleQuad;
    }

    public void setLifestyleQuad(List<ProductDto> lifestyleQuad) {
        this.lifestyleQuad = lifestyleQuad;
    }

    public List<ProductDto> getBestSellers() {
        return bestSellers;
    }

    public void setBestSellers(List<ProductDto> bestSellers) {
        this.bestSellers = bestSellers;
    }
}
