package com.sahaja.swalayan.ecommerce.domain.model.order;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payments_external_id", columnNames = {"external_id"}),
        @UniqueConstraint(name = "uk_payments_xendit_callback_token", columnNames = {"xendit_callback_token"})
    },
    indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "external_id", nullable = false, unique = true)
    private UUID externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "xendit_invoice_url", nullable = false, length = 500)
    private String xenditInvoiceUrl;

    @Column(name = "xendit_callback_token", nullable = false, unique = true, length = 100)
    private String xenditCallbackToken;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
