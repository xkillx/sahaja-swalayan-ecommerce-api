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
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderJpaRepository orderRepo;
    private final com.sahaja.swalayan.ecommerce.infrastructure.repository.RefundJobRepository refundJobRepository;
    private final com.sahaja.swalayan.ecommerce.domain.repository.PaymentRepository paymentRepository;
    private final com.sahaja.swalayan.ecommerce.infrastructure.repository.ShippingJobRepository shippingJobRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) UUID userId
    ) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction dir = (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortField));

        LocalDateTime fromDt = null; LocalDateTime toDt = null;
        if (from != null && !from.isBlank()) fromDt = LocalDate.parse(from).atStartOfDay();
        if (to != null && !to.isBlank()) toDt = LocalDate.parse(to).atTime(LocalTime.MAX);

        Page<Order> result;
        if (userId != null) {
            // Prioritize user filter if provided (simple case)
            result = orderRepo.findAllByUserId(userId, pageable);
        } else if (status != null && fromDt != null && toDt != null) {
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

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String,Object>>> requestRefund(@PathVariable UUID id, @RequestBody RefundRequest req) {
        Optional<Order> orderOpt = orderRepo.findById(id);
        if (orderOpt.isEmpty()) return ResponseEntity.status(404).body(ApiResponse.error("Order not found"));
        Order order = orderOpt.get();
        if (req == null || req.amount == null || req.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid refund amount"));
        }
        // Mark latest PAID payment as REFUND_REQUESTED (if any)
        paymentRepository.findByOrderId(order.getId()).stream()
                .max(Comparator.comparing(com.sahaja.swalayan.ecommerce.domain.model.order.Payment::getCreatedAt))
                .ifPresent(p -> {
                    if (p.getPaymentStatus() != com.sahaja.swalayan.ecommerce.domain.model.order.PaymentStatus.REFUNDED) {
                        p.setPaymentStatus(com.sahaja.swalayan.ecommerce.domain.model.order.PaymentStatus.REFUND_REQUESTED);
                        paymentRepository.save(p);
                    }
                });
        // We keep order status unchanged here; RefundJobWorker will set REFUNDED upon success
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);
        // Enqueue refund job
        var job = com.sahaja.swalayan.ecommerce.domain.model.order.RefundJob.builder()
                .orderId(order.getId())
                .amount(req.amount)
                .reason(req.reason)
                .status(com.sahaja.swalayan.ecommerce.domain.model.order.RefundJob.RefundJobStatus.PENDING)
                .attempts(0)
                .lastError(null)
                .nextRunAt(LocalDateTime.now())
                .build();
        job = refundJobRepository.save(job);
        Map<String,Object> data = new HashMap<>();
        data.put("jobId", job.getId());
        data.put("status", job.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Refund enqueued", data));
    }

    @PostMapping("/{id}/refund/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String,Object>>> retryRefund(@PathVariable UUID id) {
        var list = refundJobRepository.findByOrderIdOrderByCreatedAtDesc(id);
        if (list == null || list.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("No refund job found for order"));
        }
        var job = list.get(0);
        job.setStatus(com.sahaja.swalayan.ecommerce.domain.model.order.RefundJob.RefundJobStatus.PENDING);
        job.setNextRunAt(LocalDateTime.now());
        job.setLastError(null);
        job.setAttempts(Math.max(0, job.getAttempts() - 1));
        refundJobRepository.save(job);
        Map<String,Object> data = new HashMap<>();
        data.put("jobId", job.getId());
        data.put("status", job.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Refund retry scheduled", data));
    }

    @PostMapping("/{id}/shipping/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String,Object>>> retryShipping(@PathVariable UUID id) {
        var list = new java.util.ArrayList<>(shippingJobRepository.findByOrderIdOrderByCreatedAtDesc(id));
        if (list.isEmpty()) {
            // create a new job if none exists
            var job = com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob.builder()
                    .orderId(id)
                    .type(com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob.ShippingJobType.CREATE_ORDER)
                    .status(com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob.ShippingJobStatus.PENDING)
                    .attempts(0)
                    .lastError(null)
                    .nextRunAt(LocalDateTime.now())
                    .build();
            job = shippingJobRepository.save(job);
            Map<String,Object> data = new HashMap<>();
            data.put("jobId", job.getId());
            data.put("status", job.getStatus());
            return ResponseEntity.ok(ApiResponse.success("Shipping retry scheduled", data));
        }
        var job = list.get(0);
        job.setStatus(com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob.ShippingJobStatus.PENDING);
        job.setNextRunAt(LocalDateTime.now());
        job.setLastError(null);
        job.setAttempts(Math.max(0, job.getAttempts() - 1));
        shippingJobRepository.save(job);
        Map<String,Object> data = new HashMap<>();
        data.put("jobId", job.getId());
        data.put("status", job.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Shipping retry scheduled", data));
    }

    @PostMapping("/{id}/shipping/request-pickup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String,Object>>> requestPickup(@PathVariable UUID id) {
        var orderOpt = orderRepo.findById(id);
        if (orderOpt.isEmpty()) return ResponseEntity.status(404).body(ApiResponse.error("Order not found"));
        var order = orderOpt.get();
        if (order.getShippingOrderId() != null && !order.getShippingOrderId().isBlank()) {
            return ResponseEntity.status(409).body(ApiResponse.error("Shipping already created for this order"));
        }
        if (order.getShippingCourierCode() == null || order.getShippingCourierCode().isBlank() ||
            order.getShippingCourierService() == null || order.getShippingCourierService().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Courier selection missing on order"));
        }
        // Enqueue shipping job (CREATE_ORDER)
        var job = com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob.builder()
                .orderId(order.getId())
                .type(com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob.ShippingJobType.CREATE_ORDER)
                .status(com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob.ShippingJobStatus.PENDING)
                .attempts(0)
                .lastError(null)
                .nextRunAt(LocalDateTime.now())
                .build();
        job = shippingJobRepository.save(job);
        // Mark order shipping status as queued for pickup
        order.setShippingStatus("pickup_requested");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);
        Map<String,Object> data = new HashMap<>();
        data.put("jobId", job.getId());
        data.put("status", job.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Pickup requested and shipping job enqueued", data));
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String,Object>>> timeline(@PathVariable UUID id) {
        var result = new HashMap<String, Object>();
        var refundJobs = refundJobRepository.findByOrderIdOrderByCreatedAtDesc(id);
        var shippingJobs = shippingJobRepository.findByOrderIdOrderByCreatedAtDesc(id);
        result.put("refundJobs", refundJobs);
        result.put("shippingJobs", shippingJobs);
        // expose latest statuses for quick UI badges
        var latestRefund = (refundJobs == null || refundJobs.isEmpty()) ? null : refundJobs.get(0);
        var latestShipping = (shippingJobs == null || shippingJobs.isEmpty()) ? null : shippingJobs.get(0);
        result.put("refundJobStatus", latestRefund != null ? latestRefund.getStatus() : null);
        result.put("shippingJobStatus", latestShipping != null ? latestShipping.getStatus() : null);
        return ResponseEntity.ok(ApiResponse.success("OK", result));
    }

    @Data
    public static class UpdateStatusRequest {
        private Status status;
    }

    @Data
    public static class RefundRequest {
        public BigDecimal amount;
        public String reason;
    }
}
