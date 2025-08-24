package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.common.CustomUserDetails;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.UserJpaRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserJpaRepository userRepo;

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMe(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody UpdateMeRequest req
    ) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        var user = principal.getUser();

        // Normalize and validate phone lightly. Entity also has @Pattern validation on persist.
        if (req != null && req.phone != null) {
            String phone = req.phone.trim();
            // If provided, accept +62… style (shop normalizes) or generic +digits 10-15
            if (!phone.matches("^\\+?[0-9]{10,15}$")) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid phone number"));
            }
            user.setPhone(phone);
        }

        var saved = userRepo.save(user);
        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("name", saved.getName());
        data.put("email", saved.getEmail());
        data.put("phone", saved.getPhone());
        data.put("role", saved.getRole());
        data.put("status", saved.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Profile updated", data));
    }

    @Data
    public static class UpdateMeRequest {
        public String phone;
    }
}
