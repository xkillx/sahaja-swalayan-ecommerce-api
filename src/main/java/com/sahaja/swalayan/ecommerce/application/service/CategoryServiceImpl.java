package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.common.CategoryNotFoundException;

import com.sahaja.swalayan.ecommerce.common.EntityNotFoundException;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.service.CategoryService;
import com.sahaja.swalayan.ecommerce.domain.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import com.sahaja.swalayan.ecommerce.common.CategoryNameAlreadyExistsException;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category save(Category category) {
        categoryRepository.findByName(category.getName()).ifPresent(existing -> {
            log.error("Attempted to create duplicate category name: {}", category.getName());
            throw new CategoryNameAlreadyExistsException(category.getName());
        });
        return categoryRepository.save(category);
    }

    @Override
    public Category findById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category does not exist for id: " + id));
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        if (categoryRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public Category update(UUID id, Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category does not exist for id: " + id));
        // Check if name is changing and if new name already exists
        if (!existing.getName().equals(category.getName())) {
            categoryRepository.findByName(category.getName()).ifPresent(dup -> {
                log.warn("Attempted to update category to duplicate name: {}", category.getName());
                throw new CategoryNameAlreadyExistsException(category.getName());
            });
        }
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryRepository.save(existing);
    }

    @Override
    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category does not exist for name: " + name));
    }
}
