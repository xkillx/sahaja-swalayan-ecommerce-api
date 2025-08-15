package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.service.CategorySearchService;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.CategoryJpaRepository;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.specification.CategorySpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategorySearchServiceImpl implements CategorySearchService {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Page<Category> search(String name, String description, Pageable pageable) {
        Specification<Category> spec = Specification.allOf(
                CategorySpecifications.nameContains(name),
                CategorySpecifications.descriptionContains(description)
        );
        return categoryJpaRepository.findAll(spec, pageable);
    }
}
