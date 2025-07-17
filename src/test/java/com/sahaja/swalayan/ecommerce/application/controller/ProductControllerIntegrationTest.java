package com.sahaja.swalayan.ecommerce.application.controller;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerIntegrationTest {

    private UUID testCategoryId;
    private final String testCategoryName = "TestCategoryForProduct";

    @BeforeAll
    void setUpCategory() {
        // Create category
        String categoryUrl = "http://localhost:" + port + "/api/categories";
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
            String deleteUrl = "http://localhost:" + port + "/api/categories/" + testCategoryId;
            restTemplate.delete(deleteUrl);
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/products";
    }

    @Test
    void testCreateAndGetProduct() {
        // Create product
        ProductDTO productDTO = ProductDTO.builder()
                .name("Test Product")
                .description("Test Desc")
                .price(new BigDecimal("19.99"))
                .stock(10)
                .categoryId(testCategoryId)
                .build();

        ResponseEntity<ProductDTO> createResponse = restTemplate.postForEntity(getBaseUrl(), productDTO,
                ProductDTO.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductDTO created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Product");

        // Get product by id
        ResponseEntity<ProductDTO> getResponse = restTemplate.getForEntity(getBaseUrl() + "/" + created.getId(),
                ProductDTO.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductDTO fetched = getResponse.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getName()).isEqualTo("Test Product");

        // delete product after test
        restTemplate.delete(getBaseUrl() + "/" + created.getId());
    }

    @Test
    void testGetNotFound() {
        UUID randomId = UUID.randomUUID();
        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/" + randomId, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Product not found");
    }

    @Test
    void testGetAllProducts() {
        // save 2 products
        ProductDTO product1 = ProductDTO.builder()
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("19.99"))
                .stock(10)
                .categoryId(testCategoryId)
                .build();
        ProductDTO product2 = ProductDTO.builder()
                .name("Product 2")
                .description("Description 2")
                .price(new BigDecimal("29.99"))
                .stock(20)
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
    void testUpdateProduct() {
        // save 1 product
        ProductDTO product = ProductDTO.builder()
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("19.99"))
                .stock(10)
                .categoryId(testCategoryId)
                .build();
        ProductDTO savedProduct = restTemplate.postForEntity(getBaseUrl(), product, ProductDTO.class).getBody();

        // update product
        ProductDTO updatedProduct = ProductDTO.builder()
                .name("Updated Product 1")
                .description("Updated Description 1")
                .price(new BigDecimal("29.99"))
                .stock(20)
                .categoryId(testCategoryId)
                .build();
        ResponseEntity<ProductDTO> response = restTemplate.exchange(getBaseUrl() + "/" + savedProduct.getId(),
                HttpMethod.PUT, new HttpEntity<>(updatedProduct), ProductDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Product 1");
        assertThat(response.getBody().getDescription()).isEqualTo("Updated Description 1");
        assertThat(response.getBody().getPrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(response.getBody().getStock()).isEqualTo(20);

        // delete product after test
        restTemplate.delete(getBaseUrl() + "/" + savedProduct.getId());
    }

    // test delete product
    @Test
    void testDeleteProduct() {
        // save 1 product
        ProductDTO product = ProductDTO.builder()
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("19.99"))
                .stock(10)
                .categoryId(testCategoryId)
                .build();
        ProductDTO savedProduct = restTemplate.postForEntity(getBaseUrl(), product, ProductDTO.class).getBody();

        // delete product
        ResponseEntity<Void> response = restTemplate.exchange(getBaseUrl() + "/" + savedProduct.getId(),
                HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testCreateProductValidationErrors() {
        // Create an invalid ProductDTO (blank name, negative price, null stock)
        ProductDTO invalidProduct = ProductDTO.builder()
                .name("") // blank name
                .price(new BigDecimal("-1")) // negative price
                .stock(null) // null stock
                .categoryId(null) // null category
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), invalidProduct, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Product name is required");
        assertThat(response.getBody()).contains("Price must be greater than 0");
    }
}
