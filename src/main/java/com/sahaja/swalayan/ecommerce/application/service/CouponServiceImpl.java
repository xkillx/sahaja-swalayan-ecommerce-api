package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.common.EntityNotFoundException;
import com.sahaja.swalayan.ecommerce.domain.model.coupon.Coupon;
import com.sahaja.swalayan.ecommerce.domain.service.CouponService;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponJpaRepository repo;

    @Override
    public Coupon create(Coupon coupon) { return repo.save(coupon); }

    @Override
    public List<Coupon> list() { return repo.findAll(); }

    @Override
    public Coupon findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Coupon not found: " + id));
    }

    @Override
    public Coupon findByCode(String code) {
        return repo.findByCodeIgnoreCase(code).orElseThrow(() -> new EntityNotFoundException("Coupon not found: " + code));
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null || !coupon.isActive() || subtotal == null) return BigDecimal.ZERO;
        if (coupon.getMinSpend() != null && subtotal.compareTo(coupon.getMinSpend()) < 0) return BigDecimal.ZERO;
        BigDecimal discount;
        if (coupon.getType() == Coupon.DiscountType.PERCENT) {
            BigDecimal pct = coupon.getValue() == null ? BigDecimal.ZERO : coupon.getValue();
            discount = subtotal.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getValue() == null ? BigDecimal.ZERO : coupon.getValue();
        }
        if (discount.compareTo(subtotal) > 0) discount = subtotal; // cap
        return discount.max(BigDecimal.ZERO);
    }
}
