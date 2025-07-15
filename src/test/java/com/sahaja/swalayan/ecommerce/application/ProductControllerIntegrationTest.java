package com.sahaja.swalayan.ecommerce.application;

import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ProductControllerIntegrationTest {

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
            .build();

        ResponseEntity<ProductDTO> createResponse = restTemplate.postForEntity(getBaseUrl(), productDTO, ProductDTO.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductDTO created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Product");

        // Get product by id
        ResponseEntity<ProductDTO> getResponse = restTemplate.getForEntity(getBaseUrl() + "/" + created.getId(), ProductDTO.class);
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
            .build();
        ProductDTO product2 = ProductDTO.builder()
            .name("Product 2")
            .description("Description 2")
            .price(new BigDecimal("29.99"))
            .stock(20)
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
}
