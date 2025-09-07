package com.sahaja.swalayan.ecommerce.domain.model.notification;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_events", indexes = {
        @Index(name = "idx_ne_created_at", columnList = "created_at"),
        @Index(name = "idx_ne_audience_created_at", columnList = "audience, created_at"),
        @Index(name = "idx_ne_user_created_at", columnList = "user_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Either a specific userId, or a broadcast audience (e.g., "admins").
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "audience", length = 50)
    private String audience; // e.g., "admins"

    @Column(name = "app", length = 50)
    private String app; // e.g., "sahaja-admin", "sahaja-shop"

    @Column(name = "type", length = 100, nullable = false)
    private String type; // e.g., order_paid_awaiting_pickup, shipping_job_update

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "body", length = 500)
    private String body;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(name = "data", columnDefinition = "text")
    private String data; // JSON string with additional fields

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
