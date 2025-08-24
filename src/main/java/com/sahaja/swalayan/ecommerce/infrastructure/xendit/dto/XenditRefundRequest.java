package com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XenditRefundRequest {
    private BigDecimal amount; // required
    private String externalId; // optional idempotency identifier on provider side (in addition to header)
    private String reason;     // optional note
}