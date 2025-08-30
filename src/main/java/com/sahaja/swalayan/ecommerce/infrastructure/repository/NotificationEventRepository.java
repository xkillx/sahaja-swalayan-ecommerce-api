package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.notification.NotificationEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {
    List<NotificationEvent> findAllByAudienceOrderByCreatedAtDesc(String audience, Pageable pageable);
    List<NotificationEvent> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
