package com.example.ecommerce.common.service;

import com.example.ecommerce.config.RedisManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * High-level caching service providing generic Jackson JSON serialization 
 * and transparent cache-aside pattern with Redis.
 */
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final CacheService INSTANCE = new CacheService();

    public static CacheService getInstance() {
        return INSTANCE;
    }

    /**
     * Executes the Cache-Aside pattern:
     * 1. If Redis has key, deserialize and return cached item.
     * 2. Otherwise, invoke dbLoader supplier, cache result in Redis for ttlSeconds, and return.
     */
    public <T> T getOrLoad(String key, int ttlSeconds, TypeReference<T> typeRef, Supplier<T> dbLoader) {
        if (RedisManager.isAvailable() && key != null) {
            String cachedJson = RedisManager.get(key);
            if (cachedJson != null && !cachedJson.trim().isEmpty()) {
                try {
                    return objectMapper.readValue(cachedJson, typeRef);
                } catch (Exception e) {
                    logger.debug("Failed to deserialize cached JSON for key [{}]: {}", key, e.getMessage());
                }
            }
        }

        // Database / Source Supplier fallback
        T result = dbLoader.get();

        // Write-through to cache if valid
        if (result != null && RedisManager.isAvailable() && key != null) {
            try {
                String json = objectMapper.writeValueAsString(result);
                RedisManager.setex(key, ttlSeconds, json);
            } catch (Exception e) {
                logger.debug("Failed to serialize object to Redis JSON for key [{}]: {}", key, e.getMessage());
            }
        }

        return result;
    }

    /**
     * Invalidate specific cache key.
     */
    public void evict(String key) {
        RedisManager.del(key);
    }

    /**
     * Invalidate all cache keys matching a namespace pattern (e.g. "shopkart:products:*").
     */
    public void evictPattern(String pattern) {
        RedisManager.delPattern(pattern);
    }
}
