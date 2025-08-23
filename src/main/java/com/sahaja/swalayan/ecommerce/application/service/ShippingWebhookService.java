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
        // Try courier_tracking_id as an alternative tracking identifier
        if (orderOpt.isEmpty() && payload.getCourierTrackingId() != null && !payload.getCourierTrackingId().isBlank()) {
            orderOpt = orderRepository.findByTrackingId(payload.getCourierTrackingId());
        }
        // Some providers send waybill as courier_waybill_id; try that too as tracking id
        if (orderOpt.isEmpty() && payload.getCourierWaybillId() != null && !payload.getCourierWaybillId().isBlank()) {
            orderOpt = orderRepository.findByTrackingId(payload.getCourierWaybillId());
        }
        if (orderOpt.isEmpty() && payload.getId() != null && !payload.getId().isBlank()) {
            orderOpt = orderRepository.findByShippingOrderId(payload.getId());
        }
        // Some payloads use order_id instead of id
        if (orderOpt.isEmpty() && payload.getOrderId() != null && !payload.getOrderId().isBlank()) {
            orderOpt = orderRepository.findByShippingOrderId(payload.getOrderId());
        }
        if (orderOpt.isEmpty()) {
            log.warn("[shipping-webhook] Order not found for payload (ref={}, tracking={}, courier_tracking={}, waybill={}, id={}, order_id={})",
                    payload.getReferenceId(), payload.getTrackingId(), payload.getCourierTrackingId(), payload.getCourierWaybillId(), payload.getId(), payload.getOrderId());
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

            // Transition rules - broaden to support located, picking_up, etc.
            if (lc.contains("delivered") || lc.equals("completed") || lc.equals("finished")) {
                // final state
                if (order.getStatus() != Status.DELIVERED) {
                    order.setStatus(Status.DELIVERED);
                    changed = true;
                }
            } else if (
                    lc.contains("on_delivery") || lc.contains("on the way") ||
                    lc.contains("courier_picked_up") || lc.contains("picked_up") || lc.equals("picked") ||
                    lc.contains("in_transit")
            ) {
                if (order.getStatus() == Status.PENDING || order.getStatus() == Status.CONFIRMED) {
                    order.setStatus(Status.SHIPPED);
                    changed = true;
                }
            } else if (
                    lc.contains("waiting_pickup") || lc.contains("ready_for_pickup") || lc.contains("booked") ||
                    lc.contains("picking_up") || lc.contains("driver_allocated") || lc.contains("located")
            ) {
                if (order.getStatus() == Status.PENDING) {
                    order.setStatus(Status.CONFIRMED);
                    changed = true;
                }
            }
        }

        // Enrich courier/driver info if provided
        if (payload.getCourierWaybillId() != null && !payload.getCourierWaybillId().isBlank()) {
            if (order.getCourierWaybillId() == null || !order.getCourierWaybillId().equals(payload.getCourierWaybillId())) {
                order.setCourierWaybillId(payload.getCourierWaybillId());
                changed = true;
            }
        }
        if (payload.getCourierCompany() != null && !payload.getCourierCompany().isBlank()) {
            if (order.getCourierCompany() == null || !order.getCourierCompany().equals(payload.getCourierCompany())) {
                order.setCourierCompany(payload.getCourierCompany());
                changed = true;
            }
        }
        if (payload.getCourierType() != null && !payload.getCourierType().isBlank()) {
            if (order.getCourierType() == null || !order.getCourierType().equals(payload.getCourierType())) {
                order.setCourierType(payload.getCourierType());
                changed = true;
            }
        }
        if (payload.getCourierDriverName() != null && !payload.getCourierDriverName().isBlank()) {
            if (order.getCourierDriverName() == null || !order.getCourierDriverName().equals(payload.getCourierDriverName())) {
                order.setCourierDriverName(payload.getCourierDriverName());
                changed = true;
            }
        }
        if (payload.getCourierDriverPhone() != null && !payload.getCourierDriverPhone().isBlank()) {
            if (order.getCourierDriverPhone() == null || !order.getCourierDriverPhone().equals(payload.getCourierDriverPhone())) {
                order.setCourierDriverPhone(payload.getCourierDriverPhone());
                changed = true;
            }
        }
        if (payload.getCourierDriverPlateNumber() != null && !payload.getCourierDriverPlateNumber().isBlank()) {
            if (order.getCourierDriverPlateNumber() == null || !order.getCourierDriverPlateNumber().equals(payload.getCourierDriverPlateNumber())) {
                order.setCourierDriverPlateNumber(payload.getCourierDriverPlateNumber());
                changed = true;
            }
        }
        if (payload.getCourierDriverPhotoUrl() != null && !payload.getCourierDriverPhotoUrl().isBlank()) {
            if (order.getCourierDriverPhotoUrl() == null || !order.getCourierDriverPhotoUrl().equals(payload.getCourierDriverPhotoUrl())) {
                order.setCourierDriverPhotoUrl(payload.getCourierDriverPhotoUrl());
                changed = true;
            }
        }
        if (payload.getCourierLink() != null && !payload.getCourierLink().isBlank()) {
            if (order.getCourierLink() == null || !order.getCourierLink().equals(payload.getCourierLink())) {
                order.setCourierLink(payload.getCourierLink());
                changed = true;
            }
        }
        if (payload.getUpdatedAt() != null && !payload.getUpdatedAt().isBlank()) {
            try {
                // Parse ISO instant or local date-time; if parsing fails, skip
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(payload.getUpdatedAt());
                LocalDateTime ldt = odt.toLocalDateTime();
                if (order.getShippingUpdatedAt() == null || !order.getShippingUpdatedAt().equals(ldt)) {
                    order.setShippingUpdatedAt(ldt);
                    changed = true;
                }
            } catch (Exception ex) {
                try {
                    LocalDateTime ldt2 = LocalDateTime.parse(payload.getUpdatedAt());
                    if (order.getShippingUpdatedAt() == null || !order.getShippingUpdatedAt().equals(ldt2)) {
                        order.setShippingUpdatedAt(ldt2);
                        changed = true;
                    }
                } catch (Exception ignore) {}
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
