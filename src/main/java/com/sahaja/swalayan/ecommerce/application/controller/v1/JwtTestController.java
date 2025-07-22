package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/jwt")
@Tag(name = "JWT Test", description = "Endpoints for testing JWT extraction")
public class JwtTestController {
    private final JwtTokenUtil jwtTokenUtil;

    public JwtTestController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/extract")
    @Operation(summary = "Extract JWT claims from Authorization header")
    public ResponseEntity<ApiResponse<Map<String, Object>>> extractJwtClaims(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing or invalid Authorization header"));
        }
        String token = authHeader.substring(7);
        try {
            if (!jwtTokenUtil.validateToken(token)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid JWT token"));
            }
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);
            Map<String, Object> claimsMap = new HashMap<>();
            claims.forEach(claimsMap::put);
            return ResponseEntity.ok(ApiResponse.success("JWT claims extracted", claimsMap));
        } catch (io.jsonwebtoken.JwtException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Malformed or invalid JWT: " + ex.getMessage()));
        }
    }
}
