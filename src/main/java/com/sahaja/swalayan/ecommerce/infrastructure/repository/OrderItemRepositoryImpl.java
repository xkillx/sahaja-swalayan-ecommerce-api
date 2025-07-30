package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.order.OrderItem;
import com.sahaja.swalayan.ecommerce.domain.repository.OrderItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderItemRepositoryImpl implements OrderItemRepository {
    private final OrderItemJpaRepository jpaRepository;

    public OrderItemRepositoryImpl(OrderItemJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return jpaRepository.save(orderItem);
    }

    @Override
    public Optional<OrderItem> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<OrderItem> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(OrderItem orderItem) {
        jpaRepository.delete(orderItem);
    }

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId);
    }
}
