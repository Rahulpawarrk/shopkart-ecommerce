package com.example.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Spring Web MVC Configuration:
 * 1. Configures CORS with credentials for local Vite frontend development.
 * 2. Configures SPA static resource routing so client-side React routes forward to index.html.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        java.util.List<String> origins = new java.util.ArrayList<>();
        origins.add("http://localhost:[*]");
        origins.add("http://localhost:*");
        origins.add("http://127.0.0.1:[*]");
        origins.add("http://127.0.0.1:*");
        origins.add("https://*.onrender.com");

        String customOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (customOrigins == null || customOrigins.trim().isEmpty()) {
            customOrigins = System.getProperty("cors.allowed.origins");
        }
        if (customOrigins != null && !customOrigins.trim().isEmpty()) {
            for (String origin : customOrigins.split(",")) {
                if (!origin.trim().isEmpty()) {
                    origins.add(origin.trim());
                }
            }
        }

        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Content-Type", "X-Requested-With", "X-CSRF-TOKEN", "X-XSRF-TOKEN", "Authorization", "Accept")
                .exposedHeaders("X-CSRF-TOKEN", "X-XSRF-TOKEN")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Serve static React build files
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);

                        // If the resource exists and is readable, return it (e.g. css, js, svg, png)
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // Do not route API, asset, or JSP paths to index.html
                        if (resourcePath.startsWith("api") || resourcePath.startsWith("assets") || resourcePath.startsWith("WEB-INF")) {
                            return null;
                        }

                        // For all other React SPA routes (e.g. /cart, /checkout, /products), fallback to index.html
                        Resource index = location.createRelative("index.html");
                        if (index.exists() && index.isReadable()) {
                            return index;
                        }

                        return null;
                    }
                });
    }
}
