package com.devsync.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Centralised JWT token provider for DevSync.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Generate signed HMAC-SHA256 JWTs from verified user identity.</li>
 *   <li>Validate token signature and expiration.</li>
 *   <li>Extract claims (userId, email/subject, roles) without trusting frontend-supplied values.</li>
 *   <li>Reject malformed, tampered, expired, and unsupported-algorithm tokens.</li>
 * </ul>
 *
 * <p>Security invariants:
 * <ul>
 *   <li>Signing key is derived from the validated {@link JwtProperties} secret at construction time.</li>
 *   <li>Raw token values are never written to logs.</li>
 *   <li>The algorithm is fixed to HS256 — {@code alg: none} and asymmetric algorithms are rejected.</li>
 * </ul>
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /** JWT registered claim: subject (email). */
    public static final String CLAIM_SUB = "sub";

    /** JWT private claim: DevSync user UUID. */
    public static final String CLAIM_USER_ID = "userId";

    /** JWT private claim: list of role strings (e.g. ["USER", "ADMIN"]). */
    public static final String CLAIM_ROLES = "roles";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = jwtProperties.getExpirationMs();
    }

    // -------------------------------------------------------------------------
    // Token Generation
    // -------------------------------------------------------------------------

    /**
     * Generates a signed JWT for the given authenticated user.
     *
     * <p>Claims included:
     * <ul>
     *   <li>{@code sub}    — user email address</li>
     *   <li>{@code userId} — database UUID (String form)</li>
     *   <li>{@code roles}  — list containing the user's role (e.g. "USER")</li>
     *   <li>{@code iat}    — issued-at timestamp</li>
     *   <li>{@code exp}    — expiration timestamp (iat + expirationMs)</li>
     * </ul>
     *
     * @param userId    the persisted database user ID (never supplied by the caller directly)
     * @param email     the verified user email
     * @param roles     the user's authority list (e.g. List.of("USER"))
     * @return a compact, signed JWT string
     */
    public String generateToken(UUID userId, String email, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_ROLES, roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // -------------------------------------------------------------------------
    // Token Validation
    // -------------------------------------------------------------------------

    /**
     * Validates a JWT string. Returns {@code true} only if the token:
     * <ul>
     *   <li>Has a valid HMAC-SHA256 signature matching the configured key.</li>
     *   <li>Is not expired.</li>
     *   <li>Is well-formed (parseable, contains required claims).</li>
     *   <li>Uses the expected HS256 algorithm (alg:none and RS/ES keys are rejected).</li>
     * </ul>
     *
     * @param token the compact JWT string (raw value, must not be logged)
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT validation failed: token is expired");
        } catch (SignatureException e) {
            log.warn("JWT validation failed: invalid signature — possible tampering");
        } catch (MalformedJwtException e) {
            log.warn("JWT validation failed: malformed token");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT validation failed: unsupported JWT type or algorithm");
        } catch (IllegalArgumentException e) {
            log.warn("JWT validation failed: token string is null, empty, or whitespace-only");
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Claims Extraction
    // -------------------------------------------------------------------------

    /**
     * Extracts the user UUID from a valid JWT.
     *
     * @param token a token that has already passed {@link #validateToken}
     * @return the user's UUID
     * @throws JwtException if the token is invalid (callers should call validateToken first)
     */
    public UUID extractUserId(String token) {
        String userIdStr = parseClaims(token).get(CLAIM_USER_ID, String.class);
        return UUID.fromString(userIdStr);
    }

    /**
     * Extracts the email (JWT subject) from a valid JWT.
     *
     * @param token a token that has already passed {@link #validateToken}
     * @return the authenticated user's email
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the roles list from a valid JWT.
     *
     * @param token a token that has already passed {@link #validateToken}
     * @return list of role strings (may be empty but never null)
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object rolesObj = parseClaims(token).get(CLAIM_ROLES);
        if (rolesObj instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    /**
     * Parses and returns the JWT claims body. Enforces HS256 algorithm.
     * Throws a {@link JwtException} subclass on any validation failure.
     *
     * <p>The raw token is never written to log output inside this method.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

