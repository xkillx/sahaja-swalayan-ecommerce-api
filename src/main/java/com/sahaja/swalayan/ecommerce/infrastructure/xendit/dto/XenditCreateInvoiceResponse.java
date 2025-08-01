package com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class XenditCreateInvoiceResponse {
    private String id;

    @JsonProperty("invoice_url")
    private String invoiceUrl;
}
