package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    java.util.Optional<Order> findByTrackingId(String trackingId);
    java.util.Optional<Order> findByShippingOrderId(String shippingOrderId);
}
