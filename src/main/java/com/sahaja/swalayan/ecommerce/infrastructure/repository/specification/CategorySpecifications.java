package com.sahaja.swalayan.ecommerce.infrastructure.repository.specification;

import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import org.springframework.data.jpa.domain.Specification;

public final class CategorySpecifications {
    private CategorySpecifications() {}

    public static Specification<Category> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        final String like = "%" + name.toLowerCase().trim() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), like);
    }

    public static Specification<Category> descriptionContains(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        final String like = "%" + description.toLowerCase().trim() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), like);
    }
}
