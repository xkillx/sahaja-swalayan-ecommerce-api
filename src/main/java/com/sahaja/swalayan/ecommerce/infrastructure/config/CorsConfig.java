package com.sahaja.swalayan.ecommerce.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:}")
    private String allowedOriginsProp;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // Parse comma-separated origins from env/property CORS_ALLOWED_ORIGINS or cors.allowed-origins
        String fromEnv = System.getenv("CORS_ALLOWED_ORIGINS");
        String raw = (fromEnv != null && !fromEnv.isBlank()) ? fromEnv : allowedOriginsProp;
        if (raw != null && !raw.isBlank()) {
            List<String> origins = Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            config.setAllowedOrigins(origins);
        } else {
            // Default local dev
            config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        }

        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        // Expose minimal headers; SSE doesn't require special headers here
        config.setExposedHeaders(List.of("Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to API endpoints
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
