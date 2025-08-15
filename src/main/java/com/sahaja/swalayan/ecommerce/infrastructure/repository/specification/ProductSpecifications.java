package com.sahaja.swalayan.ecommerce.infrastructure.repository.specification;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.criteria.JoinType;

public final class ProductSpecifications {
    private ProductSpecifications() {}

    public static Specification<Product> keyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        final String like = "%" + keyword.toLowerCase().trim() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("description")), like),
                cb.like(cb.lower(root.get("sku")), like)
        );
    }

    public static Specification<Product> categoryId(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.join("category", JoinType.LEFT).get("id"), categoryId);
    }

    public static Specification<Product> minPrice(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> maxPrice(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> available(Boolean available) {
        if (available == null) {
            return null;
        }
        if (Boolean.TRUE.equals(available)) {
            return (root, query, cb) -> cb.greaterThan(root.get("quantity"), 0);
        } else {
            return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("quantity"), 0);
        }
    }
}
