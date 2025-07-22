package com.sahaja.swalayan.ecommerce.common;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class JwtTokenUtilTest {
    private static final String SECRET = "testsecretkeyforjwt12345678901234567890123456789012"; // 48+ chars for HS256
    private static final long EXPIRATION_MS = 3600000; // 1 hour
    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil(SECRET, EXPIRATION_MS);
    }

    @Test
    void generateAndValidateToken_basic() {
        String username = "testuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "USER");
        String token = jwtTokenUtil.generateToken(username, claims);

        assertThat(token).isNotNull();
        assertThat(jwtTokenUtil.validateToken(token)).isTrue();
        assertThat(jwtTokenUtil.getUsernameFromToken(token)).isEqualTo(username);
    }

    @Test
    void generateToken_withCustomClaims() {
        String username = "anotheruser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "ADMIN");
        claims.put("custom", 123);
        String token = jwtTokenUtil.generateToken(username, claims);

        Claims parsedClaims = jwtTokenUtil.getAllClaimsFromToken(token);
        assertThat(parsedClaims.get("roles")).isEqualTo("ADMIN");
        assertThat(parsedClaims.get("custom")).isEqualTo(123);
    }

    @Test
    void validateToken_invalidToken() {
        String invalidToken = "invalid.jwt.token";
        assertThat(jwtTokenUtil.validateToken(invalidToken)).isFalse();
    }

    @Test
    void getClaimFromToken_extractClaim() {
        String username = "claimuser";
        Map<String, Object> claims = Collections.singletonMap("roles", "USER");
        String token = jwtTokenUtil.generateToken(username, claims);
        String roles = jwtTokenUtil.getClaimFromToken(token, c -> (String) c.get("roles"));
        assertThat(roles).isEqualTo("USER");
    }
}
