package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
