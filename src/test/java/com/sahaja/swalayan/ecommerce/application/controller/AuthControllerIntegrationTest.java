package com.sahaja.swalayan.ecommerce.application.controller;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterRequest;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterResponse;
import com.sahaja.swalayan.ecommerce.application.dto.ConfirmResponse;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.domain.repository.ConfirmationTokenRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/auth";
    }

    @Test
    @DisplayName("Should successfully register a new user with valid data")
    void testSuccessfulUserRegistration() {
        // Arrange
        RegisterRequest request = createValidRegisterRequest();

        // Act
        ResponseEntity<ApiResponse<RegisterResponse>> response = restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, createJsonHeaders()),
                new ParameterizedTypeReference<ApiResponse<RegisterResponse>>() {
                });

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        ApiResponse<RegisterResponse> apiResponse = response.getBody();
        assertThat(apiResponse.isSuccess()).isTrue();
        assertThat(apiResponse.getMessage()).isEqualTo("Registration completed successfully");
        assertThat(apiResponse.getTimestamp()).isNotNull();

        RegisterResponse data = apiResponse.getData();
        assertThat(data).isNotNull();
        assertThat(data.getEmail()).isEqualTo(request.getEmail());
        assertThat(data.getMessage()).isEqualTo("Registration successful. Please check your email for confirmation.");
        assertThat(data.isRequiresConfirmation()).isTrue();

        // Verify user was created in database
        Optional<User> savedUser = userRepository.findByEmail(request.getEmail());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo(request.getName());
        assertThat(savedUser.get().getEmail()).isEqualTo(request.getEmail());
        assertThat(savedUser.get().getPhone()).isEqualTo(request.getPhone());
        assertThat(savedUser.get().getStatus()).isEqualTo(UserStatus.PENDING);

        // Verify confirmation token was created
        assertThat(confirmationTokenRepository.findValidTokenByUserId(savedUser.get().getId(), LocalDateTime.now()))
                .isPresent();

        // Delete user
        userRepository.deleteById(savedUser.get().getId());
    }

    @Test
    @DisplayName("Should return conflict when registering with existing email")
    void testRegistrationWithExistingEmail() {
        // Arrange - First create a user
        RegisterRequest firstRequest = createValidRegisterRequest();
        restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(firstRequest, createJsonHeaders()),
                new ParameterizedTypeReference<ApiResponse<RegisterResponse>>() {
                });

        // Try to register again with same email
        RegisterRequest duplicateRequest = createValidRegisterRequest();
        duplicateRequest.setEmail(firstRequest.getEmail()); // Same email

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(duplicateRequest, createJsonHeaders()),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("is already registered");

        // Delete user
        userRepository.findByEmail(firstRequest.getEmail()).ifPresent(user -> userRepository.deleteById(user.getId()));
    }

    @Test
    @DisplayName("Should return bad request for invalid email format")
    void testRegistrationWithInvalidEmail() {
        // Arrange
        RegisterRequest request = createValidRegisterRequest();
        request.setEmail("invalid-email-format");

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, createJsonHeaders()),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("must be a well-formed email address");
    }

    @Test
    @DisplayName("Should return bad request for blank name")
    void testRegistrationWithBlankName() {
        // Arrange
        RegisterRequest request = createValidRegisterRequest();
        request.setName("");

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, createJsonHeaders()),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("must not be blank");
    }

    @Test
    @DisplayName("Should return bad request for short password")
    void testRegistrationWithShortPassword() {
        // Arrange
        RegisterRequest request = createValidRegisterRequest();
        request.setPassword("short");

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, createJsonHeaders()),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("size must be between 8 and 100");
    }

    @Test
    @DisplayName("Should return bad request for invalid phone number")
    void testRegistrationWithInvalidPhone() {
        // Arrange
        RegisterRequest request = createValidRegisterRequest();
        request.setPhone("invalid-phone");

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, createJsonHeaders()),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid phone number");
    }

    @Test
    @DisplayName("Should return bad request for name that is too long")
    void testRegistrationWithTooLongName() {
        // Arrange
        RegisterRequest request = createValidRegisterRequest();
        request.setName("a".repeat(101)); // Exceeds max length of 100

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, createJsonHeaders()),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("size must be between 2 and 100");
    }

    @Test
    @DisplayName("Should return bad request for name that is too short")
    void testRegistrationWithTooShortName() {
        // Arrange
        RegisterRequest request = createValidRegisterRequest();
        request.setName("a"); // Below min length of 2

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, createJsonHeaders()),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("size must be between 2 and 100");
    }

    @Test
    @DisplayName("Should successfully confirm user with valid token")
    void testSuccessfulUserConfirmation() {
        // Arrange - First register a user
        RegisterRequest request = createValidRegisterRequest();
        restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, createJsonHeaders()),
                new ParameterizedTypeReference<ApiResponse<RegisterResponse>>() {
                });

        // Get the user and token
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = confirmationTokenRepository.findValidTokenByUserId(user.getId(), LocalDateTime.now())
                .orElseThrow().getToken();

        // Act
        ResponseEntity<ApiResponse<ConfirmResponse>> response = restTemplate.exchange(
                getBaseUrl() + "/confirm?token=" + token,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<ConfirmResponse>>() {
                });

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        ApiResponse<ConfirmResponse> apiResponse = response.getBody();
        assertThat(apiResponse.isSuccess()).isTrue();
        assertThat(apiResponse.getMessage()).isEqualTo("Email confirmation completed successfully");

        ConfirmResponse data = apiResponse.getData();
        assertThat(data).isNotNull();
        assertThat(data.isConfirmed()).isTrue();
        assertThat(data.getMessage()).isEqualTo("Email confirmed successfully. Your account is now active.");
        assertThat(data.getConfirmedAt()).isNotNull();

        // Verify user is now confirmed in database
        User confirmedUser = userRepository.findByEmail(request.getEmail()).orElseThrow();
        assertThat(confirmedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

        // Verify token was deleted
        assertThat(confirmationTokenRepository.findValidTokenByUserId(user.getId(), LocalDateTime.now())).isEmpty();

        // Delete user
        userRepository.deleteById(user.getId());
    }

    @Test
    @DisplayName("Should return bad request for invalid confirmation token")
    void testConfirmationWithInvalidToken() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/confirm?token=invalid-token",
                HttpMethod.GET,
                null,
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid or expired confirmation token");
    }

    @Test
    @DisplayName("Should handle multiple validation errors in single request")
    void testRegistrationWithMultipleValidationErrors() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setName(""); // Invalid: blank
        request.setEmail("invalid-email"); // Invalid: format
        request.setPassword("short"); // Invalid: too short
        request.setPhone("invalid"); // Invalid: format

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, createJsonHeaders()),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String responseBody = response.getBody();
        assertThat(responseBody).contains("must not be blank");
        assertThat(responseBody).contains("must be a well-formed email address");
        assertThat(responseBody).contains("size must be between 8 and 100");
        assertThat(responseBody).contains("Invalid phone number");
    }

    private RegisterRequest createValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Azhar Khalid");
        request.setEmail("azhar.tkjzone@gmail.com");
        request.setPassword("securepassword123");
        request.setPhone("087788662113");
        return request;
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
