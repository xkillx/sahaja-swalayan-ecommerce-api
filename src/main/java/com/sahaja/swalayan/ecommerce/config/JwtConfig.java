package com.sahaja.swalayan.ecommerce.config;

import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret:mySecretKey12345678901234567890123456789012}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 1 day in ms
    private long jwtExpirationMs;

    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil(jwtSecret, jwtExpirationMs);
    }
}
