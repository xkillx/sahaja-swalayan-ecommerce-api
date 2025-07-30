package com.sahaja.swalayan.ecommerce.domain.repository;

import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findAll();
    List<Order> findByUserId(UUID userId);
    void deleteById(UUID id);
    void delete(Order order);
    // Add custom query methods if needed
}
