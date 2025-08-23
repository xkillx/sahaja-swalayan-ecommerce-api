package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.domain.model.settings.StoreSettings;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.StoreSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1/admin/store-settings")
@RequiredArgsConstructor
public class AdminStoreSettingsController {

    private final StoreSettingsRepository repo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String,Object>>> get() {
        Optional<StoreSettings> opt = repo.findAll().stream().findFirst();
        StoreSettings s = opt.orElse(null);
        Map<String,Object> data = new HashMap<>();
        if (s != null) {
            data.put("id", s.getId());
            data.put("storeName", s.getStoreName());
            data.put("addressLine", s.getAddressLine());
            data.put("latitude", s.getLatitude());
            data.put("longitude", s.getLongitude());
            // Intentionally do not expose Google Maps API key in settings API
        }
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String,Object>>> upsert(@RequestBody Map<String,Object> body) {
        String storeName = asString(body.get("storeName"));
        String addressLine = asString(body.get("addressLine"));
        Double latitude = asDouble(body.get("latitude"));
        Double longitude = asDouble(body.get("longitude"));
        // NOTE: googleMapsApiKey is intentionally ignored to remove it from store settings management

        StoreSettings s = repo.findAll().stream().findFirst().orElse(StoreSettings.builder().build());
        s.setStoreName(storeName);
        s.setAddressLine(addressLine);
        s.setLatitude(latitude);
        s.setLongitude(longitude);
        // Do not modify stored googleMapsApiKey here; it's not managed via settings anymore
        s = repo.save(s);

        Map<String,Object> data = new HashMap<>();
        data.put("id", s.getId());
        data.put("storeName", s.getStoreName());
        data.put("addressLine", s.getAddressLine());
        data.put("latitude", s.getLatitude());
        data.put("longitude", s.getLongitude());
        return ResponseEntity.ok(ApiResponse.success("Saved", data));
    }

    private String asString(Object o) { return o == null ? null : String.valueOf(o); }
    private Double asDouble(Object o) { try { return o == null ? null : Double.valueOf(String.valueOf(o)); } catch (Exception e) { return null; } }
}
