package com.devsync.auth.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Base64;

/**
 * JWT configuration properties bound from application.yml / environment variables.
 *
 * <p>Bound from prefix {@code app.jwt}:
 * <ul>
 *   <li>{@code app.jwt.secret}   → JWT_SECRET environment variable (Base64-encoded,
 *       decoded length must be ≥ 32 bytes for HMAC-SHA256).</li>
 *   <li>{@code app.jwt.expiration-ms} → JWT_EXPIRATION environment variable
 *       (milliseconds; default 86400000 = 24 h via application.yml).</li>
 * </ul>
 *
 * <p>Validates at application startup. Fails fast if the secret is missing or too short.
 * The raw secret value is never printed to logs.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private static final Logger log = LoggerFactory.getLogger(JwtProperties.class);

    /** Minimum decoded secret length in bytes required for HMAC-SHA256. */
    private static final int MINIMUM_SECRET_BYTES = 32;

    /** Base64-encoded JWT signing secret — injected from JWT_SECRET environment variable. */
    private String secret;

    /** Token validity period in milliseconds. */
    private long expirationMs;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    /**
     * Validates JWT configuration at startup.
     * Fails the application boot if the secret is absent or the decoded key is too short.
     * Never prints the secret value.
     */
    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured. Set the JWT_SECRET environment variable. " +
                    "Generate a strong secret with: openssl rand -base64 48");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(secret);
            if (decoded.length < MINIMUM_SECRET_BYTES) {
                throw new IllegalStateException(
                        "JWT secret is too short. Decoded length is " + decoded.length +
                        " bytes but HMAC-SHA256 requires at least " + MINIMUM_SECRET_BYTES + " bytes.");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "JWT secret is not valid Base64. Ensure JWT_SECRET contains a properly " +
                    "Base64-encoded value. Generate one with: openssl rand -base64 48", e);
        }

        if (expirationMs <= 0) {
            throw new IllegalStateException(
                    "JWT expiration (app.jwt.expiration-ms) must be a positive value in milliseconds.");
        }

        log.info("JWT configuration validated: expiration={}ms ({} hours)",
                expirationMs, expirationMs / 3_600_000L);
    }
}
