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
import com.sahaja.swalayan.ecommerce.domain.model.user.Address;
import com.sahaja.swalayan.ecommerce.domain.repository.user.AddressRepository;
import com.sahaja.swalayan.ecommerce.domain.model.order.Status;
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
import static org.assertj.core.api.Assertions.assertThat;

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
    @Autowired private AddressRepository addressRepository;

    private String jwtToken;
    private User user;
    private Product product;
    private Category category;
    private Address address;

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
                .quantity(10)
                .price(new BigDecimal(20000))
                .weight(1000)
                .category(category)
                .build();
        productRepository.save(product);

        // Create an address for the user
        address = Address.builder()
                .userId(user.getId())
                .label("Home")
                .contactName("Order User")
                .contactPhone("1234567890")
                .addressLine("Jl. Test 123")
                .postalCode("12345")
                .isDefault(true)
                .build();
        address = addressRepository.save(address);

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
        if (address != null && address.getId() != null) {
            addressRepository.deleteById(address.getId());
        }
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
                .addressId(address.getId())
                .shippingCourierCode("jne")
                .shippingCourierService("REG")
                .shippingCourierServiceName("JNE Regular")
                .shippingCost(new BigDecimal("15000"))
                .build();
        String responseJson = mockMvc.perform(authenticated(post("/v1/orders"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items[0].product_id").value(product.getId().toString()))
                .andReturn().getResponse().getContentAsString();

        // Assert DB state including new shipping fields are null by default
        String createdOrderId = objectMapper.readTree(responseJson).path("data").path("id").asText();
        var createdOrderOpt = orderRepository.findById(UUID.fromString(createdOrderId));
        assertThat(createdOrderOpt).isPresent();
        var createdOrder = createdOrderOpt.get();

        // Non-shipping field assertions
        assertThat(createdOrder.getId()).isEqualTo(UUID.fromString(createdOrderId));
        assertThat(createdOrder.getUserId()).isEqualTo(user.getId());
        assertThat(createdOrder.getOrderDate()).isNotNull();
        // itemsTotal = sum of items
        assertThat(createdOrder.getItemsTotal())
                .isEqualByComparingTo(product.getPrice().multiply(BigDecimal.valueOf(2)));
        // totalAmount = itemsTotal + shippingCost
        assertThat(createdOrder.getTotalAmount())
                .isEqualByComparingTo(createdOrder.getItemsTotal().add(createdOrder.getShippingCost()));
        assertThat(createdOrder.getStatus()).isEqualTo(Status.PENDING);
        assertThat(createdOrder.getShippingAddress()).isNotNull();
        assertThat(createdOrder.getShippingAddress().getId()).isEqualTo(address.getId());
        assertThat(createdOrder.getCreatedAt()).isNotNull();
        assertThat(createdOrder.getUpdatedAt()).isNotNull();

        assertThat(createdOrder.getShippingCourierCode()).isEqualTo("jne");
        assertThat(createdOrder.getShippingCourierService()).isEqualTo("REG");
        assertThat(createdOrder.getShippingCourierServiceName()).isEqualTo("JNE Regular");
        assertThat(createdOrder.getShippingCost()).isEqualByComparingTo(new BigDecimal("15000"));
        assertThat(createdOrder.getShippingOrderId()).isNull();
        assertThat(createdOrder.getTrackingId()).isNull();
        assertThat(createdOrder.getEstimatedDeliveryDate()).isNull();
        assertThat(createdOrder.getShippingStatus()).isNull();
    }

    @Test
    void createOrderFromCart_withShippingSelection_success() throws Exception {
        // Add product to cart first
        String addToCartJson = String.format("{\"productId\":\"%s\",\"quantity\":1}", product.getId());
        mockMvc.perform(authenticated(post("/v1/cart/items"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addToCartJson))
                .andExpect(status().isOk());

        // Prepare order request with shipping fields
        BigDecimal shippingCost = new BigDecimal("15000");
        OrderRequest orderRequest = OrderRequest.builder()
                .addressId(address.getId())
                .shippingCourierCode("jne")
                .shippingCourierService("REG")
                .shippingCourierServiceName("JNE Regular")
                .shippingCost(shippingCost)
                .build();

        String responseJson = mockMvc.perform(authenticated(post("/v1/orders"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.shipping_courier_code").value("jne"))
                .andExpect(jsonPath("$.data.shipping_courier_service").value("REG"))
                .andExpect(jsonPath("$.data.shipping_courier_service_name").value("JNE Regular"))
                .andExpect(jsonPath("$.data.shipping_cost").value(15000))
                .andReturn().getResponse().getContentAsString();

        // Assert DB state reflects provided shipping fields
        String createdOrderId = objectMapper.readTree(responseJson).path("data").path("id").asText();
        var createdOrderOpt = orderRepository.findById(UUID.fromString(createdOrderId));
        assertThat(createdOrderOpt).isPresent();
        var createdOrder = createdOrderOpt.get();

        assertThat(createdOrder.getShippingCourierCode()).isEqualTo("jne");
        assertThat(createdOrder.getShippingCourierService()).isEqualTo("REG");
        assertThat(createdOrder.getShippingCourierServiceName()).isEqualTo("JNE Regular");
        assertThat(createdOrder.getShippingCost()).isEqualByComparingTo(shippingCost);
        // Validate totals semantics
        assertThat(createdOrder.getItemsTotal()).isEqualByComparingTo(product.getPrice());
        assertThat(createdOrder.getTotalAmount())
                .isEqualByComparingTo(product.getPrice().add(shippingCost));
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
                .addressId(address.getId())
                .shippingCourierCode("jne")
                .shippingCourierService("REG")
                .shippingCourierServiceName("JNE Regular")
                .shippingCost(new BigDecimal("15000"))
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
                .addressId(address.getId())
                .shippingCourierCode("jne")
                .shippingCourierService("REG")
                .shippingCourierServiceName("JNE Regular")
                .shippingCost(new BigDecimal("15000.00"))
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
