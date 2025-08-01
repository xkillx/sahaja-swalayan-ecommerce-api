package com.sahaja.swalayan.ecommerce.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahaja.swalayan.ecommerce.application.dto.AddCartItemRequest;
import com.sahaja.swalayan.ecommerce.application.dto.UpdateCartItemRequest;
import com.sahaja.swalayan.ecommerce.domain.model.cart.Cart;
import com.sahaja.swalayan.ecommerce.domain.model.cart.CartItem;
import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.domain.repository.cart.CartRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.ProductRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.CategoryRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;

import java.util.UUID;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JwtTokenUtil jwtTokenUtil;

    private String jwtToken;

    private User user;
    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
         // Create password using bcrypt
         String password = "password";
         String passwordHash = passwordEncoder.encode(password);

        // Setup user
        user = User.builder()
                .name("User")
                .email("user@example.com")
                .phone("1234567890")
                .passwordHash(passwordHash)
                .status(UserStatus.ACTIVE)
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);

        // Setup category
        category = Category.builder()
                .name("Category1")
                .build();
        categoryRepository.save(category);

        // Setup product
        product = Product.builder()
                .name("Product1")
                .stock(10)
                .price(new BigDecimal(10000))
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
        userRepository.delete(user);
        productRepository.delete(product);
        categoryRepository.delete(category);
        var cart = cartRepository.findByUserId(user.getId());
        if (cart.isPresent()) {
            cartRepository.delete(cart.get());
        }
    }

    // Helper for authenticated requests
    private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + jwtToken);
    }

    @Test
    void addItemToCart_sufficientStock_success() throws Exception {
        AddCartItemRequest req = new AddCartItemRequest();
        req.setProductId(product.getId());
        req.setQuantity(2);
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    void addItemToCart_insufficientStock_error() throws Exception {
        AddCartItemRequest req = new AddCartItemRequest();
        req.setProductId(product.getId());
        req.setQuantity(20); // more than stock
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateCartItem_valid_success() throws Exception {
        // Add item first
        AddCartItemRequest addReq = new AddCartItemRequest();
        addReq.setProductId(product.getId());
        addReq.setQuantity(1);
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk());
        // Update
        CartItem cartItem = cartRepository.findByUserId(user.getId()).get().getItems().iterator().next();
        UpdateCartItemRequest updateReq = new UpdateCartItemRequest(3);
        mockMvc.perform(authenticated(put("/v1/cart/items/" + cartItem.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].quantity").value(3));
    }

    @Test
    void updateCartItem_invalidItem_error() throws Exception {
        UpdateCartItemRequest updateReq = new UpdateCartItemRequest(2);
        mockMvc.perform(authenticated(put("/v1/cart/items/" + UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void removeCartItem_success() throws Exception {
        // Add item first
        AddCartItemRequest addReq = new AddCartItemRequest();
        addReq.setProductId(product.getId());
        addReq.setQuantity(1);
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk());

        // get cart first
        Cart cart = cartRepository.findByUserId(user.getId()).get();
        CartItem cartItem = cart.getItems().iterator().next();
        mockMvc.perform(authenticated(delete("/v1/cart/items/" + cartItem.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty());
    }
}
