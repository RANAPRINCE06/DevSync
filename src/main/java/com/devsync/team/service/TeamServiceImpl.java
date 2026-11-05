package com.devsync.team.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.team.dto.*;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.entity.TeamRole;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.repository.TeamRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TeamAuthorizationService teamAuthorizationService;

    public TeamServiceImpl(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            TeamAuthorizationService teamAuthorizationService) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.teamAuthorizationService = teamAuthorizationService;
    }

    @Override
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        if (request.getCreatorUserId() != null && !request.getCreatorUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Cannot create a team for another user");
        }

        User creatorUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found with id: " + currentUserId));

        String trimmedName = request.getName().trim();
        if (teamRepository.existsByName(trimmedName)) {
            throw new BadRequestException("Team with name '" + trimmedName + "' already exists");
        }

        Team team = Team.builder()
                .name(trimmedName)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .active(true)
                .build();

        Team savedTeam = teamRepository.save(team);

        TeamMember ownerMember = TeamMember.builder()
                .user(creatorUser)
                .team(savedTeam)
                .role(TeamRole.OWNER)
                .active(true)
                .build();

        teamMemberRepository.save(ownerMember);

        return TeamResponse.fromEntity(savedTeam);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(UUID id) {
        TeamMember membership = teamAuthorizationService.requireActiveMember(id);
        Team team = membership.getTeam();
        if (!team.isActive()) {
            throw new ResourceNotFoundException("Team not found with id: " + id);
        }
        return TeamResponse.fromEntity(team);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponse> getTeams(Pageable pageable) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return teamRepository.findActiveTeamsByUserId(currentUserId, pageable)
                .map(TeamResponse::fromEntity);
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(UUID id, UpdateTeamRequest request) {
        TeamMember membership = teamAuthorizationService.requireCanManageTeam(id);
        Team team = membership.getTeam();
        if (!team.isActive()) {
            throw new ResourceNotFoundException("Team not found with id: " + id);
        }

        String trimmedName = request.getName().trim();
        if (!team.getName().equalsIgnoreCase(trimmedName) && teamRepository.existsByName(trimmedName)) {
            throw new BadRequestException("Team with name '" + trimmedName + "' already exists");
        }

        team.setName(trimmedName);
        team.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Team updatedTeam = teamRepository.save(team);
        return TeamResponse.fromEntity(updatedTeam);
    }

    @Override
    @Transactional
    public void deleteTeam(UUID id) {
        TeamMember membership = teamAuthorizationService.requireTeamOwner(id);
        Team team = membership.getTeam();
        team.setActive(false);
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public TeamMemberResponse addMember(UUID teamId, UUID userId) {
        TeamMember callerMembership = teamAuthorizationService.requireCanManageTeam(teamId);
        Team team = callerMembership.getTeam();
        if (!team.isActive()) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Optional<TeamMember> existingOpt = teamMemberRepository.findByUserIdAndTeamId(userId, teamId);
        if (existingOpt.isPresent()) {
            TeamMember existing = existingOpt.get();
            if (existing.isActive()) {
                throw new BadRequestException("User is already a member of team '" + team.getName() + "'");
            }
            existing.setActive(true);
            existing.setRole(TeamRole.MEMBER);
            TeamMember savedMember = teamMemberRepository.save(existing);
            return TeamMemberResponse.fromEntity(savedMember);
        }

        TeamMember member = TeamMember.builder()
                .user(user)
                .team(team)
                .role(TeamRole.MEMBER)
                .active(true)
                .build();

        TeamMember savedMember = teamMemberRepository.save(member);
        return TeamMemberResponse.fromEntity(savedMember);
    }

    @Override
    @Transactional
    public void removeMember(UUID teamId, UUID userId) {
        TeamMember callerMembership = teamAuthorizationService.requireCanManageTeam(teamId);

        TeamMember targetMember = teamMemberRepository.findByUserIdAndTeamId(userId, teamId)
                .filter(TeamMember::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in team with user id: " + userId));

        if (targetMember.getRole() == TeamRole.OWNER) {
            throw new BadRequestException("Cannot remove the team owner");
        }

        if (callerMembership.getRole() == TeamRole.ADMIN && targetMember.getRole() == TeamRole.ADMIN
                && !callerMembership.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Team admins cannot remove other admins");
        }

        targetMember.setActive(false);
        teamMemberRepository.save(targetMember);
    }

    @Override
    @Transactional
    public TeamMemberResponse updateMemberRole(UUID teamId, UUID userId, UpdateTeamMemberRoleRequest request) {
        TeamMember callerMembership = teamAuthorizationService.requireTeamOwner(teamId);

        TeamMember targetMember = teamMemberRepository.findByUserIdAndTeamId(userId, teamId)
                .filter(TeamMember::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in team with user id: " + userId));

        UUID currentUserId = callerMembership.getUser().getId();
        if (currentUserId.equals(userId) && request.getRole() != TeamRole.OWNER) {
            throw new BadRequestException("Team owner cannot demote themselves without transferring ownership");
        }

        if (request.getRole() == TeamRole.OWNER && !currentUserId.equals(userId)) {
            // Transferring ownership: demote former owner to ADMIN
            callerMembership.setRole(TeamRole.ADMIN);
            teamMemberRepository.save(callerMembership);
        }

        targetMember.setRole(request.getRole());
        TeamMember updated = teamMemberRepository.save(targetMember);
        return TeamMemberResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamMemberResponse> getTeamMembers(UUID teamId, Pageable pageable) {
        teamAuthorizationService.requireActiveMember(teamId);
        return teamMemberRepository.findByTeamIdAndActiveTrue(teamId, pageable)
                .map(TeamMemberResponse::fromEntity);
    }
}
