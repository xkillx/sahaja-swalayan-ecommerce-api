package com.sahaja.swalayan.ecommerce.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_AREAS = "shipping:areas";
    public static final String CACHE_COURIERS = "shipping:couriers";
    public static final String CACHE_CANCELLATION_REASONS = "shipping:cancellationReasons";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCache areasCache = new CaffeineCache(
                CACHE_AREAS,
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(5_000)
                        .build());
        CaffeineCache couriersCache = new CaffeineCache(
                CACHE_COURIERS,
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofHours(4))
                        .maximumSize(1_000)
                        .build());
        CaffeineCache cancellationReasonsCache = new CaffeineCache(
                CACHE_CANCELLATION_REASONS,
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofHours(24))
                        .maximumSize(100)
                        .build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(Arrays.asList(areasCache, couriersCache, cancellationReasonsCache));
        manager.initializeCaches();
        log.debug("Initialized Caffeine caches with TTLs: areas=10m, couriers=4h, cancellationReasons=24h");
        return manager;
    }
}
