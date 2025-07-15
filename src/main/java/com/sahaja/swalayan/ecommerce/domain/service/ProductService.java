package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findAll();
    void deleteById(UUID id);
    Product update(UUID id, Product product);
    // Add more domain methods as needed
}
