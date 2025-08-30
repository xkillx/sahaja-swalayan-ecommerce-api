package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Subscribe to admin notifications via SSE")
    @GetMapping(path = "/stream/admin", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public SseEmitter subscribeAdmin() {
        return notificationService.subscribeAdmin();
    }

    // New secure endpoint deriving userId from the authenticated principal
    @Operation(summary = "Subscribe to your user notifications via SSE (authenticated)")
    @GetMapping(path = "/stream/me", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter subscribeMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthenticated");
        }
        try {
            UUID userId = UUID.fromString(auth.getName());
            return notificationService.subscribeUser(userId);
        } catch (IllegalArgumentException ex) {
            // If your principal name is not UUID, adapt mapping here (e.g., resolve from claims)
            throw new org.springframework.security.access.AccessDeniedException("Invalid user identity");
        }
    }

    // Keep legacy open endpoint for compatibility (consider removing later)
    @Operation(summary = "Subscribe to user notifications via SSE (legacy, not secured)")
    @GetMapping(path = "/stream/user/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeUser(@PathVariable UUID userId) {
        return notificationService.subscribeUser(userId);
    }
}
