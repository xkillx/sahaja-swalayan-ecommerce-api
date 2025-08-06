package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreaResponseDTO {
    private boolean success;

    @Singular
    private List<AreaDTO> areas;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AreaDTO {
        private String id;
        private String name;
        @JsonProperty("country_name")
        private String countryName;
        @JsonProperty("country_code")
        private String countryCode;
        @JsonProperty("administrative_division_level_1_name")
        private String administrativeDivisionLevel1Name;
        @JsonProperty("administrative_division_level_2_name")
        private String administrativeDivisionLevel2Name;
        @JsonProperty("administrative_division_level_3_name")
        private String administrativeDivisionLevel3Name;
        @JsonProperty("postal_code")
        private int postalCode;
    }
}
