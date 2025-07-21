package com.sahaja.swalayan.ecommerce.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints (stateless authentication)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS (allow cross-origin requests)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management (stateless for API)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure security headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
                .referrerPolicy(referrerPolicy -> 
                    referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - Authentication/Registration
                .requestMatchers(HttpMethod.POST, "/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/auth/confirm").permitAll()
                
                // Public endpoints - Product and Category APIs
                .requestMatchers("/v1/products/**").permitAll()
                .requestMatchers("/v1/categories/**").permitAll()
                
                // Public endpoints - Health checks and monitoring
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // Public endpoints - API Documentation
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                // Public endpoints - Static resources (if any)
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/error").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(customAuthenticationEntryPoint())
                .accessDeniedHandler(customAccessDeniedHandler())
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (configure based on your frontend URLs)
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "https://localhost:*",
            "https://*.sahajaswalayan.com"
        ));
        
        // Allow specific HTTP methods
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allow specific headers
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = 
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(401);
            
            ApiResponse<Object> apiResponse = ApiResponse.error("Authentication required");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDateTime
            
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setStatus(403);
            
            ApiResponse<Object> apiResponse = ApiResponse.error("Access denied");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDateTime
            
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        };
    }
}
