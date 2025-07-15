package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
    // Add custom query methods if needed
}
