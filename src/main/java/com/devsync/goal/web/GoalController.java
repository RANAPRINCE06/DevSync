package com.devsync.goal.web;

import com.devsync.common.response.ApiResponse;
import com.devsync.goal.dto.CreateGoalRequest;
import com.devsync.goal.dto.GoalResponse;
import com.devsync.goal.dto.UpdateGoalRequest;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import com.devsync.goal.service.GoalService;
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
@RequestMapping("/api/v1/goals")
@Tag(name = "Goal Management", description = "Endpoints for creating, retrieving, updating, soft-deleting, and filtering goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    @Operation(summary = "Create a goal", description = "Create a new learning/development goal for a team")
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@Valid @RequestBody CreateGoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID", description = "Retrieve a goal by its UUID")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalById(@PathVariable UUID id) {
        GoalResponse response = goalService.getGoalById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a goal", description = "Update title, description, dates, priority, progress percentage, or status")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGoalRequest request) {
        GoalResponse response = goalService.updateGoal(id, request);
        return ResponseEntity.ok(ApiResponse.success("Goal updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Soft delete a goal", description = "Deactivate a goal by setting active to false")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable UUID id) {
        goalService.deactivateGoal(id);
        return ResponseEntity.ok(ApiResponse.success("Goal deactivated successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get paginated goals", description = "Filter goals by owner, team, status, priority, active state, or date range with pagination")
    public ResponseEntity<ApiResponse<Page<GoalResponse>>> getGoals(
            @RequestParam(required = false) @Parameter(description = "Filter by Owner UUID") UUID ownerId,
            @RequestParam(required = false) @Parameter(description = "Filter by Team UUID") UUID teamId,
            @RequestParam(required = false) @Parameter(description = "Filter by goal status") GoalStatus status,
            @RequestParam(required = false) @Parameter(description = "Filter by goal priority") GoalPriority priority,
            @RequestParam(required = false) @Parameter(description = "Filter by active state (default true)") Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Filter goals starting on or after this date (YYYY-MM-DD)", example = "2026-09-01") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Filter goals targeting on or before this date (YYYY-MM-DD)", example = "2026-12-31") LocalDate targetDate,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<GoalResponse> page = goalService.getGoals(ownerId, teamId, status, priority, active, startDate, targetDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
