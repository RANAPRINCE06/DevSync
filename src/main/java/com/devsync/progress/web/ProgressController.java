package com.devsync.progress.web;

import com.devsync.common.response.ApiResponse;
import com.devsync.progress.dto.CreateProgressRequest;
import com.devsync.progress.dto.ProgressResponse;
import com.devsync.progress.dto.UpdateProgressRequest;
import com.devsync.progress.entity.ProgressStatus;
import com.devsync.progress.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@Tag(name = "Daily Progress Management", description = "Endpoints for creating, retrieving, updating, and filtering daily progress entries")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping
    @Operation(summary = "Create daily progress entry", description = "Record daily learning, completed tasks, study time, and next plan")
    public ResponseEntity<ApiResponse<ProgressResponse>> createProgress(@Valid @RequestBody CreateProgressRequest request) {
        ProgressResponse response = progressService.createProgress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Daily progress created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get progress by ID", description = "Retrieve a daily progress entry by UUID")
    public ResponseEntity<ApiResponse<ProgressResponse>> getProgressById(@PathVariable UUID id) {
        ProgressResponse response = progressService.getProgressById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update progress entry", description = "Update what was studied, completed tasks, study minutes, or status")
    public ResponseEntity<ApiResponse<ProgressResponse>> updateProgress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProgressRequest request) {
        ProgressResponse response = progressService.updateProgress(id, request);
        return ResponseEntity.ok(ApiResponse.success("Daily progress updated successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated progress entries", description = "Filter daily progress entries by user, team, date range, or status with pagination")
    public ResponseEntity<ApiResponse<Page<ProgressResponse>>> getProgressList(
            @RequestParam(required = false) @Parameter(description = "Filter by User UUID") UUID userId,
            @RequestParam(required = false) @Parameter(description = "Filter by Team UUID") UUID teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Filter by exact date (YYYY-MM-DD)", example = "2026-08-28") LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Filter by starting date (inclusive)", example = "2026-08-01") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Filter by ending date (inclusive)", example = "2026-08-31") LocalDate toDate,
            @RequestParam(required = false) @Parameter(description = "Filter by progress status (IN_PROGRESS, COMPLETED, PARTIAL)") ProgressStatus status,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "progressDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<ProgressResponse> page = progressService.getProgressList(userId, teamId, date, fromDate, toDate, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
