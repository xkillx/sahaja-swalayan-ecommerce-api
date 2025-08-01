package com.sahaja.swalayan.ecommerce.domain.model.order;

import com.sahaja.swalayan.ecommerce.common.InvalidPaymentMethodException;

public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    E_WALLET,
    QR_CODE,
    VIRTUAL_ACCOUNT,
    RETAIL_OUTLET,
    DIRECT_DEBIT,
    PAYLATER, BANK_TRANSFER;

    public static PaymentMethod fromString(String value) {
        if (value == null) throw new InvalidPaymentMethodException("Payment method is required");
        String normalized = value.trim().replace(" ", "_").replace("-", "_").toUpperCase();
        for (PaymentMethod pm : PaymentMethod.values()) {
            if (pm.name().equals(normalized)) {
                return pm;
            }
        }
        throw new InvalidPaymentMethodException("Invalid payment method: " + value);
    }
}
