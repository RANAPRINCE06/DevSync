package com.devsync.team.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.entity.TeamRole;
import com.devsync.team.repository.TeamMemberRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Reusable authorization service for team membership, roles, and workspace access control.
 *
 * <p>Enforces zero client trust:
 * <ul>
 *   <li>The caller's authenticated identity is always retrieved from {@link CurrentUserProvider}.</li>
 *   <li>Membership and roles are verified against the database via {@link TeamMemberRepository}.</li>
 *   <li>Non-members attempting to access private team resources receive HTTP 404 (via
 *       {@link ResourceNotFoundException}) to eliminate information leakage.</li>
 *   <li>Authenticated members lacking necessary administrative roles receive HTTP 403 (via
 *       {@link AccessDeniedException}).</li>
 *   <li>Deactivated teams immediately block all resource access with HTTP 404.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class TeamAuthorizationService {

    private final TeamMemberRepository teamMemberRepository;
    private final CurrentUserProvider currentUserProvider;

    public TeamAuthorizationService(TeamMemberRepository teamMemberRepository, CurrentUserProvider currentUserProvider) {
        this.teamMemberRepository = teamMemberRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Checks if the currently authenticated user is an active member of the specified active team.
     * Never throws; returns false if unauthenticated, not a member, or team is inactive.
     */
    public boolean isTeamMember(UUID teamId) {
        if (!currentUserProvider.isAuthenticated() || teamId == null) {
            return false;
        }
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return teamMemberRepository.findByUserIdAndTeamId(currentUserId, teamId)
                .filter(TeamMember::isActive)
                .filter(m -> m.getTeam().isActive())
                .isPresent();
    }

    /**
     * Checks if the currently authenticated user is the OWNER of the specified active team.
     * Never throws; returns false if unauthenticated or not an owner.
     */
    public boolean isTeamOwner(UUID teamId) {
        if (!currentUserProvider.isAuthenticated() || teamId == null) {
            return false;
        }
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return teamMemberRepository.findByUserIdAndTeamId(currentUserId, teamId)
                .filter(TeamMember::isActive)
                .filter(m -> m.getTeam().isActive())
                .map(m -> m.getRole() == TeamRole.OWNER)
                .orElse(false);
    }

    /**
     * Checks if the currently authenticated user can manage the specified active team (OWNER or ADMIN).
     * Never throws; returns false if unauthenticated or insufficient role.
     */
    public boolean canManageTeam(UUID teamId) {
        if (!currentUserProvider.isAuthenticated() || teamId == null) {
            return false;
        }
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return teamMemberRepository.findByUserIdAndTeamId(currentUserId, teamId)
                .filter(TeamMember::isActive)
                .filter(m -> m.getTeam().isActive())
                .map(m -> m.getRole() == TeamRole.OWNER || m.getRole() == TeamRole.ADMIN)
                .orElse(false);
    }

    /**
     * Verifies that the caller is an active member of the specified active team.
     *
     * @param teamId the team to verify
     * @return the caller's active {@link TeamMember} record
     * @throws ResourceNotFoundException if the caller is not an active member or team is inactive (HTTP 404 policy)
     */
    public TeamMember requireActiveMember(UUID teamId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        TeamMember membership = teamMemberRepository.findByUserIdAndTeamId(currentUserId, teamId)
                .filter(TeamMember::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        if (!membership.getTeam().isActive()) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }

        return membership;
    }

    /**
     * Verifies that the caller can manage the team (OWNER or ADMIN).
     *
     * @param teamId the team to verify
     * @return the caller's active {@link TeamMember} record
     * @throws ResourceNotFoundException if the caller is not a member (HTTP 404)
     * @throws AccessDeniedException     if the caller is a member but lacks management role (HTTP 403)
     */
    public TeamMember requireCanManageTeam(UUID teamId) {
        TeamMember membership = requireActiveMember(teamId);
        if (membership.getRole() != TeamRole.OWNER && membership.getRole() != TeamRole.ADMIN) {
            throw new AccessDeniedException("Only team owners and admins can manage this team");
        }
        return membership;
    }

    /**
     * Verifies that the caller is the OWNER of the specified team.
     *
     * @param teamId the team to verify
     * @return the caller's active {@link TeamMember} record
     * @throws ResourceNotFoundException if the caller is not a member (HTTP 404)
     * @throws AccessDeniedException     if the caller is a member but not the OWNER (HTTP 403)
     */
    public TeamMember requireTeamOwner(UUID teamId) {
        TeamMember membership = requireActiveMember(teamId);
        if (membership.getRole() != TeamRole.OWNER) {
            throw new AccessDeniedException("Only the team owner can perform this operation");
        }
        return membership;
    }
}
