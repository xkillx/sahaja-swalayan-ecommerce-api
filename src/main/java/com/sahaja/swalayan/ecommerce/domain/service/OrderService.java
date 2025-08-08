package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.model.order.PaymentMethod;
import java.util.UUID;
import java.util.List;

public interface OrderService {
    Order createOrderFromCart(UUID userId, UUID addressId, PaymentMethod paymentMethod);
    List<Order> getOrdersByUser(UUID userId);
    Order getOrderByIdForUser(UUID orderId, UUID userId);
    /**
     * Cancel the order if and only if its status is PENDING. Throws exception otherwise.
     * @param orderId the order to cancel
     * @param userId the user requesting cancellation
     * @return the cancelled order
     * @throws IllegalStateException if the order is not in PENDING status
     */
    Order cancelOrder(UUID orderId, UUID userId);
}
