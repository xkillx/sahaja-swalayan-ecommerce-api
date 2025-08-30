package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.notification.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, UUID> {
    Optional<NotificationToken> findByToken(String token);
    List<NotificationToken> findAllByAppAndRevokedFalse(String app);
    List<NotificationToken> findAllByUserIdAndRevokedFalse(UUID userId);
}
