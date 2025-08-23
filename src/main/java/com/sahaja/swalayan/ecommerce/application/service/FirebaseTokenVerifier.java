package com.sahaja.swalayan.ecommerce.application.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Verifies Firebase ID Tokens (RS256) against Google JWKS for securetoken@system.gserviceaccount.com
 * and validates standard claims (iss, aud, exp, sub). Extracts email/name/email_verified.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseTokenVerifier {

    private static final String JWKS_URL = "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

    @Value("${firebase.project-id:}")
    private String projectId;

    // simple in-memory cache
    private volatile JWKSet cachedJwkSet;
    private final AtomicLong lastFetched = new AtomicLong(0);
    private static final long CACHE_TTL_MILLIS = 12 * 60 * 60 * 1000L; // 12h

    private JWKSet getJwkSet(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (!forceRefresh && cachedJwkSet != null && (now - lastFetched.get()) < CACHE_TTL_MILLIS) {
            return cachedJwkSet;
        }
        try {
            JWKSet set = JWKSet.load(new URL(JWKS_URL));
            cachedJwkSet = set;
            lastFetched.set(now);
            return set;
        } catch (Exception e) {
            log.error("[firebase] Failed to fetch JWKS: {}", e.getMessage(), e);
            return cachedJwkSet; // may be null
        }
    }

    public Result verify(String idToken) {
        try {
            if (idToken == null || idToken.isBlank()) {
                return Result.error("idToken is blank");
            }
            if (projectId == null || projectId.isBlank()) {
                return Result.error("firebase.project-id is not configured");
            }

            SignedJWT jwt = SignedJWT.parse(idToken);
            JWSHeader header = jwt.getHeader();
            if (!JWSAlgorithm.RS256.equals(header.getAlgorithm())) {
                return Result.error("Unsupported alg");
            }
            String kid = header.getKeyID();
            if (kid == null || kid.isBlank()) {
                return Result.error("Missing kid");
            }

            // try cached set first, then refresh if kid not found
            RSAKey rsaKey = findKey(getJwkSet(false), kid);
            if (rsaKey == null) {
                rsaKey = findKey(getJwkSet(true), kid);
                if (rsaKey == null) {
                    return Result.error("Signing key not found");
                }
            }

            RSASSAVerifier verifier = new RSASSAVerifier(Objects.requireNonNull(rsaKey.toRSAPublicKey()));
            boolean sigOk = jwt.verify(verifier);
            if (!sigOk) return Result.error("Invalid signature");

            var claims = jwt.getJWTClaimsSet();
            // Validate iss and aud per Firebase spec
            String iss = claims.getIssuer();
            List<String> aud = claims.getAudience();
            String expectedIss = "https://securetoken.google.com/" + projectId;
            if (!expectedIss.equals(iss)) {
                return Result.error("Invalid issuer");
            }
            if (aud == null || aud.isEmpty() || !aud.contains(projectId)) {
                return Result.error("Invalid audience");
            }
            // exp, iat, nbf
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.toInstant().isBefore(Instant.now())) {
                return Result.error("Token expired");
            }
            String sub = claims.getSubject();
            if (sub == null || sub.isBlank()) {
                return Result.error("Missing subject");
            }

            String email = (String) claims.getClaim("email");
            Boolean emailVerified = (Boolean) claims.getClaim("email_verified");
            if (email == null || !Boolean.TRUE.equals(emailVerified)) {
                return Result.error("Email not verified");
            }
            String name = (String) claims.getClaim("name");
            String provider = null;
            Object firebase = claims.getClaim("firebase");
            // The firebase claim is a map with identities and sign_in_provider
            if (firebase instanceof java.util.Map<?,?> fm) {
                Object sip = fm.get("sign_in_provider");
                provider = sip != null ? sip.toString() : null;
            }

            return Result.success(email, name != null ? name : email, sub, projectId, provider);
        } catch (ParseException e) {
            return Result.error("Malformed token");
        } catch (JOSEException e) {
            return Result.error("Verification failed");
        } catch (Exception e) {
            log.error("[firebase] verify error: {}", e.getMessage(), e);
            return Result.error("Verification error");
        }
    }

    private RSAKey findKey(JWKSet set, String kid) {
        if (set == null) return null;
        for (JWK jwk : set.getKeys()) {
            if (kid.equals(jwk.getKeyID()) && jwk instanceof RSAKey rk) {
                return rk;
            }
        }
        return null;
    }

    public record Result(boolean valid, String error, String email, String name, String sub, String aud, String provider) {
        public static Result success(String email, String name, String sub, String aud, String provider) {
            return new Result(true, null, email, name, sub, aud, provider);
        }
        public static Result error(String err) {
            return new Result(false, err, null, null, null, null, null);
        }
    }
}
