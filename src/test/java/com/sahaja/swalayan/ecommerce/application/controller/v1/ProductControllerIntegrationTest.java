package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.junit.jupiter.api.Assertions.fail;

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

    @AfterEach
    void cleanUp() {
        for (UUID productId : productsToCleanup) {
            if (productId == null) {
                log.warn("[cleanUp] Skipping null productId");
                continue;
            }
            try {
                restTemplate.delete(getBaseUrl() + "/" + productId);
                log.debug("[cleanUp] Deleted product: {}", productId);
            } catch (Exception e) {
                log.warn("[cleanUp] Failed to delete product {}: {}", productId, e.getMessage());
                // Optionally, try to fetch product to see if it still exists
                try {
                    ResponseEntity<?> resp = restTemplate.getForEntity(getBaseUrl() + "/" + productId, Object.class);
                    log.warn("[cleanUp] Product {} still exists after delete attempt. Status: {}", productId, resp.getStatusCode());
                } catch (Exception ex) {
                    log.info("[cleanUp] Product {} confirmed deleted (not found)", productId);
                }
            }
        }
        productsToCleanup.clear();
        for (Path imagePath : imagesToCleanup) {
            if (imagePath == null) {
                log.warn("[cleanUp] Skipping null imagePath");
                continue;
            }
            try {
                Files.deleteIfExists(imagePath);
                log.debug("[cleanUp] Deleted image: {}", imagePath);
            } catch (Exception e) {
                log.warn("[cleanUp] Failed to delete image {}: {}", imagePath, e.getMessage());
            }
        }
        imagesToCleanup.clear();
    }

    private UUID testCategoryId;
    private final String testCategoryName = "TestCategoryForProduct";

    @BeforeAll
    void setUpCategory() {
        // Create category
        String categoryUrl = "http://localhost:" + port + "/api/v1/categories";
        CategoryDTO categoryDTO = CategoryDTO.builder()
                .name(testCategoryName)
                .description("Test Category Desc")
                .build();
        ResponseEntity<CategoryDTO> response = restTemplate.postForEntity(categoryUrl, categoryDTO, CategoryDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        testCategoryId = response.getBody().getId();
    }

    @AfterAll
    void tearDownCategory() {
        // Delete category
        if (testCategoryId != null) {
            String deleteUrl = "http://localhost:" + port + "/api/v1/categories/" + testCategoryId;
            restTemplate.delete(deleteUrl);
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/products";
    }

    @Test
    @Order(1)
    void testCreateAndGetProduct() {
        log.debug("[testCreateAndGetProduct] Start");
        // Create product
        ProductDTO productDTO = ProductDTO.builder()
                .name("Test Product")
                .description("Test Desc")
                .price(new BigDecimal("19.99"))
                .quantity(10)
                .weight(5)
                .categoryId(testCategoryId)
                .build();

        log.debug("[testCreateAndGetProduct] Creating product: {}", productDTO);
        ResponseEntity<ProductDTO> createResponse = restTemplate.postForEntity(getBaseUrl(), productDTO, ProductDTO.class);
        log.debug("[testCreateAndGetProduct] Create response: status={}, body={}", createResponse.getStatusCode(), createResponse.getBody());

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductDTO created = createResponse.getBody();
        if (created == null) {
            log.error("[testCreateAndGetProduct] Product creation failed, response body is null! Full response: {}", createResponse);
            fail("Product creation failed: response body is null");
        }
        if (created.getId() == null) {
            log.error("[testCreateAndGetProduct] Product creation failed, ID is null! ProductDTO: {}", created);
            fail("Product creation failed: ID is null");
        }
        log.debug("[testCreateAndGetProduct] Product created: {}", created);
        assertThat(created.getName()).isEqualTo("Test Product");

        // Get product by id
        log.debug("[testCreateAndGetProduct] Fetching product by id: {}", created.getId());
        ResponseEntity<ProductDTO> getResponse = restTemplate.getForEntity(getBaseUrl() + "/" + created.getId(), ProductDTO.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductDTO fetched = getResponse.getBody();
        if (fetched == null) {
            log.error("[testCreateAndGetProduct] Fetched product is null! Response: {}", getResponse);
            fail("Fetched product is null");
        }
        log.debug("[testCreateAndGetProduct] Product fetched: {}", fetched);
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getName()).isEqualTo("Test Product");

        // mark product for cleanup
        log.debug("[testCreateAndGetProduct] Marking product for cleanup: {}", created.getId());
        productsToCleanup.add(created.getId());
        log.debug("[testCreateAndGetProduct] End");
    }

    @Test
    @Order(2)
    void testGetNotFound() {
        UUID randomId = UUID.randomUUID();
        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/" + randomId, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Product not found");
    }

    @Test
    @Order(3)
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
        ProductDTO savedProduct1 = restTemplate.postForEntity(getBaseUrl(), product1, ProductDTO.class).getBody();
        ProductDTO savedProduct2 = restTemplate.postForEntity(getBaseUrl(), product2, ProductDTO.class).getBody();

        // get all products
        ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity(getBaseUrl(), ProductDTO[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);

        // delete products after test
        restTemplate.delete(getBaseUrl() + "/" + savedProduct1.getId());
        restTemplate.delete(getBaseUrl() + "/" + savedProduct2.getId());
    }

    // test update product
    @Test
    @Order(4)
    void testUpdateProduct() {
        // save 1 product
        ProductDTO product = ProductDTO.builder()
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("19.99"))
                .quantity(10)
                .weight(1)
                .categoryId(testCategoryId)
                .build();
        ProductDTO savedProduct = restTemplate.postForEntity(getBaseUrl(), product, ProductDTO.class).getBody();

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
                HttpMethod.PUT, new HttpEntity<>(updatedProduct), ProductDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Product 1");
        assertThat(response.getBody().getDescription()).isEqualTo("Updated Description 1");
        assertThat(response.getBody().getPrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(response.getBody().getQuantity()).isEqualTo(20);

        // delete product after test
        restTemplate.delete(getBaseUrl() + "/" + savedProduct.getId());
    }

    // test delete product
    @Test
    @Order(5)
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
        ProductDTO savedProduct = restTemplate.postForEntity(getBaseUrl(), product, ProductDTO.class).getBody();

        // delete product
        ResponseEntity<Void> response = restTemplate.exchange(getBaseUrl() + "/" + savedProduct.getId(),
                HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(6)
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

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), product, String.class);
        // Acceptable: BAD_REQUEST or NOT_FOUND depending on implementation
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).containsIgnoringCase("category");
    }

    @Test
    @Order(7)
    void testCreateProductWithMalformedJson() {
        // Intentionally malformed JSON (missing closing brace)
        String malformedJson = "{\"name\": \"Bad Product\", \"price\": 10.00, ";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(malformedJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), request, String.class);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        // The exact error message may vary depending on your exception handler
        assertThat(response.getBody()).containsIgnoringCase("malformed");
    }

    @Test
    @Order(8)
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

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), invalidProduct, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String responseBody = response.getBody();
        // Assert all expected validation error messages are present
        assertThat(responseBody).contains("Product name is required");
        assertThat(responseBody).contains("Price must be greater than 0");
        assertThat(responseBody).contains("Quantity is required");
        assertThat(responseBody).contains("Weight is required");
        assertThat(responseBody).contains("Category is required");
        // Optionally, check that no unexpected errors are present (if your API returns
        // just these messages)
        // Example: assertThat(responseBody).doesNotContain("Unexpected error");
    }

    @Test
    @Order(9)
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
        ResponseEntity<ProductDTO> createResponse = restTemplate.postForEntity(getBaseUrl(), productDTO, ProductDTO.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductDTO created = createResponse.getBody();
        assertThat(created).isNotNull();

        // Prepare multipart file upload
        String uploadUrl = getBaseUrl() + "/" + created.getId() + "/image";
        ClassPathResource imageResource = new ClassPathResource("test-image.png");
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", imageResource);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        ResponseEntity<ProductDTO> uploadResponse = restTemplate.postForEntity(uploadUrl, requestEntity, ProductDTO.class);
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductDTO updated = uploadResponse.getBody();
        assertThat(updated).isNotNull();
        assertThat(updated.getImageUrl()).isNotBlank();
        // Optionally: check that the file exists on disk
        Path imagePath = Paths.get("uploads/products/", updated.getImageUrl().substring(updated.getImageUrl().lastIndexOf("/") + 1));
        log.debug("[testUploadProductImage] Image stored at: {}", imagePath);
        assertThat(Files.exists(imagePath)).isTrue();

        // Mark for cleanup
        productsToCleanup.add(created.getId());
        imagesToCleanup.add(imagePath);
        log.debug("[testUploadProductImage] End");
    }

    @Test
    @Order(10)
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
            ResponseEntity<ProductDTO> createResponse = restTemplate.postForEntity(getBaseUrl(), productDTO, ProductDTO.class);
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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

            ResponseEntity<ProductDTO> uploadResponse = restTemplate.postForEntity(uploadUrl, requestEntity, ProductDTO.class);
            assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ProductDTO updated = uploadResponse.getBody();
            assertThat(updated).isNotNull();
            assertThat(updated.getImageUrl()).isNotBlank();
            imagePath = Paths.get("uploads/products/", updated.getImageUrl().substring(updated.getImageUrl().lastIndexOf("/") + 1));
            imagesToCleanup.add(imagePath);

            // Retrieve product and verify imageUrl is present
            log.debug("[testRetrieveProductImageUrlAfterUpload] Retrieving product to verify imageUrl");
            ResponseEntity<ProductDTO> getResponse = restTemplate.getForEntity(getBaseUrl() + "/" + productId, ProductDTO.class);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ProductDTO fetched = getResponse.getBody();
            assertThat(fetched).isNotNull();
            log.debug("[testRetrieveProductImageUrlAfterUpload] Verified imageUrl matches uploaded: {}", fetched.getImageUrl());
            assertThat(fetched.getImageUrl()).isEqualTo(updated.getImageUrl());
        } catch (Exception e) {
            log.error("[testRetrieveProductImageUrlAfterUpload] Test failed, cleanup registered: {}", e.getMessage());
            throw e;
        }
        log.debug("[testRetrieveProductImageUrlAfterUpload] End");
    }

    @Test
    @Order(11)
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
            ResponseEntity<ProductDTO> createResponse = restTemplate.postForEntity(getBaseUrl(), productDTO, ProductDTO.class);
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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

            ResponseEntity<ProductDTO> uploadResponse = restTemplate.postForEntity(uploadUrl, requestEntity, ProductDTO.class);
            assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ProductDTO updated = uploadResponse.getBody();
            assertThat(updated).isNotNull();
            assertThat(updated.getImageUrl()).isNotBlank();
            imagePath = Paths.get("uploads/products/", updated.getImageUrl().substring(updated.getImageUrl().lastIndexOf("/") + 1));
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
}
