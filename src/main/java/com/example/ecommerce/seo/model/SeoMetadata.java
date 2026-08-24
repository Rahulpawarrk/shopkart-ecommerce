package com.example.ecommerce.seo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates dynamic SEO page metadata (Title, Description, Canonical URL, 
 * Robots directives, OpenGraph, Twitter Cards, Product extensions, and JSON-LD).
 */
public class SeoMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SITE_NAME = "ShopKart";
    public static final String BASE_URL = "https://shopkart-ecommerce-1m2n.onrender.com";
    public static final String DEFAULT_IMAGE = BASE_URL + "/assets/images/logo.svg";

    private String title;
    private String description;
    private String canonicalUrl;
    private String robots = "index, follow";
    private String ogType = "website"; // 'website', 'product', 'article'
    private String ogTitle;
    private String ogDescription;
    private String ogImage = DEFAULT_IMAGE;
    private String twitterCard = "summary_large_image";
    private String jsonLd;

    // Product OpenGraph specific
    private String productPriceAmount;
    private String productPriceCurrency = "INR";
    private String productAvailability;

    // Breadcrumb trail for schema & navigation
    private List<BreadcrumbItem> breadcrumbs = new ArrayList<>();

    public SeoMetadata() {}

    public static class BreadcrumbItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String url;

        public BreadcrumbItem(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() { return name; }
        public String getUrl() { return url; }
    }

    public void addBreadcrumb(String name, String url) {
        if (breadcrumbs == null) breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem(name, url));
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCanonicalUrl() { return canonicalUrl; }
    public void setCanonicalUrl(String canonicalUrl) { this.canonicalUrl = canonicalUrl; }

    public String getRobots() { return robots; }
    public void setRobots(String robots) { this.robots = robots; }

    public String getOgType() { return ogType; }
    public void setOgType(String ogType) { this.ogType = ogType; }

    public String getOgTitle() { return ogTitle != null ? ogTitle : title; }
    public void setOgTitle(String ogTitle) { this.ogTitle = ogTitle; }

    public String getOgDescription() { return ogDescription != null ? ogDescription : description; }
    public void setOgDescription(String ogDescription) { this.ogDescription = ogDescription; }

    public String getOgImage() { return ogImage != null ? ogImage : DEFAULT_IMAGE; }
    public void setOgImage(String ogImage) { this.ogImage = ogImage; }

    public String getTwitterCard() { return twitterCard; }
    public void setTwitterCard(String twitterCard) { this.twitterCard = twitterCard; }

    public String getJsonLd() { return jsonLd; }
    public void setJsonLd(String jsonLd) { this.jsonLd = jsonLd; }

    public String getProductPriceAmount() { return productPriceAmount; }
    public void setProductPriceAmount(String productPriceAmount) { this.productPriceAmount = productPriceAmount; }

    public String getProductPriceCurrency() { return productPriceCurrency; }
    public void setProductPriceCurrency(String productPriceCurrency) { this.productPriceCurrency = productPriceCurrency; }

    public String getProductAvailability() { return productAvailability; }
    public void setProductAvailability(String productAvailability) { this.productAvailability = productAvailability; }

    public List<BreadcrumbItem> getBreadcrumbs() { return breadcrumbs; }
    public void setBreadcrumbs(List<BreadcrumbItem> breadcrumbs) { this.breadcrumbs = breadcrumbs; }
}
