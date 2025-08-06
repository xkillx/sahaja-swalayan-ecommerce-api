package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

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
public class PricingDTO {
    @JsonProperty("available_for_cash_on_delivery")
    private boolean availableForCashOnDelivery;
    @JsonProperty("available_for_proof_of_delivery")
    private boolean availableForProofOfDelivery;
    @JsonProperty("available_for_instant_waybill_id")
    private boolean availableForInstantWaybillId;
    @JsonProperty("available_for_insurance")
    private boolean availableForInsurance;
    @JsonProperty("available_collection_method")
    @Singular("availableCollectionMethod")
    private List<String> availableCollectionMethod;
    private String company;
    @JsonProperty("courier_name")
    private String courierName;
    @JsonProperty("courier_code")
    private String courierCode;
    @JsonProperty("courier_service_name")
    private String courierServiceName;
    @JsonProperty("courier_service_code")
    private String courierServiceCode;
    private String currency;
    private String description;
    private String duration;
    @JsonProperty("shipment_duration_range")
    private String shipmentDurationRange;
    @JsonProperty("shipment_duration_unit")
    private String shipmentDurationUnit;
    @JsonProperty("service_type")
    private String serviceType;
    @JsonProperty("shipping_type")
    private String shippingType;
    private Integer price;
    @JsonProperty("tax_lines")
    @Singular
    private List<String> taxLines;
    private String type;
}
