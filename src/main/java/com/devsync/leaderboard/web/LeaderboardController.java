package com.devsync.leaderboard.web;

import com.devsync.common.response.ApiResponse;
import com.devsync.leaderboard.dto.LeaderboardEntryResponse;
import com.devsync.leaderboard.entity.LeaderboardPeriod;
import com.devsync.leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaderboard")
@Tag(name = "Team Leaderboard Management", description = "Endpoints for computing and querying real-time team leaderboards and member rankings")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/teams/{teamId}")
    @Operation(summary = "Get team leaderboard", description = "Get ranked leaderboard of active team members for a given period (defaults to ALL_TIME)")
    public ResponseEntity<ApiResponse<Page<LeaderboardEntryResponse>>> getTeamLeaderboard(
            @PathVariable UUID teamId,
            @RequestParam(required = false, defaultValue = "ALL_TIME") @Parameter(description = "Leaderboard period (DAILY, WEEKLY, MONTHLY, ALL_TIME)") LeaderboardPeriod period,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "score", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<LeaderboardEntryResponse> page = leaderboardService.getTeamLeaderboard(teamId, period, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/teams/{teamId}/me/{userId}")
    @Operation(summary = "Get user ranking in team", description = "Retrieve rank and score breakdown for a specific user in a team")
    public ResponseEntity<ApiResponse<LeaderboardEntryResponse>> getUserRanking(
            @PathVariable UUID teamId,
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "ALL_TIME") @Parameter(description = "Leaderboard period") LeaderboardPeriod period) {

        LeaderboardEntryResponse response = leaderboardService.getUserRanking(teamId, userId, period);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/teams/{teamId}/period/{period}")
    @Operation(summary = "Get leaderboard for explicit period", description = "Retrieve ranked team leaderboard for DAILY, WEEKLY, MONTHLY, or ALL_TIME period")
    public ResponseEntity<ApiResponse<Page<LeaderboardEntryResponse>>> getLeaderboardForPeriod(
            @PathVariable UUID teamId,
            @PathVariable LeaderboardPeriod period,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "score", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<LeaderboardEntryResponse> page = leaderboardService.getLeaderboardForPeriod(teamId, period, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
