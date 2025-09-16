package com.sahaja.swalayan.ecommerce.domain.model.coupon;

import com.sahaja.swalayan.ecommerce.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DiscountType type; // PERCENT or FIXED

    @Column(nullable = false)
    private BigDecimal value; // percent: 0-100, fixed: amount in currency

    @Column
    private BigDecimal minSpend; // optional minimal subtotal requirement

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public enum DiscountType { PERCENT, FIXED }
}
