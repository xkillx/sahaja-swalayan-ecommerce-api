package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.common.ProductNotFoundException;
import com.sahaja.swalayan.ecommerce.domain.model.product.Price;
import com.sahaja.swalayan.ecommerce.domain.model.product.Stock;
import com.sahaja.swalayan.ecommerce.domain.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@Transactional
public class ProductServiceImplIntegrationTest {

    @Autowired
    private ProductService productService;

    @AfterEach
    void tearDown() {
        // productService.findAll().forEach(p -> productService.deleteById(p.getId()));
    }

    @Test
    void testFindByName() {
        Product product = Product.builder()
            .id(null)
            .name("Unique By Name")
            .description("Desc")
            .price(new Price(java.math.BigDecimal.valueOf(1234)))
            .stock(new Stock(9))
            .build();
        Product saved = productService.save(product);
        Product found = productService.findByName("Unique By Name");
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("Unique By Name");
    }

    @Test
    void testSaveAndFindById() {
        Product product = Product.builder()
            .id(null)
            .name("Test Product")
            .description("A test product")
            .price(new Price(java.math.BigDecimal.valueOf(10000)))
            .stock(new Stock(10))
            .build();
        Product saved = productService.save(product);
        Product found = productService.findById(saved.getId());
        assertThat(found.getName()).isEqualTo("Test Product");
    }

    @Test
    void testFindAll() {
        Product product1 = Product.builder()
            .id(null)
            .name("Product 1")
            .description("Description 1")
            .price(new Price(java.math.BigDecimal.valueOf(5000)))
            .stock(new Stock(5))
            .build();
        Product product2 = Product.builder()
            .id(null)
            .name("Product 2")
            .description("Description 2")
            .price(new Price(java.math.BigDecimal.valueOf(7000)))
            .stock(new Stock(7))
            .build();
        productService.save(product1);
        productService.save(product2);
        List<Product> products = productService.findAll();
        assertThat(products).hasSize(2);
    }

    @Test
    void testUpdateProduct() {
        Product product = Product.builder()
            .id(null)
            .name("Original Name")
            .description("Original Description")
            .price(new Price(java.math.BigDecimal.valueOf(2000)))
            .stock(new Stock(2))
            .build();
        Product saved = productService.save(product);

        Product updatedData = Product.builder()
            .id(null)
            .name("Updated Name")
            .description("Updated Description")
            .price(new Price(java.math.BigDecimal.valueOf(3000)))
            .stock(new Stock(5))
            .build();
        Product updated = productService.update(saved.getId(), updatedData);
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getPrice().getValue()).isEqualByComparingTo("3000");
        assertThat(updated.getStock().getValue()).isEqualTo(5);
    }

    @Test
    void testUpdateNonExistentProductThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        Product updatedData = Product.builder()
            .id(null)
            .name("Name")
            .description("Description")
            .price(new Price(java.math.BigDecimal.valueOf(1000)))
            .stock(new Stock(1))
            .build();
        assertThatThrownBy(() -> productService.update(nonExistentId, updatedData))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found with id: " + nonExistentId);
    }

    @Test
    void testDeleteById() {
        Product product = Product.builder()
            .id(null)
            .name("To Delete")
            .description("Description to delete")
            .price(new Price(java.math.BigDecimal.valueOf(3000)))
            .stock(new Stock(3))
            .build();
        Product saved = productService.save(product);
        productService.deleteById(saved.getId());
        assertThatThrownBy(() -> productService.findById(saved.getId()))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found with id: " + saved.getId());
    }
}
