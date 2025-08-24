package com.sahaja.swalayan.ecommerce.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import org.springframework.context.annotation.Configuration;
import com.sahaja.swalayan.ecommerce.common.JwtAuthenticationFilter;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.common.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenUtil, customUserDetailsService());
    }

    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http
            // Disable CSRF for API endpoints (stateless authentication)
            .csrf(AbstractHttpConfigurer::disable)


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
                // Allow CORS preflight requests globally
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public endpoints - Authentication/Registration
                .requestMatchers(HttpMethod.POST, "/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/auth/confirm").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/google").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/firebase/exchange").permitAll()
                // Allow first-admin registration to hit controller; controller enforces restriction thereafter
                .requestMatchers(HttpMethod.POST, "/v1/auth/admin/register").permitAll()
                // Public store settings (no secrets)
                .requestMatchers(HttpMethod.GET, "/v1/store-settings").permitAll()
                
                // Product APIs - method-based access control
                .requestMatchers(HttpMethod.GET, "/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/v1/products/**").hasRole("ADMIN")

                // Category APIs - method-based access control
                .requestMatchers(HttpMethod.GET, "/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/v1/categories/**").hasRole("ADMIN")

                // Public endpoint for static product images
                .requestMatchers("/uploads/products/**").permitAll()

                // Public shipping helpers
                .requestMatchers(HttpMethod.GET, "/v1/shipping/couriers").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/shipping/areas").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/shipping/rates").permitAll()

                // Public coupon validate
                .requestMatchers(HttpMethod.POST, "/v1/coupons/validate").permitAll()
                
                // Public webhooks (validated by their own tokens inside controllers)
                .requestMatchers("/v1/payments/webhook").permitAll()
                .requestMatchers("/v1/shipping/webhook").permitAll()
                
                // Public endpoints - Health checks and documentation
                .requestMatchers("/v1/auth/register", "/v1/auth/confirm", "/v1/jwt/extract", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health", "/actuator/info", "/favicon.ico", "/error")
                    .permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Admin endpoints
                .requestMatchers("/v1/admin/**").hasRole("ADMIN")

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

    // Ensure a top-level CORS filter answers preflight with proper headers even before security kicks in
    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:3001"
        ));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization","Content-Type","X-Requested-With","Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        var bean = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }



    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(401);
            
            ApiResponse<Object> apiResponse = ApiResponse.error("Authentication required");
            
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
            
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        };
    }
}
