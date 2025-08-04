package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahaja.swalayan.ecommerce.application.dto.OrderRequest;
import com.sahaja.swalayan.ecommerce.application.dto.PaymentRequest;
import com.sahaja.swalayan.ecommerce.application.dto.XenditWebhookPayload;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.model.order.Payment;
import com.sahaja.swalayan.ecommerce.domain.model.order.PaymentStatus;
import com.sahaja.swalayan.ecommerce.domain.model.order.PaymentMethod;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.domain.repository.CategoryRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.OrderRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.PaymentRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.ProductRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.cart.CartRepository;
import com.sahaja.swalayan.ecommerce.infrastructure.config.XenditProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private XenditProperties xenditProperties;

    private String jwtToken;
    private User user;
    private Product product;
    private Category category;
    private String validCallbackToken;

    @BeforeEach
    void setUp() {
        // Get the callback token for Xendit webhook validation
        validCallbackToken = xenditProperties.getCallbackToken();
        log.debug("Test setup - Using callback token: {}", validCallbackToken);

        // Create a test user
        String password = "password";
        String passwordHash = passwordEncoder.encode(password);

        user = User.builder()
                .name("Payment Test User")
                .email("paymenttestuser@example.com")
                .phone("1234567890")
                .passwordHash(passwordHash)
                .status(UserStatus.ACTIVE)
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);

        // Create test category
        category = Category.builder().name("PaymentTestCategory").build();
        categoryRepository.save(category);

        // Create test product
        product = Product.builder()
                .name("PaymentTestProduct")
                .quantity(10)
                .price(new BigDecimal("50000"))
                .weight(1000)
                .category(category)
                .build();
        productRepository.save(product);

        // Generate JWT token for this user
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        jwtToken = jwtTokenUtil.generateToken(user.getEmail(), claims);
    }

    @AfterEach
    void cleanUp() {
        // Clean up in reverse order of creation to respect dependencies
        cleanupPaymentsAndOrders();
        cleanupCartItems();
        cleanupProduct();
        cleanupCategory();
        cleanupUser(); // Always do user cleanup last
    }
    
    /**
     * Delete all payments and associated orders
     */
    private void cleanupPaymentsAndOrders() {
        if (user == null || user.getId() == null) return;
        
        // Get all orders for this user and clean up their payments
        orderRepository.findByUserId(user.getId()).forEach(order -> {
            // Delete all payments for this order
            paymentRepository.findByOrderId(order.getId())
                .forEach(payment -> deleteEntitySafely(
                    () -> paymentRepository.deleteById(payment.getId()),
                    "payment"));
            
            // Delete the order
            deleteEntitySafely(() -> orderRepository.deleteById(order.getId()), "order");
        });
    }
    
    /**
     * Delete cart items associated with test user
     */
    private void cleanupCartItems() {
        if (user == null || user.getId() == null) return;
        
        deleteEntitySafely(
            () -> cartRepository.findByUserId(user.getId()).ifPresent(cartRepository::delete),
            "cart");
    }
    
    /**
     * Delete test product
     */
    private void cleanupProduct() {
        if (product == null || product.getId() == null) return;
        
        deleteEntitySafely(() -> productRepository.delete(product), "product");
    }
    
    /**
     * Delete test category
     */
    private void cleanupCategory() {
        if (category == null || category.getId() == null) return;
        
        deleteEntitySafely(() -> categoryRepository.delete(category), "category");
    }
    
    /**
     * Delete test user
     */
    private void cleanupUser() {
        if (user == null || user.getId() == null) return;
        
        try {
            // Try direct deletion first
            userRepository.delete(user);
        } catch (Exception e) {
            log.warn("Failed direct user deletion, attempting by email: {}", e.getMessage());
            // If direct deletion fails, try to find by email and delete
            deleteEntitySafely(
                () -> userRepository.findByEmail("paymenttestuser@example.com")
                      .ifPresent(userRepository::delete),
                "user (by email)");
        }
    }
    
    /**
     * Helper method to safely delete an entity with error logging
     * 
     * @param deleteAction Runnable that performs the deletion
     * @param entityType String describing the type of entity for logging
     */
    private void deleteEntitySafely(Runnable deleteAction, String entityType) {
        try {
            deleteAction.run();
        } catch (Exception e) {
            log.warn("Error deleting {}: {}", entityType, e.getMessage());
        }
    }

    private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + jwtToken);
    }

    @Test
    @DisplayName("Should create payment successfully after creating an order")
    void testCreatePayment() throws Exception {
        log.debug("Running testCreatePayment");

        // First add product to cart
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":2}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        // Create order from cart
        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Payment Test 123")
                .paymentMethod("CREDIT_CARD")
                .build();
        String orderJson = mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract orderId from response
        String orderId = objectMapper.readTree(orderJson).path("data").path("id").asText();

        // Now create payment for the order
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(UUID.fromString(orderId))
                .amount(new BigDecimal("100000.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        mockMvc.perform(authenticated(post("/v1/payments"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.paymentStatus").value("PENDING"));

        assertThat(paymentRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Should fail to create payment with invalid body")
    void testCreatePaymentInvalid() throws Exception {
        log.debug("Running testCreatePaymentInvalid");
        // First create a valid order through the proper flow
        // Add product to cart
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":1}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        // Create order from cart
        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Payment Invalid Test 123")
                .paymentMethod("CREDIT_CARD")
                .build();
        mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk());

        // Try to create payment with missing orderId
        PaymentRequest invalidRequest = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        mockMvc.perform(authenticated(post("/v1/payments"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get payment by ID")
    void testGetPaymentById() throws Exception {
        log.debug("Running testGetPaymentById");
        // Create order and payment through the proper flow
        // Add product to cart
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":1}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        // Create order
        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Get Payment Test")
                .paymentMethod("CREDIT_CARD")
                .build();
        String orderJson = mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderJson).path("data").path("id").asText();

        // Create payment
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(UUID.fromString(orderId))
                .amount(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        String paymentJson = mockMvc.perform(authenticated(post("/v1/payments"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(paymentJson).path("data").path("paymentId").asText();

        // Now test getting the payment by ID
        mockMvc.perform(authenticated(get("/v1/payments/{id}", paymentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentId").value(paymentId));
    }

    @Test
    @DisplayName("Should return 404 for non-existent payment")
    void testGetPaymentByIdNotFound() throws Exception {
        log.debug("Running testGetPaymentByIdNotFound");
        mockMvc.perform(authenticated(get("/v1/payments/{id}", UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get payments by orderId")
    void testGetPaymentsByOrderId() throws Exception {
        log.debug("Running testGetPaymentsByOrderId");
        // Create order and payment through the proper flow
        // Add product to cart
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":3}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        // Create order
        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Get Payments By Order Test")
                .paymentMethod("BANK_TRANSFER")
                .build();
        String orderJson = mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderJson).path("data").path("id").asText();

        // Create payment
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(UUID.fromString(orderId))
                .amount(new BigDecimal("150000.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();

        mockMvc.perform(authenticated(post("/v1/payments"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated());

        // Get payments by orderId
        mockMvc.perform(authenticated(get("/v1/payments/order/{orderId}", orderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].orderId").value(orderId));
    }

    @Test
    @DisplayName("Should handle webhook with valid token")
    void testWebhookValidToken() throws Exception {
        log.debug("Running testWebhookValidToken with token: {}", validCallbackToken);

        // Create order and payment through the proper flow
        // Add product to cart
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":1}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        // Create order
        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Webhook Test")
                .paymentMethod("BANK_TRANSFER")
                .build();
        String orderJson = mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderJson).path("data").path("id").asText();

        // Create payment
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(UUID.fromString(orderId))
                .amount(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();

        String paymentJson = mockMvc.perform(authenticated(post("/v1/payments"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(paymentJson).path("data").path("paymentId").asText();
        String externalId = objectMapper.readTree(paymentJson).path("data").path("externalId").asText();

        // Now test the webhook - must use externalId, not internal paymentId
        XenditWebhookPayload payload = XenditWebhookPayload.builder()
                .externalId(externalId)
                .status("PAID")
                .build();

        // Ensure token is valid - in tests it might be null if property not loaded
        // correctly
        if (validCallbackToken == null || validCallbackToken.isBlank()) {
            validCallbackToken = "cb_token_livekey_9xsWcG5XEwLj2DcZxDq7P0vJxngE1bW28m6V1CulTzr0JVqP7V";
            log.debug("Using fallback token since actual token was null/empty");
        }
        log.debug("Webhook test - Using token: {}", validCallbackToken);

        mockMvc.perform(post("/v1/payments/webhook")
                .header("X-Callback-Token", validCallbackToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment webhook processed successfully"));

        Payment updated = paymentRepository.findById(UUID.fromString(paymentId)).orElseThrow();
        assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should reject webhook with missing token")
    void testWebhookMissingToken() throws Exception {
        log.debug("Running testWebhookMissingToken");
        // Create order and payment first
        // Add product to cart
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":1}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        // Create order
        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Missing Token Test")
                .paymentMethod("BANK_TRANSFER")
                .build();
        String orderJson = mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderJson).path("data").path("id").asText();

        // Create payment
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(UUID.fromString(orderId))
                .amount(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();

        String paymentJson = mockMvc.perform(authenticated(post("/v1/payments"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(paymentJson).path("data").path("paymentId").asText();

        // Test webhook with missing token
        XenditWebhookPayload payload = XenditWebhookPayload.builder()
                .externalId(paymentId)
                .status("PAID")
                .build();

        mockMvc.perform(post("/v1/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Missing X-Callback-Token")));
    }

    @Test
    @DisplayName("Should reject webhook with invalid token")
    void testWebhookInvalidToken() throws Exception {
        log.debug("Running testWebhookInvalidToken");
        // Create order and payment first
        // Add product to cart
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":1}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        // Create order
        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Invalid Token Test")
                .paymentMethod("BANK_TRANSFER")
                .build();
        String orderJson = mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderJson).path("data").path("id").asText();

        // Create payment
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(UUID.fromString(orderId))
                .amount(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();

        String paymentJson = mockMvc.perform(authenticated(post("/v1/payments"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(paymentJson).path("data").path("paymentId").asText();

        // Test webhook with invalid token
        XenditWebhookPayload payload = XenditWebhookPayload.builder()
                .externalId(paymentId)
                .status("PAID")
                .build();

        mockMvc.perform(post("/v1/payments/webhook")
                .header("X-Callback-Token", "wrong-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Invalid X-Callback-Token")));
    }

    @Test
    @DisplayName("Process Webhook - Bad Request with Invalid Payload")
    void processWebhook_InvalidPayload() throws Exception {
        log.debug("Running processWebhook_InvalidPayload");
        // Create invalid payload with null values
        XenditWebhookPayload invalidPayload = XenditWebhookPayload.builder()
                .build();

        // Execute with valid token but invalid payload
        mockMvc.perform(post("/v1/payments/webhook")
                .header("X-Callback-Token", validCallbackToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Invalid webhook payload")));
    }
}