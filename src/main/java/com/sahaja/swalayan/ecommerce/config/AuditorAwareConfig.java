package com.sahaja.swalayan.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditorAwareConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        // Replace this with your actual user lookup logic, e.g., from Spring Security
        return () -> Optional.of("system");
    }
}
