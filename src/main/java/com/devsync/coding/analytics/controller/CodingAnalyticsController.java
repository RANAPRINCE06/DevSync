package com.devsync.coding.analytics.controller;

import com.devsync.coding.analytics.dto.*;
import com.devsync.coding.analytics.service.CodingAnalyticsService;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/coding/analytics")
@Tag(name = "Coding Analytics", description = "Endpoints for advanced competitive programming analytics and performance insights")
public class CodingAnalyticsController {

    private final CodingAnalyticsService codingAnalyticsService;

    public CodingAnalyticsController(CodingAnalyticsService codingAnalyticsService) {
        this.codingAnalyticsService = codingAnalyticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get analytics overview", description = "High-level summary of coding statistics, connected platforms, and snapshot coverage")
    public ResponseEntity<ApiResponse<CodingAnalyticsOverviewResponse>> getOverview() {
        CodingAnalyticsOverviewResponse response = codingAnalyticsService.getOverview();
        return ResponseEntity.ok(ApiResponse.success("Analytics overview retrieved successfully", response));
    }

    @GetMapping("/rating-history")
    @Operation(summary = "Get rating progression history", description = "Historical competitive rating progression per platform without artificial cross-platform merging")
    public ResponseEntity<ApiResponse<RatingHistoryResponse>> getRatingHistory(
            @RequestParam(required = false) CodingPlatform platform,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String period) {
        RatingHistoryResponse response = codingAnalyticsService.getRatingHistory(platform, from, to, period);
        return ResponseEntity.ok(ApiResponse.success("Rating history retrieved successfully", response));
    }

    @GetMapping("/problem-distribution")
    @Operation(summary = "Get problem-solving distribution", description = "Breakdown of solved problems by difficulty tier and platform with historical deltas")
    public ResponseEntity<ApiResponse<ProblemDistributionResponse>> getProblemDistribution(
            @RequestParam(required = false) CodingPlatform platform,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String period) {
        ProblemDistributionResponse response = codingAnalyticsService.getProblemDistribution(platform, from, to, period);
        return ResponseEntity.ok(ApiResponse.success("Problem distribution retrieved successfully", response));
    }

    @GetMapping("/activity")
    @Operation(summary = "Get coding snapshot activity timeline", description = "Periodic snapshot collection timeline and coverage metrics")
    public ResponseEntity<ApiResponse<CodingActivityResponse>> getActivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String period) {
        CodingActivityResponse response = codingAnalyticsService.getActivity(from, to, period);
        return ResponseEntity.ok(ApiResponse.success("Coding activity retrieved successfully", response));
    }

    @GetMapping("/contest-performance")
    @Operation(summary = "Get contest performance metrics", description = "Contest participation totals, rating histories, and performance trajectories for contest platforms")
    public ResponseEntity<ApiResponse<ContestPerformanceResponse>> getContestPerformance(
            @RequestParam(required = false) CodingPlatform platform,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String period) {
        ContestPerformanceResponse response = codingAnalyticsService.getContestPerformance(platform, from, to, period);
        return ResponseEntity.ok(ApiResponse.success("Contest performance retrieved successfully", response));
    }

    @GetMapping("/insights")
    @Operation(summary = "Get performance insights", description = "Factual, data-backed analytical insights derived strictly from persisted historical snapshots")
    public ResponseEntity<ApiResponse<PerformanceInsightsResponse>> getInsights(
            @RequestParam(required = false) CodingPlatform platform,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String period) {
        PerformanceInsightsResponse response = codingAnalyticsService.getInsights(platform, from, to, period);
        return ResponseEntity.ok(ApiResponse.success("Performance insights retrieved successfully", response));
    }
}
