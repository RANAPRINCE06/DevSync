package com.devsync.auth.security;

import com.devsync.team.service.TeamAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Centralised ownership and access-control helper for method-level security expressions.
 *
 * <p>Annotate service or controller methods with
 * {@code @PreAuthorize("@securityService.isCurrentUser(#userId)")} or
 * {@code @PreAuthorize("@securityService.isTeamMember(#teamId)")} to enforce access control.
 *
 * <p>Spring resolves the {@code @securityService} SpEL bean reference using the default
 * bean name derived from {@link Service} — {@code securityService}.
 * <strong>Do not rename this class or its Spring bean without updating all
 * {@code @PreAuthorize} expressions.</strong>
 *
 * <p>Security invariants:
 * <ul>
 *   <li>Ownership and membership decisions are always based on the verified JWT principal —
 *       never on a user ID supplied by the HTTP client.</li>
 *   <li>Methods return {@code false} (not an exception) when the caller is unauthenticated so
 *       that {@code @PreAuthorize} can cleanly deny access without propagating a checked
 *       exception through the SpEL evaluator.</li>
 *   <li>All identity retrieval delegates to {@link CurrentUserProvider}, keeping
 *       principal-extraction logic in one place.</li>
 * </ul>
 */
@Service
public class SecurityService {

    private final CurrentUserProvider currentUserProvider;
    private TeamAuthorizationService teamAuthorizationService;

    public SecurityService(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Autowired
    public void setTeamAuthorizationService(TeamAuthorizationService teamAuthorizationService) {
        this.teamAuthorizationService = teamAuthorizationService;
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
     * Returns {@code true} if the authenticated caller is an active member of {@code teamId}.
     *
     * @param teamId the team UUID
     * @return {@code true} if the caller is an active member, {@code false} otherwise
     */
    public boolean isTeamMember(UUID teamId) {
        if (teamAuthorizationService == null) {
            return false;
        }
        return teamAuthorizationService.isTeamMember(teamId);
    }

    /**
     * Returns {@code true} if the authenticated caller is the OWNER of {@code teamId}.
     *
     * @param teamId the team UUID
     * @return {@code true} if the caller is the team owner, {@code false} otherwise
     */
    public boolean isTeamOwner(UUID teamId) {
        if (teamAuthorizationService == null) {
            return false;
        }
        return teamAuthorizationService.isTeamOwner(teamId);
    }

    /**
     * Returns {@code true} if the authenticated caller can manage {@code teamId} (OWNER or ADMIN).
     *
     * @param teamId the team UUID
     * @return {@code true} if the caller has management role, {@code false} otherwise
     */
    public boolean canManageTeam(UUID teamId) {
        if (teamAuthorizationService == null) {
            return false;
        }
        return teamAuthorizationService.canManageTeam(teamId);
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
