package com.sahaja.swalayan.ecommerce.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahaja.swalayan.ecommerce.domain.model.notification.NotificationEvent;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.NotificationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight in-app notification service using Server-Sent Events (SSE).
 * - Admin channel: broadcast important operational events to all connected admins
 * - User channel: per-user event stream by userId
 *
 * Notes:
 * - SSE delivery is in-memory/ephemeral; we also persist key events for a simple feed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    public static final long DEFAULT_TIMEOUT_MS = 0L; // never timeout (client controls lifecycle)

    private final NotificationEventRepository eventRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Admin subscribers (multiple tabs)
    private final List<SseEmitter> adminEmitters = new CopyOnWriteArrayList<>();

    // User subscribers
    private final Map<UUID, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeAdmin() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        adminEmitters.add(emitter);
        emitter.onCompletion(() -> adminEmitters.remove(emitter));
        emitter.onTimeout(() -> adminEmitters.remove(emitter));
        emitter.onError(e -> adminEmitters.remove(emitter));
        // Send initial ping
        trySend(emitter, SseEmitter.event().name("ping").data("connected:" + Instant.now().toString()));
        return emitter;
    }

    public SseEmitter subscribeUser(UUID userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeUserEmitter(userId, emitter));
        emitter.onTimeout(() -> removeUserEmitter(userId, emitter));
        emitter.onError(e -> removeUserEmitter(userId, emitter));
        trySend(emitter, SseEmitter.event().name("ping").data("connected:" + Instant.now().toString()));
        return emitter;
    }

    private void removeUserEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> list = userEmitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) userEmitters.remove(userId);
        }
    }

    private void trySend(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException e) {
            try { emitter.complete(); } catch (Exception ignore) {}
        } catch (IllegalStateException e) {
            try { emitter.complete(); } catch (Exception ignore) {}
        }
    }

    private void broadcast(List<SseEmitter> targets, String event, Object data) {
        for (SseEmitter emitter : targets) {
            trySend(emitter, SseEmitter.event().name(event).data(data));
        }
    }

    public void notifyAdmins(String event, Object payload) {
        if (!adminEmitters.isEmpty()) {
            broadcast(adminEmitters, event, payload);
        }
    }

    public void notifyUser(UUID userId, String event, Object payload) {
        List<SseEmitter> targets = userEmitters.get(userId);
        if (targets != null && !targets.isEmpty()) {
            broadcast(targets, event, payload);
        }
    }

    private void persistAdmin(String type, String title, String body, Map<?, ?> data) {
        try {
            String json = data != null ? objectMapper.writeValueAsString(data) : null;
            NotificationEvent ev = NotificationEvent.builder()
                    .audience("admins")
                    .app("sahaja-admin")
                    .type(type)
                    .title(title)
                    .body(body)
                    .data(json)
                    .build();
            eventRepo.save(ev);
        } catch (JsonProcessingException e) {
            // ignore persistence errors
        } catch (Exception e) {
            // ignore
        }
    }

    // Convenience helpers for our common events
    public void notifyOrderPaidAwaitingPickup(UUID orderId, UUID userId) {
        var payload = Map.of(
                "type", "order_paid_awaiting_pickup",
                "orderId", orderId.toString(),
                "userId", userId != null ? userId.toString() : null,
                "ts", Instant.now().toString()
        );
        notifyAdmins("order", payload);
        persistAdmin("order_paid_awaiting_pickup", "Order paid", "Order " + orderId + " awaiting pickup request", payload);
        if (userId != null) notifyUser(userId, "order", payload);
    }

    public void notifyShippingJobUpdate(UUID orderId, String status, String message) {
        var payload = new java.util.HashMap<String, Object>();
        payload.put("type", "shipping_job_update");
        payload.put("orderId", orderId.toString());
        if (status != null) payload.put("status", status);
        if (message != null) payload.put("message", message);
        payload.put("ts", Instant.now().toString());
        notifyAdmins("shipping", payload);
        persistAdmin("shipping_job_update", "Shipping update", "Order " + orderId + ": " + (status != null ? status : "update"), payload);
    }
}
