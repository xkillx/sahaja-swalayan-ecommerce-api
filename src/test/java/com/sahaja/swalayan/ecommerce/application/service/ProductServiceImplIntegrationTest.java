package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.model.product.Price;
import com.sahaja.swalayan.ecommerce.domain.model.product.Stock;
import com.sahaja.swalayan.ecommerce.domain.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProductServiceImplIntegrationTest {

    @Autowired
    private ProductService productService;

    @AfterEach
    void tearDown() {
        // productService.findAll().forEach(p -> productService.deleteById(p.getId()));
    }

    @Test
    void testSaveAndFindById() {
        Product product = new Product(
            null,
            "Test Product",
            "A test product",
            new Price(java.math.BigDecimal.valueOf(10000)),
            new Stock(10)
        );
        Product saved = productService.save(product);
        Optional<Product> found = productService.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
        // remove product
        productService.deleteById(saved.getId());
    }

    @Test
    void testFindAll() {
        Product product1 = new Product(
            null,
            "Product 1",
            "Description 1",
            new Price(java.math.BigDecimal.valueOf(5000)),
            new Stock(5)
        );
        Product product2 = new Product(
            null,
            "Product 2",
            "Description 2",
            new Price(java.math.BigDecimal.valueOf(7000)),
            new Stock(7)
        );
        productService.save(product1);
        productService.save(product2);
        List<Product> products = productService.findAll();
        assertThat(products).hasSize(2);
        // remove products
        productService.deleteById(product1.getId());
        productService.deleteById(product2.getId());
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product(
            null,
            "Original Name",
            "Original Description",
            new Price(java.math.BigDecimal.valueOf(2000)),
            new Stock(2)
        );
        Product saved = productService.save(product);

        Product updatedData = new Product(
            null,
            "Updated Name",
            "Updated Description",
            new Price(java.math.BigDecimal.valueOf(3000)),
            new Stock(5)
        );
        Product updated = productService.update(saved.getId(), updatedData);
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getPrice().getValue()).isEqualByComparingTo("3000");
        assertThat(updated.getStock().getValue()).isEqualTo(5);
        // cleanup
        productService.deleteById(saved.getId());
    }

    @Test
    void testDeleteById() {
        Product product = new Product(
            null,
            "To Delete",
            "Description to delete",
            new Price(java.math.BigDecimal.valueOf(3000)),
            new Stock(3)
        );
        Product saved = productService.save(product);
        productService.deleteById(saved.getId());
        Optional<Product> found = productService.findById(saved.getId());
        assertThat(found).isNotPresent();
    }
}
