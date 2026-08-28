package com.devsync.team.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamServiceImpl(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        User creatorUser = userRepository.findById(request.getCreatorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found with id: " + request.getCreatorUserId()));

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
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return TeamResponse.fromEntity(team);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponse> getTeams(Pageable pageable) {
        return teamRepository.findAll(pageable)
                .map(TeamResponse::fromEntity);
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(UUID id, UpdateTeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

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
    public TeamMemberResponse addMember(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (teamMemberRepository.existsByUserIdAndTeamId(userId, teamId)) {
            throw new BadRequestException("User is already a member of team '" + team.getName() + "'");
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
    @Transactional(readOnly = true)
    public Page<TeamMemberResponse> getTeamMembers(UUID teamId, Pageable pageable) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }
        return teamMemberRepository.findByTeamId(teamId, pageable)
                .map(TeamMemberResponse::fromEntity);
    }
}
