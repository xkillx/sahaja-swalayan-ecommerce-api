package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.domain.model.notification.NotificationToken;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.NotificationTokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications/tokens")
public class NotificationTokenController {

    private final NotificationTokenRepository tokenRepo;

    @Operation(summary = "Register/upsert a push token for the current device")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody RegisterTokenRequest req,
                                                                     @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        if (req == null || req.token == null || req.token.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token is required"));
        }
        NotificationToken token = tokenRepo.findByToken(req.token).orElseGet(() -> NotificationToken.builder().token(req.token).build());
        token.setUserId(req.userId);
        token.setApp(safe(req.app));
        token.setPlatform(safe(req.platform));
        token.setLocale(safe(req.locale));
        token.setUserAgent(userAgent);
        token.setRevoked(false);
        token.setLastSeenAt(LocalDateTime.now());
        token = tokenRepo.save(token);
        Map<String, Object> data = new HashMap<>();
        data.put("id", token.getId());
        data.put("token", token.getToken());
        return ResponseEntity.ok(ApiResponse.success("Registered", data));
    }

    @Operation(summary = "Unregister/revoke a push token for the current device")
    @DeleteMapping("/{token}")
    public ResponseEntity<ApiResponse<Void>> unregister(@PathVariable String token) {
        var opt = tokenRepo.findByToken(token);
        if (opt.isEmpty()) return ResponseEntity.ok(ApiResponse.success("Already removed"));
        var t = opt.get();
        t.setRevoked(true);
        tokenRepo.save(t);
        return ResponseEntity.ok(ApiResponse.success("Revoked"));
    }

    private static String safe(String v) { return v == null ? null : v.trim(); }

    @Data
    public static class RegisterTokenRequest {
        public String token;
        public String platform; // web/android/ios
        public String app; // sahaja-admin / sahaja-shop
        public String locale;
        public UUID userId; // optional; backend can validate later if needed
    }
}
