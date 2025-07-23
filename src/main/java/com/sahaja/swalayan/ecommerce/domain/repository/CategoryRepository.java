package com.sahaja.swalayan.ecommerce.domain.repository;

import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findAll();
    Optional<Category> findByName(String name);
    void deleteById(UUID id);
    void delete(Category category);
}
