package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingResponseDTO {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("object")
    private String object;

    @JsonProperty("id")
    private String id;

    @JsonProperty("waybill_id")
    private String waybillId;

    @JsonProperty("courier")
    private CourierDTO courier;

    @JsonProperty("origin")
    private OriginDTO origin;

    @JsonProperty("destination")
    private DestinationDTO destination;

    @JsonProperty("history")
    private List<TrackingHistoryDTO> history;

    @JsonProperty("link")
    private String link;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("status")
    private String status;
}
