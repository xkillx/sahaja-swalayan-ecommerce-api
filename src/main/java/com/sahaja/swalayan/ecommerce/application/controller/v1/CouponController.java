package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.domain.model.coupon.Coupon;
import com.sahaja.swalayan.ecommerce.domain.service.CouponService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // Admin endpoints
    @GetMapping("/admin/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Coupon>>> list() {
        return ResponseEntity.ok(ApiResponse.success("OK", couponService.list()));
    }

    @PostMapping("/admin/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Coupon>> create(@Valid @RequestBody CreateCouponRequest req) {
        var c = Coupon.builder()
                .code(req.getCode().trim())
                .type(req.getType())
                .value(req.getValue())
                .minSpend(req.getMinSpend())
                .active(req.isActive())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Created", couponService.create(c)));
    }

    // Public validation
    @PostMapping("/coupons/validate")
    public ResponseEntity<ApiResponse<Map<String,Object>>> validate(@RequestBody ValidateRequest req) {
        var coupon = couponService.findByCode(req.getCode());
        var subtotal = req.getSubtotal() == null ? BigDecimal.ZERO : req.getSubtotal();
        var discount = couponService.calculateDiscount(coupon, subtotal);
        Map<String,Object> data = new HashMap<>();
        data.put("code", coupon.getCode());
        data.put("type", coupon.getType());
        data.put("value", coupon.getValue());
        data.put("minSpend", coupon.getMinSpend());
        data.put("discount", discount);
        data.put("totalAfterDiscount", subtotal.subtract(discount));
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @Data
    public static class CreateCouponRequest {
        private String code;
        private Coupon.DiscountType type;
        private BigDecimal value;
        private BigDecimal minSpend;
        private boolean active = true;
    }

    @Data
    public static class ValidateRequest {
        private String code;
        private BigDecimal subtotal;
    }
}
