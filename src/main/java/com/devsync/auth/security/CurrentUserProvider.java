package com.devsync.auth.security;

import com.devsync.common.exception.UnauthorizedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Centralised accessor for the authenticated {@link DevSyncPrincipal} stored in
 * the {@link SecurityContextHolder} after JWT validation.
 *
 * <p>Business-layer services inject this component instead of reading from
 * {@code SecurityContextHolder} directly, keeping the identity-extraction logic
 * in a single, testable place.
 *
 * <p>Security invariants:
 * <ul>
 *   <li>Identity is always derived from the verified, filter-populated principal —
 *       never from request parameters or any client-supplied value.</li>
 *   <li>An absent, anonymous, or incorrectly typed principal results in an
 *       {@link UnauthorizedException}, not a silent fallback.</li>
 *   <li>The only safe method to call without authentication present is
 *       {@link #isAuthenticated()}, which returns {@code false} rather than throwing.</li>
 * </ul>
 *
 * <p>Phase A3 note: used directly by {@link SecurityService} and will be injected
 * into service-layer classes that need to enforce resource ownership in Phase A3.2+.
 */
@Component
public class CurrentUserProvider {

    /**
     * Returns the authenticated user's UUID.
     *
     * @return the {@code userId} from the verified JWT principal
     * @throws UnauthorizedException if no authenticated {@link DevSyncPrincipal} is present
     */
    public UUID getCurrentUserId() {
        return getVerifiedPrincipal().getUserId();
    }

    /**
     * Returns the authenticated user's email address.
     *
     * @return the {@code email} from the verified JWT principal
     * @throws UnauthorizedException if no authenticated {@link DevSyncPrincipal} is present
     */
    public String getCurrentUserEmail() {
        return getVerifiedPrincipal().getEmail();
    }

    /**
     * Returns the full {@link DevSyncPrincipal} for the current request.
     *
     * @return the authenticated principal
     * @throws UnauthorizedException if no authenticated {@link DevSyncPrincipal} is present
     */
    public DevSyncPrincipal getCurrentPrincipal() {
        return getVerifiedPrincipal();
    }

    /**
     * Returns {@code true} if the current security context holds a valid,
     * authenticated {@link DevSyncPrincipal}. Never throws.
     *
     * @return {@code true} when a verified principal is present, {@code false} otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getPrincipal() instanceof DevSyncPrincipal;
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    /**
     * Extracts and type-checks the {@link DevSyncPrincipal} from the security context.
     *
     * @throws UnauthorizedException on any authentication gap
     */
    private DevSyncPrincipal getVerifiedPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UnauthorizedException("Authentication required: no security context present");
        }

        if (!authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Authentication required: request is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof DevSyncPrincipal devSyncPrincipal)) {
            throw new UnauthorizedException(
                    "Authentication required: unrecognised principal type — " +
                    principal.getClass().getSimpleName());
        }

        return devSyncPrincipal;
    }
}

