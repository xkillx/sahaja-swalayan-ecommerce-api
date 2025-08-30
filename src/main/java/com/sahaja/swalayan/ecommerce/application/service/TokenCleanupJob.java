package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.infrastructure.repository.NotificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final NotificationTokenRepository tokenRepo;

    // Run daily at 03:30 to cleanup revoked tokens older than 30 days
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void cleanupRevoked() {
        try {
            var all = tokenRepo.findAll();
            LocalDateTime threshold = LocalDateTime.now().minusDays(30);
            int removed = 0;
            for (var t : all) {
                try {
                    if (t.isRevoked() && t.getUpdatedAt() != null && t.getUpdatedAt().isBefore(threshold)) {
                        tokenRepo.delete(t);
                        removed++;
                    }
                } catch (Exception ignore) {}
            }
            if (removed > 0) log.info("TokenCleanupJob: removed {} old revoked tokens", removed);
        } catch (Exception e) {
            log.warn("TokenCleanupJob failed: {}", e.getMessage());
        }
    }
}
