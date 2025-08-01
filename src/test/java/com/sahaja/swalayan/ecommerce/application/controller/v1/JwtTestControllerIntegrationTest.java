package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtTestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void extractJwtClaims_withValidToken_returnsClaims() throws Exception {
        // Insert test user required for JWT authentication
        User testUser = User
                .builder()
                .name("Test User")
                .email("testuser@example.com")
                .passwordHash("dummy")
                .phone("+621234567890")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(testUser);

        // Arrange: create a JWT with custom claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "TESTER");
        claims.put("custom", "value123");
        String subject = "testuser@example.com";
        String token = jwtTokenUtil.generateToken(subject, claims);

        // Act & Assert
        mockMvc.perform(get("/v1/jwt/extract")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.role", is("TESTER")))
                .andExpect(jsonPath("$.data.custom", is("value123")))
                .andExpect(jsonPath("$.data.sub", is(subject)))
                .andExpect(jsonPath("$.message", containsString("JWT claims extracted")));

        // Clean up test user
        userRepository.delete(testUser);
    }

    @Test
    void extractJwtClaims_withMissingHeader_returnsError() throws Exception {
        mockMvc.perform(get("/v1/jwt/extract"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Missing or invalid Authorization header")));
    }

    @Test
    void extractJwtClaims_withInvalidToken_returnsError() throws Exception {
        String invalidJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.aW52YWxpZA";
        mockMvc.perform(get("/v1/jwt/extract")
                .header("Authorization", "Bearer " + invalidJwt))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Invalid JWT token")));
    }
}
