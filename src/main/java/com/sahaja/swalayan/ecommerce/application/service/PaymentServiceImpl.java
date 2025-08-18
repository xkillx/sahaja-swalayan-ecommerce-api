package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.application.dto.PaymentRequest;
import com.sahaja.swalayan.ecommerce.application.dto.PaymentResponse;
import com.sahaja.swalayan.ecommerce.application.dto.XenditWebhookPayload;
import com.sahaja.swalayan.ecommerce.common.InvalidOrderStateException;
import com.sahaja.swalayan.ecommerce.common.InvalidPaymentAmountException;
import com.sahaja.swalayan.ecommerce.common.InvalidXenditWebhookException;
import com.sahaja.swalayan.ecommerce.common.OrderNotFoundException;
import com.sahaja.swalayan.ecommerce.common.PaymentNotFoundException;
import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.model.order.OrderItem;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto.XenditCreateInvoiceRequest;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto.XenditCreateInvoiceResponse;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.XenditInvoiceClient;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.XenditInvoiceCreationException;
import com.sahaja.swalayan.ecommerce.domain.model.order.Payment;
import com.sahaja.swalayan.ecommerce.domain.model.order.PaymentStatus;
import com.sahaja.swalayan.ecommerce.domain.model.order.Status;
import com.sahaja.swalayan.ecommerce.domain.repository.OrderRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.PaymentRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.OrderItemRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.ProductRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.domain.service.PaymentService;
import com.sahaja.swalayan.ecommerce.domain.service.ShippingService;
import com.sahaja.swalayan.ecommerce.infrastructure.config.ShippingOriginProperties;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CoordinateDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.OrderItemDTO;
import com.sahaja.swalayan.ecommerce.domain.model.product.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final XenditInvoiceClient xenditInvoiceClient;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ShippingService shippingService;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ShippingOriginProperties shippingOriginProperties;

    @Value("${xendit.success-redirect-url}")
    private String xenditSuccessRedirectUrl;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.debug("[createPayment] Received payment request: {}", request);
        UUID externalId = UUID.randomUUID();
        // Get and validate order
        var order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> {
                    log.warn("[createPayment] Order not found: {}", request.getOrderId());
                    return new OrderNotFoundException("Order not found: " + request.getOrderId());
                });
        // Validate order status
        if (order.getStatus() != Status.PENDING) {
            log.warn("[createPayment] Invalid order status for payment. OrderId: {}, Status: {}", order.getId(), order.getStatus());
            throw new InvalidOrderStateException(
                "Order status must be PENDING to create a payment. Current status: " + order.getStatus()
            );
        }
        // Validate payment amount equals order total
        var orderTotal = order.getTotalAmount();
        if (request.getAmount().compareTo(orderTotal) != 0) {
            log.warn("[createPayment] Amount mismatch for order {}. Requested: {}, Expected: {}", order.getId(), request.getAmount(), orderTotal);
            throw new InvalidPaymentAmountException("Payment amount does not match order total");
        }
        // Get user from order
        var user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> {
                    log.warn("[createPayment] User not found for order: {}", order.getUserId());
                    return new OrderNotFoundException("User not found for order: " + order.getUserId());
                });
        // Build success redirect URL with order ID
        String redirectUrl = xenditSuccessRedirectUrl;
        if (redirectUrl.contains("?")) {
            redirectUrl += "&orderId=" + order.getId();
        } else {
            redirectUrl += "?orderId=" + order.getId();
        }
        // Build Xendit invoice request using user's email
        XenditCreateInvoiceRequest xenditRequest =
                XenditCreateInvoiceRequest.builder()
                        .externalId(externalId.toString())
                        .amount(request.getAmount())
                        .payerEmail(user.getEmail())
                        .description("Payment for Order #" + order.getId() + " by " + user.getEmail())
                        .successRedirectUrl(redirectUrl)
                        .build();
        log.debug("[createPayment] Creating Xendit invoice for order: {}, user: {}, amount: {}", order.getId(), user.getEmail(), request.getAmount());
        // Call Xendit API
        XenditCreateInvoiceResponse xenditResponse;
        try {
            xenditResponse = xenditInvoiceClient.createInvoice(xenditRequest);
            log.debug("[createPayment] Xendit invoice created for order: {}. InvoiceId: {}", order.getId(), xenditResponse.getId());
        } catch (Exception e) {
            log.error("[createPayment] Failed to create Xendit invoice for order: {}. Error: {}", order.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create Xendit invoice", e);
        }
        if (xenditResponse == null || xenditResponse.getInvoiceUrl() == null || xenditResponse.getId() == null) {
            log.error("[createPayment] Failed to create Xendit invoice for order: {}", order.getId());
            throw new XenditInvoiceCreationException("Failed to create Xendit invoice");
        }
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .externalId(externalId)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(request.getAmount())
                .xenditInvoiceUrl(xenditResponse.getInvoiceUrl())
                .xenditCallbackToken(xenditResponse.getId())
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);
        log.debug("[createPayment] Payment created and saved. PaymentId: {}, OrderId: {}, User: {}", payment.getId(), order.getId(), user.getEmail());
        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getExternalId(), payment.getPaymentStatus(), payment.getXenditInvoiceUrl());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getExternalId(), payment.getPaymentStatus(), payment.getXenditInvoiceUrl());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByOrderId(UUID orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(p -> new PaymentResponse(p.getId(), p.getOrderId(), p.getExternalId(), p.getPaymentStatus(), p.getXenditInvoiceUrl()))
                .toList();
    }

    @Override
    @Transactional
    public void handleXenditWebhook(XenditWebhookPayload payload) {
        log.debug("[handleXenditWebhook] Received webhook payload: {}", payload);
        UUID externalId = parseAndValidatePayload(payload);
        String status = payload.getStatus();
        log.debug("[handleXenditWebhook] Processing status: {} for externalId: {}", status, externalId);

        Payment payment = getPaymentByExternalId(externalId);
        applyStatusUpdate(payment, status);
        paymentRepository.save(payment);
        log.debug("[handleXenditWebhook] Payment updated and saved. PaymentId: {}, Status: {}", payment.getId(), payment.getPaymentStatus());

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            safelyCreateShippingForPaidPayment(externalId, payment);
        }
    }

    // -------------------- Helper Methods (Webhook Handling) --------------------
    private UUID parseAndValidatePayload(XenditWebhookPayload payload) {
        if (payload == null || payload.getExternalId() == null || payload.getStatus() == null) {
            log.debug("[handleXenditWebhook] Invalid webhook payload: {}", payload);
            throw new InvalidXenditWebhookException("Invalid webhook payload");
        }
        try {
            UUID externalId = UUID.fromString(payload.getExternalId());
            log.debug("[handleXenditWebhook] Parsed externalId: {}", externalId);
            return externalId;
        } catch (Exception e) {
            log.debug("[handleXenditWebhook] Invalid external_id format: {}", payload.getExternalId());
            throw new InvalidXenditWebhookException("Invalid external_id format", e);
        }
    }

    private Payment getPaymentByExternalId(UUID externalId) {
        Payment payment = paymentRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    log.debug("[handleXenditWebhook] Payment not found for externalId: {}", externalId);
                    return new InvalidXenditWebhookException("Payment not found for externalId: " + externalId);
                });
        log.debug("[handleXenditWebhook] Found payment: {}", payment);
        return payment;
    }

    private void applyStatusUpdate(Payment payment, String status) {
        if ("PAID".equalsIgnoreCase(status)) {
            var orderForValidation = orderRepository.findById(payment.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + payment.getOrderId()));
            var expectedTotal = orderForValidation.getTotalAmount();
            if (payment.getAmount() == null || payment.getAmount().compareTo(expectedTotal) != 0) {
                log.warn("[handleXenditWebhook] Amount mismatch for order {}. Payment: {}, Expected: {}",
                        orderForValidation.getId(), payment.getAmount(), expectedTotal);
                throw new InvalidPaymentAmountException("Paid amount does not match order total");
            }
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            log.debug("[handleXenditWebhook] Payment marked as PAID. PaymentId: {}", payment.getId());
        } else if ("EXPIRED".equalsIgnoreCase(status)) {
            payment.setPaymentStatus(PaymentStatus.EXPIRED);
            log.debug("[handleXenditWebhook] Payment marked as EXPIRED. PaymentId: {}", payment.getId());
        } else {
            log.debug("[handleXenditWebhook] Unknown payment status received: {}", status);
            throw new InvalidXenditWebhookException("Unknown payment status: " + status);
        }
    }

    private void safelyCreateShippingForPaidPayment(UUID externalId, Payment payment) {
        try {
            createShippingOrder(payment);
        } catch (Exception ex) {
            // Do not rethrow; payment update must not be rolled back due to shipping failure
            log.error("[handleXenditWebhook] Error while creating shipping for Payment externalId: {}. Error: {}", externalId, ex.getMessage(), ex);
        }
    }

    private void createShippingOrder(Payment payment) {
        // Fetch order and validate
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + payment.getOrderId()));
        log.debug("[handleXenditWebhook] Preparing shipping request for OrderId: {}", order.getId());

        if (isShippingSelectionMissing(order)) {
            log.debug("[handleXenditWebhook] Shipping courier not selected on order: {}. Skipping shipping creation.", order.getId());
            return; // do not throw, payment already processed
        }

        // Build items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems == null || orderItems.isEmpty()) {
            log.debug("[handleXenditWebhook] No order items found for order: {}. Skipping shipping creation.", order.getId());
            return;
        }
        List<OrderItemDTO> itemDTOs = buildOrderItemDTOs(orderItems);
        if (itemDTOs.isEmpty()) {
            log.debug("[handleXenditWebhook] No valid items to ship for order: {}. Skipping shipping creation.", order.getId());
            return;
        }

        // Destination (customer address)
        var addr = order.getShippingAddress();
        if (addr == null) {
            log.debug("[handleXenditWebhook] Shipping address is null for order: {}. Skipping shipping creation.", order.getId());
            return;
        }

        CoordinateDTO destinationCoordinate = buildCoordinate(addr.getLatitude(), addr.getLongitude());
        CoordinateDTO originCoordinate = buildCoordinate(shippingOriginProperties.getLatitude(), shippingOriginProperties.getLongitude());

        // Determine destination email from order user
        var destinationEmail = userRepository.findById(order.getUserId())
                .map(u -> u.getEmail())
                .orElse(null);

        CreateOrderRequestDTO.CreateOrderRequestDTOBuilder builder = CreateOrderRequestDTO.builder()
                .referenceId(order.getId().toString())
                // Shipper (optional)
                .shipperContactName(shippingOriginProperties.getContactName())
                .shipperContactPhone(shippingOriginProperties.getContactPhone())
                .shipperContactEmail(shippingOriginProperties.getContactEmail())
                .shipperOrganization(shippingOriginProperties.getOrganization())
                // Origin
                .originContactName(shippingOriginProperties.getContactName())
                .originContactPhone(shippingOriginProperties.getContactPhone())
                .originContactEmail(shippingOriginProperties.getContactEmail())
                .originAddress(shippingOriginProperties.getAddress())
                .originNote(shippingOriginProperties.getNote())
                .originPostalCode(shippingOriginProperties.getPostalCode())
                .originAreaId(shippingOriginProperties.getAreaId())
                .originLocationId(shippingOriginProperties.getLocationId())
                .originCollectionMethod(shippingOriginProperties.getCollectionMethod())
                .originCoordinate(originCoordinate)
                // Destination
                .destinationContactName(addr.getContactName())
                .destinationContactPhone(addr.getContactPhone())
                .destinationContactEmail(destinationEmail)
                .destinationAddress(addr.getAddressLine())
                .destinationPostalCode(addr.getPostalCode())
                .destinationAreaId(addr.getAreaId())
                .destinationLocationId(null)
                .destinationProofOfDelivery(null)
                .destinationProofOfDeliveryNote(null)
                .destinationCashOnDelivery(null)
                .destinationCashOnDeliveryType(null)
                .destinationCoordinate(destinationCoordinate)
                // Courier
                .courierCompany(order.getShippingCourierCode())
                .courierType(order.getShippingCourierService() != null ? order.getShippingCourierService().toLowerCase() : null)
                .courierInsurance(null)
                // Delivery
                .deliveryType("now")
                .orderNote(null);

        // Add items via singular builder method
        for (OrderItemDTO it : itemDTOs) {
            builder.item(it);
        }

        CreateOrderRequestDTO requestDTO = builder.build();
        log.debug("[handleXenditWebhook] Calling shippingService.createOrder for OrderId: {} with request: {}", order.getId(), requestDTO);
        CreateOrderResponseDTO response = shippingService.createOrder(requestDTO);
        if (response != null && response.isSuccess()) {
            String shippingOrderId = response.getId();
            String trackingId = response.getCourier() != null ? response.getCourier().getTrackingId() : null;
            String shippingStatus = response.getStatus();

            order.setShippingOrderId(shippingOrderId);
            order.setTrackingId(trackingId);
            order.setShippingStatus(shippingStatus);
            orderRepository.save(order);
            log.debug("[handleXenditWebhook] Shipping order created for OrderId: {}. ShippingOrderId: {}, TrackingId: {}", order.getId(), shippingOrderId, trackingId);
        } else {
            log.error("[handleXenditWebhook] Failed to create shipping order for OrderId: {}. Response: {}", order.getId(), response);
        }
    }

    private boolean isShippingSelectionMissing(Order order) {
        return order.getShippingCourierCode() == null || order.getShippingCourierCode().isBlank()
                || order.getShippingCourierService() == null || order.getShippingCourierService().isBlank();
    }

    private List<OrderItemDTO> buildOrderItemDTOs(List<OrderItem> orderItems) {
        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        for (OrderItem oi : orderItems) {
            Product product = productRepository.findById(oi.getProductId()).orElse(null);
            if (product == null) {
                log.debug("[handleXenditWebhook] Product not found for productId: {}. Skipping this item.", oi.getProductId());
                continue;
            }
            Integer value = oi.getPricePerUnit() != null ? oi.getPricePerUnit().setScale(0, RoundingMode.HALF_UP).intValue() : null;
            OrderItemDTO item = OrderItemDTO.builder()
                    .name(product.getName())
                    .description(product.getDescription())
                    // Category is optional in Biteship; omit to let it default to 'others'
                    .category(null)
                    .sku(product.getSku())
                    .value(value)
                    .quantity(oi.getQuantity())
                    .weight(product.getWeight())
                    .height(product.getHeight())
                    .length(product.getLength())
                    .width(product.getWidth())
                    .build();
            itemDTOs.add(item);
        }
        return itemDTOs;
    }

    private CoordinateDTO buildCoordinate(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return CoordinateDTO.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
