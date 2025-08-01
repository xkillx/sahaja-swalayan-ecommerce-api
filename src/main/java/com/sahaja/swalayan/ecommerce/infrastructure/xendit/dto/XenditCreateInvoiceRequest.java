package com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XenditCreateInvoiceRequest {
    @JsonProperty("external_id")
    private String externalId;

    private Number amount;

    @JsonProperty("payer_email")
    private String payerEmail;

    private String description;

    @JsonProperty("success_redirect_url")
    private String successRedirectUrl;
}
