package com.sahaja.swalayan.ecommerce.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XenditWebhookPayload {
    @JsonProperty("external_id")
    private String externalId;
    @JsonProperty("status")
    private String status;
}

