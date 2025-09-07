package com.sahaja.swalayan.ecommerce.application.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.NotificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final NotificationTokenRepository tokenRepo;
    private volatile boolean initialized = false;

    @Value("${firebase.service.account:}")
    private String serviceAccountPathProp;

    @Value("${firebase.service.account.json:}")
    private String serviceAccountJsonProp;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps() != null && !FirebaseApp.getApps().isEmpty()) {
                initialized = true;
                log.info("FirebaseApp already initialized.");
                return;
            }

            // 1) If a path is provided, try file system path first, then classpath
            String path = (serviceAccountPathProp != null && !serviceAccountPathProp.trim().isEmpty()) ? serviceAccountPathProp.trim() : null;
            if (path != null) {
                try {
                    var fs = new org.springframework.core.io.FileSystemResource(path);
                    if (fs.exists()) {
                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(fs.getInputStream()))
                                .build();
                        FirebaseApp.initializeApp(options);
                        initialized = true;
                        log.info("FirebaseApp initialized from explicit file path: {}", path);
                        return;
                    }
                } catch (Exception ignore) {}

                try {
                    var cp = new ClassPathResource(path);
                    if (cp.exists()) {
                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(cp.getInputStream()))
                                .build();
                        FirebaseApp.initializeApp(options);
                        initialized = true;
                        log.info("FirebaseApp initialized from classpath resource: {}", path);
                        return;
                    }
                } catch (Exception ignore) {}
            }

            // 2) Try default classpath filename
            try {
                var cpDefault = new ClassPathResource("firebase-service-account.json");
                if (cpDefault.exists()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(cpDefault.getInputStream()))
                            .build();
                    FirebaseApp.initializeApp(options);
                    initialized = true;
                    log.info("FirebaseApp initialized from classpath default firebase-service-account.json");
                    return;
                }
            } catch (Exception ignore) {}

            // 3) Try JSON provided via property (not recommended for prod)
            try {
                if (serviceAccountJsonProp != null && !serviceAccountJsonProp.trim().isEmpty()) {
                    byte[] bytes = serviceAccountJsonProp.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    java.io.InputStream is = new java.io.ByteArrayInputStream(bytes);
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(is))
                            .build();
                    FirebaseApp.initializeApp(options);
                    initialized = true;
                    log.info("FirebaseApp initialized from inline JSON property");
                    return;
                }
            } catch (Exception ignore) {}

            // 4) Fallback: Application Default Credentials (e.g., GOOGLE_APPLICATION_CREDENTIALS)
            try {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
                FirebaseApp.initializeApp(options);
                initialized = true;
                log.info("FirebaseApp initialized from application default credentials");
                return;
            } catch (Exception ignore) {}

            log.warn("Firebase not configured (no service account). Push notifications will be no-op. Configure firebase.service.account or GOOGLE_APPLICATION_CREDENTIALS.");
        } catch (Exception e) {
            log.warn("Failed to initialize FirebaseApp: {}", e.getMessage());
        }
    }

    private boolean isEnabled() { return initialized; }

    public void sendOrderPaidToAdmins(UUID orderId) {
        if (!isEnabled()) return;
        var tokens = tokenRepo.findAllByAppAndRevokedFalse("sahaja-admin");
        multicastSend(tokens.stream().map(t -> t.getToken()).collect(Collectors.toList()),
                Notification.builder().setTitle("Order paid")
                        .setBody("Order " + orderId + " awaiting pickup request")
                        .build(),
                WebpushConfig.builder().setFcmOptions(WebpushFcmOptions.withLink("/admin/orders/" + orderId)).build(),
                new java.util.HashMap<>() {{ put("type","order_paid_awaiting_pickup"); put("orderId", orderId.toString()); }}
        );
    }

    public void sendShippingUpdateToAdmins(UUID orderId, String status, String message) {
        if (!isEnabled()) return;
        var tokens = tokenRepo.findAllByAppAndRevokedFalse("sahaja-admin");
        var data = new java.util.HashMap<String,String>();
        data.put("type","shipping_job_update");
        data.put("orderId", orderId.toString());
        if (status != null) data.put("status", status);
        if (message != null) data.put("message", message);
        multicastSend(tokens.stream().map(t -> t.getToken()).collect(Collectors.toList()),
                Notification.builder().setTitle("Shipping update")
                        .setBody("Order " + orderId + ": " + (status != null ? status : "update"))
                        .build(),
                WebpushConfig.builder().setFcmOptions(WebpushFcmOptions.withLink("/admin/orders/" + orderId)).build(),
                data
        );
    }

    public void sendRefundUpdateToAdmins(UUID orderId, String status, String message) {
        if (!isEnabled()) return;
        var tokens = tokenRepo.findAllByAppAndRevokedFalse("sahaja-admin");
        var data = new java.util.HashMap<String,String>();
        data.put("type","refund_job_update");
        if (orderId != null) data.put("orderId", orderId.toString());
        if (status != null) data.put("status", status);
        if (message != null) data.put("message", message);
        multicastSend(tokens.stream().map(t -> t.getToken()).collect(Collectors.toList()),
                Notification.builder().setTitle("Refund update")
                        .setBody("Refund " + (orderId != null ? ("for order " + orderId + ": ") : "") + (status != null ? status : "update"))
                        .build(),
                WebpushConfig.builder().setFcmOptions(WebpushFcmOptions.withLink(orderId != null ? "/admin/orders/" + orderId : "/admin")).build(),
                data
        );
    }

    private void multicastSend(List<String> tokens,
                               Notification notification,
                               WebpushConfig webpush,
                               java.util.Map<String,String> data) {
        if (tokens == null || tokens.isEmpty()) return;
        // Firebase allows up to 500 tokens per request
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i += 500) {
            batches.add(tokens.subList(i, Math.min(tokens.size(), i + 500)));
        }
        for (var batch : batches) {
            try {
                var message = MulticastMessage.builder()
                        .addAllTokens(batch)
                        .setNotification(notification)
                        .putAllData(data != null ? data : java.util.Collections.emptyMap())
                        .setWebpushConfig(webpush)
                        .build();
                var response = FirebaseMessaging.getInstance().sendMulticast(message);
                log.info("Push sent: success={} failure={}", response.getSuccessCount(), response.getFailureCount());
                // Clean up invalid tokens
                IntStream.range(0, response.getResponses().size()).forEach(i -> {
                    var r = response.getResponses().get(i);
                    if (!r.isSuccessful()) {
                        var ex = r.getException();
                        if (ex instanceof FirebaseMessagingException fmEx) {
                            var code = fmEx.getMessagingErrorCode();
                            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                                var badToken = batch.get(i);
                                tokenRepo.findByToken(badToken).ifPresent(t -> { t.setRevoked(true); tokenRepo.save(t); });
                                log.info("Revoked invalid token");
                            }
                        }
                    }
                });
            } catch (Exception e) {
                log.warn("Multicast push failed: {}", e.getMessage());
            }
        }
    }
}
