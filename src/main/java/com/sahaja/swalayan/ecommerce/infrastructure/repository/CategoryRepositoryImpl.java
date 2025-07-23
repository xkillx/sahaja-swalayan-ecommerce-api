package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.repository.CategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {
    private final CategoryJpaRepository jpaRepository;

    public CategoryRepositoryImpl(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        return jpaRepository.save(category);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public void delete(Category category) {
        jpaRepository.delete(category);
    }
}
