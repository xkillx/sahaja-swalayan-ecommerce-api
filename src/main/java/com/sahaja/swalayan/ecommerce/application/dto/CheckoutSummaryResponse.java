package com.sahaja.swalayan.ecommerce.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSummaryResponse {
    private OrderDTO order;

    @JsonProperty("subtotal")
    private BigDecimal subtotal; // Sum of order items (already stored in order.totalAmount)

    @JsonProperty("shipping_cost")
    private BigDecimal shippingCost; // From order.shippingCost

    @JsonProperty("grand_total")
    private BigDecimal grandTotal; // subtotal + shippingCost

    private List<PaymentResponse> payments; // All payments created for this order (if any)
}
