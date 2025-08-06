package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierRateRequestDTO {
    @JsonProperty("origin_latitude")
    private Double originLatitude;
    @JsonProperty("origin_longitude")
    private Double originLongitude;
    @JsonProperty("destination_latitude")
    private Double destinationLatitude;
    @JsonProperty("destination_longitude")
    private Double destinationLongitude;
    @JsonProperty("origin_postal_code")
    private String originPostalCode;
    @JsonProperty("destination_postal_code")
    private String destinationPostalCode;
    @JsonProperty("origin_area_id")
    private String originAreaId;
    @JsonProperty("destination_area_id")
    private String destinationAreaId;
    private String type;
    private String couriers;
    @Singular
    private List<ItemDTO> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDTO {
        private String name;
        private String description;
        private String sku;
        private Integer value;
        private Integer quantity;
        private Integer weight;
        private Integer height;
        private Integer length;
        private Integer width;
    }
}
