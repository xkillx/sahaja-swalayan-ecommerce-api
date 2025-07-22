package com.sahaja.swalayan.ecommerce.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

/**
 * Utility class for generating and validating JWT tokens using io.jsonwebtoken
 * (JJWT).
 * - Generates JWT with subject, issued time, expiration, and custom claims
 * (e.g. roles)
 * - Validates token and extracts username
 * - Uses HS256 and a secret key
 * - Configurable expiration time
 */
public class JwtTokenUtil {
    private final SecretKey secretKey;
    private final long jwtExpirationMs;

    /**
     * Constructs JwtTokenUtil with a secret and expiration time in milliseconds.
     * 
     * @param secret          the secret key string
     * @param jwtExpirationMs expiration in milliseconds
     */
    public JwtTokenUtil(String secret, long jwtExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Generate a JWT token with subject and custom claims.
     * 
     * @param subject the username or user id
     * @param claims  additional claims (e.g. roles)
     * @return JWT token
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder() // new builder
                .claims(claims) // .setClaims → .claims
                .subject(subject) // .setSubject → .subject
                .issuedAt(now) // .setIssuedAt → .issuedAt
                .expiration(expiryDate) // .setExpiration → .expiration
                .signWith(secretKey) // .signWith(key, alg) → .signWith(key) with HS256 by default
                .compact();
    }

    /**
     * Validate the JWT token.
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser() // <-- no Builder anymore
                    .verifyWith(secretKey) // <-- replaces .setSigningKey(...)
                    .build() // <-- finalise the parser
                    .parseSignedClaims(token); // <-- replaces .parseClaimsJws(...)
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Extract username (subject) from token.
     * 
     * @param token JWT token
     * @return subject (username)
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract a custom claim from token.
     * 
     * @param <T>            type of claim
     * @param token          JWT token
     * @param claimsResolver function to extract claim
     * @return claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get all claims from token.
     * 
     * @param token JWT token
     * @return Claims
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
}
}
