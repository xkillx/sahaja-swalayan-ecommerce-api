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
public class XenditRefundResponse {
    private String id;
    private String status; // e.g., PENDING, SUCCEEDED, FAILED
    private BigDecimal amount;
}