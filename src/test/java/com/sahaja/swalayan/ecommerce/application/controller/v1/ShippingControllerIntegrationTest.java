package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.OrderItemDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.TrackingResponseDTO;
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

    private String getRatesUrl() {
        // Context-path is '/api' in tests
        return "http://localhost:" + port + "/api/v1/shipping/rates";
    }

    private String getOrdersUrl() {
        // Context-path is '/api' in tests
        return "http://localhost:" + port + "/api/v1/shipping/orders";
    }

    private String getTrackingUrl(String trackingId) {
        // Context-path is '/api' in tests
        return "http://localhost:" + port + "/api/v1/shipping/orders/" + trackingId;
    }

    private String getPublicTrackingUrl(String waybillId, String courierCode) {
        // Context-path is '/api' in tests
        return "http://localhost:" + port + "/api/v1/shipping/trackings/" + waybillId + "/couriers/" + courierCode;
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

    @Test
    void calculateRates_returnsPricing() {
        // Ensure user persisted in DB (per test convention)
        assertThat(userRepository.findByEmail(testUser.getEmail())).isPresent();

        // Prepare auth header with valid JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CUSTOMER");
        String token = jwtTokenUtil.generateToken(testUser.getEmail(), claims);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build request body based on Biteship docs example
        CourierRateRequestDTO request = CourierRateRequestDTO.builder()
                .originPostalCode("12440")
                .destinationPostalCode("12240")
                .couriers("anteraja,jne,sicepat")
                .item(CourierRateRequestDTO.ItemDTO.builder()
                        .name("Shoes")
                        .description("Black colored size 45")
                        .value(199000)
                        .length(30)
                        .width(15)
                        .height(20)
                        .weight(200)
                        .quantity(2)
                        .build())
                .build();

        // Act
        ResponseEntity<CourierRateResponseDTO> response = restTemplate.exchange(
                getRatesUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                CourierRateResponseDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        CourierRateResponseDTO body = Objects.requireNonNull(response.getBody());
        // General structure based on Biteship API
        assertThat(body.getOrigin()).isNotNull();
        assertThat(body.getDestination()).isNotNull();
        assertThat(body.getPricing()).isNotNull();
        assertThat(body.getPricing()).isNotEmpty();
        // Essential fields on first pricing item
        assertThat(body.getPricing().get(0).getCourierName()).isNotBlank();
        assertThat(body.getPricing().get(0).getCourierCode()).isNotBlank();
        assertThat(body.getPricing().get(0).getCourierServiceName()).isNotBlank();
        assertThat(body.getPricing().get(0).getCourierServiceCode()).isNotBlank();
        assertThat(body.getPricing().get(0).getPrice()).isNotNull();
    }

    @Test
    void createOrder_createsOrderSuccessfully() {
        // Ensure user persisted in DB (per test convention)
        assertThat(userRepository.findByEmail(testUser.getEmail())).isPresent();

        // Prepare auth header with valid JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CUSTOMER");
        String token = jwtTokenUtil.generateToken(testUser.getEmail(), claims);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build request body based on Biteship docs example (adapted to our DTO fields)
        CreateOrderRequestDTO request = CreateOrderRequestDTO.builder()
                .shipperContactName("Amir")
                .shipperContactPhone("088888888888")
                .shipperContactEmail("biteship@test.com")
                .shipperOrganization("Biteship Org Test")
                .originContactName("Amir")
                .originContactPhone("088888888888")
                .originAddress("Plaza Senayan, Jalan Asia Afrika, Jakarta")
                .originPostalCode("12440")
                .destinationContactName("John Doe")
                .destinationContactPhone("088888888888")
                .destinationContactEmail("jon@test.com")
                .destinationAddress("Lebak Bulus MRT, Jakarta")
                .destinationPostalCode("12950")
                .courierCompany("jne")
                .courierType("reg")
                .courierInsurance(165000)
                .deliveryType("now")
                .item(OrderItemDTO.builder()
                        .name("Black L")
                        .description("White Shirt")
                        .category("fashion")
                        .value(165000)
                        .quantity(1)
                        .height(10)
                        .length(10)
                        .weight(200)
                        .width(10)
                        .build())
                .build();

        // Act
        ResponseEntity<CreateOrderResponseDTO> response = restTemplate.exchange(
                getOrdersUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                CreateOrderResponseDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        CreateOrderResponseDTO body = Objects.requireNonNull(response.getBody());
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getObject()).isNotBlank();
        assertThat(body.getObject()).isEqualToIgnoringCase("order");
        assertThat(body.getId()).isNotBlank();
        assertThat(body.getDestination()).isNotNull();
        assertThat(body.getCourier()).isNotNull();
        assertThat(body.getItems()).isNotNull();
        assertThat(body.getItems()).isNotEmpty();
        assertThat(body.getCurrency()).isNotBlank();
        assertThat(body.getPrice()).isNotNull();
        assertThat(body.getStatus()).isNotBlank();
    }

    @Test
    void trackOrder_returnsTrackingDetails() {
        // Ensure user persisted in DB (per test convention)
        assertThat(userRepository.findByEmail(testUser.getEmail())).isPresent();

        // Prepare auth header with valid JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CUSTOMER");
        String token = jwtTokenUtil.generateToken(testUser.getEmail(), claims);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        // First, create an order to obtain a valid trackingId
        CreateOrderRequestDTO request = CreateOrderRequestDTO.builder()
                .shipperContactName("Amir")
                .shipperContactPhone("088888888888")
                .shipperContactEmail("biteship@test.com")
                .shipperOrganization("Biteship Org Test")
                .originContactName("Amir")
                .originContactPhone("088888888888")
                .originAddress("Plaza Senayan, Jalan Asia Afrika, Jakarta")
                .originPostalCode("12440")
                .destinationContactName("John Doe")
                .destinationContactPhone("088888888888")
                .destinationContactEmail("jon@test.com")
                .destinationAddress("Lebak Bulus MRT, Jakarta")
                .destinationPostalCode("12950")
                .courierCompany("jne")
                .courierType("reg")
                .courierInsurance(165000)
                .deliveryType("now")
                .item(OrderItemDTO.builder()
                        .name("Black L")
                        .description("White Shirt")
                        .category("fashion")
                        .value(165000)
                        .quantity(1)
                        .height(10)
                        .length(10)
                        .weight(200)
                        .width(10)
                        .build())
                .build();

        ResponseEntity<CreateOrderResponseDTO> createOrderResponse = restTemplate.exchange(
                getOrdersUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                CreateOrderResponseDTO.class
        );

        assertThat(createOrderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CreateOrderResponseDTO created = Objects.requireNonNull(createOrderResponse.getBody());
        assertThat(created.isSuccess()).isTrue();
        assertThat(created.getCourier()).isNotNull();
        assertThat(created.getCourier().getTrackingId()).isNotBlank();

        String trackingId = created.getCourier().getTrackingId();

        // Retrieve tracking details
        ResponseEntity<TrackingResponseDTO> trackingResponse = restTemplate.exchange(
                getTrackingUrl(trackingId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TrackingResponseDTO.class
        );

        assertThat(trackingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        TrackingResponseDTO tracking = Objects.requireNonNull(trackingResponse.getBody());
        assertThat(tracking.isSuccess()).isTrue();
        assertThat(tracking.getId()).isNotBlank();
        assertThat(tracking.getStatus()).isNotBlank();
        assertThat(tracking.getCourier()).isNotNull();
        assertThat(tracking.getOrigin()).isNotNull();
        assertThat(tracking.getDestination()).isNotNull();
        // history may be empty for brand-new orders, so just assert non-null list
        assertThat(tracking.getHistory()).isNotNull();
    }

    @Test
    void publicTracking_returnsTrackingDetails() {
        // Ensure user persisted in DB (per test convention)
        assertThat(userRepository.findByEmail(testUser.getEmail())).isPresent();

        // Prepare auth header with valid JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CUSTOMER");
        String token = jwtTokenUtil.generateToken(testUser.getEmail(), claims);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        // First, create an order to obtain a valid waybillId and courier code
        CreateOrderRequestDTO request = CreateOrderRequestDTO.builder()
                .shipperContactName("Amir")
                .shipperContactPhone("088888888888")
                .shipperContactEmail("biteship@test.com")
                .shipperOrganization("Biteship Org Test")
                .originContactName("Amir")
                .originContactPhone("088888888888")
                .originAddress("Plaza Senayan, Jalan Asia Afrika, Jakarta")
                .originPostalCode("12440")
                .destinationContactName("John Doe")
                .destinationContactPhone("088888888888")
                .destinationContactEmail("jon@test.com")
                .destinationAddress("Lebak Bulus MRT, Jakarta")
                .destinationPostalCode("12950")
                .courierCompany("jne")
                .courierType("reg")
                .courierInsurance(165000)
                .deliveryType("now")
                .item(OrderItemDTO.builder()
                        .name("Black L")
                        .description("White Shirt")
                        .category("fashion")
                        .value(165000)
                        .quantity(1)
                        .height(10)
                        .length(10)
                        .weight(200)
                        .width(10)
                        .build())
                .build();

        ResponseEntity<CreateOrderResponseDTO> createOrderResponse = restTemplate.exchange(
                getOrdersUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                CreateOrderResponseDTO.class
        );

        assertThat(createOrderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CreateOrderResponseDTO created = Objects.requireNonNull(createOrderResponse.getBody());
        assertThat(created.isSuccess()).isTrue();
        assertThat(created.getCourier()).isNotNull();
        assertThat(created.getCourier().getWaybillId()).isNotBlank();
        assertThat(created.getCourier().getCompany()).isNotBlank();

        String waybillId = created.getCourier().getWaybillId();
        String courierCode = created.getCourier().getCompany();

        // Retrieve public tracking details
        ResponseEntity<TrackingResponseDTO> trackingResponse = restTemplate.exchange(
                getPublicTrackingUrl(waybillId, courierCode),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TrackingResponseDTO.class
        );

        assertThat(trackingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        TrackingResponseDTO tracking = Objects.requireNonNull(trackingResponse.getBody());
        assertThat(tracking.isSuccess()).isTrue();
        assertThat(tracking.getId()).isNotBlank();
        assertThat(tracking.getStatus()).isNotBlank();
        assertThat(tracking.getCourier()).isNotNull();
        assertThat(tracking.getOrigin()).isNotNull();
        assertThat(tracking.getDestination()).isNotNull();
        assertThat(tracking.getHistory()).isNotNull();
    }
}
