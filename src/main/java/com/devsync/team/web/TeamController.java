package com.devsync.team.web;

import com.devsync.common.response.ApiResponse;
import com.devsync.team.dto.*;
import com.devsync.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@Tag(name = "Team Management", description = "Endpoints for creating, retrieving, updating teams and team memberships")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @Operation(summary = "Create a new team", description = "Create a team and assign the creator as OWNER transactionally")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        TeamResponse team = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created successfully", team));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID", description = "Retrieve a team by UUID")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable UUID id) {
        TeamResponse team = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.success(team));
    }

    @GetMapping
    @Operation(summary = "Get paginated teams", description = "Retrieve teams list with pagination support")
    public ResponseEntity<ApiResponse<Page<TeamResponse>>> getTeams(Pageable pageable) {
        Page<TeamResponse> teams = teamService.getTeams(pageable);
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update team details", description = "Update team name or description by UUID")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTeamRequest request) {
        TeamResponse updatedTeam = teamService.updateTeam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", updatedTeam));
    }

    @PostMapping("/{teamId}/members/{userId}")
    @Operation(summary = "Add member to team", description = "Add a user to a team with default MEMBER role")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> addMember(
            @PathVariable UUID teamId,
            @PathVariable UUID userId) {
        TeamMemberResponse member = teamService.addMember(teamId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added to team successfully", member));
    }

    @GetMapping("/{teamId}/members")
    @Operation(summary = "Get team members", description = "Retrieve paginated list of team members for a team")
    public ResponseEntity<ApiResponse<Page<TeamMemberResponse>>> getTeamMembers(
            @PathVariable UUID teamId,
            Pageable pageable) {
        Page<TeamMemberResponse> members = teamService.getTeamMembers(teamId, pageable);
        return ResponseEntity.ok(ApiResponse.success(members));
    }
}
