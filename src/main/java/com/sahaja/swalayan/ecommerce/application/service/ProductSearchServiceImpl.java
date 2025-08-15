package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.service.ProductSearchService;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.ProductJpaRepository;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.specification.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Page<Product> search(String keyword,
                                UUID categoryId,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                Boolean available,
                                Pageable pageable) {
        // Normalize price range if needed
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            log.debug("Swapping minPrice {} and maxPrice {} for search normalization", minPrice, maxPrice);
            BigDecimal tmp = minPrice;
            minPrice = maxPrice;
            maxPrice = tmp;
        }

        Specification<Product> spec = Specification.where(null);

        Specification<Product> keywordSpec = ProductSpecifications.keyword(keyword);
        if (keywordSpec != null) spec = spec.and(keywordSpec);

        Specification<Product> categorySpec = ProductSpecifications.categoryId(categoryId);
        if (categorySpec != null) spec = spec.and(categorySpec);

        Specification<Product> minPriceSpec = ProductSpecifications.minPrice(minPrice);
        if (minPriceSpec != null) spec = spec.and(minPriceSpec);

        Specification<Product> maxPriceSpec = ProductSpecifications.maxPrice(maxPrice);
        if (maxPriceSpec != null) spec = spec.and(maxPriceSpec);

        Specification<Product> availableSpec = ProductSpecifications.available(available);
        if (availableSpec != null) spec = spec.and(availableSpec);

        return productJpaRepository.findAll(spec, pageable);
    }
}
