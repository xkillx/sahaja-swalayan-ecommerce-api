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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/notifications/feed")
@RequiredArgsConstructor
public class NotificationFeedController {

    private final NotificationEventRepository eventRepo;

    @Operation(summary = "Get admin notification feed (latest first)")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminFeed(
            @RequestParam(defaultValue = "20") int size) {
        int limit = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(0, limit);
        List<NotificationEvent> list = eventRepo.findAllByAudienceOrderByCreatedAtDesc("admins", pageable);
        Map<String, Object> data = new HashMap<>();
        data.put("content", list);
        data.put("size", list.size());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }
}
