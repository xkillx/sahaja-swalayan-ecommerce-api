package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.application.dto.PaymentRequest;
import com.sahaja.swalayan.ecommerce.application.dto.PaymentResponse;
import com.sahaja.swalayan.ecommerce.application.dto.XenditWebhookPayload;
import com.sahaja.swalayan.ecommerce.common.InvalidOrderStateException;
import com.sahaja.swalayan.ecommerce.common.InvalidXenditWebhookException;
import com.sahaja.swalayan.ecommerce.common.OrderNotFoundException;
import com.sahaja.swalayan.ecommerce.common.PaymentNotFoundException;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto.XenditCreateInvoiceRequest;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto.XenditCreateInvoiceResponse;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.XenditInvoiceClient;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.XenditInvoiceCreationException;
import com.sahaja.swalayan.ecommerce.domain.model.order.Payment;
import com.sahaja.swalayan.ecommerce.domain.model.order.PaymentStatus;
import com.sahaja.swalayan.ecommerce.domain.model.order.Status;
import com.sahaja.swalayan.ecommerce.domain.repository.OrderRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.PaymentRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.domain.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
                .paymentMethod(request.getPaymentMethod())
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
        if (payload == null || payload.getExternalId() == null || payload.getStatus() == null) {
            log.debug("[handleXenditWebhook] Invalid webhook payload: {}", payload);
            throw new InvalidXenditWebhookException("Invalid webhook payload");
        }
        UUID externalId;
        try {
            externalId = UUID.fromString(payload.getExternalId());
            log.debug("[handleXenditWebhook] Parsed externalId: {}", externalId);
        } catch (Exception e) {
            log.debug("[handleXenditWebhook] Invalid external_id format: {}", payload.getExternalId());
            throw new InvalidXenditWebhookException("Invalid external_id format", e);
        }
        String status = payload.getStatus();
        log.debug("[handleXenditWebhook] Processing status: {} for externalId: {}", status, externalId);

        Payment payment = paymentRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    log.debug("[handleXenditWebhook] Payment not found for externalId: {}", externalId);
                    return new InvalidXenditWebhookException("Payment not found for externalId: " + externalId);
                });
        log.debug("[handleXenditWebhook] Found payment: {}", payment);
        if ("PAID".equalsIgnoreCase(status)) {
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
        paymentRepository.save(payment);
        log.debug("[handleXenditWebhook] Payment updated and saved. PaymentId: {}, Status: {}", payment.getId(), payment.getPaymentStatus());
    }
}
