package com.sahaja.swalayan.ecommerce.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahaja.swalayan.ecommerce.domain.model.notification.NotificationEvent;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.NotificationEventRepository;
import io.micrometer.core.instrument.MeterRegistry;
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
import java.util.concurrent.atomic.AtomicInteger;

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
    private final MeterRegistry meterRegistry;
    private final AtomicInteger adminOpen = new AtomicInteger(0);
    private final AtomicInteger userOpen = new AtomicInteger(0);

    // Admin subscribers (multiple tabs)
    private final List<SseEmitter> adminEmitters = new CopyOnWriteArrayList<>();

    // User subscribers
    private final Map<UUID, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeAdmin() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        adminEmitters.add(emitter);
        adminOpen.incrementAndGet();
        meterRegistry.counter("sse.connections.open", "channel", "admin").increment();
        emitter.onCompletion(() -> {
            adminEmitters.remove(emitter);
            adminOpen.decrementAndGet();
            meterRegistry.counter("sse.connections.closed", "channel", "admin").increment();
        });
        emitter.onTimeout(() -> {
            adminEmitters.remove(emitter);
            adminOpen.decrementAndGet();
            meterRegistry.counter("sse.connections.timeout", "channel", "admin").increment();
        });
        emitter.onError(e -> {
            adminEmitters.remove(emitter);
            adminOpen.decrementAndGet();
            meterRegistry.counter("sse.connections.error", "channel", "admin").increment();
        });
        // Send initial ping
        trySend(emitter, SseEmitter.event().name("ping").data("connected:" + Instant.now().toString()));
        return emitter;
    }

    public SseEmitter subscribeUser(UUID userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        userOpen.incrementAndGet();
        meterRegistry.counter("sse.connections.open", "channel", "user").increment();
        emitter.onCompletion(() -> {
            removeUserEmitter(userId, emitter);
            userOpen.decrementAndGet();
            meterRegistry.counter("sse.connections.closed", "channel", "user").increment();
        });
        emitter.onTimeout(() -> {
            removeUserEmitter(userId, emitter);
            userOpen.decrementAndGet();
            meterRegistry.counter("sse.connections.timeout", "channel", "user").increment();
        });
        emitter.onError(e -> {
            removeUserEmitter(userId, emitter);
            userOpen.decrementAndGet();
            meterRegistry.counter("sse.connections.error", "channel", "user").increment();
        });
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
            try { meterRegistry.counter("sse.events.sent").increment(); } catch (Exception ignore) {}
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

    public void notifyRefundJobUpdate(UUID orderId, String status, String message) {
        var payload = new java.util.HashMap<String, Object>();
        payload.put("type", "refund_job_update");
        payload.put("orderId", orderId != null ? orderId.toString() : null);
        if (status != null) payload.put("status", status);
        if (message != null) payload.put("message", message);
        payload.put("ts", Instant.now().toString());
        notifyAdmins("refund", payload);
        persistAdmin("refund_job_update", "Refund update", "Order " + (orderId != null ? orderId : "-") + ": " + (status != null ? status : "update"), payload);
    }

    @javax.annotation.PostConstruct
    void registerGauges() {
        try {
            io.micrometer.core.instrument.Gauge.builder("sse.connections.current", adminOpen, java.util.concurrent.atomic.AtomicInteger::get)
                    .description("Current open SSE connections for admin channel")
                    .tag("channel", "admin")
                    .register(meterRegistry);
            io.micrometer.core.instrument.Gauge.builder("sse.connections.current", userOpen, java.util.concurrent.atomic.AtomicInteger::get)
                    .description("Current open SSE connections for user channel (aggregate)")
                    .tag("channel", "user")
                    .register(meterRegistry);
        } catch (Exception ignore) {}
    }

    // Periodic heartbeat to keep SSE connections alive (every 25s)
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 25000)
    public void heartbeat() {
        try {
            if (!adminEmitters.isEmpty()) {
                for (var e : adminEmitters) {
                    trySend(e, SseEmitter.event().name("ping").data("heartbeat:" + Instant.now().toString()));
                }
            }
            if (!userEmitters.isEmpty()) {
                for (var entry : userEmitters.entrySet()) {
                    var list = entry.getValue();
                    if (list != null) {
                        for (var e : list) {
                            trySend(e, SseEmitter.event().name("ping").data("heartbeat:" + Instant.now().toString()));
                        }
                    }
                }
            }
        } catch (Exception ignore) {}
    }
}