package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.domain.model.coupon.Coupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CouponService {
    Coupon create(Coupon coupon);
    List<Coupon> list();
    Coupon findById(UUID id);
    Coupon findByCode(String code);
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal);
}
