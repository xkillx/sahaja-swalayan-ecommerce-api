package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.application.dto.ShippingWebhookPayload;
import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.model.order.Status;
import com.sahaja.swalayan.ecommerce.domain.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ShippingWebhookService {
    private final OrderRepository orderRepository;

    public ShippingWebhookService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void handleWebhook(ShippingWebhookPayload payload) {
        if (payload == null) {
            log.debug("[shipping-webhook] Empty payload");
            return;
        }
        log.debug("[shipping-webhook] Received: {}", payload);

        // Locate order: prefer referenceId (we set orderId as reference when creating shipping order)
        Optional<Order> orderOpt = Optional.empty();
        if (payload.getReferenceId() != null && !payload.getReferenceId().isBlank()) {
            try {
                UUID orderId = UUID.fromString(payload.getReferenceId());
                orderOpt = orderRepository.findById(orderId);
            } catch (Exception ignore) {
                // ignore parse error
            }
        }
        if (orderOpt.isEmpty() && payload.getTrackingId() != null && !payload.getTrackingId().isBlank()) {
            orderOpt = orderRepository.findByTrackingId(payload.getTrackingId());
        }
        if (orderOpt.isEmpty() && payload.getId() != null && !payload.getId().isBlank()) {
            orderOpt = orderRepository.findByShippingOrderId(payload.getId());
        }
        if (orderOpt.isEmpty()) {
            log.warn("[shipping-webhook] Order not found for payload (ref={}, tracking={}, id={})",
                    payload.getReferenceId(), payload.getTrackingId(), payload.getId());
            return;
        }

        Order order = orderOpt.get();
        boolean changed = false;

        // Update tracking and shipping order ids if provided
        if (payload.getTrackingId() != null && !payload.getTrackingId().isBlank()) {
            if (order.getTrackingId() == null || !order.getTrackingId().equals(payload.getTrackingId())) {
                order.setTrackingId(payload.getTrackingId());
                changed = true;
            }
        }
        if (payload.getId() != null && !payload.getId().isBlank()) {
            if (order.getShippingOrderId() == null || !order.getShippingOrderId().equals(payload.getId())) {
                order.setShippingOrderId(payload.getId());
                changed = true;
            }
        }

        // Map provider status to our shippingStatus and overall order Status
        String providerStatus = (payload.getStatus() == null ? "" : payload.getStatus()).trim();
        if (!providerStatus.isEmpty()) {
            String lc = providerStatus.toLowerCase(Locale.ROOT);
            order.setShippingStatus(providerStatus);
            changed = true;

            // Transition rules
            if (lc.contains("delivered") || lc.equals("completed") || lc.equals("finished")) {
                // final state
                if (order.getStatus() != Status.DELIVERED) {
                    order.setStatus(Status.DELIVERED);
                    changed = true;
                }
            } else if (lc.contains("on_delivery") || lc.contains("on the way") || lc.contains("courier_picked_up") || lc.contains("picked_up") || lc.contains("in_transit")) {
                if (order.getStatus() == Status.PENDING || order.getStatus() == Status.CONFIRMED) {
                    order.setStatus(Status.SHIPPED);
                    changed = true;
                }
            } else if (lc.contains("waiting_pickup") || lc.contains("ready_for_pickup") || lc.contains("booked")) {
                if (order.getStatus() == Status.PENDING) {
                    order.setStatus(Status.CONFIRMED);
                    changed = true;
                }
            }
        }

        if (changed) {
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.debug("[shipping-webhook] Order updated id={} status={} shippingStatus={} trackingId={}",
                    order.getId(), order.getStatus(), order.getShippingStatus(), order.getTrackingId());
        } else {
            log.debug("[shipping-webhook] No changes required for order id={}", order.getId());
        }
    }
}
