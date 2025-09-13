package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.domain.model.notification.NotificationEvent;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.NotificationEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/notifications/feed")
@RequiredArgsConstructor
public class NotificationFeedController {

    @Operation(summary = "Mark one admin notification as read")
    @PatchMapping("/admin/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public org.springframework.http.ResponseEntity<com.sahaja.swalayan.ecommerce.application.dto.ApiResponse<java.util.Map<String, Object>>> readOneAdmin(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID id) {
        var evOpt = eventRepo.findById(id);
        if (evOpt.isEmpty()) {
            return org.springframework.http.ResponseEntity.status(404)
                    .body(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.error("Notification not found"));
        }
        var ev = evOpt.get();
        if (ev.getReadAt() == null) {
            ev.setReadAt(java.time.LocalDateTime.now());
            eventRepo.save(ev);
        }
        var data = new java.util.HashMap<String, Object>();
        data.put("id", ev.getId());
        data.put("readAt", ev.getReadAt());
        return org.springframework.http.ResponseEntity.ok(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.success("OK", data));
    }

    @Operation(summary = "Mark all admin notifications as read")
    @PatchMapping("/admin/read-all")
    @PreAuthorize("hasRole('ADMIN')")
    public org.springframework.http.ResponseEntity<com.sahaja.swalayan.ecommerce.application.dto.ApiResponse<java.util.Map<String, Object>>> readAllAdmin() {
        var list = eventRepo.findAllByAudienceOrderByCreatedAtDesc("admins", org.springframework.data.domain.PageRequest.of(0, 500));
        int updated = 0;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (var ev : list) {
            if (ev.getReadAt() == null) {
                ev.setReadAt(now);
                updated++;
            }
        }
        if (updated > 0) eventRepo.saveAll(list);
        var data = new java.util.HashMap<String, Object>();
        data.put("updated", updated);
        return org.springframework.http.ResponseEntity.ok(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.success("OK", data));
    }

    private final NotificationEventRepository eventRepo;

    @Operation(summary = "Get admin notification feed (latest first)")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminFeed(
            @RequestParam(defaultValue = "20") int size) {
        int limit = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(0, limit);
        List<NotificationEvent> list = eventRepo.findAllByAudienceOrderByCreatedAtDesc("admins", pageable);
        long unread = eventRepo.countByAudienceAndReadAtIsNull("admins");
        Map<String, Object> data = new HashMap<>();
        data.put("content", list);
        data.put("size", list.size());
        data.put("unreadCount", unread);
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }
}
