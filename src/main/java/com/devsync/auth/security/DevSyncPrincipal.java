package com.devsync.auth.security;

import java.util.UUID;

/**
 * Immutable principal placed into the Spring SecurityContext after successful JWT validation.
 *
 * <p>Carries the verified user UUID and email extracted from the token claims.
 * Business logic can retrieve this from the SecurityContext without hitting the database.
 *
 * <p>Phase A3 note: Object-level authorization will use {@code userId} to verify
 * that the caller owns the requested resource (task, goal, team, etc.).
 */
public final class DevSyncPrincipal {

    private final UUID userId;
    private final String email;

    public DevSyncPrincipal(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        // Never include sensitive data in toString; userId is an opaque UUID safe to log.
        return "DevSyncPrincipal{userId=" + userId + "}";
    }
}

