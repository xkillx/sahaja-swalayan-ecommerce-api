package com.sahaja.swalayan.ecommerce.domain.model.notification;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_tokens", indexes = {
        @Index(name = "idx_nt_token", columnList = "token", unique = true),
        @Index(name = "idx_nt_user", columnList = "user_id"),
        @Index(name = "idx_nt_app_revoked", columnList = "app, revoked")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId; // nullable; can be anonymous device

    @Column(name = "token", nullable = false, length = 512, unique = true)
    private String token;

    @Column(name = "platform", length = 50)
    private String platform; // web/android/ios

    @Column(name = "app", length = 50)
    private String app; // sahaja-admin / sahaja-shop

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "locale", length = 20)
    private String locale;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (lastSeenAt == null) lastSeenAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
