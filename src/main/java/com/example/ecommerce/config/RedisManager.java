package com.example.ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Thread-safe Redis connection pool and caching manager for ShopKart.
 * Supports cloud Redis URLs (Upstash, Render, Redis Cloud, AWS ElastiCache) 
 * as well as localhost with graceful degradation if Redis is offline.
 */
public class RedisManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisManager.class);
    private static volatile JedisPool pool;
    private static volatile boolean available = false;
    private static final Object lock = new Object();

    static {
        initPool();
    }

    private RedisManager() {}

    /**
     * Initializes the Jedis connection pool safely.
     */
    public static void initPool() {
        synchronized (lock) {
            if (pool != null && !pool.isClosed()) {
                return;
            }

            try {
                JedisPoolConfig poolConfig = new JedisPoolConfig();
                poolConfig.setMaxTotal(30);
                poolConfig.setMaxIdle(10);
                poolConfig.setMinIdle(2);
                poolConfig.setTestOnBorrow(true);
                poolConfig.setTestWhileIdle(true);
                poolConfig.setMinEvictableIdleDuration(Duration.ofSeconds(60));
                poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
                poolConfig.setBlockWhenExhausted(true);
                poolConfig.setMaxWait(Duration.ofMillis(2000));

                String redisUrl = System.getenv("REDIS_URL");
                if (redisUrl != null && !redisUrl.trim().isEmpty()) {
                    redisUrl = redisUrl.trim();
                    logger.info("Initializing Redis connection from REDIS_URL environment variable...");
                    URI uri = URI.create(redisUrl);
                    pool = new JedisPool(poolConfig, uri, 3000);
                } else {
                    String host = System.getenv("REDIS_HOST");
                    if (host == null || host.trim().isEmpty()) {
                        host = "localhost";
                    }
                    int port = 6379;
                    String portStr = System.getenv("REDIS_PORT");
                    if (portStr != null && !portStr.trim().isEmpty()) {
                        try {
                            port = Integer.parseInt(portStr.trim());
                        } catch (NumberFormatException ignored) {}
                    }
                    String password = System.getenv("REDIS_PASSWORD");
                    if (password != null && password.trim().isEmpty()) {
                        password = null;
                    }

                    JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                            .connectionTimeoutMillis(2000)
                            .socketTimeoutMillis(2000)
                            .password(password)
                            .build();

                    pool = new JedisPool(poolConfig, new HostAndPort(host, port), clientConfig);
                }

                // Verify connectivity with PING (quick 1.5s timeout)
                try (Jedis jedis = pool.getResource()) {
                    String pong = jedis.ping();
                    if ("PONG".equalsIgnoreCase(pong)) {
                        available = true;
                        logger.info("Redis cache service connected successfully (PING -> PONG).");
                    }
                }
            } catch (Exception e) {
                available = false;
                logger.warn("Redis is not available ({}: {}). Application will operate normally with direct database queries.", 
                        e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * Checks if Redis cache is online and reachable.
     */
    public static boolean isAvailable() {
        return available && pool != null && !pool.isClosed();
    }

    /**
     * Gets a string value from Redis.
     */
    public static String get(String key) {
        if (!isAvailable() || key == null) return null;
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            logger.debug("Redis GET failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Sets a string value with TTL in seconds.
     */
    public static void setex(String key, int ttlSeconds, String value) {
        if (!isAvailable() || key == null || value == null) return;
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(key, ttlSeconds, value);
        } catch (Exception e) {
            logger.debug("Redis SETEX failed for key {}: {}", key, e.getMessage());
        }
    }

    /**
     * Deletes a key from Redis.
     */
    public static void del(String... keys) {
        if (!isAvailable() || keys == null || keys.length == 0) return;
        try (Jedis jedis = pool.getResource()) {
            jedis.del(keys);
        } catch (Exception e) {
            logger.debug("Redis DEL failed: {}", e.getMessage());
        }
    }

    /**
     * Deletes all keys matching a prefix/pattern safely using SCAN.
     */
    public static void delPattern(String pattern) {
        if (!isAvailable() || pattern == null) return;
        try (Jedis jedis = pool.getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams scanParams = new ScanParams().match(pattern).count(100);
            Set<String> matchingKeys = new HashSet<>();

            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                List<String> result = scanResult.getResult();
                if (result != null && !result.isEmpty()) {
                    matchingKeys.addAll(result);
                }
                cursor = scanResult.getCursor();
            } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

            if (!matchingKeys.isEmpty()) {
                jedis.del(matchingKeys.toArray(new String[0]));
                logger.debug("Invalidated {} cached Redis keys matching pattern [{}]", matchingKeys.size(), pattern);
            }
        } catch (Exception e) {
            logger.debug("Redis delPattern failed for [{}]: {}", pattern, e.getMessage());
        }
    }

    /**
     * Gracefully shuts down connection pool during application termination.
     */
    public static void shutdown() {
        synchronized (lock) {
            if (pool != null && !pool.isClosed()) {
                logger.info("Closing Redis connection pool...");
                pool.close();
                pool = null;
                available = false;
            }
        }
    }
}
