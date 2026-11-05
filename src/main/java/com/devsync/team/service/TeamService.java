package com.devsync.team.service;

import com.devsync.team.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TeamService {

    TeamResponse createTeam(CreateTeamRequest request);

    TeamResponse getTeamById(UUID id);

    Page<TeamResponse> getTeams(Pageable pageable);

    TeamResponse updateTeam(UUID id, UpdateTeamRequest request);

    void deleteTeam(UUID id);

    TeamMemberResponse addMember(UUID teamId, UUID userId);

    void removeMember(UUID teamId, UUID userId);

    TeamMemberResponse updateMemberRole(UUID teamId, UUID userId, UpdateTeamMemberRoleRequest request);

    Page<TeamMemberResponse> getTeamMembers(UUID teamId, Pageable pageable);
}
