package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.OrderJpaRepository;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderJpaRepository orderRepo;
    private final UserJpaRepository userRepo;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> overview(
            @RequestParam(value = "days", defaultValue = "7") int days,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        if (days <= 0) days = 7;
        LocalDate to = (toDate != null) ? toDate : LocalDate.now();
        LocalDate from = to.minusDays(days - 1);
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        BigDecimal revenue = orderRepo.sumRevenueBetween(fromDt, toDt);
        long orders = orderRepo.countByOrderDateBetween(fromDt, toDt);
        long newUsers = userRepo.countByCreatedAtBetween(fromDt, toDt);

        List<Object[]> rows = orderRepo.dailyRevenue(fromDt, toDt);
        Map<LocalDate, BigDecimal> byDate = new HashMap<>();
        for (Object[] r : rows) {
            LocalDate d = (LocalDate) r[0];
            BigDecimal sum = (BigDecimal) r[1];
            byDate.put(d, sum);
        }
        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            BigDecimal v = byDate.getOrDefault(d, BigDecimal.ZERO);
            Map<String, Object> pt = new HashMap<>();
            pt.put("date", d.toString());
            pt.put("revenue", v);
            series.add(pt);
        }

        var latest = orderRepo.findTop5ByOrderByCreatedAtDesc();
        List<Map<String, Object>> latestOrders = new ArrayList<>();
        for (var o : latest) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("createdAt", o.getCreatedAt());
            m.put("orderDate", o.getOrderDate());
            m.put("totalAmount", o.getTotalAmount());
            m.put("status", o.getStatus());
            m.put("userId", o.getUserId());
            latestOrders.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("kpis", Map.of(
                "revenueLastDays", revenue,
                "ordersLastDays", orders,
                "newUsersLastDays", newUsers
        ));
        data.put("dailyRevenue", series);
        data.put("latestOrders", latestOrders);

        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }
}
