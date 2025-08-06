package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping;

import com.sahaja.swalayan.ecommerce.infrastructure.config.BiteshipProperties;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.AreaResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.TrackingResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.RetrieveOrderResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancellationReasonResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancelOrderRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancelOrderResponseDTO;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class BiteshipShippingClient {

    private final RestTemplate restTemplate;
    private final BiteshipProperties biteshipProperties;

    public BiteshipShippingClient(RestTemplate restTemplate, BiteshipProperties biteshipProperties) {
        this.restTemplate = restTemplate;
        this.biteshipProperties = biteshipProperties;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + biteshipProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    public AreaResponseDTO searchAreas(String input) {
        String url = UriComponentsBuilder.fromUriString(biteshipProperties.getBaseUrl() + "/v1/maps/areas")
                .queryParam("countries", "ID")
                .queryParam("input", input)
                .queryParam("type", "single")
                .toUriString();

        HttpHeaders headers = createAuthHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<AreaResponseDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, AreaResponseDTO.class);

        return response.getBody();
    }

    public CourierRateResponseDTO getCourierRates(CourierRateRequestDTO request) {
        String url = biteshipProperties.getBaseUrl() + "/v1/rates/couriers";

        HttpHeaders headers = createAuthHeaders();

        HttpEntity<CourierRateRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<CourierRateResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                CourierRateResponseDTO.class);

        return response.getBody();
    }

    public CreateOrderResponseDTO createOrder(CreateOrderRequestDTO requestDTO) {
        String url = biteshipProperties.getBaseUrl() + "/v1/orders";

        HttpHeaders headers = createAuthHeaders();

        HttpEntity<CreateOrderRequestDTO> entity = new HttpEntity<>(requestDTO, headers);

        ResponseEntity<CreateOrderResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                CreateOrderResponseDTO.class);

        return response.getBody();
    }

    public RetrieveOrderResponseDTO retrieveOrderById(String orderId) {
        String url = UriComponentsBuilder.fromUriString(biteshipProperties.getBaseUrl() + "/v1/orders/{id}")
                .buildAndExpand(orderId)
                .toUriString();

        HttpHeaders headers = createAuthHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<RetrieveOrderResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                RetrieveOrderResponseDTO.class
        );

        return response.getBody();
    }

    public CancellationReasonResponseDTO getCancellationReasons(String lang) {
        String url = UriComponentsBuilder.fromUriString(biteshipProperties.getBaseUrl() + "/v1/orders/cancellation_reasons")
                .queryParam("lang", lang)
                .toUriString();

        HttpHeaders headers = createAuthHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CancellationReasonResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CancellationReasonResponseDTO.class
        );
        return response.getBody();
    }

    public CancelOrderResponseDTO cancelOrder(String orderId, CancelOrderRequestDTO request) {
        String url = UriComponentsBuilder.fromUriString(biteshipProperties.getBaseUrl() + "/v1/orders/{orderId}/cancel")
                .buildAndExpand(orderId)
                .toUriString();

        HttpHeaders headers = createAuthHeaders();

        HttpEntity<CancelOrderRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<CancelOrderResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                CancelOrderResponseDTO.class
        );
        return response.getBody();
    }

    public TrackingResponseDTO getTrackingById(String trackingId) {
        String url = UriComponentsBuilder.fromUriString(biteshipProperties.getBaseUrl() + "/v1/trackings/{trackingId}")
                .buildAndExpand(trackingId)
                .toUriString();

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TrackingResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                TrackingResponseDTO.class
        );
        return response.getBody();
    }

    public CourierResponseDTO getAvailableCouriers() {
        String url = biteshipProperties.getBaseUrl() + "/v1/couriers";

        HttpHeaders headers = createAuthHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<CourierResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CourierResponseDTO.class
        );
        return response.getBody();
    }
}

