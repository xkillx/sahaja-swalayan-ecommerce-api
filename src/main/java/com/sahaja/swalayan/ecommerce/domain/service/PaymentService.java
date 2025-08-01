package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.application.dto.PaymentRequest;
import com.sahaja.swalayan.ecommerce.application.dto.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse getPayment(UUID id);
    List<PaymentResponse> getByOrderId(UUID orderId);
    void handleXenditWebhook(com.sahaja.swalayan.ecommerce.application.dto.XenditWebhookPayload payload);
}