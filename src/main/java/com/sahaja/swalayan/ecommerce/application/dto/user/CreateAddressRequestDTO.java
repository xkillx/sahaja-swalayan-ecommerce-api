package com.sahaja.swalayan.ecommerce.application.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAddressRequestDTO {
    @NotBlank(message = "Label is required")
    private String label;

    @JsonProperty("contact_name")
    @NotBlank(message = "Contact name is required")
    private String contactName;

    @JsonProperty("contact_phone")
    @NotBlank(message = "Contact phone is required")
    private String contactPhone;

    @JsonProperty("address_line")
    @NotBlank(message = "Address line is required")
    private String addressLine;

    @JsonProperty("postal_code")
    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @JsonProperty("area_id")
    private String areaId;

    private Double latitude;

    private Double longitude;

    @JsonProperty("is_default")
    @NotNull(message = "is_default is required")
    private Boolean isDefault;
}
