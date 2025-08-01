package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahaja.swalayan.ecommerce.application.dto.OrderRequest;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.domain.repository.*;
import com.sahaja.swalayan.ecommerce.domain.repository.cart.CartRepository;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JwtTokenUtil jwtTokenUtil;
    @Autowired private CartRepository cartRepository;
    @Autowired private OrderRepository orderRepository;

    private String jwtToken;
    private User user;
    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        String password = "password";
        String passwordHash = passwordEncoder.encode(password);

        user = User.builder()
                .name("Order User")
                .email("orderuser@example.com")
                .phone("1234567890")
                .passwordHash(passwordHash)
                .status(UserStatus.ACTIVE)
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);

        category = Category.builder().name("OrderCat").build();
        categoryRepository.save(category);

        product = Product.builder()
                .name("OrderProduct")
                .stock(10)
                .price(new BigDecimal(20000))
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
        // Delete all orders for this user (cascades to order_items)
        orderRepository.findByUserId(user.getId())
            .forEach(order -> orderRepository.deleteById(order.getId()));
        cartRepository.findByUserId(user.getId()).ifPresent(cartRepository::delete);
        productRepository.delete(product);
        categoryRepository.delete(category);
        userRepository.delete(user);
    }

    private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + jwtToken);
    }

    @Test
    void createOrderFromCart_success() throws Exception {
        // Add product to cart first
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":2}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        // Create order from cart
        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Test 123")
                .paymentMethod("CREDIT_CARD")
                .build();
        mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items[0].productId").value(product.getId().toString()))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void getOrderById_success() throws Exception {
        // Add product to cart and create order
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":1}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Test 456")
                .paymentMethod("CREDIT_CARD")
                .build();
        String orderJson = mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract orderId from response
        String orderId = objectMapper.readTree(orderJson).path("data").path("id").asText();

        // Fetch order by id
        mockMvc.perform(authenticated(get("/v1/orders/" + orderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    void cancelOrder_success() throws Exception {
        // Add product to cart and create order
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":1}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addToCartJson))
                .andExpect(status().isOk());

        OrderRequest orderRequest = OrderRequest.builder()
                .shippingAddress("Jl. Test 789")
                .paymentMethod("CREDIT_CARD")
                .build();
        String orderJson = mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderJson).path("data").path("id").asText();

        // Cancel order
        mockMvc.perform(authenticated(post("/v1/orders/" + orderId + "/cancel")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
