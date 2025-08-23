package com.sahaja.swalayan.ecommerce.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleTokenVerifier {

    private final ObjectMapper objectMapper;

    @Value("${google.oauth.client-ids:}")
    private String clientIdsCsv;

    private Set<String> getAllowedAudiences() {
        if (clientIdsCsv == null || clientIdsCsv.isBlank()) return Set.of();
        String[] parts = clientIdsCsv.split(",");
        Set<String> set = new HashSet<>();
        for (String p : parts) {
            String v = p.trim();
            if (!v.isEmpty()) set.add(v);
        }
        return set;
    }

    public Result verify(String idToken) {
        try {
            if (idToken == null || idToken.isBlank()) {
                return Result.error("idToken is blank");
            }
            // Call Google tokeninfo endpoint
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + java.net.URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("[google] tokeninfo status {} body {}", resp.statusCode(), resp.body());
                return Result.error("Invalid Google token");
            }
            JsonNode node = objectMapper.readTree(resp.body());
            String aud = node.path("aud").asText(null);
            String email = node.path("email").asText(null);
            boolean emailVerified = node.path("email_verified").asText("false").equalsIgnoreCase("true") || node.path("email_verified").asBoolean(false);
            String name = node.path("name").asText(null);
            String sub = node.path("sub").asText(null);

            Set<String> allowed = getAllowedAudiences();
            if (!allowed.isEmpty() && (aud == null || !allowed.contains(aud))) {
                log.warn("[google] aud {} not allowed", aud);
                return Result.error("Invalid audience");
            }
            if (email == null || !emailVerified) {
                return Result.error("Email not verified");
            }
            return Result.success(email, name != null ? name : email, sub, aud);
        } catch (Exception ex) {
            log.error("[google] verify error: {}", ex.getMessage(), ex);
            return Result.error("Verification failed");
        }
    }

    public record Result(boolean valid, String error, String email, String name, String sub, String aud) {
        public static Result success(String email, String name, String sub, String aud) {
            return new Result(true, null, email, name, sub, aud);
        }
        public static Result error(String err) {
            return new Result(false, err, null, null, null, null);
        }
    }
}
