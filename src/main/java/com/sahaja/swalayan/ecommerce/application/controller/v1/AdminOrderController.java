package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.model.order.Status;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.OrderJpaRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderJpaRepository orderRepo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction dir = (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortField));

        LocalDateTime fromDt = null; LocalDateTime toDt = null;
        if (from != null && !from.isBlank()) fromDt = LocalDate.parse(from).atStartOfDay();
        if (to != null && !to.isBlank()) toDt = LocalDate.parse(to).atTime(LocalTime.MAX);

        Page<Order> result;
        if (status != null && fromDt != null && toDt != null) {
            result = orderRepo.findAllByStatusAndOrderDateBetween(status, fromDt, toDt, pageable);
        } else if (status != null) {
            result = orderRepo.findAllByStatus(status, pageable);
        } else if (fromDt != null && toDt != null) {
            result = orderRepo.findAllByOrderDateBetween(fromDt, toDt, pageable);
        } else {
            result = orderRepo.findAll(pageable);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("content", result.getContent());
        data.put("page", result.getNumber());
        data.put("size", result.getSize());
        data.put("totalElements", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> details(@PathVariable UUID id) {
        Optional<Order> order = orderRepo.findById(id);
        return order
                .map(o -> ResponseEntity.ok(ApiResponse.success("OK", o)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Order not found")));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> updateStatus(@PathVariable UUID id, @RequestBody UpdateStatusRequest req) {
        Optional<Order> orderOpt = orderRepo.findById(id);
        if (orderOpt.isEmpty()) return ResponseEntity.status(404).body(ApiResponse.error("Order not found"));
        Order order = orderOpt.get();
        order.setStatus(req.status);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepo.save(order);
        return ResponseEntity.ok(ApiResponse.success("Status updated", order));
    }

    @Data
    public static class UpdateStatusRequest {
        private Status status;
    }
}
