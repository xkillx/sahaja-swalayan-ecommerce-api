package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO representing a single courier object from the Biteship Retrieve a Courier API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierRetrieveDTO {
    @JsonProperty("available_for_cash_on_delivery")
    private boolean availableForCashOnDelivery;

    @JsonProperty("available_for_proof_of_delivery")
    private boolean availableForProofOfDelivery;

    @JsonProperty("available_for_instant_waybill_id")
    private boolean availableForInstantWaybillId;

    @JsonProperty("courier_name")
    private String courierName;

    @JsonProperty("courier_code")
    private String courierCode;

    @JsonProperty("courier_service_name")
    private String courierServiceName;

    @JsonProperty("courier_service_code")
    private String courierServiceCode;

    @JsonProperty("tier")
    private String tier;

    @JsonProperty("description")
    private String description;

    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("shipping_type")
    private String shippingType;

    @JsonProperty("shipment_duration_range")
    private String shipmentDurationRange;

    @JsonProperty("shipment_duration_unit")
    private String shipmentDurationUnit;
}
