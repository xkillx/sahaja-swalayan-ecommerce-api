package com.sahaja.swalayan.ecommerce.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.debug("[RestTemplate] Request URI: {}", request.getURI());
        log.debug("[RestTemplate] Request Method: {}", request.getMethod());
        log.debug("[RestTemplate] Request Headers: {}", request.getHeaders());
        log.debug("[RestTemplate] Request Body: {}", new String(body, StandardCharsets.UTF_8));
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        String body = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        log.debug("[RestTemplate] Response Status: {} {}", response.getStatusCode(), response.getStatusText());
        log.debug("[RestTemplate] Response Headers: {}", response.getHeaders());
        log.debug("[RestTemplate] Response Body: {}", body);
    }
}
