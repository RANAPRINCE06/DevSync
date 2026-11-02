package com.devsync.auth.security;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Centralised ownership and access-control helper for method-level security expressions.
 *
 * <p>Annotate service or controller methods with
 * {@code @PreAuthorize("@securityService.isCurrentUser(#userId)")} to enforce that
 * the caller owns the resource identified by {@code userId}.
 *
 * <p>Spring resolves the {@code @securityService} SpEL bean reference using the default
 * bean name derived from {@link Service} — {@code securityService}.
 * <strong>Do not rename this class or its Spring bean without updating all
 * {@code @PreAuthorize} expressions.</strong>
 *
 * <p>Security invariants:
 * <ul>
 *   <li>Ownership decisions are always based on the verified JWT principal —
 *       never on a user ID supplied by the HTTP client.</li>
 *   <li>{@link #isCurrentUser(UUID)} returns {@code false} (not an exception) when the
 *       caller is unauthenticated so that {@code @PreAuthorize} can cleanly deny access
 *       without propagating a checked exception through the SpEL evaluator.</li>
 *   <li>All identity retrieval delegates to {@link CurrentUserProvider}, keeping
 *       principal-extraction logic in one place.</li>
 * </ul>
 *
 * <p>Phase A3 note: Per-module ownership enforcement (tasks, goals, teams) will be
 * added in Phase A3.2+ using {@code @PreAuthorize} expressions backed by this service.
 */
@Service
public class SecurityService {

    private final CurrentUserProvider currentUserProvider;

    public SecurityService(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Returns {@code true} if {@code userId} matches the authenticated caller's UUID.
     *
     * <p>Returns {@code false} — without throwing — when the caller is unauthenticated.
     * This allows {@code @PreAuthorize} to handle the denial cleanly.
     *
     * @param userId the resource owner's UUID to compare against the caller
     * @return {@code true} if the caller is authenticated and owns the resource
     */
    public boolean isCurrentUser(UUID userId) {
        if (!currentUserProvider.isAuthenticated()) {
            return false;
        }
        UUID callerId = currentUserProvider.getCurrentUserId();
        return callerId != null && callerId.equals(userId);
    }

    /**
     * Returns the authenticated caller's UUID.
     *
     * <p>Delegates to {@link CurrentUserProvider#getCurrentUserId()}.
     *
     * @return the caller's UUID from the verified JWT principal
     * @throws com.devsync.common.exception.UnauthorizedException if not authenticated
     */
    public UUID getCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    /**
     * Returns {@code true} if the current request carries a valid, authenticated principal.
     *
     * <p>Delegates to {@link CurrentUserProvider#isAuthenticated()}.
     *
     * @return {@code true} when authenticated, {@code false} otherwise — never throws
     */
    public boolean isAuthenticated() {
        return currentUserProvider.isAuthenticated();
    }
}

