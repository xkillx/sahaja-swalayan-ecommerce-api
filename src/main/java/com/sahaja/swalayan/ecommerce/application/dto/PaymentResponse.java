package com.sahaja.swalayan.ecommerce.application.dto;

import com.sahaja.swalayan.ecommerce.domain.model.order.PaymentStatus;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private UUID paymentId;
    private UUID orderId;
    private UUID externalId;
    private PaymentStatus paymentStatus;
    private String xenditInvoiceUrl;
}

