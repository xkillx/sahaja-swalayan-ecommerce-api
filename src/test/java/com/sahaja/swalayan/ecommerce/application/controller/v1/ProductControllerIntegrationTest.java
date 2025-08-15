package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;

import lombok.extern.slf4j.Slf4j;

import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.ProductRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.CategoryRepository;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class ProductControllerIntegrationTest {

    private final List<UUID> productsToCleanup = new ArrayList<>();
    private final List<Path> imagesToCleanup = new ArrayList<>();

    private UUID testCategoryId;
    private final String testCategoryName = "TestCategoryForProduct";

    @BeforeEach
    void setUpAuthAndCategory() {
        // Ensure admin user exists for write operations
        adminUser = User.builder()
                .name("Admin IT User")
                .email("admin.product.it@example.com")
                .passwordHash("dummy")
                .phone("+620000000000")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(adminUser);

        // Create new test category with ADMIN token
        String categoryUrl = "http://localhost:" + port + "/api/v1/categories";
        CategoryDTO categoryDTO = CategoryDTO.builder()
                .name(testCategoryName)
                .description("Test Category Desc")
                .build();
        ResponseEntity<CategoryDTO> response = restTemplate.exchange(
                categoryUrl,
                HttpMethod.POST,
                new HttpEntity<>(categoryDTO, adminHeaders()),
                CategoryDTO.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        testCategoryId = response.getBody().getId();
        log.debug("[setUpAuthAndCategory] Created test category: {}", testCategoryId);
    }

    @AfterEach
    void cleanUpResources() {
        // Clean up products
        for (UUID productId : productsToCleanup) {
            if (productId == null) {
                log.warn("[cleanUpResources] Skipping null productId");
                continue;
            }
            try {
                restTemplate.exchange(getBaseUrl() + "/" + productId, HttpMethod.DELETE,
                        new HttpEntity<>(adminHeaders()), Void.class);
                log.debug("[cleanUpResources] Deleted product: {}", productId);
            } catch (Exception e) {
                log.warn("[cleanUpResources] Failed to delete product {}: {}", productId, e.getMessage());
            }
        }
        productsToCleanup.clear();
        // Clean up images
        for (Path imagePath : imagesToCleanup) {
            if (imagePath == null) {
                log.warn("[cleanUpResources] Skipping null imagePath");
                continue;
            }
            try {
                Files.deleteIfExists(imagePath);
                log.debug("[cleanUpResources] Deleted image: {}", imagePath);
            } catch (Exception e) {
                log.warn("[cleanUpResources] Failed to delete image {}: {}", imagePath, e.getMessage());
            }
        }
        imagesToCleanup.clear();
        // Clean up test category
        if (testCategoryId != null) {
            try {
                String deleteUrl = "http://localhost:" + port + "/api/v1/categories/" + testCategoryId;
                restTemplate.exchange(deleteUrl, HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), Void.class);
                log.debug("[cleanUpResources] Deleted test category: {}", testCategoryId);
            } catch (Exception e) {
                log.warn("[cleanUpResources] Failed to delete test category {}: {}", testCategoryId, e.getMessage());
            }
            testCategoryId = null;
        }

        // Clean up users
        try {
            if (nonAdminUser != null && nonAdminUser.getId() != null) {
                userRepository.delete(nonAdminUser);
                nonAdminUser = null;
            }
            if (adminUser != null && adminUser.getId() != null) {
                userRepository.delete(adminUser);
                adminUser = null;
            }
        } catch (Exception e) {
            log.warn("[cleanUpResources] Failed to delete users: {}", e.getMessage());
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User adminUser;
    private User nonAdminUser;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/products";
    }

    private HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        // Generate fresh token per call to avoid clock skew issues
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", "ADMIN");
        String token = jwtTokenUtil.generateToken(adminUser.getEmail(), claims);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @Order(1)
    void testCreateProductWithCategory() {
        log.debug("[testCreateProductWithCategory] Start");
        ProductDTO productDTO = ProductDTO.builder()
                .name("Test Product")
                .description("Test Desc")
                .price(new BigDecimal("19.99"))
                .quantity(10)
                .weight(5)
                .categoryId(testCategoryId)
                .build();

        ResponseEntity<ProductDTO> createResponse = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(productDTO, adminHeaders()),
                ProductDTO.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductDTO created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Product");
        assertThat(created.getDescription()).isEqualTo("Test Desc");
        assertThat(created.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(created.getQuantity()).isEqualTo(10);
        assertThat(created.getWeight()).isEqualTo(5);
        assertThat(created.getCategoryId()).isEqualTo(testCategoryId);
        // DB assertion
        assertThat(productRepository.findById(created.getId())).isPresent();
        assertThat(categoryRepository.findById(testCategoryId)).isPresent();
        productsToCleanup.add(created.getId());
        log.debug("[testCreateProductWithCategory] End");
    }

    @Test
    @Order(2)
    void testCreateProductWithUncategorised() {
        log.debug("[testCreateProductWithUncategorised] Start");
        ProductDTO noCategoryProduct = ProductDTO.builder()
                .name("No Category Product")
                .description("No cat")
                .price(new BigDecimal("9.99"))
                .quantity(3)
                .weight(1)
                .build();
        ResponseEntity<ProductDTO> noCatResponse = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(noCategoryProduct, adminHeaders()),
                ProductDTO.class
        );
        assertThat(noCatResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductDTO noCatCreated = noCatResponse.getBody();
        assertThat(noCatCreated).isNotNull();
        assertThat(noCatCreated.getId()).isNotNull();
        assertThat(noCatCreated.getName()).isEqualTo("No Category Product");
        assertThat(noCatCreated.getDescription()).isEqualTo("No cat");
        assertThat(noCatCreated.getPrice()).isEqualTo(new BigDecimal("9.99"));
        assertThat(noCatCreated.getQuantity()).isEqualTo(3);
        assertThat(noCatCreated.getWeight()).isEqualTo(1);
        assertThat(noCatCreated.getCategoryId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        productsToCleanup.add(noCatCreated.getId());
        log.debug("[testCreateProductWithUncategorised] End");
    }

    @Test
    @Order(3)
    void testGetNotFound() {
        UUID randomId = UUID.randomUUID();
        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/" + randomId, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Product not found");
    }

    @Test
    @Order(4)
    void testGetAllProducts() {
        // save 2 products
        ProductDTO product1 = ProductDTO.builder()
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("19.99"))
                .quantity(10)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO product2 = ProductDTO.builder()
                .name("Product 2")
                .description("Description 2")
                .price(new BigDecimal("29.99"))
                .quantity(20)
                .weight(2)
                .categoryId(testCategoryId)
                .build();
        ProductDTO savedProduct1 = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(product1, adminHeaders()), ProductDTO.class
        ).getBody();
        ProductDTO savedProduct2 = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(product2, adminHeaders()), ProductDTO.class
        ).getBody();

        // get all products
        ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity(getBaseUrl(), ProductDTO[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);

        // delete products after test
        restTemplate.exchange(getBaseUrl() + "/" + savedProduct1.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
        restTemplate.exchange(getBaseUrl() + "/" + savedProduct2.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }

    // test update product
    @Test
    @Order(5)
    void testUpdateProduct() {
        // Create a product with no category (should default to Uncategorised)
        ProductDTO uncategorisedProduct = ProductDTO.builder()
                .name("Uncat Product")
                .description("Uncategorised")
                .price(new BigDecimal("5.00"))
                .quantity(2)
                .weight(1)
                .build();
        ProductDTO savedProduct = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(uncategorisedProduct, adminHeaders()), ProductDTO.class
        ).getBody();

        // update product
        ProductDTO updatedProduct = ProductDTO.builder()
                .name("Updated Product 1")
                .description("Updated Description 1")
                .price(new BigDecimal("29.99"))
                .quantity(20)
                .weight(2)
                .categoryId(testCategoryId)
                .build();
        ResponseEntity<ProductDTO> response = restTemplate.exchange(getBaseUrl() + "/" + savedProduct.getId(),
                HttpMethod.PUT, new HttpEntity<>(updatedProduct, adminHeaders()), ProductDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Product 1");
        assertThat(response.getBody().getDescription()).isEqualTo("Updated Description 1");
        assertThat(response.getBody().getPrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(response.getBody().getQuantity()).isEqualTo(20);
        assertThat(response.getBody().getWeight()).isEqualTo(2);
        assertThat(response.getBody().getCategoryId()).isEqualTo(testCategoryId);

        // delete product after test
        restTemplate.exchange(getBaseUrl() + "/" + savedProduct.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }

    // test delete product
    @Test
    @Order(6)
    void testDeleteProduct() {
        // save 1 product
        ProductDTO product = ProductDTO.builder()
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("19.99"))
                .quantity(10)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO savedProduct = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(product, adminHeaders()), ProductDTO.class
        ).getBody();

        // delete product
        ResponseEntity<Void> response = restTemplate.exchange(getBaseUrl() + "/" + savedProduct.getId(),
                HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(7)
    void testCreateProductWithInvalidCategory() {
        // Use a random UUID for categoryId that does not exist
        UUID invalidCategoryId = UUID.randomUUID();
        ProductDTO product = ProductDTO.builder()
                .name("Product Invalid Category")
                .description("Should fail")
                .price(new BigDecimal("10.00"))
                .quantity(5)
                .weight(1)
                .categoryId(invalidCategoryId)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(product, adminHeaders()), String.class
        );
        // Should return 400 BAD_REQUEST with ApiResponse error
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid categoryId supplied");
        assertThat(response.getBody()).contains("success").contains("false");
    }

    @Test
    @Order(8)
    void testCreateProductWithMalformedJson() {
        // Intentionally malformed JSON (missing closing brace)
        String malformedJson = "{\"name\": \"Bad Product\", \"price\": 10.00, ";
        HttpHeaders headers = adminHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(malformedJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), request, String.class);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        // The exact error message may vary depending on your exception handler
        assertThat(response.getBody()).containsIgnoringCase("malformed");
    }

    @Test
    @Order(9)
    void testCreateProductValidationErrors() {
        // Create an invalid ProductDTO (blank name, negative price, null stock, null
        // category)
        ProductDTO invalidProduct = ProductDTO.builder()
                .name("") // blank name
                .price(new BigDecimal("-1")) // negative price
                .quantity(null) // null stock
                .weight(null) // null weight
                .categoryId(null) // null category
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(invalidProduct, adminHeaders()), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String responseBody = response.getBody();
        // Assert all expected validation error messages are present
        assertThat(responseBody).contains("Product name is required");
        assertThat(responseBody).contains("Price must be greater than 0");
        assertThat(responseBody).contains("Quantity is required");
        assertThat(responseBody).contains("Weight is required"); // category is now optional, do not assert

        // Optionally, check that no unexpected errors are present (if your API returns
        // just these messages)
        // Example: assertThat(responseBody).doesNotContain("Unexpected error");
    }

    @Test
    @Order(10)
    void testUploadProductImage() throws Exception {
        log.debug("[testUploadProductImage] Start");
        // Create a product first
        ProductDTO productDTO = ProductDTO.builder()
                .name("Image Product")
                .description("Product with image")
                .price(new BigDecimal("29.99"))
                .quantity(5)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ResponseEntity<ProductDTO> createResponse = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(productDTO, adminHeaders()), ProductDTO.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductDTO created = createResponse.getBody();
        assertThat(created).isNotNull();

        // Prepare multipart file upload
        String uploadUrl = getBaseUrl() + "/" + created.getId() + "/image";
        ClassPathResource imageResource = new ClassPathResource("test-image.png");
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", imageResource);
        HttpHeaders headers = adminHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        ResponseEntity<ProductDTO> uploadResponse = restTemplate.postForEntity(uploadUrl, requestEntity,
                ProductDTO.class);
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductDTO updated = uploadResponse.getBody();
        assertThat(updated).isNotNull();
        assertThat(updated.getImageUrl()).isNotBlank();
        // Optionally: check that the file exists on disk
        Path imagePath = Paths.get("uploads/products/",
                updated.getImageUrl().substring(updated.getImageUrl().lastIndexOf("/") + 1));
        log.debug("[testUploadProductImage] Image stored at: {}", imagePath);
        assertThat(Files.exists(imagePath)).isTrue();

        // Mark for cleanup
        productsToCleanup.add(created.getId());
        imagesToCleanup.add(imagePath);
        log.debug("[testUploadProductImage] End");
    }

    @Test
    @Order(11)
    void testRetrieveProductImageUrlAfterUpload() throws Exception {
        log.debug("[testRetrieveProductImageUrlAfterUpload] Start");
        UUID productId = null;
        Path imagePath = null;
        try {
            // Create a product
            ProductDTO productDTO = ProductDTO.builder()
                    .name("RetrieveImage Product")
                    .description("Product for image retrieval")
                    .price(new BigDecimal("29.99"))
                    .quantity(5)
                    .weight(1)
                    .categoryId(testCategoryId)
                    .build();
            ResponseEntity<ProductDTO> createResponse = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(productDTO, adminHeaders()), ProductDTO.class
            );
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ProductDTO created = createResponse.getBody();
            assertThat(created).isNotNull();
            productId = created.getId();
            productsToCleanup.add(productId);

            // Upload image
            String uploadUrl = getBaseUrl() + "/" + productId + "/image";
            ClassPathResource imageResource = new ClassPathResource("test-image.png");
            LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("file", imageResource);
            HttpHeaders headers = adminHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

            ResponseEntity<ProductDTO> uploadResponse = restTemplate.postForEntity(uploadUrl, requestEntity,
                    ProductDTO.class);
            assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ProductDTO updated = uploadResponse.getBody();
            assertThat(updated).isNotNull();
            assertThat(updated.getImageUrl()).isNotBlank();
            imagePath = Paths.get("uploads/products/",
                    updated.getImageUrl().substring(updated.getImageUrl().lastIndexOf("/") + 1));
            imagesToCleanup.add(imagePath);

            // Retrieve product and verify imageUrl is present
            log.debug("[testRetrieveProductImageUrlAfterUpload] Retrieving product to verify imageUrl");
            ResponseEntity<ProductDTO> getResponse = restTemplate.getForEntity(getBaseUrl() + "/" + productId,
                    ProductDTO.class);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ProductDTO fetched = getResponse.getBody();
            assertThat(fetched).isNotNull();
            log.debug("[testRetrieveProductImageUrlAfterUpload] Verified imageUrl matches uploaded: {}",
                    fetched.getImageUrl());
            assertThat(fetched.getImageUrl()).isEqualTo(updated.getImageUrl());
        } catch (Exception e) {
            log.error("[testRetrieveProductImageUrlAfterUpload] Test failed, cleanup registered: {}", e.getMessage());
            throw e;
        }
        log.debug("[testRetrieveProductImageUrlAfterUpload] End");
    }

    @Test
    @Order(12)
    void testStaticServingOfProductImage() throws Exception {
        log.debug("[testStaticServingOfProductImage] Start");
        UUID productId = null;
        Path imagePath = null;
        try {
            // Create a product
            ProductDTO productDTO = ProductDTO.builder()
                    .name("StaticImage Product")
                    .description("Product for static file test")
                    .price(new BigDecimal("19.99"))
                    .quantity(1)
                    .weight(1)
                    .categoryId(testCategoryId)
                    .build();
            ResponseEntity<ProductDTO> createResponse = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(productDTO, adminHeaders()), ProductDTO.class
            );
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ProductDTO created = createResponse.getBody();
            assertThat(created).isNotNull();
            productId = created.getId();
            productsToCleanup.add(productId);

            // Upload image
            String uploadUrl = getBaseUrl() + "/" + productId + "/image";
            ClassPathResource imageResource = new ClassPathResource("test-image.png");
            LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("file", imageResource);
            HttpHeaders headers = adminHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

            ResponseEntity<ProductDTO> uploadResponse = restTemplate.postForEntity(uploadUrl, requestEntity,
                    ProductDTO.class);
            assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ProductDTO updated = uploadResponse.getBody();
            assertThat(updated).isNotNull();
            assertThat(updated.getImageUrl()).isNotBlank();
            imagePath = Paths.get("uploads/products/",
                    updated.getImageUrl().substring(updated.getImageUrl().lastIndexOf("/") + 1));
            imagesToCleanup.add(imagePath);

            // Fetch the image via static resource URL
            String imageUrl = "http://localhost:" + port + "/api" + updated.getImageUrl();
            log.debug("[testStaticServingOfProductImage] Fetching image from: {}", imageUrl);
            ResponseEntity<byte[]> imageResponse = restTemplate.getForEntity(imageUrl, byte[].class);
            assertThat(imageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(imageResponse.getHeaders().getContentType().toString()).startsWith("image/");
            assertThat(imageResponse.getBody()).isNotNull();

            // Compare content length to original file
            byte[] originalBytes = imageResource.getInputStream().readAllBytes();
            assertThat(imageResponse.getBody().length).isEqualTo(originalBytes.length);
        } catch (Exception e) {
            log.error("[testStaticServingOfProductImage] Test failed, cleanup registered: {}", e.getMessage());
            throw e;
        }
        log.debug("[testStaticServingOfProductImage] End");
    }

    @Test
    @Order(13)
    void testCreateProduct_UnauthorizedWithoutToken() {
        ProductDTO productDTO = ProductDTO.builder()
                .name("Unauthorized Product")
                .description("Should be 401")
                .price(new BigDecimal("10.00"))
                .quantity(1)
                .weight(1)
                .categoryId(testCategoryId)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(productDTO), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(14)
    void testCreateProduct_ForbiddenForNonAdmin() {
        // create CUSTOMER user and token
        nonAdminUser = User.builder()
                .name("Non Admin User")
                .email("customer.product.it@example.com")
                .passwordHash("dummy")
                .phone("+620000000001")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(nonAdminUser);

        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", "CUSTOMER");
        String token = jwtTokenUtil.generateToken(nonAdminUser.getEmail(), claims);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ProductDTO productDTO = ProductDTO.builder()
                .name("Forbidden Product")
                .description("Should be 403")
                .price(new BigDecimal("10.00"))
                .quantity(1)
                .weight(1)
                .categoryId(testCategoryId)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(productDTO, headers), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(15)
    void testSearchByKeyword_withCategoryScope() throws Exception {
        // Arrange: create two products under the test category
        ProductDTO p1 = ProductDTO.builder()
                .name("Ultra Keyword Phone")
                .description("Amazing smartphone with ultra camera")
                .price(new BigDecimal("999.99"))
                .quantity(5)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO p2 = ProductDTO.builder()
                .name("Basic Feature Phone")
                .description("Simple device")
                .price(new BigDecimal("49.99"))
                .quantity(3)
                .weight(1)
                .categoryId(testCategoryId)
                .build();

        ProductDTO c1 = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(p1, adminHeaders()), ProductDTO.class
        ).getBody();
        ProductDTO c2 = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(p2, adminHeaders()), ProductDTO.class
        ).getBody();
        assertThat(c1).isNotNull();
        assertThat(c2).isNotNull();
        productsToCleanup.add(c1.getId());
        productsToCleanup.add(c2.getId());
        // DB assertions
        assertThat(productRepository.findById(c1.getId())).isPresent();
        assertThat(productRepository.findById(c2.getId())).isPresent();

        // Act: search by keyword within the category
        String url = getBaseUrl() + "/search?keyword=ultra&categoryId=" + testCategoryId;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode content = root.get("content");
        assertThat(content).isNotNull();
        assertThat(content.size()).isGreaterThanOrEqualTo(1);
        List<String> names = new ArrayList<>();
        for (JsonNode item : content) {
            names.add(item.get("name").asText());
        }
        assertThat(names).anyMatch(n -> n.toLowerCase().contains("ultra"));
        assertThat(names).allMatch(n -> n != null && !n.isEmpty());
    }

    @Test
    @Order(16)
    void testSearchByCategory_onlyReturnsInCategory() throws Exception {
        // Arrange: one in test category, one in Uncategorised (omit categoryId)
        ProductDTO inCat = ProductDTO.builder()
                .name("Cat Product A")
                .description("In category")
                .price(new BigDecimal("10.00"))
                .quantity(1)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO uncat = ProductDTO.builder()
                .name("Uncat Product B")
                .description("No category provided")
                .price(new BigDecimal("12.00"))
                .quantity(1)
                .weight(1)
                .build();

        ProductDTO savedInCat = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(inCat, adminHeaders()), ProductDTO.class
        ).getBody();
        ProductDTO savedUncat = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(uncat, adminHeaders()), ProductDTO.class
        ).getBody();
        assertThat(savedInCat).isNotNull();
        assertThat(savedUncat).isNotNull();
        productsToCleanup.add(savedInCat.getId());
        productsToCleanup.add(savedUncat.getId());

        // Act: search with categoryId filter
        String url = getBaseUrl() + "/search?categoryId=" + testCategoryId;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert only items from the category are present
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.readTree(response.getBody()).get("content");
        assertThat(content).isNotNull();
        for (JsonNode item : content) {
            JsonNode categoryNode = item.get("category");
            assertThat(categoryNode).isNotNull();
            assertThat(UUID.fromString(categoryNode.get("id").asText())).isEqualTo(testCategoryId);
        }
    }

    @Test
    @Order(17)
    void testSearchByPriceRange_filtersByMinAndMax() throws Exception {
        // Arrange: three products with different prices
        ProductDTO p10 = ProductDTO.builder()
                .name("Price 10")
                .description("P10")
                .price(new BigDecimal("10.00"))
                .quantity(1)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO p20 = ProductDTO.builder()
                .name("Price 20")
                .description("P20")
                .price(new BigDecimal("20.00"))
                .quantity(1)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO p30 = ProductDTO.builder()
                .name("Price 30")
                .description("P30")
                .price(new BigDecimal("30.00"))
                .quantity(1)
                .weight(1)
                .categoryId(testCategoryId)
                .build();

        ProductDTO c10 = restTemplate.exchange(getBaseUrl(), HttpMethod.POST,
                new HttpEntity<>(p10, adminHeaders()), ProductDTO.class).getBody();
        ProductDTO c20 = restTemplate.exchange(getBaseUrl(), HttpMethod.POST,
                new HttpEntity<>(p20, adminHeaders()), ProductDTO.class).getBody();
        ProductDTO c30 = restTemplate.exchange(getBaseUrl(), HttpMethod.POST,
                new HttpEntity<>(p30, adminHeaders()), ProductDTO.class).getBody();
        assertThat(c10).isNotNull();
        assertThat(c20).isNotNull();
        assertThat(c30).isNotNull();
        productsToCleanup.add(c10.getId());
        productsToCleanup.add(c20.getId());
        productsToCleanup.add(c30.getId());

        // Act: min=15, max=25 should return only 20
        String url = getBaseUrl() + "/search?categoryId=" + testCategoryId + "&minPrice=15&maxPrice=25";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.readTree(response.getBody()).get("content");
        assertThat(content).isNotNull();
        assertThat(content.size()).isEqualTo(1);
        assertThat(content.get(0).get("name").asText()).isEqualTo("Price 20");
        assertThat(new BigDecimal(content.get(0).get("price").asText())).isEqualByComparingTo("20.00");
    }

    @Test
    @Order(18)
    void testSearchByAvailability_trueAndFalse() throws Exception {
        // Arrange: available and not available
        ProductDTO available = ProductDTO.builder()
                .name("Available X")
                .description("stock > 0")
                .price(new BigDecimal("5.00"))
                .quantity(2)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO notAvailable = ProductDTO.builder()
                .name("Not Available Y")
                .description("stock = 0")
                .price(new BigDecimal("5.00"))
                .quantity(0)
                .weight(1)
                .categoryId(testCategoryId)
                .build();

        ProductDTO cAvail = restTemplate.exchange(getBaseUrl(), HttpMethod.POST,
                new HttpEntity<>(available, adminHeaders()), ProductDTO.class).getBody();
        ProductDTO cNotAvail = restTemplate.exchange(getBaseUrl(), HttpMethod.POST,
                new HttpEntity<>(notAvailable, adminHeaders()), ProductDTO.class).getBody();
        assertThat(cAvail).isNotNull();
        assertThat(cNotAvail).isNotNull();
        productsToCleanup.add(cAvail.getId());
        productsToCleanup.add(cNotAvail.getId());

        ObjectMapper mapper = new ObjectMapper();

        // Act + Assert: available=true
        String urlTrue = getBaseUrl() + "/search?categoryId=" + testCategoryId + "&available=true";
        ResponseEntity<String> respTrue = restTemplate.getForEntity(urlTrue, String.class);
        assertThat(respTrue.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode contentTrue = mapper.readTree(respTrue.getBody()).get("content");
        assertThat(contentTrue).isNotNull();
        for (JsonNode item : contentTrue) {
            assertThat(item.get("quantity").asInt()).isGreaterThan(0);
        }

        // Act + Assert: available=false
        String urlFalse = getBaseUrl() + "/search?categoryId=" + testCategoryId + "&available=false";
        ResponseEntity<String> respFalse = restTemplate.getForEntity(urlFalse, String.class);
        assertThat(respFalse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode contentFalse = mapper.readTree(respFalse.getBody()).get("content");
        assertThat(contentFalse).isNotNull();
        for (JsonNode item : contentFalse) {
            assertThat(item.get("quantity").asInt()).isEqualTo(0);
        }
    }

    @Test
    @Order(19)
    void testSearchPaginationAndSorting_byPriceDesc() throws Exception {
        // Arrange: create three products with different prices
        ProductDTO a = ProductDTO.builder()
                .name("Paginated A")
                .description("A")
                .price(new BigDecimal("30.00"))
                .quantity(1)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO b = ProductDTO.builder()
                .name("Paginated B")
                .description("B")
                .price(new BigDecimal("20.00"))
                .quantity(1)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO c = ProductDTO.builder()
                .name("Paginated C")
                .description("C")
                .price(new BigDecimal("10.00"))
                .quantity(1)
                .weight(1)
                .categoryId(testCategoryId)
                .build();

        ProductDTO ca = restTemplate.exchange(getBaseUrl(), HttpMethod.POST,
                new HttpEntity<>(a, adminHeaders()), ProductDTO.class).getBody();
        ProductDTO cb = restTemplate.exchange(getBaseUrl(), HttpMethod.POST,
                new HttpEntity<>(b, adminHeaders()), ProductDTO.class).getBody();
        ProductDTO cc = restTemplate.exchange(getBaseUrl(), HttpMethod.POST,
                new HttpEntity<>(c, adminHeaders()), ProductDTO.class).getBody();
        assertThat(ca).isNotNull();
        assertThat(cb).isNotNull();
        assertThat(cc).isNotNull();
        productsToCleanup.add(ca.getId());
        productsToCleanup.add(cb.getId());
        productsToCleanup.add(cc.getId());

        ObjectMapper mapper = new ObjectMapper();

        // Page 0, size 2, sort price desc -> [30, 20]
        String urlPage0 = getBaseUrl() + "/search?categoryId=" + testCategoryId + "&page=0&size=2&sort=price,desc";
        ResponseEntity<String> resp0 = restTemplate.getForEntity(urlPage0, String.class);
        assertThat(resp0.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode content0 = mapper.readTree(resp0.getBody()).get("content");
        assertThat(content0.size()).isEqualTo(2);
        BigDecimal p0 = new BigDecimal(content0.get(0).get("price").asText());
        BigDecimal p1 = new BigDecimal(content0.get(1).get("price").asText());
        assertThat(p0).isGreaterThanOrEqualTo(p1);

        // Page 1, size 2 -> remaining [10]
        String urlPage1 = getBaseUrl() + "/search?categoryId=" + testCategoryId + "&page=1&size=2&sort=price,desc";
        ResponseEntity<String> resp1 = restTemplate.getForEntity(urlPage1, String.class);
        assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode content1 = mapper.readTree(resp1.getBody()).get("content");
        assertThat(content1.size()).isEqualTo(1);
        BigDecimal pOnly = new BigDecimal(content1.get(0).get("price").asText());
        assertThat(pOnly).isEqualByComparingTo("10.00");
    }
}
