package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.domain.model.settings.StoreSettings;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.StoreSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1/store-settings")
@RequiredArgsConstructor
public class StoreSettingsController {

    private final StoreSettingsRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublic() {
        Optional<StoreSettings> opt = repo.findAll().stream().findFirst();
        Map<String, Object> data = new HashMap<>();
        if (opt.isPresent()) {
            StoreSettings s = opt.get();
            data.put("storeName", s.getStoreName());
            data.put("addressLine", s.getAddressLine());
            data.put("latitude", s.getLatitude());
            data.put("longitude", s.getLongitude());
        }
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }
}
