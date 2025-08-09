package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierResponseDTO;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShippingControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    private String getBaseUrl() {
        // Context-path is '/api' in tests, following existing integration tests
        return "http://localhost:" + port + "/api/v1/shipping/couriers";
    }

    @BeforeEach
    void setUpUser() {
        testUser = User.builder()
                .name("Shipping Test User")
                .email("shipping.test.user@example.com")
                .passwordHash("dummy")
                .phone("+620000000000")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDownUser() {
        if (testUser != null && testUser.getId() != null) {
            userRepository.delete(testUser);
        }
    }

    @Test
    void getAvailableCouriers_returnsList() {
        // Ensure user persisted in DB (per test convention)
        assertThat(userRepository.findByEmail(testUser.getEmail())).isPresent();

        // Prepare auth header with valid JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CUSTOMER");
        String token = jwtTokenUtil.generateToken(testUser.getEmail(), claims);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // Act
        ResponseEntity<CourierResponseDTO> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CourierResponseDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        CourierResponseDTO body = Objects.requireNonNull(response.getBody());
        // Based on Biteship API: success flag and object should be 'courier'
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getObject()).isNotBlank();
        assertThat(body.getObject()).isEqualToIgnoringCase("courier");
        // General structure assertions
        assertThat(body.getCouriers()).isNotNull();
        assertThat(body.getCouriers()).isNotEmpty();
        // Check essential fields on first courier
        assertThat(body.getCouriers().get(0).getCourierName()).isNotBlank();
        assertThat(body.getCouriers().get(0).getCourierCode()).isNotBlank();
        assertThat(body.getCouriers().get(0).getCourierServiceName()).isNotBlank();
        assertThat(body.getCouriers().get(0).getCourierServiceCode()).isNotBlank();
    }
}
