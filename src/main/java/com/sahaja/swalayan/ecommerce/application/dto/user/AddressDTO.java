package com.sahaja.swalayan.ecommerce.application.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private UUID id;
    private String label;
    @JsonProperty("contact_name")
    private String contactName;
    @JsonProperty("contact_phone")
    private String contactPhone;
    @JsonProperty("address_line")
    private String addressLine;
    @JsonProperty("postal_code")
    private String postalCode;
    @JsonProperty("area_id")
    private String areaId;
    private Double latitude;
    private Double longitude;
    @JsonProperty("is_default")
    private Boolean isDefault;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
