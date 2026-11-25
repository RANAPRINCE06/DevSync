package com.devsync.coding.intelligence.controller;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.intelligence.dto.*;
import com.devsync.coding.intelligence.service.CodingPerformanceService;
import com.devsync.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coding")
@Tag(name = "Coding Performance & Developer Intelligence", description = "Endpoints for platform badges, contest histories, and GitHub activity telemetry")
public class CodingPerformanceController {

    private final CodingPerformanceService codingPerformanceService;

    public CodingPerformanceController(CodingPerformanceService codingPerformanceService) {
        this.codingPerformanceService = codingPerformanceService;
    }

    @GetMapping("/achievements")
    @Operation(summary = "Get platform badges & achievements", description = "Retrieve verified badges, achievements, and titles across connected coding platforms for the authenticated user")
    public ResponseEntity<ApiResponse<PlatformAchievementsSummaryResponse>> getAchievements() {
        PlatformAchievementsSummaryResponse response = codingPerformanceService.getAchievementsSummary();
        return ResponseEntity.ok(ApiResponse.success("Achievements retrieved successfully", response));
    }

    @GetMapping("/contests")
    @Operation(summary = "Get all platform contest histories", description = "Retrieve contest rating histories and participation details across all contest platforms")
    public ResponseEntity<ApiResponse<Map<CodingPlatform, PlatformContestHistoryResponse>>> getAllContests() {
        Map<CodingPlatform, PlatformContestHistoryResponse> response = codingPerformanceService.getAllContestHistories();
        return ResponseEntity.ok(ApiResponse.success("Contest histories retrieved successfully", response));
    }

    @GetMapping("/contests/{platform}")
    @Operation(summary = "Get platform-specific contest history", description = "Retrieve contest rating history and progression for an individual competitive programming platform")
    public ResponseEntity<ApiResponse<PlatformContestHistoryResponse>> getContestHistoryByPlatform(
            @PathVariable CodingPlatform platform) {
        PlatformContestHistoryResponse response = codingPerformanceService.getContestHistory(platform);
        return ResponseEntity.ok(ApiResponse.success("Contest history for " + platform + " retrieved successfully", response));
    }

    @GetMapping("/github/contributions")
    @Operation(summary = "Get GitHub contribution heatmap telemetry", description = "Retrieve 52-week contribution calendar, active days, and streak analysis")
    public ResponseEntity<ApiResponse<GitHubContributionResponse>> getGitHubContributions() {
        GitHubContributionResponse response = codingPerformanceService.getGitHubContributions();
        return ResponseEntity.ok(ApiResponse.success("GitHub contributions retrieved successfully", response));
    }

    @GetMapping("/github/commits")
    @Operation(summary = "Get GitHub commit activity", description = "Retrieve daily and monthly commit trends with repository breakdown")
    public ResponseEntity<ApiResponse<GitHubCommitActivityResponse>> getGitHubCommits(
            @RequestParam(required = false, defaultValue = "30d") String period) {
        GitHubCommitActivityResponse response = codingPerformanceService.getGitHubCommitActivity(period);
        return ResponseEntity.ok(ApiResponse.success("GitHub commit activity retrieved successfully", response));
    }

    @GetMapping("/github/repositories")
    @Operation(summary = "Get GitHub repository activity insights", description = "Retrieve top public repositories, languages, stars, forks, and commit updates")
    public ResponseEntity<ApiResponse<List<GitHubRepositoryResponse>>> getGitHubRepositories() {
        List<GitHubRepositoryResponse> response = codingPerformanceService.getGitHubRepositories();
        return ResponseEntity.ok(ApiResponse.success("GitHub repositories retrieved successfully", response));
    }
}
