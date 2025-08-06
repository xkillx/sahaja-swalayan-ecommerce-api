package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    @JsonProperty("location_id")
    private String locationId;
    private Double latitude;
    private Double longitude;
    @JsonProperty("postal_code")
    private String postalCode;
    @JsonProperty("country_name")
    private String countryName;
    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("administrative_division_level_1_name")
    private String administrativeDivisionLevel1Name;
    @JsonProperty("administrative_division_level_1_type")
    private String administrativeDivisionLevel1Type;
    @JsonProperty("administrative_division_level_2_name")
    private String administrativeDivisionLevel2Name;
    @JsonProperty("administrative_division_level_2_type")
    private String administrativeDivisionLevel2Type;
    @JsonProperty("administrative_division_level_3_name")
    private String administrativeDivisionLevel3Name;
    @JsonProperty("administrative_division_level_3_type")
    private String administrativeDivisionLevel3Type;
    @JsonProperty("administrative_division_level_4_name")
    private String administrativeDivisionLevel4Name;
    @JsonProperty("administrative_division_level_4_type")
    private String administrativeDivisionLevel4Type;
    private String address;
}
