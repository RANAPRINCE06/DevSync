package com.devsync.coding.controller;

import com.devsync.coding.dto.CodingStatisticsResponse;
import com.devsync.coding.dto.ConnectPlatformRequest;
import com.devsync.coding.dto.PlatformAccountResponse;
import com.devsync.coding.dto.PlatformMetadataResponse;
import com.devsync.coding.dto.PlatformStatSnapshotResponse;
import com.devsync.coding.dto.ProfilePreviewRequest;
import com.devsync.coding.dto.ProfilePreviewResponse;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.service.CodingProfileService;
import com.devsync.coding.service.CodingStatisticsService;
import com.devsync.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController("codingIntelligenceController")
@RequestMapping("/api/v1/coding")
@Tag(name = "Coding Intelligence", description = "Endpoints for competitive programming platform connections, synchronization, and statistics")
public class CodingProfileController {

    private final CodingProfileService codingProfileService;
    private final CodingStatisticsService codingStatisticsService;

    public CodingProfileController(CodingProfileService codingProfileService,
                                   CodingStatisticsService codingStatisticsService) {
        this.codingProfileService = codingProfileService;
        this.codingStatisticsService = codingStatisticsService;
    }

    @GetMapping("/platforms")
    @Operation(summary = "List supported platforms and capabilities", description = "Retrieve all available competitive programming platforms with support flags and capabilities")
    public ResponseEntity<ApiResponse<List<PlatformMetadataResponse>>> getSupportedPlatforms() {
        List<PlatformMetadataResponse> platforms = Arrays.stream(CodingPlatform.values())
                .map(PlatformMetadataResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(platforms));
    }

    @PostMapping("/accounts/preview")
    @Operation(summary = "Preview platform profile from URL", description = "Parses profile URL, detects platform, extracts username, and fetches live public profile preview without persisting")
    public ResponseEntity<ApiResponse<ProfilePreviewResponse>> previewProfile(
            @Valid @RequestBody ProfilePreviewRequest request) {
        ProfilePreviewResponse response = codingProfileService.previewProfile(request.getProfileUrl());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/accounts")
    @Operation(summary = "Connect a platform account", description = "Link a new public competitive programming or developer platform profile for the authenticated user")
    public ResponseEntity<ApiResponse<PlatformAccountResponse>> connectPlatform(
            @Valid @RequestBody ConnectPlatformRequest request) {
        PlatformAccountResponse response = codingProfileService.connectPlatform(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Platform account connected successfully", response));
    }

    @GetMapping("/accounts")
    @Operation(summary = "List connected accounts", description = "Retrieve all active connected coding platform accounts for the authenticated user")
    public ResponseEntity<ApiResponse<List<PlatformAccountResponse>>> getConnectedAccounts() {
        List<PlatformAccountResponse> response = codingProfileService.getConnectedAccounts();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "Get account details", description = "Retrieve details and latest snapshot for a specific connected platform account")
    public ResponseEntity<ApiResponse<PlatformAccountResponse>> getAccountById(
            @PathVariable UUID accountId) {
        PlatformAccountResponse response = codingProfileService.getAccountById(accountId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/accounts/{accountId}")
    @Operation(summary = "Disconnect account", description = "Deactivate a connected platform account while preserving historical snapshots")
    public ResponseEntity<ApiResponse<Void>> disconnectAccount(
            @PathVariable UUID accountId) {
        codingProfileService.disconnectAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success("Platform account disconnected successfully", null));
    }

    @PostMapping("/accounts/{accountId}/sync")
    @Operation(summary = "Synchronize platform account", description = "Fetch and update statistics from the external platform for an owned account")
    public ResponseEntity<ApiResponse<PlatformAccountResponse>> syncAccount(
            @PathVariable UUID accountId) {
        PlatformAccountResponse response = codingProfileService.syncAccount(accountId);
        String message = response.getSyncStatus() == com.devsync.coding.enums.SyncStatus.SUCCESS
                ? "Platform account synchronized successfully"
                : "Platform synchronization encountered an error: " + response.getLastSyncError();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get coding statistics", description = "Retrieve aggregated statistics across all connected platform accounts for the authenticated user")
    public ResponseEntity<ApiResponse<CodingStatisticsResponse>> getCodingStatistics() {
        CodingStatisticsResponse response = codingStatisticsService.getAggregatedStatistics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accounts/{accountId}/history")
    @Operation(summary = "Get account history", description = "Retrieve paginated historical statistical snapshots for a specific platform account")
    public ResponseEntity<ApiResponse<Page<PlatformStatSnapshotResponse>>> getAccountHistory(
            @PathVariable UUID accountId,
            @ParameterObject @PageableDefault(size = 20, sort = "snapshotAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PlatformStatSnapshotResponse> response = codingProfileService.getAccountHistory(accountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
