package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.model.order.Payment;
import com.sahaja.swalayan.ecommerce.domain.model.order.PaymentStatus;
import com.sahaja.swalayan.ecommerce.domain.model.order.RefundJob;
import com.sahaja.swalayan.ecommerce.domain.model.order.Status;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.OrderJpaRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.OrderRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.PaymentRepository;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.RefundJobRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefundJobWorker {

    private final RefundJobRepository refundJobRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final com.sahaja.swalayan.ecommerce.infrastructure.xendit.XenditRefundClient xenditRefundClient;
    private final com.sahaja.swalayan.ecommerce.infrastructure.config.XenditProperties xenditProperties;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final MeterRegistry meterRegistry;

    // Process refund jobs every 30 seconds
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void process() {
        List<RefundJob> due = refundJobRepository.findDue(LocalDateTime.now(), RefundJob.RefundJobStatus.PENDING);
        for (RefundJob job : due) {
            try {
                job.setStatus(RefundJob.RefundJobStatus.IN_PROGRESS);
                job.setLastError(null);
                refundJobRepository.save(job);

                // Minimal refund processor: find the latest PAID payment for the order
                Optional<Payment> paidOpt = paymentRepository.findByOrderId(job.getOrderId()).stream()
                        .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                        .max(Comparator.comparing(Payment::getCreatedAt));

                if (paidOpt.isEmpty()) {
                    throw new IllegalStateException("No PAID payment found for order " + job.getOrderId());
                }

                Payment payment = paidOpt.get();
                // Idempotency: if already refunded, mark job as succeeded
                if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
                    job.setStatus(RefundJob.RefundJobStatus.SUCCEEDED);
                    refundJobRepository.save(job);
                    continue;
                }

                // Perform REAL refund via Xendit
                // Preconditions: find latest PAID payment and use its invoice ID (xenditCallbackToken)
                String invoiceId = payment.getXenditCallbackToken();
                if (invoiceId == null || invoiceId.isBlank()) {
                    throw new IllegalStateException("Missing invoice id (xenditCallbackToken) on payment");
                }
                java.math.BigDecimal amount = job.getAmount();
                if (amount == null || amount.signum() <= 0) {
                    throw new IllegalArgumentException("Invalid refund amount");
                }
                // Build refund request
                var req = com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto.XenditRefundRequest.builder()
                        .amount(amount)
                        .externalId("refund-" + job.getId())
                        .reason(job.getReason())
                        .build();
                String idempotencyKey = "refund-" + job.getOrderId();
                var resp = xenditRefundClient.createRefund(invoiceId, req, idempotencyKey);
                if (resp == null || resp.getId() == null) {
                    throw new IllegalStateException("Refund failed: empty response");
                }

                // Mark payment and order as refunded
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);

                Order order = orderRepository.findById(job.getOrderId()).orElse(null);
                if (order != null) {
                    order.setStatus(Status.REFUNDED);
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                }

                job.setStatus(RefundJob.RefundJobStatus.SUCCEEDED);
                refundJobRepository.save(job);
                try { meterRegistry.counter("jobs.refund", "status", job.getStatus().name()).increment(); } catch (Exception ignore) {}
                try { notificationService.notifyRefundJobUpdate(job.getOrderId(), job.getStatus().name(), "Refund succeeded"); } catch (Exception ignore) {}
                try { pushNotificationService.sendRefundUpdateToAdmins(job.getOrderId(), job.getStatus().name(), "Refund succeeded"); } catch (Exception ignore) {}
                log.debug("[refund-job] Succeeded for order {}", job.getOrderId());
            } catch (org.springframework.web.client.HttpStatusCodeException httpEx) {
                // Classify retryable vs non-retryable based on status
                int status = httpEx.getStatusCode().value();
                boolean retryable = status >= 500 || status == 429; // transient
                int attempts = job.getAttempts() + 1;
                job.setAttempts(attempts);
                job.setLastError("HTTP " + status + ": " + httpEx.getResponseBodyAsString());
                if (retryable && attempts < 7) {
                    job.setStatus(RefundJob.RefundJobStatus.PENDING);
                    job.setNextRunAt(nextBackoff(attempts));
                } else {
                    job.setStatus(RefundJob.RefundJobStatus.FAILED);
                }
                refundJobRepository.save(job);
                try { meterRegistry.counter("jobs.refund", "status", job.getStatus().name()).increment(); } catch (Exception ignore) {}
                try { notificationService.notifyRefundJobUpdate(job.getOrderId(), job.getStatus().name(), job.getLastError()); } catch (Exception ignore) {}
                try { pushNotificationService.sendRefundUpdateToAdmins(job.getOrderId(), job.getStatus().name(), job.getLastError()); } catch (Exception ignore) {}
                log.warn("[refund-job] HTTP failure for order {}: {}", job.getOrderId(), httpEx.getMessage());
            } catch (Exception ex) {
                // Non-http errors: retry unless clearly a validation bug
                boolean retryable = !(ex instanceof IllegalArgumentException || ex instanceof IllegalStateException);
                int attempts = job.getAttempts() + 1;
                job.setAttempts(attempts);
                job.setLastError(ex.getMessage());
                if (retryable && attempts < 7) {
                    job.setStatus(RefundJob.RefundJobStatus.PENDING);
                    job.setNextRunAt(nextBackoff(attempts));
                } else {
                    job.setStatus(RefundJob.RefundJobStatus.FAILED);
                }
                refundJobRepository.save(job);
                try { meterRegistry.counter("jobs.refund", "status", job.getStatus().name()).increment(); } catch (Exception ignore) {}
                try { notificationService.notifyRefundJobUpdate(job.getOrderId(), job.getStatus().name(), job.getLastError()); } catch (Exception ignore) {}
                try { pushNotificationService.sendRefundUpdateToAdmins(job.getOrderId(), job.getStatus().name(), job.getLastError()); } catch (Exception ignore) {}
                log.warn("[refund-job] Failed for order {}: {}", job.getOrderId(), ex.getMessage());
            }
        }
    }

    private LocalDateTime nextBackoff(int attempts) {
        switch (attempts) {
            case 1: return LocalDateTime.now().plusMinutes(1);
            case 2: return LocalDateTime.now().plusMinutes(5);
            case 3: return LocalDateTime.now().plusMinutes(15);
            case 4: return LocalDateTime.now().plusHours(1);
            case 5: return LocalDateTime.now().plusHours(6);
            case 6: return LocalDateTime.now().plusHours(12);
            default: return LocalDateTime.now().plusDays(1);
        }
    }
}
