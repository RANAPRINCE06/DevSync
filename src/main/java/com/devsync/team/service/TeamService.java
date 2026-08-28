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

    TeamMemberResponse addMember(UUID teamId, UUID userId);

    Page<TeamMemberResponse> getTeamMembers(UUID teamId, Pageable pageable);
}
