package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.service.NotificationService;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenUtil jwtTokenUtil;

    @Operation(summary = "Subscribe to admin notifications via SSE")
    @GetMapping(path = "/stream/admin", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public SseEmitter subscribeAdmin() {
        return notificationService.subscribeAdmin();
    }

    // SSE helper that authenticates using a JWT in the query string (for EventSource clients)
    @Operation(summary = "Subscribe to admin notifications via SSE (token in query for EventSource)")
    @GetMapping(path = "/stream/admin/auth", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeAdminWithToken(@RequestParam(name = "token") String token) {
        // Never bubble exceptions here to avoid content negotiation issues with SSE
        try {
            if (token == null || token.isBlank() || !jwtTokenUtil.validateToken(token)) {
                return errorSse("Invalid token");
            }
            var claims = jwtTokenUtil.getAllClaimsFromToken(token);
            Object rolesObj = claims.get("roles");
            boolean isAdmin = false;
            if (rolesObj instanceof java.util.Collection<?> col) {
                isAdmin = col.stream().anyMatch(r -> String.valueOf(r).toUpperCase().contains("ADMIN"));
            } else if (rolesObj != null) {
                isAdmin = String.valueOf(rolesObj).toUpperCase().contains("ADMIN");
            }
            if (!isAdmin) {
                return errorSse("Admin role required");
            }
            return notificationService.subscribeAdmin();
        } catch (Exception e) {
            return errorSse("Invalid token");
        }
    }

    // New secure endpoint deriving userId from the authenticated principal (JWT claim preferred)
    @Operation(summary = "Subscribe to your user notifications via SSE (authenticated)")
    @GetMapping(path = "/stream/me", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter subscribeMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = resolveUserId(auth);
        if (userId == null) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid user identity");
        }
        return notificationService.subscribeUser(userId);
    }

    // Keep legacy endpoint, but enforce ownership securely
    @Operation(summary = "Subscribe to user notifications via SSE (legacy; will be removed soon)")
    @GetMapping(path = "/stream/user/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter subscribeUser(@PathVariable UUID userId, Authentication auth) {
        UUID principalId = resolveUserId(auth);
        if (principalId == null || !principalId.equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return notificationService.subscribeUser(userId);
    }

    private UUID resolveUserId(Authentication auth) {
        if (auth == null) return null;
        try {
            // Option A: JWT-based auth with userId claim (via reflection to avoid hard dependency)
            if (auth.getClass().getName().contains("JwtAuthenticationToken")) {
                try {
                    java.lang.reflect.Method getToken = auth.getClass().getMethod("getToken");
                    Object tokenObj = getToken.invoke(auth);
                    if (tokenObj != null) {
                        java.lang.reflect.Method getClaims = tokenObj.getClass().getMethod("getClaims");
                        Object claims = getClaims.invoke(tokenObj);
                        if (claims instanceof java.util.Map<?,?> map) {
                            Object claim = map.get("userId");
                            if (claim != null) return UUID.fromString(String.valueOf(claim));
                        }
                    }
                } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}
        try {
            // Option B: CustomUserDetails principal
            Object principal = auth.getPrincipal();
            if (principal != null && principal.getClass().getName().equals("com.sahaja.swalayan.ecommerce.common.CustomUserDetails")) {
                try {
                    java.lang.reflect.Method m = principal.getClass().getMethod("getId");
                    Object id = m.invoke(principal);
                    if (id != null) return (UUID) id;
                } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}
        try {
            // Option C: Principal name is a UUID string
            if (auth.getName() != null) return UUID.fromString(auth.getName());
        } catch (Exception ignore) {}
        return null;
    }

    private SseEmitter errorSse(String message) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().name("error").data(message != null ? message : "error"));
        } catch (Exception ignore) {
        } finally {
            try { emitter.complete(); } catch (Exception ignore2) {}
        }
        return emitter;
    }
}
