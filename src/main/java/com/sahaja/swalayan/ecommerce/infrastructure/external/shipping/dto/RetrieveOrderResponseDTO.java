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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrieveOrderResponseDTO {
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("object")
    private String object;

    @JsonProperty("id")
    private String id;

    @JsonProperty("draft_order_id")
    private String draftOrderId;

    @JsonProperty("short_id")
    private String shortId;

    @JsonProperty("shipper")
    private ShipperDTO shipper;

    @JsonProperty("origin")
    private OriginDTO origin;

    @JsonProperty("destination")
    private DestinationDTO destination;

    @JsonProperty("delivery")
    private DeliveryDTO delivery;

    @JsonProperty("voucher")
    private VoucherDTO voucher;

    @JsonProperty("courier")
    private CourierDTO courier;

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("invoice_id")
    private String invoiceId;

    @JsonProperty("items")
    @Singular
    private List<ItemDTO> items;

    @JsonProperty("extra")
    private Object extra;

    @JsonProperty("metadata")
    private Object metadata;

    @JsonProperty("tags")
    @Singular
    private List<String> tags;

    @JsonProperty("note")
    private String note;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("tax_lines")
    @Singular
    private List<Object> taxLines;

    @JsonProperty("price")
    private Integer price;

    @JsonProperty("status")
    private String status;

    @JsonProperty("ticket_status")
    private String ticketStatus;
}
