package com.sahaja.swalayan.ecommerce.application.controller;

import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.service.CategoryService;

import jakarta.validation.Valid;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
import com.sahaja.swalayan.ecommerce.application.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Category saved = categoryService.save(categoryMapper.toEntity(categoryDTO));
        return new ResponseEntity<>(categoryMapper.toDTO(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable UUID id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(categoryMapper.toDTO(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        List<CategoryDTO> dtos = categoryMapper.toDTOList(categories);
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryDTO categoryDTO) {
        Category updated = categoryService.update(id, categoryMapper.toEntity(categoryDTO));
        return ResponseEntity.ok(categoryMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<CategoryDTO> getCategoryByName(@RequestParam String name) {
        Category category = categoryService.findByName(name);
        return ResponseEntity.ok(categoryMapper.toDTO(category));
    }
}
