package com.sahaja.swalayan.ecommerce.infrastructure.xendit;

import com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto.XenditCreateInvoiceRequest;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto.XenditCreateInvoiceResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import com.sahaja.swalayan.ecommerce.infrastructure.config.XenditProperties;

@Component
@Slf4j
public class XenditInvoiceClient {
    private final RestTemplate restTemplate;
    private final XenditProperties xenditProperties;

    public XenditInvoiceClient(RestTemplate restTemplate,
                               XenditProperties xenditProperties) {
        this.restTemplate = restTemplate;
        this.xenditProperties = xenditProperties;
    }

    public XenditCreateInvoiceResponse createInvoice(XenditCreateInvoiceRequest request) {
        String url = xenditProperties.getBaseUrl() + "/v2/invoices";
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(xenditProperties.getApiKey(), "");
        headers.set("Content-Type", "application/json");
        
        // Create request entity
        HttpEntity<XenditCreateInvoiceRequest> entity = new HttpEntity<>(request, headers);
        
        // Log request details
        log.debug("Xendit API Request - URL: {}", url);
        log.debug("Xendit API Request - Headers: {}", headers);
        log.debug("Xendit API Request - Body: {}", request);
        
        try {
            // Make API call
            log.debug("Sending request to Xendit API to create invoice");
            ResponseEntity<XenditCreateInvoiceResponse> response = restTemplate.postForEntity(
                    url, entity, XenditCreateInvoiceResponse.class);
            
            // Log response details
            log.debug("Xendit API Response - Status: {}", response.getStatusCode());
            log.debug("Xendit API Response - Headers: {}", response.getHeaders());
            log.debug("Xendit API Response - Body: {}", response.getBody());
            log.debug("Successfully received response from Xendit API");
            
            return response.getBody();
        } catch (Exception e) {
            log.debug("Error calling Xendit API: {}", e.getMessage(), e);
            throw e;
        }
    }
}
