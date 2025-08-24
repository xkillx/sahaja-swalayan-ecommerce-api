package com.sahaja.swalayan.ecommerce.infrastructure.xendit;

import com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto.XenditRefundRequest;
import com.sahaja.swalayan.ecommerce.infrastructure.xendit.dto.XenditRefundResponse;
import com.sahaja.swalayan.ecommerce.infrastructure.config.XenditProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class XenditRefundClient {

    private final RestTemplate restTemplate;
    private final XenditProperties xenditProperties;

    public XenditRefundResponse createRefund(String invoiceId, XenditRefundRequest request, String idempotencyKey) {
        String url = xenditProperties.getBaseUrl() + "/v2/invoices/" + invoiceId + "/refunds";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(xenditProperties.getApiKey(), "");
        headers.set("Content-Type", "application/json");
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            headers.set("Idempotency-Key", idempotencyKey);
        }
        HttpEntity<XenditRefundRequest> entity = new HttpEntity<>(request, headers);
        try {
            log.debug("[xendit-refund] POST {} body={} idempo={}", url, request, idempotencyKey);
            ResponseEntity<XenditRefundResponse> resp = restTemplate.postForEntity(url, entity, XenditRefundResponse.class);
            log.debug("[xendit-refund] status={} body={}", resp.getStatusCode(), resp.getBody());
            return resp.getBody();
        } catch (HttpStatusCodeException ex) {
            // Propagate with details so caller can decide retryable/non-retryable
            log.warn("[xendit-refund] HTTP {} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        } catch (Exception e) {
            log.warn("[xendit-refund] error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
