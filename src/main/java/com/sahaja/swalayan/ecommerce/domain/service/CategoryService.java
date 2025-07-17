package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Category save(Category category);
    Category findById(UUID id);
    List<Category> findAll();
    void deleteById(UUID id);
    Category update(UUID id, Category category);
    Category findByName(String name);
}
