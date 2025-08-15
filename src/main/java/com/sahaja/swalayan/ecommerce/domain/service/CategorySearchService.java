package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategorySearchService {
    Page<Category> search(String name, String description, Pageable pageable);
}
