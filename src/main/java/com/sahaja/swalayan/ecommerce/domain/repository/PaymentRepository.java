package com.sahaja.swalayan.ecommerce.domain.repository;

import com.sahaja.swalayan.ecommerce.domain.model.order.Payment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(UUID id);
    List<Payment> findAll();
    List<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByExternalId(UUID externalId);
    void deleteById(UUID id);
    void delete(Payment payment);
}
