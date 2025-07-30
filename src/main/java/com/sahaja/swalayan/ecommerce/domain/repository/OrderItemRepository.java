package com.sahaja.swalayan.ecommerce.domain.repository;

import com.sahaja.swalayan.ecommerce.domain.model.order.OrderItem;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {
    List<OrderItem> findByOrderId(UUID orderId);
    OrderItem save(OrderItem orderItem);
    Optional<OrderItem> findById(UUID id);
    List<OrderItem> findAll();
    void deleteById(UUID id);
    void delete(OrderItem orderItem);
    // Add custom query methods if needed
}
