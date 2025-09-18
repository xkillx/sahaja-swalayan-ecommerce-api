package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.model.order.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    Optional<Order> findByTrackingId(String trackingId);
    Optional<Order> findByShippingOrderId(String shippingOrderId);

    // Eager-loading variants to avoid LazyInitializationException when mapping to DTOs outside transaction
    @EntityGraph(attributePaths = {"items", "shippingAddress"})
    List<Order> findAllWithDetailsByUserId(UUID userId);

    @EntityGraph(attributePaths = {"items", "shippingAddress"})
    Optional<Order> findWithDetailsById(UUID id);

    // Admin list helpers
    Page<Order> findAll(Pageable pageable);
    Page<Order> findAllByStatus(Status status, Pageable pageable);
    Page<Order> findAllByOrderDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<Order> findAllByStatusAndOrderDateBetween(Status status, LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<Order> findAllByUserId(UUID userId, Pageable pageable);

    List<Order> findTop5ByOrderByCreatedAtDesc();

    // Aggregates for dashboard
    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.orderDate between :from and :to")
    BigDecimal sumRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByOrderDateBetween(LocalDateTime from, LocalDateTime to);

    @Query("select cast(o.orderDate as date) as d, coalesce(sum(o.totalAmount),0) as revenue from Order o where o.orderDate between :from and :to group by cast(o.orderDate as date) order by d")
    List<Object[]> dailyRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
