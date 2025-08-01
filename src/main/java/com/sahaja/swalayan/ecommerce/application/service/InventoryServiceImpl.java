package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.cart.CartItem;
import com.sahaja.swalayan.ecommerce.domain.repository.ProductRepository;
import com.sahaja.swalayan.ecommerce.domain.service.InventoryService;
import com.sahaja.swalayan.ecommerce.common.InsufficientStockException;
import com.sahaja.swalayan.ecommerce.common.ProductNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final ProductRepository productRepository;

    public InventoryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void reserveStock(Set<CartItem> cartItems) {
        log.debug("Reserving stock for {} cart items", cartItems.size());
        for (CartItem item : cartItems) {
            UUID productId = item.getProduct().getId();
            int quantityRequested = item.getQuantity();
            var productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                log.error("Product not found for reservation: {}", productId);
                throw new ProductNotFoundException("Product not found: " + productId);
            }
            var product = productOpt.get();
            int availableStock = product.getStock();
            log.debug("Checking stock for productId={}, name={}, requested={}, available={}", productId, product.getName(), quantityRequested, availableStock);
            if (availableStock < quantityRequested) {
                log.warn("Insufficient stock for productId={}, name={}, requested={}, available={}", productId, product.getName(), quantityRequested, availableStock);
                throw new InsufficientStockException(
                        "Insufficient stock for product " + product.getName() + " (ID: " + productId + ") - requested: "
                                + quantityRequested + ", available: " + availableStock);
            }
            product.setStock(availableStock - quantityRequested);
            productRepository.save(product);
            log.debug("Reserved {} units for productId={}, new stock={}", quantityRequested, productId, product.getStock());
        }
    }
}
